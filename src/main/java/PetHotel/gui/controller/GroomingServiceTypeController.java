package PetHotel.gui.controller;

import PetHotel.bus.GroomingBUS;
import PetHotel.model.AppUser;
import PetHotel.model.ServiceCategory;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.fxml.FXML;

import java.util.List;
import java.util.function.Consumer;

/**
 * GroomingServiceTypeController — Điều khiển dialog chọn loại dịch vụ grooming.
 *
 * Hiển thị danh sách các loại dịch vụ grooming có sẵn và cho phép người dùng chọn một.
 * Khi chọn, gửi callback về GroomingBookingController để tiếp tục luồng đặt lịch.
 */
public class GroomingServiceTypeController {

    @FXML
    private VBox serviceListContainer;

    @FXML
    private Button btnCancel;

    private final GroomingBUS groomingBUS = new GroomingBUS();
    private AppUser currentUser;
    private Consumer<ServiceCategory> onServiceTypeSelected;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser == null) {
            showError("Chưa đăng nhập. Không thể chọn loại dịch vụ.");
            closeWindow();
            return;
        }

        btnCancel.setOnAction(e -> closeWindow());

        loadGroomingServiceTypes();
    }

    /**
     * Tải danh sách loại dịch vụ grooming
     */
    private void loadGroomingServiceTypes() {
        try {
            List<ServiceCategory> categories = groomingBUS.getGroomingServiceCategories(currentUser);

            if (categories.isEmpty()) {
                Label noDataLabel = new Label("Không có loại dịch vụ grooming nào");
                noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #999999;");
                serviceListContainer.getChildren().add(noDataLabel);
                return;
            }

            for (ServiceCategory category : categories) {
                VBox serviceCard = createServiceTypeCard(category);
                serviceListContainer.getChildren().add(serviceCard);
            }

        } catch (Exception e) {
            showError("Không thể tải loại dịch vụ grooming: " + e.getMessage());
        }
    }

    /**
     * Tạo một card hiển thị loại dịch vụ
     */
    private VBox createServiceTypeCard(ServiceCategory category) {
        VBox card = new VBox(8);
        card.setStyle("-fx-border-color: #d4a574; -fx-border-width: 1; " +
                      "-fx-border-radius: 8; -fx-padding: 16; " +
                      "-fx-background-color: #fef9f3; " +
                      "-fx-cursor: hand;");
        card.setPrefHeight(60);

        // Service type name
        Label nameLabel = new Label(category.getCategoryName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #3b2314;");

        // Service type note
        Label noteLabel = new Label(category.getNote() != null ? category.getNote() : "");
        noteLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666; -fx-wrap-text: true;");

        card.getChildren().addAll(nameLabel, noteLabel);

        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-border-color: #c49b5e; -fx-border-width: 2; " +
                         "-fx-border-radius: 8; -fx-padding: 16; " +
                         "-fx-background-color: #fef0e6; " +
                         "-fx-cursor: hand;");
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-border-color: #d4a574; -fx-border-width: 1; " +
                         "-fx-border-radius: 8; -fx-padding: 16; " +
                         "-fx-background-color: #fef9f3; " +
                         "-fx-cursor: hand;");
        });

        // Click handler
        card.setOnMouseClicked(e -> selectServiceType(category));

        return card;
    }

    /**
     * Xử lý khi người dùng chọn loại dịch vụ
     */
    private void selectServiceType(ServiceCategory category) {
        if (onServiceTypeSelected != null) {
            onServiceTypeSelected.accept(category);
        }
        closeWindow();
    }

    /**
     * Set callback cho khi chọn loại dịch vụ
     */
    public void setOnServiceTypeSelected(Consumer<ServiceCategory> callback) {
        this.onServiceTypeSelected = callback;
    }

    private void closeWindow() {
        Stage stage = (Stage) serviceListContainer.getScene().getWindow();
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
