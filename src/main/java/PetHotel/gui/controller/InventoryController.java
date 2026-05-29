package PetHotel.gui.controller;

import PetHotel.bus.InventoryBUS;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.model.BranchInventory;
import PetHotel.util.Role;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.List;

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

    private void loadInventory() {
        try {
            List<BranchInventory> items = inventoryBUS.searchInventory(
                branchCombo.getValue(),
                searchField.getText(),
                stockStatusCombo.getValue(),
                currentUser
            );
            inventoryTable.setItems(FXCollections.observableArrayList(items));
            totalLabel.setText("Hiển thị " + items.size() + " dòng tồn kho");
        } catch (ValidationException | SQLException e) {
            inventoryTable.setItems(FXCollections.observableArrayList());
            totalLabel.setText("Hiển thị 0 dòng tồn kho");
            showError(e.getMessage());
        } catch (RuntimeException e) {
            inventoryTable.setItems(FXCollections.observableArrayList());
            totalLabel.setText("Hiển thị 0 dòng tồn kho");
            showError("Không thể tải tồn kho: " + e.getMessage());
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
}
