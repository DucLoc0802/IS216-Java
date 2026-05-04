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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

/**
 * InventoryController — Quản Lý Kho Hàng
 * ─────────────────────────────────────────────────────────────────
 * Xử lý: InventoryManagement.fxml
 * Use cases:
 *   - Xem tồn kho hiện tại (tab 1)
 *   - Xem / thêm / sửa lịch sử nhập hàng (tab 2)
 *   - Điều chỉnh tồn kho
 *   - Cảnh báo tồn kho thấp (tự động khi load)
 */
public class InventoryController {

    // ── Stats ────────────────────────────────────────────────────
    @FXML private Label statTotalSKU;
    @FXML private Label statOkStock;
    @FXML private Label statLowStock;
    @FXML private Label statCriticalStock;
    @FXML private Label statMonthImport;

    // ── Low stock alert area ──────────────────────────────────────
    @FXML private HBox lowStockCards;

    // ── Tab switcher ─────────────────────────────────────────────
    @FXML private ToggleButton tabStock;
    @FXML private ToggleButton tabImport;
    @FXML private VBox         stockPanel;
    @FXML private VBox         importPanel;

    // ── Stock tab ────────────────────────────────────────────────
    @FXML private TextField         searchStock;
    @FXML private ComboBox<String>  filterStockCat;
    @FXML private ComboBox<String>  filterStockStatus;

    @FXML private TableView<Object>          stockTable;
    @FXML private TableColumn<Object,String> colSkuId;
    @FXML private TableColumn<Object,String> colSkuName;
    @FXML private TableColumn<Object,String> colSkuCat;
    @FXML private TableColumn<Object,String> colSkuUnit;
    @FXML private TableColumn<Object,String> colSkuQty;
    @FXML private TableColumn<Object,String> colSkuMin;
    @FXML private TableColumn<Object,String> colSkuStatus;
    @FXML private TableColumn<Object,String> colSkuAction;

    // ── Import tab ───────────────────────────────────────────────
    @FXML private TextField  searchImport;
    @FXML private DatePicker importDateFrom;
    @FXML private DatePicker importDateTo;

    @FXML private Button btnEditImport;
    @FXML private Button btnDeleteImport;

    @FXML private TableView<Object>          importTable;
    @FXML private TableColumn<Object,String> colImportId;
    @FXML private TableColumn<Object,String> colImportDate;
    @FXML private TableColumn<Object,String> colImportProduct;
    @FXML private TableColumn<Object,String> colImportQty;
    @FXML private TableColumn<Object,String> colImportUnit;
    @FXML private TableColumn<Object,String> colImportPrice;
    @FXML private TableColumn<Object,String> colImportTotal;
    @FXML private TableColumn<Object,String> colImportSupplier;
    @FXML private TableColumn<Object,String> colImportNote;

    @FXML private Pagination importPagination;
    @FXML private Label      importPageInfo;

    // ── State ────────────────────────────────────────────────────
    private Object selectedImport = null;

    // ── Lifecycle ────────────────────────────────────────────────

    @FXML
    public void initialize() {
        setupStockTableColumns();
        setupImportTableColumns();
        setupImportSelectionListener();

        // Mặc định date range = tháng hiện tại
        importDateFrom.setValue(LocalDate.now().withDayOfMonth(1));
        importDateTo.setValue(LocalDate.now());

        loadStats();
        loadLowStockAlerts();
        loadStockData();
    }

    // ── Column setup ─────────────────────────────────────────────

