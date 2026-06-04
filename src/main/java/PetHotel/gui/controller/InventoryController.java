package PetHotel.gui.controller;

import PetHotel.bus.InventoryBUS;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.model.BranchInventory;
import PetHotel.util.Role;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventoryController {
    @FXML private ComboBox<String> branchCombo;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> stockStatusCombo;
    @FXML private TableView<BranchInventory> inventoryTable;
    @FXML private TableColumn<BranchInventory, String> colProductId;
    @FXML private TableColumn<BranchInventory, String> colProductName;
    @FXML private TableColumn<BranchInventory, String> colCategoryName;
    @FXML private TableColumn<BranchInventory, String> colUnit;
    @FXML private TableColumn<BranchInventory, String> colQuantity;
    @FXML private TableColumn<BranchInventory, String> colReorderPoint;
    @FXML private TableColumn<BranchInventory, String> colStockStatus;
    @FXML private TableColumn<BranchInventory, String> colLastUpdated;
    @FXML private Label totalLabel;
    @FXML private VBox lowStockAlertPanel;
    @FXML private Label lowStockAlertIcon;
    @FXML private Label lowStockSummaryLabel;
    @FXML private Label lowStockDetailLabel;
    @FXML private HBox lowStockItemsBox;
    @FXML private Button btnShowLowStock;
    @FXML private Button btnShowOutStock;

    private final InventoryBUS inventoryBUS = new InventoryBUS();
    private AppUser currentUser;
    private boolean loadingFilters;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        setupTable();
        setupFilters();
        loadInventory();
    }

    private void setupFilters() {
        loadingFilters = true;
        try {
            stockStatusCombo.setItems(FXCollections.observableArrayList(
                BranchInventory.STATUS_ALL,
                BranchInventory.STATUS_IN_STOCK,
                BranchInventory.STATUS_LOW,
                BranchInventory.STATUS_OUT
            ));
            stockStatusCombo.setValue(BranchInventory.STATUS_ALL);

            List<String> branchIds = inventoryBUS.getBranchIds(currentUser);
            branchCombo.setItems(FXCollections.observableArrayList(branchIds));

            if (currentUser != null && currentUser.getRole() == Role.BRANCH_MANAGER) {
                branchCombo.setValue(resolveCurrentBranch());
                branchCombo.setDisable(true);
            } else if (!branchIds.isEmpty()) {
                branchCombo.setValue(branchIds.get(0));
            }
        } catch (Exception e) {
            showError("Không thể tải bộ lọc tồn kho: " + e.getMessage());
        } finally {
            loadingFilters = false;
        }
    }

    private void setupTable() {
        colProductId.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getProductId())));
        colProductName.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getProductName())));
        colCategoryName.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getCategoryName())));
        colUnit.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getUnit())));
        colQuantity.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getQuantityText()));
        colReorderPoint.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getReorderPointText()));
        colStockStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStockStatus()));
        colLastUpdated.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getLastUpdatedText()));
        setupReorderPointColumn();

        inventoryTable.setRowFactory(table -> new TableRow<>() {
            @Override
            protected void updateItem(BranchInventory item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("inventory-row-low", "inventory-row-out");
                if (!empty && item != null) {
                    if (isOutOfStock(item)) {
                        getStyleClass().add("inventory-row-out");
                    } else if (isLowStock(item)) {
                        getStyleClass().add("inventory-row-low");
                    }
                }
            }
        });

        colStockStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(status);
                badge.getStyleClass().add("status-badge");
                switch (status) {
                    case BranchInventory.STATUS_IN_STOCK -> badge.getStyleClass().add("status-instock");
                    case BranchInventory.STATUS_LOW -> badge.getStyleClass().add("status-low");
                    case BranchInventory.STATUS_OUT -> badge.getStyleClass().add("status-outstock");
                    default -> badge.getStyleClass().add("status-pending");
                }
                setGraphic(badge);
                setText(null);
            }
        });
    }

    private void setupReorderPointColumn() {
        colReorderPoint.setMinWidth(185);
        colReorderPoint.setPrefWidth(205);
        colReorderPoint.setCellFactory(column -> new TableCell<>() {
            private final Label valueLabel = new Label();
            private final Button editButton = new Button("Sửa");
            private final HBox content = new HBox(8, valueLabel, editButton);

            {
                content.setAlignment(Pos.CENTER_LEFT);
                editButton.setMinWidth(54);
                editButton.setPrefWidth(54);
                editButton.getStyleClass().addAll("action-btn", "action-btn-outline", "inventory-table-action");
                editButton.setOnAction(event -> {
                    BranchInventory item = getTableRow() == null ? null : getTableRow().getItem();
                    if (item != null) {
                        editReorderPoint(item);
                    }
                });
            }

            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                setText(null);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                valueLabel.setText(valueOrDash(value));
                setGraphic(content);
            }
        });
    }

    @FXML
    public void onSearch() {
        if (!loadingFilters) {
            loadInventory();
        }
    }

    @FXML
    public void onClearFilter() {
        searchField.clear();
        stockStatusCombo.setValue(BranchInventory.STATUS_ALL);
        if (currentUser != null && currentUser.getRole() != Role.BRANCH_MANAGER && !branchCombo.getItems().isEmpty()) {
            branchCombo.setValue(branchCombo.getItems().get(0));
        }
        loadInventory();
    }

    @FXML
    public void onShowLowStock() {
        stockStatusCombo.setValue(BranchInventory.STATUS_LOW);
        loadInventory();
    }

    @FXML
    public void onShowOutStock() {
        stockStatusCombo.setValue(BranchInventory.STATUS_OUT);
        loadInventory();
    }

    private void editReorderPoint(BranchInventory item) {
        TextInputDialog dialog = new TextInputDialog(
            item.getReorderPoint() == null ? "" : item.getReorderPointText()
        );
        dialog.setTitle("Sửa điểm đặt hàng lại");
        dialog.setHeaderText(valueOrDash(item.getProductName()));
        dialog.setContentText("Điểm đặt hàng lại mới:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        try {
            BigDecimal reorderPoint = parseOptionalQuantity(result.get());
            inventoryBUS.updateReorderPoint(item.getBranchId(), item.getProductId(), reorderPoint, currentUser);
            loadInventory();
            showInfo("Đã cập nhật điểm đặt hàng lại cho " + valueOrDash(item.getProductName()) + ".");
        } catch (ValidationException | SQLException e) {
            showError(e.getMessage());
        } catch (RuntimeException e) {
            showError("Không thể cập nhật điểm đặt hàng lại: " + e.getMessage());
        }
    }

    private void loadInventory() {
        try {
            List<BranchInventory> items = inventoryBUS.searchInventory(
                branchCombo.getValue(),
                searchField.getText(),
                stockStatusCombo.getValue(),
                currentUser
            );
            List<BranchInventory> alertSource = BranchInventory.STATUS_ALL.equals(stockStatusCombo.getValue())
                ? items
                : inventoryBUS.searchInventory(
                    branchCombo.getValue(),
                    searchField.getText(),
                    BranchInventory.STATUS_ALL,
                    currentUser
            );
            inventoryTable.setItems(FXCollections.observableArrayList(items));
            updateLowStockAlert(alertSource);
            totalLabel.setText(buildTotalText(items.size(), items));
        } catch (ValidationException | SQLException e) {
            inventoryTable.setItems(FXCollections.observableArrayList());
            totalLabel.setText("Hiển thị 0 dòng tồn kho");
            updateLowStockAlert(List.of());
            showError(e.getMessage());
        } catch (RuntimeException e) {
            inventoryTable.setItems(FXCollections.observableArrayList());
            totalLabel.setText("Hiển thị 0 dòng tồn kho");
            updateLowStockAlert(List.of());
            showError("Không thể tải tồn kho: " + e.getMessage());
        }
    }

    private void updateLowStockAlert(List<BranchInventory> sourceItems) {
        List<BranchInventory> warningItems = new ArrayList<>();
        int lowCount = 0;
        int outCount = 0;

        for (BranchInventory item : sourceItems) {
            if (isOutOfStock(item)) {
                outCount++;
                warningItems.add(item);
            } else if (isLowStock(item)) {
                lowCount++;
                warningItems.add(item);
            }
        }
        warningItems.sort(this::compareWarningItems);

        boolean hasWarning = !warningItems.isEmpty();
        setAlertStyle(hasWarning);
        lowStockAlertIcon.setText(hasWarning ? "!" : "OK");
        btnShowLowStock.setVisible(lowCount > 0);
        btnShowLowStock.setManaged(lowCount > 0);
        btnShowOutStock.setVisible(outCount > 0);
        btnShowOutStock.setManaged(outCount > 0);

        if (!hasWarning) {
            lowStockSummaryLabel.setText("Không có cảnh báo tồn kho thấp");
            lowStockDetailLabel.setText("Các sản phẩm trong phạm vi hiện tại đều cao hơn điểm đặt hàng lại.");
            lowStockItemsBox.getChildren().clear();
            lowStockItemsBox.setVisible(false);
            lowStockItemsBox.setManaged(false);
            return;
        }

        int totalWarning = lowCount + outCount;
        lowStockSummaryLabel.setText("Có " + totalWarning + " sản phẩm cần chú ý");
        lowStockDetailLabel.setText(
            outCount + " hết hàng, " + lowCount + " sắp hết trong " + currentScopeText()
        );
        lowStockItemsBox.getChildren().setAll(buildWarningCards(warningItems));
        lowStockItemsBox.setVisible(true);
        lowStockItemsBox.setManaged(true);
    }

    private List<VBox> buildWarningCards(List<BranchInventory> warningItems) {
        List<VBox> cards = new ArrayList<>();
        int limit = Math.min(warningItems.size(), 4);
        for (int i = 0; i < limit; i++) {
            cards.add(createWarningCard(warningItems.get(i)));
        }
        if (warningItems.size() > limit) {
            VBox moreCard = new VBox(4);
            moreCard.getStyleClass().addAll("inventory-alert-item", "inventory-alert-more");
            Label countLabel = new Label("+" + (warningItems.size() - limit));
            countLabel.getStyleClass().add("inventory-alert-more-count");
            Label hintLabel = new Label("mục khác");
            hintLabel.getStyleClass().add("inventory-alert-meta");
            moreCard.getChildren().addAll(countLabel, hintLabel);
            cards.add(moreCard);
        }
        return cards;
    }

    private VBox createWarningCard(BranchInventory item) {
        boolean outOfStock = isOutOfStock(item);
        VBox card = new VBox(4);
        card.getStyleClass().addAll(
            "inventory-alert-item",
            outOfStock ? "inventory-alert-item-out" : "inventory-alert-item-low"
        );

        Label nameLabel = new Label(valueOrDash(item.getProductName()));
        nameLabel.getStyleClass().add("inventory-alert-product");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(220);

        Label statusLabel = new Label(valueOrDash(item.getBranchId()) + " - " + item.getStockStatus());
        statusLabel.getStyleClass().add(outOfStock ? "inventory-alert-status-out" : "inventory-alert-status-low");

        Label quantityLabel = new Label(
            "Còn " + item.getQuantityText() + unitSuffix(item)
                + " / ngưỡng " + item.getReorderPointText()
        );
        quantityLabel.getStyleClass().add("inventory-alert-meta");

        card.getChildren().addAll(nameLabel, statusLabel, quantityLabel);
        return card;
    }

    private void setAlertStyle(boolean hasWarning) {
        lowStockAlertPanel.getStyleClass().removeAll("inventory-alert-panel-ok", "inventory-alert-panel-warning");
        lowStockAlertPanel.getStyleClass().add(hasWarning ? "inventory-alert-panel-warning" : "inventory-alert-panel-ok");
    }

    private String buildTotalText(int visibleCount, List<BranchInventory> alertSource) {
        int lowCount = 0;
        int outCount = 0;
        for (BranchInventory item : alertSource) {
            if (isOutOfStock(item)) {
                outCount++;
            } else if (isLowStock(item)) {
                lowCount++;
            }
        }
        return "Hiển thị " + visibleCount + " dòng tồn kho"
            + " - " + lowCount + " sắp hết"
            + " - " + outCount + " hết hàng";
    }

    private int compareWarningItems(BranchInventory first, BranchInventory second) {
        int priorityCompare = Integer.compare(warningPriority(first), warningPriority(second));
        if (priorityCompare != 0) {
            return priorityCompare;
        }
        return valueOrDash(first.getProductName()).compareToIgnoreCase(valueOrDash(second.getProductName()));
    }

    private int warningPriority(BranchInventory item) {
        return isOutOfStock(item) ? 0 : 1;
    }

    private boolean isLowStock(BranchInventory item) {
        return item != null && BranchInventory.STATUS_LOW.equals(item.getStockStatus());
    }

    private boolean isOutOfStock(BranchInventory item) {
        return item != null && BranchInventory.STATUS_OUT.equals(item.getStockStatus());
    }

    private String currentScopeText() {
        String branchId = branchCombo.getValue();
        return branchId == null || branchId.isBlank()
            ? "toàn bộ chi nhánh"
            : "chi nhánh " + branchId.trim();
    }

    private String unitSuffix(BranchInventory item) {
        String unit = item == null ? null : item.getUnit();
        return unit == null || unit.isBlank() ? "" : " " + unit.trim();
    }

    private BigDecimal parseOptionalQuantity(String rawValue) {
        String value = rawValue == null ? "" : rawValue.trim();
        if (value.isEmpty() || "-".equals(value)) {
            return null;
        }

        try {
            String normalized = value.replace(" ", "");
            if (normalized.contains(",") && normalized.contains(".")) {
                normalized = normalized.replace(",", "");
            } else {
                normalized = normalized.replace(",", ".");
            }
            BigDecimal quantity = new BigDecimal(normalized);
            if (quantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("Điểm đặt hàng lại phải lớn hơn hoặc bằng 0.");
            }
            return quantity;
        } catch (NumberFormatException e) {
            throw new ValidationException("Điểm đặt hàng lại phải là số hợp lệ.");
        }
    }

    private String resolveCurrentBranch() {
        String branchId = SessionManager.getInstance().getBranchId();
        if (branchId != null && !branchId.isBlank()) {
            return branchId.trim();
        }
        if (currentUser != null && currentUser.getEmployee() != null) {
            return currentUser.getEmployee().getBranchId();
        }
        return null;
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message == null ? "Có lỗi xảy ra." : message, ButtonType.OK).showAndWait();
    }

    private void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message == null ? "Đã cập nhật." : message, ButtonType.OK).showAndWait();
    }
}
