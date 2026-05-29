package PetHotel.gui.controller;

import PetHotel.bus.MaterialWasteBUS;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.model.MaterialWaste;
import PetHotel.util.Role;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
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
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MaterialWasteManagementController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Button btnAddWaste;
    @FXML private Button btnApproveWaste;
    @FXML private Button btnRejectWaste;
    @FXML private Label totalLabel;
    @FXML private TableView<MaterialWaste> wasteTable;
    @FXML private TableColumn<MaterialWaste, String> colWasteId;
    @FXML private TableColumn<MaterialWaste, String> colProduct;
    @FXML private TableColumn<MaterialWaste, String> colEmployee;
    @FXML private TableColumn<MaterialWaste, String> colBranch;
    @FXML private TableColumn<MaterialWaste, String> colQuantity;
    @FXML private TableColumn<MaterialWaste, String> colReason;
    @FXML private TableColumn<MaterialWaste, String> colRecordedAt;
    @FXML private TableColumn<MaterialWaste, String> colStatus;
    @FXML private TableColumn<MaterialWaste, Void> colActions;

    private final MaterialWasteBUS materialWasteBUS = new MaterialWasteBUS();
    private AppUser currentUser;
    private MaterialWaste selectedWaste;
    private boolean canReview;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        canReview = currentUser != null
                && (currentUser.getRole() == Role.BRANCH_MANAGER || currentUser.getRole() == Role.ADMIN);
        setupFilters();
        setupColumns();
        setupSelection();
        setupRoleUi();
        loadWastes();
    }

    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList(
                MaterialWaste.STATUS_ALL,
                "Chờ duyệt",
                "Đã duyệt",
                "Đã hủy"
        ));
        statusFilter.setValue(MaterialWaste.STATUS_ALL);
    }

    private void setupColumns() {
        colWasteId.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getMaterialWasteId())));
        colProduct.setCellValueFactory(cell -> new SimpleStringProperty(
                valueOrDash(cell.getValue().getProductName()) + " (" + valueOrDash(cell.getValue().getProductId()) + ")"));
        colEmployee.setCellValueFactory(cell -> new SimpleStringProperty(
                valueOrDash(cell.getValue().getEmployeeName()) + " (" + valueOrDash(cell.getValue().getEmployeeId()) + ")"));
        colBranch.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getBranchId())));
        colQuantity.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getQuantityText()));
        colReason.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().getReason())));
        colRecordedAt.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRecordedAtText()));
        colStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatusText()));
        colStatus.setCellFactory(column -> new TableCell<>() {
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
                if ("Đã duyệt".equals(status)) {
                    badge.getStyleClass().add("status-instock");
                } else if ("Đã hủy".equals(status)) {
                    badge.getStyleClass().add("status-outstock");
                } else {
                    badge.getStyleClass().add("status-pending");
                }
                setText(null);
                setGraphic(badge);
            }
        });

        colActions.setMinWidth(210);
        colActions.setPrefWidth(220);
        colActions.setCellFactory(column -> new TableCell<>() {
            private final Button approveButton = new Button("Duyệt");
            private final Button rejectButton = new Button("Hủy");
            private final HBox actions = new HBox(6, approveButton, rejectButton);

            {
                approveButton.setMinWidth(88);
                approveButton.setPrefWidth(88);
                rejectButton.setMinWidth(78);
                rejectButton.setPrefWidth(78);
                approveButton.getStyleClass().addAll("action-btn", "action-btn-primary");
                rejectButton.getStyleClass().addAll("action-btn", "action-btn-danger");
                approveButton.setOnAction(event -> approveWaste(getCurrentWaste()));
                rejectButton.setOnAction(event -> rejectWaste(getCurrentWaste()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                MaterialWaste waste = getCurrentWaste();
                if (empty || waste == null || !canReview || !MaterialWaste.STATUS_PENDING.equals(waste.getStatus())) {
                    setGraphic(null);
                    return;
                }
                setGraphic(actions);
            }

            private MaterialWaste getCurrentWaste() {
                int index = getIndex();
                if (index < 0 || index >= getTableView().getItems().size()) {
                    return null;
                }
                return getTableView().getItems().get(index);
            }
        });
    }

    private void setupSelection() {
        wasteTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selectedWaste = newValue;
            updateActionButtons();
        });
    }

    private void setupRoleUi() {
        if (!canReview) {
            btnApproveWaste.setVisible(false);
            btnApproveWaste.setManaged(false);
            btnRejectWaste.setVisible(false);
            btnRejectWaste.setManaged(false);
        }
    }

    private void updateActionButtons() {
        boolean pending = selectedWaste != null && MaterialWaste.STATUS_PENDING.equals(selectedWaste.getStatus());
        btnApproveWaste.setDisable(!canReview || !pending);
        btnRejectWaste.setDisable(!canReview || !pending);
    }

    private void loadWastes() {
        try {
            List<MaterialWaste> wastes = materialWasteBUS.getWastes(
                    searchField.getText(),
                    statusFilter.getValue(),
                    currentUser
            );
            wasteTable.setItems(FXCollections.observableArrayList(wastes));
            totalLabel.setText("Hiển thị " + wastes.size() + " phiếu hao hụt");
            updateActionButtons();
        } catch (ValidationException | SQLException e) {
            wasteTable.setItems(FXCollections.observableArrayList());
            totalLabel.setText("Hiển thị 0 phiếu hao hụt");
            showError(e.getMessage());
        }
    }

    @FXML
    public void onSearch() {
        loadWastes();
    }

    @FXML
    public void onClearFilter() {
        searchField.clear();
        statusFilter.setValue(MaterialWaste.STATUS_ALL);
        loadWastes();
    }

    @FXML
    public void onAddWaste() {
        MaterialWasteController.open(null, null, this::loadWastes);
    }

    @FXML
    public void onApproveWaste() {
        approveWaste(selectedWaste);
    }

    @FXML
    public void onRejectWaste() {
        rejectWaste(selectedWaste);
    }

    private void approveWaste(MaterialWaste waste) {
        if (waste == null) {
            return;
        }
        if (!confirm("Duyệt phiếu hao hụt " + waste.getMaterialWasteId() + " và trừ tồn kho?")) {
            return;
        }
        try {
            materialWasteBUS.approveWaste(waste.getMaterialWasteId(), askManagerNote("Ghi chú duyệt phiếu"), currentUser);
            showInfo("Đã duyệt phiếu hao hụt và trừ tồn kho.");
            loadWastes();
        } catch (ValidationException | SQLException e) {
            showError(e.getMessage());
        }
    }

    private void rejectWaste(MaterialWaste waste) {
        if (waste == null) {
            return;
        }
        if (!confirm("Hủy phiếu hao hụt " + waste.getMaterialWasteId() + "?")) {
            return;
        }
        try {
            materialWasteBUS.rejectWaste(waste.getMaterialWasteId(), askManagerNote("Lý do hủy phiếu"), currentUser);
            showInfo("Đã hủy phiếu hao hụt.");
            loadWastes();
        } catch (ValidationException | SQLException e) {
            showError(e.getMessage());
        }
    }

    private String askManagerNote(String title) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(title + " (không bắt buộc):");
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    private void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).showAndWait();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message == null ? "Có lỗi xảy ra." : message, ButtonType.OK).showAndWait();
    }
}
