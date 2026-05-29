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
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.Consumer;

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
    private Consumer<Booking> onSaveCallback;

    public void setOnSaveCallback(java.util.function.Consumer<Booking> callback) {
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
            ObservableList<Customer> allCustomers = FXCollections.observableArrayList(customers);
            cbCustomer.setItems(allCustomers);
            cbCustomer.setEditable(true);
            cbCustomer.setConverter(new javafx.util.StringConverter<>() {
                public String toString(Customer c) {
                    return c == null ? "" : c.getFullName() + " - " + c.getPhone();
                }
                public Customer fromString(String s) {
                    if (s == null || s.isEmpty()) return null;
                    return allCustomers.stream()
                        .filter(c -> (c.getFullName() + " - " + c.getPhone()).equals(s))
                        .findFirst().orElse(null);
                }
            });

            // Tìm kiếm realtime
            cbCustomer.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
                // Nếu đang chọn từ dropdown thì bỏ qua
                if (cbCustomer.getValue() != null &&
                    (cbCustomer.getValue().getFullName() + " - " + 
                    cbCustomer.getValue().getPhone()).equals(newVal)) return;

                String search = newVal == null ? "" : newVal.trim().toLowerCase();
                if (search.isEmpty()) {
                    cbCustomer.setItems(allCustomers);
                } else {
                    ObservableList<Customer> filtered = allCustomers.filtered(c ->
                        c.getFullName().toLowerCase().contains(search) ||
                        c.getPhone().contains(search)
                    );
                    cbCustomer.setItems(filtered);
                }
                cbCustomer.show();
            });

            // Khi chọn xong → load thú cưng
            cbCustomer.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) loadPets();
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
                ObservableList<String> petNames = FXCollections.observableArrayList();
                if (pets.size() >= 2) {
                    petNames.add("Tất cả");
                }
                pets.forEach(p -> petNames.add(p.getPetName()));
                cbPet.setItems(petNames);
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
        lblError.setVisible(false);

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
            List<Pet> pets = petDAO.findByCustomerId(cbCustomer.getValue().getCustomerId());

            if ("Tất cả".equals(petName)) {
                // Tạo booking với pet đầu tiên, các pet còn lại add thêm vào booking_room_pet
                String firstPetId = pets.isEmpty() ? null : pets.get(0).getPetId();
                bookingBUS.createBooking(booking, cbRoom.getValue().getRoomId(), firstPetId);
                // Add các pet còn lại
                if (pets.size() > 1) {
                    String bookingRoomId = bookingBUS.getBookingRoomId(booking.getBookingId());
                    for (int i = 1; i < pets.size(); i++) {
                        bookingBUS.addPetToBookingRoom(bookingRoomId, pets.get(i).getPetId());
                    }
                }
            } else {
                String petId = null;
                if (petName != null && !petName.isEmpty()) {
                    for (Pet p : pets) {
                        if (petName.equals(p.getPetName())) {
                            petId = p.getPetId();
                            break;
                        }
                    }
                }
                bookingBUS.createBooking(booking, cbRoom.getValue().getRoomId(), petId);
            }
            if (onSaveCallback != null) onSaveCallback.accept(booking);
            closeDialog();

        } catch (NumberFormatException e) {
            lblError.setText("Số tiền đặt cọc không hợp lệ.");
        } catch (Exception e) {
            lblError.setText(e.getMessage());
        }
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
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