package PetHotel.gui.controller;

import PetHotel.bus.ServiceProductStandardBUS;
import PetHotel.dao.ProductDAO;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.model.PetService;
import PetHotel.model.Product;
import PetHotel.model.ServiceProductStandard;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ServiceProductStandardController {

    @FXML private Label lblServiceInfo;

    @FXML private TableView<ServiceProductStandard> standardTable;
    @FXML private TableColumn<ServiceProductStandard, String> colProductName;
    @FXML private TableColumn<ServiceProductStandard, String> colSpecies;
    @FXML private TableColumn<ServiceProductStandard, String> colWeightRange;
    @FXML private TableColumn<ServiceProductStandard, String> colUsageAmount;
    @FXML private TableColumn<ServiceProductStandard, String> colUnit;
    @FXML private TableColumn<ServiceProductStandard, String> colNote;

    @FXML private ComboBox<Product> cbProduct;
    @FXML private ComboBox<String> cbSpecies;
    @FXML private ComboBox<String> cbUnit;

    @FXML private TextField txtMinWeight;
    @FXML private TextField txtMaxWeight;
    @FXML private TextField txtUsageAmount;
    @FXML private TextArea txtNote;

    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;
    @FXML private Button btnClose;

    private final ServiceProductStandardBUS standardBUS = new ServiceProductStandardBUS();
    private final ProductDAO productDAO = new ProductDAO();

    private AppUser currentUser;
    private PetService currentService;
    private ServiceProductStandard selectedStandard;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupComboboxes();
        setupSelectionListener();

        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
    }

    public void setCurrentUser(AppUser currentUser) {
        this.currentUser = currentUser;
    }

    public void setService(PetService service) {
        this.currentService = service;

        if (service != null) {
            lblServiceInfo.setText(
                    "Dịch vụ: " + service.getServiceId() + " - " + service.getServiceName()
            );
        }

        loadProducts();
        loadStandards();
    }

    private void setupTableColumns() {
        colProductName.setCellValueFactory(cell ->
                new SimpleStringProperty(valueOrDash(cell.getValue().getProductName()))
        );

        colSpecies.setCellValueFactory(cell ->
                new SimpleStringProperty(valueOrDash(cell.getValue().getSpecies()))
        );

        colWeightRange.setCellValueFactory(cell -> {
            ServiceProductStandard s = cell.getValue();
            String min = s.getMinWeightKg() == null ? "0" : s.getMinWeightKg().toPlainString();
            String max = s.getMaxWeightKg() == null ? "0" : s.getMaxWeightKg().toPlainString();
            return new SimpleStringProperty(min + " - " + max + " kg");
        });

        colUsageAmount.setCellValueFactory(cell -> {
            BigDecimal amount = cell.getValue().getUsageAmount();
            return new SimpleStringProperty(amount == null ? "—" : amount.toPlainString());
        });

        colUnit.setCellValueFactory(cell ->
                new SimpleStringProperty(valueOrDash(cell.getValue().getUsageUnit()))
        );

        colNote.setCellValueFactory(cell ->
                new SimpleStringProperty(valueOrDash(cell.getValue().getNote()))
        );
    }

    private void setupComboboxes() {
        cbSpecies.setItems(FXCollections.observableArrayList("DOG", "CAT"));
        cbUnit.setItems(FXCollections.observableArrayList("ML", "L", "G", "KG"));

        cbSpecies.setValue("DOG");
        cbUnit.setValue("ML");

        cbProduct.setConverter(new StringConverter<Product>() {
            @Override
            public String toString(Product product) {
                if (product == null) {
                    return "";
                }

                return product.getProductId() + " - " + product.getProductName();
            }

            @Override
            public Product fromString(String string) {
                return null;
            }
        });

        cbProduct.setCellFactory(param -> new ListCell<Product>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null
                        ? null
                        : item.getProductId() + " - " + item.getProductName());
            }
        });

        cbProduct.setButtonCell(new ListCell<Product>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null
                        ? null
                        : item.getProductId() + " - " + item.getProductName());
            }
        });

        cbProduct.setOnAction(e -> {
            Product product = cbProduct.getValue();
            if (product != null && product.getUnit() != null && !product.getUnit().trim().isEmpty()) {
                String unit = product.getUnit().trim().toUpperCase();

                if ("ML".equals(unit) || "L".equals(unit) || "G".equals(unit) || "KG".equals(unit)) {
                    cbUnit.setValue(unit);
                }
            }
        });
    }

    private void setupSelectionListener() {
        standardTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selectedStandard = newValue;

            if (newValue == null) {
                btnUpdate.setDisable(true);
                btnDelete.setDisable(true);
                return;
            }

            fillForm(newValue);
            btnUpdate.setDisable(false);
            btnDelete.setDisable(false);
        });
    }

    private void loadProducts() {
        try {
            List<Product> products = productDAO.findActiveProducts();
            cbProduct.setItems(FXCollections.observableArrayList(products));
        } catch (Exception e) {
            e.printStackTrace();
            showError("Không thể tải danh sách sản phẩm: " + e.getMessage());
        }
    }

    private void loadStandards() {
        if (currentService == null || currentUser == null) {
            return;
        }

        try {
            List<ServiceProductStandard> standards =
                    standardBUS.getByService(currentService.getServiceId(), currentUser);

            standardTable.setItems(FXCollections.observableArrayList(standards));

        } catch (ValidationException e) {
            showWarning(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Không thể tải định mức vật tư: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        try {
            ServiceProductStandard sps = readForm();
            standardBUS.create(sps, currentUser);

            showInfo("Thêm định mức vật tư thành công.");
            loadStandards();
            clearForm();

        } catch (ValidationException e) {
            showWarning(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Không thể thêm định mức vật tư: " + e.getMessage());
        } catch (Exception e) {
            showWarning("Dữ liệu nhập không hợp lệ: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedStandard == null) {
            showWarning("Vui lòng chọn một dòng định mức để cập nhật.");
            return;
        }

        try {
            ServiceProductStandard sps = readForm();
            sps.setServiceProductStandardId(selectedStandard.getServiceProductStandardId());

            standardBUS.update(sps, currentUser);

            showInfo("Cập nhật định mức vật tư thành công.");
            loadStandards();
            clearForm();

        } catch (ValidationException e) {
            showWarning(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Không thể cập nhật định mức vật tư: " + e.getMessage());
        } catch (Exception e) {
            showWarning("Dữ liệu nhập không hợp lệ: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedStandard == null) {
            showWarning("Vui lòng chọn một dòng định mức để xóa.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn xóa định mức vật tư này?");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            standardBUS.delete(selectedStandard.getServiceProductStandardId(), currentUser);

            showInfo("Xóa định mức vật tư thành công.");
            loadStandards();
            clearForm();

        } catch (ValidationException e) {
            showWarning(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Không thể xóa định mức vật tư: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearForm() {
        clearForm();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    private ServiceProductStandard readForm() throws ValidationException {
        if (currentService == null) {
            throw new ValidationException("Chưa chọn dịch vụ.");
        }

        Product product = cbProduct.getValue();
        if (product == null) {
            throw new ValidationException("Vui lòng chọn sản phẩm.");
        }

        ServiceProductStandard sps = new ServiceProductStandard();

        sps.setServiceId(currentService.getServiceId());
        sps.setProductId(product.getProductId());
        sps.setProductName(product.getProductName());
        sps.setSpecies(cbSpecies.getValue());
        sps.setMinWeightKg(parseBigDecimal(txtMinWeight.getText(), "Cân nặng từ"));
        sps.setMaxWeightKg(parseBigDecimal(txtMaxWeight.getText(), "Cân nặng đến"));
        sps.setUsageAmount(parseBigDecimal(txtUsageAmount.getText(), "Số lượng dùng"));
        sps.setUsageUnit(cbUnit.getValue());
        sps.setNote(txtNote.getText());

        return sps;
    }

    private BigDecimal parseBigDecimal(String text, String fieldName) throws ValidationException {
        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException(fieldName + " không được rỗng.");
        }

        try {
            return new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " phải là số hợp lệ.");
        }
    }

    private void fillForm(ServiceProductStandard sps) {
        selectProductById(sps.getProductId());

        cbSpecies.setValue(sps.getSpecies());
        txtMinWeight.setText(sps.getMinWeightKg() == null ? "" : sps.getMinWeightKg().toPlainString());
        txtMaxWeight.setText(sps.getMaxWeightKg() == null ? "" : sps.getMaxWeightKg().toPlainString());
        txtUsageAmount.setText(sps.getUsageAmount() == null ? "" : sps.getUsageAmount().toPlainString());
        cbUnit.setValue(sps.getUsageUnit());
        txtNote.setText(sps.getNote() == null ? "" : sps.getNote());
    }

    private void selectProductById(String productId) {
        if (productId == null) {
            cbProduct.setValue(null);
            return;
        }

        for (Product product : cbProduct.getItems()) {
            if (productId.equals(product.getProductId())) {
                cbProduct.setValue(product);
                return;
            }
        }

        cbProduct.setValue(null);
    }

    private void clearForm() {
        standardTable.getSelectionModel().clearSelection();
        selectedStandard = null;

        cbProduct.setValue(null);
        cbSpecies.setValue("DOG");
        cbUnit.setValue("ML");

        txtMinWeight.clear();
        txtMaxWeight.clear();
        txtUsageAmount.clear();
        txtNote.clear();

        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "—" : value;
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}