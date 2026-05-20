package PetHotel.gui.controller;

import java.sql.SQLException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import PetHotel.bus.GroomingBUS;
import PetHotel.dao.EmployeeDAO;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.model.BookingService;
import PetHotel.model.Employee;
import PetHotel.util.Role;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * GroomingController — Quản lý lịch grooming.
 * 
 * Chức năng:
 *  - Xem lịch grooming theo ngày
 *  - Lọc theo nhân viên, trạng thái
 *  - Cập nhật trạng thái
 *  - Hỗ trợ role: Lễ tân, Nhân viên chăm sóc, Quản lý chi nhánh
 */
public class GroomingController {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private TextField txtSearch;
    @FXML private DatePicker selectedDate;
    @FXML private ComboBox<String> filterStaff;
    @FXML private ComboBox<String> filterGroomStatus;
    @FXML private TableView<GroomingRow> groomingTable;
    @FXML private Button btnCreateGrooming;
    @FXML private Button btnAssignGroomingStaff;
    @FXML private Button btnCancelGrooming;
    @FXML private Label panelTitle;
    @FXML private TableColumn<GroomingRow, String> colGrId;
    @FXML private TableColumn<GroomingRow, String> colGrTime;
    @FXML private TableColumn<GroomingRow, String> colGrPet;
    @FXML private TableColumn<GroomingRow, String> colGrOwner;
    @FXML private TableColumn<GroomingRow, String> colGrService;
    @FXML private TableColumn<GroomingRow, String> colGrStaff;
    @FXML private TableColumn<GroomingRow, String> colGrStatus;
    @FXML private TableColumn<GroomingRow, Void> colGrAction;

    @FXML private HBox assignedStatsBar;
    @FXML private Label lblTotalTasks;
    @FXML private Label lblPendingTasks;
    @FXML private Label lblInProgressTasks;
    @FXML private Label lblCompletedTasks;

    @FXML private VBox taskDetailPanel;
    @FXML private Label lblDetailCustomer;
    @FXML private Label lblDetailPet;
    @FXML private Label lblDetailPetSpecies;
    @FXML private Label lblDetailService;
    @FXML private Label lblDetailTime;
    @FXML private TextArea txtDetailNote;
    @FXML private Label lblDetailAddress;
    @FXML private Button btnStartTask;
    @FXML private Button btnCompleteTask;
    @FXML private Button btnUpdateStatus;
    @FXML private Button btnCloseDetails;

