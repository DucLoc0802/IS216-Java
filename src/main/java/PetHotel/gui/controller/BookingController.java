package PetHotel.gui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

public class BookingController {

    @FXML private TextField searchField;
    @FXML private TableView<?> bookingTable;

    @FXML
    public void initialize() {
        System.out.println("Đã load giao diện Quản lý Booking");
    }

    @FXML
    public void onCreateBooking(ActionEvent event) {
        showAlert("Thông báo", "Chức năng tạo booking đang phát triển.");
    }

    @FXML
    public void onCheckin(ActionEvent event) {
        showAlert("Thông báo", "Chức năng check-in đang phát triển.");
    }

    @FXML
    public void onCheckout(ActionEvent event) {
        showAlert("Thông báo", "Chức năng check-out đang phát triển.");
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

    @FXML
    public void onFilter(ActionEvent event) { }

    @FXML
    public void onTableClick(MouseEvent event) { }

    @FXML
    public void handleSearch(ActionEvent event) { }

    @FXML
    public void handleCreateBooking(ActionEvent event) { onCreateBooking(event); }

    @FXML
    public void handleCheckIn(ActionEvent event) { onCheckin(event); }

    @FXML
    public void handleCheckOut(ActionEvent event) { onCheckout(event); }

    @FXML
    public void handleCancelBooking(ActionEvent event) { }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}