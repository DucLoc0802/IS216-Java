package PetHotel.gui.controller;

import PetHotel.model.AppUser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AccountFormController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cbRole; // Phân quyền (Admin, Quản lý, Lễ tân)
    
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    // Biến lưu trữ employeeId nếu đang ở chế độ Sửa (Edit), nếu null là Thêm mới (Add)
    private String editingEmployeeId = null;

    @FXML
    public void initialize() {
        // Khởi tạo danh sách quyền
        cbRole.getItems().addAll("Admin", "Quản lý", "Lễ tân");
        cbRole.getSelectionModel().selectFirst();
    }

    @FXML 
    public void onCancel(ActionEvent event) { 
        System.out.println("Hủy bỏ, đóng Popup..."); 
        // TODO: Viết code đóng Stage (cửa sổ) ở đây
    }

    @FXML 
    public void onSubmit(ActionEvent event) { 
        System.out.println("Tiến hành Validate và Lưu tài khoản xuống Database..."); 
    }
    
    // Hàm này được gọi từ trang Management truyền data sang nếu bấm nút "Sửa"
    public void setEditData(AppUser user) {
        // TODO: Lấy data từ user truyền vào các ô Text
        // txtUsername.setText(user.getUserName());
        // this.editingEmployeeId = user.getEmployeeId();
    }

    @FXML
    public void handleSave(ActionEvent event) {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();
        String role = cbRole.getValue();

        // Validate cơ bản
        if (user.isEmpty() || pass.isEmpty()) {
            System.out.println("Lỗi: Không được để trống!");
            return;
        }

        if (editingEmployeeId == null) {
            System.out.println("Tiến hành INSERT tài khoản mới xuống DB...");
            // TODO: Gọi AppUserBUS.add()
        } else {
            System.out.println("Tiến hành UPDATE tài khoản đang sửa xuống DB...");
            // TODO: Gọi AppUserBUS.update()
        }

        // Lưu xong thì đóng cửa sổ form lại
        closeForm();
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        closeForm();
    }

    private void closeForm() {
        // Lấy cửa sổ (Stage) hiện tại của nút Hủy và đóng nó đi
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}