package PetHotel.gui.controller;

import PetHotel.bus.InventoryBUS;
import PetHotel.model.AppUser;
import PetHotel.model.InventoryItem;
import PetHotel.model.Product;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;

public class StockAdjustmentController {
    @FXML private ComboBox<Product> cmbProduct;
    @FXML private Label lblSystemQty;
    @FXML private TextField txtActualQty;
    @FXML private TextField txtReorderPoint;
    @FXML private TextArea txtNote;
    @FXML private Label lblProductError;
    @FXML private Label lblActualError;
    @FXML private Label lblReorderError;
    @FXML private Label lblGeneralError;

    private final InventoryBUS inventoryBUS = new InventoryBUS();
    private AppUser currentUser;
    private Runnable onSaved;
    private String preselectedProductId;

    public static void open(InventoryItem item, Runnable onSaved) {
        try {
            FXMLLoader loader = new FXMLLoader(
                StockAdjustmentController.class.getResource("/PetHotel/gui/view/StockAdjustmentForm.fxml")
            );
            VBox root = loader.load();
            StockAdjustmentController controller = loader.getController();
            controller.onSaved = onSaved;
            controller.preselect(item);

            Stage stage = new Stage();
            stage.setTitle("Điều Chỉnh Tồn Kho");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Không thể mở form điều chỉnh tồn kho: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        setupProductCombo();
        loadProducts();
        clearErrorsOnInput();
    }

    private void preselect(InventoryItem item) {
        if (item == null) {
            return;
        }
        preselectedProductId = item.getProductId();
        selectProduct(preselectedProductId);
        lblSystemQty.setText(item.getStockWithUnit());
        txtActualQty.setText(item.getQuantityText());
        txtReorderPoint.setText(item.getReorderPointText());
    }

    private void setupProductCombo() {
        cmbProduct.setCellFactory(param -> new ProductCell());
        cmbProduct.setButtonCell(new ProductCell());
        cmbProduct.valueProperty().addListener((obs, oldValue, newValue) -> {
            lblProductError.setText("");
            loadSelectedInventory(newValue);
        });
    }

    private void loadProducts() {
        try {
            cmbProduct.setItems(FXCollections.observableArrayList(inventoryBUS.getProducts(currentUser)));
            if (preselectedProductId != null) {
                selectProduct(preselectedProductId);
            }
        } catch (Exception e) {
            showError("Không thể tải danh sách vật tư: " + e.getMessage());
        }
    }

    private void selectProduct(String productId) {
        if (productId == null || cmbProduct.getItems() == null) {
            return;
        }
        for (Product product : cmbProduct.getItems()) {
            if (productId.equals(product.getProductId())) {
                cmbProduct.setValue(product);
                return;
            }
        }
    }

    private void loadSelectedInventory(Product product) {
        if (product == null) {
            lblSystemQty.setText("—");
            return;
        }
        try {
            InventoryItem item = inventoryBUS.getInventoryItem(resolveBranchId(), product.getProductId(), currentUser);
            if (item == null) {
                lblSystemQty.setText("0 " + product.getUnit());
                txtActualQty.setText("0");
                txtReorderPoint.setText("0");
            } else {
                lblSystemQty.setText(item.getStockWithUnit());
                txtActualQty.setText(item.getQuantityText());
                txtReorderPoint.setText(item.getReorderPointText());
            }
        } catch (Exception e) {
            lblSystemQty.setText("—");
            lblGeneralError.setText(e.getMessage());
        }
    }

    @FXML
    public void handleSave() {
        clearErrors();
        try {
            Product product = cmbProduct.getValue();
            if (product == null) {
                lblProductError.setText("Vui lòng chọn vật tư.");
                return;
            }

            BigDecimal actualQty = parseDecimal(txtActualQty.getText(), "Số lượng thực tế", lblActualError);
            BigDecimal reorderPoint = parseDecimal(txtReorderPoint.getText(), "Ngưỡng tối thiểu", lblReorderError);

            inventoryBUS.adjustStock(
                resolveBranchId(),
                product.getProductId(),
                actualQty,
                reorderPoint,
                txtNote.getText(),
                currentUser
            );

            new Alert(Alert.AlertType.INFORMATION, "Đã hoàn tất kiểm kê và cập nhật tồn kho.", ButtonType.OK).showAndWait();
            if (onSaved != null) {
                onSaved.run();
            }
            closeWindow();
        } catch (Exception e) {
            lblGeneralError.setText(e.getMessage());
        }
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }

    private BigDecimal parseDecimal(String value, String fieldName, Label errorLabel) {
        if (value == null || value.trim().isEmpty()) {
            errorLabel.setText(fieldName + " không được để trống.");
            throw new IllegalArgumentException(fieldName + " không được để trống.");
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            errorLabel.setText(fieldName + " phải là số.");
            throw new IllegalArgumentException(fieldName + " phải là số.");
        }
    }

    private void clearErrorsOnInput() {
        txtActualQty.textProperty().addListener((obs, oldValue, newValue) -> lblActualError.setText(""));
        txtReorderPoint.textProperty().addListener((obs, oldValue, newValue) -> lblReorderError.setText(""));
    }

    private void clearErrors() {
        lblProductError.setText("");
        lblActualError.setText("");
        lblReorderError.setText("");
        lblGeneralError.setText("");
    }

    private String resolveBranchId() {
        String branchId = SessionManager.getInstance().getBranchId();
        if (branchId != null && !branchId.isBlank()) {
            return branchId.trim();
        }
        if (currentUser != null && currentUser.getEmployee() != null) {
            return currentUser.getEmployee().getBranchId();
        }
        return null;
    }

    private void closeWindow() {
        Stage stage = (Stage) txtActualQty.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }

    private static class ProductCell extends ListCell<Product> {
        @Override
        protected void updateItem(Product item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? null : item.getProductName() + " (" + item.getProductId() + ")");
        }
    }
}
