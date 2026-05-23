package PetHotel.gui.controller;

import PetHotel.bus.BookingBUS;
import PetHotel.dao.CustomerDAO;
import PetHotel.dao.PetDAO;
import PetHotel.dao.RoomDAO;
import PetHotel.model.Booking;
import PetHotel.model.Customer;
import PetHotel.model.Pet;
import PetHotel.model.Room;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class CreateBookingController {

    @FXML private ComboBox<Customer> cbCustomer;
    @FXML private ComboBox<String> cbPet;
    @FXML private ComboBox<String> cbRoomType;
    @FXML private ComboBox<Room> cbRoom;
    @FXML private DatePicker dateCheckin;
    @FXML private DatePicker dateCheckout;
    @FXML private TextField txtDeposit;
    @FXML private TextArea txtNote;
    @FXML private Label lblError;
    @FXML private Button btnCancel;

    private final BookingBUS bookingBUS = new BookingBUS();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final PetDAO petDAO = new PetDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private Runnable onSaveCallback;

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    public void initialize() {
        loadCustomers();
        loadRoomTypes();

        dateCheckin.setValue(LocalDate.now());
        dateCheckout.setValue(LocalDate.now().plusDays(1));

        // Khi chọn loại phòng → load phòng tương ứng
        cbRoomType.setOnAction(e -> loadRooms());

        // Khi chọn khách hàng → load thú cưng
        cbCustomer.setOnAction(e -> loadPets());
    }

    private void loadCustomers() {
        try {
            List<Customer> customers = customerDAO.findAll();
            cbCustomer.setItems(FXCollections.observableArrayList(customers));
            cbCustomer.setConverter(new javafx.util.StringConverter<>() {
                public String toString(Customer c) {
                    return c == null ? "" : c.getFullName() + " - " + c.getPhone();
                }
                public Customer fromString(String s) { return null; }
            });
        } catch (Exception e) {
            lblError.setText("Lỗi tải khách hàng: " + e.getMessage());
        }
    }

    private void loadPets() {
        Customer selected = cbCustomer.getValue();
        if (selected == null) {
            cbPet.setItems(FXCollections.observableArrayList());
            cbPet.setValue(null);
            return;
        }
        try {
            List<Pet> pets = petDAO.findByCustomerId(selected.getCustomerId());
            if (pets.isEmpty()) {
                cbPet.setItems(FXCollections.observableArrayList());
                cbPet.setValue(null);
            } else {
                cbPet.setItems(FXCollections.observableArrayList(
                    pets.stream().map(Pet::getPetName).toArray(String[]::new)));
                cbPet.setValue(null);
            }
        } catch (Exception e) {
            lblError.setText("Lỗi tải thú cưng: " + e.getMessage());
        }
    }

    private void loadRoomTypes() {
        cbRoomType.setItems(FXCollections.observableArrayList(
            "Tất cả", "STANDARD", "PREMIUM", "SUITE"
        ));
        cbRoomType.setValue("Tất cả");
        loadRooms();
    }

    private void loadRooms() {
        try {
            String type = "Tất cả".equals(cbRoomType.getValue()) ? null : cbRoomType.getValue();
            List<Room> rooms = roomDAO.search("", "AVAILABLE", type);
            cbRoom.setItems(FXCollections.observableArrayList(rooms));
            cbRoom.setConverter(new javafx.util.StringConverter<>() {
                public String toString(Room r) {
                    return r == null ? "" : r.getRoomNumber() + " - " + r.getTypeName()
                        + " (" + r.getBasePricePerDay() + " VNĐ/ngày)";
                }
                public Room fromString(String s) { return null; }
            });
        } catch (Exception e) {
            lblError.setText("Lỗi tải phòng: " + e.getMessage());
        }
    }

    @FXML
    public void onSave(ActionEvent event) {
        lblError.setText("");

        // Validate
        if (cbCustomer.getValue() == null) {
            lblError.setText("Vui lòng chọn khách hàng.");
            return;
        }
        if (cbRoom.getValue() == null) {
            lblError.setText("Vui lòng chọn phòng.");
            return;
        }
        if (dateCheckin.getValue() == null) {
            lblError.setText("Vui lòng chọn ngày check-in.");
            return;
        }
        if (dateCheckout.getValue() == null) {
            lblError.setText("Vui lòng chọn ngày check-out.");
            return;
        }

        try {
            Booking booking = new Booking();
            booking.setCustomerId(cbCustomer.getValue().getCustomerId());
            booking.setCheckinExpectedAt(
                dateCheckin.getValue().atStartOfDay().atOffset(ZoneOffset.UTC));
            booking.setCheckoutExpectedAt(
                dateCheckout.getValue().atStartOfDay().atOffset(ZoneOffset.UTC));
            booking.setSpecialNote(txtNote.getText());

            // Deposit
            String depositStr = txtDeposit.getText().trim();
            booking.setDepositAmount(depositStr.isEmpty() ?
                BigDecimal.ZERO : new BigDecimal(depositStr));

            // Get selected pet name → look up petId from selected customer's pets
            String petName = cbPet.getValue();
            String petId = null;
            if (petName != null && !petName.isEmpty()) {
                List<Pet> pets = petDAO.findByCustomerId(cbCustomer.getValue().getCustomerId());
                for (Pet p : pets) {
                    if (petName.equals(p.getPetName())) {
                        petId = p.getPetId();
                        break;
                    }
                }
            }

            bookingBUS.createBooking(booking, cbRoom.getValue().getRoomId(), petId);

            if (onSaveCallback != null) onSaveCallback.run();
            closeDialog();

        } catch (NumberFormatException e) {
            lblError.setText("Số tiền đặt cọc không hợp lệ.");
        } catch (Exception e) {
            lblError.setText(e.getMessage());
        }
    }

    @FXML
    public void onCancel(ActionEvent event) {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}