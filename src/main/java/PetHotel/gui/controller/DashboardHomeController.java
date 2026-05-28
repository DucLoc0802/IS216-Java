package PetHotel.gui.controller;

import java.text.DecimalFormat;
import java.util.Map;

import PetHotel.bus.ReportBUS;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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

    private final ReportBUS reportBUS = new ReportBUS();
    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");

    @FXML
    public void initialize() {
        System.out.println("Đã load xong giao diện Dashboard Home.");
        loadStatistics();
    }

    private void loadStatistics() {
        try {
            Map<String, Number> summary = reportBUS.getDashboardSummary();
            int inUse = number(summary, "roomInUse").intValue();
            int available = number(summary, "roomAvailable").intValue();
            int maintenance = number(summary, "roomMaintenance").intValue();
            int totalRoom = number(summary, "roomTotal").intValue();
            int lowStock = number(summary, "lowStock").intValue();

            setText(statBookingTotal, String.valueOf(number(summary, "todayBooking").intValue()));
            setText(statRoomOccupied, inUse + "/" + totalRoom);
            setText(statRevenue, moneyFormat.format(number(summary, "todayRevenue").doubleValue()) + " VNĐ");
            setText(statLowStock, String.valueOf(lowStock));
            setText(statRestockNeeded, String.valueOf(lowStock));
            setText(statGroomingPending, String.valueOf(number(summary, "groomingPending").intValue()));

            setText(roomOccupied, String.valueOf(inUse));
            setText(roomAvailable, String.valueOf(available));
            setText(roomCleaning, String.valueOf(maintenance));
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể tải dữ liệu dashboard: " + ex.getMessage());
        }
    }

    private Number number(Map<String, Number> summary, String key) {
        return summary.getOrDefault(key, 0);
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
    public void onRefreshDashboard(ActionEvent event) {
        loadStatistics();
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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
