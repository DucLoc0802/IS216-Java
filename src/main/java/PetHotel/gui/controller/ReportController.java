package PetHotel.gui.controller;

import PetHotel.model.AppUser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ReportController {

    // ==========================================
    // 1. ÁNH XẠ CÁC THÀNH PHẦN GIAO DIỆN (UI COMPONENTS)
    // ==========================================

    // --- Bộ lọc Thời gian & Chi nhánh ---
    @FXML private Button btnPeriodToday;
    @FXML private Button btnPeriodWeek;
    @FXML private Button btnPeriodMonth;
    @FXML private Button btnPeriodQuarter;
    @FXML private Button btnPeriodYear;
    @FXML private Button btnPeriodCustom;
    
    @FXML private HBox customDateRow;
    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;
    @FXML private ComboBox<String> filterBranch;

    // --- Các thẻ chỉ số KPI (Thống kê tổng quan) ---
    @FXML private Label kpiPeriodLabel;
    @FXML private Label kpiRevenue;
    @FXML private Label kpiRevenueDelta;
    @FXML private Label kpiBooking;
    @FXML private Label kpiBookingDelta;
    @FXML private Label kpiOccupancy;
    @FXML private Label kpiOccupancyDelta;
    @FXML private Label kpiGrooming;
    @FXML private Label kpiGroomingDelta;
    @FXML private Label kpiNewCustomers;
    @FXML private Label kpiCustDelta;

    // --- Biểu đồ Quản lý ---
    @FXML private ComboBox<String> revenueChartGroupBy;
    @FXML private VBox revenueChartArea;
    @FXML private VBox occupancyChartArea;

    // --- Bảng Xếp Hạng Sản Phẩm Quản lý ---
    @FXML private TableView<?> topProductTable;
    @FXML private TableColumn<?, ?> colRank;
    @FXML private TableColumn<?, ?> colProdName;
    @FXML private TableColumn<?, ?> colProdCat;
    @FXML private TableColumn<?, ?> colProdSold;
    @FXML private TableColumn<?, ?> colProdRevenue;

    // --- Thống Kê Tồn Kho Quản lý ---
    @FXML private Label invStatOk;
    @FXML private Label invStatLow;
    @FXML private Label invStatCritical;

    // --- Danh sách báo cáo định kỳ Quản lý ---
    @FXML private VBox reportTaskList;

    // ==========================================
    // 🔑 BẮT BUỘC: PHÂN LUỒNG CONTAINER BÁO CÁO
    // ==========================================
    @FXML private VBox managerReportContainer;
    @FXML private TabPane ceoReportTabPane;

    // ==========================================
    // 👑 BÁO CÁO CEO: CÁC THÀNH PHẦN GIAO DIỆN MỚI
    // ==========================================

    // --- Tab 1: Tổng quan ---
    @FXML private Label ceoKpiRevenue;
    @FXML private Label ceoKpiBooking;
    @FXML private Label ceoKpiBranchCount;
    @FXML private Label ceoKpiPets;
    @FXML private Label ceoKpiCustomers;
    @FXML private Label ceoKpiDebt;
    @FXML private Label ceoKpiOccupancy;
    @FXML private Label ceoKpiTopBranch;
    @FXML private BarChart<String, Number> ceoOverviewRevenueChart;
    @FXML private CategoryAxis ceoOverviewRevX;
    @FXML private PieChart ceoOverviewRevenuePie;
    @FXML private TableView<BranchOverview> ceoOverviewTable;
    @FXML private TableColumn<BranchOverview, String> ceoColOverBranch;
    @FXML private TableColumn<BranchOverview, String> ceoColOverRevenue;
    @FXML private TableColumn<BranchOverview, String> ceoColOverBooking;
    @FXML private TableColumn<BranchOverview, String> ceoColOverRoom;
    @FXML private TableColumn<BranchOverview, String> ceoColOverDebt;
    @FXML private TableColumn<BranchOverview, String> ceoColOverStatus;

    // --- Tab 2: Chi nhánh ---
    @FXML private BarChart<String, Number> ceoBranchBookingChart;
    @FXML private CategoryAxis ceoBranchBookX;
    @FXML private BarChart<String, Number> ceoBranchOccupancyChart;
    @FXML private CategoryAxis ceoBranchOccX;
    @FXML private TableView<BranchDetail> ceoBranchTable;
    @FXML private TableColumn<BranchDetail, String> ceoColBranchName;
    @FXML private TableColumn<BranchDetail, String> ceoColBranchRevenue;
    @FXML private TableColumn<BranchDetail, String> ceoColBranchBooking;
    @FXML private TableColumn<BranchDetail, String> ceoColBranchOccupancy;
    @FXML private TableColumn<BranchDetail, String> ceoColBranchDebt;
    @FXML private TableColumn<BranchDetail, String> ceoColBranchLowStock;
    @FXML private TableColumn<BranchDetail, String> ceoColBranchStatus;

    // --- Tab 3: Doanh thu ---
    @FXML private BarChart<String, Number> ceoRevenueMonthlyChart;
    @FXML private CategoryAxis ceoRevMonthX;
    @FXML private PieChart ceoRevenueBranchPie;
    @FXML private TableView<BranchRevenue> ceoRevenueTable;
    @FXML private TableColumn<BranchRevenue, String> ceoColRevBranch;
    @FXML private TableColumn<BranchRevenue, String> ceoColRevTotal;
    @FXML private TableColumn<BranchRevenue, String> ceoColRevPaid;
    @FXML private TableColumn<BranchRevenue, String> ceoColRevDebt;
    @FXML private TableColumn<BranchRevenue, String> ceoColRevInvoices;

    // --- Tab 4: Booking ---
    @FXML private BarChart<String, Number> ceoBookingBranchChart;
    @FXML private CategoryAxis ceoBookBranchX;
    @FXML private PieChart ceoBookingStatusPie;
    @FXML private TableView<BranchBooking> ceoBookingTable;
    @FXML private TableColumn<BranchBooking, String> ceoColBookBranch;
    @FXML private TableColumn<BranchBooking, String> ceoColBookTotal;
    @FXML private TableColumn<BranchBooking, String> ceoColBookPending;
    @FXML private TableColumn<BranchBooking, String> ceoColBookConfirmed;
    @FXML private TableColumn<BranchBooking, String> ceoColBookIn;
    @FXML private TableColumn<BranchBooking, String> ceoColBookOut;
    @FXML private TableColumn<BranchBooking, String> ceoColBookCancel;

    // --- Tab 5: Dịch vụ ---
    @FXML private BarChart<String, Number> ceoServicePopularityChart;
    @FXML private CategoryAxis ceoServPopX;
    @FXML private PieChart ceoServiceRevenuePie;
    @FXML private TableView<ServiceUsage> ceoServiceTable;
    @FXML private TableColumn<ServiceUsage, String> ceoColServName;
    @FXML private TableColumn<ServiceUsage, String> ceoColServCount;
    @FXML private TableColumn<ServiceUsage, String> ceoColServRevenue;
    @FXML private TableColumn<ServiceUsage, String> ceoColServTopBranch;

    // --- Tab 6: Phòng ---
    @FXML private BarChart<String, Number> ceoRoomOccupancyChart;
    @FXML private CategoryAxis ceoRoomOccX;
    @FXML private PieChart ceoRoomStatusPie;
    @FXML private TableView<RoomStatusDetail> ceoRoomTable;
    @FXML private TableColumn<RoomStatusDetail, String> ceoColRoomBranch;
    @FXML private TableColumn<RoomStatusDetail, String> ceoColRoomTotal;
    @FXML private TableColumn<RoomStatusDetail, String> ceoColRoomInUse;
    @FXML private TableColumn<RoomStatusDetail, String> ceoColRoomVacant;
    @FXML private TableColumn<RoomStatusDetail, String> ceoColRoomMaint;
    @FXML private TableColumn<RoomStatusDetail, String> ceoColRoomRate;

    // --- Tab 7: Kho ---
    @FXML private BarChart<String, Number> ceoInventoryAlertChart;
    @FXML private CategoryAxis ceoInvAlertX;
    @FXML private TableView<InventoryAlertDetail> ceoInventoryTable;
    @FXML private TableColumn<InventoryAlertDetail, String> ceoColInvBranch;
    @FXML private TableColumn<InventoryAlertDetail, String> ceoColInvProd;
    @FXML private TableColumn<InventoryAlertDetail, String> ceoColInvStock;
    @FXML private TableColumn<InventoryAlertDetail, String> ceoColInvMin;
    @FXML private TableColumn<InventoryAlertDetail, String> ceoColInvUnit;
    @FXML private TableColumn<InventoryAlertDetail, String> ceoColInvStatus;

    // --- Tab 8: Nhân viên ---
    @FXML private BarChart<String, Number> ceoStaffCountChart;
    @FXML private CategoryAxis ceoStaffBranchX;
    @FXML private PieChart ceoStaffRolePie;
    @FXML private TableView<StaffDistribution> ceoStaffTable;
    @FXML private TableColumn<StaffDistribution, String> ceoColStaffBranch;
    @FXML private TableColumn<StaffDistribution, String> ceoColStaffTotal;
    @FXML private TableColumn<StaffDistribution, String> ceoColStaffReception;
    @FXML private TableColumn<StaffDistribution, String> ceoColStaffCare;
    @FXML private TableColumn<StaffDistribution, String> ceoColStaffManager;
    @FXML private TableColumn<StaffDistribution, String> ceoColStaffStatus;


    // ==========================================
    // 2. KHỞI TẠO DỮ LIỆU BAN ĐẦU
    // ==========================================
    
    @FXML
    public void initialize() {
        System.out.println("Đã nạp thành công giao diện Báo Cáo & Thống Kê!");
        
        AppUser currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getRole() == PetHotel.util.Role.CEO) {
            // Hiển thị giao diện CEO
            if (managerReportContainer != null) {
                managerReportContainer.setVisible(false);
                managerReportContainer.setManaged(false);
            }
            if (ceoReportTabPane != null) {
                ceoReportTabPane.setVisible(true);
                ceoReportTabPane.setManaged(true);
            }

            // Nạp bộ lọc chi nhánh cho CEO
            filterBranch.getItems().clear();
            filterBranch.getItems().addAll(
                "Tất cả chi nhánh",
                "BR001 - Chi nhánh Pet Hotel 01",
                "BR002 - Chi nhánh Pet Hotel 02",
                "BR003 - Chi nhánh Pet Hotel 03",
                "BR004 - Chi nhánh Pet Hotel 04",
                "BR005 - Chi nhánh Pet Hotel 05"
            );
            filterBranch.getSelectionModel().selectFirst();

            setupCeoTableColumns();
            loadCeoReportData();
        } else {
            // Hiển thị giao diện Quản lý chi nhánh gốc
            if (managerReportContainer != null) {
                managerReportContainer.setVisible(true);
                managerReportContainer.setManaged(true);
            }
            if (ceoReportTabPane != null) {
                ceoReportTabPane.setVisible(false);
                ceoReportTabPane.setManaged(false);
            }

            filterBranch.getItems().clear();
            filterBranch.getItems().addAll("Tất cả chi nhánh", "Chi nhánh Q.1", "Chi nhánh Q.3");
            filterBranch.getSelectionModel().selectFirst();

            loadReportData();
        }
    }

    // Hàm gọi dữ liệu từ Database (BUS/DAO) để đổ lên màn hình của quản lý
    private void loadReportData() {
        System.out.println("Đang truy xuất dữ liệu thống kê từ cơ sở dữ liệu cho Quản Lý Chi Nhánh...");
        // Báo cáo Manager gốc
        if (kpiRevenue != null) kpiRevenue.setText("125.000.000 VNĐ");
        if (kpiBooking != null) kpiBooking.setText("310");
        if (kpiOccupancy != null) kpiOccupancy.setText("85%");
        if (kpiGrooming != null) kpiGrooming.setText("180");
        if (kpiNewCustomers != null) kpiNewCustomers.setText("45");
        if (invStatOk != null) invStatOk.setText("18");
        if (invStatLow != null) invStatLow.setText("3");
        if (invStatCritical != null) invStatCritical.setText("1");
    }

    // ==========================================
    // 👑 BÁO CÁO CEO: THIẾT LẬP CỘT BẢNG & CĂN GIỮA
    // ==========================================
    private <S, T> void alignColumnCenter(TableColumn<S, T> column) {
        if (column != null) {
            column.setStyle("-fx-alignment: CENTER;");
        }
    }

    private void setupCeoTableColumns() {
        // Tab 1 Overview
        ceoColOverBranch.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("branch"));
        ceoColOverRevenue.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("revenue"));
        ceoColOverBooking.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("booking"));
        ceoColOverRoom.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("occupancy"));
        ceoColOverDebt.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("debt"));
        ceoColOverStatus.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));

        alignColumnCenter(ceoColOverBranch);
        alignColumnCenter(ceoColOverRevenue);
        alignColumnCenter(ceoColOverBooking);
        alignColumnCenter(ceoColOverRoom);
        alignColumnCenter(ceoColOverDebt);
        alignColumnCenter(ceoColOverStatus);

        ceoColOverStatus.setCellFactory(column -> new TableCell<BranchOverview, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setStyle("");
                } else {
                    setText(item);
                    if ("Tốt".equalsIgnoreCase(item)) setStyle("-fx-text-fill: #59A14F; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    else if ("Ổn định".equalsIgnoreCase(item)) setStyle("-fx-text-fill: #4E79A7; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    else if ("Cần theo dõi".equalsIgnoreCase(item)) setStyle("-fx-text-fill: #F2C14E; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    else if ("Rủi ro".equalsIgnoreCase(item)) setStyle("-fx-text-fill: #E15759; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    else setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        // Tab 2 Branch
        ceoColBranchName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
        ceoColBranchRevenue.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("revenue"));
        ceoColBranchBooking.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("booking"));
        ceoColBranchOccupancy.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("occupancy"));
        ceoColBranchDebt.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("debt"));
        ceoColBranchLowStock.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("lowStock"));
        ceoColBranchStatus.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));

        alignColumnCenter(ceoColBranchName);
        alignColumnCenter(ceoColBranchRevenue);
        alignColumnCenter(ceoColBranchBooking);
        alignColumnCenter(ceoColBranchOccupancy);
        alignColumnCenter(ceoColBranchDebt);
        alignColumnCenter(ceoColBranchLowStock);
        alignColumnCenter(ceoColBranchStatus);

        ceoColBranchStatus.setCellFactory(column -> new TableCell<BranchDetail, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setStyle("");
                } else {
                    setText(item);
                    if ("Tốt".equalsIgnoreCase(item)) setStyle("-fx-text-fill: #59A14F; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    else if ("Ổn định".equalsIgnoreCase(item)) setStyle("-fx-text-fill: #4E79A7; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    else if ("Cần theo dõi".equalsIgnoreCase(item)) setStyle("-fx-text-fill: #F2C14E; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    else if ("Rủi ro".equalsIgnoreCase(item)) setStyle("-fx-text-fill: #E15759; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    else setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        // Tab 3 Revenue
        ceoColRevBranch.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("branch"));
        ceoColRevTotal.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("total"));
        ceoColRevPaid.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("paid"));
        ceoColRevDebt.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("debt"));
        ceoColRevInvoices.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("invoices"));

        alignColumnCenter(ceoColRevBranch);
        alignColumnCenter(ceoColRevTotal);
        alignColumnCenter(ceoColRevPaid);
        alignColumnCenter(ceoColRevDebt);
        alignColumnCenter(ceoColRevInvoices);

        // Tab 4 Booking
        ceoColBookBranch.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("branch"));
        ceoColBookTotal.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("total"));
        ceoColBookPending.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("pending"));
        ceoColBookConfirmed.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("confirmed"));
        ceoColBookIn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("checkIn"));
        ceoColBookOut.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("checkOut"));
        ceoColBookCancel.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("cancel"));

        alignColumnCenter(ceoColBookBranch);
        alignColumnCenter(ceoColBookTotal);
        alignColumnCenter(ceoColBookPending);
        alignColumnCenter(ceoColBookConfirmed);
        alignColumnCenter(ceoColBookIn);
        alignColumnCenter(ceoColBookOut);
        alignColumnCenter(ceoColBookCancel);

        // Tab 5 Service
        ceoColServName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
        ceoColServCount.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("count"));
        ceoColServRevenue.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("revenue"));
        ceoColServTopBranch.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("topBranch"));

        alignColumnCenter(ceoColServName);
        alignColumnCenter(ceoColServCount);
        alignColumnCenter(ceoColServRevenue);
        alignColumnCenter(ceoColServTopBranch);

        // Tab 6 Room
        ceoColRoomBranch.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("branch"));
        ceoColRoomTotal.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("total"));
        ceoColRoomInUse.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("inUse"));
        ceoColRoomVacant.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("vacant"));
        ceoColRoomMaint.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("maintenance"));
        ceoColRoomRate.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("occupancy"));

        alignColumnCenter(ceoColRoomBranch);
        alignColumnCenter(ceoColRoomTotal);
        alignColumnCenter(ceoColRoomInUse);
        alignColumnCenter(ceoColRoomVacant);
        alignColumnCenter(ceoColRoomMaint);
        alignColumnCenter(ceoColRoomRate);

        // Tab 7 Inventory
        ceoColInvBranch.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("branch"));
        ceoColInvProd.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("product"));
        ceoColInvStock.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("stock"));
        ceoColInvMin.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("minStock"));
        ceoColInvUnit.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("unit"));
        ceoColInvStatus.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));

        alignColumnCenter(ceoColInvBranch);
        alignColumnCenter(ceoColInvProd);
        alignColumnCenter(ceoColInvStock);
        alignColumnCenter(ceoColInvMin);
        alignColumnCenter(ceoColInvUnit);
        alignColumnCenter(ceoColInvStatus);

        ceoColInvStatus.setCellFactory(column -> new TableCell<InventoryAlertDetail, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setStyle("");
                } else {
                    setText(item);
                    if ("Đủ hàng".equalsIgnoreCase(item)) setStyle("-fx-text-fill: #59A14F; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    else if ("Sắp hết".equalsIgnoreCase(item)) setStyle("-fx-text-fill: #F2C14E; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    else if ("Hết hàng".equalsIgnoreCase(item) || "Cần nhập thêm".equalsIgnoreCase(item)) setStyle("-fx-text-fill: #E15759; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    else setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        // Tab 8 Staff
        ceoColStaffBranch.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("branch"));
        ceoColStaffTotal.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("total"));
        ceoColStaffReception.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("reception"));
        ceoColStaffCare.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("care"));
        ceoColStaffManager.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("manager"));
        ceoColStaffStatus.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));

        alignColumnCenter(ceoColStaffBranch);
        alignColumnCenter(ceoColStaffTotal);
        alignColumnCenter(ceoColStaffReception);
        alignColumnCenter(ceoColStaffCare);
        alignColumnCenter(ceoColStaffManager);
        alignColumnCenter(ceoColStaffStatus);

        ceoColStaffStatus.setCellFactory(column -> new TableCell<StaffDistribution, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setStyle("");
                } else {
                    setText(item);
                    if ("Đầy đủ".equalsIgnoreCase(item) || "Tốt".equalsIgnoreCase(item)) setStyle("-fx-text-fill: #59A14F; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    else if ("Thiếu lễ tân".equalsIgnoreCase(item) || "Thiếu chăm sóc".equalsIgnoreCase(item)) setStyle("-fx-text-fill: #F2C14E; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    else setStyle("-fx-alignment: CENTER;");
                }
            }
        });
    }

    // ==========================================
    // 👑 BÁO CÁO CEO: TRUY XUẤT VÀ NẠP DỮ LIỆU DEMO
    // ==========================================
    private void loadCeoReportData() {
        System.out.println("Đang truy xuất dữ liệu thống kê từ cơ sở dữ liệu chuỗi chi nhánh cho CEO...");
        loadCeoOverviewDemoData();
        loadCeoBranchDemoData();
        loadCeoRevenueDemoData();
        loadCeoBookingDemoData();
        loadCeoServiceDemoData();
        loadCeoRoomDemoData();
        loadCeoInventoryDemoData();
        loadCeoEmployeeDemoData();
    }

    private void loadCeoOverviewDemoData() {
        ceoKpiRevenue.setText("320.000.000 VNĐ");
        ceoKpiBooking.setText("845");
        ceoKpiBranchCount.setText("5");
        ceoKpiPets.setText("96 thú cưng");
        ceoKpiCustomers.setText("1.240");
        ceoKpiDebt.setText("24.000.000 VNĐ");
        ceoKpiOccupancy.setText("78%");
        ceoKpiTopBranch.setText("Pet Hotel Q.1");

        // Charts
        ceoOverviewRevenueChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");
        series.getData().add(new XYChart.Data<>("BR001 (Q.1)", 120000000));
        series.getData().add(new XYChart.Data<>("BR002 (Q.3)", 80000000));
        series.getData().add(new XYChart.Data<>("BR003 (B.Thạnh)", 65000000));
        series.getData().add(new XYChart.Data<>("BR004 (T.Bình)", 40000000));
        series.getData().add(new XYChart.Data<>("BR005 (G.Vấp)", 15000000));
        ceoOverviewRevenueChart.getData().add(series);

        // Pie Chart
        ceoOverviewRevenuePie.getData().clear();
        ceoOverviewRevenuePie.getData().addAll(
            new PieChart.Data("Tiền phòng (60%)", 60),
            new PieChart.Data("Dịch vụ Spa/Grooming (30%)", 30),
            new PieChart.Data("Phụ phí khác (10%)", 10)
        );

        // Palette thương hiệu
        String[] colors = {"#A65A2E", "#4E79A7", "#BAB0AC"};
        javafx.application.Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #A65A2E;");
                }
            }
            int idx = 0;
            for (PieChart.Data d : ceoOverviewRevenuePie.getData()) {
                if (d.getNode() != null && idx < colors.length) {
                    d.getNode().setStyle("-fx-pie-color: " + colors[idx] + ";");
                }
                idx++;
            }
        });

        // Table
        ObservableList<BranchOverview> dataList = FXCollections.observableArrayList(
            new BranchOverview("BR001 - Chi nhánh Pet Hotel Q.1", "120.000.000 VNĐ", "310", "85%", "4.000.000 VNĐ", "Tốt"),
            new BranchOverview("BR002 - Chi nhánh Pet Hotel Q.3", "80.000.000 VNĐ", "210", "78%", "6.000.000 VNĐ", "Ổn định"),
            new BranchOverview("BR003 - Chi nhánh Pet Hotel Bình Thạnh", "65.000.000 VNĐ", "165", "72%", "2.000.000 VNĐ", "Tốt"),
            new BranchOverview("BR004 - Chi nhánh Pet Hotel Tân Bình", "40.000.000 VNĐ", "110", "60%", "7.000.000 VNĐ", "Cần theo dõi"),
            new BranchOverview("BR005 - Chi nhánh Pet Hotel Gò Vấp", "15.000.000 VNĐ", "50", "45%", "5.000.000 VNĐ", "Rủi ro")
        );
        ceoOverviewTable.setItems(dataList);
    }

    private void loadCeoBranchDemoData() {
        ceoBranchBookingChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Booking");
        series.getData().add(new XYChart.Data<>("BR001 (Q.1)", 310));
        series.getData().add(new XYChart.Data<>("BR002 (Q.3)", 210));
        series.getData().add(new XYChart.Data<>("BR003 (B.Thạnh)", 165));
        series.getData().add(new XYChart.Data<>("BR004 (T.Bình)", 110));
        series.getData().add(new XYChart.Data<>("BR005 (G.Vấp)", 50));
        ceoBranchBookingChart.getData().add(series);

        ceoBranchOccupancyChart.getData().clear();
        XYChart.Series<String, Number> series2 = new XYChart.Series<>();
        series2.setName("Công suất %");
        series2.getData().add(new XYChart.Data<>("BR001 (Q.1)", 85));
        series2.getData().add(new XYChart.Data<>("BR002 (Q.3)", 78));
        series2.getData().add(new XYChart.Data<>("BR003 (B.Thạnh)", 72));
        series2.getData().add(new XYChart.Data<>("BR004 (T.Bình)", 60));
        series2.getData().add(new XYChart.Data<>("BR005 (G.Vấp)", 45));
        ceoBranchOccupancyChart.getData().add(series2);

        javafx.application.Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #4E79A7;");
                }
            }
            for (XYChart.Data<String, Number> data : series2.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #4E79A7;");
                }
            }
        });

        ObservableList<BranchDetail> dataList = FXCollections.observableArrayList(
            new BranchDetail("BR001 - Chi nhánh Pet Hotel Q.1", "120.000.000 VNĐ", "310", "85%", "4.000.000 VNĐ", "2", "Tốt"),
            new BranchDetail("BR002 - Chi nhánh Pet Hotel Q.3", "80.000.000 VNĐ", "210", "78%", "6.000.000 VNĐ", "5", "Ổn định"),
            new BranchDetail("BR003 - Chi nhánh Pet Hotel Bình Thạnh", "65.000.000 VNĐ", "165", "72%", "2.000.000 VNĐ", "1", "Tốt"),
            new BranchDetail("BR004 - Chi nhánh Pet Hotel Tân Bình", "40.000.000 VNĐ", "110", "60%", "7.000.000 VNĐ", "8", "Cần theo dõi"),
            new BranchDetail("BR005 - Chi nhánh Pet Hotel Gò Vấp", "15.000.000 VNĐ", "50", "45%", "5.000.000 VNĐ", "0", "Rủi ro")
        );
        ceoBranchTable.setItems(dataList);
    }

    private void loadCeoRevenueDemoData() {
        ceoRevenueMonthlyChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");
        series.getData().add(new XYChart.Data<>("Tháng 1", 220000000));
        series.getData().add(new XYChart.Data<>("Tháng 2", 260000000));
        series.getData().add(new XYChart.Data<>("Tháng 3", 280000000));
        series.getData().add(new XYChart.Data<>("Tháng 4", 310000000));
        series.getData().add(new XYChart.Data<>("Tháng 5", 320000000));
        ceoRevenueMonthlyChart.getData().add(series);

        ceoRevenueBranchPie.getData().clear();
        ceoRevenueBranchPie.getData().addAll(
            new PieChart.Data("BR001 Q.1 (38%)", 38),
            new PieChart.Data("BR002 Q.3 (25%)", 25),
            new PieChart.Data("BR003 Bình Thạnh (20%)", 20),
            new PieChart.Data("BR004 Tân Bình (12%)", 12),
            new PieChart.Data("BR005 Gò Vấp (5%)", 5)
        );

        String[] colors = {"#A65A2E", "#4E79A7", "#59A14F", "#F2C14E", "#E15759"};
        javafx.application.Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #A65A2E;");
                }
            }
            int idx = 0;
            for (PieChart.Data d : ceoRevenueBranchPie.getData()) {
                if (d.getNode() != null && idx < colors.length) {
                    d.getNode().setStyle("-fx-pie-color: " + colors[idx] + ";");
                }
                idx++;
            }
        });

        ObservableList<BranchRevenue> dataList = FXCollections.observableArrayList(
            new BranchRevenue("BR001 - Chi nhánh Pet Hotel Q.1", "120.000.000 VNĐ", "116.000.000 VNĐ", "4.000.000 VNĐ", "310"),
            new BranchRevenue("BR002 - Chi nhánh Pet Hotel Q.3", "80.000.000 VNĐ", "74.000.000 VNĐ", "6.000.000 VNĐ", "210"),
            new BranchRevenue("BR003 - Chi nhánh Pet Hotel Bình Thạnh", "65.000.000 VNĐ", "63.000.000 VNĐ", "2.000.000 VNĐ", "165"),
            new BranchRevenue("BR004 - Chi nhánh Pet Hotel Tân Bình", "40.000.000 VNĐ", "33.000.000 VNĐ", "7.000.000 VNĐ", "110"),
            new BranchRevenue("BR005 - Chi nhánh Pet Hotel Gò Vấp", "15.000.000 VNĐ", "10.000.000 VNĐ", "5.000.000 VNĐ", "50")
        );
        ceoRevenueTable.setItems(dataList);
    }

    private void loadCeoBookingDemoData() {
        ceoBookingBranchChart.getData().clear();
        XYChart.Series<String, Number> seriesSuccess = new XYChart.Series<>();
        seriesSuccess.setName("Thành công");
        seriesSuccess.getData().add(new XYChart.Data<>("BR001 (Q.1)", 295));
        seriesSuccess.getData().add(new XYChart.Data<>("BR002 (Q.3)", 195));
        seriesSuccess.getData().add(new XYChart.Data<>("BR003 (B.Thạnh)", 157));
        seriesSuccess.getData().add(new XYChart.Data<>("BR004 (T.Bình)", 98));
        seriesSuccess.getData().add(new XYChart.Data<>("BR005 (G.Vấp)", 42));

        XYChart.Series<String, Number> seriesCancel = new XYChart.Series<>();
        seriesCancel.setName("Đã Hủy");
        seriesCancel.getData().add(new XYChart.Data<>("BR001 (Q.1)", 15));
        seriesCancel.getData().add(new XYChart.Data<>("BR002 (Q.3)", 15));
        seriesCancel.getData().add(new XYChart.Data<>("BR003 (B.Thạnh)", 8));
        seriesCancel.getData().add(new XYChart.Data<>("BR004 (T.Bình)", 12));
        seriesCancel.getData().add(new XYChart.Data<>("BR005 (G.Vấp)", 8));

        ceoBookingBranchChart.getData().addAll(seriesSuccess, seriesCancel);

        ceoBookingStatusPie.getData().clear();
        ceoBookingStatusPie.getData().addAll(
            new PieChart.Data("Pending (10%)", 10),
            new PieChart.Data("Confirmed (20%)", 20),
            new PieChart.Data("Checked-in (45%)", 45),
            new PieChart.Data("Checked-out (20%)", 20),
            new PieChart.Data("Cancelled (5%)", 5)
        );

        String[] colors = {"#F2C14E", "#4E79A7", "#59A14F", "#8E6BBE", "#E15759"};
        javafx.application.Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : seriesSuccess.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #59A14F;");
                }
            }
            for (XYChart.Data<String, Number> data : seriesCancel.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #E15759;");
                }
            }
            int idx = 0;
            for (PieChart.Data d : ceoBookingStatusPie.getData()) {
                if (d.getNode() != null && idx < colors.length) {
                    d.getNode().setStyle("-fx-pie-color: " + colors[idx] + ";");
                }
                idx++;
            }
        });

        ObservableList<BranchBooking> dataList = FXCollections.observableArrayList(
            new BranchBooking("BR001 - Chi nhánh Pet Hotel Q.1", "310", "31", "62", "140", "62", "15"),
            new BranchBooking("BR002 - Chi nhánh Pet Hotel Q.3", "210", "21", "42", "94", "42", "15"),
            new BranchBooking("BR003 - Chi nhánh Pet Hotel Bình Thạnh", "165", "16", "33", "74", "33", "9"),
            new BranchBooking("BR004 - Chi nhánh Pet Hotel Tân Bình", "110", "11", "22", "50", "22", "12"),
            new BranchBooking("BR005 - Chi nhánh Pet Hotel Gò Vấp", "50", "5", "10", "22", "10", "8")
        );
        ceoBookingTable.setItems(dataList);
    }

    private void loadCeoServiceDemoData() {
        ceoServicePopularityChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Lượt dùng");
        series.getData().add(new XYChart.Data<>("Grooming Combo", 320));
        series.getData().add(new XYChart.Data<>("Tắm thú cưng", 240));
        series.getData().add(new XYChart.Data<>("Cắt tỉa lông", 180));
        series.getData().add(new XYChart.Data<>("Cắt móng", 120));
        series.getData().add(new XYChart.Data<>("Vệ sinh tai", 85));
        ceoServicePopularityChart.getData().add(series);

        ceoServiceRevenuePie.getData().clear();
        ceoServiceRevenuePie.getData().addAll(
            new PieChart.Data("Tắm thú cưng (24%)", 24),
            new PieChart.Data("Grooming Combo (42%)", 42),
            new PieChart.Data("Cắt tỉa lông (18%)", 18),
            new PieChart.Data("Cắt móng (11%)", 11),
            new PieChart.Data("Vệ sinh tai (5%)", 5)
        );

        String[] colors = {"#4E79A7", "#A65A2E", "#F2C14E", "#59A14F", "#8E6BBE"};
        javafx.application.Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #4E79A7;");
                }
            }
            int idx = 0;
            for (PieChart.Data d : ceoServiceRevenuePie.getData()) {
                if (d.getNode() != null && idx < colors.length) {
                    d.getNode().setStyle("-fx-pie-color: " + colors[idx] + ";");
                }
                idx++;
            }
        });

        ObservableList<ServiceUsage> dataList = FXCollections.observableArrayList(
            new ServiceUsage("Grooming full combo", "320", "160.000.000 VNĐ", "BR001 - Chi nhánh Pet Hotel Q.1"),
            new ServiceUsage("Tắm thú cưng", "240", "48.000.000 VNĐ", "BR002 - Chi nhánh Pet Hotel Q.3"),
            new ServiceUsage("Cắt tỉa lông", "180", "54.000.000 VNĐ", "BR001 - Chi nhánh Pet Hotel Q.1"),
            new ServiceUsage("Cắt móng", "120", "12.000.000 VNĐ", "BR003 - Chi nhánh Pet Hotel Bình Thạnh"),
            new ServiceUsage("Vệ sinh tai", "85", "8.500.000 VNĐ", "BR004 - Chi nhánh Pet Hotel Tân Bình")
        );
        ceoServiceTable.setItems(dataList);
    }

    private void loadCeoRoomDemoData() {
        ceoRoomOccupancyChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Công suất");
        series.getData().add(new XYChart.Data<>("BR001 (Q.1)", 85));
        series.getData().add(new XYChart.Data<>("BR002 (Q.3)", 78));
        series.getData().add(new XYChart.Data<>("BR003 (B.Thạnh)", 72));
        series.getData().add(new XYChart.Data<>("BR004 (T.Bình)", 60));
        series.getData().add(new XYChart.Data<>("BR005 (G.Vấp)", 45));
        ceoRoomOccupancyChart.getData().add(series);

        ceoRoomStatusPie.getData().clear();
        ceoRoomStatusPie.getData().addAll(
            new PieChart.Data("Đang sử dụng (78%)", 78),
            new PieChart.Data("Còn trống (15%)", 15),
            new PieChart.Data("Bảo trì (7%)", 7)
        );

        String[] colors = {"#59A14F", "#4E79A7", "#F2C14E"};
        javafx.application.Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #59A14F;");
                }
            }
            int idx = 0;
            for (PieChart.Data d : ceoRoomStatusPie.getData()) {
                if (d.getNode() != null && idx < colors.length) {
                    d.getNode().setStyle("-fx-pie-color: " + colors[idx] + ";");
                }
                idx++;
            }
        });

        ObservableList<RoomStatusDetail> dataList = FXCollections.observableArrayList(
            new RoomStatusDetail("BR001 - Chi nhánh Pet Hotel Q.1", "30", "25", "4", "1", "85%"),
            new RoomStatusDetail("BR002 - Chi nhánh Pet Hotel Q.3", "30", "23", "5", "2", "78%"),
            new RoomStatusDetail("BR003 - Chi nhánh Pet Hotel Bình Thạnh", "25", "18", "5", "2", "72%"),
            new RoomStatusDetail("BR004 - Chi nhánh Pet Hotel Tân Bình", "20", "12", "6", "2", "60%"),
            new RoomStatusDetail("BR005 - Chi nhánh Pet Hotel Gò Vấp", "15", "7", "6", "2", "45%")
        );
        ceoRoomTable.setItems(dataList);
    }

    private void loadCeoInventoryDemoData() {
        ceoInventoryAlertChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tồn kho thấp");
        series.getData().add(new XYChart.Data<>("BR001 (Q.1)", 2));
        series.getData().add(new XYChart.Data<>("BR002 (Q.3)", 5));
        series.getData().add(new XYChart.Data<>("BR003 (B.Thạnh)", 1));
        series.getData().add(new XYChart.Data<>("BR004 (T.Bình)", 8));
        series.getData().add(new XYChart.Data<>("BR005 (G.Vấp)", 0));
        ceoInventoryAlertChart.getData().add(series);

        javafx.application.Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #E15759;");
                }
            }
        });

        ObservableList<InventoryAlertDetail> dataList = FXCollections.observableArrayList(
            new InventoryAlertDetail("BR001 - Chi nhánh Pet Hotel Q.1", "Thức ăn chó Royal Canin", "3", "5", "bao", "Sắp hết"),
            new InventoryAlertDetail("BR002 - Chi nhánh Pet Hotel Q.3", "Cát vệ sinh mèo", "1", "10", "bao", "Cần nhập thêm"),
            new InventoryAlertDetail("BR002 - Chi nhánh Pet Hotel Q.3", "Pate mèo Whiskas", "0", "20", "lon", "Hết hàng"),
            new InventoryAlertDetail("BR003 - Chi nhánh Pet Hotel Bình Thạnh", "Sữa tắm chuyên dụng", "2", "5", "chai", "Sắp hết"),
            new InventoryAlertDetail("BR004 - Chi nhánh Pet Hotel Tân Bình", "Thuốc tẩy giun thú cưng", "1", "15", "hộp", "Cần nhập thêm")
        );
        ceoInventoryTable.setItems(dataList);
    }

    private void loadCeoEmployeeDemoData() {
        ceoStaffCountChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nhân viên");
        series.getData().add(new XYChart.Data<>("BR001 (Q.1)", 15));
        series.getData().add(new XYChart.Data<>("BR002 (Q.3)", 12));
        series.getData().add(new XYChart.Data<>("BR003 (B.Thạnh)", 10));
        series.getData().add(new XYChart.Data<>("BR004 (T.Bình)", 8));
        series.getData().add(new XYChart.Data<>("BR005 (G.Vấp)", 6));
        ceoStaffCountChart.getData().add(series);

        ceoStaffRolePie.getData().clear();
        ceoStaffRolePie.getData().addAll(
            new PieChart.Data("Lễ tân (25%)", 25),
            new PieChart.Data("Chăm sóc (60%)", 60),
            new PieChart.Data("Quản lý (12%)", 12),
            new PieChart.Data("Admin/CEO (3%)", 3)
        );

        String[] colors = {"#4E79A7", "#59A14F", "#A65A2E", "#8E6BBE"};
        javafx.application.Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #59A14F;");
                }
            }
            int idx = 0;
            for (PieChart.Data d : ceoStaffRolePie.getData()) {
                if (d.getNode() != null && idx < colors.length) {
                    d.getNode().setStyle("-fx-pie-color: " + colors[idx] + ";");
                }
                idx++;
            }
        });

        ObservableList<StaffDistribution> dataList = FXCollections.observableArrayList(
            new StaffDistribution("BR001 - Chi nhánh Pet Hotel Q.1", "15", "3", "10", "1", "Đầy đủ"),
            new StaffDistribution("BR002 - Chi nhánh Pet Hotel Q.3", "12", "2", "8", "1", "Thiếu lễ tân"),
            new StaffDistribution("BR003 - Chi nhánh Pet Hotel Bình Thạnh", "10", "2", "6", "1", "Thiếu chăm sóc"),
            new StaffDistribution("BR004 - Chi nhánh Pet Hotel Tân Bình", "8", "2", "5", "1", "Đầy đủ"),
            new StaffDistribution("BR005 - Chi nhánh Pet Hotel Gò Vấp", "6", "1", "4", "1", "Thiếu lễ tân")
        );
        ceoStaffTable.setItems(dataList);
    }

    // ==========================================
    // 3. XỬ LÝ SỰ KIỆN: BỘ LỌC THỜI GIAN KỲ BÁO CÁO
    // ==========================================

    @FXML
    public void onPeriodToday(ActionEvent event) {
        setPeriodActive(btnPeriodToday);
        System.out.println("Lọc báo cáo: Hôm nay");
        refreshActiveReportData();
    }

    @FXML
    public void onPeriodWeek(ActionEvent event) {
        setPeriodActive(btnPeriodWeek);
        System.out.println("Lọc báo cáo: Tuần này");
        refreshActiveReportData();
    }

    @FXML
    public void onPeriodMonth(ActionEvent event) {
        setPeriodActive(btnPeriodMonth);
        System.out.println("Lọc báo cáo: Tháng này");
        refreshActiveReportData();
    }

    @FXML
    public void onPeriodQuarter(ActionEvent event) {
        setPeriodActive(btnPeriodQuarter);
        System.out.println("Lọc báo cáo: Quý này");
        refreshActiveReportData();
    }

    @FXML
    public void onPeriodYear(ActionEvent event) {
        setPeriodActive(btnPeriodYear);
        System.out.println("Lọc báo cáo: Năm nay");
        refreshActiveReportData();
    }

    @FXML
    public void onPeriodCustom(ActionEvent event) {
        setPeriodActive(btnPeriodCustom);
        // Hiển thị thanh chọn ngày tùy chỉnh (Từ ngày - Đến ngày)
        customDateRow.setVisible(true);
        customDateRow.setManaged(true);
        System.out.println("Mở bộ chọn ngày tùy chỉnh...");
    }

    @FXML
    public void onApplyCustomPeriod(ActionEvent event) {
        System.out.println("Áp dụng khoảng thời gian tùy chỉnh từ: " + dateFrom.getValue() + " đến: " + dateTo.getValue());
        refreshActiveReportData();
    }

    // Hàm tiện ích: Đổi màu nút chọn thời gian đang Active và ẩn thanh Custom Date
    private void setPeriodActive(Button activeBtn) {
        btnPeriodToday.getStyleClass().remove("period-btn-active");
        btnPeriodWeek.getStyleClass().remove("period-btn-active");
        btnPeriodMonth.getStyleClass().remove("period-btn-active");
        btnPeriodQuarter.getStyleClass().remove("period-btn-active");
        btnPeriodYear.getStyleClass().remove("period-btn-active");
        btnPeriodCustom.getStyleClass().remove("period-btn-active");

        activeBtn.getStyleClass().add("period-btn-active");
        
        if (activeBtn != btnPeriodCustom) {
            customDateRow.setVisible(false);
            customDateRow.setManaged(false);
        }
    }

    private void refreshActiveReportData() {
        AppUser currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getRole() == PetHotel.util.Role.CEO) {
            loadCeoReportData();
        } else {
            loadReportData();
        }
    }

    // ==========================================
    // 4. XỬ LÝ SỰ KIỆN: XUẤT BÁO CÁO & XEM CHI TIẾT
    // ==========================================

    @FXML
    public void onRefreshReport(ActionEvent event) {
        System.out.println("Làm mới dữ liệu báo cáo cho chi nhánh: " + filterBranch.getValue());
        refreshActiveReportData();
    }

    @FXML
    public void onExportReport(ActionEvent event) {
        System.out.println("Tiến hành xuất báo cáo ra file Excel/PDF...");
    }

    @FXML
    public void onScheduleReport(ActionEvent event) {
        System.out.println("Mở Form cấu hình gửi báo cáo định kỳ qua Email...");
    }

    @FXML
    public void onViewProductReport(ActionEvent event) {
        System.out.println("Chuyển hướng sang trang Báo Cáo Sản Phẩm chi tiết...");
    }

    @FXML
    public void onViewInventoryReport(ActionEvent event) {
        System.out.println("Chuyển hướng sang trang Báo Cáo Tồn Kho chi tiết...");
    }

    @FXML
    public void onDownloadReport(ActionEvent event) {
        System.out.println("Tải xuống file báo cáo định kỳ đã lưu...");
    }

    // ==========================================
    // 5. HELPER DATA CLASSES DÀNH CHO BẢNG CEO
    // ==========================================
    public static class BranchOverview {
        private final String branch;
        private final String revenue;
        private final String booking;
        private final String occupancy;
        private final String debt;
        private final String status;

        public BranchOverview(String branch, String revenue, String booking, String occupancy, String debt, String status) {
            this.branch = branch;
            this.revenue = revenue;
            this.booking = booking;
            this.occupancy = occupancy;
            this.debt = debt;
            this.status = status;
        }

        public String getBranch() { return branch; }
        public String getRevenue() { return revenue; }
        public String getBooking() { return booking; }
        public String getOccupancy() { return occupancy; }
        public String getDebt() { return debt; }
        public String getStatus() { return status; }
    }

    public static class BranchDetail {
        private final String name;
        private final String revenue;
        private final String booking;
        private final String occupancy;
        private final String debt;
        private final String lowStock;
        private final String status;

        public BranchDetail(String name, String revenue, String booking, String occupancy, String debt, String lowStock, String status) {
            this.name = name;
            this.revenue = revenue;
            this.booking = booking;
            this.occupancy = occupancy;
            this.debt = debt;
            this.lowStock = lowStock;
            this.status = status;
        }

        public String getName() { return name; }
        public String getRevenue() { return revenue; }
        public String getBooking() { return booking; }
        public String getOccupancy() { return occupancy; }
        public String getDebt() { return debt; }
        public String getLowStock() { return lowStock; }
        public String getStatus() { return status; }
    }

    public static class BranchRevenue {
        private final String branch;
        private final String total;
        private final String paid;
        private final String debt;
        private final String invoices;

        public BranchRevenue(String branch, String total, String paid, String debt, String invoices) {
            this.branch = branch;
            this.total = total;
            this.paid = paid;
            this.debt = debt;
            this.invoices = invoices;
        }

        public String getBranch() { return branch; }
        public String getTotal() { return total; }
        public String getPaid() { return paid; }
        public String getDebt() { return debt; }
        public String getInvoices() { return invoices; }
    }

    public static class BranchBooking {
        private final String branch;
        private final String total;
        private final String pending;
        private final String confirmed;
        private final String checkIn;
        private final String checkOut;
        private final String cancel;

        public BranchBooking(String branch, String total, String pending, String confirmed, String checkIn, String checkOut, String cancel) {
            this.branch = branch;
            this.total = total;
            this.pending = pending;
            this.confirmed = confirmed;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.cancel = cancel;
        }

        public String getBranch() { return branch; }
        public String getTotal() { return total; }
        public String getPending() { return pending; }
        public String getConfirmed() { return confirmed; }
        public String getCheckIn() { return checkIn; }
        public String getCheckOut() { return checkOut; }
        public String getCancel() { return cancel; }
    }

    public static class ServiceUsage {
        private final String name;
        private final String count;
        private final String revenue;
        private final String topBranch;

        public ServiceUsage(String name, String count, String revenue, String topBranch) {
            this.name = name;
            this.count = count;
            this.revenue = revenue;
            this.topBranch = topBranch;
        }

        public String getName() { return name; }
        public String getCount() { return count; }
        public String getRevenue() { return revenue; }
        public String getTopBranch() { return topBranch; }
    }

    public static class RoomStatusDetail {
        private final String branch;
        private final String total;
        private final String inUse;
        private final String vacant;
        private final String maintenance;
        private final String occupancy;

        public RoomStatusDetail(String branch, String total, String inUse, String vacant, String maintenance, String occupancy) {
            this.branch = branch;
            this.total = total;
            this.inUse = inUse;
            this.vacant = vacant;
            this.maintenance = maintenance;
            this.occupancy = occupancy;
        }

        public String getBranch() { return branch; }
        public String getTotal() { return total; }
        public String getInUse() { return inUse; }
        public String getVacant() { return vacant; }
        public String getMaintenance() { return maintenance; }
        public String getOccupancy() { return occupancy; }
    }

    public static class InventoryAlertDetail {
        private final String branch;
        private final String product;
        private final String stock;
        private final String minStock;
        private final String unit;
        private final String status;

        public InventoryAlertDetail(String branch, String product, String stock, String minStock, String unit, String status) {
            this.branch = branch;
            this.product = product;
            this.stock = stock;
            this.minStock = minStock;
            this.unit = unit;
            this.status = status;
        }

        public String getBranch() { return branch; }
        public String getProduct() { return product; }
        public String getStock() { return stock; }
        public String getMinStock() { return minStock; }
        public String getUnit() { return unit; }
        public String getStatus() { return status; }
    }

    public static class StaffDistribution {
        private final String branch;
        private final String total;
        private final String reception;
        private final String care;
        private final String manager;
        private final String status;

        public StaffDistribution(String branch, String total, String reception, String care, String manager, String status) {
            this.branch = branch;
            this.total = total;
            this.reception = reception;
            this.care = care;
            this.manager = manager;
            this.status = status;
        }

        public String getBranch() { return branch; }
        public String getTotal() { return total; }
        public String getReception() { return reception; }
        public String getCare() { return care; }
        public String getManager() { return manager; }
        public String getStatus() { return status; }
    }
}