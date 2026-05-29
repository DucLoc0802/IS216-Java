package PetHotel.gui.controller;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import PetHotel.bus.InventoryBUS;
import PetHotel.bus.StockAuditBUS;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.model.StockAudit;
import PetHotel.model.StockAuditDetail;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;

public class StockAuditController {
    private static final String ALL = "Tất cả";

    @FXML private ComboBox<String> branchCombo;
    @FXML private ComboBox<String> statusFilter;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private TableView<StockAudit> auditTable;
    @FXML private TableColumn<StockAudit, String> colAuditId;
    @FXML private TableColumn<StockAudit, String> colAuditBranchId;
    @FXML private TableColumn<StockAudit, String> colAuditEmployeeName;
    @FXML private TableColumn<StockAudit, String> colAuditDate;
    @FXML private TableColumn<StockAudit, String> colAuditStatus;
    @FXML private TableColumn<StockAudit, String> colAuditNote;
    @FXML private TableColumn<StockAudit, Void> colAuditActions;
    @FXML private Label totalLabel;

    @FXML private Label formTitle;
    @FXML private ComboBox<String> auditBranchCombo;
    @FXML private TextArea auditNoteArea;
    @FXML private TableView<StockAuditDetail> auditDetailTable;
    @FXML private TableColumn<StockAuditDetail, String> colDetailProductId;
    @FXML private TableColumn<StockAuditDetail, String> colDetailProductName;
    @FXML private TableColumn<StockAuditDetail, String> colDetailUnit;
    @FXML private TableColumn<StockAuditDetail, String> colSystemQuantity;
    @FXML private TableColumn<StockAuditDetail, String> colActualQuantity;
    @FXML private TableColumn<StockAuditDetail, String> colDifferenceQuantity;
    @FXML private TableColumn<StockAuditDetail, String> colDifferenceRate;
    @FXML private TableColumn<StockAuditDetail, String> colDetailNote;
    @FXML private Button btnEditAudit;
    @FXML private Button btnCompleteAudit;
    @FXML private Button btnCancelAudit;

    private final StockAuditBUS stockAuditBUS = new StockAuditBUS();
    private final InventoryBUS inventoryBUS = new InventoryBUS();
    private final ObservableList<StockAuditDetail> detailRows = FXCollections.observableArrayList();

