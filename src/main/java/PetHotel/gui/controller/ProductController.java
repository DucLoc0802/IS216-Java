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
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * ProductController — Quản Lý Sản Phẩm
 * ─────────────────────────────────────────────────────────────────
 * Xử lý: ProductManagement.fxml
 * Use cases:
 *   - Tra cứu / lọc sản phẩm
 *   - Thêm / Sửa / Xóa sản phẩm
 *   - Phân loại danh mục
 *   - Xem chi tiết sản phẩm ở panel phải
 */
public class ProductController {

    // ── Stats ────────────────────────────────────────────────────
    @FXML private Label statTotalProducts;
    @FXML private Label statCategories;
    @FXML private Label statInStock;
    @FXML private Label statLowStock;

    // ── Filter ───────────────────────────────────────────────────
    @FXML private TextField         searchField;
    @FXML private ComboBox<String>  filterCategory;
    @FXML private ComboBox<String>  filterStock;

    // ── Toolbar ──────────────────────────────────────────────────
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnDetail;
    @FXML private Label  totalLabel;

    // ── Table ────────────────────────────────────────────────────
    @FXML private TableView<Object>          productTable;
    @FXML private TableColumn<Object,String> colProdId;
    @FXML private TableColumn<Object,String> colProdName;
    @FXML private TableColumn<Object,String> colProdCategory;
    @FXML private TableColumn<Object,String> colProdUnit;
    @FXML private TableColumn<Object,String> colProdPrice;
    @FXML private TableColumn<Object,String> colProdQty;
    @FXML private TableColumn<Object,String> colProdMinQty;
    @FXML private TableColumn<Object,String> colProdStatus;
    @FXML private TableColumn<Object,String> colProdActions;

    @FXML private Pagination pagination;
    @FXML private Label      pageInfo;

    // ── Detail panel ─────────────────────────────────────────────
    @FXML private VBox   noSelectionHint;
    @FXML private Label  detailProductIcon;
    @FXML private Label  detailProductName;
    @FXML private Label  detailProductCategory;
    @FXML private Label  detailProdId;
    @FXML private Label  detailProdUnit;
    @FXML private Label  detailProdPrice;
    @FXML private Label  detailProdQty;
    @FXML private Label  detailProdMin;
    @FXML private Label  detailProdSupplier;
    @FXML private Label  detailProdDesc;
    @FXML private Button btnDetailEdit;
    @FXML private Button btnDetailDelete;

    // ── State ────────────────────────────────────────────────────
    private Object selectedProduct = null;

    // ── Lifecycle ────────────────────────────────────────────────

    @FXML
    public void initialize() {
        setupColumns();
        setupSelectionListener();
        loadCategoryFilter();
        loadStats();
        loadProducts();
        showNoSelection();
    }

    // ── Setup ────────────────────────────────────────────────────

    private void setupColumns() {
        // Stock status badge
        colProdStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) { setGraphic(null); return; }
                Label badge = new Label(value);
                badge.getStyleClass().add("status-badge");
                switch (value) {
                    case "Còn hàng"     -> badge.getStyleClass().add("status-instock");
                    case "Tồn kho thấp" -> badge.getStyleClass().add("status-low");
                    case "Hết hàng"     -> badge.getStyleClass().add("status-outstock");
                    default             -> badge.getStyleClass().add("status-pending");
                }
                setGraphic(badge); setText(null);
            }
        });
    }

    private void setupSelectionListener() {
        productTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                boolean has = newVal != null;
                selectedProduct = newVal;
                btnEdit.setDisable(!has);
                btnDelete.setDisable(!has);
                btnDetail.setDisable(!has);
                btnDetailEdit.setDisable(!has);
                btnDetailDelete.setDisable(!has);
                if (has) showProductDetail(newVal);
                else     showNoSelection();
            }
        );
    }

    // ── Data ─────────────────────────────────────────────────────

    private void loadStats() {
        // TODO: ProductBUS.getStats()
        statTotalProducts.setText("0");
        statCategories.setText("0");
        statInStock.setText("0");
        statLowStock.setText("0");
    }

    private void loadCategoryFilter() {
        // TODO: filterCategory.setItems(FXCollections.observableArrayList(ProductBUS.getAllCategories()))
    }

    private void loadProducts() {
        // TODO: ProductBUS.search(keyword, category, stockStatus, page)
        ObservableList<Object> data = FXCollections.observableArrayList();
        productTable.setItems(data);
        totalLabel.setText("Hiển thị 0 / 0 sản phẩm");
        pageInfo.setText("Hiển thị 0 / 0 sản phẩm");
    }

    private void showProductDetail(Object product) {
        noSelectionHint.setVisible(false);
        noSelectionHint.setManaged(false);
        // TODO: bind từng field với product model
        detailProductName.setText("—");
        detailProductCategory.setText("—");
        detailProdId.setText("—");
        detailProdUnit.setText("—");
        detailProdPrice.setText("—");
        detailProdQty.setText("—");
        detailProdMin.setText("—");
        detailProdSupplier.setText("—");
        detailProdDesc.setText("—");
    }

    private void showNoSelection() {
        noSelectionHint.setVisible(true);
        noSelectionHint.setManaged(true);
    }

    // ── FXML Handlers ────────────────────────────────────────────

    @FXML public void onSearch() {
        String kw  = searchField.getText().trim();
        String cat = filterCategory.getValue();
        String stk = filterStock.getValue();
        // TODO: ProductBUS.search(kw, cat, stk, 0)
        System.out.println("Tìm SP: kw=" + kw + " | cat=" + cat + " | stock=" + stk);
        loadProducts();
    }

    @FXML public void onClearFilter() {
        searchField.clear();
        filterCategory.setValue(null);
        filterStock.setValue(null);
        loadProducts();
    }

    @FXML public void onTableClick(MouseEvent event) {
        if (event.getClickCount() == 2 && selectedProduct != null) {
            openProductForm(selectedProduct);
        }
    }

    @FXML public void onAddProduct(ActionEvent event) {
        openProductForm(null);
    }

    @FXML public void onEditProduct(ActionEvent event) {
        if (selectedProduct == null) return;
        openProductForm(selectedProduct);
    }

    @FXML public void onDeleteProduct(ActionEvent event) {
        if (selectedProduct == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Xóa sản phẩm này khỏi hệ thống?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác Nhận Xóa");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                // TODO: ProductBUS.delete(selectedProduct.getId())
                loadProducts();
                showNoSelection();
            }
        });
    }

    @FXML public void onViewDetail(ActionEvent event) {
        // Đã hiển thị ở panel phải, hoặc mở dialog đầy đủ
        System.out.println("Xem chi tiết sản phẩm...");
    }

    @FXML public void onManageCategory(ActionEvent event) {
        // TODO: mở dialog quản lý danh mục
        System.out.println("Mở quản lý danh mục...");
    }

    // ── Helpers ──────────────────────────────────────────────────

    private void openProductForm(Object product) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/PetHotel/gui/view/ProductForm.fxml")
            );
            Parent root = loader.load();
            Stage dialog = new Stage();
            dialog.setTitle(product == null ? "Thêm Sản Phẩm" : "Sửa Sản Phẩm");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
            loadProducts();
            loadStats();
        } catch (IOException e) {
            System.err.println("Không mở được ProductForm.fxml: " + e.getMessage());
        }
    }
}
