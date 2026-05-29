package PetHotel.gui.controller;

import PetHotel.bus.BookingBUS;
import PetHotel.bus.GroomingBUS;
import PetHotel.bus.InvoiceBUS;
import PetHotel.bus.RoomBUS;
import PetHotel.bus.EmployeeBUS;
import PetHotel.bus.AuditLogLocalService;
import PetHotel.bus.ReportBUS;
import PetHotel.dao.AppUserDAO;
import PetHotel.dao.EmployeeDAO;
import PetHotel.model.Booking;
import PetHotel.model.BookingService;
import PetHotel.model.Room;
import PetHotel.model.AppUser;
import PetHotel.model.Employee;
import PetHotel.model.Invoice;
import PetHotel.model.AuditLog;
import PetHotel.util.Role;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;

import java.text.DecimalFormat;
import java.util.Map;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardHomeController {

    // ── Stat labels ──
    @FXML private Label statBookingTotal;
    @FXML private Label statRoomOccupied;
    @FXML private Label statRevenue;
    @FXML private Label statLowStock;
    @FXML private Label statRestockNeeded;
    @FXML private Label statGroomingPending;

    @FXML private Label roomOccupied;
    @FXML private Label roomAvailable;
    @FXML private Label roomCleaning;

    // ── Customization containers ──
    @FXML private VBox cardBookingTotal;
    @FXML private VBox cardRoomOccupied;
    @FXML private VBox cardRevenue;
    @FXML private VBox cardLowStock;
    @FXML private VBox cardRestockNeeded;
    @FXML private VBox cardGroomingPending;

    @FXML private VBox quickActionsContainer;
    @FXML private VBox bookingTableContainer;
    @FXML private VBox lowStockAlertContainer;
    @FXML private VBox sideColumnContainer;

    @FXML private PieChart roomStatusChart;

    @FXML private BarChart<String, Number> monthlyStatsChart;
    @FXML private CategoryAxis monthXAxis;
    @FXML private NumberAxis bookingYAxis;
    @FXML private Label chartSectionHeader;

    // ── Booking Table ──
    @FXML private TableView<Booking> todayBookingTable;
    @FXML private TableColumn<Booking, String> colBkId;
    @FXML private TableColumn<Booking, String> colBkPet;
    @FXML private TableColumn<Booking, String> colBkOwner;
    @FXML private TableColumn<Booking, String> colBkRoom;
    @FXML private TableColumn<Booking, String> colBkCkin;
    @FXML private TableColumn<Booking, String> colBkStatus;

    @FXML private VBox groomingList;

    // ==========================================
    // 🔑 1. ADMIN DASHBOARD ELEMENTS
    // ==========================================
    @FXML private VBox adminDashboardContainer;
    @FXML private TableView<AppUser> loggedInUsersTable;
    @FXML private TableColumn<AppUser, String> colUserUsername;
    @FXML private TableColumn<AppUser, String> colUserFullName;
    @FXML private TableColumn<AppUser, String> colUserBranch;
    @FXML private TableColumn<AppUser, String> colUserLastLogin;
    @FXML private TableColumn<AppUser, String> colUserOnlineStatus;
    
    @FXML private TableView<AuditLog> auditLogTable;
    @FXML private TableColumn<AuditLog, String> colLogTime;
    @FXML private TableColumn<AuditLog, String> colLogUser;
    @FXML private TableColumn<AuditLog, String> colLogAction;
    @FXML private TableColumn<AuditLog, String> colLogDetails;

    // ==========================================
    // 🦮 2. CARE STAFF DASHBOARD ELEMENTS
    // ==========================================
    @FXML private VBox careStaffDashboardContainer;
    @FXML private TableView<BookingService> todayGroomingTable;
    @FXML private TableColumn<BookingService, String> colGroomId;
    @FXML private TableColumn<BookingService, String> colGroomPet;
    @FXML private TableColumn<BookingService, String> colGroomService;
    @FXML private TableColumn<BookingService, String> colGroomTime;
    @FXML private TableColumn<BookingService, String> colGroomStatus;
    
    @FXML private TableView<Room> waitingPetsRoomTable;
    @FXML private TableColumn<Room, String> colRoomNumber;
    @FXML private TableColumn<Room, String> colRoomType;
    @FXML private TableColumn<Room, String> colRoomPets;
    @FXML private TableColumn<Room, String> colRoomStatus;

    // ==========================================
    // 📈 3. BRANCH MANAGER DASHBOARD ELEMENTS
    // ==========================================
    @FXML private VBox managerDashboardContainer;
    @FXML private ComboBox<String> revenuePeriodSelector;
    @FXML private BarChart<String, Number> managerRevenueChart;
    @FXML private CategoryAxis revXAxis;
    @FXML private NumberAxis revYAxis;
    
    @FXML private TableView<StaffPerformance> staffPerformanceTable;
    @FXML private TableColumn<StaffPerformance, String> colStaffName;
    @FXML private TableColumn<StaffPerformance, String> colStaffRole;
    @FXML private TableColumn<StaffPerformance, String> colStaffBookings;
    @FXML private TableColumn<StaffPerformance, String> colStaffGroomings;

    // ==========================================
    // 👑 4. CEO DASHBOARD ELEMENTS
    // ==========================================
    @FXML private VBox ceoDashboardContainer;
    @FXML private ComboBox<String> ceoRevenuePeriodSelector;
    @FXML private BarChart<String, Number> ceoRevenueChart;
    @FXML private CategoryAxis ceoRevXAxis;
    @FXML private NumberAxis ceoRevYAxis;
    
    @FXML private BarChart<String, Number> ceoServiceUsageChart;
    @FXML private CategoryAxis ceoServiceXAxis;
    @FXML private NumberAxis ceoServiceYAxis;

    // Helper class for Manager Performance
    public static class StaffPerformance {
        private final String name;
        private final String role;
        private final int bookings;
        private final int groomings;

        public StaffPerformance(String name, String role, int bookings, int groomings) {
            this.name = name;
            this.role = role;
            this.bookings = bookings;
            this.groomings = groomings;
        }

        public String getName() { return name; }
        public String getRole() { return role; }
        public int getBookings() { return bookings; }
        public int getGroomings() { return groomings; }
    }

    private final ReportBUS reportBUS = new ReportBUS();
    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");

    @FXML
    public void initialize() {
        System.out.println("Đã load xong giao diện Dashboard Home.");
        setupTableColumns();

        AppUser currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            applyRoleDashboard(currentUser.getRole());
        }

        loadStatistics();
    }

    private void setupTableColumns() {
        // --- Standard Booking Table Columns ---
        if (colBkId != null) {
            colBkId.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getBookingId()));
        }
        if (colBkPet != null) {
            colBkPet.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getPetName() != null ? cellData.getValue().getPetName() : "—"));
        }
        if (colBkOwner != null) {
            colBkOwner.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getCustomerName() != null ? cellData.getValue().getCustomerName() : "—"));
        }
        if (colBkRoom != null) {
            colBkRoom.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getRoomNumber() != null ? cellData.getValue().getRoomNumber() : "—"));
        }
        if (colBkCkin != null) {
            colBkCkin.setCellValueFactory(cellData -> {
                if (cellData.getValue().getCheckinExpectedAt() != null) {
                    return new SimpleStringProperty(cellData.getValue().getCheckinExpectedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                }
                return new SimpleStringProperty("—");
            });
        }
        if (colBkStatus != null) {
            colBkStatus.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getStatus()));
        }

        // --- 🔑 Admin Columns ---
        if (colUserUsername != null) {
            colUserUsername.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUserName()));
        }
        if (colUserFullName != null) {
            colUserFullName.setCellValueFactory(cellData -> {
                Employee emp = cellData.getValue().getEmployee();
                return new SimpleStringProperty(emp != null ? emp.getFullName() : "—");
            });
        }
        if (colUserBranch != null) {
            colUserBranch.setCellValueFactory(cellData -> {
                Employee emp = cellData.getValue().getEmployee();
                return new SimpleStringProperty(emp != null && emp.getBranchId() != null ? emp.getBranchId() : "—");
            });
        }
        if (colUserLastLogin != null) {
            colUserLastLogin.setCellValueFactory(cellData -> {
                if (cellData.getValue().getLastLogin() != null) {
                    return new SimpleStringProperty(cellData.getValue().getLastLogin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                }
                return new SimpleStringProperty("—");
            });
        }
        if (colUserOnlineStatus != null) {
            colUserOnlineStatus.setCellValueFactory(cellData -> {
                OffsetDateTime now = OffsetDateTime.now();
                OffsetDateTime lastLogin = cellData.getValue().getLastLogin();
                if (lastLogin != null) {
                    long minutes = Math.abs(Duration.between(lastLogin.toInstant(), now.toInstant()).toMinutes());
                    return new SimpleStringProperty(minutes <= 5 ? "Online" : "Offline");
                }
                return new SimpleStringProperty("Offline");
            });
            colUserOnlineStatus.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        if ("Online".equals(item)) {
                            setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #95a5a6;");
                        }
                    }
                }
            });
        }

        if (colLogTime != null) {
            colLogTime.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM HH:mm:ss"))));
        }
        if (colLogUser != null) {
            colLogUser.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmployeeName()));
        }
        if (colLogAction != null) {
            colLogAction.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAction()));
        }
        if (colLogDetails != null) {
            colLogDetails.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDetails()));
        }

        // --- 🦮 Care Staff Columns ---
        if (colGroomId != null) {
            colGroomId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookingServiceId()));
        }
        if (colGroomPet != null) {
            colGroomPet.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPetName() != null ? cellData.getValue().getPetName() : "—"));
        }
        if (colGroomService != null) {
            colGroomService.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getServiceName() != null ? cellData.getValue().getServiceName() : "—"));
        }
        if (colGroomTime != null) {
            colGroomTime.setCellValueFactory(cellData -> {
                if (cellData.getValue().getScheduledAt() != null) {
                    return new SimpleStringProperty(cellData.getValue().getScheduledAt().format(DateTimeFormatter.ofPattern("HH:mm")));
                }
                return new SimpleStringProperty("—");
            });
        }
        if (colGroomStatus != null) {
            colGroomStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        }

        if (colRoomNumber != null) {
            colRoomNumber.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoomNumber()));
        }
        if (colRoomType != null) {
            colRoomType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTypeName()));
        }
        if (colRoomPets != null) {
            colRoomPets.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCurrentPetNames() != null ? cellData.getValue().getCurrentPetNames() : "—"));
        }
        if (colRoomStatus != null) {
            colRoomStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        }

        // --- 📈 Manager Columns ---
        if (colStaffName != null) {
            colStaffName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        }
        if (colStaffRole != null) {
            colStaffRole.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRole()));
        }
        if (colStaffBookings != null) {
            colStaffBookings.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getBookings())));
        }
        if (colStaffGroomings != null) {
            colStaffGroomings.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getGroomings())));
        }
    }

    private void applyRoleDashboard(Role role) {
        if (role == null) return;
        
        // Bắt đầu bằng việc ẩn tất cả các container chuyên biệt
        hideNode(adminDashboardContainer);
        hideNode(careStaffDashboardContainer);
        hideNode(managerDashboardContainer);
        hideNode(ceoDashboardContainer);

        switch (role) {
            case PET_CARE_STAFF:
                hideNode(cardRevenue);
                hideNode(cardLowStock);
                hideNode(cardRestockNeeded);
                hideNode(quickActionsContainer);
                hideNode(bookingTableContainer);
                hideNode(lowStockAlertContainer);
                hideNode(sideColumnContainer);
                
                showNode(careStaffDashboardContainer);
                break;

            case RECEPTIONIST:
                hideNode(cardLowStock);
                hideNode(cardRestockNeeded);
                hideNode(lowStockAlertContainer);
                break;

            case ADMIN:
                hideNode(quickActionsContainer);
                hideNode(cardRevenue);
                hideNode(cardLowStock);
                hideNode(cardRestockNeeded);
                hideNode(lowStockAlertContainer);
                hideNode(bookingTableContainer);
                hideNode(sideColumnContainer);

                showNode(adminDashboardContainer);
                break;

            case BRANCH_MANAGER:
                hideNode(quickActionsContainer);
                hideNode(lowStockAlertContainer);
                hideNode(bookingTableContainer);
                hideNode(sideColumnContainer);

                showNode(managerDashboardContainer);
                break;

            case CEO:
                hideNode(quickActionsContainer);
                hideNode(lowStockAlertContainer);
                hideNode(bookingTableContainer);
                hideNode(sideColumnContainer);

                showNode(ceoDashboardContainer);
                break;

            default:
                break;
        }
    }

    private void hideNode(javafx.scene.Node node) {
        if (node != null) {
            node.setVisible(false);
            node.setManaged(false);
        }
    }

    private void showNode(javafx.scene.Node node) {
        if (node != null) {
            node.setVisible(true);
            node.setManaged(true);
        }
    }

    private void loadStatistics() {
        try {
            AppUser currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) return;

            // 1. Tải các thông số tổng hợp từ Database thật qua reportBUS (KPI cards)
            Map<String, Number> summary = reportBUS.getDashboardSummary();
            int inUse = number(summary, "roomInUse").intValue();
            int available = number(summary, "roomAvailable").intValue();
            int maintenance = number(summary, "roomMaintenance").intValue();
            int totalRoom = number(summary, "roomTotal").intValue();
            int lowStock = number(summary, "lowStock").intValue();

            setText(statBookingTotal, String.valueOf(number(summary, "todayBooking").intValue()));
            setText(statRoomOccupied, inUse + "/" + totalRoom);
            setText(statRevenue, moneyFormat.format(number(summary, "todayRevenue").doubleValue()) + " VNĐ");
            setText(statLowStock, String.valueOf(lowStock));
            setText(statRestockNeeded, String.valueOf(lowStock));
            setText(statGroomingPending, String.valueOf(number(summary, "groomingPending").intValue()));

            setText(roomOccupied, String.valueOf(inUse));
            setText(roomAvailable, String.valueOf(available));
            setText(roomCleaning, String.valueOf(maintenance));

            if (roomStatusChart != null) {
                roomStatusChart.getData().clear();
                if (inUse > 0) {
                    roomStatusChart.getData().add(new PieChart.Data("Đang dùng (" + inUse + ")", inUse));
                }
                if (available > 0) {
                    roomStatusChart.getData().add(new PieChart.Data("Trống (" + available + ")", available));
                }
                if (maintenance > 0) {
                    roomStatusChart.getData().add(new PieChart.Data("Bảo trì (" + maintenance + ")", maintenance));
                }
            }

            // Tải danh sách Booking hôm nay
            List<Booking> allBookings = new BookingBUS().getAllBookings();
            LocalDate today = LocalDate.now();
            List<Booking> todayBookings = allBookings.stream()
                .filter(b -> b.getCheckinExpectedAt() != null && b.getCheckinExpectedAt().toLocalDate().equals(today))
                .collect(Collectors.toList());
            
            if (todayBookingTable != null) {
                todayBookingTable.setItems(FXCollections.observableArrayList(todayBookings));
            }

            // =========================================================
            // 🛎️ 2. RECEPTIONIST SPECIFIC STATS (WEEKLY DAILY CHART)
            // =========================================================
            if (currentUser.getRole() == Role.RECEPTIONIST) {
                if (monthlyStatsChart != null) {
                    monthlyStatsChart.getData().clear();
                    if (chartSectionHeader != null) {
                        chartSectionHeader.setText("Thống Kê Đặt Phòng Hàng Ngày (Tuần Hiện Tại)");
                    }
                    if (monthXAxis != null) {
                        monthXAxis.setLabel("Ngày Trong Tuần");
                    }
                    
                    LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
                    LocalDate endOfWeek = startOfWeek.plusDays(6);
                    
                    int[] weeklyCounts = new int[7];
                    for (Booking b : allBookings) {
                        if (b.getCheckinExpectedAt() != null) {
                            LocalDate bDate = b.getCheckinExpectedAt().toLocalDate();
                            if (!bDate.isBefore(startOfWeek) && !bDate.isAfter(endOfWeek)) {
                                int dayIndex = bDate.getDayOfWeek().getValue() - 1;
                                if (dayIndex >= 0 && dayIndex < 7) {
                                    weeklyCounts[dayIndex]++;
                                }
                            }
                        }
                    }
                    
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Đặt Phòng");
                    String[] dayLabels = {"Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"};
                    for (int i = 0; i < 7; i++) {
                        series.getData().add(new XYChart.Data<>(dayLabels[i], weeklyCounts[i]));
                    }
                    monthlyStatsChart.getData().add(series);
                }
            } else if (currentUser.getRole() == Role.CEO || currentUser.getRole() == Role.BRANCH_MANAGER) {
                // Default monthly stats if needed
                if (monthlyStatsChart != null && currentUser.getRole() == Role.CEO) {
                    monthlyStatsChart.getData().clear();
                    int[] monthlyCounts = new int[12];
                    int currentYear = LocalDate.now().getYear();
                    for (Booking b : allBookings) {
                        if (b.getCheckinExpectedAt() != null && b.getCheckinExpectedAt().getYear() == currentYear) {
                            int monthIndex = b.getCheckinExpectedAt().getMonthValue() - 1;
                            if (monthIndex >= 0 && monthIndex < 12) {
                                monthlyCounts[monthIndex]++;
                            }
                        }
                    }
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Đặt Phòng");
                    String[] monthLabels = {"Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};
                    for (int m = 0; m < 12; m++) {
                        series.getData().add(new XYChart.Data<>(monthLabels[m], monthlyCounts[m]));
                    }
                    monthlyStatsChart.getData().add(series);
                }
            }

            // =========================================================
            // 🔑 3. ADMIN SPECIFIC LOAD
            // =========================================================
            if (currentUser.getRole() == Role.ADMIN) {
                // Tải danh sách user đăng nhập hôm nay
                List<AppUser> loggedInToday = new AppUserDAO().findLoggedInToday();
                if (loggedInUsersTable != null) {
                    loggedInUsersTable.setItems(FXCollections.observableArrayList(loggedInToday));
                }

                // Tải danh sách audit log từ file JSON
                List<AuditLog> logs = AuditLogLocalService.getAllLogs();
                if (auditLogTable != null) {
                    auditLogTable.setItems(FXCollections.observableArrayList(logs));
                }
            }

            // =========================================================
            // 🦮 4. CARE STAFF SPECIFIC LOAD
            // =========================================================
            if (currentUser.getRole() == Role.PET_CARE_STAFF) {
                // Tải lịch grooming được giao cho nhân viên hôm nay
                List<BookingService> myGroomingToday = new GroomingBUS().getEmployeeScheduleToday(currentUser.getEmployeeId());
                if (todayGroomingTable != null) {
                    todayGroomingTable.setItems(FXCollections.observableArrayList(myGroomingToday));
                }

                // Tải danh sách phòng đang có thú cưng chờ
                List<Room> allRooms = new RoomBUS().getAllRooms();
                List<Room> waitingRooms = allRooms.stream()
                    .filter(r -> r.getCurrentPetNames() != null && !r.getCurrentPetNames().trim().isEmpty())
                    .collect(Collectors.toList());
                if (waitingPetsRoomTable != null) {
                    waitingPetsRoomTable.setItems(FXCollections.observableArrayList(waitingRooms));
                }
            }

            // =========================================================
            // 📈 5. BRANCH MANAGER SPECIFIC LOAD
            // =========================================================
            if (currentUser.getRole() == Role.BRANCH_MANAGER && currentUser.getEmployee() != null) {
                String branchId = currentUser.getEmployee().getBranchId();

                // Setup selector và sự kiện
                if (revenuePeriodSelector != null) {
                    revenuePeriodSelector.setItems(FXCollections.observableArrayList("Trong ngày", "Trong tuần", "Trong tháng"));
                    if (revenuePeriodSelector.getSelectionModel().isEmpty()) {
                        revenuePeriodSelector.getSelectionModel().select(0);
                    }
                    revenuePeriodSelector.setOnAction(e -> updateBranchRevenueChart(branchId));
                }

                // Vẽ chart doanh thu chi nhánh ban đầu
                updateBranchRevenueChart(branchId);

                // Tải hiệu suất nhân viên chi nhánh
                List<Employee> branchEmployees = new EmployeeBUS().searchEmployees(null, null, "Dang hoat dong", branchId);
                List<StaffPerformance> performances = new ArrayList<>();
                EmployeeBUS empBUS = new EmployeeBUS();
                for (Employee emp : branchEmployees) {
                    int[] summaryPerf = empBUS.getPerformanceSummary(emp.getEmployeeId());
                    String roleName = emp.getRoleCode() != null ? mapRoleCodeToName(emp.getRoleCode()) : "Nhân viên";
                    performances.add(new StaffPerformance(emp.getFullName(), roleName, summaryPerf[0], summaryPerf[1]));
                }
                
                if (staffPerformanceTable != null) {
                    staffPerformanceTable.setItems(FXCollections.observableArrayList(performances));
                }
            }

            // =========================================================
            // 👑 6. CEO SPECIFIC LOAD
            // =========================================================
            if (currentUser.getRole() == Role.CEO) {
                // Setup selector và sự kiện
                if (ceoRevenuePeriodSelector != null) {
                    ceoRevenuePeriodSelector.setItems(FXCollections.observableArrayList("Trong ngày", "Trong tuần", "Trong tháng"));
                    if (ceoRevenuePeriodSelector.getSelectionModel().isEmpty()) {
                        ceoRevenuePeriodSelector.getSelectionModel().select(0);
                    }
                    ceoRevenuePeriodSelector.setOnAction(e -> updateSystemRevenueChart());
                }

                // Vẽ chart doanh thu toàn hệ thống ban đầu
                updateSystemRevenueChart();

                // Thống kê dịch vụ trong ngày
                updateSystemServiceUsageChart();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể tải dữ liệu dashboard: " + e.getMessage());
        }
    }

    private void updateBranchRevenueChart(String branchId) {
        if (managerRevenueChart == null) return;
        managerRevenueChart.getData().clear();
        if (revXAxis != null) {
            revXAxis.getCategories().clear();
        }

        try {
            List<Invoice> branchInvoices = new InvoiceBUS().getBranchInvoices(branchId);
            String selectedPeriod = revenuePeriodSelector.getSelectionModel().getSelectedItem();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Doanh Thu");

            if ("Trong ngày".equalsIgnoreCase(selectedPeriod)) {
                revXAxis.setLabel("Khung Giờ");
                double[] hourlyRev = new double[12]; // 0-2h, 2-4h, ... 22-24h
                LocalDate today = LocalDate.now();

                for (Invoice inv : branchInvoices) {
                    if (inv.getCreateDate() != null && !"CANCELLED".equalsIgnoreCase(inv.getStatus())) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(inv.getCreateDate());
                        LocalDate invDate = LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
                        if (invDate.equals(today)) {
                            int hour = cal.get(Calendar.HOUR_OF_DAY);
                            int index = hour / 2;
                            if (index >= 0 && index < 12) {
                                hourlyRev[index] += inv.getTotalAmount();
                            }
                        }
                    }
                }

                String[] hourLabels = {"0-2h", "2-4h", "4-6h", "6-8h", "8-10h", "10-12h", "12-14h", "14-16h", "16-18h", "18-20h", "20-22h", "22-24h"};
                for (int i = 0; i < 12; i++) {
                    series.getData().add(new XYChart.Data<>(hourLabels[i], hourlyRev[i]));
                }

            } else if ("Trong tuần".equalsIgnoreCase(selectedPeriod)) {
                revXAxis.setLabel("Thứ");
                double[] dailyRev = new double[7];
                LocalDate today = LocalDate.now();
                LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
                LocalDate endOfWeek = startOfWeek.plusDays(6);

                for (Invoice inv : branchInvoices) {
                    if (inv.getCreateDate() != null && !"CANCELLED".equalsIgnoreCase(inv.getStatus())) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(inv.getCreateDate());
                        LocalDate invDate = LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
                        if (!invDate.isBefore(startOfWeek) && !invDate.isAfter(endOfWeek)) {
                            int dayIndex = invDate.getDayOfWeek().getValue() - 1;
                            if (dayIndex >= 0 && dayIndex < 7) {
                                dailyRev[dayIndex] += inv.getTotalAmount();
                            }
                        }
                    }
                }

                String[] dayLabels = {"Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"};
                for (int i = 0; i < 7; i++) {
                    series.getData().add(new XYChart.Data<>(dayLabels[i], dailyRev[i]));
                }

            } else if ("Trong tháng".equalsIgnoreCase(selectedPeriod)) {
                revXAxis.setLabel("Ngày Trong Tháng");
                LocalDate today = LocalDate.now();
                int lengthOfMonth = today.lengthOfMonth();
                double[] dailyRev = new double[lengthOfMonth];

                for (Invoice inv : branchInvoices) {
                    if (inv.getCreateDate() != null && !"CANCELLED".equalsIgnoreCase(inv.getStatus())) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(inv.getCreateDate());
                        LocalDate invDate = LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
                        if (invDate.getYear() == today.getYear() && invDate.getMonth() == today.getMonth()) {
                            int dayIndex = invDate.getDayOfMonth() - 1;
                            if (dayIndex >= 0 && dayIndex < lengthOfMonth) {
                                dailyRev[dayIndex] += inv.getTotalAmount();
                            }
                        }
                    }
                }

                for (int i = 0; i < lengthOfMonth; i++) {
                    series.getData().add(new XYChart.Data<>(String.valueOf(i + 1), dailyRev[i]));
                }
            }

            managerRevenueChart.getData().add(series);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String mapRoleCodeToName(String code) {
        return switch (code.trim()) {
            case "0" -> "Admin";
            case "1" -> "Lễ Tân";
            case "2" -> "Chăm Sóc Thú Cưng";
            case "3" -> "Quản Lý Chi Nhánh";
            case "4" -> "Giám Đốc (CEO)";
            default -> "Nhân viên";
        };
    }

    private Number number(Map<String, Number> summary, String key) {
        return summary.getOrDefault(key, 0);
    }

    private void setText(Label label, String value) {
        if (label != null) {
            label.setText(value);
        }
    }

    @FXML
    public void onQuickCreateBooking(ActionEvent event) {
        System.out.println("Mở form Tạo Booking nhanh...");
    }

    @FXML
    public void onRefreshDashboard(ActionEvent event) {
        loadStatistics();
    }

    @FXML
    public void onQuickCheckin(ActionEvent event) {
        System.out.println("Mở form Check-in...");
    }

    @FXML
    public void onQuickCheckout(ActionEvent event) {
        System.out.println("Mở form Check-out...");
    }

    @FXML
    public void onQuickCreateInvoice(ActionEvent event) {
        System.out.println("Mở form Tạo Hóa Đơn nhanh...");
    }

    @FXML
    public void onQuickAddCustomer(ActionEvent event) {
        System.out.println("Mở form Thêm Khách Hàng nhanh...");
    }

    @FXML
    public void onQuickCheckRoom(ActionEvent event) {
        System.out.println("Mở bảng Kiểm tra trạng thái phòng...");
    }

    @FXML
    public void onViewAllBooking(ActionEvent event) {
        System.out.println("Chuyển hướng sang trang Quản lý Booking chi tiết...");
    }

    @FXML
    public void onViewAllGrooming(ActionEvent event) {
        System.out.println("Chuyển hướng sang trang Quản lý Grooming chi tiết...");
    }

    @FXML
    public void onViewInventory(ActionEvent event) {
        System.out.println("Chuyển hướng sang trang Quản lý Tồn Kho...");
    }

