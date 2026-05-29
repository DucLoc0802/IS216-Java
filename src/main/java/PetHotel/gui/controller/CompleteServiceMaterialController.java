package PetHotel.gui.controller;

import java.sql.SQLException;
import java.util.List;

import PetHotel.bus.GroomingBUS;
import PetHotel.dao.ServiceProductStandardDAO;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.model.BookingService;
import PetHotel.model.MaterialUsageConfirmRow;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller cho dialog xác nhận hoàn thành dịch vụ grooming với trừ tồn kho.
 */
public class CompleteServiceMaterialController {

    @FXML private Label lblTaskId;
    @FXML private Label lblPetName;
    @FXML private Label lblServiceName;
    @FXML private Label lblSpecies;
    @FXML private Label lblWeight;
    @FXML private Label lblEmployee;

    @FXML private TableView<MaterialUsageConfirmRow> tableMaterials;
    @FXML private TableColumn<MaterialUsageConfirmRow, String> colProductId;
    @FXML private TableColumn<MaterialUsageConfirmRow, String> colProductName;
    @FXML private TableColumn<MaterialUsageConfirmRow, String> colStandard;
    @FXML private TableColumn<MaterialUsageConfirmRow, String> colActualAmount;
    @FXML private TableColumn<MaterialUsageConfirmRow, String> colInventory;
    @FXML private TableColumn<MaterialUsageConfirmRow, String> colNote;

    @FXML private Label lblNoMaterials;
    @FXML private TextArea txtCompletionNote;
    @FXML private Button btnCancel;
    @FXML private Button btnConfirm;

    private BookingService bookingService;
    private AppUser currentUser;
    private Runnable onSuccess;

    private final GroomingBUS groomingBUS = new GroomingBUS();
    private final ServiceProductStandardDAO spsDAO = new ServiceProductStandardDAO();

