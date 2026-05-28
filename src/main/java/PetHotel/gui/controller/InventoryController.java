package PetHotel.gui.controller;

import PetHotel.bus.InventoryBUS;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.model.CategoryProduct;
import PetHotel.model.GoodsReceipt;
import PetHotel.model.InventoryItem;
import PetHotel.model.InventoryStats;
import PetHotel.util.Role;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InventoryController {
    @FXML private Label statTotalSKU;
    @FXML private Label statOkStock;
    @FXML private Label statLowStock;
    @FXML private Label statCriticalStock;
    @FXML private Label statMonthImport;

    @FXML private Button btnAddImportTop;
    @FXML private Button btnAdjustStockTop;
    @FXML private Button btnRecordWasteTop;
    @FXML private VBox lowStockAlertPanel;
    @FXML private HBox lowStockCards;

    @FXML private ToggleButton tabStock;
    @FXML private ToggleButton tabImport;
    @FXML private VBox stockPanel;
    @FXML private VBox importPanel;

    @FXML private TextField searchStock;
    @FXML private ComboBox<String> filterStockCat;
    @FXML private ComboBox<String> filterStockStatus;

    @FXML private TableView<InventoryItem> stockTable;
    @FXML private TableColumn<InventoryItem, String> colSkuId;
    @FXML private TableColumn<InventoryItem, String> colSkuName;
    @FXML private TableColumn<InventoryItem, String> colSkuCat;
    @FXML private TableColumn<InventoryItem, String> colSkuUnit;
    @FXML private TableColumn<InventoryItem, String> colSkuQty;
    @FXML private TableColumn<InventoryItem, String> colSkuMin;
    @FXML private TableColumn<InventoryItem, String> colSkuStatus;
    @FXML private TableColumn<InventoryItem, Void> colSkuAction;

    @FXML private TextField searchImport;
    @FXML private DatePicker importDateFrom;
    @FXML private DatePicker importDateTo;

    @FXML private Button btnEditImport;
    @FXML private Button btnDeleteImport;

    @FXML private TableView<GoodsReceipt> importTable;
    @FXML private TableColumn<GoodsReceipt, String> colImportId;
    @FXML private TableColumn<GoodsReceipt, String> colImportDate;
    @FXML private TableColumn<GoodsReceipt, String> colImportProduct;
    @FXML private TableColumn<GoodsReceipt, String> colImportQty;
    @FXML private TableColumn<GoodsReceipt, String> colImportUnit;
    @FXML private TableColumn<GoodsReceipt, String> colImportPrice;
    @FXML private TableColumn<GoodsReceipt, String> colImportTotal;
    @FXML private TableColumn<GoodsReceipt, String> colImportSupplier;
    @FXML private TableColumn<GoodsReceipt, String> colImportNote;

    @FXML private Pagination importPagination;
    @FXML private Label importPageInfo;

    private final InventoryBUS inventoryBUS = new InventoryBUS();
    private final Map<String, String> categoryNameToId = new HashMap<>();

    private AppUser currentUser;
    private String branchId;
    private GoodsReceipt selectedImport;
    private boolean canManageStock;
    private boolean canRecordWaste;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        branchId = resolveBranchId();

        if (currentUser == null) {
            showError("Chưa đăng nhập. Không thể mở kho vật tư.");
            return;
        }

        canManageStock = currentUser.getRole() == Role.BRANCH_MANAGER || currentUser.getRole() == Role.ADMIN;
        canRecordWaste = currentUser.getRole() == Role.PET_CARE_STAFF || canManageStock;

        setupRoleUi();
        setupFilters();
        setupStockTableColumns();
        setupImportTableColumns();
        setupImportSelectionListener();

        if (importDateFrom != null) {
            importDateFrom.setValue(LocalDate.now().withDayOfMonth(1));
        }
        if (importDateTo != null) {
            importDateTo.setValue(LocalDate.now());
        }

        updateTabState(true);
        refreshAll();
    }

    private void setupRoleUi() {
        showManaged(btnAddImportTop, canManageStock);
        showManaged(btnAdjustStockTop, canManageStock);
        showManaged(btnRecordWasteTop, canRecordWaste);
        showManaged(tabImport, canManageStock);
        showManaged(lowStockAlertPanel, canManageStock);
    }

    private void setupFilters() {
        try {
            categoryNameToId.clear();
            filterStockCat.getItems().clear();
            filterStockCat.getItems().add("Tất cả");

            List<CategoryProduct> categories = inventoryBUS.getCategories(currentUser);
            for (CategoryProduct category : categories) {
                filterStockCat.getItems().add(category.getCategoryName());
                categoryNameToId.put(category.getCategoryName(), category.getProductCategoryId());
            }
            filterStockCat.setValue("Tất cả");
            filterStockStatus.setValue("Tất cả");
        } catch (Exception e) {
            showError("Không thể tải bộ lọc kho vật tư: " + e.getMessage());
        }
    }

    private void setupStockTableColumns() {
        colSkuId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getProductId()));
        colSkuName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getProductName()));
        colSkuCat.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCategoryName()));
        colSkuUnit.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUnit()));
        colSkuQty.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getQuantityText()));
        colSkuMin.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getReorderPointText()));
        colSkuStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus()));

        colSkuStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label badge = new Label(value);
                badge.getStyleClass().add("status-badge");
                switch (value) {
                    case InventoryItem.STATUS_OK -> badge.getStyleClass().add("status-instock");
                    case InventoryItem.STATUS_LOW -> badge.getStyleClass().add("status-low");
                    case InventoryItem.STATUS_CRITICAL -> badge.getStyleClass().add("status-critical");
                    case InventoryItem.STATUS_OUT -> badge.getStyleClass().add("status-outstock");
                    default -> badge.getStyleClass().add("status-pending");
                }
                setGraphic(badge);
                setText(null);
            }
        });

        colSkuAction.setCellFactory(col -> new TableCell<>() {
            private final Button actionButton = new Button();

            {
                actionButton.getStyleClass().addAll("action-btn", "action-btn-outline");
                actionButton.setOnAction(event -> {
                    InventoryItem item = getTableView().getItems().get(getIndex());
                    if (canManageStock) {
                        openAdjustmentForm(item);
                    } else {
                        openWasteForm(item);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }
                InventoryItem inventoryItem = getTableView().getItems().get(getIndex());
                if (canManageStock) {
                    actionButton.setText("Điều chỉnh");
                    actionButton.setDisable(false);
                    setGraphic(actionButton);
                } else if (canRecordWaste) {
                    actionButton.setText("Tiêu hao");
                    actionButton.setDisable(InventoryItem.STATUS_OUT.equals(inventoryItem.getStatus()));
                    setGraphic(actionButton);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void setupImportTableColumns() {
        colImportId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getGoodsReceiptId()));
        colImportDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getReceiptDateText()));
        colImportProduct.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getProductSummary()));
        colImportQty.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getQuantitySummary()));
        colImportUnit.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUnitSummary()));
        colImportPrice.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUnitPriceText()));
        colImportTotal.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTotalText()));
        colImportSupplier.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getSupplierName())));
        colImportNote.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getNote())));
    }

    private void setupImportSelectionListener() {
        importTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedImport = newVal;
            boolean has = newVal != null && canManageStock && !"CANCELLED".equals(newVal.getStatus());
            btnEditImport.setDisable(!has);
            btnDeleteImport.setDisable(!has);
        });
    }

    private void refreshAll() {
        loadStats();
        if (canManageStock) {
            loadLowStockAlerts();
        }
        loadStockData();
        if (canManageStock && importPanel.isVisible()) {
            loadImportData();
        }
    }

    private void loadStats() {
        try {
            InventoryStats stats = inventoryBUS.getStats(branchId, currentUser);
            statTotalSKU.setText(String.valueOf(stats.getTotalSku()));
            statOkStock.setText(String.valueOf(stats.getOkStock()));
            statLowStock.setText(String.valueOf(stats.getLowStock()));
            statCriticalStock.setText(String.valueOf(stats.getCriticalStock()));
            statMonthImport.setText(String.valueOf(stats.getMonthImport()));
        } catch (Exception e) {
            showError("Không thể tải thống kê tồn kho: " + e.getMessage());
        }
    }

    private void loadLowStockAlerts() {
        try {
            lowStockCards.getChildren().clear();
            List<InventoryItem> items = inventoryBUS.getLowStockItems(branchId, currentUser);
            if (items.isEmpty()) {
                Label empty = new Label("Không có vật tư dưới ngưỡng tối thiểu.");
                empty.getStyleClass().add("table-empty");
                lowStockCards.getChildren().add(empty);
                return;
            }

            int count = Math.min(items.size(), 8);
            for (int i = 0; i < count; i++) {
                lowStockCards.getChildren().add(buildLowStockCard(items.get(i)));
            }
        } catch (Exception e) {
            showError("Không thể tải cảnh báo tồn kho thấp: " + e.getMessage());
        }
    }

    private VBox buildLowStockCard(InventoryItem item) {
        VBox card = new VBox(4);
        card.setPrefWidth(175);
        card.getStyleClass().addAll("inv-card",
            InventoryItem.STATUS_CRITICAL.equals(item.getStatus()) || InventoryItem.STATUS_OUT.equals(item.getStatus())
                ? "inv-card-critical" : "inv-card-low");

        Label name = new Label(item.getProductName());
        name.setWrapText(true);
        name.getStyleClass().add("inv-product-name");

        HBox qtyLine = new HBox(4);
        Label qty = new Label(item.getQuantityText());
        qty.getStyleClass().addAll("inv-qty-big",
            InventoryItem.STATUS_LOW.equals(item.getStatus()) ? "inv-qty-low" : "inv-qty-critical");
        Label unit = new Label(item.getUnit() + " còn lại");
        unit.getStyleClass().add("inv-unit");
        qtyLine.getChildren().addAll(qty, unit);

        Label min = new Label("Tối thiểu: " + item.getReorderPointText() + " " + item.getUnit());
        min.getStyleClass().addAll("stock-qty",
            InventoryItem.STATUS_LOW.equals(item.getStatus()) ? "stock-qty-low" : "stock-qty-critical");

        Button importNow = new Button("Nhập thêm");
        importNow.getStyleClass().addAll("action-btn",
            InventoryItem.STATUS_LOW.equals(item.getStatus()) ? "action-btn-amber" : "action-btn-danger");
        importNow.setOnAction(e -> ImportFormController.openForProduct(item.getProductId(), this::refreshAll));

        card.getChildren().addAll(name, qtyLine, min, importNow);
        return card;
    }

    private void loadStockData() {
        try {
            String categoryName = filterStockCat.getValue();
            String categoryId = categoryNameToId.get(categoryName);
            String status = filterStockStatus.getValue();
            List<InventoryItem> items = inventoryBUS.searchInventory(
                branchId,
                searchStock.getText(),
                categoryId,
                status,
                currentUser
            );
            stockTable.setItems(FXCollections.observableArrayList(items));
        } catch (Exception e) {
            showError("Không thể tải danh sách tồn kho: " + e.getMessage());
        }
    }

    private void loadImportData() {
        if (!canManageStock) {
            return;
        }
        try {
            List<GoodsReceipt> receipts = inventoryBUS.searchReceipts(
                branchId,
                searchImport.getText(),
                importDateFrom.getValue(),
                importDateTo.getValue(),
                currentUser
            );
            importTable.setItems(FXCollections.observableArrayList(receipts));
            importPageInfo.setText("Hiển thị " + receipts.size() + " / " + receipts.size() + " phiếu nhập");
            importPagination.setPageCount(1);
        } catch (Exception e) {
            showError("Không thể tải lịch sử nhập hàng: " + e.getMessage());
        }
    }

    @FXML
    public void onTabStock(ActionEvent event) {
        updateTabState(true);
        stockPanel.setVisible(true);
        stockPanel.setManaged(true);
        importPanel.setVisible(false);
        importPanel.setManaged(false);
        loadStockData();
    }

    @FXML
    public void onTabImport(ActionEvent event) {
        if (!canManageStock) {
            onTabStock(event);
            return;
        }
        updateTabState(false);
        importPanel.setVisible(true);
        importPanel.setManaged(true);
        stockPanel.setVisible(false);
        stockPanel.setManaged(false);
        loadImportData();
    }

    @FXML
    public void onSearchStock() {
        loadStockData();
    }

    @FXML
    public void onFilterStock(ActionEvent event) {
        onSearchStock();
    }

    @FXML
    public void onSearchImport() {
        loadImportData();
    }

    @FXML
    public void onFilterImport(ActionEvent event) {
        onSearchImport();
    }

    @FXML
    public void onImportTableClick(MouseEvent event) {
        if (event.getClickCount() == 2 && selectedImport != null && canManageStock) {
            openImportForm(selectedImport);
        }
    }

    @FXML
    public void onAddImport(ActionEvent event) {
        openImportForm(null);
    }

    @FXML
    public void onEditImport(ActionEvent event) {
        if (selectedImport != null) {
            openImportForm(selectedImport);
        }
    }

    @FXML
    public void onDeleteImport(ActionEvent event) {
        if (selectedImport == null) return;
        Alert confirm = new Alert(
            Alert.AlertType.CONFIRMATION,
            "Hủy phiếu nhập " + selectedImport.getGoodsReceiptId()
                + "?\nTồn kho đã cộng từ phiếu này sẽ được đảo lại nếu còn đủ số lượng.",
            ButtonType.YES,
            ButtonType.NO
        );
        confirm.setTitle("Xác Nhận Hủy Phiếu");
        confirm.setHeaderText(null);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                inventoryBUS.cancelReceipt(selectedImport.getGoodsReceiptId(), branchId, currentUser);
                refreshAll();
            } catch (Exception e) {
                showError("Không thể hủy phiếu nhập: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onAdjustStock(ActionEvent event) {
        InventoryItem selected = stockTable.getSelectionModel().getSelectedItem();
        openAdjustmentForm(selected);
    }

    @FXML
    public void onRecordWaste(ActionEvent event) {
        InventoryItem selected = stockTable.getSelectionModel().getSelectedItem();
        openWasteForm(selected);
    }

    private void openImportForm(GoodsReceipt receipt) {
        if (!canManageStock) {
            showWarning("Bạn không có quyền nhập hàng.");
            return;
        }
        ImportFormController.open(receipt, this::refreshAll);
    }

    private void openAdjustmentForm(InventoryItem item) {
        if (!canManageStock) {
            showWarning("Bạn không có quyền điều chỉnh tồn kho.");
            return;
        }
        StockAdjustmentController.open(item, this::refreshAll);
    }

    private void openWasteForm(InventoryItem item) {
        if (!canRecordWaste) {
            showWarning("Bạn không có quyền ghi nhận tiêu hao vật tư.");
            return;
        }
        MaterialWasteController.open(item, null, this::refreshAll);
    }

    private void updateTabState(boolean stockActive) {
        tabStock.setSelected(stockActive);
        tabImport.setSelected(!stockActive);
        tabStock.getStyleClass().remove("period-btn-active");
        tabImport.getStyleClass().remove("period-btn-active");
        if (stockActive) {
            tabStock.getStyleClass().add("period-btn-active");
        } else {
            tabImport.getStyleClass().add("period-btn-active");
        }
    }

    private String resolveBranchId() {
        String sessionBranchId = SessionManager.getInstance().getBranchId();
        if (sessionBranchId != null && !sessionBranchId.isBlank()) {
            return sessionBranchId.trim();
        }
        AppUser user = SessionManager.getInstance().getCurrentUser();
        if (user != null && user.getEmployee() != null && user.getEmployee().getBranchId() != null) {
            return user.getEmployee().getBranchId().trim();
        }
        return null;
    }

    private void showManaged(Node node, boolean visible) {
        if (node != null) {
            node.setVisible(visible);
            node.setManaged(visible);
        }
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "—" : value;
    }

    private void showWarning(String message) {
        new Alert(Alert.AlertType.WARNING, message, ButtonType.OK).showAndWait();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }
}
