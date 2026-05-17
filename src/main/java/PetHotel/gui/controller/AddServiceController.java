package PetHotel.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import PetHotel.bus.ServiceBUS;
import PetHotel.model.AppUser;
import PetHotel.model.ServiceCategory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

/**
 * AddServiceController — Điều khiển dialog thêm dịch vụ mới
 * 
 * Chỉ dành cho Quản lý chi nhánh
 */
public class AddServiceController {

    @FXML
    private TextField txtServiceName;

    @FXML
    private ComboBox<ServiceCategory> cmbCategory;

    @FXML
    private TextField txtSpecies;

    @FXML
    private TextField txtPrice;

    @FXML
    private TextField txtDuration;

    @FXML
    private Label lblServiceNameError;

    @FXML
    private Label lblCategoryError;

    @FXML
    private Label lblSpeciesError;

    @FXML
    private Label lblPriceError;

    @FXML
    private Label lblDurationError;

    private final ServiceBUS serviceBUS = new ServiceBUS();
    private AppUser currentUser;
    private Consumer<Void> onServiceAdded; // Callback khi thêm thành công

    /**
     * Mở dialog thêm dịch vụ mới
     */
    public static void openAddServiceDialog(Consumer<Void> onAdded) {
        try {
            FXMLLoader loader = new FXMLLoader(
                AddServiceController.class.getResource("/PetHotel/gui/view/AddServiceDialog.fxml")
            );
            VBox root = loader.load();

            AddServiceController controller = loader.getController();
            controller.onServiceAdded = onAdded;

            Stage stage = new Stage();
            stage.setTitle("Thêm Dịch Vụ Mới");
            stage.setScene(new javafx.scene.Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Không thể mở dialog thêm dịch vụ: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser == null) {
            showError("Chưa đăng nhập. Không thể thêm dịch vụ mới.");
            closeWindow();
            return;
        }

        // Tải danh sách categories
        try {
            List<ServiceCategory> categories = serviceBUS.getAllServiceCategories(currentUser);
            ObservableList<ServiceCategory> categoryList = FXCollections.observableArrayList(categories);
            cmbCategory.setItems(categoryList);
            cmbCategory.setCellFactory(param -> new ListCell<ServiceCategory>() {
                @Override
                protected void updateItem(ServiceCategory item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getCategoryName());
                }
            });

            cmbCategory.setButtonCell(new ListCell<ServiceCategory>() {
                @Override
                protected void updateItem(ServiceCategory item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getCategoryName());
                }
            });
        } catch (Exception e) {
            showError("Không thể tải danh sách loại dịch vụ: " + e.getMessage());
        }

        // Clear error messages khi user nhập dữ liệu
        txtServiceName.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                lblServiceNameError.setText("");
            }
        });

        cmbCategory.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                lblCategoryError.setText("");
            }
        });

        txtSpecies.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() <= 50) {
                lblSpeciesError.setText("");
            }
        });

        txtPrice.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                lblPriceError.setText("");
            }
        });

        txtDuration.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                lblDurationError.setText("");
            }
        });
    }

    @FXML
    public void handleSave() {
        // Clear previous errors
        lblServiceNameError.setText("");
        lblCategoryError.setText("");
        lblSpeciesError.setText("");
        lblPriceError.setText("");
        lblDurationError.setText("");

        try {
            // Lấy dữ liệu từ form
            String serviceName = txtServiceName.getText().trim();
            ServiceCategory category = cmbCategory.getValue();
            String species = txtSpecies.getText().trim();
            String priceStr = txtPrice.getText().trim();
            String durationStr = txtDuration.getText().trim();

            // Validate từng trường
            if (serviceName.isEmpty()) {
                lblServiceNameError.setText("Tên dịch vụ không được để trống");
                return;
            }

            if (category == null) {
                lblCategoryError.setText("Loại dịch vụ không được để trống");
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                lblPriceError.setText("Giá dịch vụ phải là số");
                return;
            }

            int duration;
            try {
                duration = Integer.parseInt(durationStr);
            } catch (NumberFormatException e) {
                lblDurationError.setText("Thời gian phải là số");
                return;
            }

            // Gọi BUS để tạo dịch vụ (validate dữ liệu sẽ được thực hiện trong BUS)
            serviceBUS.createNewService(
                serviceName,
                category.getServiceCategoryId(),
                species.isEmpty() ? null : species,
                price,
                duration,
                currentUser
            );

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thành Công");
            alert.setHeaderText(null);
            alert.setContentText("Dịch vụ '" + serviceName + "' đã được thêm thành công!");
            alert.showAndWait();

            // Callback to refresh parent
            if (onServiceAdded != null) {
                onServiceAdded.accept(null);
            }

            closeWindow();

        } catch (Exception e) {
            String errorMsg = e.getMessage();

            // Xác định lỗi từ BUS và hiển thị tương ứng
            if (errorMsg != null && errorMsg.contains("Tên dịch vụ")) {
                lblServiceNameError.setText(errorMsg);
            } else if (errorMsg != null && errorMsg.contains("Loại dịch vụ")) {
                lblCategoryError.setText(errorMsg);
            } else if (errorMsg != null && errorMsg.contains("Loài")) {
                lblSpeciesError.setText(errorMsg);
            } else if (errorMsg != null && errorMsg.contains("Giá")) {
                lblPriceError.setText(errorMsg);
            } else if (errorMsg != null && errorMsg.contains("Thời gian")) {
                lblDurationError.setText(errorMsg);
            } else if (errorMsg != null && errorMsg.contains("quyền")) {
                showError(errorMsg);
            } else {
                showError("Lỗi khi thêm dịch vụ: " + errorMsg);
            }
        }
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }

    /**
     * Đóng cửa sổ
     */
    private void closeWindow() {
        Stage stage = (Stage) txtServiceName.getScene().getWindow();
        stage.close();
    }

    /**
     * Hiển thị thông báo lỗi
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
