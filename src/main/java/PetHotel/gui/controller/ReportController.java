package PetHotel.gui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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

    // --- Biểu đồ ---
    @FXML private ComboBox<String> revenueChartGroupBy;
    @FXML private VBox revenueChartArea;
    @FXML private VBox occupancyChartArea;

    // --- Bảng Xếp Hạng Sản Phẩm ---
    @FXML private TableView<?> topProductTable;
    @FXML private TableColumn<?, ?> colRank;
    @FXML private TableColumn<?, ?> colProdName;
    @FXML private TableColumn<?, ?> colProdCat;
    @FXML private TableColumn<?, ?> colProdSold;
    @FXML private TableColumn<?, ?> colProdRevenue;

    // --- Thống Kê Tồn Kho ---
    @FXML private Label invStatOk;
    @FXML private Label invStatLow;
    @FXML private Label invStatCritical;

    // --- Danh sách báo cáo định kỳ ---
    @FXML private VBox reportTaskList;


    // ==========================================
    // 2. KHỞI TẠO DỮ LIỆU BAN ĐẦU
    // ==========================================
    
    @FXML
    public void initialize() {
        System.out.println("Đã nạp thành công giao diện Báo Cáo & Thống Kê!");
        
        // Khởi tạo danh sách chi nhánh mẫu
        filterBranch.getItems().addAll("Tất cả chi nhánh", "Chi nhánh Q.1", "Chi nhánh Q.3");
        filterBranch.getSelectionModel().selectFirst();
        
        // Load dữ liệu mặc định ban đầu
        loadReportData();
    }

    // Hàm gọi dữ liệu từ Database (BUS/DAO) để đổ lên màn hình
    private void loadReportData() {
        System.out.println("Đang truy xuất dữ liệu thống kê từ cơ sở dữ liệu...");
        // TODO: Viết code lấy dữ liệu thật ở đây
        // Ví dụ: kpiRevenue.setText("125.000.000đ");
    }

    // ==========================================
    // 3. XỬ LÝ SỰ KIỆN: BỘ LỌC THỜI GIAN KỲ BÁO CÁO
    // ==========================================

    @FXML
    public void onPeriodToday(ActionEvent event) {
        setPeriodActive(btnPeriodToday);
        System.out.println("Lọc báo cáo: Hôm nay");
        loadReportData();
    }

    @FXML
    public void onPeriodWeek(ActionEvent event) {
        setPeriodActive(btnPeriodWeek);
        System.out.println("Lọc báo cáo: Tuần này");
        loadReportData();
    }

    @FXML
    public void onPeriodMonth(ActionEvent event) {
        setPeriodActive(btnPeriodMonth);
        System.out.println("Lọc báo cáo: Tháng này");
        loadReportData();
    }

    @FXML
    public void onPeriodQuarter(ActionEvent event) {
        setPeriodActive(btnPeriodQuarter);
        System.out.println("Lọc báo cáo: Quý này");
        loadReportData();
    }

    @FXML
    public void onPeriodYear(ActionEvent event) {
        setPeriodActive(btnPeriodYear);
        System.out.println("Lọc báo cáo: Năm nay");
        loadReportData();
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
        loadReportData();
    }

    // Hàm tiện ích: Đổi màu nút chọn thời gian đang Active và ẩn thanh Custom Date
    private void setPeriodActive(Button activeBtn) {
        // Tắt màu active của tất cả các nút
        btnPeriodToday.getStyleClass().remove("period-btn-active");
        btnPeriodWeek.getStyleClass().remove("period-btn-active");
        btnPeriodMonth.getStyleClass().remove("period-btn-active");
        btnPeriodQuarter.getStyleClass().remove("period-btn-active");
        btnPeriodYear.getStyleClass().remove("period-btn-active");
        btnPeriodCustom.getStyleClass().remove("period-btn-active");

        // Bật màu cho nút được click
        activeBtn.getStyleClass().add("period-btn-active");
        
        // Mặc định ẩn thanh chọn ngày tùy chỉnh, trừ khi bấm vào nút Tùy chọn
        if (activeBtn != btnPeriodCustom) {
            customDateRow.setVisible(false);
            customDateRow.setManaged(false);
        }
    }

    // ==========================================
    // 4. XỬ LÝ SỰ KIỆN: XUẤT BÁO CÁO & XEM CHI TIẾT
    // ==========================================

    @FXML
    public void onRefreshReport(ActionEvent event) {
        System.out.println("Làm mới dữ liệu báo cáo cho chi nhánh: " + filterBranch.getValue());
        loadReportData();
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
}