<<<<<<< HEAD
    private void updateSystemRevenueChart() {
        if (ceoRevenueChart == null) return;
        ceoRevenueChart.getData().clear();
        if (ceoRevXAxis != null) {
            ceoRevXAxis.getCategories().clear();
        }

        try {
            List<Invoice> allInvoices = new InvoiceBUS().searchInvoices(null, null, null, null);
            String selectedPeriod = ceoRevenuePeriodSelector.getSelectionModel().getSelectedItem();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Doanh Thu");

            if ("Trong ngày".equalsIgnoreCase(selectedPeriod)) {
                ceoRevXAxis.setLabel("Khung Giờ");
                double[] hourlyRev = new double[12];
                LocalDate today = LocalDate.now();

                for (Invoice inv : allInvoices) {
                    if (inv.getCreateDate() != null && !"CANCELLED".equalsIgnoreCase(inv.getStatus())) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(inv.getCreateDate());
                        LocalDate invDate = LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
                        if (invDate.equals(today)) {
                            int hour = cal.get(Calendar.HOUR_OF_DAY);
                            int index = hour / 2;
                            if (index >= 0 && index < 12) {
                                hourlyRev[index] += inv.getTotalAmount();
                            }
                        }
                    }
                }

                String[] hourLabels = {"0-2h", "2-4h", "4-6h", "6-8h", "8-10h", "10-12h", "12-14h", "14-16h", "16-18h", "18-20h", "20-22h", "22-24h"};
                for (int i = 0; i < 12; i++) {
                    series.getData().add(new XYChart.Data<>(hourLabels[i], hourlyRev[i]));
                }

            } else if ("Trong tuần".equalsIgnoreCase(selectedPeriod)) {
                ceoRevXAxis.setLabel("Thứ");
                double[] dailyRev = new double[7];
                LocalDate today = LocalDate.now();
                LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
                LocalDate endOfWeek = startOfWeek.plusDays(6);

                for (Invoice inv : allInvoices) {
                    if (inv.getCreateDate() != null && !"CANCELLED".equalsIgnoreCase(inv.getStatus())) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(inv.getCreateDate());
                        LocalDate invDate = LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
                        if (!invDate.isBefore(startOfWeek) && !invDate.isAfter(endOfWeek)) {
                            int dayIndex = invDate.getDayOfWeek().getValue() - 1;
                            if (dayIndex >= 0 && dayIndex < 7) {
                                dailyRev[dayIndex] += inv.getTotalAmount();
                            }
                        }
                    }
                }

                String[] dayLabels = {"Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"};
                for (int i = 0; i < 7; i++) {
                    series.getData().add(new XYChart.Data<>(dayLabels[i], dailyRev[i]));
                }

            } else if ("Trong tháng".equalsIgnoreCase(selectedPeriod)) {
                ceoRevXAxis.setLabel("Tuần/Ngày Trong Tháng");
                LocalDate today = LocalDate.now();
                
                double w1 = 0; // Days 1 to 7
                double w2 = 0; // Days 8 to 14
                double w3 = 0; // Days 15 to 21
                double wOthers = 0; // Days 22 to end of month

                for (Invoice inv : allInvoices) {
                    if (inv.getCreateDate() != null && !"CANCELLED".equalsIgnoreCase(inv.getStatus())) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(inv.getCreateDate());
                        LocalDate invDate = LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
                        if (invDate.getYear() == today.getYear() && invDate.getMonth() == today.getMonth()) {
                            int day = invDate.getDayOfMonth();
                            if (day >= 1 && day <= 7) {
                                w1 += inv.getTotalAmount();
                            } else if (day >= 8 && day <= 14) {
                                w2 += inv.getTotalAmount();
                            } else if (day >= 15 && day <= 21) {
                                w3 += inv.getTotalAmount();
                            } else {
                                wOthers += inv.getTotalAmount();
                            }
                        }
                    }
                }

                series.getData().add(new XYChart.Data<>("Tuần 1", w1));
                series.getData().add(new XYChart.Data<>("Tuần 2", w2));
                series.getData().add(new XYChart.Data<>("Tuần 3", w3));
                series.getData().add(new XYChart.Data<>("Ngày còn lại", wOthers));
            }

            ceoRevenueChart.getData().add(series);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateSystemServiceUsageChart() {
        if (ceoServiceUsageChart == null) return;
        ceoServiceUsageChart.getData().clear();

        try {
            AppUser currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) return;

            String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            List<BookingService> todayServices = new GroomingBUS().getGroomingScheduleByDate(todayStr, null, null, currentUser);

            // Group services by name and count their frequency
            java.util.Map<String, Long> serviceCounts = todayServices.stream()
                .filter(bs -> bs.getServiceName() != null && !"CANCELLED".equalsIgnoreCase(bs.getStatus()))
                .collect(Collectors.groupingBy(BookingService::getServiceName, Collectors.counting()));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Lượt sử dụng");

            if (serviceCounts.isEmpty()) {
                ceoServiceXAxis.setLabel("Dịch Vụ");
            } else {
                serviceCounts.forEach((name, count) -> {
                    series.getData().add(new XYChart.Data<>(name, count));
                });
            }

            ceoServiceUsageChart.getData().add(series);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
