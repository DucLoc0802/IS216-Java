package PetHotel.gui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class AccountController {

    @FXML private TextField txtSearch;
    @FXML private TableView<?> tableAccount; 

    @FXML
    public void initialize() {
        System.out.println("Đã load giao diện Quản lý Tài Khoản");
        // TODO: Cấu hình các cột (Username, Họ tên, Quyền, Trạng thái...)
    }
    @FXML public void onCreateAccount(ActionEvent event) { System.out.println("Mở form Tạo Tài khoản mới..."); }
    @FXML public void onSearch(ActionEvent event) { System.out.println("Tìm kiếm tài khoản..."); }
    @FXML public void onApplyFilter(ActionEvent event) { System.out.println("Áp dụng bộ lọc tài khoản..."); }
    @FXML public void onClearFilter(ActionEvent event) { System.out.println("Xóa bộ lọc..."); }

    // --- Thao tác nhiều dòng (Bulk Actions) ---
    @FXML public void onLockSelected(ActionEvent event) { System.out.println("Khóa các tài khoản đã chọn..."); }
    @FXML public void onUnlockSelected(ActionEvent event) { System.out.println("Mở khóa các tài khoản đã chọn..."); }
    @FXML public void onResetPassword(ActionEvent event) { System.out.println("Đặt lại MK các tài khoản đã chọn..."); }
    @FXML public void onManagePermission(ActionEvent event) { System.out.println("Phân quyền hàng loạt..."); }

    // --- Thao tác chi tiết 1 dòng ---
    @FXML public void onTableClick(MouseEvent event) { System.out.println("Click chọn 1 tài khoản để xem Panel bên phải..."); }
    @FXML public void onEditAccount(ActionEvent event) { System.out.println("Sửa tài khoản đang xem chi tiết..."); }
    @FXML public void onLockSingle(ActionEvent event) { System.out.println("Khóa tài khoản đang xem chi tiết..."); }
    @FXML public void onResetSinglePwd(ActionEvent event) { System.out.println("Đặt lại MK tài khoản đang xem chi tiết..."); }
    @FXML public void onSavePermission(ActionEvent event) { System.out.println("Lưu thiết lập phân quyền..."); }

    // --- Audit Log ---
    @FXML public void onFilterLog(ActionEvent event) { System.out.println("Lọc Audit Log..."); }
    @FXML public void onExportLog(ActionEvent event) { System.out.println("Xuất file Audit Log..."); }
    @FXML
    public void handleSearch(ActionEvent event) {
        System.out.println("Tìm kiếm tài khoản: " + txtSearch.getText());
    }

    @FXML
    public void handleAdd(ActionEvent event) {
        System.out.println("Mở form thêm tài khoản mới (AccountForm)");
    }

    @FXML
    public void handleEdit(ActionEvent event) {
        System.out.println("Mở form sửa tài khoản đang chọn");
    }

    @FXML
    public void handleResetPassword(ActionEvent event) {
        System.out.println("Reset mật khẩu về mặc định (vd: 123456) cho tài khoản này");
    }

    @FXML
    public void handleDelete(ActionEvent event) {
        System.out.println("Khóa/Xóa tài khoản đang chọn");
    }
}