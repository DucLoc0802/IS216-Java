package PetHotel.gui.controller;

import PetHotel.bus.ProductBUS;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.function.Consumer;

public class ProductCategoryDialogController {
    @FXML private TextField categoryNameField;
    @FXML private Label categoryNameErrorLabel;

    private final ProductBUS productBUS = new ProductBUS();
    private AppUser currentUser;
    private Consumer<String> onCategoryAdded;

    public static void openAddCategoryDialog(Consumer<String> onAdded) {
        try {
            FXMLLoader loader = new FXMLLoader(
                ProductCategoryDialogController.class.getResource("/PetHotel/gui/view/ProductCategoryDialog.fxml")
            );
            VBox root = loader.load();

            ProductCategoryDialogController controller = loader.getController();
            controller.onCategoryAdded = onAdded;

            Stage stage = new Stage();
            stage.setTitle("Thêm Loại Sản Phẩm");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Không thể mở form loại sản phẩm: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        categoryNameField.textProperty().addListener((obs, oldValue, newValue) -> categoryNameErrorLabel.setText(""));
    }

    @FXML
    public void handleSave() {
        categoryNameErrorLabel.setText("");
        String categoryName = categoryNameField.getText();

        try {
            productBUS.createCategory(categoryName, currentUser);
            String normalizedName = categoryName.trim();
            showInfo("Đã thêm loại sản phẩm " + normalizedName + ".");

            if (onCategoryAdded != null) {
                onCategoryAdded.accept(normalizedName);
            }
            closeWindow();
        } catch (ValidationException e) {
            categoryNameErrorLabel.setText(e.getMessage());
        } catch (SQLException e) {
            showError("Không thể thêm loại sản phẩm: " + e.getMessage());
        } catch (RuntimeException e) {
            categoryNameErrorLabel.setText(e.getMessage());
        }
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) categoryNameField.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
