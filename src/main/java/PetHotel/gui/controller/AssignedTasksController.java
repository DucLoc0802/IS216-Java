package PetHotel.gui.controller;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import PetHotel.bus.GroomingBUS;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.model.BookingService;
import PetHotel.util.Role;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
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

public class AssignedTasksController {

    @FXML private Label lblStaffName;
    @FXML private Button btnRefresh;

    @FXML private DatePicker dpFilterDate;
    @FXML private ComboBox<String> cbFilterStatus;
    @FXML private TextField txtSearch;

    @FXML private Label lblTotalTasks;
    @FXML private Label lblPendingTasks;
    @FXML private Label lblInProgressTasks;
    @FXML private Label lblCompletedTasks;

    @FXML private TableView<BookingService> tasksTable;
    @FXML private TableColumn<BookingService, String> colTaskId;
    @FXML private TableColumn<BookingService, String> colScheduleTime;
    @FXML private TableColumn<BookingService, String> colPetName;
    @FXML private TableColumn<BookingService, String> colCustomerName;
    @FXML private TableColumn<BookingService, String> colCustomerPhone;
    @FXML private TableColumn<BookingService, String> colServiceName;
    @FXML private TableColumn<BookingService, String> colStatus;
    @FXML private TableColumn<BookingService, Void> colAction;

    @FXML private VBox detailsPanel;
    @FXML private Label lblDetailCustomer;
    @FXML private Label lblDetailPet;
    @FXML private Label lblDetailPetSpecies;
    @FXML private Label lblDetailService;
    @FXML private Label lblDetailTime;
    @FXML private TextArea txtDetailNote;
    @FXML private Label lblDetailAddress;

    @FXML private Button btnStartTask;
    @FXML private Button btnCompleteTask;
    @FXML private Button btnRecordWaste;
    @FXML private Button btnUpdateStatus;
    @FXML private Button btnCloseDetails;

