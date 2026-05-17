package PetHotel.gui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DashboardHomeController {

    // --- CÁC THẺ THỐNG KÊ (STAT CARDS) ---
    @FXML private Label statBookingTotal;
    @FXML private Label statBookingDelta;
    @FXML private Label statRoomOccupied;
    @FXML private Label statRoomPct;
    @FXML private Label statRevenue;
    @FXML private Label statRevenueDelta;
    @FXML private Label statLowStock;
    @FXML private Label statGroomingPending;
    @FXML private Label statGroomingToday;

    // --- TRẠNG THÁI PHÒNG CHI TIẾT ---
    @FXML private Label roomOccupied;
    @FXML private Label roomAvailable;
    @FXML private Label roomCleaning;

    // --- CÁC BẢNG VÀ DANH SÁCH ---
    @FXML private TableView<?> todayBookingTable;
    @FXML private VBox groomingList;
    @FXML private HBox lowStockContainer;

    @FXML
    public void initialize() {
        System.out.println("Đã load xong giao diện Dashboard Home (Nội dung chính)!");
        loadStatistics();
    }

    private void loadStatistics() {
        // Mặc định tạm thời
        if (statBookingTotal != null) statBookingTotal.setText("0");
        if (statRoomOccupied != null) statRoomOccupied.setText("0/30");
        if (statRevenue != null) statRevenue.setText("0 VNĐ");
        
        if (roomOccupied != null) roomOccupied.setText("0");
        if (roomAvailable != null) roomAvailable.setText("0");
        if (roomCleaning != null) roomCleaning.setText("0");
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ SỰ KIỆN QUICK ACTIONS
    // ==========================================

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

    // ==========================================
    // CÁC HÀM XỬ LÝ CHUYỂN TRANG XEM TẤT CẢ
    // ==========================================

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