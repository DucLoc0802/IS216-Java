package PetHotel.gui.controller;

import PetHotel.bus.ProductBUS;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.model.Product;
import PetHotel.util.Role;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ProductFormController {
    private static final List<String> ALLOWED_UNITS = List.of("G", "KG", "L", "ML");

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private TextField productIdField;
    @FXML private TextField productNameField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private ComboBox<String> unitField;
    @FXML private TextField importPriceField;
    @FXML private TextField minQuantityField;
    @FXML private CheckBox activeCheck;
    @FXML private TextArea noteArea;
    @FXML private Label nameErrorLabel;
    @FXML private Label categoryErrorLabel;
    @FXML private Label unitErrorLabel;
    @FXML private Label priceErrorLabel;
    @FXML private Label minQuantityErrorLabel;
    @FXML private Label noteErrorLabel;
    @FXML private Button saveButton;

    private final ProductBUS productBUS = new ProductBUS();
    private AppUser currentUser;
    private Product editingProduct;
    private Consumer<Product> onProductSaved;

    public static void openProductDialog(Product product, Consumer<Product> onSaved) {
        try {
            FXMLLoader loader = new FXMLLoader(
                ProductFormController.class.getResource("/PetHotel/gui/view/ProductForm.fxml")
            );
            VBox root = loader.load();

            ProductFormController controller = loader.getController();
            controller.onProductSaved = onSaved;
            controller.setEditingProduct(product);

            Stage stage = new Stage();
            stage.setTitle(product == null ? "Thêm Sản Phẩm" : "Sửa Sản Phẩm");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Không thể mở form sản phẩm: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        setupCategories();
        setupUnits();
        setupClearErrorHandlers();
    }

    private void setEditingProduct(Product product) {
        editingProduct = product;
        if (product == null) {
            prepareAddForm();
        } else {
            prepareEditForm(product);
        }
    }

    private void setupCategories() {
        try {
            categoryCombo.setItems(FXCollections.observableArrayList(productBUS.getCategories()));
        } catch (SQLException e) {
            categoryCombo.setItems(FXCollections.observableArrayList(List.of("Vệ sinh", "Cắt tỉa", "Nhuộm", "Khác")));
            showError("Không thể tải loại sản phẩm: " + e.getMessage());
        }
    }

    private void setupUnits() {
        unitField.setItems(FXCollections.observableArrayList(ALLOWED_UNITS));
    }

    private void setupClearErrorHandlers() {
        productNameField.textProperty().addListener((obs, oldValue, newValue) -> nameErrorLabel.setText(""));
        categoryCombo.valueProperty().addListener((obs, oldValue, newValue) -> categoryErrorLabel.setText(""));
        unitField.valueProperty().addListener((obs, oldValue, newValue) -> unitErrorLabel.setText(""));
        importPriceField.textProperty().addListener((obs, oldValue, newValue) -> priceErrorLabel.setText(""));
        minQuantityField.textProperty().addListener((obs, oldValue, newValue) -> minQuantityErrorLabel.setText(""));
        noteArea.textProperty().addListener((obs, oldValue, newValue) -> noteErrorLabel.setText(""));
    }

    private void prepareAddForm() {
        titleLabel.setText("Thêm Sản Phẩm");
        subtitleLabel.setText("Tạo sản phẩm/vật tư tiêu hao dùng cho dịch vụ");
        productIdField.clear();
        productNameField.clear();
        selectCategory("Khác");
        unitField.setValue(null);
        importPriceField.setText("0");
        minQuantityField.setText("0");
        activeCheck.setSelected(true);
        activeCheck.setDisable(true);
        noteArea.clear();
        saveButton.setText("Thêm Sản Phẩm");
    }

    private void prepareEditForm(Product product) {
        titleLabel.setText("Sửa Sản Phẩm");
        subtitleLabel.setText("Cập nhật thông tin sản phẩm/vật tư tiêu hao");
        productIdField.setText(valueOrEmpty(product.getProductId()));
        productNameField.setText(valueOrEmpty(product.getProductName()));
        selectCategory(valueOrDefault(product.getProductCategory(), "Khác"));
        unitField.setValue(normalizeAllowedUnit(product.getUnit()));
        importPriceField.setText(formatDecimal(product.getImportPrice()));
        minQuantityField.setText(formatDecimal(product.getMinQuantity()));
        activeCheck.setSelected(product.isActive());
        activeCheck.setDisable(false);
        noteArea.setText(valueOrEmpty(product.getNote()));
        saveButton.setText("Lưu Thay Đổi");
    }

    @FXML
    public void handleSave() {
        clearErrors();
        if (!canManage()) {
            showError("Bạn không có quyền quản lý sản phẩm.");
            return;
        }

        try {
            Product product = buildProductFromForm();
            if (editingProduct == null) {
                productBUS.createProduct(product);
                showInfo("Đã thêm sản phẩm " + product.getProductId() + ".");
            } else {
                productBUS.updateProduct(product);
                showInfo("Đã cập nhật sản phẩm " + product.getProductId() + ".");
            }

            if (onProductSaved != null) {
                onProductSaved.accept(product);
            }
            closeWindow();
        } catch (ValidationException e) {
            showFieldError(e.getMessage());
        } catch (SQLException e) {
            showError("Không thể lưu sản phẩm: " + e.getMessage());
        } catch (RuntimeException e) {
            showFieldError(e.getMessage());
        }
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }

    private Product buildProductFromForm() {
        Product product = new Product();
        if (editingProduct != null) {
            product.setProductId(editingProduct.getProductId());
        }
        product.setProductName(productNameField.getText());
        product.setProductCategory(categoryCombo.getValue());
        product.setUnit(unitField.getValue());
        product.setImportPrice(parseDecimal(importPriceField.getText(), "Giá nhập"));
        product.setMinQuantity(parseDecimal(minQuantityField.getText(), "Tồn tối thiểu"));
        product.setActive(editingProduct == null || activeCheck.isSelected());
        product.setNote(noteArea.getText());
        return product;
    }

    private void selectCategory(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            categoryCombo.setValue("Khác");
            return;
        }
        if (!categoryCombo.getItems().contains(categoryName)) {
            List<String> values = new ArrayList<>(categoryCombo.getItems());
            values.add(categoryName);
            categoryCombo.setItems(FXCollections.observableArrayList(values));
        }
        categoryCombo.setValue(categoryName);
    }

    private BigDecimal parseDecimal(String rawValue, String fieldName) {
        String value = rawValue == null ? "" : rawValue.trim();
        if (value.isEmpty()) {
            return BigDecimal.ZERO;
        }

        try {
            String normalized = value.replace(" ", "");
            if (normalized.contains(",") && normalized.contains(".")) {
                normalized = normalized.replace(",", "");
            } else {
                normalized = normalized.replace(",", ".");
            }
            return new BigDecimal(normalized);
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " phải là số hợp lệ.");
        }
    }

    private boolean canManage() {
        if (currentUser == null) {
            return false;
        }
        Role role = currentUser.getRole();
        return role == Role.ADMIN || role == Role.BRANCH_MANAGER;
    }

    private void clearErrors() {
        nameErrorLabel.setText("");
        categoryErrorLabel.setText("");
        unitErrorLabel.setText("");
        priceErrorLabel.setText("");
        minQuantityErrorLabel.setText("");
        noteErrorLabel.setText("");
    }

    private void showFieldError(String message) {
        String text = message == null ? "Dữ liệu nhập chưa hợp lệ." : message;
        if (text.contains("Tên sản phẩm")) {
            nameErrorLabel.setText(text);
        } else if (text.contains("Loại sản phẩm")) {
            categoryErrorLabel.setText(text);
        } else if (text.contains("Đơn vị")) {
            unitErrorLabel.setText(text);
        } else if (text.contains("Giá nhập")) {
            priceErrorLabel.setText(text);
        } else if (text.contains("Tồn tối thiểu")) {
            minQuantityErrorLabel.setText(text);
        } else if (text.contains("Ghi chú")) {
            noteErrorLabel.setText(text);
        } else {
            showError(text);
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) productNameField.getScene().getWindow();
        stage.close();
    }

    private String formatDecimal(BigDecimal value) {
        BigDecimal safeValue = value == null ? BigDecimal.ZERO : value;
        return safeValue.stripTrailingZeros().toPlainString();
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    private String normalizeAllowedUnit(String unit) {
        if (unit == null) {
            return null;
        }
        String normalized = unit.trim().toUpperCase();
        return ALLOWED_UNITS.contains(normalized) ? normalized : null;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
