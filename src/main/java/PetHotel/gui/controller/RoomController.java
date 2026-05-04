package PetHotel.gui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class RoomController {

    @FXML private TextField txtSearch;
    @FXML private TableView<?> tableRoom;

    // ... các khai báo @FXML ở trên giữ nguyên

    @FXML
    public void onSearch(javafx.event.ActionEvent event) {
        System.out.println("Đang tìm kiếm phòng...");
    }

    @FXML
    public void onFilter(javafx.event.ActionEvent event) {
        System.out.println("Đang lọc danh sách phòng...");
    }

    @FXML
    public void onAddRoom(javafx.event.ActionEvent event) {
        System.out.println("Mở popup thêm phòng mới...");
    }

    @FXML
    public void onRoomTypes(javafx.event.ActionEvent event) {
        System.out.println("Mở bảng cấu hình Loại phòng & Giá...");
    }
    @FXML
    public void initialize() {
        System.out.println("Đã load giao diện Quản lý Phòng");
        // TODO: Cấu hình các cột (Mã phòng, Loại phòng, Giá, Trạng thái...)
    }

    @FXML
    public void handleSearch(ActionEvent event) {
        System.out.println("Tìm kiếm phòng: " + txtSearch.getText());
    }

    @FXML
    public void handleAdd(ActionEvent event) {
        System.out.println("Thêm phòng mới (ví dụ khách sạn mở rộng thêm phòng)");
    }

    @FXML
    public void handleEdit(ActionEvent event) {
        System.out.println("Sửa thông tin phòng (Đổi giá, đổi loại phòng)");
    }

    @FXML
    public void handleMarkAsCleaned(ActionEvent event) {
        System.out.println("Cập nhật trạng thái phòng từ 'Đang dọn' sang 'Trống'");
    }

    @FXML
    public void handleDelete(ActionEvent event) {
        System.out.println("Xóa phòng này khỏi hệ thống (Chỉ xóa khi phòng chưa từng có ai ở)");
    }
}