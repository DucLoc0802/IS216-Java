package PetHotel.gui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DashboardHomeController {

    @FXML private Label statBookingTotal;
    @FXML private Label statRoomOccupied;
    @FXML private Label statRevenue;
    @FXML private Label statLowStock;
    @FXML private Label statRestockNeeded;
    @FXML private Label statGroomingPending;

    @FXML private Label roomOccupied;
    @FXML private Label roomAvailable;
    @FXML private Label roomCleaning;

    @FXML private TableView<?> todayBookingTable;
    @FXML private VBox groomingList;
    @FXML private HBox lowStockContainer;

    @FXML
    public void initialize() {
        System.out.println("Đã load xong giao diện Dashboard Home.");
        loadStatistics();
    }

    private void loadStatistics() {
        setText(statBookingTotal, "0");
        setText(statRoomOccupied, "0/30");
        setText(statRevenue, "0 VNĐ");
        setText(statLowStock, "—");
        setText(statRestockNeeded, "—");
        setText(statGroomingPending, "—");

        setText(roomOccupied, "0");
        setText(roomAvailable, "0");
        setText(roomCleaning, "0");
    }

    private void setText(Label label, String value) {
        if (label != null) {
            label.setText(value);
        }
    }

    @FXML
    public void onQuickCreateBooking(ActionEvent event) {
        System.out.println("Mở form Tạo Booking nhanh...");
    }

    @FXML
    public void onQuickCheckin(ActionEvent event) {
        System.out.println("Mở form Check-in...");
    }

    @FXML
    public void onQuickCheckout(ActionEvent event) {
        System.out.println("Mở form Check-out...");
    }

    @FXML
    public void onQuickCreateInvoice(ActionEvent event) {
        System.out.println("Mở form Tạo Hóa Đơn nhanh...");
    }

    @FXML
    public void onQuickAddCustomer(ActionEvent event) {
        System.out.println("Mở form Thêm Khách Hàng nhanh...");
    }

    @FXML
    public void onQuickCheckRoom(ActionEvent event) {
        System.out.println("Mở bảng Kiểm tra trạng thái phòng...");
    }

    @FXML
    public void onViewAllBooking(ActionEvent event) {
        System.out.println("Chuyển hướng sang trang Quản lý Booking chi tiết...");
    }

    @FXML
    public void onViewAllGrooming(ActionEvent event) {
        System.out.println("Chuyển hướng sang trang Quản lý Grooming chi tiết...");
    }

    @FXML
    public void onViewInventory(ActionEvent event) {
        System.out.println("Chuyển hướng sang trang Quản lý Tồn Kho...");
    }
}