    private final GroomingBUS groomingBUS = new GroomingBUS();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private AppUser currentUser;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter fullDateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final ObservableList<GroomingRow> loadedRows = FXCollections.observableArrayList();
    private final Map<String, String> staffFilterToId = new HashMap<>();
    private boolean viewAllSchedules = false;
    private GroomingRow selectedGroomingRow;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Chưa đăng nhập");
            return;
        }

        setupRoleUI();

        if (btnAssignGroomingStaff != null) {
            btnAssignGroomingStaff.setOnAction(e -> onOpenStaffAssignment());
        }

        setupTableColumns();
        setupSelectionHandlers();
        setupDetailButtons();

        selectedDate.setValue(LocalDate.now());
        selectedDate.setOnAction(e -> {
            viewAllSchedules = false;
            loadGroomingSchedule();
        });

        ObservableList<String> statusList = FXCollections.observableArrayList(
            "Tất cả", "PENDING", "SCHEDULED", "IN_PROGRESS", "DONE", "CANCELLED"
        );
        filterGroomStatus.setItems(statusList);
        filterGroomStatus.setValue("Tất cả");
        filterGroomStatus.setOnAction(e -> loadGroomingSchedule());

        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, oldValue, newValue) -> applyClientFilter());
        }

        loadStaffList();
        loadGroomingSchedule();
    }

    // Mở màn hình phân công nhân viên grooming
    @FXML
    private void onOpenStaffAssignment() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/PetHotel/gui/view/GroomingStaffAssignment.fxml")
            );

            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Phân công nhân viên Grooming");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadGroomingSchedule();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(
                    Alert.AlertType.ERROR,
                    "Lỗi",
                    "Không thể mở màn hình phân công nhân viên: " + e.getMessage()
            );
        }
    }
    /**
     * Thiết lập các cột trong bảng
     */
    private void setupTableColumns() {
        groomingTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        groomingTable.setFixedCellSize(38);

        colGrId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));
        colGrTime.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTime()));
        colGrPet.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPetName()));
        colGrOwner.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOwnerName()));
        colGrService.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getServiceName()));
        colGrStaff.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStaffName()));
        colGrStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        colGrStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label badge = new Label(getStatusDisplayName(status));
                badge.getStyleClass().add("status-badge");
                badge.getStyleClass().add(statusStyleClass(status));
                setGraphic(badge);
                setText(null);
            }
        });

        // Action column
        colGrAction.setCellFactory(col -> new TableCell<GroomingRow, Void>() {
            private final Button btnStart = new Button("Bắt đầu");
            private final Button btnDone = new Button("Hoàn thành");
            private final Button btnCancel = new Button("Hủy");

            {
                btnStart.getStyleClass().addAll("action-btn", "action-btn-amber");
                btnDone.getStyleClass().addAll("action-btn", "action-btn-success");
                btnCancel.getStyleClass().addAll("action-btn", "action-btn-danger");
                
                btnStart.setOnAction(e -> handleStatusChange(getTableRow().getItem(), BookingService.STATUS_IN_PROGRESS));
                btnDone.setOnAction(e -> confirmCompleteTask(getTableRow().getItem()));
                btnCancel.setOnAction(e -> handleCancelGrooming(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    GroomingRow row = getTableRow().getItem();
                    HBox actions = new HBox(4);
                    actions.setStyle("-fx-alignment: CENTER;");
                    
                    if (BookingService.STATUS_PENDING.equals(row.getStatus())
                            || BookingService.STATUS_SCHEDULED.equals(row.getStatus())) {
                        actions.getChildren().add(btnStart);
                    }
                    if (BookingService.STATUS_IN_PROGRESS.equals(row.getStatus())) {
                        actions.getChildren().add(btnDone);
                    }
                    if (canCancelGrooming() && isCancelableStatus(row.getStatus())) {
                        actions.getChildren().add(btnCancel);
                    }
                    
                    setGraphic(actions.getChildren().isEmpty() ? null : actions);
                }
            }
        });
    }

    private void setupSelectionHandlers() {
        groomingTable.getSelectionModel().selectedItemProperty().addListener((obs, oldRow, newRow) -> {
            selectedGroomingRow = newRow;
            updateCancelButtonState();
            showTaskDetails(newRow);
        });
    }

    private void setupDetailButtons() {
        if (btnStartTask != null) {
            btnStartTask.setOnAction(e -> handleStatusChange(selectedGroomingRow, BookingService.STATUS_IN_PROGRESS));
        }

        if (btnCompleteTask != null) {
            btnCompleteTask.setOnAction(e -> confirmCompleteTask(selectedGroomingRow));
        }

        if (btnUpdateStatus != null) {
            btnUpdateStatus.setOnAction(e -> handleUpdateStatus());
        }

        if (btnCloseDetails != null) {
            btnCloseDetails.setOnAction(e -> hideTaskDetails());
        }
    }

    /**
     * Tải danh sách nhân viên grooming
     */
    private void loadStaffList() {
        try {
            if (filterStaff == null) {
                return;
            }
            if (isPetCareStaff()) {
                filterStaff.setItems(FXCollections.observableArrayList("Tất cả"));
                filterStaff.setValue("Tất cả");
                return;
            }

            ObservableList<String> staffList = FXCollections.observableArrayList();
            staffFilterToId.clear();
            staffList.add("Tất cả");

            List<Employee> employees = groomingBUS.getWorkingEmployeesByBranch(getCurrentBranchId(), currentUser);
            for (Employee employee : employees) {
                String displayText = employeeDisplayText(employee);
                staffList.add(displayText);
                staffFilterToId.put(displayText, employee.getEmployeeId());
            }

            filterStaff.setItems(staffList);
            filterStaff.setValue("Tất cả");
            filterStaff.setOnAction(e -> loadGroomingSchedule());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Tải lịch grooming theo ngày đã chọn
     */
    private void loadGroomingSchedule() {
        try {
            LocalDate selectedLocalDate = selectedDate.getValue();
            if (selectedLocalDate == null) {
                selectedLocalDate = LocalDate.now();
                selectedDate.setValue(selectedLocalDate);
            }

            // Update panel title
            if (panelTitle != null) {
                if (viewAllSchedules) {
                    panelTitle.setText("Lịch Grooming - Tất Cả");
                } else {
                    panelTitle.setText("Lịch Grooming Hôm Nay (" + selectedLocalDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")");
                }
            }

            String dateStr = selectedLocalDate.format(dateFormatter);   

            String selectedStatus = filterGroomStatus.getValue();
            String status = (selectedStatus == null || "Tất cả".equals(selectedStatus))
                    ? null
                    : selectedStatus;

            String staffId = null;

            if (currentUser.hasRole(Role.PET_CARE_STAFF)) {
                staffId = currentUser.getEmployeeId();
            }

            // Lễ tân và quản lý chi nhánh xem tất cả
            if (currentUser.hasRole(Role.RECEPTIONIST)
                    || currentUser.hasRole(Role.BRANCH_MANAGER)
                    || currentUser.hasRole(Role.ADMIN)) {
                staffId = getSelectedStaffId();
            }

            List<BookingService> bookingServices = viewAllSchedules
                    ? groomingBUS.getAllGroomingSchedules(staffId, status, currentUser)
                    : groomingBUS.getGroomingScheduleByDate(dateStr, staffId, status, currentUser);

            ObservableList<GroomingRow> rows = FXCollections.observableArrayList();

            for (BookingService bs : bookingServices) {
                rows.add(new GroomingRow(bs, employeeDAO, viewAllSchedules));
            }

            loadedRows.setAll(rows);
            applyClientFilter();
            updateCancelButtonState();

        } catch (ValidationException e) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Database", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Xử lý thay đổi trạng thái
     */
    private void handleStatusChange(GroomingRow row, String newStatus) {
        try {
            if (row == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn công việc grooming");
                return;
            }

            if (BookingService.STATUS_IN_PROGRESS.equals(newStatus)
                    && !BookingService.STATUS_PENDING.equals(row.getStatus())
                    && !BookingService.STATUS_SCHEDULED.equals(row.getStatus())) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chỉ công việc đang chờ mới có thể bắt đầu");
                return;
            }

            if (BookingService.STATUS_DONE.equals(newStatus)
                    && !BookingService.STATUS_IN_PROGRESS.equals(row.getStatus())) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chỉ công việc đang thực hiện mới có thể hoàn thành");
                return;
            }
            
            groomingBUS.updateGroomingStatus(row.getId(), newStatus, currentUser);
            
            showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                "Cập nhật trạng thái thành: " + getStatusDisplayName(newStatus));
            
            loadGroomingSchedule();
            hideTaskDetails();
            
        } catch (ValidationException e) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Database", e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleCancelGrooming(GroomingRow row) {
        if (row == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn lịch grooming cần hủy");
            return;
        }

        if (!canCancelGrooming()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Bạn không có quyền hủy lịch grooming");
            return;
        }

        if (!isCancelableStatus(row.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chỉ có thể hủy lịch chưa hoàn thành hoặc chưa bị hủy");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Hủy lịch grooming");
        confirm.setHeaderText("Xác nhận hủy lịch " + row.getId());
        confirm.setContentText(
                "Thú cưng: " + row.getPetName()
                + "\nKhách hàng: " + row.getOwnerName()
                + "\nDịch vụ: " + row.getServiceName()
                + "\nThời gian: " + row.getTime()
                + "\n\nLịch sẽ được chuyển sang trạng thái CANCELLED."
        );

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            groomingBUS.cancelGroomingSchedule(row.getId(), currentUser);

            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã hủy lịch grooming " + row.getId());

            loadGroomingSchedule();
            hideTaskDetails();

        } catch (ValidationException e) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Database", e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean canCancelGrooming() {
        return currentUser != null
                && (currentUser.hasRole(Role.RECEPTIONIST)
                    || currentUser.hasRole(Role.BRANCH_MANAGER)
                    || currentUser.hasRole(Role.ADMIN));
    }

    private boolean isCancelableStatus(String status) {
        return BookingService.STATUS_PENDING.equals(status)
                || BookingService.STATUS_SCHEDULED.equals(status)
                || BookingService.STATUS_IN_PROGRESS.equals(status);
    }

    private void updateCancelButtonState() {
        if (btnCancelGrooming == null) {
            return;
        }

        GroomingRow selectedRow = groomingTable.getSelectionModel().getSelectedItem();
        btnCancelGrooming.setDisable(!(canCancelGrooming()
                && selectedRow != null
                && isCancelableStatus(selectedRow.getStatus())));
    }

    /**
     * Lấy staff ID từ tên
     */
    private String getStaffIdFromName(String staffName) {
        if (staffName == null || "Tất cả".equals(staffName)) {
            return null;
        }
        return staffFilterToId.get(staffName);
    }

    private String getSelectedStaffId() {
        if (filterStaff == null) {
            return null;
        }
        return getStaffIdFromName(filterStaff.getValue());
    }

    @FXML
    public void onCreateGrooming(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/PetHotel/gui/view/GroomingBookingDialog.fxml")
            );

            Parent root = loader.load();

            GroomingBookingController controller = loader.getController();
            controller.setCurrentBranchId(getCurrentBranchId());

            controller.setOnSuccess(() -> {
                loadGroomingSchedule();
            });

            Stage stage = new Stage();
            stage.setTitle("Đặt lịch Grooming");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở form đặt lịch grooming: " + e.getMessage());
        }
    }

    @FXML
    public void onCancelSelectedGrooming(ActionEvent event) {
        handleCancelGrooming(groomingTable.getSelectionModel().getSelectedItem());
    }

    @FXML
    public void onPrevDay(ActionEvent event) {
        viewAllSchedules = false;
        selectedDate.setValue(selectedDate.getValue().minusDays(1));
        loadGroomingSchedule();
    }

    @FXML
    public void onNextDay(ActionEvent event) {
        viewAllSchedules = false;
        selectedDate.setValue(selectedDate.getValue().plusDays(1));
        loadGroomingSchedule();
    }

    @FXML
    public void onToday(ActionEvent event) {
        selectedDate.setValue(LocalDate.now());
        viewAllSchedules = false;
        loadGroomingSchedule();
    }

    @FXML
    public void onViewAllSchedules(ActionEvent event) {
        viewAllSchedules = true;

        if (selectedDate.getValue() == null) {
            selectedDate.setValue(LocalDate.now());
        }

        if (filterStaff != null) {
            filterStaff.setValue("Tất cả");
        }
        if (filterGroomStatus != null) {
            filterGroomStatus.setValue("Tất cả");
        }

        loadGroomingSchedule();
    }

    @FXML
    public void onSearch(ActionEvent event) {
        applyClientFilter();
    }

    @FXML
    public void onClearFilter(ActionEvent event) {
        if (txtSearch != null) {
            txtSearch.clear();
        }
        if (filterGroomStatus != null) {
            filterGroomStatus.setValue("Tất cả");
        }
        if (filterStaff != null) {
            filterStaff.setValue("Tất cả");
        }
        selectedDate.setValue(LocalDate.now());
        viewAllSchedules = false;
        loadGroomingSchedule();
    }

    private void applyClientFilter() {
        if (groomingTable == null) {
            return;
        }

        String keyword = txtSearch == null ? "" : txtSearch.getText();
        String normalizedKeyword = normalizeForSearch(keyword);
        ObservableList<GroomingRow> displayRows = FXCollections.observableArrayList();

        if (normalizedKeyword.isEmpty()) {
            displayRows.setAll(loadedRows);
        } else {
            for (GroomingRow row : loadedRows) {
                if (row.matchesKeyword(normalizedKeyword)) {
                    displayRows.add(row);
                }
            }
        }

        groomingTable.setItems(displayRows);
        updateAssignedStats(displayRows);
    }

    private void updateAssignedStats(List<GroomingRow> rows) {
        if (!isPetCareStaff() || assignedStatsBar == null) {
            return;
        }

        int pending = 0;
        int inProgress = 0;
        int done = 0;

        for (GroomingRow row : rows) {
            if (BookingService.STATUS_PENDING.equals(row.getStatus())
                    || BookingService.STATUS_SCHEDULED.equals(row.getStatus())) {
                pending++;
            } else if (BookingService.STATUS_IN_PROGRESS.equals(row.getStatus())) {
                inProgress++;
            } else if (BookingService.STATUS_DONE.equals(row.getStatus())) {
                done++;
            }
        }

        lblTotalTasks.setText(String.valueOf(rows.size()));
        lblPendingTasks.setText(String.valueOf(pending));
        lblInProgressTasks.setText(String.valueOf(inProgress));
        lblCompletedTasks.setText(String.valueOf(done));
    }

    private void showTaskDetails(GroomingRow row) {
        if (row == null || taskDetailPanel == null) {
            hideTaskDetails();
            return;
        }

        taskDetailPanel.setVisible(true);
        taskDetailPanel.setManaged(true);

        lblDetailCustomer.setText(valueOrDash(row.getOwnerName()));
        lblDetailPet.setText(valueOrDash(row.getPetName()));
        lblDetailPetSpecies.setText(valueOrDash(row.getPetSpecies()));
        lblDetailService.setText(valueOrDash(row.getServiceName()));
        lblDetailTime.setText(formatDetailTime(row));
        txtDetailNote.setText(valueOrEmpty(row.getNote()));
        lblDetailAddress.setText(valueOrDash(row.getCustomerAddress()));

        boolean canStart = BookingService.STATUS_PENDING.equals(row.getStatus())
                || BookingService.STATUS_SCHEDULED.equals(row.getStatus());
        boolean canComplete = BookingService.STATUS_IN_PROGRESS.equals(row.getStatus());
        boolean canUpdate = !BookingService.STATUS_DONE.equals(row.getStatus())
                && !BookingService.STATUS_CANCELLED.equals(row.getStatus());

        btnStartTask.setDisable(!canStart);
        btnCompleteTask.setDisable(!canComplete);
        btnUpdateStatus.setDisable(!canUpdate);
    }

    private void hideTaskDetails() {
        if (taskDetailPanel != null) {
            taskDetailPanel.setVisible(false);
            taskDetailPanel.setManaged(false);
        }
        selectedGroomingRow = null;
        if (groomingTable != null) {
            groomingTable.getSelectionModel().clearSelection();
        }
    }

    private void handleUpdateStatus() {
        if (selectedGroomingRow == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn công việc grooming");
            return;
        }

        if (BookingService.STATUS_DONE.equals(selectedGroomingRow.getStatus())
                || BookingService.STATUS_CANCELLED.equals(selectedGroomingRow.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Công việc đã kết thúc, không thể cập nhật tiếp");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                getStatusChoiceDefault(selectedGroomingRow.getStatus()),
                "Chờ thực hiện",
                "Đang thực hiện",
                "Hoàn thành"
        );

        dialog.setTitle("Cập nhật trạng thái grooming");
        dialog.setHeaderText("Cập nhật trạng thái cho công việc " + selectedGroomingRow.getId());
        dialog.setContentText("Chọn trạng thái mới:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        String newStatus = convertDisplayNameToStatus(result.get());
        if (newStatus == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Trạng thái không hợp lệ");
            return;
        }

        if (BookingService.STATUS_DONE.equals(newStatus)) {
            confirmCompleteTask(selectedGroomingRow);
            return;
        }

        handleStatusChange(selectedGroomingRow, newStatus);
    }

    private void confirmCompleteTask(GroomingRow row) {
        if (row == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn công việc grooming");
            return;
        }

        if (!BookingService.STATUS_IN_PROGRESS.equals(row.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chỉ công việc đang thực hiện mới có thể hoàn thành");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận hoàn thành grooming");
        confirm.setHeaderText("Xác nhận công việc grooming đã hoàn tất?");
        confirm.setContentText(
                "Mã công việc: " + row.getId()
                + "\nThú cưng: " + row.getPetName()
                + "\nDịch vụ: " + row.getServiceName()
        );

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            handleStatusChange(row, BookingService.STATUS_DONE);
        }
    }

    private String getStatusDisplayName(String status) {
        if (BookingService.STATUS_PENDING.equals(status)) {
            return "Chờ phân công";
        }
        if (BookingService.STATUS_SCHEDULED.equals(status)) {
            return "Chờ thực hiện";
        }
        if (BookingService.STATUS_IN_PROGRESS.equals(status)) {
            return "Đang thực hiện";
        }
        if (BookingService.STATUS_DONE.equals(status)) {
            return "Hoàn thành";
        }
        if (BookingService.STATUS_CANCELLED.equals(status)) {
            return "Đã hủy";
        }
        return valueOrDash(status);
    }

    private String convertDisplayNameToStatus(String displayName) {
        if ("Chờ thực hiện".equals(displayName)) {
            return BookingService.STATUS_SCHEDULED;
        }
        if ("Đang thực hiện".equals(displayName)) {
            return BookingService.STATUS_IN_PROGRESS;
        }
        if ("Hoàn thành".equals(displayName)) {
            return BookingService.STATUS_DONE;
        }
        return null;
    }

    private String getStatusChoiceDefault(String status) {
        if (BookingService.STATUS_IN_PROGRESS.equals(status)) {
            return "Đang thực hiện";
        }
        return "Chờ thực hiện";
    }

    private String statusStyleClass(String status) {
        if (BookingService.STATUS_IN_PROGRESS.equals(status)) {
            return "status-inprogress";
        }
        if (BookingService.STATUS_DONE.equals(status)) {
            return "status-done";
        }
        if (BookingService.STATUS_CANCELLED.equals(status)) {
            return "status-cancelled";
        }
        return "status-pending";
    }

    private String formatDetailTime(GroomingRow row) {
        if (row.getScheduledAt() == null) {
            return "—";
        }
        return row.getScheduledAt().format(fullDateTimeFormatter);
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "—" : value.trim();
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private boolean isPetCareStaff() {
        return currentUser != null && currentUser.hasRole(Role.PET_CARE_STAFF);
    }

    private String getCurrentBranchId() {
        if (currentUser != null
                && currentUser.getEmployee() != null
                && currentUser.getEmployee().getBranchId() != null
                && !currentUser.getEmployee().getBranchId().trim().isEmpty()) {
            return currentUser.getEmployee().getBranchId().trim();
        }

        String branchId = SessionManager.getInstance().getBranchId();
        if (branchId != null && !branchId.trim().isEmpty()) {
            return branchId.trim();
        }

        return "BR001";
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

    private static String normalizeForSearch(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Thiết lập UI theo role người dùng
    private void setupRoleUI() {
        boolean canCreateGrooming =
                currentUser.hasRole(Role.RECEPTIONIST);

        boolean canAssignStaff =
                currentUser.hasRole(Role.BRANCH_MANAGER);

        boolean canCancelGrooming = canCancelGrooming();
        boolean petCareStaff = isPetCareStaff();

        if (titleLabel != null && subtitleLabel != null && petCareStaff) {
            titleLabel.setText("Grooming");
            subtitleLabel.setText("Công việc grooming được phân công cho bạn");
        }

        if (btnCreateGrooming != null) {
            btnCreateGrooming.setVisible(canCreateGrooming);
            btnCreateGrooming.setManaged(canCreateGrooming);
        }

        if (btnAssignGroomingStaff != null) {
            btnAssignGroomingStaff.setVisible(canAssignStaff);
            btnAssignGroomingStaff.setManaged(canAssignStaff);
        }

        if (btnCancelGrooming != null) {
            btnCancelGrooming.setVisible(canCancelGrooming);
            btnCancelGrooming.setManaged(canCancelGrooming);
            btnCancelGrooming.setDisable(true);
        }

        if (filterStaff != null && petCareStaff) {
            filterStaff.setVisible(false);
            filterStaff.setManaged(false);
        }

        if (assignedStatsBar != null) {
            assignedStatsBar.setVisible(petCareStaff);
            assignedStatsBar.setManaged(petCareStaff);
        }

        if (colGrStaff != null && petCareStaff) {
            colGrStaff.setVisible(false);
        }

        if (colGrAction != null) {
            colGrAction.setVisible(true);
        }
    }
    /**
     * Inner class: GroomingRow — Dữ liệu hiển thị trong bảng
     */
    public static class GroomingRow {
        private final BookingService bookingService;
        private String id;
        private String time;
        private String petName;
        private String ownerName;
        private String serviceName;
        private String staffName;
        private String status;

        public GroomingRow(BookingService bs, EmployeeDAO employeeDAO) {
            this(bs, employeeDAO, false);
        }

        public GroomingRow(BookingService bs, EmployeeDAO employeeDAO, boolean showDate) {
            this.bookingService = bs;
            this.id = bs.getBookingServiceId();

            this.time = bs.getScheduledAt() != null
                    ? bs.getScheduledAt().format(showDate
                            ? DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                            : DateTimeFormatter.ofPattern("HH:mm"))
                    : "";

            this.petName = bs.getPetName() != null ? bs.getPetName() : "—";
            this.ownerName = bs.getCustomerName() != null ? bs.getCustomerName() : "—";
            this.serviceName = bs.getServiceName() != null ? bs.getServiceName() : "—";

            // Get staff name with ID
            if (bs.getEmployeeId() != null && !bs.getEmployeeId().isEmpty()) {
                try {
                    Employee employee = employeeDAO.findById(bs.getEmployeeId());
                    if (employee != null && employee.getFullName() != null) {
                        this.staffName = bs.getEmployeeId() + " - " + employee.getFullName();
                    } else {
                        this.staffName = bs.getEmployeeId() + " - (Không tìm thấy)";
                    }
                } catch (Exception e) {
                    this.staffName = bs.getEmployeeId() + " - (Lỗi tải tên)";
                    e.printStackTrace();
                }
            } else {
                this.staffName = "Chưa phân công";
            }

            this.status = bs.getStatus();
        }

        public String getId() { return id; }
        public String getTime() { return time; }
        public String getPetName() { return petName; }
        public String getOwnerName() { return ownerName; }
        public String getServiceName() { return serviceName; }
        public String getStaffName() { return staffName; }
        public String getStatus() { return status; }
        public OffsetDateTime getScheduledAt() { return bookingService.getScheduledAt(); }
        public String getPetSpecies() { return bookingService.getPetSpecies(); }
        public String getCustomerPhone() { return bookingService.getCustomerPhone(); }
        public String getCustomerAddress() { return bookingService.getCustomerAddress(); }
        public String getNote() { return bookingService.getNote(); }

        public boolean matchesKeyword(String normalizedKeyword) {
            return normalizeForSearch(id).contains(normalizedKeyword)
                    || normalizeForSearch(time).contains(normalizedKeyword)
                    || normalizeForSearch(petName).contains(normalizedKeyword)
                    || normalizeForSearch(ownerName).contains(normalizedKeyword)
                    || normalizeForSearch(serviceName).contains(normalizedKeyword)
                    || normalizeForSearch(staffName).contains(normalizedKeyword)
                    || normalizeForSearch(status).contains(normalizedKeyword)
                    || normalizeForSearch(getPetSpecies()).contains(normalizedKeyword)
                    || normalizeForSearch(getCustomerPhone()).contains(normalizedKeyword);
        }
    }
}
