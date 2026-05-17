package PetHotel.gui.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * EmployeeController — Quản Lý Nhân Viên
 * ─────────────────────────────────────────────────────────────────
 * Xử lý: EmployeeManagement.fxml
 * Use cases:
 *   - Tra cứu / lọc nhân viên
 *   - Thêm / Sửa nhân viên
 *   - Ngưng / Kích hoạt nhân viên
 *   - Xem thống kê hiệu suất
 *   - Hiển thị chi tiết hồ sơ ở panel phải
 */
public class EmployeeController {

    // ── Stats ────────────────────────────────────────────────────
    @FXML private Label statTotal;
    @FXML private Label statActive;
    @FXML private Label statInactive;
    @FXML private Label statGroomers;
    @FXML private Label statAvgPerf;

    // ── Filter ───────────────────────────────────────────────────
    @FXML private TextField         searchField;
    @FXML private ComboBox<String>  filterRole;
    @FXML private ComboBox<String>  filterStatus;
    @FXML private ComboBox<String>  filterBranch;

    // ── Toolbar ──────────────────────────────────────────────────
    @FXML private Button btnEdit;
    @FXML private Button btnDeactivate;
    @FXML private Button btnActivate;
    @FXML private Button btnPerf;
    @FXML private Label  selectionInfo;

    // ── Table ────────────────────────────────────────────────────
    @FXML private TableView<Object>          employeeTable;
    @FXML private TableColumn<Object,String> colAvatar;
    @FXML private TableColumn<Object,String> colEmpId;
    @FXML private TableColumn<Object,String> colEmpName;
    @FXML private TableColumn<Object,String> colEmpRole;
    @FXML private TableColumn<Object,String> colEmpBranch;
    @FXML private TableColumn<Object,String> colEmpPhone;
    @FXML private TableColumn<Object,String> colEmpPerf;
    @FXML private TableColumn<Object,String> colEmpStatus;
    @FXML private TableColumn<Object,String> colEmpAction;

    @FXML private Pagination pagination;
    @FXML private Label      pageInfo;

    // ── Detail panel ─────────────────────────────────────────────
    @FXML private VBox   noSelectionHint;
    @FXML private Label  detailAvatar;
    @FXML private Label  detailName;
    @FXML private Label  detailRole;
    @FXML private Label  detailStatus;
    @FXML private Label  detailId;
    @FXML private Label  detailCCCD;
    @FXML private Label  detailDob;
    @FXML private Label  detailPhone;
    @FXML private Label  detailEmail;
    @FXML private Label  detailBranch;
    @FXML private Label  detailJoinDate;
    @FXML private Label  detailAddress;
    @FXML private Button btnDetailEdit;
    @FXML private Button btnDetailDeact;
    @FXML private Button btnDetailPerf;

    // ── Performance panel ─────────────────────────────────────────
    @FXML private ComboBox<String> perfPeriod;
    @FXML private Label            perfGrooming;
    @FXML private Label            perfCheckin;
    @FXML private Label            perfRating;
    @FXML private VBox             occupancyChartArea; // re-used id from chart placeholder

    // ── State ────────────────────────────────────────────────────
    private Object selectedEmployee = null;

    // ── Lifecycle ────────────────────────────────────────────────

    @FXML
    public void initialize() {
        setupColumns();
        setupSelectionListener();
        loadBranchFilter();
        loadStats();
        loadEmployees();
        showNoSelection();
    }

    // ── Setup ────────────────────────────────────────────────────

