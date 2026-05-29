package PetHotel.gui.controller;

import java.io.IOException;
import java.util.List;

import PetHotel.bus.EmployeeBUS;
import PetHotel.model.Employee;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class EmployeeController {

    @FXML private Label statTotal;
    @FXML private Label statActive;
    @FXML private Label statInactive;
    @FXML private Label statGroomers;
    @FXML private Label statAvgPerf;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterRole;
    @FXML private ComboBox<String> filterStatus;
    @FXML private ComboBox<String> filterBranch;

    @FXML private Button btnEdit;
    @FXML private Button btnDeactivate;
    @FXML private Button btnActivate;
    @FXML private Button btnPerf;
    @FXML private Label selectionInfo;

    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> colAvatar;
    @FXML private TableColumn<Employee, String> colEmpId;
    @FXML private TableColumn<Employee, String> colEmpName;
    @FXML private TableColumn<Employee, String> colEmpRole;
    @FXML private TableColumn<Employee, String> colEmpBranch;
    @FXML private TableColumn<Employee, String> colEmpPhone;
    @FXML private TableColumn<Employee, String> colEmpPerf;
    @FXML private TableColumn<Employee, String> colEmpStatus;
    @FXML private TableColumn<Employee, String> colEmpAction;

    @FXML private Pagination pagination;
    @FXML private Label pageInfo;

    @FXML private VBox noSelectionHint;
    @FXML private Label detailAvatar;
    @FXML private Label detailName;
    @FXML private Label detailRole;
    @FXML private Label detailStatus;
    @FXML private Label detailId;
    @FXML private Label detailCCCD;
    @FXML private Label detailDob;
    @FXML private Label detailPhone;
    @FXML private Label detailEmail;
    @FXML private Label detailBranch;
    @FXML private Label detailJoinDate;
    @FXML private Label detailAddress;
    @FXML private Button btnDetailEdit;
    @FXML private Button btnDetailDeact;
    @FXML private Button btnDetailPerf;

    @FXML private ComboBox<String> perfPeriod;
    @FXML private Label perfGrooming;
    @FXML private Label perfCheckin;
    @FXML private Label perfRating;

    private final EmployeeBUS employeeBUS = new EmployeeBUS();
    private final ObservableList<Employee> employeeData = FXCollections.observableArrayList();
    private Employee selectedEmployee;

    @FXML
    public void initialize() {
        setupColumns();
        employeeTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        setupSelectionListener();
        loadBranchFilter();
        loadStats();
        loadEmployees();
        showNoSelection();
    }

    private void setupColumns() {
        colAvatar.setCellValueFactory(cell ->
            new SimpleStringProperty(firstLetter(cell.getValue().getFullName())));
        colEmpId.setCellValueFactory(cell ->
            new SimpleStringProperty(cell.getValue().getEmployeeId()));
        colEmpName.setCellValueFactory(cell ->
            new SimpleStringProperty(cell.getValue().getFullName()));
        colEmpRole.setCellValueFactory(cell ->
            new SimpleStringProperty(displayRole(cell.getValue())));
        colEmpBranch.setCellValueFactory(cell ->
            new SimpleStringProperty(valueOrDash(cell.getValue().getBranchId())));
        colEmpPhone.setCellValueFactory(cell ->
            new SimpleStringProperty(valueOrDash(cell.getValue().getPhone())));
        colEmpPerf.setCellValueFactory(cell -> new SimpleStringProperty("—"));
        colEmpStatus.setCellValueFactory(cell ->
            new SimpleStringProperty(mapStatusToLabel(cell.getValue().getStatusCode())));
        colEmpAction.setCellValueFactory(cell -> new SimpleStringProperty("Xem"));

        colEmpStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(value);
                badge.getStyleClass().add("status-badge");
                badge.getStyleClass().add(
                    "Đang hoạt động".equals(value) ? "status-active" : "status-locked"
                );
                setGraphic(badge);
                setText(null);
            }
        });

        colEmpRole.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(value);
                badge.getStyleClass().add("status-badge");
                if ("Quản Lý".equals(value)) {
                    badge.getStyleClass().add("status-manager");
                } else if ("Lễ Tân".equals(value)) {
                    badge.getStyleClass().add("status-staff");
                } else if ("Nhân Viên Chăm Sóc".equals(value)) {
                    badge.getStyleClass().add("status-admin");
                } else {
                    badge.getStyleClass().add("status-pending");
                }
                setGraphic(badge);
                setText(null);
            }
        });

        colAvatar.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setGraphic(null);
                    return;
                }
                Label av = new Label(value);
                av.setStyle(
                    "-fx-background-color: -ph-brown; -fx-background-radius:18px;" +
                    "-fx-min-width:32px; -fx-max-width:32px;" +
                    "-fx-min-height:32px; -fx-max-height:32px;" +
                    "-fx-alignment:center; -fx-text-fill:white;" +
                    "-fx-font-weight:bold; -fx-font-size:13px;"
                );
                setGraphic(av);
                setText(null);
            }
        });
    }

    private void setupSelectionListener() {
        employeeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedEmployee = newVal;
            boolean has = newVal != null;
            setToolbarEnabled(has);
            if (has) {
                showEmployeeDetail(newVal);
            } else {
                showNoSelection();
            }
        });
    }

    private void loadStats() {
        int[] stats = employeeBUS.getStats();
        statTotal.setText(String.valueOf(stats[0]));
        statActive.setText(String.valueOf(stats[1]));
        statInactive.setText(String.valueOf(stats[2]));
        statGroomers.setText(String.valueOf(stats[3]));
        statAvgPerf.setText("—");
    }

    private void loadBranchFilter() {
        List<String> branches = employeeBUS.getBranches();
        filterBranch.setItems(FXCollections.observableArrayList(branches));
    }

    private void loadEmployees() {
        List<Employee> employees = employeeBUS.searchEmployees(
            searchField != null ? searchField.getText() : null,
            filterRole != null ? filterRole.getValue() : null,
            filterStatus != null ? filterStatus.getValue() : null,
            filterBranch != null ? filterBranch.getValue() : null
        );
        employeeData.setAll(employees);
        employeeTable.setItems(employeeData);
        pageInfo.setText("Hiển thị " + employees.size() + " / " + employees.size() + " nhân viên");
        pagination.setPageCount(1);
    }

    private void showEmployeeDetail(Employee emp) {
        noSelectionHint.setVisible(false);
        noSelectionHint.setManaged(false);

        detailAvatar.setText(firstLetter(emp.getFullName()));
        detailName.setText(valueOrDash(emp.getFullName()));
        detailRole.setText(displayRole(emp));
        detailStatus.setText(mapStatusToLabel(emp.getStatusCode()));
        detailId.setText(valueOrDash(emp.getEmployeeId()));
        detailCCCD.setText("—");
        detailDob.setText("—");
        detailPhone.setText(valueOrDash(emp.getPhone()));
        detailEmail.setText(valueOrDash(emp.getEmail()));
        detailBranch.setText(valueOrDash(emp.getBranchId()));
        detailJoinDate.setText(emp.getHireDate() != null ? emp.getHireDate().toLocalDate().toString() : "—");
        detailAddress.setText(valueOrDash(emp.getNote()));

        loadPerformanceSummary();
    }

    private void showNoSelection() {
        noSelectionHint.setVisible(true);
        noSelectionHint.setManaged(true);
        detailAvatar.setText("NV");
        detailName.setText("—");
        detailRole.setText("—");
        detailStatus.setText("—");
        detailId.setText("—");
        detailCCCD.setText("—");
        detailDob.setText("—");
        detailPhone.setText("—");
        detailEmail.setText("—");
        detailBranch.setText("—");
        detailJoinDate.setText("—");
        detailAddress.setText("—");
        resetPerformance();
    }

    private void loadPerformanceSummary() {
        if (selectedEmployee == null) {
            resetPerformance();
            return;
        }
        int[] perf = employeeBUS.getPerformanceSummary(selectedEmployee.getEmployeeId());
        perfGrooming.setText(String.valueOf(perf[0]));
        perfCheckin.setText(String.valueOf(perf[1]));
        perfRating.setText("—");
    }

    private void resetPerformance() {
        perfGrooming.setText("—");
        perfCheckin.setText("—");
        perfRating.setText("—");
    }

    @FXML
    public void onSearch() {
        loadEmployees();
    }

    @FXML
    public void onClearFilter() {
        searchField.clear();
        filterRole.setValue(null);
        filterStatus.setValue(null);
        filterBranch.setValue(null);
        loadEmployees();
    }

    @FXML
    public void onTableClick(MouseEvent event) {
        if (event.getClickCount() == 2 && selectedEmployee != null) {
            openEmployeeForm(selectedEmployee);
        }
    }

    @FXML
    public void onAddEmployee(javafx.event.ActionEvent event) {
        openEmployeeForm(null);
    }

    @FXML
    public void onEditEmployee(javafx.event.ActionEvent event) {
        if (selectedEmployee == null) return;
        openEmployeeForm(selectedEmployee);
    }

    @FXML
    public void onDeactivateEmployee(javafx.event.ActionEvent event) {
        if (selectedEmployee == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Ngưng hoạt động nhân viên này?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    employeeBUS.deactivateEmployee(selectedEmployee.getEmployeeId());
                    loadEmployees();
                    loadStats();
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Không thể cập nhật", e.getMessage());
                }
            }
        });
    }

    @FXML
    public void onActivateEmployee(javafx.event.ActionEvent event) {
        if (selectedEmployee == null) return;
        try {
            employeeBUS.activateEmployee(selectedEmployee.getEmployeeId());
            loadEmployees();
            loadStats();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Không thể cập nhật", e.getMessage());
        }
    }

    @FXML
    public void onViewPerformance(javafx.event.ActionEvent event) {
        if (selectedEmployee == null) return;
        loadPerformanceSummary();
        showAlert(Alert.AlertType.INFORMATION, "Hiệu suất nhân viên",
            "Đã tải thống kê hiệu suất cơ bản cho " + selectedEmployee.getFullName() + ".");
    }

    private void setToolbarEnabled(boolean enabled) {
        btnEdit.setDisable(!enabled);
        btnDeactivate.setDisable(!enabled);
        btnActivate.setDisable(!enabled);
        btnPerf.setDisable(!enabled);
        btnDetailEdit.setDisable(!enabled);
        btnDetailDeact.setDisable(!enabled);
        btnDetailPerf.setDisable(!enabled);
        selectionInfo.setText(enabled ? "1 nhân viên được chọn" : "");
    }

    private void openEmployeeForm(Employee employee) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PetHotel/gui/view/EmployeeForm.fxml"));
            Parent root = loader.load();

            EmployeeFormController controller = loader.getController();
            controller.setParentController(this);
            if (employee != null) {
                controller.setEditData(employee);
            }

            Stage dialog = new Stage();
            dialog.setTitle(employee == null ? "Thêm Nhân Viên" : "Sửa Thông Tin Nhân Viên");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            loadEmployees();
            loadStats();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Không mở được form", e.getMessage());
        }
    }

    public void refreshData() {
        loadEmployees();
        loadStats();
    }

    private String displayRole(Employee employee) {
        String roleCode = employee.getRoleCode();
        if (roleCode == null || roleCode.isBlank()) {
            return "Chưa phân quyền";
        }
        return switch (roleCode) {
            case "1" -> "Lễ Tân";
            case "2" -> "Nhân Viên Chăm Sóc";
            case "3" -> "Quản Lý";
            case "4" -> "CEO";
            case "5", "0" -> "Quản Trị Viên";
            default -> "Chưa phân quyền";
        };
    }

    private String mapStatusToLabel(String statusCode) {
        if ("WORKING".equalsIgnoreCase(statusCode)) return "Đang hoạt động";
        if ("ON_LEAVE".equalsIgnoreCase(statusCode)) return "Tạm nghỉ";
        if ("RESIGNED".equalsIgnoreCase(statusCode)) return "Ngưng hoạt động";
        return valueOrDash(statusCode);
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private String firstLetter(String value) {
        return value == null || value.isBlank() ? "?" : String.valueOf(value.charAt(0)).toUpperCase();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
