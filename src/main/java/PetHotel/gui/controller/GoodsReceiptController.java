package PetHotel.gui.controller;

import PetHotel.bus.GoodsReceiptBUS;
import PetHotel.bus.InventoryBUS;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.model.GoodsReceipt;
import PetHotel.model.GoodsReceiptDetail;
import PetHotel.model.Product;
import PetHotel.util.Role;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class GoodsReceiptController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private TableView<GoodsReceipt> receiptTable;
    @FXML private TableColumn<GoodsReceipt, String> colReceiptId;
    @FXML private TableColumn<GoodsReceipt, String> colBranchId;
    @FXML private TableColumn<GoodsReceipt, String> colEmployeeName;
    @FXML private TableColumn<GoodsReceipt, String> colSupplierName;
    @FXML private TableColumn<GoodsReceipt, String> colReceiptDate;
    @FXML private TableColumn<GoodsReceipt, String> colTotalQuantity;
    @FXML private TableColumn<GoodsReceipt, String> colTotalItemCount;
    @FXML private TableColumn<GoodsReceipt, String> colStatus;
    @FXML private TableColumn<GoodsReceipt, Void> colActions;
    @FXML private Label totalLabel;

    @FXML private Label formTitle;
    @FXML private ComboBox<String> branchCombo;
    @FXML private TextField supplierField;
    @FXML private DatePicker receiptDatePicker;
    @FXML private TextArea receiptNoteArea;
    @FXML private ComboBox<Product> productCombo;
    @FXML private TextField quantityField;
    @FXML private TextField unitField;
    @FXML private TextField lineTotalField;
    @FXML private TextField detailNoteField;
    @FXML private TableView<GoodsReceiptDetail> detailTable;
    @FXML private TableColumn<GoodsReceiptDetail, String> colDetailProduct;
    @FXML private TableColumn<GoodsReceiptDetail, String> colDetailQuantity;
    @FXML private TableColumn<GoodsReceiptDetail, String> colDetailUnit;
    @FXML private TableColumn<GoodsReceiptDetail, String> colDetailLineTotal;
    @FXML private TableColumn<GoodsReceiptDetail, String> colDetailNote;
    @FXML private Button btnEditReceipt;
    @FXML private Button btnApproveReceipt;
    @FXML private Button btnCancelReceipt;

    private final GoodsReceiptBUS goodsReceiptBUS = new GoodsReceiptBUS();
    private final InventoryBUS inventoryBUS = new InventoryBUS();
    private final ObservableList<GoodsReceiptDetail> detailRows = FXCollections.observableArrayList();

    private AppUser currentUser;
    private GoodsReceipt selectedReceipt;
    private GoodsReceipt editingReceipt;
    private boolean loadingFilters;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        setupFilters();
        setupProductCombo();
        setupReceiptTable();
        setupDetailTable();
        resetForm();
        loadReceipts();
    }

    private void setupFilters() {
        loadingFilters = true;
        try {
            statusFilter.setItems(FXCollections.observableArrayList("Tất cả", "DRAFT", "APPROVED", "CANCELLED"));
            statusFilter.setValue("Tất cả");
            fromDatePicker.setValue(LocalDate.now().withDayOfMonth(1));
            toDatePicker.setValue(LocalDate.now());

            List<String> branches = inventoryBUS.getBranchIds(currentUser);
            branchCombo.setItems(FXCollections.observableArrayList(branches));
            if (currentUser != null && currentUser.getRole() == Role.BRANCH_MANAGER) {
                branchCombo.setValue(resolveCurrentBranch());
                branchCombo.setDisable(true);
            } else if (!branches.isEmpty()) {
                branchCombo.setValue(branches.get(0));
            }
        } catch (Exception e) {
            showError("Không thể tải bộ lọc nhập kho: " + e.getMessage());
        } finally {
            loadingFilters = false;
        }
    }

    private void setupProductCombo() {
        productCombo.setCellFactory(param -> new ProductCell());
        productCombo.setButtonCell(new ProductCell());
        productCombo.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                unitField.setText(newValue.getUnit());
            }
            refreshLineTotal();
        });
        quantityField.textProperty().addListener((obs, oldValue, newValue) -> refreshLineTotal());
        try {
            productCombo.setItems(FXCollections.observableArrayList(inventoryBUS.getProducts(currentUser)));
        } catch (Exception e) {
            showError("Không thể tải danh sách sản phẩm: " + e.getMessage());
        }
    }

    private void setupReceiptTable() {
        colReceiptId.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getGoodsReceiptId())));
        colBranchId.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getBranchId())));
        colEmployeeName.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getEmployeeName())));
        colSupplierName.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getSupplierName())));
        colReceiptDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getReceiptDateText()));
        colTotalQuantity.setCellValueFactory(cell -> new SimpleStringProperty(numberText(cell.getValue().getTotalQuantity())));
        colTotalItemCount.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getTotalItemCount())));
        colStatus.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getStatus())));
        colActions.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Sửa");
            private final Button approveButton = new Button("Duyệt");
            private final Button cancelButton = new Button("Hủy");
            private final HBox actions = new HBox(6, editButton, approveButton, cancelButton);

            {
                editButton.getStyleClass().addAll("action-btn", "action-btn-outline");
                approveButton.getStyleClass().addAll("action-btn", "action-btn-primary");
                cancelButton.getStyleClass().addAll("action-btn", "action-btn-danger");
                editButton.setOnAction(event -> editReceipt(getCurrentReceipt()));
                approveButton.setOnAction(event -> approveReceipt(getCurrentReceipt()));
                cancelButton.setOnAction(event -> cancelReceipt(getCurrentReceipt()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                GoodsReceipt receipt = getCurrentReceipt();
                if (empty || receipt == null) {
                    setGraphic(null);
                    return;
                }
                boolean draft = "DRAFT".equals(receipt.getStatus());
                editButton.setDisable(!draft);
                approveButton.setDisable(!draft);
                cancelButton.setDisable(!draft);
                setGraphic(actions);
            }

            private GoodsReceipt getCurrentReceipt() {
                int index = getIndex();
                if (index < 0 || index >= getTableView().getItems().size()) {
                    return null;
                }
                return getTableView().getItems().get(index);
            }
        });

        receiptTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selectedReceipt = newValue;
            boolean draft = newValue != null && "DRAFT".equals(newValue.getStatus());
            btnEditReceipt.setDisable(!draft);
            btnApproveReceipt.setDisable(!draft);
            btnCancelReceipt.setDisable(!draft);
        });
    }

    private void setupDetailTable() {
        detailTable.setItems(detailRows);
        colDetailProduct.setCellValueFactory(cell -> new SimpleStringProperty(
            valueOrDash(cell.getValue().getProductName()) + " (" + valueOrDash(cell.getValue().getProductId()) + ")"));
        colDetailQuantity.setCellValueFactory(cell -> new SimpleStringProperty(numberText(cell.getValue().getQuantity())));
        colDetailUnit.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getUnit())));
        colDetailLineTotal.setCellValueFactory(cell -> new SimpleStringProperty(numberText(cell.getValue().getLineTotal())));
        colDetailNote.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getNote())));
    }

    @FXML
    public void onSearch() {
        if (!loadingFilters) {
            loadReceipts();
        }
    }

    @FXML
    public void onClearFilter() {
        searchField.clear();
        statusFilter.setValue("Tất cả");
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        loadReceipts();
    }

    @FXML
    public void onNewReceipt() {
        resetForm();
    }

    @FXML
    public void onEditReceipt() {
        editReceipt(selectedReceipt);
    }

    @FXML
    public void onApproveReceipt() {
        approveReceipt(selectedReceipt);
    }

    @FXML
    public void onCancelReceipt() {
        cancelReceipt(selectedReceipt);
    }

    @FXML
    public void onAddDetailLine() {
        try {
            Product product = productCombo.getValue();
            if (product == null) {
                throw new ValidationException("Vui lòng chọn sản phẩm.");
            }
            GoodsReceiptDetail detail = new GoodsReceiptDetail();
            detail.setProductId(product.getProductId());
            detail.setProductName(product.getProductName());
            BigDecimal quantity = parseDecimal(quantityField.getText(), "Số lượng");
            detail.setQuantity(quantity);
            detail.setUnit(unitField.getText());
            detail.setLineTotal(resolveLineTotal(product, quantity));
            detail.setNote(detailNoteField.getText());
            detailRows.add(detail);

            productCombo.setValue(null);
            quantityField.clear();
            unitField.clear();
            lineTotalField.clear();
            detailNoteField.clear();
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void onRemoveDetailLine() {
        GoodsReceiptDetail selected = detailTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            detailRows.remove(selected);
        }
    }

    @FXML
    public void onSaveDraft() {
        try {
            GoodsReceipt receipt = buildReceiptFromForm();
            if (editingReceipt == null) {
                goodsReceiptBUS.createDraftReceipt(receipt, detailRows, currentUser);
            } else {
                goodsReceiptBUS.updateDraftReceipt(receipt, detailRows, currentUser);
            }
            showInfo("Đã lưu phiếu nhập DRAFT.");
            resetForm();
            loadReceipts();
        } catch (ValidationException | SQLException e) {
            showError(e.getMessage());
        } catch (RuntimeException e) {
            showError("Không thể lưu phiếu nhập: " + e.getMessage());
        }
    }

    @FXML
    public void onSaveAndApprove() {
        try {
            GoodsReceipt receipt = buildReceiptFromForm();
            GoodsReceipt saved;
            if (editingReceipt == null) {
                saved = goodsReceiptBUS.createDraftReceipt(receipt, detailRows, currentUser);
            } else {
                goodsReceiptBUS.updateDraftReceipt(receipt, detailRows, currentUser);
                saved = receipt;
            }
            goodsReceiptBUS.approveReceipt(saved.getGoodsReceiptId(), currentUser);
            showInfo("Đã duyệt phiếu nhập và cập nhật tồn kho.");
            resetForm();
            loadReceipts();
        } catch (ValidationException | SQLException e) {
            showError(e.getMessage());
        } catch (RuntimeException e) {
            showError("Không thể duyệt phiếu nhập: " + e.getMessage());
        }
    }

    private void loadReceipts() {
        try {
            List<GoodsReceipt> receipts = goodsReceiptBUS.search(
                searchField.getText(),
                statusFilter.getValue(),
                fromDatePicker.getValue(),
                toDatePicker.getValue(),
                currentUser
            );
            receiptTable.setItems(FXCollections.observableArrayList(receipts));
            totalLabel.setText("Hiển thị " + receipts.size() + " phiếu nhập");
        } catch (ValidationException | SQLException e) {
            receiptTable.setItems(FXCollections.observableArrayList());
            totalLabel.setText("Hiển thị 0 phiếu nhập");
            showError(e.getMessage());
        }
    }

    private void editReceipt(GoodsReceipt receipt) {
        if (receipt == null) {
            return;
        }
        try {
            GoodsReceipt fullReceipt = goodsReceiptBUS.findById(receipt.getGoodsReceiptId(), currentUser);
            if (fullReceipt == null) {
                showError("Không tìm thấy phiếu nhập.");
                return;
            }
            if (!"DRAFT".equals(fullReceipt.getStatus())) {
                showError("Chỉ được sửa phiếu nhập DRAFT.");
                return;
            }
            editingReceipt = fullReceipt;
            formTitle.setText("Sửa phiếu nhập " + fullReceipt.getGoodsReceiptId());
            branchCombo.setValue(fullReceipt.getBranchId());
            supplierField.setText(valueOrEmpty(fullReceipt.getSupplierName()));
            receiptDatePicker.setValue(fullReceipt.getReceiptDate());
            receiptNoteArea.setText(valueOrEmpty(fullReceipt.getNote()));
            detailRows.setAll(fullReceipt.getDetails());
        } catch (ValidationException | SQLException e) {
            showError(e.getMessage());
        }
    }

    private void approveReceipt(GoodsReceipt receipt) {
        if (receipt == null) {
            return;
        }
        if (!confirm("Duyệt phiếu nhập " + receipt.getGoodsReceiptId() + " và tăng tồn kho?")) {
            return;
        }
        try {
            goodsReceiptBUS.approveReceipt(receipt.getGoodsReceiptId(), currentUser);
            showInfo("Đã duyệt phiếu nhập.");
            loadReceipts();
        } catch (ValidationException | SQLException e) {
            showError(e.getMessage());
        }
    }

    private void cancelReceipt(GoodsReceipt receipt) {
        if (receipt == null) {
            return;
        }
        if (!confirm("Hủy phiếu nhập " + receipt.getGoodsReceiptId() + "?")) {
            return;
        }
        try {
            goodsReceiptBUS.cancelReceipt(receipt.getGoodsReceiptId(), currentUser);
            showInfo("Đã hủy phiếu nhập.");
            resetForm();
            loadReceipts();
        } catch (ValidationException | SQLException e) {
            showError(e.getMessage());
        }
    }

    private GoodsReceipt buildReceiptFromForm() {
        GoodsReceipt receipt = new GoodsReceipt();
        if (editingReceipt != null) {
            receipt.setGoodsReceiptId(editingReceipt.getGoodsReceiptId());
        }
        receipt.setBranchId(branchCombo.getValue());
        receipt.setSupplierName(supplierField.getText());
        receipt.setReceiptDate(receiptDatePicker.getValue());
        receipt.setNote(receiptNoteArea.getText());
        return receipt;
    }

    private void resetForm() {
        editingReceipt = null;
        formTitle.setText("Lập phiếu nhập");
        if (currentUser != null && currentUser.getRole() == Role.BRANCH_MANAGER) {
            branchCombo.setValue(resolveCurrentBranch());
        }
        supplierField.clear();
        receiptDatePicker.setValue(LocalDate.now());
        receiptNoteArea.clear();
        detailRows.clear();
        productCombo.setValue(null);
        quantityField.clear();
        unitField.clear();
        lineTotalField.clear();
        detailNoteField.clear();
    }

    private BigDecimal parseDecimal(String rawValue, String fieldName) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            throw new ValidationException(fieldName + " không được rỗng.");
        }
        try {
            return new BigDecimal(rawValue.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " phải là số hợp lệ.");
        }
    }

    private BigDecimal parseOptionalDecimal(String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return parseDecimal(rawValue, "Thành tiền");
    }

    private BigDecimal resolveLineTotal(Product product, BigDecimal quantity) {
        BigDecimal enteredLineTotal = parseOptionalDecimal(lineTotalField.getText());
        if (enteredLineTotal.compareTo(BigDecimal.ZERO) > 0) {
            return enteredLineTotal;
        }
        return calculateLineTotal(product, quantity);
    }

    private void refreshLineTotal() {
        Product product = productCombo.getValue();
        if (product == null || quantityField.getText() == null || quantityField.getText().trim().isEmpty()) {
            lineTotalField.clear();
            return;
        }

        try {
            BigDecimal quantity = parseDecimal(quantityField.getText(), "Số lượng");
            if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                lineTotalField.clear();
                return;
            }
            lineTotalField.setText(numberText(calculateLineTotal(product, quantity)));
        } catch (ValidationException e) {
            lineTotalField.clear();
        }
    }

    private BigDecimal calculateLineTotal(Product product, BigDecimal quantity) {
        BigDecimal costPrice = product == null || product.getCostPrice() == null
            ? BigDecimal.ZERO
            : product.getCostPrice();
        BigDecimal safeQuantity = quantity == null ? BigDecimal.ZERO : quantity;
        return costPrice.multiply(safeQuantity);
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

    private String numberText(BigDecimal value) {
        BigDecimal normalized = (value == null ? BigDecimal.ZERO : value).stripTrailingZeros();
        return normalized.scale() <= 0 ? normalized.toPlainString() : normalized.toPlainString();
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    private void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).showAndWait();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message == null ? "Có lỗi xảy ra." : message, ButtonType.OK).showAndWait();
    }

    private static class ProductCell extends ListCell<Product> {
        @Override
        protected void updateItem(Product item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? null : item.getProductName() + " (" + item.getProductId() + ")");
        }
    }
}
