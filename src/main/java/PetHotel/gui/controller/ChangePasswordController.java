package PetHotel.gui.controller;

import PetHotel.bus.AuthBUS;
import PetHotel.exception.AuthenticationException;
import PetHotel.exception.AuthorizationException;
import PetHotel.exception.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class ChangePasswordController {

    @FXML private PasswordField txtOldPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label lblHint;
    @FXML private Button btnSave;

    @FXML
    public void initialize() {
        lblHint.setText("Mật khẩu cần ít nhất 8 ký tự, gồm chữ hoa, chữ thường và số.");
    }

    @FXML
    public void onSave() {
        AuthBUS authBUS = SessionManager.getInstance().getAuthBUS();
        String employeeId = SessionManager.getInstance().getUserId();

        if (authBUS == null || employeeId == null) {
            showAlert(AlertType.ERROR, "Phiên đăng nhập không hợp lệ", "Bạn cần đăng nhập lại.");
            return;
        }

        String oldPassword = txtOldPassword.getText();
        String newPassword = txtNewPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        if (newPassword == null || !newPassword.equals(confirmPassword)) {
            showAlert(AlertType.WARNING, "Xác nhận mật khẩu sai",
                "Mật khẩu xác nhận không khớp với mật khẩu mới.");
            return;
        }

        try {
            authBUS.changePassword(employeeId, oldPassword, newPassword);
            showAlert(AlertType.INFORMATION, "Đổi mật khẩu thành công",
                "Mật khẩu của bạn đã được cập nhật.");
            closeStage();
        } catch (ValidationException | AuthenticationException | AuthorizationException e) {
            showAlert(AlertType.WARNING, "Không thể đổi mật khẩu", e.getMessage());
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống", e.getMessage());
        }
    }

    @FXML
    public void onCancel() {
        closeStage();
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeStage() {
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }
}
