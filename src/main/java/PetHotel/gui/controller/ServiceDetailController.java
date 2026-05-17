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
import PetHotel.model.PetService;
import PetHotel.model.ServiceCategory;
import PetHotel.util.Role;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

/**
 * ServiceDetailController — Điều khiển dialog xem/sửa/xóa chi tiết dịch vụ
 * 
 * Chỉ dành cho Quản lý chi nhánh
 */
public class ServiceDetailController {

    @FXML
    private Label lblTitle;

    @FXML
    private TextField txtServiceId;

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

    @FXML
    private Button btnSave;

    @FXML
    private Button btnDelete;

    @FXML
    private VBox formContainer;

    private final ServiceBUS serviceBUS = new ServiceBUS();
    private AppUser currentUser;
    private PetService currentService;
    private boolean isEditMode = false;
    private Consumer<Void> onServiceUpdated; // Callback khi cập nhật thành công

    /**
     * Mở dialog xem chi tiết dịch vụ
     */
    public static void openServiceDetail(PetService service, Consumer<Void> onUpdated) {
        try {
            FXMLLoader loader = new FXMLLoader(
                ServiceDetailController.class.getResource("/PetHotel/gui/view/ServiceDetailDialog.fxml")
            );
            VBox root = loader.load();

            ServiceDetailController controller = loader.getController();
            controller.setService(service);
            controller.onServiceUpdated = onUpdated;

            Stage stage = new Stage();
            stage.setTitle("Chi Tiết Dịch Vụ");
            stage.setScene(new javafx.scene.Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Không thể mở dialog chi tiết dịch vụ: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Set dịch vụ cần xem/sửa
     */
    private void setService(PetService service) {
        this.currentService = service;
        this.currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser == null) {
            showError("Chưa đăng nhập. Không thể xem chi tiết dịch vụ.");
            closeWindow();
            return;
        }

        // Kiểm tra quyền: chỉ Quản lý chi nhánh được phép sửa/xóa
        boolean canEdit = currentUser.hasRole(Role.BRANCH_MANAGER) || currentUser.hasRole(Role.ADMIN);

        // Hiển thị nút Sửa và Xóa nếu người dùng có quyền
        btnSave.setVisible(canEdit);
        btnDelete.setVisible(canEdit);

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

        // Điền dữ liệu từ service hiện tại
        populateFields(service);

        // Disable edit mode mặc định
        setEditMode(false);

        // Clear error messages khi user nhập dữ liệu
        txtServiceName.textProperty().addListener((obs, oldVal, newVal) -> lblServiceNameError.setText(""));
        cmbCategory.valueProperty().addListener((obs, oldVal, newVal) -> lblCategoryError.setText(""));
        txtSpecies.textProperty().addListener((obs, oldVal, newVal) -> lblSpeciesError.setText(""));
        txtPrice.textProperty().addListener((obs, oldVal, newVal) -> lblPriceError.setText(""));
        txtDuration.textProperty().addListener((obs, oldVal, newVal) -> lblDurationError.setText(""));
    }

    /**
     * Điền dữ liệu từ PetService vào các trường input
     */
    private void populateFields(PetService service) {
        txtServiceId.setText(service.getServiceId());
        txtServiceName.setText(service.getServiceName());
        txtSpecies.setText(service.getSpecies() != null ? service.getSpecies() : "");
        txtPrice.setText(String.format("%.0f", service.getBasePrice()));
        txtDuration.setText(String.valueOf(service.getDurationMinutes()));

        // Tìm và set category
        for (ServiceCategory cat : cmbCategory.getItems()) {
            if (cat.getServiceCategoryId().equals(service.getServiceCategoryId())) {
                cmbCategory.setValue(cat);
                break;
            }
        }
    }

    /**
     * Set chế độ edit/view
     */
    private void setEditMode(boolean isEdit) {
        this.isEditMode = isEdit;
        
        txtServiceName.setDisable(!isEdit);
        cmbCategory.setDisable(!isEdit);
        txtSpecies.setDisable(!isEdit);
        txtPrice.setDisable(!isEdit);
        txtDuration.setDisable(!isEdit);

        if (isEdit) {
            lblTitle.setText("Sửa Thông Tin Dịch Vụ");
            btnSave.setText("Lưu Thay Đổi");
        } else {
            lblTitle.setText("Chi Tiết Dịch Vụ");
            btnSave.setText("Sửa Dịch Vụ");
        }
    }

    /**
     * Toggle giữa chế độ view và edit
     */
    @FXML
    public void handleSave() {
        if (!isEditMode) {
            // Chuyển sang chế độ edit
            setEditMode(true);
            btnSave.setText("Lưu Thay Đổi");
            btnDelete.setDisable(false);
        } else {
            // Lưu thay đổi
            saveServiceChanges();
        }
    }

    /**
     * Lưu thay đổi dịch vụ
     */
    private void saveServiceChanges() {
        // Clear error messages
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

            // Validate
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

            // Update service object
            currentService.setServiceName(serviceName);
            currentService.setServiceCategoryId(category.getServiceCategoryId());
            currentService.setSpecies(species.isEmpty() ? null : species);
            currentService.setBasePrice(price);
            currentService.setDurationMinutes(duration);

            // Lưu vào database
            serviceBUS.updateService(currentService, currentUser);

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thành Công");
            alert.setHeaderText(null);
            alert.setContentText("Dịch vụ '" + serviceName + "' đã được cập nhật thành công!");
            alert.showAndWait();

            // Callback to refresh parent
            if (onServiceUpdated != null) {
                onServiceUpdated.accept(null);
            }

            closeWindow();

        } catch (Exception e) {
            showError("Lỗi khi cập nhật dịch vụ: " + e.getMessage());
        }
    }

    /**
     * Xóa dịch vụ
     */
    @FXML
    public void handleDelete() {
        // Confirm delete
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác Nhận Xóa");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Bạn có chắc chắn muốn xóa dịch vụ '" + currentService.getServiceName() + "'?\n\n(Dịch vụ sẽ được đánh dấu là không hoạt động)");

        java.util.Optional<javafx.scene.control.ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            try {
                serviceBUS.deleteService(currentService.getServiceId(), currentUser);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành Công");
                alert.setHeaderText(null);
                alert.setContentText("Dịch vụ '" + currentService.getServiceName() + "' đã được xóa thành công!");
                alert.showAndWait();

                // Callback to refresh parent
                if (onServiceUpdated != null) {
                    onServiceUpdated.accept(null);
                }

                closeWindow();

            } catch (Exception e) {
                showError("Lỗi khi xóa dịch vụ: " + e.getMessage());
            }
        }
    }

    /**
     * Đóng dialog
     */
    @FXML
    public void handleCancel() {
        closeWindow();
    }

    /**
     * Đóng cửa sổ
     */
    private void closeWindow() {
        Stage stage = (Stage) txtServiceId.getScene().getWindow();
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
