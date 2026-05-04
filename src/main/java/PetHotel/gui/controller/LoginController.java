package PetHotel.gui.controller;

import java.io.IOException;

import PetHotel.bus.AccountBUS;
import PetHotel.model.Account;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
    private Text wrongWarning;
    // Khai báo lớp BUS để xử lý logic
    private AccountBUS accountBUS;

    // Hàm initialize() sẽ tự động chạy ngay khi giao diện FXML được load lên
    @FXML
    public void initialize() {
        accountBUS = new AccountBUS();
    }

    // 2. Hàm xử lý sự kiện khi bấm nút (Trùng tên với onAction="#handleLogin")
    @FXML
    public void handleLogin(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        
        Account loginResult = accountBUS.login(username, password);

        if (loginResult != null) {
            wrongWarning.getStyleClass().remove("wrong-warning");            
            // THỰC HIỆN CHUYỂN TRANG
            try {
                // Bước 1: Tải file Dashboard.fxml
                // LƯU Ý: Đường dẫn bắt đầu bằng dấu / và trỏ đúng vào thư mục resources của bạn
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/PetHotel/gui/view/MainDashboard.fxml"));
                Parent root = loader.load();

                // Bước 2: Tạo một cửa sổ (Stage) mới
                Stage dashboardStage = new Stage();
                dashboardStage.setTitle("PetHotel - Hệ thống quản lý");
                dashboardStage.setScene(new Scene(root));
                
                // (Mẹo) Thường Dashboard sẽ mở full màn hình, bạn có thể bật dòng này:
                // dashboardStage.setMaximized(true);

                // Bước 3: Hiển thị cửa sổ Dashboard lên
                dashboardStage.show();

                // Bước 4: Tắt cửa sổ Login hiện tại
                // Chúng ta sẽ lấy cái Cửa sổ (Stage) đang chứa ô txtUsername và ra lệnh đóng nó
                Stage currentStage = (Stage) txtUsername.getScene().getWindow();
                currentStage.close();

            } catch (IOException e) {
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Lỗi hệ thống", "Không thể tải giao diện Dashboard!");
            }

        } else {
            // Đăng nhập sai -> Thêm class để hiện cảnh báo đỏ
            wrongWarning.getStyleClass().add("wrong-warning");
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
}