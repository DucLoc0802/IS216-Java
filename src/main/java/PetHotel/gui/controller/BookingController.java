package PetHotel.gui.controller;

import PetHotel.bus.BookingBUS;
import PetHotel.model.Booking;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class BookingController {

    @FXML private TextField searchField;
    @FXML private TableView<Booking> bookingTable;
    @FXML private TableColumn<Booking, String> colBkId;
    @FXML private TableColumn<Booking, String> colBkPet;
    @FXML private TableColumn<Booking, String> colBkOwner;
    @FXML private TableColumn<Booking, String> colBkRoom;
    @FXML private TableColumn<Booking, String> colBkFrom;
    @FXML private TableColumn<Booking, String> colBkTo;
    @FXML private TableColumn<Booking, String> colBkNote;
    @FXML private TableColumn<Booking, String> colBkStatus;
    @FXML private TableColumn<Booking, String> colBkActions;

    @FXML private ToggleButton tabAll;
    @FXML private ToggleButton tabPending;
    @FXML private ToggleButton tabCheckedIn;
    @FXML private ToggleButton tabCheckout;
    @FXML private ToggleButton tabCancelled;

    private final BookingBUS bookingBUS = new BookingBUS();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        setupColumns();
        setupTabs();
        loadBookings(null);
    }

    private void setupColumns() {
        colBkId.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getBookingId()));
        colBkPet.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getPetName() != null ?
                d.getValue().getPetName() : "—"));
        colBkOwner.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getCustomerName() != null ?
                d.getValue().getCustomerName() : "—"));
        colBkRoom.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getRoomNumber() != null ?
                d.getValue().getRoomNumber() : "—"));
        colBkFrom.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getCheckinExpectedAt() != null ?
                d.getValue().getCheckinExpectedAt().format(dtf) : "—"));
        colBkTo.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getCheckoutExpectedAt() != null ?
                d.getValue().getCheckoutExpectedAt().format(dtf) : "—"));
        colBkNote.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getSpecialNote() != null ?
                d.getValue().getSpecialNote() : ""));
        colBkStatus.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getStatus()));

        // Cột thao tác
        colBkActions.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("...");
            {
                btn.setOnAction(e -> {
                    Booking b = getTableView().getItems().get(getIndex());
                    showActionMenu(b);
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void setupTabs() {
        tabAll.setOnAction(e -> loadBookings(null));
        tabPending.setOnAction(e -> loadBookings("PENDING"));
        tabCheckedIn.setOnAction(e -> loadBookings("CHECKED_IN"));
        tabCheckout.setOnAction(e -> loadBookings("CHECKED_OUT"));
        tabCancelled.setOnAction(e -> loadBookings("CANCELLED"));
    }

    private void loadBookings(String status) {
        try {
            List<Booking> bookings = bookingBUS.searchBookings(
                searchField != null ? searchField.getText() : "", status);
            bookingTable.setItems(FXCollections.observableArrayList(bookings));
        } catch (Exception e) {
            showAlert("Lỗi", "Không thể tải danh sách booking: " + e.getMessage());
        }
    }

    private void showActionMenu(Booking booking) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Thao tác - " + booking.getBookingId());
        alert.setHeaderText("Trạng thái hiện tại: " + booking.getStatus());

        ButtonType btnCheckin  = new ButtonType("✔ Check-in");
        ButtonType btnCheckout = new ButtonType("↩ Check-out");
        ButtonType btnCancel   = new ButtonType("✖ Hủy booking");
        ButtonType btnClose    = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnCheckin, btnCheckout, btnCancel, btnClose);
        alert.showAndWait().ifPresent(result -> {
            try {
                if (result == btnCheckin) {
                    bookingBUS.checkIn(booking.getBookingId());
                    showAlert("Thành công", "Check-in thành công!");
                } else if (result == btnCheckout) {
                    bookingBUS.checkOut(booking.getBookingId());
                    showAlert("Thành công", "Check-out thành công!");
                } else if (result == btnCancel) {
                    bookingBUS.cancelBooking(booking.getBookingId());
                    showAlert("Thành công", "Đã hủy booking!");
                }
                loadBookings(null);
            } catch (Exception e) {
                showAlert("Lỗi", e.getMessage());
            }
        });
    }

    @FXML
    public void onCreateBooking(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/PetHotel/gui/view/CreateBookingDialog.fxml"));
            javafx.scene.Parent root = loader.load();

            CreateBookingController controller = loader.getController();
            controller.setOnSaveCallback(() -> loadBookings(null));

            javafx.stage.Stage dialog = new javafx.stage.Stage();
            dialog.setTitle("Tạo Booking Mới");
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialog.setScene(new javafx.scene.Scene(root));
            dialog.showAndWait();
        } catch (Exception e) {
            showAlert("Lỗi", "Không thể mở form: " + e.getMessage());
        }
    }

    @FXML
    public void onCheckin(ActionEvent event) {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Thông báo", "Vui lòng chọn booking."); return; }
        try {
            bookingBUS.checkIn(selected.getBookingId());
            loadBookings(null);
            showAlert("Thành công", "Check-in thành công!");
        } catch (Exception e) {
            showAlert("Lỗi", e.getMessage());
        }
    }

    @FXML
    public void onCheckout(ActionEvent event) {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Thông báo", "Vui lòng chọn booking."); return; }
        try {
            bookingBUS.checkOut(selected.getBookingId());
            loadBookings(null);
            showAlert("Thành công", "Check-out thành công!");
        } catch (Exception e) {
            showAlert("Lỗi", e.getMessage());
        }
    }

    @FXML
    public void onCheckRoom(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/PetHotel/gui/view/CheckRoomDialog.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage dialog = new javafx.stage.Stage();
            dialog.setTitle("Kiểm Tra Phòng Trống");
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialog.setScene(new javafx.scene.Scene(root));
            dialog.showAndWait();
        } catch (Exception e) {
            showAlert("Lỗi", "Không thể mở: " + e.getMessage());
        }
    }

    @FXML public void onFilter(ActionEvent event) { loadBookings(null); }
    @FXML public void onTableClick(MouseEvent event) { }
    @FXML public void handleSearch(ActionEvent event) { loadBookings(null); }
    @FXML public void handleCreateBooking(ActionEvent event) { onCreateBooking(event); }
    @FXML public void handleCheckIn(ActionEvent event) { onCheckin(event); }
    @FXML public void handleCheckOut(ActionEvent event) { onCheckout(event); }
    @FXML public void handleCancelBooking(ActionEvent event) {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Thông báo", "Vui lòng chọn booking."); return; }
        try {
            bookingBUS.cancelBooking(selected.getBookingId());
            loadBookings(null);
        } catch (Exception e) {
            showAlert("Lỗi", e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}