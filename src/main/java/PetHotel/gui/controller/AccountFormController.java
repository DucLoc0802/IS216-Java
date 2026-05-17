package PetHotel.gui.controller;

import PetHotel.bus.AccountBUS;
import PetHotel.exception.AuthorizationException;
import PetHotel.exception.DuplicateRecordException;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.util.Role;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AccountFormController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtEmployeeId;
    @FXML private ComboBox<String> cbRole;

    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    private AccountBUS accountBUS;
    private AccountController parentController;

    // Biến lưu trữ employeeId nếu đang ở chế độ Sửa (Edit), nếu null là Thêm mới (Add)
    private String editingEmployeeId = null;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        accountBUS = new AccountBUS();
        // Khởi tạo danh sách quyền
        cbRole.getItems().addAll(
            Role.ADMIN.getDisplayName(),
            Role.CEO.getDisplayName(),
            Role.BRANCH_MANAGER.getDisplayName(),
            Role.RECEPTIONIST.getDisplayName(),
            Role.PET_CARE_STAFF.getDisplayName()
        );
        cbRole.getSelectionModel().selectFirst();
    }

    /**
     * Gắn AccountController cha để refresh dữ liệu sau khi lưu.
     */
    public void setParentController(AccountController controller) {
        this.parentController = controller;
    }

    /**
     * Đặt dữ liệu cho chế độ Sửa tài khoản.
     */
    public void setEditData(AppUser user) {
        isEditMode = true;
        this.editingEmployeeId = user.getEmployeeId();

        txtUsername.setText(user.getUserName());
        txtUsername.setDisable(true); // Không cho sửa username
        txtEmployeeId.setText(user.getEmployeeId());
        txtEmployeeId.setDisable(true);
        txtPassword.setPromptText("Để trống nếu không đổi mật khẩu");

        // Chọn đúng role
        for (int i = 0; i < cbRole.getItems().size(); i++) {
            if (cbRole.getItems().get(i).equals(user.getRole().getDisplayName())) {
                cbRole.getSelectionModel().select(i);
                break;
            }
        }
    }

    @FXML
    public void onCancel(ActionEvent event) {
        closeForm();
    }

    @FXML
    public void onSubmit(ActionEvent event) {
        handleSave(event);
    }

    @FXML
    public void handleSave(ActionEvent event) {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();
        String employeeId = txtEmployeeId.getText();
        String roleStr = cbRole.getValue();

        // Validate cơ bản
        if (user == null || user.trim().isEmpty()) {
            showAlert(AlertType.WARNING, "Lỗi", "Tên đăng nhập không được để trống.");
            return;
        }

        Role role = mapDisplayNameToRole(roleStr);
        if (role == null) {
            showAlert(AlertType.WARNING, "Lỗi", "Vui lòng chọn vai trò hợp lệ.");
            return;
        }

        AppUser currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert(AlertType.ERROR, "Lỗi", "Bạn cần đăng nhập để thực hiện thao tác này.");
            return;
        }

        if (isEditMode) {
            // Chế độ Sửa: cập nhật role và có thể đổi mật khẩu
            try {
                // Cập nhật role
                accountBUS.updateRole(currentUser, editingEmployeeId, role);

                // Nếu có nhập mật khẩu mới thì đặt lại
                if (pass != null && !pass.trim().isEmpty()) {
                    accountBUS.resetPassword(currentUser, editingEmployeeId, pass);
                }

                showAlert(AlertType.INFORMATION, "Thành công", "Đã cập nhật tài khoản.");
                if (parentController != null) parentController.refreshAccountData();
                closeForm();

            } catch (ValidationException e) {
                showAlert(AlertType.WARNING, "Lỗi", e.getMessage());
            } catch (AuthorizationException e) {
                showAlert(AlertType.ERROR, "Không có quyền", e.getMessage());
            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Lỗi", e.getMessage());
            }

        } else {
            // Chế độ Thêm mới
            if (pass == null || pass.trim().isEmpty()) {
                showAlert(AlertType.WARNING, "Lỗi", "Mật khẩu không được để trống.");
                return;
            }
            if (employeeId == null || employeeId.trim().isEmpty()) {
                showAlert(AlertType.WARNING, "Lỗi", "Mã nhân viên không được để trống.");
                return;
            }

            try {
                accountBUS.createAccount(currentUser, employeeId.trim(), user.trim(), pass, role);
                showAlert(AlertType.INFORMATION, "Thành công", "Đã tạo tài khoản mới.");
                if (parentController != null) parentController.refreshAccountData();
                closeForm();

            } catch (DuplicateRecordException e) {
                showAlert(AlertType.WARNING, "Trùng lặp", e.getMessage());
            } catch (ValidationException e) {
                showAlert(AlertType.WARNING, "Lỗi", e.getMessage());
            } catch (AuthorizationException e) {
                showAlert(AlertType.ERROR, "Không có quyền", e.getMessage());
            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Lỗi", e.getMessage());
            }
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        closeForm();
    }

    private void closeForm() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private Role mapDisplayNameToRole(String displayName) {
        for (Role r : Role.values()) {
            if (r.getDisplayName().equals(displayName)) return r;
        }
        return null;
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}