package PetHotel.gui.controller;

import PetHotel.bus.EmployeeBUS;
import PetHotel.exception.AuthorizationException;
import PetHotel.exception.NotFoundException;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.model.Employee;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ProfileController {

    private final EmployeeBUS employeeBUS = new EmployeeBUS();

    @FXML private Label lblEmployeeId;
    @FXML private Label lblRole;
    @FXML private Label lblBranch;
    @FXML private Label lblHireDate;
    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private TextArea txtNote;
    @FXML private Button btnSave;

    @FXML
    public void initialize() {
        AppUser currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert(AlertType.ERROR, "Phiên đăng nhập không hợp lệ", "Bạn cần đăng nhập lại.");
            closeStage();
            return;
        }

        Employee employee = currentUser.getEmployee();
        if (employee == null) {
            employee = employeeBUS.getProfile(currentUser.getEmployeeId());
            currentUser.setEmployee(employee);
        }

        lblEmployeeId.setText(currentUser.getEmployeeId());
        lblRole.setText(currentUser.getRole().getDisplayName());
        lblBranch.setText(valueOrDash(employee.getBranchId()));
        lblHireDate.setText(employee.getHireDate() != null
            ? employee.getHireDate().toLocalDate().toString()
            : "-");
        txtFullName.setText(valueOrEmpty(employee.getFullName()));
        txtEmail.setText(valueOrEmpty(employee.getEmail()));
        txtPhone.setText(valueOrEmpty(employee.getPhone()));
        txtNote.setText(valueOrEmpty(employee.getNote()));
    }

    @FXML
    public void onSave() {
        AppUser currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert(AlertType.ERROR, "Phiên đăng nhập không hợp lệ", "Bạn cần đăng nhập lại.");
            return;
        }

        try {
            employeeBUS.updateOwnProfile(
                currentUser,
                txtFullName.getText(),
                txtEmail.getText(),
                txtPhone.getText(),
                txtNote.getText()
            );
            showAlert(AlertType.INFORMATION, "Cập nhật thành công",
                "Thông tin cá nhân đã được cập nhật.");
            closeStage();
        } catch (ValidationException | AuthorizationException | NotFoundException e) {
            showAlert(AlertType.WARNING, "Không thể cập nhật", e.getMessage());
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống", e.getMessage());
        }
    }

    @FXML
    public void onCancel() {
        closeStage();
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
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
