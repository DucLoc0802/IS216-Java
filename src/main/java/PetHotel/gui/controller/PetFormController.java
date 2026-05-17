package PetHotel.gui.controller;

import PetHotel.bus.AuthBUS;
import PetHotel.bus.CustomerBUS;
import PetHotel.bus.PetBUS;
import PetHotel.dao.PetDAO;
import PetHotel.exception.ValidationException;
import PetHotel.model.Customer;
import PetHotel.model.Pet;

import java.sql.SQLException;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class PetFormController {

    @FXML private Label avatarLabel;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label sectionTitleLabel;
    @FXML private ScrollPane formScrollPane;
    @FXML private TextField txtPetId;
    @FXML private TextField txtPetName;
    @FXML private ComboBox<String> cbSpecies;
    @FXML private TextField txtBreed;
    @FXML private TextField txtDob;
    @FXML private TextField txtWeight;
    @FXML private TextField txtColor;
    @FXML private ComboBox<String> cbSex;
    @FXML private ComboBox<Customer> cbOwner;
    @FXML private TextField txtOwnerDisplay;
    @FXML private TextField txtOwnerPhone;
    @FXML private TextArea txtNote;
    @FXML private TextArea txtInitialHealth;
    @FXML private Label errorLabel;
    @FXML private Button btnSave;

    private PetBUS petBUS;
    private CustomerBUS customerBUS;
    private final PetDAO petDAO = new PetDAO();
    private Customer fixedOwner;
    private Runnable onSaved;
    private boolean ownersLoaded;

    @FXML
    public void initialize() {
        long start = System.currentTimeMillis();
        System.out.println("PetFormController initialized");
        AuthBUS authBUS = SessionManager.getInstance().getAuthBUS();
        petBUS = new PetBUS(authBUS);
        customerBUS = new CustomerBUS(authBUS);

        formScrollPane.setFitToWidth(true);
        formScrollPane.setFitToHeight(false);
        cbSpecies.setValue("DOG");
        cbOwner.setConverter(new StringConverter<>() {
            @Override
            public String toString(Customer c) {
                return c == null ? "" : c.getCustomerId() + " - " + c.getFullName();
            }

            @Override
            public Customer fromString(String s) {
                return null;
            }
        });
        cbOwner.valueProperty().addListener((obs, oldVal, newVal) -> {
            txtOwnerPhone.setText(newVal == null ? "" : newVal.getPhone());
            if (newVal != null) {
                System.out.println("Selected owner: " + newVal.getCustomerId() + " - " + newVal.getFullName());
            }
        });
        txtOwnerDisplay.setVisible(false);
        txtOwnerDisplay.setManaged(false);

        txtPetId.setText("Đang tạo...");
        btnSave.setDisable(true);
        refreshNextPetIdAsync(start);
        Platform.runLater(() -> {
            txtPetName.requestFocus();
            txtPetName.deselect();
        });
    }

    public void setOwner(Customer owner) {
        this.fixedOwner = owner;
        if (owner == null) return;
        System.out.println("Selected owner: " + owner.getCustomerId() + " - " + owner.getFullName());
        cbOwner.setItems(FXCollections.observableArrayList(owner));
        cbOwner.setValue(owner);
        cbOwner.setVisible(false);
        cbOwner.setManaged(false);
        txtOwnerDisplay.setText(owner.getCustomerId() + " - " + owner.getFullName());
        txtOwnerDisplay.setVisible(true);
        txtOwnerDisplay.setManaged(true);
        txtOwnerPhone.setText(owner.getPhone());
        titleLabel.setText("Thêm thú cưng mới");
        subtitleLabel.setText("Tạo thú cưng mới cho " + owner.getFullName());
        sectionTitleLabel.setText("Tạo thú cưng mới cho khách hàng này");
        Platform.runLater(() -> txtPetName.requestFocus());
    }

    public void prepareForPetManagement() {
        titleLabel.setText("Thêm Thú Cưng");
        subtitleLabel.setText("Tạo hồ sơ thú cưng và liên kết chủ sở hữu");
        sectionTitleLabel.setText("Tạo thú cưng mới");
        cbOwner.setVisible(true);
        cbOwner.setManaged(true);
        txtOwnerDisplay.setVisible(false);
        txtOwnerDisplay.setManaged(false);
        loadOwners();
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    @FXML
    private void onSave() {
        errorLabel.setText("");
        try {
            Customer owner = fixedOwner != null ? fixedOwner : cbOwner.getValue();
            if (owner == null) {
                throw new ValidationException("Vui lòng chọn chủ sở hữu.");
            }
            String combinedNote = combineNotes(txtNote.getText(), txtInitialHealth.getText());
            Double weight = parseWeight(txtWeight.getText());
            System.out.println("Saving pet. pet_id preview=" + txtPetId.getText() + ", owner=" + owner.getCustomerId());
            Pet created = petBUS.createPet(
                    owner.getCustomerId(),
                    txtPetName.getText(),
                    cbSpecies.getValue(),
                    txtBreed.getText(),
                    cbSex.getValue(),
                    weight,
                    combinedNote
            );
            System.out.println("Insert pet success: " + created.getPetId() + ", owner=" + created.getCustomerId());
            if (onSaved != null) onSaved.run();
            close();
        } catch (Exception e) {
            System.err.println("Insert pet failed: " + e.getMessage());
            e.printStackTrace();
            errorLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void loadOwners() {
        if (ownersLoaded) return;
        try {
            long start = System.currentTimeMillis();
            List<Customer> customers = customerBUS.getAllCustomers();
            cbOwner.setItems(FXCollections.observableArrayList(customers));
            ownersLoaded = true;
            System.out.println("[PetForm] Owners loaded in " + (System.currentTimeMillis() - start) + "ms");
        } catch (Exception e) {
            errorLabel.setText("Không tải được danh sách khách hàng: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void refreshNextPetIdAsync(long openStart) {
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws SQLException {
                return petDAO.generateNextPetId();
            }
        };
        task.setOnSucceeded(e -> {
            txtPetId.setText(task.getValue());
            btnSave.setDisable(false);
            System.out.println("[PetForm] Pet ID generated in " + (System.currentTimeMillis() - openStart) + "ms: " + task.getValue());
        });
        task.setOnFailed(e -> {
            txtPetId.setText("PET001");
            btnSave.setDisable(false);
            System.err.println("[PetForm] Cannot generate pet id");
            task.getException().printStackTrace();
            errorLabel.setText("Không thể sinh mã thú cưng. Khi lưu, DAO sẽ thử sinh lại mã mới.");
        });
        Thread thread = new Thread(task, "pet-form-id-loader");
        thread.setDaemon(true);
        thread.start();
    }

    private Double parseWeight(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Cân nặng phải là số.");
        }
    }

    private String combineNotes(String note, String health) {
        String n = note == null ? "" : note.trim();
        String h = health == null ? "" : health.trim();
        if (h.isEmpty()) return n;
        return n.isEmpty() ? "Sức khỏe ban đầu: " + h : n + "\nSức khỏe ban đầu: " + h;
    }

    private void close() {
        Stage stage = (Stage) txtPetName.getScene().getWindow();
        stage.close();
    }
}
