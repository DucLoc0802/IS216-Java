package PetHotel.gui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class GroomingController {

    @FXML private TextField txtSearch;
    @FXML private DatePicker dpDateFilter; // Lọc lịch theo ngày
    @FXML private TableView<?> tableGrooming;

    @FXML public void onCreateGrooming(ActionEvent event) { System.out.println("Mở form Đặt lịch Grooming..."); }
    @FXML public void onPrevDay(ActionEvent event) { System.out.println("Xem lịch ngày hôm qua..."); }
    @FXML public void onNextDay(ActionEvent event) { System.out.println("Xem lịch ngày mai..."); }
    @FXML public void onToday(ActionEvent event) { System.out.println("Quay về lịch hôm nay..."); }

    @FXML
    public void initialize() {
        System.out.println("Đã load giao diện Quản lý Grooming");
    }

    @FXML
    public void handleFilter(ActionEvent event) {
        System.out.println("Lọc lịch Grooming theo ngày và từ khóa");
    }

    @FXML
    public void handleAdd(ActionEvent event) {
        System.out.println("Tạo lịch Grooming mới");
    }

    @FXML
    public void handleUpdateStatus(ActionEvent event) {
        System.out.println("Cập nhật trạng thái: Chờ -> Đang làm -> Hoàn thành");
    }
    
    @FXML
    public void handleCancel(ActionEvent event) {
        System.out.println("Hủy lịch Grooming");
    }
}