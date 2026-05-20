package PetHotel.gui.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import PetHotel.bus.GroomingBUS;
import PetHotel.model.AppUser;
import PetHotel.model.BookingService;
import PetHotel.model.Employee;
import PetHotel.util.Role;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * GroomingStaffAssignmentController — Phân công nhân viên thực hiện grooming.
 *
 * Cho phép quản lý chi nhánh:
 * - Xem danh sách công việc grooming chưa phân công
 * - Chọn nhân viên để thực hiện từng công việc
 * - Xem tình trạng khối lượng công việc của nhân viên
 * - Lưu phân công
 */
public class GroomingStaffAssignmentController {

    @FXML private DatePicker dpFilterDate;
    @FXML private ComboBox<String> cbFilterStatus;
    @FXML private TextField txtSearch;
    @FXML private Button btnRefresh;

    @FXML private TableView<BookingService> taskTable;
    @FXML private TableColumn<BookingService, String> colTaskId;
    @FXML private TableColumn<BookingService, String> colScheduleTime;
    @FXML private TableColumn<BookingService, String> colPetName;
    @FXML private TableColumn<BookingService, String> colCustomerName;
    @FXML private TableColumn<BookingService, String> colServiceName;
    @FXML private TableColumn<BookingService, String> colStatus;

    @FXML private Label lblSelectedTaskId;
    @FXML private Label lblSelectedPetName;
    @FXML private Label lblSelectedService;
    @FXML private Label lblSelectedTime;

    @FXML private ComboBox<Employee> cbStaff;
    @FXML private Label lblTodayTasks;
    @FXML private Label lblStaffStatus;
    @FXML private TextArea txtAssignmentNote;
    @FXML private Button btnAssign;
    @FXML private Button btnClear;