    private AppUser currentUser;
    private StockAudit selectedAudit;
    private StockAudit editingAudit;
    private boolean loadingFilters;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        setupFilters();
        setupAuditTable();
        setupDetailTable();
        resetForm();
        loadAudits();
    }

    private void setupFilters() {
        loadingFilters = true;
        try {
            statusFilter.setItems(FXCollections.observableArrayList(ALL, "DRAFT", "COMPLETED", "CANCELLED"));
            statusFilter.setValue(ALL);
            fromDatePicker.setValue(LocalDate.now().withDayOfMonth(1));
            toDatePicker.setValue(LocalDate.now());

            List<String> branches = inventoryBUS.getBranchIds(currentUser);
            List<String> filterBranches = new ArrayList<>();
            filterBranches.add(ALL);
            filterBranches.addAll(branches);

            branchCombo.setItems(FXCollections.observableArrayList(filterBranches));
            auditBranchCombo.setItems(FXCollections.observableArrayList(branches));

            if (currentUser != null && currentUser.getRole() == Role.BRANCH_MANAGER) {
                String branchId = resolveCurrentBranch();
                branchCombo.setValue(branchId);
                auditBranchCombo.setValue(branchId);
                branchCombo.setDisable(true);
                auditBranchCombo.setDisable(true);
            } else {
                branchCombo.setValue(ALL);
                if (!branches.isEmpty()) {
                    auditBranchCombo.setValue(branches.get(0));
                }
            }
        } catch (Exception e) {
            showError("Không thể tải bộ lọc kiểm kê: " + e.getMessage());
        } finally {
            loadingFilters = false;
        }
    }

    private void setupAuditTable() {
        colAuditId.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getStockAuditId())));
        colAuditBranchId.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getBranchId())));
        colAuditEmployeeName.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getEmployeeName())));
        colAuditDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getAuditDateText()));
        colAuditStatus.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getStatus())));
        colAuditNote.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getNote())));
        colAuditStatus.setCellFactory(column -> new TableCell<>() {
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
                    case "COMPLETED" -> badge.getStyleClass().add("status-instock");
                    case "CANCELLED" -> badge.getStyleClass().add("status-outstock");
                    default -> badge.getStyleClass().add("status-pending");
                }
                setGraphic(badge);
                setText(null);
            }
        });
        colAuditActions.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Sửa");
            private final Button completeButton = new Button("Hoàn tất");
            private final Button cancelButton = new Button("Hủy");
            private final HBox actions = new HBox(6, editButton, completeButton, cancelButton);

            {
                editButton.getStyleClass().addAll("action-btn", "action-btn-outline");
                completeButton.getStyleClass().addAll("action-btn", "action-btn-primary");
                cancelButton.getStyleClass().addAll("action-btn", "action-btn-danger");
                editButton.setOnAction(event -> editAudit(getCurrentAudit()));
                completeButton.setOnAction(event -> completeAudit(getCurrentAudit()));
                cancelButton.setOnAction(event -> cancelAudit(getCurrentAudit()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                StockAudit audit = getCurrentAudit();
                if (empty || audit == null) {
                    setGraphic(null);
                    return;
                }
                boolean draft = "DRAFT".equals(audit.getStatus());
                editButton.setDisable(!draft);
                completeButton.setDisable(!draft);
                cancelButton.setDisable(!draft);
                setGraphic(actions);
            }

            private StockAudit getCurrentAudit() {
                int index = getIndex();
                if (index < 0 || index >= getTableView().getItems().size()) {
                    return null;
                }
                return getTableView().getItems().get(index);
            }
        });

        auditTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selectedAudit = newValue;
            boolean draft = newValue != null && "DRAFT".equals(newValue.getStatus());
            btnEditAudit.setDisable(!draft);
            btnCompleteAudit.setDisable(!draft);
            btnCancelAudit.setDisable(!draft);
        });
    }

    private void setupDetailTable() {
        auditDetailTable.setItems(detailRows);
        auditDetailTable.setEditable(true);
        colDetailProductId.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getProductId())));
        colDetailProductName.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getProductName())));
        colDetailUnit.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getUnit())));
        colSystemQuantity.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSystemQuantityText()));
        colActualQuantity.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getActualQuantityText()));
        colDifferenceQuantity.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDifferenceQuantityText()));
        colDifferenceRate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDifferenceRateText()));
        colDetailNote.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getNote())));

        colActualQuantity.setCellFactory(TextFieldTableCell.forTableColumn());
        colActualQuantity.setOnEditCommit(event -> {
            try {
                event.getRowValue().setActualQuantity(parseDecimal(event.getNewValue(), "Số lượng thực tế"));
                auditDetailTable.refresh();
            } catch (ValidationException e) {
                showError(e.getMessage());
                auditDetailTable.refresh();
            }
        });

        colDetailNote.setCellFactory(TextFieldTableCell.forTableColumn());
        colDetailNote.setOnEditCommit(event -> event.getRowValue().setNote(event.getNewValue()));
    }

    @FXML
    public void onSearch() {
        if (!loadingFilters) {
            loadAudits();
        }
    }

    @FXML
    public void onClearFilter() {
        statusFilter.setValue(ALL);
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        if (currentUser != null && currentUser.getRole() == Role.BRANCH_MANAGER) {
            branchCombo.setValue(resolveCurrentBranch());
        } else {
            branchCombo.setValue(ALL);
        }
        loadAudits();
    }

    @FXML
    public void onCreateAudit() {
        try {
            stockAuditBUS.createAudit(auditBranchCombo.getValue(), auditNoteArea.getText(), currentUser);
            showInfo("Đã tạo phiếu kiểm kê DRAFT từ tồn kho hiện tại.");
            resetForm();
            loadAudits();
        } catch (ValidationException | SQLException e) {
            showError(e.getMessage());
        } catch (RuntimeException e) {
            showError("Không thể tạo phiếu kiểm kê: " + e.getMessage());
        }
    }

    @FXML
    public void onEditAudit() {
        editAudit(selectedAudit);
    }

    @FXML
    public void onSaveDraft() {
        try {
            saveCurrentDraft();
            showInfo("Đã lưu phiếu kiểm kê DRAFT.");
            loadAudits();
        } catch (ValidationException | SQLException e) {
            showError(e.getMessage());
        } catch (RuntimeException e) {
            showError("Không thể lưu phiếu kiểm kê: " + e.getMessage());
        }
    }

    @FXML
    public void onCompleteAudit() {
        completeAudit(editingAudit != null ? editingAudit : selectedAudit);
    }

    @FXML
    public void onCancelAudit() {
        cancelAudit(editingAudit != null ? editingAudit : selectedAudit);
    }

    private void loadAudits() {
        try {
            List<StockAudit> audits = stockAuditBUS.search(
                normalizeCombo(branchCombo.getValue()),
                statusFilter.getValue(),
                fromDatePicker.getValue(),
                toDatePicker.getValue(),
                currentUser
            );
            auditTable.setItems(FXCollections.observableArrayList(audits));
            totalLabel.setText("Hiển thị " + audits.size() + " phiếu kiểm kê");
        } catch (ValidationException | SQLException e) {
            auditTable.setItems(FXCollections.observableArrayList());
            totalLabel.setText("Hiển thị 0 phiếu kiểm kê");
            showError(e.getMessage());
        }
    }

    private void editAudit(StockAudit audit) {
        if (audit == null) {
            return;
        }
        try {
            StockAudit fullAudit = stockAuditBUS.findById(audit.getStockAuditId(), currentUser);
            if (fullAudit == null) {
                showError("Không tìm thấy phiếu kiểm kê.");
                return;
            }
            if (!"DRAFT".equals(fullAudit.getStatus())) {
                showError("Chỉ được sửa phiếu kiểm kê DRAFT.");
                return;
            }
            editingAudit = fullAudit;
            selectedAudit = fullAudit;
            formTitle.setText("Sửa kiểm kê " + fullAudit.getStockAuditId());
            auditBranchCombo.setValue(fullAudit.getBranchId());
            auditBranchCombo.setDisable(true);
            auditNoteArea.setText(valueOrEmpty(fullAudit.getNote()));
            detailRows.setAll(fullAudit.getDetails());
        } catch (ValidationException | SQLException e) {
            showError(e.getMessage());
        }
    }

    private void completeAudit(StockAudit audit) {
        if (audit == null) {
            return;
        }
        if (!confirm("Hoàn tất kiểm kê " + audit.getStockAuditId() + " và cập nhật tồn kho thực tế?")) {
            return;
        }
        try {
            if (editingAudit != null && editingAudit.getStockAuditId().equals(audit.getStockAuditId())) {
                saveCurrentDraft();
            }
            stockAuditBUS.completeAudit(audit.getStockAuditId(), currentUser);
            showInfo("Đã hoàn tất kiểm kê và cập nhật tồn kho.");
            resetForm();
            loadAudits();
        } catch (ValidationException | SQLException e) {
            showError(e.getMessage());
        }
    }

    private void cancelAudit(StockAudit audit) {
        if (audit == null) {
            return;
        }
        if (!confirm("Hủy phiếu kiểm kê " + audit.getStockAuditId() + "?")) {
            return;
        }
        try {
            stockAuditBUS.cancelAudit(audit.getStockAuditId(), currentUser);
            showInfo("Đã hủy phiếu kiểm kê.");
            resetForm();
            loadAudits();
        } catch (ValidationException | SQLException e) {
            showError(e.getMessage());
        }
    }

    private void saveCurrentDraft() throws SQLException {
        if (editingAudit == null) {
            throw new ValidationException("Vui lòng chọn phiếu kiểm kê DRAFT để sửa.");
        }
        StockAudit audit = new StockAudit();
        audit.setStockAuditId(editingAudit.getStockAuditId());
        audit.setNote(auditNoteArea.getText());
        stockAuditBUS.updateDraftAudit(audit, detailRows, currentUser);
    }

    private void resetForm() {
        editingAudit = null;
        formTitle.setText("Tạo phiếu kiểm kê");
        auditNoteArea.clear();
        detailRows.clear();
        if (currentUser != null && currentUser.getRole() == Role.BRANCH_MANAGER) {
            auditBranchCombo.setValue(resolveCurrentBranch());
            auditBranchCombo.setDisable(true);
        } else {
            auditBranchCombo.setDisable(false);
            if (!auditBranchCombo.getItems().isEmpty()) {
                auditBranchCombo.setValue(auditBranchCombo.getItems().get(0));
            }
        }
    }

    private BigDecimal parseDecimal(String rawValue, String fieldName) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            throw new ValidationException(fieldName + " không được rỗng.");
        }
        try {
            BigDecimal value = new BigDecimal(rawValue.trim().replace(",", "."));
            if (value.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException(fieldName + " phải lớn hơn hoặc bằng 0.");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " phải là số hợp lệ.");
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

    private String normalizeCombo(String value) {
        return value == null || value.trim().isEmpty() || ALL.equals(value) ? null : value.trim();
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
}
