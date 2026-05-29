package PetHotel.gui.controller;

import PetHotel.bus.BookingBUS;
import PetHotel.model.Booking;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class BookingController {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
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
    @FXML private Button btnEdit;
    @FXML private Button btnDeleteBooking;
    @FXML private Button btnCancel;
    @FXML private Button btnSpecialNote;
    @FXML private DatePicker filterDateFrom;
    @FXML private DatePicker filterDateTo;

    private final BookingBUS bookingBUS = new BookingBUS();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        setupColumns();
        setupTabs();
        loadBookings(null);

        ToggleGroup tabGroup = new ToggleGroup();
        tabAll.setToggleGroup(tabGroup);
        tabPending.setToggleGroup(tabGroup);
        tabCheckedIn.setToggleGroup(tabGroup);
        tabCheckout.setToggleGroup(tabGroup);
        tabCancelled.setToggleGroup(tabGroup);

        bookingTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            boolean selected = newVal != null;
            btnEdit.setDisable(!selected);
            btnDeleteBooking.setDisable(!selected);
            btnCancel.setDisable(!selected);
            if (btnSpecialNote != null) btnSpecialNote.setDisable(!selected);
        });
    }

    private void setupColumns() {
        bookingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        bookingTable.setFixedCellSize(36);
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
            bookingTable.refresh();
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
                    booking.setStatus("CHECKED_IN");
                    bookingTable.refresh();
                    showAlert("Thành công", "Check-in thành công!");
                } else if (result == btnCheckout) {
                    bookingBUS.checkOut(booking.getBookingId());
                    booking.setStatus("CHECKED_OUT");
                    bookingTable.refresh();
                    showAlert("Thành công", "Check-out thành công!");
                } else if (result == btnCancel) {
                    bookingBUS.cancelBooking(booking.getBookingId());
                    booking.setStatus("CANCELLED");
                    bookingTable.refresh();
                    showAlert("Thành công", "Đã hủy booking!");
                }
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
            selected.setStatus("CHECKED_IN");
            bookingTable.refresh();
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
            selected.setStatus("CHECKED_OUT");
            bookingTable.refresh();
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
    @FXML public void onClearFilter(ActionEvent event) {
        searchField.clear();
        filterDateFrom.setValue(null);
        filterDateTo.setValue(null);
        loadBookings(null);
    }
    @FXML public void onTableClick(MouseEvent event) { }
    @FXML public void handleSearch(ActionEvent event) { loadBookings(null); }
    @FXML public void handleCreateBooking(ActionEvent event) { onCreateBooking(event); }
    @FXML public void handleCheckIn(ActionEvent event) { onCheckin(event); }
    @FXML public void handleCheckOut(ActionEvent event) { onCheckout(event); }
    @FXML
    public void onDeleteBooking(ActionEvent event) {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Vui lòng chọn booking để xóa.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xóa booking");
        confirmAlert.setHeaderText("Xóa booking: " + selected.getBookingId());
        confirmAlert.setContentText("Bạn có chắc chắn muốn xóa vĩnh viễn booking này?");
        ButtonType btnYes = new ButtonType("Xóa");
        ButtonType btnNo = new ButtonType("Không", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(btnYes, btnNo);

        confirmAlert.showAndWait().ifPresent(result -> {
            if (result == btnYes) {
                try {
                    bookingBUS.deleteBooking(selected.getBookingId());
                    loadBookings(null);
                    showAlert("Thành công", "Đã xóa booking!");
                } catch (Exception e) {
                    showAlert("Lỗi", e.getMessage());
                }
            }
        });
    }

    @FXML public void onEditBooking(ActionEvent event) {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Vui lòng chọn booking để sửa.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PetHotel/gui/view/EditBookingDialog.fxml"));
            Parent root = loader.load();

            EditBookingController editController = loader.getController();
            editController.setBooking(selected);
            editController.setOnSaveCallback(() -> {
                loadBookings(null);
            });

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Chỉnh sửa booking - " + selected.getBookingId());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(bookingTable.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể mở cửa sổ sửa: " + e.getMessage());
        }
    }
    @FXML 
    public void handleCancelBooking(ActionEvent event) {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Thông báo", "Vui lòng chọn booking."); return; }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận hủy booking");
        confirmAlert.setHeaderText("Hủy booking: " + selected.getBookingId());
        confirmAlert.setContentText("Bạn có chắc chắn muốn hủy không?");
        ButtonType btnYes = new ButtonType("Có");
        ButtonType btnNo = new ButtonType("Không", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(btnYes, btnNo);
        
        confirmAlert.showAndWait().ifPresent(result -> {
            if (result == btnYes) {
                try {
                    bookingBUS.cancelBooking(selected.getBookingId());
                    selected.setStatus("CANCELLED");
                    bookingTable.refresh();
                    showAlert("Thành công", "Đã hủy booking!");
                } catch (Exception e) {
                    showAlert("Lỗi", e.getMessage());
                }
            }
        });
    }

    @FXML
    public void onSpecialCareNote(ActionEvent event) {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Vui lòng chọn booking.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PetHotel/gui/view/SpecialCareNoteDialog.fxml"));
            Parent root = loader.load();

            SpecialCareNoteController controller = loader.getController();
            controller.setBooking(selected);
            controller.setOnSaveCallback(() -> {
                selected.setSpecialNote(selected.getSpecialNote()); 
                bookingTable.refresh();
            });

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ghi chú chăm sóc - " + selected.getBookingId());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(bookingTable.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            bookingTable.refresh();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể mở: " + e.getMessage());
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