    private void setupStockTableColumns() {
        colSkuStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) { setGraphic(null); return; }
                Label badge = new Label(value);
                badge.getStyleClass().add("status-badge");
                switch (value) {
                    case "Đủ hàng"     -> badge.getStyleClass().add("status-instock");
                    case "Tồn kho thấp"-> badge.getStyleClass().add("status-low");
                    case "Nguy hiểm"   -> badge.getStyleClass().add("status-critical");
                    case "Hết hàng"    -> badge.getStyleClass().add("status-outstock");
                    default            -> badge.getStyleClass().add("status-pending");
                }
                setGraphic(badge); setText(null);
            }
        });
    }

    private void setupImportTableColumns() {
        // TODO: cellValueFactory khi có ImportRecord model
    }

    private void setupImportSelectionListener() {
        importTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                selectedImport = newVal;
                boolean has = newVal != null;
                btnEditImport.setDisable(!has);
                btnDeleteImport.setDisable(!has);
            }
        );
    }

    // ── Data ─────────────────────────────────────────────────────

    private void loadStats() {
        // TODO: InventoryBUS.getStats()
        statTotalSKU.setText("0");
        statOkStock.setText("0");
        statLowStock.setText("0");
        statCriticalStock.setText("0");
        statMonthImport.setText("0");
    }

    private void loadLowStockAlerts() {
        // TODO: InventoryBUS.getLowStockItems() → tạo VBox card cho mỗi item
        // lowStockCards.getChildren().clear();
        // for (Product p : lowStockList) {
        //     lowStockCards.getChildren().add(buildLowStockCard(p));
        // }
    }

    private void loadStockData() {
        ObservableList<Object> data = FXCollections.observableArrayList();
        stockTable.setItems(data);
    }

    private void loadImportData() {
        ObservableList<Object> data = FXCollections.observableArrayList();
        importTable.setItems(data);
        importPageInfo.setText("Hiển thị 0 / 0 phiếu nhập");
    }

    // ── FXML Handlers ────────────────────────────────────────────

    /** Tab: Tồn Kho */
    @FXML public void onTabStock(ActionEvent event) {
        stockPanel.setVisible(true);  stockPanel.setManaged(true);
        importPanel.setVisible(false); importPanel.setManaged(false);
        loadStockData();
    }

    /** Tab: Lịch Sử Nhập Hàng */
    @FXML public void onTabImport(ActionEvent event) {
        importPanel.setVisible(true);  importPanel.setManaged(true);
        stockPanel.setVisible(false);  stockPanel.setManaged(false);
        loadImportData();
    }

    @FXML public void onSearchStock() {
        // TODO: InventoryBUS.searchStock(searchStock.getText(), filterStockCat, filterStockStatus)
        loadStockData();
    }

    @FXML public void onFilterStock(ActionEvent event) { onSearchStock(); }

    @FXML public void onSearchImport() {
        // TODO: InventoryBUS.searchImport(...)
        loadImportData();
    }

    @FXML public void onFilterImport(ActionEvent event) { onSearchImport(); }

    @FXML public void onImportTableClick(MouseEvent event) {
        if (event.getClickCount() == 2 && selectedImport != null) {
            openImportForm(selectedImport);
        }
    }

    @FXML public void onAddImport(ActionEvent event) {
        openImportForm(null);
    }

    @FXML public void onEditImport(ActionEvent event) {
        if (selectedImport == null) return;
        openImportForm(selectedImport);
    }

    @FXML public void onDeleteImport(ActionEvent event) {
        if (selectedImport == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Xóa phiếu nhập hàng này?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác Nhận Xóa"); confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                // TODO: InventoryBUS.deleteImport(selectedImport.getId())
                loadImportData();
                loadStats();
            }
        });
    }

    @FXML public void onAdjustStock(ActionEvent event) {
        // TODO: mở dialog điều chỉnh tồn kho
        System.out.println("Mở form điều chỉnh tồn kho...");
    }

    // ── Helpers ──────────────────────────────────────────────────

    private void openImportForm(Object importRecord) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/PetHotel/gui/view/ImportForm.fxml")
            );
            Parent root = loader.load();
            Stage dialog = new Stage();
            dialog.setTitle(importRecord == null ? "Tạo Phiếu Nhập Hàng" : "Sửa Phiếu Nhập Hàng");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
            loadImportData();
            loadStats();
            loadLowStockAlerts();
        } catch (IOException e) {
            System.err.println("Không mở được ImportForm.fxml: " + e.getMessage());
        }
    }
}
