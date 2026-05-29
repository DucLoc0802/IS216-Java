package PetHotel.gui.controller;

import PetHotel.bus.AccountBUS;
import PetHotel.exception.AuthorizationException;
import PetHotel.exception.DuplicateRecordException;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.model.Employee;
import PetHotel.util.Role;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class AccountFormController {

    @FXML private Label lblTitle;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<Employee> cbEmployee;
    @FXML private ComboBox<String> cbRole;

    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    private AccountBUS accountBUS;
    private AccountController parentController;
    private String editingEmployeeId = null;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        accountBUS = new AccountBUS();
        configureEmployeeCombo();
        loadAvailableEmployees();

        cbRole.getItems().addAll(
            Role.ADMIN.getDisplayName(),
            Role.CEO.getDisplayName(),
            Role.BRANCH_MANAGER.getDisplayName(),
            Role.RECEPTIONIST.getDisplayName(),
            Role.PET_CARE_STAFF.getDisplayName()
        );
        cbRole.getSelectionModel().selectFirst();
    }

    public void setParentController(AccountController controller) {
        this.parentController = controller;
    }

    public void setEditData(AppUser user) {
        isEditMode = true;
        this.editingEmployeeId = user.getEmployeeId();

        if (lblTitle != null) {
            lblTitle.setText("Cập Nhật Tài Khoản");
        }
        if (btnSave != null) {
            btnSave.setText("Cập Nhật");
        }

        txtUsername.setText(user.getUserName());
        txtUsername.setDisable(true);
        txtPassword.setPromptText("Để trống nếu không đổi mật khẩu");

        Employee employee = user.getEmployee();
        if (employee == null) {
            employee = new Employee();
            employee.setEmployeeId(user.getEmployeeId());
        }
        if (employee.getFullName() == null || employee.getFullName().isBlank()) {
            employee.setFullName(user.getEmployeeId());
        }
        cbEmployee.setItems(FXCollections.observableArrayList(employee));
        cbEmployee.getSelectionModel().select(employee);
        cbEmployee.setDisable(true);

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
        Employee selectedEmployee = cbEmployee.getValue();
        String roleStr = cbRole.getValue();

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
            try {
                accountBUS.updateRole(currentUser, editingEmployeeId, role);

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
            if (pass == null || pass.trim().isEmpty()) {
                showAlert(AlertType.WARNING, "Lỗi", "Mật khẩu không được để trống.");
                return;
            }
            if (selectedEmployee == null || selectedEmployee.getEmployeeId() == null || selectedEmployee.getEmployeeId().trim().isEmpty()) {
                showAlert(AlertType.WARNING, "Lỗi", "Vui lòng chọn nhân viên.");
                return;
            }

            try {
                accountBUS.createAccount(currentUser, selectedEmployee.getEmployeeId().trim(), user.trim(), pass, role);
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

    private void configureEmployeeCombo() {
        cbEmployee.setConverter(new StringConverter<>() {
            @Override
            public String toString(Employee employee) {
                return employee == null ? "" : displayEmployee(employee);
            }

            @Override
            public Employee fromString(String string) {
                return null;
            }
        });

        cbEmployee.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Employee item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : displayEmployee(item));
            }
        });
    }

    private void loadAvailableEmployees() {
        cbEmployee.setItems(FXCollections.observableArrayList(accountBUS.getEmployeesWithoutAccount()));
    }

    private String displayEmployee(Employee employee) {
        String employeeId = employee.getEmployeeId() == null ? "" : employee.getEmployeeId().trim();
        String fullName = employee.getFullName() == null ? "" : employee.getFullName().trim();
        return fullName.isEmpty() ? employeeId : employeeId + " - " + fullName;
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