    private void setupColumns() {
        // Status badge
        colEmpStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) { setGraphic(null); return; }
                Label badge = new Label(value);
                badge.getStyleClass().add("status-badge");
                badge.getStyleClass().add(
                    value.equals("Đang hoạt động") ? "status-active" : "status-locked"
                );
                setGraphic(badge); setText(null);
            }
        });

        // Role badge
        colEmpRole.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) { setGraphic(null); return; }
                Label badge = new Label(value);
                badge.getStyleClass().add("status-badge");
                switch (value) {
                    case "Quản Lý"   -> badge.getStyleClass().add("status-manager");
                    case "Lễ Tân"    -> badge.getStyleClass().add("status-staff");
                    case "Groomer"   -> badge.getStyleClass().add("status-admin");
                    default          -> badge.getStyleClass().add("status-pending");
                }
                setGraphic(badge); setText(null);
            }
        });

        // Avatar column (first letter of name)
        colAvatar.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty) { setGraphic(null); return; }
                Label av = new Label(value != null ? value.substring(0,1) : "?");
                av.setStyle(
                    "-fx-background-color: -ph-brown; -fx-background-radius:18px;" +
                    "-fx-min-width:32px; -fx-max-width:32px;" +
                    "-fx-min-height:32px; -fx-max-height:32px;" +
                    "-fx-alignment:center; -fx-text-fill:white;" +
                    "-fx-font-weight:bold; -fx-font-size:13px;"
                );
                setGraphic(av); setText(null);
            }
        });
    }

    private void setupSelectionListener() {
        employeeTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                boolean has = newVal != null;
                selectedEmployee = newVal;
                setToolbarEnabled(has);
                if (has) showEmployeeDetail(newVal);
                else     showNoSelection();
            }
        );
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

    // ── Data ─────────────────────────────────────────────────────

    private void loadStats() {
        // TODO: EmployeeBUS.getStats()
        statTotal.setText("0");
        statActive.setText("0");
        statInactive.setText("0");
        statGroomers.setText("0");
        statAvgPerf.setText("—");
    }

    private void loadBranchFilter() {
        // TODO: filterBranch.setItems(FXCollections.observableList(BranchBUS.getAll()))
    }

    private void loadEmployees() {
        // TODO: EmployeeBUS.search(keyword, role, status, branch, page)
        ObservableList<Object> data = FXCollections.observableArrayList();
        employeeTable.setItems(data);
        pageInfo.setText("Hiển thị 0 / 0 nhân viên");
    }

    private void showEmployeeDetail(Object emp) {
        noSelectionHint.setVisible(false);
        noSelectionHint.setManaged(false);
        // TODO: bind từng label với employee model
        detailName.setText("—");
        detailId.setText("—");
        detailCCCD.setText("—");
        detailDob.setText("—");
        detailPhone.setText("—");
        detailEmail.setText("—");
        detailBranch.setText("—");
        detailJoinDate.setText("—");
        detailAddress.setText("—");
        loadPerformanceSummary();
    }

    private void showNoSelection() {
        noSelectionHint.setVisible(true);
        noSelectionHint.setManaged(true);
        resetPerformance();
    }

    private void loadPerformanceSummary() {
        // TODO: EmployeeBUS.getPerformance(selectedEmployee.getId(), perfPeriod.getValue())
        perfGrooming.setText("0");
        perfCheckin.setText("0");
        perfRating.setText("—");
    }

    private void resetPerformance() {
        perfGrooming.setText("—");
        perfCheckin.setText("—");
        perfRating.setText("—");
    }

    // ── FXML Handlers ────────────────────────────────────────────

    @FXML public void onSearch() {
        // TODO: EmployeeBUS.search(...)
        loadEmployees();
    }

    @FXML public void onClearFilter() {
        searchField.clear();
        filterRole.setValue(null);
        filterStatus.setValue(null);
        filterBranch.setValue(null);
        loadEmployees();
    }

    @FXML public void onTableClick(MouseEvent event) {
        if (event.getClickCount() == 2 && selectedEmployee != null) {
            openEmployeeForm(selectedEmployee);
        }
    }

    @FXML public void onAddEmployee(ActionEvent event) {
        openEmployeeForm(null);
    }

    @FXML public void onEditEmployee(ActionEvent event) {
        if (selectedEmployee == null) return;
        openEmployeeForm(selectedEmployee);
    }

    @FXML public void onDeactivateEmployee(ActionEvent event) {
        if (selectedEmployee == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Ngưng hoạt động nhân viên này?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác Nhận"); confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                // TODO: EmployeeBUS.deactivate(selectedEmployee.getId())
                loadEmployees(); loadStats();
            }
        });
    }

    @FXML public void onActivateEmployee(ActionEvent event) {
        if (selectedEmployee == null) return;
        // TODO: EmployeeBUS.activate(selectedEmployee.getId())
        loadEmployees(); loadStats();
    }

    @FXML public void onViewPerformance(ActionEvent event) {
        if (selectedEmployee == null) return;
        // TODO: mở dialog chi tiết hiệu suất
        System.out.println("Xem chi tiết hiệu suất nhân viên...");
    }

    // ── Helpers ──────────────────────────────────────────────────

    private void openEmployeeForm(Object employee) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/PetHotel/gui/view/EmployeeForm.fxml")
            );
            Parent root = loader.load();
            Stage dialog = new Stage();
            dialog.setTitle(employee == null ? "Thêm Nhân Viên" : "Sửa Thông Tin Nhân Viên");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
            loadEmployees();
            loadStats();
        } catch (IOException e) {
            System.err.println("Không mở được EmployeeForm.fxml: " + e.getMessage());
        }
    }
}
