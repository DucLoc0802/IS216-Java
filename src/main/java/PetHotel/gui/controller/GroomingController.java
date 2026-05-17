package PetHotel.gui.controller;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import PetHotel.bus.GroomingBUS;
import PetHotel.dao.EmployeeDAO;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.model.BookingService;
import PetHotel.util.Role;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

    @FXML private TextField txtSearch;
    @FXML private DatePicker selectedDate;
    @FXML private ComboBox<String> filterStaff;
    @FXML private ComboBox<String> filterGroomStatus;
    @FXML private TableView<GroomingRow> groomingTable;
    @FXML private Button btnCreateGrooming;
    @FXML private Button btnAssignGroomingStaff;
    @FXML private TableColumn<GroomingRow, String> colGrId;
    @FXML private TableColumn<GroomingRow, String> colGrTime;
    @FXML private TableColumn<GroomingRow, String> colGrPet;
    @FXML private TableColumn<GroomingRow, String> colGrOwner;
    @FXML private TableColumn<GroomingRow, String> colGrService;
    @FXML private TableColumn<GroomingRow, String> colGrStaff;
    @FXML private TableColumn<GroomingRow, String> colGrStatus;
    @FXML private TableColumn<GroomingRow, Void> colGrAction;
    
    @FXML private VBox staffWorkloadList;

    private final GroomingBUS groomingBUS = new GroomingBUS();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private AppUser currentUser;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("HH:mm");

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

        selectedDate.setValue(LocalDate.now());
        selectedDate.setOnAction(e -> loadGroomingSchedule());

        ObservableList<String> statusList = FXCollections.observableArrayList(
            "Tất cả", "PENDING", "SCHEDULED", "IN_PROGRESS", "DONE", "CANCELLED"
        );
        filterGroomStatus.setItems(statusList);
        filterGroomStatus.setValue("Tất cả");
        filterGroomStatus.setOnAction(e -> loadGroomingSchedule());

        loadStaffList();
        loadGroomingSchedule();
        loadStaffWorkload();
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
            loadStaffWorkload();

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
        colGrId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getId()));
        colGrTime.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTime()));
        colGrPet.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPetName()));
        colGrOwner.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOwnerName()));
        colGrService.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getServiceName()));
        colGrStaff.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStaffName()));
        colGrStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
        
        // Action column
        colGrAction.setCellFactory(col -> new TableCell<GroomingRow, Void>() {
            private final Button btnStart = new Button("Bắt đầu");
            private final Button btnDone = new Button("Hoàn thành");
            private final Button btnCancel = new Button("Hủy");

            {
                btnStart.setStyle("-fx-font-size: 11; -fx-padding: 4 8 4 8;");
                btnDone.setStyle("-fx-font-size: 11; -fx-padding: 4 8 4 8;");
                btnCancel.setStyle("-fx-font-size: 11; -fx-padding: 4 8 4 8;");
                
                btnStart.setOnAction(e -> handleStatusChange(getTableRow().getItem(), "IN_PROGRESS"));
                btnDone.setOnAction(e -> handleStatusChange(getTableRow().getItem(), "DONE"));
                btnCancel.setOnAction(e -> handleStatusChange(getTableRow().getItem(), "CANCELLED"));
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
                    
                    if ("PENDING".equals(row.getStatus()) || "SCHEDULED".equals(row.getStatus())) {
                        actions.getChildren().add(btnStart);
                    }
                    if ("IN_PROGRESS".equals(row.getStatus())) {
                        actions.getChildren().add(btnDone);
                    }
                    if (!"DONE".equals(row.getStatus()) && !"CANCELLED".equals(row.getStatus())) {
                        actions.getChildren().add(btnCancel);
                    }
                    
                    setGraphic(actions);
                }
            }
        });
    }

    /**
     * Tải danh sách nhân viên grooming
     */
    private void loadStaffList() {
        try {
            // Lấy danh sách nhân viên có role PET_CARE_STAFF (role_emp = '2')
            ObservableList<String> staffList = FXCollections.observableArrayList();
            staffList.add("Tất cả");
            
            // Tạm thời sử dụng giá trị mẫu
            staffList.addAll("Nhân viên 1", "Nhân viên 2", "Nhân viên 3");
            
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
            }
            String dateStr = selectedLocalDate.format(dateFormatter);
            
            String selectedStaff = filterStaff.getValue();
            String staffId = "Tất cả".equals(selectedStaff) ? null : getStaffIdFromName(selectedStaff);
            
            String selectedStatus = filterGroomStatus.getValue();
            String status = "Tất cả".equals(selectedStatus) ? null : selectedStatus;

            // Gọi BUS để lấy dữ liệu
            List<BookingService> bookingServices = groomingBUS.getGroomingScheduleByDate(dateStr, staffId, status, currentUser);

            // Convert sang GroomingRow và hiển thị
            ObservableList<GroomingRow> rows = FXCollections.observableArrayList();
            for (BookingService bs : bookingServices) {
                rows.add(new GroomingRow(bs));
            }
            groomingTable.setItems(rows);

        } catch (ValidationException e) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Database", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tải khối lượng công việc nhân viên hôm nay
     */
    private void loadStaffWorkload() {
        try {
            staffWorkloadList.getChildren().clear();
            
            // Tạm thời hiển thị thông tin mẫu
            
            Label lblLoading = new Label("Đang tải...");
            staffWorkloadList.getChildren().add(lblLoading);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Xử lý thay đổi trạng thái
     */
    private void handleStatusChange(GroomingRow row, String newStatus) {
        try {
            if (row == null) return;
            
            groomingBUS.updateGroomingStatus(row.getId(), newStatus, currentUser);
            
            showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                "Cập nhật trạng thái thành: " + newStatus);
            
            loadGroomingSchedule();
            loadStaffWorkload();
            
        } catch (ValidationException e) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Database", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lấy staff ID từ tên
     */
    private String getStaffIdFromName(String staffName) {
        // TODO: Implement mapping from name to ID
        return null;
    }

    @FXML
    public void onCreateGrooming(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/PetHotel/gui/view/GroomingBookingDialog.fxml")
            );

            Parent root = loader.load();

            GroomingBookingController controller = loader.getController();
            controller.setCurrentBranchId("BR001");

            controller.setOnSuccess(() -> {
                loadGroomingSchedule();
                loadStaffWorkload();
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
    public void onPrevDay(ActionEvent event) {
        selectedDate.setValue(selectedDate.getValue().minusDays(1));
    }

    @FXML
    public void onNextDay(ActionEvent event) {
        selectedDate.setValue(selectedDate.getValue().plusDays(1));
    }

    @FXML
    public void onToday(ActionEvent event) {
        selectedDate.setValue(LocalDate.now());
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
        System.out.println("DEBUG role = " + currentUser.getRole());
        System.out.println("DEBUG btnCreateGrooming = " + btnCreateGrooming);
        System.out.println("DEBUG btnAssignGroomingStaff = " + btnAssignGroomingStaff);

        boolean canCreateGrooming =
                currentUser.hasRole(Role.RECEPTIONIST);

        boolean canAssignStaff =
                currentUser.hasRole(Role.BRANCH_MANAGER);

        if (btnCreateGrooming != null) {
            btnCreateGrooming.setVisible(canCreateGrooming);
            btnCreateGrooming.setManaged(canCreateGrooming);
        }

        if (btnAssignGroomingStaff != null) {
            btnAssignGroomingStaff.setVisible(canAssignStaff);
            btnAssignGroomingStaff.setManaged(canAssignStaff);
        }

        if (colGrAction != null && currentUser.hasRole(Role.PET_CARE_STAFF)) {
            colGrAction.setVisible(false);
        }
    }
    /**
     * Inner class: GroomingRow — Dữ liệu hiển thị trong bảng
     */
    public static class GroomingRow {
        private String id;
        private String time;
        private String petName;
        private String ownerName;
        private String serviceName;
        private String staffName;
        private String status;

        public GroomingRow(BookingService bs) {
            this.id = bs.getBookingServiceId();
            this.time = bs.getScheduledAt() != null ? 
                bs.getScheduledAt().format(DateTimeFormatter.ofPattern("HH:mm")) : "";
            this.petName = "—"; // TODO: Load từ database
            this.ownerName = "—";
            this.serviceName = "Grooming";
            this.staffName = bs.getEmployeeId() != null ? bs.getEmployeeId() : "Chưa phân công";
            this.status = bs.getStatus();
        }

        public String getId() { return id; }
        public String getTime() { return time; }
        public String getPetName() { return petName; }
        public String getOwnerName() { return ownerName; }
        public String getServiceName() { return serviceName; }
        public String getStaffName() { return staffName; }
        public String getStatus() { return status; }
    }
}
