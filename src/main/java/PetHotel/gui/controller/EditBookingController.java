package PetHotel.gui.controller;

import PetHotel.dao.BookingDAO;
import PetHotel.dao.RoomDAO;
import PetHotel.model.Booking;
import PetHotel.model.Room;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.List;

public class EditBookingController {

    @FXML private Label lblBookingId;
    @FXML private Label lblCustomer;
    @FXML private Label lblPet;
    @FXML private ComboBox<Room> cbRoom;
    @FXML private Label lblStatus;
    @FXML private DatePicker dateCheckin;
    @FXML private DatePicker dateCheckout;
    @FXML private TextField txtDeposit;
    @FXML private TextArea txtNote;
    @FXML private Label lblError;
    @FXML private Button btnCancel;

    private final BookingDAO bookingDAO = new BookingDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private Booking currentBooking;
    private Runnable onSaveCallback;

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    public void setBooking(Booking booking) {
        this.currentBooking = booking;
        lblBookingId.setText("Mã booking: " + booking.getBookingId());
        lblCustomer.setText(booking.getCustomerName() != null ? booking.getCustomerName() : "—");
        lblPet.setText(booking.getPetName() != null ? booking.getPetName() : "—");
        lblStatus.setText(booking.getStatus());
        if (booking.getCheckinExpectedAt() != null)
            dateCheckin.setValue(booking.getCheckinExpectedAt().toLocalDate());
        if (booking.getCheckoutExpectedAt() != null)
            dateCheckout.setValue(booking.getCheckoutExpectedAt().toLocalDate());
        txtDeposit.setText(booking.getDepositAmount() != null ?
            booking.getDepositAmount().toPlainString() : "0");
        txtNote.setText(booking.getSpecialNote() != null ? booking.getSpecialNote() : "");

        // Load danh sách phòng và chọn phòng hiện tại
        loadRooms();
    }

    private void loadRooms() {
        try {
            List<Room> rooms = roomDAO.search("", "AVAILABLE", null);
            Room currentRoom = roomDAO.findByBookingId(currentBooking.getBookingId());
            if (currentRoom != null) {
                // Đảm bảo phòng hiện tại có trong danh sách
                boolean found = rooms.stream().anyMatch(r -> r.getRoomId().equals(currentRoom.getRoomId()));
                if (!found) {
                    rooms.add(0, currentRoom);
                }
                cbRoom.setItems(FXCollections.observableArrayList(rooms));
                cbRoom.setValue(currentRoom);
            } else {
                cbRoom.setItems(FXCollections.observableArrayList(rooms));
            }
            cbRoom.setConverter(new javafx.util.StringConverter<>() {
                public String toString(Room r) {
                    return r == null ? "" : r.getRoomNumber() + " - " + r.getTypeName()
                        + " (" + (int)r.getBasePricePerDay() + " VNĐ/ngày)";
                }
                public Room fromString(String s) { return null; }
            });
        } catch (Exception e) {
            lblError.setText("Lỗi tải danh sách phòng: " + e.getMessage());
            lblError.setVisible(true);
        }
    }

    @FXML
    public void onSave(ActionEvent event) {
        lblError.setVisible(false);
        if (dateCheckin.getValue() == null || dateCheckout.getValue() == null) {
            lblError.setText("Vui lòng chọn đầy đủ ngày check-in và check-out.");
            lblError.setVisible(true);
            return;
        }
        if (!dateCheckout.getValue().isAfter(dateCheckin.getValue())) {
            lblError.setText("Ngày check-out phải sau ngày check-in.");
            lblError.setVisible(true);
            return;
        }
        try {
            // Cập nhật các trường thông tin
            currentBooking.setCheckinExpectedAt(
                dateCheckin.getValue().atStartOfDay().atOffset(ZoneOffset.UTC));
            currentBooking.setCheckoutExpectedAt(
                dateCheckout.getValue().atStartOfDay().atOffset(ZoneOffset.UTC));
            String dep = txtDeposit.getText().trim();
            currentBooking.setDepositAmount(dep.isEmpty() ?
                BigDecimal.ZERO : new BigDecimal(dep));
            currentBooking.setSpecialNote(txtNote.getText());

            // Lấy phòng mới được chọn từ ComboBox
            Room selectedRoom = cbRoom.getValue();
            String newRoomId = (selectedRoom != null) ? selectedRoom.getRoomId() : null;

            // Gọi DAO cập nhật cả booking và booking_room
            bookingDAO.update(currentBooking, newRoomId);

            if (onSaveCallback != null) onSaveCallback.run();
            closeDialog();
        } catch (NumberFormatException e) {
            lblError.setText("Số tiền đặt cọc không hợp lệ.");
            lblError.setVisible(true);
        } catch (Exception e) {
            lblError.setText(e.getMessage());
            lblError.setVisible(true);
        }
    }

    @FXML
    public void onCancel(ActionEvent event) { closeDialog(); }

    private void closeDialog() {
        ((Stage) btnCancel.getScene().getWindow()).close();
    }
}