    /**
     * Mở dialog xác nhận hoàn thành dịch vụ.
     * Static method để tiện sử dụng từ AssignedTasksController.
     *
     * @param bookingService     Thông tin công việc dịch vụ
     * @param currentUser        Người dùng đang đăng nhập
     * @param onSuccess          Callback khi hoàn thành thành công
     */
    public static void openDialog(BookingService bookingService, AppUser currentUser, Runnable onSuccess) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    CompleteServiceMaterialController.class.getResource(
                            "/PetHotel/gui/view/CompleteServiceMaterialDialog.fxml"
                    )
            );

            VBox root = loader.load();

            CompleteServiceMaterialController controller = loader.getController();
            controller.initData(bookingService, currentUser, onSuccess);

            Stage stage = new Stage();
            stage.setTitle("Xác Nhận Hoàn Thành Dịch Vụ");
            stage.setScene(new Scene(root));
            stage.setWidth(800);
            stage.setHeight(600);
            stage.setResizable(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Không thể mở màn hình xác nhận vật tư: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Khởi tạo dữ liệu cho dialog.
     */
    private void initData(BookingService bs, AppUser user, Runnable callback) {
        this.bookingService = bs;
        this.currentUser = user;
        this.onSuccess = callback;

        setupColumns();
        setupButtons();
        displayServiceInfo();
        loadMaterials();
    }

    /**
     * Hiển thị thông tin dịch vụ.
     */
    private void displayServiceInfo() {
        lblTaskId.setText(bookingService.getBookingServiceId());
        lblPetName.setText(valueOrDash(bookingService.getPetName()));
        lblServiceName.setText(valueOrDash(bookingService.getServiceName()));
        lblSpecies.setText(valueOrDash(bookingService.getPetSpecies()));

        if (bookingService instanceof BookingService) {
            // Thử lấy weight từ bookingService nếu có extended field
            // Nếu không có, hiển thị dấu gạch
            lblWeight.setText("—");
        }

        lblEmployee.setText(valueOrDash(bookingService.getEmployeeId()));
    }

    /**
     * Tải danh sách vật tư từ SERVICE_PRODUCT_STANDARD.
     */
    private void loadMaterials() {
        try {
            // Cần lấy branchId từ booking - tạm thời sử dụng logic để lấy từ context
            // Hoặc mở rộng BookingService để chứa branchId
            String branchId = getCurrentBranchId();

            List<MaterialUsageConfirmRow> materials = spsDAO.findStandardsForBookingService(
                    bookingService.getBookingServiceId(),
                    branchId
            );

            if (materials == null || materials.isEmpty()) {
                lblNoMaterials.setVisible(true);
                tableMaterials.setItems(FXCollections.observableArrayList());
            } else {
                lblNoMaterials.setVisible(false);
                tableMaterials.setItems(FXCollections.observableArrayList(materials));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Lỗi khi tải danh sách vật tư: " + e.getMessage());
        }
    }

    /**
     * Setup các cột bảng.
     */
    private void setupColumns() {
        colProductId.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProductId()));

        colProductName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProductName()));

        colStandard.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                    cellData.getValue().getStandardAmount() + " " +
                    cellData.getValue().getStandardUnit()
                ));

        // Cột số lượng thực tế - cho phép chỉnh sửa
        colActualAmount.setCellFactory(col -> new TableCell<MaterialUsageConfirmRow, String>() {
            private javafx.scene.control.TextField textField;

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0) {
                    setGraphic(null);
                    return;
                }

                MaterialUsageConfirmRow row = getTableView().getItems().get(getIndex());
                if (textField == null) {
                    textField = new javafx.scene.control.TextField();
                    textField.setPrefWidth(100);
                    textField.setStyle("-fx-font-size: 11px;");
                    textField.textProperty().addListener((obs, oldVal, newVal) -> {
                        try {
                            if (newVal != null && !newVal.isEmpty()) {
                                row.setActualAmount(new java.math.BigDecimal(newVal));
                            }
                        } catch (NumberFormatException e) {
                            // Ignore invalid input
                        }
                    });
                }

                java.math.BigDecimal amount = row.getActualAmount();
                textField.setText(amount != null ? amount.toPlainString() : "0");
                setGraphic(textField);
            }
        });

        colInventory.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                    cellData.getValue().getInventoryQuantity() != null ?
                        cellData.getValue().getInventoryQuantity().toPlainString() :
                        "0"
                ));

        colNote.setCellValueFactory(cellData ->
                new SimpleStringProperty(valueOrDash(cellData.getValue().getNote())));
    }

    /**
     * Setup các nút.
     */
    private void setupButtons() {
        btnCancel.setOnAction(e -> closeDialog());

        btnConfirm.setOnAction(e -> {
            try {
                confirmCompletion();
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Lỗi: " + ex.getMessage());
            }
        });
    }

    /**
     * Xác nhận hoàn thành dịch vụ.
     */
    private void confirmCompletion() throws ValidationException, SQLException {
        // Lấy danh sách vật tư từ bảng
        List<MaterialUsageConfirmRow> materials = tableMaterials.getItems();

        // Lấy ghi chú bổ sung
        String completionNote = txtCompletionNote.getText();

        // Gọi BUS để xác nhận hoàn thành
        groomingBUS.completeGroomingServiceWithMaterials(
                bookingService.getBookingServiceId(),
                materials,
                completionNote,
                currentUser
        );

        showSuccess("Hoàn thành dịch vụ thành công!\nTồn kho đã được trừ.");

        if (onSuccess != null) {
            onSuccess.run();
        }

        closeDialog();
    }

    /**
     * Đóng dialog.
     */
    private void closeDialog() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    /**
     * Lấy branchId từ booking.
     * Cần mở rộng BookingService hoặc BookingServiceDAO để hỗ trợ điều này.
     */
    private String getCurrentBranchId() {
        // Tạm thời sử dụng SessionManager hoặc giá trị mặc định
        // Bạn nên lấy từ booking.branch_id
        return "BR001"; // Placeholder - cần sửa
    }

    // ── Helper Methods ──────────────────────────────────────────

    private String valueOrDash(Object value) {
        return value == null ? "—" : value.toString();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành Công");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