    private final GroomingBUS groomingBUS = new GroomingBUS();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    private AppUser currentUser;
    private BookingService selectedTask;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser == null) {
            showError("Chưa đăng nhập.");
            return;
        }

        if (!currentUser.hasRole(Role.PET_CARE_STAFF)
                && !currentUser.hasRole(Role.BRANCH_MANAGER)
                && !currentUser.hasRole(Role.ADMIN)) {
            showError("Bạn không có quyền truy cập màn hình công việc được phân công.");
            return;
        }

        lblStaffName.setText("Nhân viên: " + currentUser.getEmployeeId());

        setupFilters();
        setupTableColumns();
        setupSelection();
        setupButtons();

        detailsPanel.setVisible(false);
        detailsPanel.setManaged(false);

        loadTasks();
        loadStatistics();
    }

    private void setupFilters() {
        cbFilterStatus.setItems(FXCollections.observableArrayList(
                "Tất cả", "SCHEDULED", "IN_PROGRESS", "DONE", "CANCELLED"
        ));
        cbFilterStatus.setValue("Tất cả");

        cbFilterStatus.setOnAction(e -> {
            loadTasks();
            loadStatistics();
        });

        dpFilterDate.setOnAction(e -> {
            loadTasks();
            loadStatistics();
        });

        txtSearch.textProperty().addListener((obs, oldValue, newValue) -> loadTasks());
    }

    private void setupTableColumns() {
        colTaskId.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getBookingServiceId()));

        colScheduleTime.setCellValueFactory(cellData -> {
            if (cellData.getValue().getScheduledAt() == null) {
                return new SimpleStringProperty("—");
            }
            return new SimpleStringProperty(cellData.getValue().getScheduledAt().format(timeFormatter));
        });

        colPetName.setCellValueFactory(cellData ->
                new SimpleStringProperty(valueOrDash(cellData.getValue().getPetName())));

        colCustomerName.setCellValueFactory(cellData ->
                new SimpleStringProperty(valueOrDash(cellData.getValue().getCustomerName())));

        colCustomerPhone.setCellValueFactory(cellData ->
                new SimpleStringProperty(valueOrDash(cellData.getValue().getCustomerPhone())));

        colServiceName.setCellValueFactory(cellData ->
                new SimpleStringProperty(valueOrDash(cellData.getValue().getServiceName())));

        colStatus.setCellValueFactory(cellData ->
                new SimpleStringProperty(valueOrDash(cellData.getValue().getStatus())));

        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnStart = new Button("Bắt đầu");
            private final Button btnDone = new Button("Hoàn thành");

            {
                btnStart.setOnAction(e -> {
                    BookingService task = getTableView().getItems().get(getIndex());
                    updateTaskStatus(task, BookingService.STATUS_IN_PROGRESS);
                });

                btnDone.setOnAction(e -> {
                    BookingService task = getTableView().getItems().get(getIndex());
                    confirmCompleteTask(task);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }

                BookingService task = getTableView().getItems().get(getIndex());
                HBox box = new HBox(6);

                if (BookingService.STATUS_SCHEDULED.equals(task.getStatus())) {
                    box.getChildren().add(btnStart);
                } else if (BookingService.STATUS_IN_PROGRESS.equals(task.getStatus())) {
                    box.getChildren().add(btnDone);
                }

                setGraphic(box.getChildren().isEmpty() ? null : box);
            }
        });
    }

    private void setupSelection() {
        tasksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldTask, newTask) -> {
            selectedTask = newTask;
            showTaskDetails(newTask);
        });
    }

    private void setupButtons() {
        btnRefresh.setOnAction(e -> {
            loadTasks();
            loadStatistics();
        });

        btnUpdateStatus.setOnAction(e -> {
            if (selectedTask == null) {
                showWarning("Vui lòng chọn công việc.");
                return;
            }

            handleUpdateStatus();
        });

        btnStartTask.setOnAction(e -> {
            if (selectedTask == null) {
                showWarning("Vui lòng chọn công việc.");
                return;
            }

            updateTaskStatus(selectedTask, BookingService.STATUS_IN_PROGRESS);
        });

        btnCompleteTask.setOnAction(e -> {
            if (selectedTask == null) {
                showWarning("Vui lòng chọn công việc.");
                return;
            }

            confirmCompleteTask(selectedTask);
        });

        btnRecordWaste.setOnAction(e -> {
            if (selectedTask == null) {
                showWarning("Vui lòng chọn công việc.");
                return;
            }
            if (!BookingService.STATUS_IN_PROGRESS.equals(selectedTask.getStatus())) {
                showWarning("Chỉ ghi nhận tiêu hao khi dịch vụ đang thực hiện.");
                return;
            }
            MaterialWasteController.openForTask(selectedTask, () -> {
                loadTasks();
                loadStatistics();
            });
        });

        btnCloseDetails.setOnAction(e -> {
            detailsPanel.setVisible(false);
            detailsPanel.setManaged(false);
        });
    }

    private void loadTasks() {
        try {
            LocalDate date = dpFilterDate.getValue();
            if (date == null) {
                date = LocalDate.now();
                dpFilterDate.setValue(date);
            }

            String status = cbFilterStatus.getValue();
            String filterStatus = "Tất cả".equals(status) ? null : status;

            String keyword = txtSearch.getText();
            if (keyword != null) {
                keyword = keyword.trim();
            }

            List<BookingService> tasks = groomingBUS.getEmployeeAssignedTasks(
                    currentUser.getEmployeeId(),
                    date.format(dateFormatter),
                    filterStatus,
                    keyword == null || keyword.isEmpty() ? null : keyword,
                    currentUser
            );

            tasksTable.setItems(FXCollections.observableArrayList(tasks));

        } catch (ValidationException e) {
            showWarning(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Lỗi database: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Không thể tải công việc: " + e.getMessage());
        }
    }

    private void loadStatistics() {
        try {
            LocalDate date = dpFilterDate.getValue();
            if (date == null) {
                date = LocalDate.now();
            }

            String dateStr = date.format(dateFormatter);
            String employeeId = currentUser.getEmployeeId();

            int scheduled = groomingBUS.getEmployeeTaskCountByStatus(
                    employeeId, dateStr, BookingService.STATUS_SCHEDULED, currentUser
            );

            int inProgress = groomingBUS.getEmployeeTaskCountByStatus(
                    employeeId, dateStr, BookingService.STATUS_IN_PROGRESS, currentUser
            );

            int done = groomingBUS.getEmployeeTaskCountByStatus(
                    employeeId, dateStr, BookingService.STATUS_DONE, currentUser
            );

            int total = groomingBUS.getEmployeeTaskCount(
                    employeeId, dateStr, currentUser
            );

            lblTotalTasks.setText(String.valueOf(total));
            lblPendingTasks.setText(String.valueOf(scheduled));
            lblInProgressTasks.setText(String.valueOf(inProgress));
            lblCompletedTasks.setText(String.valueOf(done));

        } catch (Exception e) {
            e.printStackTrace();
            showError("Không thể tải thống kê: " + e.getMessage());
        }
    }

    private void showTaskDetails(BookingService task) {
        if (task == null) {
            detailsPanel.setVisible(false);
            return;
        }

        detailsPanel.setVisible(true);
        detailsPanel.setManaged(true);

        lblDetailCustomer.setText(valueOrDash(task.getCustomerName()));
        lblDetailPet.setText(valueOrDash(task.getPetName()));
        lblDetailPetSpecies.setText(valueOrDash(task.getPetSpecies()));
        lblDetailService.setText(valueOrDash(task.getServiceName()));

        if (task.getScheduledAt() != null) {
            lblDetailTime.setText(task.getScheduledAt().format(timeFormatter));
        } else {
            lblDetailTime.setText("—");
        }

        txtDetailNote.setText(task.getNote() == null ? "" : task.getNote());
        lblDetailAddress.setText(valueOrDash(task.getCustomerAddress()));

        btnUpdateStatus.setDisable(BookingService.STATUS_DONE.equals(task.getStatus()));
        btnStartTask.setDisable(!BookingService.STATUS_SCHEDULED.equals(task.getStatus()));
        btnCompleteTask.setDisable(!BookingService.STATUS_IN_PROGRESS.equals(task.getStatus()));
        btnRecordWaste.setDisable(!BookingService.STATUS_IN_PROGRESS.equals(task.getStatus()));
    }

    private void handleUpdateStatus() {
        if (selectedTask == null) {
            showWarning("Vui lòng chọn công việc.");
            return;
        }

        if (BookingService.STATUS_DONE.equals(selectedTask.getStatus())) {
            showWarning("Dịch vụ đã hoàn thành, không thể cập nhật tiếp.");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                getStatusDisplayName(selectedTask.getStatus()),
                "Chờ thực hiện",
                "Đang thực hiện",
                "Hoàn thành"
        );

        dialog.setTitle("Cập nhật trạng thái dịch vụ");
        dialog.setHeaderText("Cập nhật trạng thái cho công việc: " + selectedTask.getBookingServiceId());
        dialog.setContentText("Chọn trạng thái mới:");

        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) {
            return;
        }

        String selectedStatusDisplay = result.get();
        String newStatus = convertDisplayNameToStatus(selectedStatusDisplay);

        if (newStatus == null) {
            showWarning("Trạng thái không hợp lệ.");
            return;
        }

        if (BookingService.STATUS_DONE.equals(newStatus)) {
            confirmCompleteTask(selectedTask);
            return;
        }

        updateTaskStatus(selectedTask, newStatus);
    }

    private String getStatusDisplayName(String status) {
        if (BookingService.STATUS_SCHEDULED.equals(status)) {
            return "Chờ thực hiện";
        }

        if (BookingService.STATUS_IN_PROGRESS.equals(status)) {
            return "Đang thực hiện";
        }

        if (BookingService.STATUS_DONE.equals(status)) {
            return "Hoàn thành";
        }

        return "Chờ thực hiện";
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

    private void confirmCompleteTask(BookingService task) {
        if (task == null) {
            showWarning("Vui lòng chọn công việc.");
            return;
        }

        if (!BookingService.STATUS_IN_PROGRESS.equals(task.getStatus())) {
            showWarning("Chỉ công việc đang thực hiện mới có thể xác nhận hoàn thành.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận hoàn thành dịch vụ");
        confirm.setHeaderText("Xác nhận dịch vụ grooming đã hoàn tất?");
        confirm.setContentText(
                "Mã công việc: " + task.getBookingServiceId()
                + "\nThú cưng: " + valueOrDash(task.getPetName())
                + "\nDịch vụ: " + valueOrDash(task.getServiceName())
        );

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            updateTaskStatus(task, BookingService.STATUS_DONE);
        }
    }

    private void updateTaskStatus(BookingService task, String newStatus) {
        try {
            if (task == null) {
                showWarning("Vui lòng chọn công việc.");
                return;
            }

            if (BookingService.STATUS_IN_PROGRESS.equals(newStatus)
                    && !BookingService.STATUS_SCHEDULED.equals(task.getStatus())) {
                showWarning("Chỉ công việc SCHEDULED mới có thể bắt đầu.");
                return;
            }

            if (BookingService.STATUS_DONE.equals(newStatus)
                    && !BookingService.STATUS_IN_PROGRESS.equals(task.getStatus())) {
                showWarning("Chỉ công việc IN_PROGRESS mới có thể hoàn thành.");
                return;
            }

            groomingBUS.updateGroomingStatus(
                    task.getBookingServiceId(),
                    newStatus,
                    currentUser
            );

            showInfo("Cập nhật trạng thái thành công.");

            loadTasks();
            loadStatistics();
            detailsPanel.setVisible(false);

        } catch (ValidationException e) {
            showWarning(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Lỗi database: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Không thể cập nhật trạng thái: " + e.getMessage());
        }
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "—" : value;
    }

    private void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).showAndWait();
    }

    private void showWarning(String message) {
        new Alert(Alert.AlertType.WARNING, message, ButtonType.OK).showAndWait();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }
}
