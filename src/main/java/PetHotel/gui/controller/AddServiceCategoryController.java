package PetHotel.gui.controller;

import java.util.function.Consumer;

import PetHotel.bus.ServiceBUS;
import PetHotel.model.AppUser;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * AddServiceCategoryController — Điều khiển dialog thêm loại dịch vụ mới
 * 
 * Chỉ dành cho Quản lý chi nhánh
 */
public class AddServiceCategoryController {

    @FXML
    private TextField txtCategoryName;

    @FXML
    private TextArea txtNote;

    @FXML
    private Label lblCategoryNameError;

    @FXML
    private Label lblNoteError;

    private final ServiceBUS serviceBUS = new ServiceBUS();
    private AppUser currentUser;
    private Consumer<String> onCategoryAdded; // Callback khi thêm thành công

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser == null) {
            showError("Chưa đăng nhập. Không thể thêm loại dịch vụ mới.");
            closeWindow();
            return;
        }

        // Clear error messages khi user nhập dữ liệu
        txtCategoryName.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                lblCategoryNameError.setText("");
            }
        });

        txtNote.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() <= 4000) {
                lblNoteError.setText("");
            }
        });
    }

    @FXML
    public void handleSave() {
        // Clear previous errors
        lblCategoryNameError.setText("");
        lblNoteError.setText("");

        try {
            String categoryName = txtCategoryName.getText();
            String note = txtNote.getText();

            // Validate & create
            serviceBUS.createNewServiceCategory(categoryName, note, currentUser);

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thành Công");
            alert.setHeaderText(null);
            alert.setContentText("Loại dịch vụ '" + categoryName + "' đã được thêm thành công!");
            alert.showAndWait();

            // Callback to refresh parent
            if (onCategoryAdded != null) {
                onCategoryAdded.accept(categoryName);
            }

            closeWindow();

        } catch (Exception e) {
            String errorMsg = e.getMessage();

            // Determine which field has error
            if (errorMsg != null && errorMsg.contains("Tên loại dịch vụ")) {
                lblCategoryNameError.setText(errorMsg);
            } else if (errorMsg != null && errorMsg.contains("Ghi chú")) {
                lblNoteError.setText(errorMsg);
            } else {
                showError("Lỗi khi thêm loại dịch vụ: " + errorMsg);
            }
        }
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }

    /**
     * Set callback để gọi khi thêm thành công
     */
    public void setOnCategoryAdded(Consumer<String> callback) {
        this.onCategoryAdded = callback;
    }

    private void closeWindow() {
        Stage stage = (Stage) txtCategoryName.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
