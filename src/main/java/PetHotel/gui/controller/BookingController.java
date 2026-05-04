package PetHotel.gui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class BookingController {

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbStatusFilter; // Bộ lọc: Tất cả, Đang chờ, Đã Check-in...
    @FXML private TableView<?> tableBooking;

    @FXML
    public void initialize() {
        System.out.println("Đã load giao diện Quản lý Booking");
        // cbStatusFilter.getItems().addAll("Tất cả", "Chờ Check-in", "Đang ở", "Đã trả phòng", "Đã hủy");
    }

    @FXML public void onCreateBooking(ActionEvent event) { System.out.println("Mở form Tạo Booking mới..."); }
    @FXML public void onCheckin(ActionEvent event) { System.out.println("Thực hiện Check-in..."); }
    @FXML public void onCheckout(ActionEvent event) { System.out.println("Thực hiện Check-out..."); }
    @FXML public void onCheckRoom(ActionEvent event) { System.out.println("Mở bảng Kiểm phòng..."); }
    @FXML public void onFilter(ActionEvent event) { System.out.println("Lọc danh sách Booking..."); }
    
    @FXML 
    public void onTableClick(MouseEvent event) { 
        System.out.println("Vừa click vào một dòng trong bảng Booking!"); 
    }

    @FXML
    public void handleSearch(ActionEvent event) {
        System.out.println("Lọc Booking theo từ khóa: " + txtSearch.getText());
    }

    @FXML
    public void handleCreateBooking(ActionEvent event) {
        System.out.println("Mở form Tạo Booking mới");
    }

    @FXML
    public void handleCheckIn(ActionEvent event) {
        System.out.println("Chuyển trạng thái Booking này sang 'Đã Check-in' và xếp phòng");
    }

    @FXML
    public void handleCheckOut(ActionEvent event) {
        System.out.println("Chuyển trạng thái sang 'Đã trả phòng' và sinh Hóa đơn");
    }

    @FXML
    public void handleCancelBooking(ActionEvent event) {
        System.out.println("Hủy Booking này");
    }
}