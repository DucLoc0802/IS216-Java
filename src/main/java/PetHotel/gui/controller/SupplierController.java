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
 * SupplierController — Quản Lý Nhà Cung Cấp
 * ─────────────────────────────────────────────────────────────────
 * Xử lý: SupplierManagement.fxml
 * Use cases:
 *   - Tra cứu / lọc nhà cung cấp
 *   - Thêm / Sửa / Xóa / Ngưng hợp tác
 *   - Xem sản phẩm cung cấp
 *   - Xem lịch sử nhập hàng từ NCC
 */
public class SupplierController {

    // ── Filter ───────────────────────────────────────────────────
    @FXML private TextField         searchField;
    @FXML private ComboBox<String>  filterStatus;

    // ── Toolbar ──────────────────────────────────────────────────
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnDeactivate;
    @FXML private Button btnHistory;
    @FXML private Label  totalLabel;

    // ── Table ────────────────────────────────────────────────────
    @FXML private TableView<Object>          supplierTable;
    @FXML private TableColumn<Object,String> colSupId;
    @FXML private TableColumn<Object,String> colSupName;
    @FXML private TableColumn<Object,String> colSupContact;
    @FXML private TableColumn<Object,String> colSupPhone;
    @FXML private TableColumn<Object,String> colSupEmail;
    @FXML private TableColumn<Object,String> colSupProducts;
    @FXML private TableColumn<Object,String> colSupStatus;
    @FXML private TableColumn<Object,String> colSupActions;

    @FXML private Pagination pagination;
    @FXML private Label      pageInfo;

    // ── Detail panel ─────────────────────────────────────────────
    @FXML private VBox   noSelectionHint;
    @FXML private Label  detailSupIcon;
    @FXML private Label  detailSupName;
    @FXML private Label  detailSupStatus;
    @FXML private Label  detailSupId;
    @FXML private Label  detailSupContact;
    @FXML private Label  detailSupPhone;
    @FXML private Label  detailSupEmail;
    @FXML private Label  detailSupAddress;
    @FXML private Label  detailSupBank;
    @FXML private Label  detailSupNote;
    @FXML private Button btnDetailEdit;
    @FXML private Button btnDetailHistory;
    @FXML private VBox   supplierProductList;
    @FXML private Label  supProdEmptyHint;

    // ── State ────────────────────────────────────────────────────
    private Object selectedSupplier = null;

    // ── Lifecycle ────────────────────────────────────────────────

    @FXML
    public void initialize() {
        setupColumns();
        setupSelectionListener();
        loadSuppliers();
        showNoSelection();
    }

    // ── Setup ────────────────────────────────────────────────────

    private void setupColumns() {
        colSupStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) { setGraphic(null); return; }
                Label badge = new Label(value);
                badge.getStyleClass().add("status-badge");
                badge.getStyleClass().add(
                    value.equals("Đang hợp tác") ? "status-active" : "status-locked"
                );
                setGraphic(badge); setText(null);
            }
        });
    }

    private void setupSelectionListener() {
        supplierTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                boolean has = newVal != null;
                selectedSupplier = newVal;
                btnEdit.setDisable(!has);
                btnDelete.setDisable(!has);
                btnDeactivate.setDisable(!has);
                btnHistory.setDisable(!has);
                btnDetailEdit.setDisable(!has);
                btnDetailHistory.setDisable(!has);
                if (has) showSupplierDetail(newVal);
                else     showNoSelection();
            }
        );
    }

    // ── Data ─────────────────────────────────────────────────────

    private void loadSuppliers() {
        // TODO: SupplierBUS.search(keyword, status, page)
        ObservableList<Object> data = FXCollections.observableArrayList();
        supplierTable.setItems(data);
        totalLabel.setText("Hiển thị 0 / 0 nhà cung cấp");
        pageInfo.setText("Hiển thị 0 / 0 nhà cung cấp");
    }

    private void showSupplierDetail(Object supplier) {
        noSelectionHint.setVisible(false);
        noSelectionHint.setManaged(false);
        // TODO: bind từng label với supplier model
        detailSupName.setText("—");
        detailSupId.setText("—");
        detailSupContact.setText("—");
        detailSupPhone.setText("—");
        detailSupEmail.setText("—");
        detailSupAddress.setText("—");
        detailSupBank.setText("—");
        detailSupNote.setText("—");
        loadSupplierProducts();
    }

    private void showNoSelection() {
        noSelectionHint.setVisible(true);
        noSelectionHint.setManaged(true);
    }

    private void loadSupplierProducts() {
        // TODO: SupplierBUS.getProductsBySupplier(selectedSupplier.getId())
        supplierProductList.getChildren().clear();
        supProdEmptyHint.setVisible(true);
    }

    // ── FXML Handlers ────────────────────────────────────────────

    @FXML public void onSearch() {
        // TODO: SupplierBUS.search(searchField.getText(), filterStatus.getValue(), 0)
        loadSuppliers();
    }

    @FXML public void onClearFilter() {
        searchField.clear();
        filterStatus.setValue(null);
        loadSuppliers();
    }

    @FXML public void onTableClick(MouseEvent event) {
        if (event.getClickCount() == 2 && selectedSupplier != null) {
            openSupplierForm(selectedSupplier);
        }
    }

    @FXML public void onAddSupplier(ActionEvent event) {
        openSupplierForm(null);
    }

    @FXML public void onEditSupplier(ActionEvent event) {
        if (selectedSupplier == null) return;
        openSupplierForm(selectedSupplier);
    }

    @FXML public void onDeleteSupplier(ActionEvent event) {
        if (selectedSupplier == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Xóa nhà cung cấp này?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác Nhận Xóa"); confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                // TODO: SupplierBUS.delete(selectedSupplier.getId())
                loadSuppliers(); showNoSelection();
            }
        });
    }

    @FXML public void onDeactivate(ActionEvent event) {
        if (selectedSupplier == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Ngưng hợp tác với nhà cung cấp này?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác Nhận"); confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                // TODO: SupplierBUS.deactivate(selectedSupplier.getId())
                loadSuppliers();
            }
        });
    }

    @FXML public void onViewHistory(ActionEvent event) {
        if (selectedSupplier == null) return;
        // TODO: mở dialog lịch sử nhập hàng từ NCC này
        System.out.println("Xem lịch sử nhập hàng từ nhà cung cấp...");
    }

    // ── Helpers ──────────────────────────────────────────────────

    private void openSupplierForm(Object supplier) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/PetHotel/gui/view/SupplierForm.fxml")
            );
            Parent root = loader.load();
            Stage dialog = new Stage();
            dialog.setTitle(supplier == null ? "Thêm Nhà Cung Cấp" : "Sửa Nhà Cung Cấp");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
            loadSuppliers();
        } catch (IOException e) {
            System.err.println("Không mở được SupplierForm.fxml: " + e.getMessage());
        }
    }
}
