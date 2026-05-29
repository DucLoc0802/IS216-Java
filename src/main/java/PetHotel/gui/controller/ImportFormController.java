package PetHotel.gui.controller;

import PetHotel.bus.InventoryBUS;
import PetHotel.model.AppUser;
import PetHotel.model.GoodsReceipt;
import PetHotel.model.GoodsReceiptDetail;
import PetHotel.model.Product;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ImportFormController {
    @FXML private Label lblTitle;
    @FXML private ComboBox<String> cmbSupplier;
    @FXML private DatePicker dpReceiptDate;
    @FXML private ComboBox<Product> cmbProduct;
    @FXML private TextField txtQuantity;
    @FXML private TextField txtUnit;
    @FXML private TextArea txtNote;
    @FXML private Label lblSupplierError;
    @FXML private Label lblDateError;
    @FXML private Label lblProductError;
    @FXML private Label lblQuantityError;
    @FXML private Label lblGeneralError;

    private final InventoryBUS inventoryBUS = new InventoryBUS();
    private AppUser currentUser;
    private GoodsReceipt editingReceipt;
    private Runnable onSaved;
    private String preselectedProductId;

    public static void open(GoodsReceipt receipt, Runnable onSaved) {
        openInternal(receipt, null, onSaved);
    }

    public static void openForProduct(String productId, Runnable onSaved) {
        openInternal(null, productId, onSaved);
    }

    private static void openInternal(GoodsReceipt receipt, String productId, Runnable onSaved) {
        try {
            FXMLLoader loader = new FXMLLoader(
                ImportFormController.class.getResource("/PetHotel/gui/view/ImportForm.fxml")
            );
            VBox root = loader.load();

            ImportFormController controller = loader.getController();
            controller.onSaved = onSaved;
            controller.preselectedProductId = productId;
            controller.setReceipt(receipt);
            controller.selectProduct(productId);

            Stage stage = new Stage();
            stage.setTitle(receipt == null ? "Tạo Phiếu Nhập Hàng" : "Sửa Phiếu Nhập Hàng");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Không thể mở form nhập hàng: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        dpReceiptDate.setValue(LocalDate.now());
        cmbSupplier.setEditable(true);
        setupProductCombo();
        loadProducts();
        clearErrorsOnInput();
    }

    private void setReceipt(GoodsReceipt receipt) {
        if (receipt == null) {
            lblTitle.setText("Tạo Phiếu Nhập Hàng");
            return;
        }

        try {
            editingReceipt = inventoryBUS.getReceiptById(receipt.getGoodsReceiptId(), currentUser);
            if (editingReceipt == null) {
                showError("Không tìm thấy phiếu nhập.");
                closeWindow();
                return;
            }

            lblTitle.setText("Sửa Phiếu Nhập Hàng");
            cmbSupplier.setValue(editingReceipt.getSupplierName());
            cmbSupplier.getEditor().setText(editingReceipt.getSupplierName());
            dpReceiptDate.setValue(editingReceipt.getReceiptDate());
            txtNote.setText(editingReceipt.getNote() == null ? "" : editingReceipt.getNote());

            if (!editingReceipt.getDetails().isEmpty()) {
                GoodsReceiptDetail detail = editingReceipt.getDetails().get(0);
                selectProduct(detail.getProductId());
                txtQuantity.setText(detail.getQuantityText());
                txtUnit.setText(detail.getUnit());
            }
        } catch (Exception e) {
            showError("Không thể tải phiếu nhập: " + e.getMessage());
            closeWindow();
        }
    }

    private void setupProductCombo() {
        cmbProduct.setCellFactory(param -> new ProductCell());
        cmbProduct.setButtonCell(new ProductCell());
        cmbProduct.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                txtUnit.setText(newValue.getUnit());
                lblProductError.setText("");
            }
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
                txtUnit.setText(product.getUnit());
                return;
            }
        }
    }

    @FXML
    public void handleSave() {
        clearErrors();
        try {
            String supplierName = cmbSupplier.getEditor().getText();
            Product product = cmbProduct.getValue();
            BigDecimal quantity = parseQuantity(txtQuantity.getText());

            GoodsReceiptDetail detail = new GoodsReceiptDetail();
            detail.setProductId(product == null ? null : product.getProductId());
            detail.setQuantity(quantity);
            detail.setUnit(txtUnit.getText());
            detail.setNote(txtNote.getText());

            List<GoodsReceiptDetail> details = List.of(detail);
            if (editingReceipt == null) {
                inventoryBUS.createReceipt(
                    resolveBranchId(),
                    supplierName,
                    dpReceiptDate.getValue(),
                    details,
                    txtNote.getText(),
                    currentUser
                );
            } else {
                inventoryBUS.updateReceipt(
                    editingReceipt.getGoodsReceiptId(),
                    resolveBranchId(),
                    supplierName,
                    dpReceiptDate.getValue(),
                    details,
                    txtNote.getText(),
                    currentUser
                );
            }

            new Alert(Alert.AlertType.INFORMATION, "Phiếu nhập đã được lưu và cập nhật tồn kho.", ButtonType.OK).showAndWait();
            if (onSaved != null) {
                onSaved.run();
            }
            closeWindow();
        } catch (Exception e) {
            showFormError(e.getMessage());
        }
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }

    private BigDecimal parseQuantity(String value) {
        if (value == null || value.trim().isEmpty()) {
            lblQuantityError.setText("Vui lòng nhập số lượng.");
            throw new IllegalArgumentException("Vui lòng nhập số lượng.");
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            lblQuantityError.setText("Số lượng phải là số.");
            throw new IllegalArgumentException("Số lượng phải là số.");
        }
    }

    private void showFormError(String message) {
        String error = message == null ? "Dữ liệu không hợp lệ." : message;
        if (error.contains("Nguồn nhập") || error.contains("nguồn nhập")) {
            lblSupplierError.setText(error);
        } else if (error.contains("ngày") || error.contains("Ngày")) {
            lblDateError.setText(error);
        } else if (error.contains("vật tư") || error.contains("Vui lòng chọn")) {
            lblProductError.setText(error);
        } else if (error.contains("Số lượng")) {
            lblQuantityError.setText(error);
        } else {
            lblGeneralError.setText(error);
        }
    }

    private void clearErrorsOnInput() {
        cmbSupplier.getEditor().textProperty().addListener((obs, oldValue, newValue) -> lblSupplierError.setText(""));
        dpReceiptDate.valueProperty().addListener((obs, oldValue, newValue) -> lblDateError.setText(""));
        txtQuantity.textProperty().addListener((obs, oldValue, newValue) -> lblQuantityError.setText(""));
    }

    private void clearErrors() {
        lblSupplierError.setText("");
        lblDateError.setText("");
        lblProductError.setText("");
        lblQuantityError.setText("");
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
        Stage stage = (Stage) txtQuantity.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }

    private static class ProductCell extends ListCell<Product> {
        @Override
        protected void updateItem(Product item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item.getProductName() + " (" + item.getProductId() + ")");
            }
        }
    }
}
