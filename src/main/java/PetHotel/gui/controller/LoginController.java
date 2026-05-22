package PetHotel.gui.controller;

import java.io.IOException;

import PetHotel.bus.AuthBUS;
import PetHotel.exception.AuthenticationException;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class LoginController {

    // 1. Dùng @FXML để móc nối với các fx:id bên file FXML
    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnLogin;

    @FXML
    private Text wrongWarning;
    // Khai báo lớp AuthBUS để xử lý logic xác thực
    private AuthBUS authBUS;

    // Hàm initialize() sẽ tự động chạy ngay khi giao diện FXML được load lên
    @FXML
    public void initialize() {
        authBUS = new AuthBUS();
        txtUsername.textProperty().addListener((obs, oldVal, newVal) -> updateLoginButtonState());
        txtPassword.textProperty().addListener((obs, oldVal, newVal) -> updateLoginButtonState());
        updateLoginButtonState();
    }

    // 2. Hàm xử lý sự kiện khi bấm nút (Trùng tên với onAction="#handleLogin")
    @FXML
    public void handleLogin(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        try {
            // Gọi AuthBUS.login() — trả về AppUser nếu thành công, ném exception nếu thất bại
            AppUser loginResult = authBUS.login(username, password);

            // Đăng nhập thành công → ẩn cảnh báo
            wrongWarning.getStyleClass().remove("wrong-warning");

            // Lưu thông tin session
            AppUser currentUser = authBUS.getCurrentUser();
            // BranchId mặc định là chi nhánh của employee (lấy từ Employee profile)
            String branchId = (currentUser.getEmployee() != null)
                              ? currentUser.getEmployee().getBranchId()
                              : null;
            SessionManager.getInstance().login(currentUser, branchId, null);

            // Lưu AuthBUS instance dùng chung vào SessionManager
            SessionManager.getInstance().setAuthBUS(authBUS);

            // THỰC HIỆN CHUYỂN TRANG
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/PetHotel/gui/view/MainDashboard.fxml"));
                Parent root = loader.load();

                Stage dashboardStage = new Stage();
                dashboardStage.setTitle("PetHotel - Hệ thống quản lý");
                dashboardStage.setScene(new Scene(root));
                dashboardStage.setMaximized(true);
                dashboardStage.show();

                // Đóng cửa sổ Login hiện tại
                Stage currentStage = (Stage) txtUsername.getScene().getWindow();
                currentStage.close();

            } catch (IOException e) {
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Lỗi hệ thống", "Không thể tải giao diện Dashboard!");
            }

        } catch (ValidationException e) {
            // Lỗi validate (username/password rỗng)
            wrongWarning.getStyleClass().add("wrong-warning");
            showAlert(AlertType.WARNING, "Thông tin không hợp lệ", e.getMessage());
        } catch (AuthenticationException e) {
            // Lỗi xác thực (sai user/pass, tài khoản bị khóa)
            wrongWarning.getStyleClass().add("wrong-warning");
            showAlert(AlertType.ERROR, "Đăng nhập thất bại", e.getMessage());
        } catch (RuntimeException e) {
            // Lỗi hệ thống (DB, ...)
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Lỗi hệ thống", e.getMessage());
        }
    }

    // Hàm tiện ích để hiển thị Popup thông báo (Alert)
    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void updateLoginButtonState() {
        boolean canLogin = txtUsername != null
            && txtPassword != null
            && !txtUsername.getText().trim().isEmpty()
            && !txtPassword.getText().trim().isEmpty();
        btnLogin.setDisable(!canLogin);
    }
}
