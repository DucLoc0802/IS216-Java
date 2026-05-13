package PetHotel.gui.controller;

import PetHotel.bus.EmployeeBUS;
import PetHotel.exception.ValidationException;
import PetHotel.model.Employee;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EmployeeFormController {

    private final EmployeeBUS employeeBUS = new EmployeeBUS();
    private EmployeeController parentController;
    private Employee editingEmployee;

    @FXML private TextField txtEmployeeId;
    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private ComboBox<String> cbBranch;
    @FXML private ComboBox<String> cbStatus;
    @FXML private TextArea txtNote;
    @FXML private Button btnSave;

    @FXML
    public void initialize() {
        cbBranch.setItems(FXCollections.observableArrayList(employeeBUS.getBranches()));
        cbStatus.setItems(FXCollections.observableArrayList("WORKING", "ON_LEAVE", "RESIGNED"));
        cbStatus.setValue("WORKING");
    }

    public void setParentController(EmployeeController parentController) {
        this.parentController = parentController;
    }

    public void setEditData(Employee employee) {
        this.editingEmployee = employee;
        txtEmployeeId.setText(employee.getEmployeeId());
        txtEmployeeId.setDisable(true);
        txtFullName.setText(employee.getFullName());
        txtEmail.setText(employee.getEmail());
        txtPhone.setText(employee.getPhone());
        cbBranch.setValue(employee.getBranchId());
        cbStatus.setValue(employee.getStatusCode());
        txtNote.setText(employee.getNote());
    }

    @FXML
    public void onSave() {
        try {
            if (editingEmployee == null) {
                employeeBUS.createEmployee(
                    cbBranch.getValue(),
                    txtFullName.getText(),
                    txtEmail.getText(),
                    txtPhone.getText(),
                    txtNote.getText()
                );
            } else {
                employeeBUS.updateEmployee(
                    editingEmployee.getEmployeeId(),
                    cbBranch.getValue(),
                    txtFullName.getText(),
                    txtEmail.getText(),
                    txtPhone.getText(),
                    cbStatus.getValue(),
                    txtNote.getText()
                );
            }

            if (parentController != null) {
                parentController.refreshData();
            }
            closeStage();
        } catch (ValidationException e) {
            showAlert(AlertType.WARNING, "Dữ liệu chưa hợp lệ", e.getMessage());
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Không thể lưu", e.getMessage());
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
