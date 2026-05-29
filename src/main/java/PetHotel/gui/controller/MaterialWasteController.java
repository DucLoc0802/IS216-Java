package PetHotel.gui.controller;

import PetHotel.bus.InventoryBUS;
import PetHotel.model.AppUser;
import PetHotel.model.BookingService;
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

public class MaterialWasteController {
    @FXML private Label lblContext;
    @FXML private ComboBox<Product> cmbProduct;
    @FXML private Label lblAvailableQty;
    @FXML private TextField txtQuantity;
    @FXML private ComboBox<String> cmbReason;
    @FXML private TextArea txtNote;
    @FXML private Label lblProductError;
    @FXML private Label lblQuantityError;
    @FXML private Label lblReasonError;
    @FXML private Label lblGeneralError;

    private final InventoryBUS inventoryBUS = new InventoryBUS();
    private AppUser currentUser;
    private Runnable onSaved;
    private String preselectedProductId;
    private BookingService taskContext;

    public static void open(InventoryItem item, BookingService task, Runnable onSaved) {
        openInternal(item == null ? null : item.getProductId(), task, onSaved);
    }

    public static void openForTask(BookingService task, Runnable onSaved) {
        openInternal(null, task, onSaved);
    }

    private static void openInternal(String productId, BookingService task, Runnable onSaved) {
        try {
            FXMLLoader loader = new FXMLLoader(
                MaterialWasteController.class.getResource("/PetHotel/gui/view/MaterialWasteForm.fxml")
            );
            VBox root = loader.load();
            MaterialWasteController controller = loader.getController();
            controller.onSaved = onSaved;
            controller.taskContext = task;
            controller.setContext(task);
            controller.preselectedProductId = productId;
            controller.selectProduct(productId);

            Stage stage = new Stage();
            stage.setTitle("Ghi Nhận Tiêu Hao Vật Tư");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Không thể mở form tiêu hao vật tư: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        setupProductCombo();
        cmbReason.setEditable(true);
        cmbReason.setItems(FXCollections.observableArrayList(
            "Tiêu hao theo dịch vụ grooming",
            "Vật tư hư hỏng",
            "Vật tư hết hạn",
            "Sai lệch nhỏ khi sử dụng"
        ));
        cmbReason.setValue("Tiêu hao theo dịch vụ grooming");
        loadProducts();
        clearErrorsOnInput();
    }

    private void setContext(BookingService task) {
        if (task == null) {
            lblContext.setText("Ghi nhận vật tư đã dùng và trừ tồn kho thực tế.");
            return;
        }
        lblContext.setText("Công việc " + task.getBookingServiceId()
            + " · " + valueOrDash(task.getServiceName())
            + " · " + valueOrDash(task.getPetName()));
        txtNote.setText("Công việc: " + task.getBookingServiceId()
            + " - " + valueOrDash(task.getServiceName()));
    }

    private void setupProductCombo() {
        cmbProduct.setCellFactory(param -> new ProductCell());
        cmbProduct.setButtonCell(new ProductCell());
        cmbProduct.valueProperty().addListener((obs, oldValue, newValue) -> {
            lblProductError.setText("");
            loadAvailableQty(newValue);
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

    private void loadAvailableQty(Product product) {
        if (product == null) {
            lblAvailableQty.setText("—");
            return;
        }
        try {
            InventoryItem item = inventoryBUS.getInventoryItem(resolveBranchId(), product.getProductId(), currentUser);
            lblAvailableQty.setText(item == null ? "0 " + product.getUnit() : item.getStockWithUnit());
        } catch (Exception e) {
            lblAvailableQty.setText("—");
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
            BigDecimal quantity = parseQuantity(txtQuantity.getText());
            String reason = cmbReason.getEditor().getText();
            String note = buildNote();

            inventoryBUS.recordMaterialWaste(
                resolveBranchId(),
                product.getProductId(),
                quantity,
                reason,
                note,
                currentUser
            );

            new Alert(Alert.AlertType.INFORMATION, "Đã ghi nhận tiêu hao và trừ tồn kho.", ButtonType.OK).showAndWait();
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
            lblQuantityError.setText("Vui lòng nhập số lượng tiêu hao.");
            throw new IllegalArgumentException("Vui lòng nhập số lượng tiêu hao.");
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            lblQuantityError.setText("Số lượng tiêu hao phải là số.");
            throw new IllegalArgumentException("Số lượng tiêu hao phải là số.");
        }
    }

    private String buildNote() {
        String note = txtNote.getText() == null ? "" : txtNote.getText().trim();
        if (taskContext == null) {
            return note;
        }
        String context = "booking_service_id=" + taskContext.getBookingServiceId();
        return note.isEmpty() ? context : note + "\n" + context;
    }

    private void showFormError(String message) {
        String error = message == null ? "Dữ liệu không hợp lệ." : message;
        if (error.contains("vật tư") || error.contains("Vui lòng chọn")) {
            lblProductError.setText(error);
        } else if (error.contains("Số lượng")) {
            lblQuantityError.setText(error);
        } else if (error.contains("lý do") || error.contains("Lý do")) {
            lblReasonError.setText(error);
        } else {
            lblGeneralError.setText(error);
        }
    }

    private void clearErrorsOnInput() {
        txtQuantity.textProperty().addListener((obs, oldValue, newValue) -> lblQuantityError.setText(""));
        cmbReason.getEditor().textProperty().addListener((obs, oldValue, newValue) -> lblReasonError.setText(""));
    }

    private void clearErrors() {
        lblProductError.setText("");
        lblQuantityError.setText("");
        lblReasonError.setText("");
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

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "—" : value;
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
            setText(empty || item == null ? null : item.getProductName() + " (" + item.getProductId() + ")");
        }
    }
}
