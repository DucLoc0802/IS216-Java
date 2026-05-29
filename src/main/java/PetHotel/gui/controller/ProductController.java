package PetHotel.gui.controller;

import PetHotel.bus.ProductBUS;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.model.Product;
import PetHotel.util.Role;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ProductController {
    private static final List<String> FALLBACK_CATEGORIES = List.of(
        "Vệ sinh",
        "Cắt tỉa",
        "Nhuộm",
        "Khác"
    );

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Button btnAddProduct;
    @FXML private Button btnAddCategory;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Label totalLabel;

    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> colProductId;
    @FXML private TableColumn<Product, String> colProductName;
    @FXML private TableColumn<Product, String> colProductCategory;
    @FXML private TableColumn<Product, String> colUnit;
    @FXML private TableColumn<Product, String> colImportPrice;
    @FXML private TableColumn<Product, String> colMinQuantity;
    @FXML private TableColumn<Product, String> colStatus;
    @FXML private TableColumn<Product, Void> colActions;

    private final ProductBUS productBUS = new ProductBUS();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    private AppUser currentUser;
    private Product selectedProduct;
    private boolean canManage;
    private boolean loadingFilters;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        canManage = currentUser != null
            && (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.BRANCH_MANAGER);

        setupRoleUi();
        setupColumns();
        setupSelectionListener();
        setupFilters();
        loadProducts();
    }

    private void setupRoleUi() {
        boolean canViewImportPrice = currentUser != null
            && currentUser.getRole() != Role.RECEPTIONIST
            && currentUser.getRole() != Role.PET_CARE_STAFF;
        colImportPrice.setVisible(canViewImportPrice);
        colImportPrice.setManaged(canViewImportPrice);

        showManaged(btnAddProduct, canManage);
        showManaged(btnAddCategory, canManage);
        btnEdit.setDisable(true);
        btnDelete.setDisable(true);
    }

    private void setupFilters() {
        loadingFilters = true;
        List<String> categories = loadCategories();

        try {
            categoryFilter.setItems(FXCollections.observableArrayList(withAllOption(categories)));
            categoryFilter.setValue(ProductBUS.STATUS_ALL);

            statusFilter.setItems(FXCollections.observableArrayList(
                ProductBUS.STATUS_ALL,
                ProductBUS.STATUS_ACTIVE,
                ProductBUS.STATUS_INACTIVE
            ));
            statusFilter.setValue(ProductBUS.STATUS_ALL);
        } finally {
            loadingFilters = false;
        }
    }

    private void setupColumns() {
        colProductId.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getProductId())));
        colProductName.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getProductName())));
        colProductCategory.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getProductCategory())));
        colUnit.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getUnit())));
        colImportPrice.setCellValueFactory(cell -> new SimpleStringProperty(formatCurrency(cell.getValue().getImportPrice())));
        colMinQuantity.setCellValueFactory(cell -> new SimpleStringProperty(formatDecimal(cell.getValue().getMinQuantity())));
        colStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatusText()));

        colActions.setMinWidth(230);
        colActions.setPrefWidth(240);

        colStatus.setCellFactory(column -> new TableCell<>() {
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
                badge.getStyleClass().add(ProductBUS.STATUS_ACTIVE.equals(value) ? "status-active" : "status-locked");
                setGraphic(badge);
                setText(null);
            }
        });

        colActions.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Sửa");
            private final Button deleteButton = new Button("Ngừng dùng");
            private final HBox actions = new HBox(8, editButton, deleteButton);

            {
                editButton.setMinWidth(72);
                editButton.setPrefWidth(72);
                deleteButton.setMinWidth(126);
                deleteButton.setPrefWidth(126);
                editButton.setMaxWidth(Double.MAX_VALUE);
                deleteButton.setMaxWidth(Double.MAX_VALUE);
                editButton.getStyleClass().addAll("action-btn", "action-btn-outline");
                deleteButton.getStyleClass().addAll("action-btn", "action-btn-danger");

                editButton.setOnAction(event -> {
                    Product product = getCurrentRowProduct();
                    if (product != null) {
                        productTable.getSelectionModel().select(product);
                        openProductForm(product);
                    }
                });

                deleteButton.setOnAction(event -> {
                    Product product = getCurrentRowProduct();
                    if (product != null) {
                        productTable.getSelectionModel().select(product);
                        deleteProduct(product);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                Product product = getCurrentRowProduct();
                if (empty || product == null || !canManage) {
                    setGraphic(null);
                    return;
                }
                deleteButton.setDisable(!product.isActive());
                setGraphic(actions);
            }

            private Product getCurrentRowProduct() {
                int index = getIndex();
                if (index < 0 || index >= getTableView().getItems().size()) {
                    return null;
                }
                return getTableView().getItems().get(index);
            }
        });
    }

    private void setupSelectionListener() {
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selectedProduct = newValue;
            boolean hasSelection = newValue != null && canManage;
            btnEdit.setDisable(!hasSelection);
            btnDelete.setDisable(!hasSelection || !newValue.isActive());
        });
    }

    private void loadProducts() {
        try {
            List<Product> products = productBUS.getProducts(
                searchField.getText(),
                categoryFilter.getValue(),
                statusFilter.getValue()
            );
            productTable.setItems(FXCollections.observableArrayList(products));
            totalLabel.setText("Hiển thị " + products.size() + " sản phẩm");
        } catch (SQLException e) {
            productTable.setItems(FXCollections.observableArrayList());
            totalLabel.setText("Hiển thị 0 sản phẩm");
            showError("Không thể tải danh sách sản phẩm: " + e.getMessage());
        } catch (RuntimeException e) {
            productTable.setItems(FXCollections.observableArrayList());
            totalLabel.setText("Hiển thị 0 sản phẩm");
            showError("Lỗi tải dữ liệu sản phẩm: " + e.getMessage());
        }
    }

    @FXML
    public void onSearch() {
        if (!loadingFilters) {
            loadProducts();
        }
    }

    @FXML
    public void onClearFilter() {
        searchField.clear();
        categoryFilter.setValue(ProductBUS.STATUS_ALL);
        statusFilter.setValue(ProductBUS.STATUS_ALL);
        loadProducts();
    }

    @FXML
    public void onTableClick(MouseEvent event) {
        if (event.getClickCount() == 2 && selectedProduct != null && canManage) {
            openProductForm(selectedProduct);
        }
    }

    @FXML
    public void onAddProduct(ActionEvent event) {
        openProductForm(null);
    }

    @FXML
    public void onAddCategory(ActionEvent event) {
        if (!canManage) {
            return;
        }
        ProductCategoryDialogController.openAddCategoryDialog(categoryName -> {
            setupFilters();
            categoryFilter.setValue(categoryName);
            loadProducts();
        });
    }

    @FXML
    public void onEditProduct(ActionEvent event) {
        if (selectedProduct != null) {
            openProductForm(selectedProduct);
        }
    }

    @FXML
    public void onDeleteProduct(ActionEvent event) {
        if (selectedProduct != null) {
            deleteProduct(selectedProduct);
        }
    }

    private void openProductForm(Product product) {
        if (!canManage) {
            return;
        }
        ProductFormController.openProductDialog(product, savedProduct -> {
            setupFilters();
            loadProducts();
            if (savedProduct != null) {
                selectProduct(savedProduct.getProductId());
            }
        });
    }

    private void selectProduct(String productId) {
        if (productId == null || productTable.getItems() == null) {
            return;
        }
        for (Product product : productTable.getItems()) {
            if (productId.equals(product.getProductId())) {
                productTable.getSelectionModel().select(product);
                productTable.scrollTo(product);
                break;
            }
        }
    }

    private void deleteProduct(Product product) {
        if (!canManage || product == null || !product.isActive()) {
            return;
        }

        Alert confirm = new Alert(
            Alert.AlertType.CONFIRMATION,
            "Ngừng sử dụng sản phẩm " + product.getProductId() + "?",
            ButtonType.YES,
            ButtonType.NO
        );
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText(null);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.YES) {
            return;
        }

        try {
            productBUS.deleteProduct(product.getProductId());
            showInfo("Đã ngừng sử dụng sản phẩm " + product.getProductId() + ".");
            loadProducts();
        } catch (ValidationException e) {
            showError(e.getMessage());
        } catch (SQLException e) {
            showError("Không thể xóa mềm sản phẩm: " + e.getMessage());
        } catch (RuntimeException e) {
            showError("Không thể xóa mềm sản phẩm: " + e.getMessage());
        }
    }

    private List<String> loadCategories() {
        try {
            List<String> categories = productBUS.getCategories();
            return categories.isEmpty() ? FALLBACK_CATEGORIES : categories;
        } catch (SQLException e) {
            showError("Không thể tải loại sản phẩm: " + e.getMessage());
            return FALLBACK_CATEGORIES;
        } catch (RuntimeException e) {
            showError("Không thể tải loại sản phẩm: " + e.getMessage());
            return FALLBACK_CATEGORIES;
        }
    }

    private List<String> withAllOption(List<String> categories) {
        List<String> values = new ArrayList<>();
        values.add(ProductBUS.STATUS_ALL);
        values.addAll(categories);
        return values;
    }

    private String formatCurrency(BigDecimal value) {
        return currencyFormat.format(value == null ? BigDecimal.ZERO : value);
    }

    private String formatDecimal(BigDecimal value) {
        BigDecimal safeValue = value == null ? BigDecimal.ZERO : value;
        return safeValue.stripTrailingZeros().toPlainString();
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    private void showManaged(javafx.scene.Node node, boolean visible) {
        if (node != null) {
            node.setVisible(visible);
            node.setManaged(visible);
        }
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
