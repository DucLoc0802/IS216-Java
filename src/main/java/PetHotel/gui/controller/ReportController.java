package PetHotel.gui.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import PetHotel.bus.ReportBUS;
import PetHotel.model.BookingReport;
import PetHotel.model.ChainReport;
import PetHotel.model.InventoryReport;
import PetHotel.model.RevenueReport;
import PetHotel.model.RoomUsageReport;
import PetHotel.util.Role;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class ReportController {

    @FXML private ComboBox<String> cbbReportType;
    @FXML private DatePicker dpFromDate;
    @FXML private DatePicker dpToDate;
    @FXML private Label lblSummaryTitle1;
    @FXML private Label lblSummaryValue1;
    @FXML private Label lblSummaryNote1;
    @FXML private Label lblSummaryTitle2;
    @FXML private Label lblSummaryValue2;
    @FXML private Label lblSummaryNote2;
    @FXML private Label lblSummaryTitle3;
    @FXML private Label lblSummaryValue3;
    @FXML private Label lblSummaryNote3;
    @FXML private Label lblSummaryTitle4;
    @FXML private Label lblSummaryValue4;
    @FXML private Label lblSummaryNote4;
    @FXML private TabPane tabPaneReport;
    @FXML private Tab tabRevenue;
    @FXML private Tab tabBooking;
    @FXML private Tab tabRoomUsage;
    @FXML private Tab tabInventory;
    @FXML private Tab tabChain;

    @FXML private TableView<RevenueReport> tableRevenue;
    @FXML private TableColumn<RevenueReport, String> colRevenuePeriod;
    @FXML private TableColumn<RevenueReport, Integer> colRevenueInvoiceCount;
    @FXML private TableColumn<RevenueReport, Double> colRevenueTotal;
    @FXML private TableColumn<RevenueReport, Double> colRevenuePaid;
    @FXML private TableColumn<RevenueReport, Double> colRevenueRemaining;

    @FXML private TableView<BookingReport> tableBooking;
    @FXML private TableColumn<BookingReport, String> colBookingPeriod;
    @FXML private TableColumn<BookingReport, Integer> colBookingCount;
    @FXML private TableColumn<BookingReport, Integer> colBookingNew;
    @FXML private TableColumn<BookingReport, Integer> colBookingCompleted;
    @FXML private TableColumn<BookingReport, Integer> colBookingCancelled;

    @FXML private TableView<RoomUsageReport> tableRoomUsage;
    @FXML private TableColumn<RoomUsageReport, String> colRoomPeriod;
    @FXML private TableColumn<RoomUsageReport, Integer> colRoomTotal;
    @FXML private TableColumn<RoomUsageReport, Integer> colRoomInUse;
    @FXML private TableColumn<RoomUsageReport, Integer> colRoomAvailable;
    @FXML private TableColumn<RoomUsageReport, Double> colRoomUsageRate;

    @FXML private TableView<InventoryReport> tableInventory;
    @FXML private TableColumn<InventoryReport, String> colInventoryScope;
    @FXML private TableColumn<InventoryReport, Integer> colInventorySku;
    @FXML private TableColumn<InventoryReport, Double> colInventoryStock;
    @FXML private TableColumn<InventoryReport, Integer> colInventoryLow;
    @FXML private TableColumn<InventoryReport, Integer> colInventoryOut;

    @FXML private TableView<ChainReport> tableChain;
    @FXML private TableColumn<ChainReport, String> colChainBranchId;
    @FXML private TableColumn<ChainReport, String> colChainBranchName;
    @FXML private TableColumn<ChainReport, Double> colChainRevenue;
    @FXML private TableColumn<ChainReport, Integer> colChainBooking;
    @FXML private TableColumn<ChainReport, Integer> colChainRoomInUse;

    private final ReportBUS reportBUS = new ReportBUS();
    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");
    private final DateTimeFormatter fileDateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");

    @FXML
    public void initialize() {
        cbbReportType.setItems(FXCollections.observableArrayList("Theo ngày", "Theo tuần", "Theo tháng"));
        cbbReportType.setValue("Theo tháng");

        configureColumns();
        applyRoleTabs();

        tabPaneReport.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> loadSelectedReport());

        LocalDate today = LocalDate.now();
        dpFromDate.setValue(today.withDayOfMonth(1));
        dpToDate.setValue(today);
        loadSelectedReport();
    }

    @FXML
    public void onFilterReport(ActionEvent event) {
        loadSelectedReport();
    }

    @FXML
    public void onResetReport(ActionEvent event) {
        LocalDate today = LocalDate.now();
        cbbReportType.setValue("Theo tháng");
        dpFromDate.setValue(today.withDayOfMonth(1));
        dpToDate.setValue(today);
        loadSelectedReport();
    }

    @FXML
    public void onExportReport(ActionEvent event) {
        try {
            Path exportFile = exportCurrentReport();
            showExportSuccessDialog(exportFile);
        } catch (IllegalArgumentException ex) {
            showAlert(Alert.AlertType.WARNING, "Không thể xuất báo cáo", ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể xuất báo cáo: " + ex.getMessage());
        }
    }

    private void configureColumns() {
        colRevenuePeriod.setCellValueFactory(new PropertyValueFactory<>("period"));
        colRevenueInvoiceCount.setCellValueFactory(new PropertyValueFactory<>("invoiceCount"));
        colRevenueTotal.setCellValueFactory(new PropertyValueFactory<>("totalRevenue"));
        colRevenuePaid.setCellValueFactory(new PropertyValueFactory<>("totalPaid"));
        colRevenueRemaining.setCellValueFactory(new PropertyValueFactory<>("remaining"));

        colBookingPeriod.setCellValueFactory(new PropertyValueFactory<>("period"));
        colBookingCount.setCellValueFactory(new PropertyValueFactory<>("bookingCount"));
        colBookingNew.setCellValueFactory(new PropertyValueFactory<>("newBookingCount"));
        colBookingCompleted.setCellValueFactory(new PropertyValueFactory<>("completedBookingCount"));
        colBookingCancelled.setCellValueFactory(new PropertyValueFactory<>("cancelledBookingCount"));

        colRoomPeriod.setCellValueFactory(new PropertyValueFactory<>("period"));
        colRoomTotal.setCellValueFactory(new PropertyValueFactory<>("totalRoom"));
        colRoomInUse.setCellValueFactory(new PropertyValueFactory<>("inUseRoom"));
        colRoomAvailable.setCellValueFactory(new PropertyValueFactory<>("availableRoom"));
        colRoomUsageRate.setCellValueFactory(new PropertyValueFactory<>("usageRate"));

        colInventoryScope.setCellValueFactory(new PropertyValueFactory<>("scope"));
        colInventorySku.setCellValueFactory(new PropertyValueFactory<>("totalSku"));
        colInventoryStock.setCellValueFactory(new PropertyValueFactory<>("totalStock"));
        colInventoryLow.setCellValueFactory(new PropertyValueFactory<>("lowStockCount"));
        colInventoryOut.setCellValueFactory(new PropertyValueFactory<>("outOfStockCount"));

        colChainBranchId.setCellValueFactory(new PropertyValueFactory<>("branchId"));
        colChainBranchName.setCellValueFactory(new PropertyValueFactory<>("branchName"));
        colChainRevenue.setCellValueFactory(new PropertyValueFactory<>("totalRevenue"));
        colChainBooking.setCellValueFactory(new PropertyValueFactory<>("bookingCount"));
        colChainRoomInUse.setCellValueFactory(new PropertyValueFactory<>("roomInUse"));
    }

    private void applyRoleTabs() {
        Role role = currentRole();
        if (role == Role.RECEPTIONIST || role == Role.PET_CARE_STAFF) {
            showAlert(Alert.AlertType.WARNING, "Không có quyền", "Bạn không có quyền xem báo cáo.");
            tabPaneReport.setDisable(true);
            return;
        }
        if (role != Role.CEO && role != Role.ADMIN) {
            tabPaneReport.getTabs().remove(tabChain);
        }
    }

    private void loadSelectedReport() {
        if (tabPaneReport == null || tabPaneReport.isDisabled()) {
            return;
        }

        try {
            Tab selected = tabPaneReport.getSelectionModel().getSelectedItem();
            if (selected == tabRevenue) {
                loadRevenueReport();
            } else if (selected == tabBooking) {
                loadBookingReport();
            } else if (selected == tabRoomUsage) {
                loadRoomUsageReport();
            } else if (selected == tabInventory) {
                loadInventoryReport();
            } else if (selected == tabChain) {
                loadChainReport();
            }
        } catch (IllegalArgumentException ex) {
            showAlert(Alert.AlertType.WARNING, "Dữ liệu không hợp lệ", ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể tải báo cáo: " + ex.getMessage());
        }
    }

    private void loadRevenueReport() throws Exception {
        List<RevenueReport> reports = reportBUS.getRevenueReport(currentType(), fromDate(), toDate());
        tableRevenue.setItems(FXCollections.observableArrayList(reports));

        double revenue = 0;
        double paid = 0;
        double remaining = 0;
        int invoices = 0;
        for (RevenueReport report : reports) {
            revenue += report.getTotalRevenue();
            paid += report.getTotalPaid();
            remaining += report.getRemaining();
            invoices += report.getInvoiceCount();
        }
        setSummary("Tổng doanh thu", formatMoney(revenue), "payments SUCCESS",
            "Đã thanh toán", formatMoney(paid), "Tổng tiền thực thu",
            "Số hóa đơn", String.valueOf(invoices), "Hóa đơn có giao dịch",
            "Còn lại", formatMoney(remaining), "Theo tổng hóa đơn");
    }

    private void loadBookingReport() throws Exception {
        List<BookingReport> reports = reportBUS.getBookingReport(currentType(), fromDate(), toDate());
        tableBooking.setItems(FXCollections.observableArrayList(reports));

        int total = 0;
        int newCount = 0;
        int completed = 0;
        int cancelled = 0;
        for (BookingReport report : reports) {
            total += report.getBookingCount();
            newCount += report.getNewBookingCount();
            completed += report.getCompletedBookingCount();
            cancelled += report.getCancelledBookingCount();
        }
        setSummary("Tổng booking", String.valueOf(total), "Từ bảng booking",
            "Booking mới", String.valueOf(newCount), "PENDING/CONFIRMED",
            "Hoàn thành", String.valueOf(completed), "CHECKED_OUT",
            "Đã hủy", String.valueOf(cancelled), "CANCELLED");
    }

    private void loadRoomUsageReport() throws Exception {
        List<RoomUsageReport> reports = reportBUS.getRoomUsageReport(fromDate(), toDate());
        tableRoomUsage.setItems(FXCollections.observableArrayList(reports));

        RoomUsageReport report = reports.isEmpty() ? new RoomUsageReport() : reports.get(0);
        setSummary("Tổng phòng", String.valueOf(report.getTotalRoom()), "Từ bảng room",
            "Đang sử dụng", String.valueOf(report.getInUseRoom()), "status IN_USE",
            "Còn trống", String.valueOf(report.getAvailableRoom()), "status AVAILABLE",
            "Công suất", formatPercent(report.getUsageRate()), "Hiện tại");
    }

    private void loadInventoryReport() throws Exception {
        List<InventoryReport> reports = reportBUS.getInventoryReport();
        tableInventory.setItems(FXCollections.observableArrayList(reports));

        int sku = 0;
        double stock = 0;
        int low = 0;
        int out = 0;
        for (InventoryReport report : reports) {
            sku += report.getTotalSku();
            stock += report.getTotalStock();
            low += report.getLowStockCount();
            out += report.getOutOfStockCount();
        }
        setSummary("Tổng SKU", String.valueOf(sku), "branch_inventory",
            "Tổng tồn", moneyFormat.format(stock), "Số lượng tồn kho",
            "Tồn thấp", String.valueOf(low), "<= reorder_point",
            "Hết hàng", String.valueOf(out), "quantity = 0");
    }

    private void loadChainReport() throws Exception {
        List<ChainReport> reports = reportBUS.getChainReport(currentType(), fromDate(), toDate(), currentRole());
        tableChain.setItems(FXCollections.observableArrayList(reports));

        double revenue = 0;
        int booking = 0;
        int roomInUse = 0;
        for (ChainReport report : reports) {
            revenue += report.getTotalRevenue();
            booking += report.getBookingCount();
            roomInUse += report.getRoomInUse();
        }
        setSummary("Doanh thu chuỗi", formatMoney(revenue), "Theo chi nhánh",
            "Booking", String.valueOf(booking), "Tổng booking",
            "Phòng đang dùng", String.valueOf(roomInUse), "Toàn hệ thống",
            "Số chi nhánh", String.valueOf(reports.size()), "branch");
    }

    private Path exportCurrentReport() throws IOException {
        Tab selected = tabPaneReport.getSelectionModel().getSelectedItem();
        if (selected == null) {
            throw new IllegalArgumentException("Chưa chọn báo cáo để xuất.");
        }

        String key = reportKey(selected);
        String title = reportTitle(selected);
        Path exportDir = Path.of("exports");
        Files.createDirectories(exportDir);
        Path exportFile = exportDir.resolve("report_" + key + "_" + LocalDate.now().format(fileDateFormat) + ".txt");

        StringBuilder content = new StringBuilder();
        content.append(title).append("\n");
        content.append("Thời gian xuất: ").append(LocalDateTime.now()).append("\n");
        content.append("Loại thống kê: ").append(currentType()).append("\n");
        content.append("Từ ngày: ").append(dpFromDate.getValue()).append("\n");
        content.append("Đến ngày: ").append(dpToDate.getValue()).append("\n\n");
        appendTableData(content, selected);

        Files.writeString(exportFile, content.toString(), StandardCharsets.UTF_8);
        return exportFile;
    }

    private void appendTableData(StringBuilder content, Tab selected) {
        if (selected == tabRevenue) {
            content.append("Thời gian\tSố hóa đơn\tTổng doanh thu\tĐã thanh toán\tCòn lại\n");
            for (RevenueReport row : tableRevenue.getItems()) {
                content.append(row.getPeriod()).append('\t')
                    .append(row.getInvoiceCount()).append('\t')
                    .append(row.getTotalRevenue()).append('\t')
                    .append(row.getTotalPaid()).append('\t')
                    .append(row.getRemaining()).append('\n');
            }
        } else if (selected == tabBooking) {
            content.append("Thời gian\tSố booking\tBooking mới\tHoàn thành\tĐã hủy\n");
            for (BookingReport row : tableBooking.getItems()) {
                content.append(row.getPeriod()).append('\t')
                    .append(row.getBookingCount()).append('\t')
                    .append(row.getNewBookingCount()).append('\t')
                    .append(row.getCompletedBookingCount()).append('\t')
                    .append(row.getCancelledBookingCount()).append('\n');
            }
        } else if (selected == tabRoomUsage) {
            content.append("Thời gian\tTổng phòng\tĐang sử dụng\tCòn trống\tTỷ lệ sử dụng\n");
            for (RoomUsageReport row : tableRoomUsage.getItems()) {
                content.append(row.getPeriod()).append('\t')
                    .append(row.getTotalRoom()).append('\t')
                    .append(row.getInUseRoom()).append('\t')
                    .append(row.getAvailableRoom()).append('\t')
                    .append(formatPercent(row.getUsageRate())).append('\n');
            }
        } else if (selected == tabInventory) {
            content.append("Chi nhánh\tTổng SKU\tTổng tồn kho\tTồn thấp\tHết hàng\n");
            for (InventoryReport row : tableInventory.getItems()) {
                content.append(row.getScope()).append('\t')
                    .append(row.getTotalSku()).append('\t')
                    .append(row.getTotalStock()).append('\t')
                    .append(row.getLowStockCount()).append('\t')
                    .append(row.getOutOfStockCount()).append('\n');
            }
        } else if (selected == tabChain) {
            content.append("Mã chi nhánh\tTên chi nhánh\tTổng doanh thu\tSố booking\tPhòng đang dùng\n");
            for (ChainReport row : tableChain.getItems()) {
                content.append(row.getBranchId()).append('\t')
                    .append(row.getBranchName()).append('\t')
                    .append(row.getTotalRevenue()).append('\t')
                    .append(row.getBookingCount()).append('\t')
                    .append(row.getRoomInUse()).append('\n');
            }
        }
    }

    private String reportKey(Tab tab) {
        if (tab == tabBooking) return "booking";
        if (tab == tabRoomUsage) return "room_usage";
        if (tab == tabInventory) return "inventory";
        if (tab == tabChain) return "chain";
        return "revenue";
    }

    private String reportTitle(Tab tab) {
        if (tab == tabBooking) return "BÁO CÁO THỐNG KÊ BOOKING";
        if (tab == tabRoomUsage) return "BÁO CÁO CÔNG SUẤT PHÒNG";
        if (tab == tabInventory) return "BÁO CÁO THỐNG KÊ KHO";
        if (tab == tabChain) return "BÁO CÁO TOÀN CHUỖI";
        return "BÁO CÁO DOANH THU";
    }

    private String currentType() {
        return cbbReportType.getValue() == null ? "Theo tháng" : cbbReportType.getValue();
    }

    private Date fromDate() {
        return toDate(dpFromDate.getValue());
    }

    private Date toDate() {
        return toDate(dpToDate.getValue());
    }

    private Date toDate(LocalDate localDate) {
        return localDate == null ? null : Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Role currentRole() {
        return SessionManager.getInstance().getCurrentUser() == null
            ? null
            : SessionManager.getInstance().getCurrentUser().getRole();
    }

    private void setSummary(
            String title1, String value1, String note1,
            String title2, String value2, String note2,
            String title3, String value3, String note3,
            String title4, String value4, String note4) {
        lblSummaryTitle1.setText(title1);
        lblSummaryValue1.setText(value1);
        lblSummaryNote1.setText(note1);
        lblSummaryTitle2.setText(title2);
        lblSummaryValue2.setText(value2);
        lblSummaryNote2.setText(note2);
        lblSummaryTitle3.setText(title3);
        lblSummaryValue3.setText(value3);
        lblSummaryNote3.setText(note3);
        lblSummaryTitle4.setText(title4);
        lblSummaryValue4.setText(value4);
        lblSummaryNote4.setText(note4);
    }

    private String formatMoney(double value) {
        return moneyFormat.format(value) + " VNĐ";
    }

    private String formatPercent(double value) {
        return String.format("%.1f%%", value);
    }

    private void showExportSuccessDialog(Path exportFile) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Xuất báo cáo thành công");
        dialog.setHeaderText(null);

        ButtonType closeButtonType = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);

        Label icon = new Label("✓");
        icon.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: #16a34a;");
        Label title = new Label("Xuất báo cáo thành công");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #8b5a2b;");
        Label message = new Label("Báo cáo đã được xuất thành công.");
        Label path = new Label(exportFile.toAbsolutePath().toString());
        path.setWrapText(true);
        path.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8; -fx-padding: 10; -fx-text-fill: #4b5563;");

        VBox content = new VBox(10, icon, title, message, path);
        content.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 22; -fx-min-width: 420;");
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setStyle("-fx-background-color: white; -fx-padding: 12;");

        Button closeButton = (Button) dialog.getDialogPane().lookupButton(closeButtonType);
        closeButton.setStyle("-fx-background-color: #b86b2b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 18;");
        dialog.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