    private final GroomingBUS groomingBUS = new GroomingBUS();
    private AppUser currentUser;
    private BookingService selectedTask;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser == null) {
            showError("Lỗi", "Chưa đăng nhập");
            return;
        }

        // Check role
        if (!currentUser.hasRole(Role.BRANCH_MANAGER) && !currentUser.hasRole(Role.ADMIN)) {
            showError("Lỗi", "Bạn không có quyền truy cập chức năng này");
            return;
        }

        setupTableColumns();
        setupFilters();
        setupTableSelection();
        setupButtonActions();

        dpFilterDate.setValue(LocalDate.now());
        loadUnassignedTasks();
        loadStaffList();
    }

    private void setupTableColumns() {
        colTaskId.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getBookingServiceId()
                ));

        colScheduleTime.setCellValueFactory(cellData -> {
            var time = cellData.getValue().getScheduledAt();
            if (time != null) {
                return new javafx.beans.property.SimpleStringProperty(time.format(displayFormatter));
            }
            return new javafx.beans.property.SimpleStringProperty("—");
        });

        colPetName.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        valueOrDash(cellData.getValue().getPetName())
                ));

        colCustomerName.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        valueOrDash(cellData.getValue().getCustomerName())
                ));

        colServiceName.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        valueOrDash(cellData.getValue().getServiceName())
                ));

        colStatus.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        valueOrDash(cellData.getValue().getStatus())
                ));
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "—" : value;
    }

    private void setupFilters() {
        ObservableList<String> statusList = FXCollections.observableArrayList(
                "Tất cả", "PENDING", "SCHEDULED"
        );
        cbFilterStatus.setItems(statusList);
        cbFilterStatus.setValue("Tất cả");
        cbFilterStatus.setOnAction(e -> loadUnassignedTasks());

        dpFilterDate.setOnAction(e -> loadUnassignedTasks());
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> loadUnassignedTasks());
    }

    private void setupTableSelection() {
        taskTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedTask = newVal;
                displaySelectedTask();
            }
        });
    }

    private void setupButtonActions() {
        btnRefresh.setOnAction(e -> {
            loadUnassignedTasks();
            loadStaffList();
        });

        btnAssign.setOnAction(e -> handleAssign());
        btnClear.setOnAction(e -> handleClear());

        cbStaff.setOnAction(e -> displayStaffAvailability());
    }

    private void loadUnassignedTasks() {
        try {
            LocalDate selectedDate = dpFilterDate.getValue();
            if (selectedDate == null) {
                selectedDate = LocalDate.now();
            }

            String dateStr = selectedDate.format(dateFormatter);
            String status = cbFilterStatus.getValue();
            String filterStatus = "Tất cả".equals(status) ? null : status;
            String keyword = txtSearch.getText().trim();

            // Load unassigned or pending grooming tasks
            List<BookingService> tasks = groomingBUS.getUnassignedGroomingTasks(
                    dateStr,
                    filterStatus,
                    keyword.isEmpty() ? null : keyword,
                    currentUser
            );

            ObservableList<BookingService> observableTasks = FXCollections.observableArrayList(tasks);
            taskTable.setItems(observableTasks);

        } catch (Exception e) {
            showError("Lỗi", "Không thể tải danh sách công việc: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadStaffList() {
        try {
            String branchId = getCurrentBranchId();
            List<Employee> staffList = groomingBUS.getWorkingEmployeesByBranch(branchId, currentUser);

            SearchableComboBoxUtil.setup(cbStaff, staffList, this::employeeDisplayText);

        } catch (Exception e) {
            showError("Lỗi", "Không thể tải danh sách nhân viên: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displaySelectedTask() {
        if (selectedTask == null) {
            return;
        }

        lblSelectedTaskId.setText(valueOrDash(selectedTask.getBookingServiceId()));
        lblSelectedPetName.setText(valueOrDash(selectedTask.getPetName()));
        lblSelectedService.setText(valueOrDash(selectedTask.getServiceName()));

        if (selectedTask.getScheduledAt() != null) {
            lblSelectedTime.setText(selectedTask.getScheduledAt().format(displayFormatter));
        } else {
            lblSelectedTime.setText("—");
        }

        cbStaff.getSelectionModel().clearSelection();
        txtAssignmentNote.clear();
    }

    private void displayStaffAvailability() {
        Employee selectedStaff = SearchableComboBoxUtil.getSelectedOrExactTextMatch(cbStaff);
        if (selectedStaff == null) {
            lblTodayTasks.setText("0");
            lblStaffStatus.setText("Có sẵn");
            return;
        }

        try {
            int taskCount = groomingBUS.getEmployeeTaskCount(
                    selectedStaff.getEmployeeId(),
                    getSelectedDate().format(dateFormatter),
                    currentUser
            );

            lblTodayTasks.setText(String.valueOf(taskCount));

            // Simple logic: if > 3 tasks, mark as busy
            if (taskCount >= 3) {
                lblStaffStatus.setText("Bận");
                lblStaffStatus.setStyle("-fx-text-fill: #d97706;");
            } else {
                lblStaffStatus.setText("Có sẵn");
                lblStaffStatus.setStyle("-fx-text-fill: #2d7c2d;");
            }

        } catch (Exception e) {
            showError("Lỗi", "Không thể tải thông tin nhân viên: " + e.getMessage());
        }
    }

    private void handleAssign() {
        if (selectedTask == null) {
            showWarning("Cảnh báo", "Vui lòng chọn công việc");
            return;
        }

        Employee selectedStaff = SearchableComboBoxUtil.getSelectedOrExactTextMatch(cbStaff);
        if (selectedStaff == null) {
            showWarning("Cảnh báo", "Vui lòng chọn nhân viên trong danh sách gợi ý");
            return;
        }

        try {
            String note = txtAssignmentNote.getText().trim();

            // Update the booking service with employee ID
            groomingBUS.assignEmployeeToTask(
                    selectedTask.getBookingServiceId(),
                    selectedStaff.getEmployeeId(),
                    note.isEmpty() ? null : note,
                    currentUser
            );

            showInfo("Thành công", "Phân công thành công");

            // Reload data
            loadUnassignedTasks();
            loadStaffList();
            handleClear();

        } catch (Exception e) {
            showError("Lỗi", "Không thể phân công: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleClear() {
        selectedTask = null;
        taskTable.getSelectionModel().clearSelection();
        lblSelectedTaskId.setText("—");
        lblSelectedPetName.setText("—");
        lblSelectedService.setText("—");
        lblSelectedTime.setText("—");
        cbStaff.getSelectionModel().clearSelection();
        txtAssignmentNote.clear();
        lblTodayTasks.setText("0");
        lblStaffStatus.setText("Có sẵn");
        lblStaffStatus.setStyle("-fx-text-fill: #2d7c2d;");
    }

    private String getCurrentBranchId() {
        if (currentUser.getEmployee() != null && currentUser.getEmployee().getBranchId() != null) {
            return currentUser.getEmployee().getBranchId();
        }

        String branchId = SessionManager.getInstance().getBranchId();
        if (branchId != null && !branchId.trim().isEmpty()) {
            return branchId.trim();
        }

        return "BR001";
    }

    private LocalDate getSelectedDate() {
        LocalDate selectedDate = dpFilterDate.getValue();
        if (selectedDate == null) {
            selectedDate = LocalDate.now();
            dpFilterDate.setValue(selectedDate);
        }
        return selectedDate;
    }

    private void showError(String title, String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }

    private void showWarning(String title, String message) {
        new Alert(Alert.AlertType.WARNING, message, ButtonType.OK).showAndWait();
    }

    private void showInfo(String title, String message) {
        new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).showAndWait();
    }

    private String employeeDisplayText(Employee employee) {
        if (employee == null) {
            return "";
        }
        String employeeId = employee.getEmployeeId() == null ? "" : employee.getEmployeeId().trim();
        String fullName = employee.getFullName() == null ? "" : employee.getFullName().trim();
        if (employeeId.isEmpty()) {
            return fullName;
        }
        if (fullName.isEmpty()) {
            return employeeId;
        }
        return employeeId + " - " + fullName;
    }

}
