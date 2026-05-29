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
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import PetHotel.bus.ReportBUS;
import PetHotel.model.BookingReport;
import PetHotel.model.ChainReport;
import PetHotel.model.InventoryItemReport;
import PetHotel.model.InventoryReport;
import PetHotel.model.RevenueReport;
import PetHotel.model.RoomTypeReport;
import PetHotel.model.RoomUsageReport;
import PetHotel.model.ServiceReport;
import PetHotel.util.Role;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

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
    @FXML private Tab tabOverview;
    @FXML private Tab tabRevenue;
    @FXML private Tab tabBooking;
    @FXML private Tab tabService;
    @FXML private Tab tabRoomUsage;
    @FXML private Tab tabInventory;
    @FXML private Tab tabChain;

    @FXML private Label lblOverviewTodayBooking;
    @FXML private Label lblOverviewCheckedIn;
    @FXML private Label lblOverviewUpcomingCheckIn;
    @FXML private Label lblOverviewUpcomingCheckOut;
    @FXML private Label lblOverviewRevenue;
    @FXML private Label lblOverviewPaid;
    @FXML private Label lblOverviewRemaining;
    @FXML private Label lblOverviewRoomUsage;
    @FXML private BarChart<String, Number> chartOverviewBookingByDay;
    @FXML private PieChart pieOverviewBookingStatus;

    @FXML private BarChart<String, Number> chartRevenueByPeriod;
    @FXML private PieChart pieRevenueSource;

    @FXML private BarChart<String, Number> chartBookingByPeriod;
    @FXML private PieChart pieBookingStatus;

    @FXML private BarChart<String, Number> chartServiceUsage;
    @FXML private BarChart<String, Number> chartServiceRevenue;

    @FXML private BarChart<String, Number> chartRoomUsageByType;
    @FXML private PieChart pieRoomStatus;

    @FXML private BarChart<String, Number> chartInventoryTopUsage;
    @FXML private BarChart<String, Number> chartInventoryLowStock;

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
    @FXML private TableColumn<BookingReport, Integer> colBookingPending;
    @FXML private TableColumn<BookingReport, Integer> colBookingConfirmed;
    @FXML private TableColumn<BookingReport, Integer> colBookingCheckedIn;
    @FXML private TableColumn<BookingReport, Integer> colBookingCheckedOut;

    @FXML private TableView<ServiceReport> tableService;
    @FXML private TableColumn<ServiceReport, String> colServiceName;
    @FXML private TableColumn<ServiceReport, Integer> colServiceUsageCount;
    @FXML private TableColumn<ServiceReport, String> colServiceRevenue;
    @FXML private TableColumn<ServiceReport, String> colServiceUsageRate;

    @FXML private TableView<RoomTypeReport> tableRoomType;
    @FXML private TableColumn<RoomTypeReport, String> colRoomTypeName;
    @FXML private TableColumn<RoomTypeReport, Integer> colRoomTypeTotal;
    @FXML private TableColumn<RoomTypeReport, Integer> colRoomTypeInUse;
    @FXML private TableColumn<RoomTypeReport, Integer> colRoomTypeAvailable;
    @FXML private TableColumn<RoomTypeReport, Integer> colRoomTypeMaintenance;
    @FXML private TableColumn<RoomTypeReport, String> colRoomTypeUsageRate;

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

    @FXML private TableView<InventoryItemReport> tableInventoryLowStock;
    @FXML private TableColumn<InventoryItemReport, String> colInventoryProductName;
    @FXML private TableColumn<InventoryItemReport, String> colInventoryCurrentStock;
    @FXML private TableColumn<InventoryItemReport, String> colInventoryMinimumStock;
    @FXML private TableColumn<InventoryItemReport, String> colInventoryUnit;
    @FXML private TableColumn<InventoryItemReport, String> colInventoryStatus;

    @FXML private TableView<ChainReport> tableChain;
    @FXML private TableColumn<ChainReport, String> colChainBranchId;
    @FXML private TableColumn<ChainReport, String> colChainBranchName;
    @FXML private TableColumn<ChainReport, Double> colChainRevenue;
    @FXML private TableColumn<ChainReport, Integer> colChainBooking;
    @FXML private TableColumn<ChainReport, Integer> colChainRoomInUse;

    private final ReportBUS reportBUS = new ReportBUS();
    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");
    private final DecimalFormat quantityFormat = new DecimalFormat("#,###.##");
    private final DateTimeFormatter fileDateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final DateTimeFormatter chartDateFormat = DateTimeFormatter.ofPattern("dd/MM");

    private static final String COLOR_REVENUE = "#A65A2E";
    private static final String COLOR_BOOKING = "#4E79A7";
    private static final String COLOR_GOOD = "#59A14F";
    private static final String COLOR_ATTENTION = "#F2C14E";
    private static final String COLOR_DONE = "#8E6BBE";
    private static final String COLOR_DANGER = "#E15759";
    private static final String COLOR_OTHER = "#BAB0AC";
    private static final List<String> BOOKING_STATUS_COLORS =
        List.of(COLOR_ATTENTION, COLOR_BOOKING, COLOR_GOOD, COLOR_DONE, COLOR_DANGER);
    private static final List<String> ROOM_STATUS_COLORS =
        List.of(COLOR_GOOD, COLOR_BOOKING, COLOR_ATTENTION, COLOR_DANGER);
    private static final List<String> REVENUE_SOURCE_COLORS =
        List.of(COLOR_REVENUE, COLOR_BOOKING, COLOR_ATTENTION);
    private static final List<String> SERVICE_COLORS =
        List.of(COLOR_BOOKING, COLOR_REVENUE, COLOR_ATTENTION, COLOR_GOOD, COLOR_DONE);

    @FXML
    public void initialize() {
        cbbReportType.setItems(FXCollections.observableArrayList("Theo ngày", "Theo tuần", "Theo tháng"));
        cbbReportType.setValue("Theo tháng");

        configureColumns();
        configureChartDefaults();
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
        colBookingPending.setCellValueFactory(new PropertyValueFactory<>("pendingBookingCount"));
        colBookingConfirmed.setCellValueFactory(new PropertyValueFactory<>("confirmedBookingCount"));
        colBookingCheckedIn.setCellValueFactory(new PropertyValueFactory<>("checkedInBookingCount"));
        colBookingCheckedOut.setCellValueFactory(new PropertyValueFactory<>("checkedOutBookingCount"));

        colServiceName.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        colServiceUsageCount.setCellValueFactory(new PropertyValueFactory<>("usageCount"));
        colServiceRevenue.setCellValueFactory(data ->
            new SimpleStringProperty(formatMoney(data.getValue().getRevenue())));
        colServiceUsageRate.setCellValueFactory(data ->
            new SimpleStringProperty(formatPercent(data.getValue().getUsageRate())));

        colRoomTypeName.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        colRoomTypeTotal.setCellValueFactory(new PropertyValueFactory<>("totalRoom"));
        colRoomTypeInUse.setCellValueFactory(new PropertyValueFactory<>("inUseRoom"));
        colRoomTypeAvailable.setCellValueFactory(new PropertyValueFactory<>("availableRoom"));
        colRoomTypeMaintenance.setCellValueFactory(new PropertyValueFactory<>("maintenanceRoom"));
        colRoomTypeUsageRate.setCellValueFactory(data ->
            new SimpleStringProperty(formatPercent(data.getValue().getUsageRate())));

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

        colInventoryProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colInventoryCurrentStock.setCellValueFactory(data ->
            new SimpleStringProperty(formatQuantity(data.getValue().getCurrentStock())));
        colInventoryMinimumStock.setCellValueFactory(data ->
            new SimpleStringProperty(formatQuantity(data.getValue().getMinimumStock())));
        colInventoryUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colInventoryStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colChainBranchId.setCellValueFactory(new PropertyValueFactory<>("branchId"));
        colChainBranchName.setCellValueFactory(new PropertyValueFactory<>("branchName"));
        colChainRevenue.setCellValueFactory(new PropertyValueFactory<>("totalRevenue"));
        colChainBooking.setCellValueFactory(new PropertyValueFactory<>("bookingCount"));
        colChainRoomInUse.setCellValueFactory(new PropertyValueFactory<>("roomInUse"));

        fixTableColumnBindings();
    }

    private void fixTableColumnBindings() {
        applyReportTableStyle();
        alignColumnCenter(colRevenuePeriod);
        applyIntegerCell(colRevenueInvoiceCount, COLOR_OTHER, "center");
        applyMoneyCell(colRevenueTotal, COLOR_REVENUE);
        applyMoneyCell(colRevenuePaid, COLOR_GOOD);
        applyMoneyCell(colRevenueRemaining, COLOR_ATTENTION);

        alignColumnCenter(colBookingPeriod);
        alignColumnCenter(colServiceName);
        alignColumnCenter(colRoomTypeName);
        alignColumnCenter(colRoomPeriod);
        alignColumnCenter(colInventoryProductName);
        alignColumnCenter(colInventoryUnit);
        alignColumnCenter(colInventoryScope);
        alignColumnCenter(colChainBranchId);
        alignColumnCenter(colChainBranchName);
        applyMoneyCell(colChainRevenue, COLOR_REVENUE);
        applyIntegerCell(colChainBooking, COLOR_BOOKING, "center");
        applyIntegerCell(colChainRoomInUse, COLOR_GOOD, "center");

        applyIntegerCell(colBookingCount, COLOR_BOOKING, "center");
        applyIntegerCell(colBookingNew, COLOR_ATTENTION, "center");
        applyIntegerCell(colBookingCompleted, COLOR_DONE, "center");
        applyIntegerCell(colBookingCancelled, COLOR_DANGER, "center");
        applyIntegerCell(colBookingPending, COLOR_ATTENTION, "center");
        applyIntegerCell(colBookingConfirmed, COLOR_BOOKING, "center");
        applyIntegerCell(colBookingCheckedIn, COLOR_GOOD, "center");
        applyIntegerCell(colBookingCheckedOut, COLOR_DONE, "center");

        applyIntegerCell(colServiceUsageCount, COLOR_BOOKING, "center");
        applyStringCell(colServiceRevenue, COLOR_REVENUE, "center");
        applyStringCell(colServiceUsageRate, COLOR_GOOD, "center");

        applyIntegerCell(colRoomTypeTotal, COLOR_BOOKING, "center");
        applyIntegerCell(colRoomTypeInUse, COLOR_GOOD, "center");
        applyIntegerCell(colRoomTypeAvailable, COLOR_BOOKING, "center");
        applyIntegerCell(colRoomTypeMaintenance, COLOR_ATTENTION, "center");
        applyStringCell(colRoomTypeUsageRate, COLOR_GOOD, "center");
        applyIntegerCell(colRoomTotal, COLOR_BOOKING, "center");
        applyIntegerCell(colRoomInUse, COLOR_GOOD, "center");
        applyIntegerCell(colRoomAvailable, COLOR_BOOKING, "center");
        applyPercentCell(colRoomUsageRate, COLOR_GOOD);

        applyIntegerCell(colInventorySku, COLOR_BOOKING, "center");
        applyQuantityCell(colInventoryStock, COLOR_BOOKING);
        applyIntegerCell(colInventoryLow, COLOR_ATTENTION, "center");
        applyIntegerCell(colInventoryOut, COLOR_DANGER, "center");
        applyStringCell(colInventoryCurrentStock, COLOR_ATTENTION, "center");
        applyStringCell(colInventoryMinimumStock, COLOR_OTHER, "center");
        applyInventoryStatusCell();
    }

    @SafeVarargs
    private final void centerColumns(TableColumn<?, ?>... columns) {
        for (TableColumn<?, ?> column : columns) {
            column.setStyle("-fx-alignment: CENTER;");
        }
    }

    private void applyReportTableStyle() {
        addReportTableStyle(tableRevenue, tableBooking, tableService, tableRoomType,
            tableRoomUsage, tableInventoryLowStock, tableInventory, tableChain);
        centerColumns(
            colRevenuePeriod, colRevenueInvoiceCount, colRevenueTotal, colRevenuePaid, colRevenueRemaining,
            colBookingPeriod, colBookingCount, colBookingNew, colBookingCompleted, colBookingCancelled,
            colBookingPending, colBookingConfirmed, colBookingCheckedIn, colBookingCheckedOut,
            colServiceName, colServiceUsageCount, colServiceRevenue, colServiceUsageRate,
            colRoomTypeName, colRoomTypeTotal, colRoomTypeInUse, colRoomTypeAvailable,
            colRoomTypeMaintenance, colRoomTypeUsageRate,
            colRoomPeriod, colRoomTotal, colRoomInUse, colRoomAvailable, colRoomUsageRate,
            colInventoryProductName, colInventoryCurrentStock, colInventoryMinimumStock,
            colInventoryUnit, colInventoryStatus,
            colInventoryScope, colInventorySku, colInventoryStock, colInventoryLow, colInventoryOut,
            colChainBranchId, colChainBranchName, colChainRevenue, colChainBooking, colChainRoomInUse
        );
    }

    @SafeVarargs
    private final void addReportTableStyle(TableView<?>... tables) {
        for (TableView<?> table : tables) {
            if (!table.getStyleClass().contains("report-table")) {
                table.getStyleClass().add("report-table");
            }
        }
    }

    private <S, T> void alignColumnCenter(TableColumn<S, T> column) {
        alignColumn(column, "center");
    }

    private <S, T> void alignColumn(TableColumn<S, T> column, String alignment) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(T value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(String.valueOf(value));
                setStyle(cellStyle(null, alignment, false));
            }
        });
    }

    private <S> void applyMoneyCell(TableColumn<S, Double> column, String color) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(formatMoney(value));
                setStyle(cellStyle(color, "center", true));
            }
        });
    }

    private <S> void applyQuantityCell(TableColumn<S, Double> column, String color) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(formatQuantity(value));
                setStyle(cellStyle(color, "center", false));
            }
        });
    }

    private <S> void applyPercentCell(TableColumn<S, Double> column, String color) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(formatPercent(value));
                setStyle(cellStyle(color, "center", true));
            }
        });
    }

    private <S> void applyIntegerCell(TableColumn<S, Integer> column, String color, String alignment) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(String.valueOf(value));
                setStyle(cellStyle(color, alignment, true));
            }
        });
    }

    private <S> void applyStringCell(TableColumn<S, String> column, String color, String alignment) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(value);
                setStyle(cellStyle(color, alignment, color != null));
            }
        });
    }

    private void applyInventoryStatusCell() {
        colInventoryStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(status);
                setStyle(cellStyle(statusColor(status), "center", true));
            }
        });
    }

    private String cellStyle(String color, String alignment, boolean bold) {
        StringBuilder style = new StringBuilder("-fx-alignment: ").append(alignment).append(";");
        if (color != null) {
            style.append("-fx-text-fill: ").append(color).append(";");
        }
        if (bold) {
            style.append("-fx-font-weight: bold;");
        }
        return style.toString();
    }

    private String statusColor(String status) {
        if (status == null) {
            return COLOR_OTHER;
        }
        if (status.contains("Hết hàng")) {
            return COLOR_DANGER;
        }
        if (status.contains("Cần nhập thêm")) {
            return COLOR_DANGER;
        }
        if (status.contains("Sắp hết")) {
            return COLOR_ATTENTION;
        }
        if (status.contains("Đủ hàng")) {
            return COLOR_GOOD;
        }
        return COLOR_OTHER;
    }

    private void configureChartDefaults() {
        configureBarChart(chartOverviewBookingByDay, false, false);
        configureBarChart(chartRevenueByPeriod, false, true);
        configureBarChart(chartBookingByPeriod, false, false);
        configureBarChart(chartServiceUsage, true, false);
        configureBarChart(chartServiceRevenue, true, true);
        configureBarChart(chartRoomUsageByType, false, false);
        configureBarChart(chartInventoryTopUsage, true, false);
        configureBarChart(chartInventoryLowStock, true, false);

        configurePieChart(pieOverviewBookingStatus);
        configurePieChart(pieRevenueSource);
        configurePieChart(pieBookingStatus);
        configurePieChart(pieRoomStatus);
    }

    private void configureBarChart(BarChart<String, Number> chart, boolean rotateXLabels, boolean moneyAxis) {
        if (chart == null) {
            return;
        }
        chart.setLegendVisible(false);
        chart.setCategoryGap(18);
        chart.setBarGap(3);
        chart.getXAxis().setTickLabelRotation(rotateXLabels ? -45 : 0);
        chart.getXAxis().setTickLabelGap(8);
        if (chart.getYAxis() instanceof NumberAxis numberAxis) {
            numberAxis.setForceZeroInRange(true);
            if (moneyAxis) {
                numberAxis.setTickLabelFormatter(new StringConverter<>() {
                    @Override
                    public String toString(Number value) {
                        double number = value == null ? 0 : value.doubleValue();
                        if (Math.abs(number) >= 1_000_000) {
                            return moneyFormat.format(number / 1_000_000) + "tr";
                        }
                        if (Math.abs(number) >= 1_000) {
                            return moneyFormat.format(number / 1_000) + "k";
                        }
                        return moneyFormat.format(number);
                    }

                    @Override
                    public Number fromString(String value) {
                        return 0;
                    }
                });
            }
        }
    }

    private void configurePieChart(PieChart chart) {
        if (chart == null) {
            return;
        }
        chart.setLabelsVisible(false);
        chart.setLegendVisible(true);
        chart.setLegendSide(Side.BOTTOM);
        chart.setLabelLineLength(0);
        chart.setStartAngle(90);
        chart.setClockwise(true);
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
            if (selected == tabOverview) {
                loadOverviewReport();
            } else if (selected == tabRevenue) {
                loadRevenueReport();
            } else if (selected == tabBooking) {
                loadBookingReport();
            } else if (selected == tabService) {
                loadServiceReport();
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

    private void loadOverviewReport() {
        List<BookingReport> bookingReports = getBookingReportsOrDemo();
        List<RevenueReport> revenueReports = getRevenueReportsOrDemo();
        RoomUsageReport roomUsage = firstRoomUsage(getRoomUsageReportsOrDemo());
        BookingStatusTotals statusTotals = calculateBookingStatusTotals(bookingReports);

        int todayBooking = bookingReports.isEmpty()
            ? 0
            : bookingReports.get(bookingReports.size() - 1).getBookingCount();
        double revenue = sumRevenue(revenueReports);
        double paid = sumPaid(revenueReports);
        double remaining = sumRemaining(revenueReports);

        lblOverviewTodayBooking.setText(String.valueOf(todayBooking));
        lblOverviewCheckedIn.setText(String.valueOf(statusTotals.checkedIn));
        lblOverviewUpcomingCheckIn.setText(String.valueOf(statusTotals.confirmed));
        lblOverviewUpcomingCheckOut.setText(String.valueOf(statusTotals.checkedIn == 0 ? 0 : Math.max(1, statusTotals.checkedIn / 2)));
        lblOverviewRevenue.setText(formatMoney(revenue));
        lblOverviewPaid.setText(formatMoney(paid));
        lblOverviewRemaining.setText(formatMoney(remaining));
        lblOverviewRoomUsage.setText(formatPercent(roomUsage.getUsageRate()));

        updateBookingBarChart(chartOverviewBookingByDay, bookingReports, COLOR_BOOKING);
        updateBookingStatusPie(pieOverviewBookingStatus, statusTotals);

        setSummary("Booking hôm nay", String.valueOf(todayBooking), "Trong khoảng lọc",
            "Đang lưu trú", String.valueOf(statusTotals.checkedIn), "CHECKED_IN",
            "Doanh thu", formatMoney(revenue), "Tổng doanh thu",
            "Công suất", formatPercent(roomUsage.getUsageRate()), "Hiện tại");
    }

    private void loadRevenueReport() {
        List<RevenueReport> reports = getRevenueReportsOrDemo();
        tableRevenue.setItems(FXCollections.observableArrayList(reports));
        updateRevenueCharts(reports);

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

    private void loadBookingReport() {
        List<BookingReport> reports = getBookingReportsOrDemo();
        tableBooking.setItems(FXCollections.observableArrayList(reports));
        BookingStatusTotals statusTotals = calculateBookingStatusTotals(reports);
        updateBookingBarChart(chartBookingByPeriod, reports, COLOR_BOOKING);
        updateBookingStatusPie(pieBookingStatus, statusTotals);

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

    private void loadServiceReport() {
        List<ServiceReport> reports = loadDemoServiceReport();
        tableService.setItems(FXCollections.observableArrayList(reports));
        updateServiceCharts(reports);

        int usageCount = 0;
        double revenue = 0;
        String topService = reports.isEmpty() ? "Chưa có dữ liệu" : reports.get(0).getServiceName();
        for (ServiceReport report : reports) {
            usageCount += report.getUsageCount();
            revenue += report.getRevenue();
        }
        setSummary("Lượt dịch vụ", String.valueOf(usageCount), "Top dịch vụ",
            "Doanh thu dịch vụ", formatMoney(revenue), "Theo dịch vụ",
            "Dịch vụ nổi bật", topService, "Số lượt cao nhất",
            "Số dịch vụ", String.valueOf(reports.size()), "Đang hiển thị");
    }

    private void loadRoomUsageReport() {
        List<RoomUsageReport> reports = getRoomUsageReportsOrDemo();
        tableRoomUsage.setItems(FXCollections.observableArrayList(reports));
        List<RoomTypeReport> roomTypeReports = loadDemoRoomTypeReport();
        tableRoomType.setItems(FXCollections.observableArrayList(roomTypeReports));
        updateRoomCharts(roomTypeReports);

        RoomUsageReport report = reports.isEmpty() ? new RoomUsageReport() : reports.get(0);
        setSummary("Tổng phòng", String.valueOf(report.getTotalRoom()), "Từ bảng room",
            "Đang sử dụng", String.valueOf(report.getInUseRoom()), "status IN_USE",
            "Còn trống", String.valueOf(report.getAvailableRoom()), "status AVAILABLE",
            "Công suất", formatPercent(report.getUsageRate()), "Hiện tại");
    }

    private void loadInventoryReport() {
        List<InventoryReport> reports = getInventoryReportsOrDemo();
        tableInventory.setItems(FXCollections.observableArrayList(reports));
        List<InventoryItemReport> itemReports = loadDemoInventoryItemReport();
        tableInventoryLowStock.setItems(FXCollections.observableArrayList(itemReports));
        updateInventoryCharts(itemReports);

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

    private List<RevenueReport> getRevenueReportsOrDemo() {
        try {
            List<RevenueReport> reports = reportBUS.getRevenueReport(currentType(), fromDate(), toDate());
            return reports.isEmpty() ? loadDemoRevenueReport() : reports;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            return loadDemoRevenueReport();
        }
    }

    private List<BookingReport> getBookingReportsOrDemo() {
        try {
            List<BookingReport> reports = reportBUS.getBookingReport(currentType(), fromDate(), toDate());
            return reports.isEmpty() ? loadDemoBookingReport() : reports;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            return loadDemoBookingReport();
        }
    }

    private List<RoomUsageReport> getRoomUsageReportsOrDemo() {
        try {
            List<RoomUsageReport> reports = reportBUS.getRoomUsageReport(fromDate(), toDate());
            return reports.isEmpty() ? loadDemoRoomReport() : reports;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            return loadDemoRoomReport();
        }
    }

    private List<InventoryReport> getInventoryReportsOrDemo() {
        try {
            List<InventoryReport> reports = reportBUS.getInventoryReport();
            return reports.isEmpty() ? loadDemoInventoryReport() : reports;
        } catch (Exception ex) {
            return loadDemoInventoryReport();
        }
    }

    private List<RevenueReport> loadDemoRevenueReport() {
        List<String> periods = demoTimePeriods(7);
        int[] invoices = {8, 11, 9, 14, 12, 16, 18};
        double[] totals = {5200000, 7350000, 6100000, 9800000, 8750000, 11200000, 12650000};
        double[] paid = {4700000, 6900000, 5900000, 9050000, 8200000, 10400000, 11800000};
        List<RevenueReport> reports = new ArrayList<>();
        for (int i = 0; i < periods.size(); i++) {
            reports.add(new RevenueReport(periods.get(i), invoices[i], totals[i], paid[i], totals[i] - paid[i]));
        }
        return reports;
    }

    private List<BookingReport> loadDemoBookingReport() {
        List<String> periods = demoDayPeriods(7);
        int[] pending = {2, 3, 1, 3, 4, 2, 3};
        int[] confirmed = {3, 4, 3, 5, 4, 5, 6};
        int[] checkedIn = {4, 5, 5, 6, 7, 6, 8};
        int[] checkedOut = {2, 3, 2, 4, 3, 4, 5};
        int[] cancelled = {0, 1, 0, 1, 1, 0, 1};
        List<BookingReport> reports = new ArrayList<>();
        for (int i = 0; i < periods.size(); i++) {
            BookingReport report = new BookingReport();
            int total = pending[i] + confirmed[i] + checkedIn[i] + checkedOut[i] + cancelled[i];
            report.setPeriod(periods.get(i));
            report.setBookingCount(total);
            report.setPendingBookingCount(pending[i]);
            report.setConfirmedBookingCount(confirmed[i]);
            report.setCheckedInBookingCount(checkedIn[i]);
            report.setCheckedOutBookingCount(checkedOut[i]);
            report.setCancelledBookingCount(cancelled[i]);
            report.setNewBookingCount(pending[i] + confirmed[i]);
            report.setCompletedBookingCount(checkedOut[i]);
            reports.add(report);
        }
        return reports;
    }

    private List<ServiceReport> loadDemoServiceReport() {
        return List.of(
            new ServiceReport("Tắm thú cưng", 48, 7200000, 32.0),
            new ServiceReport("Grooming full combo", 31, 9300000, 20.7),
            new ServiceReport("Cắt tỉa lông", 27, 5400000, 18.0),
            new ServiceReport("Cắt móng", 24, 1800000, 16.0),
            new ServiceReport("Vệ sinh tai", 20, 1600000, 13.3)
        );
    }

    private List<RoomUsageReport> loadDemoRoomReport() {
        RoomUsageReport report = new RoomUsageReport();
        report.setPeriod("Hiện tại");
        report.setTotalRoom(42);
        report.setInUseRoom(29);
        report.setAvailableRoom(10);
        report.setUsageRate(69.0);
        return List.of(report);
    }

    private List<RoomTypeReport> loadDemoRoomTypeReport() {
        return List.of(
            new RoomTypeReport("Standard", 16, 10, 5, 1, 0),
            new RoomTypeReport("Deluxe", 12, 9, 2, 1, 0),
            new RoomTypeReport("Premium", 8, 6, 1, 1, 0),
            new RoomTypeReport("VIP", 6, 4, 2, 0, 0)
        );
    }

    private List<InventoryReport> loadDemoInventoryReport() {
        InventoryReport report = new InventoryReport();
        report.setScope("CN01 - Chi nhánh trung tâm");
        report.setTotalSku(86);
        report.setTotalStock(1240);
        report.setLowStockCount(9);
        report.setOutOfStockCount(2);
        return List.of(report);
    }

    private List<InventoryItemReport> loadDemoInventoryItemReport() {
        return List.of(
            new InventoryItemReport("Sữa tắm dịu nhẹ", 6, 12, "chai", "Cần nhập thêm", 38),
            new InventoryItemReport("Khăn lau thú cưng", 18, 25, "cái", "Sắp hết", 54),
            new InventoryItemReport("Thức ăn hạt mini", 0, 10, "kg", "Hết hàng", 42),
            new InventoryItemReport("Dung dịch vệ sinh tai", 5, 8, "chai", "Sắp hết", 28),
            new InventoryItemReport("Găng tay chăm sóc", 32, 20, "đôi", "Đủ hàng", 47)
        );
    }

    private List<String> demoTimePeriods(int count) {
        List<String> periods = new ArrayList<>();
        LocalDate end = dpToDate.getValue() == null ? LocalDate.now() : dpToDate.getValue();
        String type = currentType();
        for (int i = count - 1; i >= 0; i--) {
            if (type.contains("tháng")) {
                periods.add(end.minusMonths(i).format(DateTimeFormatter.ofPattern("MM/yyyy")));
            } else if (type.contains("tuần")) {
                periods.add("Tuần " + end.minusWeeks(i).format(chartDateFormat));
            } else {
                periods.add(end.minusDays(i).format(chartDateFormat));
            }
        }
        return periods;
    }

    private List<String> demoDayPeriods(int count) {
        List<String> periods = new ArrayList<>();
        LocalDate end = dpToDate.getValue() == null ? LocalDate.now() : dpToDate.getValue();
        for (int i = count - 1; i >= 0; i--) {
            periods.add(end.minusDays(i).format(chartDateFormat));
        }
        return periods;
    }

    private void updateRevenueCharts(List<RevenueReport> reports) {
        Map<String, Number> revenueByPeriod = new LinkedHashMap<>();
        for (RevenueReport report : reports) {
            revenueByPeriod.put(report.getPeriod(), report.getTotalRevenue());
        }
        populateBarChart(chartRevenueByPeriod, "Doanh thu", revenueByPeriod, List.of(COLOR_REVENUE));

        double total = Math.max(1, sumRevenue(reports));
        Map<String, Number> sourceShare = new LinkedHashMap<>();
        sourceShare.put("Tiền phòng", total * 0.62);
        sourceShare.put("Dịch vụ", total * 0.28);
        sourceShare.put("Phụ phí", total * 0.10);
        populatePieChart(pieRevenueSource, sourceShare, REVENUE_SOURCE_COLORS);
    }

    private void updateBookingBarChart(BarChart<String, Number> chart, List<BookingReport> reports, String color) {
        Map<String, Number> bookingByPeriod = new LinkedHashMap<>();
        for (BookingReport report : reports) {
            bookingByPeriod.put(report.getPeriod(), report.getBookingCount());
        }
        populateBarChart(chart, "Booking", bookingByPeriod, List.of(color));
    }

    private void updateBookingStatusPie(PieChart chart, BookingStatusTotals totals) {
        Map<String, Number> values = new LinkedHashMap<>();
        values.put("Pending", totals.pending);
        values.put("Confirmed", totals.confirmed);
        values.put("Checked-in", totals.checkedIn);
        values.put("Checked-out", totals.checkedOut);
        values.put("Cancelled", totals.cancelled);
        populatePieChart(chart, values, BOOKING_STATUS_COLORS);
    }

    private void updateServiceCharts(List<ServiceReport> reports) {
        Map<String, Number> usageByService = new LinkedHashMap<>();
        Map<String, Number> revenueByService = new LinkedHashMap<>();
        for (ServiceReport report : reports) {
            usageByService.put(report.getServiceName(), report.getUsageCount());
            revenueByService.put(report.getServiceName(), report.getRevenue());
        }
        populateBarChart(chartServiceUsage, "Số lượt sử dụng", usageByService, SERVICE_COLORS);
        populateBarChart(chartServiceRevenue, "Doanh thu", revenueByService, SERVICE_COLORS);
    }

    private void updateRoomCharts(List<RoomTypeReport> reports) {
        Map<String, Number> usageByType = new LinkedHashMap<>();
        int inUse = 0;
        int available = 0;
        int maintenance = 0;
        int inactive = 0;
        for (RoomTypeReport report : reports) {
            usageByType.put(report.getRoomType(), report.getUsageRate());
            inUse += report.getInUseRoom();
            available += report.getAvailableRoom();
            maintenance += report.getMaintenanceRoom();
            inactive += report.getInactiveRoom();
        }
        populateBarChart(chartRoomUsageByType, "Công suất", usageByType, List.of(COLOR_GOOD));

        Map<String, Number> statusShare = new LinkedHashMap<>();
        statusShare.put("Đang sử dụng", inUse);
        statusShare.put("Còn trống", available);
        statusShare.put("Bảo trì", maintenance);
        statusShare.put("Không hoạt động", inactive);
        populatePieChart(pieRoomStatus, statusShare, ROOM_STATUS_COLORS);
    }

    private void updateInventoryCharts(List<InventoryItemReport> reports) {
        Map<String, Number> usageByProduct = new LinkedHashMap<>();
        Map<String, Number> lowStockByProduct = new LinkedHashMap<>();
        for (InventoryItemReport report : reports) {
            usageByProduct.put(report.getProductName(), report.getUsageCount());
            lowStockByProduct.put(report.getProductName(), report.getCurrentStock());
        }
        populateBarChart(chartInventoryTopUsage, "Số lượt sử dụng", usageByProduct, List.of(COLOR_BOOKING));
        populateBarChart(chartInventoryLowStock, "Tồn hiện tại", lowStockByProduct, List.of(COLOR_ATTENTION));
    }

    private void populateBarChart(
            BarChart<String, Number> chart,
            String seriesName,
            Map<String, Number> values,
            List<String> colors) {
        if (chart == null) {
            return;
        }

        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(seriesName);
        values.forEach((label, value) -> series.getData().add(new XYChart.Data<>(label, value)));
        chart.getData().setAll(series);
        Platform.runLater(() -> styleBarChart(chart, colors));
    }

    private void populatePieChart(PieChart chart, Map<String, Number> values, List<String> colors) {
        if (chart == null) {
            return;
        }

        configurePieChart(chart);
        chart.setLegendVisible(true);
        chart.getData().clear();
        double total = values.values().stream().mapToDouble(Number::doubleValue).sum();
        if (total <= 0) {
            return;
        }

        List<PieChart.Data> pieData = new ArrayList<>();
        values.forEach((label, value) -> {
            double numericValue = value.doubleValue();
            if (numericValue > 0) {
                double percent = numericValue * 100.0 / total;
                pieData.add(new PieChart.Data(label + " " + String.format("%.0f%%", percent), numericValue));
            }
        });
        chart.setData(FXCollections.observableArrayList(pieData));
        Platform.runLater(() -> stylePieChart(chart, colors));
    }

    private void styleBarChart(BarChart<String, Number> chart, List<String> colors) {
        boolean rotateLabels = chart.getData().stream()
            .flatMap(series -> series.getData().stream())
            .anyMatch(data -> data.getXValue() != null && data.getXValue().length() > 10);
        if (rotateLabels) {
            chart.getXAxis().setTickLabelRotation(-45);
        }
        int index = 0;
        for (XYChart.Series<String, Number> series : chart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();
                if (node != null) {
                    node.setStyle("-fx-bar-fill: " + colors.get(index % colors.size()) + ";");
                }
                index++;
            }
        }
    }

    private void stylePieChart(PieChart chart, List<String> colors) {
        for (int i = 0; i < chart.getData().size(); i++) {
            Node node = chart.getData().get(i).getNode();
            if (node != null) {
                node.setStyle("-fx-pie-color: " + colors.get(i % colors.size()) + ";");
            }
        }
        for (Node legendSymbol : chart.lookupAll(".chart-legend-item-symbol")) {
            int index = defaultColorIndex(legendSymbol);
            if (index >= 0) {
                legendSymbol.setStyle("-fx-background-color: " + colors.get(index % colors.size()) + ";");
            }
        }
    }

    private int defaultColorIndex(Node node) {
        for (String styleClass : node.getStyleClass()) {
            if (styleClass.startsWith("default-color")) {
                try {
                    return Integer.parseInt(styleClass.substring("default-color".length()));
                } catch (NumberFormatException ignored) {
                    return -1;
                }
            }
        }
        return -1;
    }

    private BookingStatusTotals calculateBookingStatusTotals(List<BookingReport> reports) {
        BookingStatusTotals totals = new BookingStatusTotals();
        for (BookingReport report : reports) {
            totals.pending += report.getPendingBookingCount();
            totals.confirmed += report.getConfirmedBookingCount();
            totals.checkedIn += report.getCheckedInBookingCount();
            totals.checkedOut += report.getCheckedOutBookingCount();
            totals.cancelled += report.getCancelledBookingCount();
        }
        if (totals.pending + totals.confirmed + totals.checkedIn + totals.checkedOut + totals.cancelled == 0) {
            for (BookingReport report : reports) {
                totals.pending += report.getNewBookingCount();
                totals.checkedOut += report.getCompletedBookingCount();
                totals.cancelled += report.getCancelledBookingCount();
            }
        }
        return totals;
    }

    private RoomUsageReport firstRoomUsage(List<RoomUsageReport> reports) {
        return reports.isEmpty() ? new RoomUsageReport() : reports.get(0);
    }

    private double sumRevenue(List<RevenueReport> reports) {
        double revenue = 0;
        for (RevenueReport report : reports) {
            revenue += report.getTotalRevenue();
        }
        return revenue;
    }

    private double sumPaid(List<RevenueReport> reports) {
        double paid = 0;
        for (RevenueReport report : reports) {
            paid += report.getTotalPaid();
        }
        return paid;
    }

    private double sumRemaining(List<RevenueReport> reports) {
        double remaining = 0;
        for (RevenueReport report : reports) {
            remaining += report.getRemaining();
        }
        return remaining;
    }

    private static class BookingStatusTotals {
        private int pending;
        private int confirmed;
        private int checkedIn;
        private int checkedOut;
        private int cancelled;
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
        if (selected == tabOverview) {
            content.append("Chỉ số\tGiá trị\n")
                .append("Tổng booking hôm nay\t").append(lblOverviewTodayBooking.getText()).append('\n')
                .append("Booking đang lưu trú\t").append(lblOverviewCheckedIn.getText()).append('\n')
                .append("Booking sắp check-in\t").append(lblOverviewUpcomingCheckIn.getText()).append('\n')
                .append("Booking sắp check-out\t").append(lblOverviewUpcomingCheckOut.getText()).append('\n')
                .append("Tổng doanh thu\t").append(lblOverviewRevenue.getText()).append('\n')
                .append("Đã thanh toán\t").append(lblOverviewPaid.getText()).append('\n')
                .append("Còn phải thu\t").append(lblOverviewRemaining.getText()).append('\n')
                .append("Công suất phòng\t").append(lblOverviewRoomUsage.getText()).append('\n');
        } else if (selected == tabRevenue) {
            content.append("Thời gian\tSố hóa đơn\tTổng doanh thu\tĐã thanh toán\tCòn lại\n");
            for (RevenueReport row : tableRevenue.getItems()) {
                content.append(row.getPeriod()).append('\t')
                    .append(row.getInvoiceCount()).append('\t')
                    .append(row.getTotalRevenue()).append('\t')
                    .append(row.getTotalPaid()).append('\t')
                    .append(row.getRemaining()).append('\n');
            }
        } else if (selected == tabBooking) {
            content.append("Thời gian\tSố booking\tBooking mới\tHoàn thành\tĐã hủy\tPending\tConfirmed\tChecked-in\tChecked-out\n");
            for (BookingReport row : tableBooking.getItems()) {
                content.append(row.getPeriod()).append('\t')
                    .append(row.getBookingCount()).append('\t')
                    .append(row.getNewBookingCount()).append('\t')
                    .append(row.getCompletedBookingCount()).append('\t')
                    .append(row.getCancelledBookingCount()).append('\t')
                    .append(row.getPendingBookingCount()).append('\t')
                    .append(row.getConfirmedBookingCount()).append('\t')
                    .append(row.getCheckedInBookingCount()).append('\t')
                    .append(row.getCheckedOutBookingCount()).append('\n');
            }
        } else if (selected == tabService) {
            content.append("Dịch vụ\tSố lượt sử dụng\tDoanh thu\tTỷ lệ sử dụng\n");
            for (ServiceReport row : tableService.getItems()) {
                content.append(row.getServiceName()).append('\t')
                    .append(row.getUsageCount()).append('\t')
                    .append(row.getRevenue()).append('\t')
                    .append(formatPercent(row.getUsageRate())).append('\n');
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
            content.append("\nLoại phòng\tTổng phòng\tĐang sử dụng\tCòn trống\tBảo trì\tTỷ lệ sử dụng\n");
            for (RoomTypeReport row : tableRoomType.getItems()) {
                content.append(row.getRoomType()).append('\t')
                    .append(row.getTotalRoom()).append('\t')
                    .append(row.getInUseRoom()).append('\t')
                    .append(row.getAvailableRoom()).append('\t')
                    .append(row.getMaintenanceRoom()).append('\t')
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
            content.append("\nSản phẩm\tTồn hiện tại\tMức tối thiểu\tĐơn vị\tTrạng thái\n");
            for (InventoryItemReport row : tableInventoryLowStock.getItems()) {
                content.append(row.getProductName()).append('\t')
                    .append(row.getCurrentStock()).append('\t')
                    .append(row.getMinimumStock()).append('\t')
                    .append(row.getUnit()).append('\t')
                    .append(row.getStatus()).append('\n');
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
        if (tab == tabOverview) return "overview";
        if (tab == tabBooking) return "booking";
        if (tab == tabService) return "service";
        if (tab == tabRoomUsage) return "room_usage";
        if (tab == tabInventory) return "inventory";
        if (tab == tabChain) return "chain";
        return "revenue";
    }

    private String reportTitle(Tab tab) {
        if (tab == tabOverview) return "BÁO CÁO TỔNG QUAN";
        if (tab == tabBooking) return "BÁO CÁO THỐNG KÊ BOOKING";
        if (tab == tabService) return "BÁO CÁO THỐNG KÊ DỊCH VỤ";
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

    private String formatQuantity(double value) {
        return quantityFormat.format(value);
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
