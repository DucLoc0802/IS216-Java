package PetHotel.gui.controller;

import PetHotel.bus.AuthBUS;
import PetHotel.bus.PetBUS;
import PetHotel.model.Pet;
import PetHotel.model.PetHealthRecord;
import PetHotel.util.Role;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HealthRecordFormController {

    private static final String STATUS_NORMAL = "Bình thường";
    private static final String STATUS_MONITORING = "Cần theo dõi";
    private static final String STATUS_ABNORMAL = "Bất thường";
    private static final String VIEW_ONLY_MESSAGE =
            "Chế độ xem: chỉ nhân viên chăm sóc được ghi nhận sức khỏe.";
    private static final DateTimeFormatter DISPLAY_DATE_TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private BorderPane rootPane;
    @FXML private VBox formContainer;
    @FXML private GridPane formGrid;

    @FXML private TextField txtPetId;
    @FXML private TextField txtPetName;
    @FXML private ComboBox<String> cbStatus;
    @FXML private TextField txtSymptom;
    @FXML private TextField txtNote;
    @FXML private TextField txtBookingId;
    @FXML private TextField txtRecordedAt;
    @FXML private TextField txtRecorder;

    @FXML private Label errorLabel;
    @FXML private Button btnSave;

    private PetBUS petBUS;
    private Pet pet;
    private Runnable onSaved;
    private boolean saved;
    private boolean editMode = false;

    @FXML
    public void initialize() {
        AuthBUS authBUS = SessionManager.getInstance().getAuthBUS();
        petBUS = new PetBUS(authBUS);

        setupComboBox();
        setupDefaultValues();

        setupReadOnlyField(txtPetId);
        setupReadOnlyField(txtPetName);
        setupReadOnlyField(txtRecordedAt);
        setupReadOnlyField(txtRecorder);

        setupTextEntryField(txtSymptom);
        setupTextEntryField(txtNote);
        setupTextEntryField(txtBookingId);
        setupHitTestLogging();
        setupFocusAndTextLogging();
        setupPostLayoutDebug();

        applyMode();
        printFieldState("initialize");
    }

    public void setPet(Pet pet) {
        if (pet == null) {
            throw new IllegalArgumentException("Không có thú cưng để ghi nhận sức khỏe.");
        }

        this.pet = pet;
        txtPetId.setText(pet.getPetId());
        txtPetName.setText(pet.getPetName());

        if (editMode) {
            prepareNewRecordInput();
        } else {
            populateLatestRecordForView();
        }

        applyMode();
        printFieldState("setPet");
        System.out.println("[HealthForm] pet set: " + pet.getPetId());
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;

        if (editMode) {
            prepareNewRecordInput();
        } else {
            populateLatestRecordForView();
        }

        applyMode();
        printFieldState("setEditMode");

        if (editMode) {
            Platform.runLater(() -> {
                txtSymptom.requestFocus();
                txtSymptom.deselect();
            });
        }
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void onSave() {
        clearError();

        try {
            if (!editMode || !isPetCareStaff()) {
                throw new IllegalArgumentException(
                        "Chỉ nhân viên chăm sóc được ghi nhận sức khỏe thú cưng.");
            }

            validateInput();

            petBUS.addHealthRecord(
                    pet.getPetId(),
                    txtBookingId.getText().trim(),
                    buildHealthNote(),
                    healthStatusValue());

            saved = true;

            if (onSaved != null) {
                onSaved.run();
            }

            close();
        } catch (Exception e) {
            e.printStackTrace();
            showError(e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void setupComboBox() {
        if (cbStatus == null) {
            return;
        }

        cbStatus.getItems().setAll(STATUS_NORMAL, STATUS_MONITORING, STATUS_ABNORMAL);
        cbStatus.setValue(STATUS_NORMAL);
        cbStatus.setMouseTransparent(false);
    }

    private void setupDefaultValues() {
        if (txtRecordedAt != null) {
            txtRecordedAt.setText(LocalDateTime.now().format(DISPLAY_DATE_TIME));
        }

        if (txtRecorder != null) {
            String currentUserId = SessionManager.getInstance().getUserId();
            txtRecorder.setText(currentUserId == null || currentUserId.isBlank()
                    ? "Nhân viên"
                    : currentUserId);
        }
    }

    private void setupReadOnlyField(TextField field) {
        if (field == null) {
            return;
        }

        field.setEditable(false);
        field.setDisable(false);
        field.setMouseTransparent(false);
        field.setFocusTraversable(false);
    }

    private void setupTextEntryField(TextField field) {
        if (field == null) {
            return;
        }

        field.setDisable(false);
        field.setMouseTransparent(false);
        field.setPickOnBounds(true);

        field.addEventFilter(MouseEvent.MOUSE_ENTERED, event -> {
            if (field.isEditable()) {
                field.setCursor(Cursor.TEXT);
            }
        });

        field.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (field.isEditable()) {
                field.setCursor(Cursor.TEXT);
                field.requestFocus();
            }
        });

        field.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (field.isEditable()) {
                field.setCursor(Cursor.TEXT);
                field.requestFocus();
            }
        });
    }

    private void setupHitTestLogging() {
        if (rootPane != null) {
            rootPane.addEventFilter(MouseEvent.MOUSE_MOVED, e ->
                    System.out.println("[HealthHitTest] moved target = " + e.getTarget()));
            rootPane.addEventFilter(MouseEvent.MOUSE_CLICKED, e ->
                    System.out.println("[HealthHitTest] clicked target = " + e.getTarget()));
        }

        if (txtSymptom != null) {
            txtSymptom.addEventFilter(MouseEvent.MOUSE_ENTERED, e ->
                    System.out.println("[HealthHitTest] entered txtSymptom"));
        }

        if (txtNote != null) {
            txtNote.addEventFilter(MouseEvent.MOUSE_ENTERED, e ->
                    System.out.println("[HealthHitTest] entered txtNote"));
        }
    }

    private void setupFocusAndTextLogging() {
        txtSymptom.focusedProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("[HealthFocus] txtSymptom focus = " + newVal);
        });

        txtNote.focusedProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("[HealthFocus] txtNote focus = " + newVal);
        });

        txtSymptom.textProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("[HealthText] txtSymptom = " + newVal);
        });

        txtNote.textProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("[HealthText] txtNote = " + newVal);
        });
    }

    private void setupPostLayoutDebug() {
        Platform.runLater(() -> {
            System.out.println("[HealthDebug] scene = " + rootPane.getScene());
            System.out.println("[HealthDebug] symptom bounds = "
                    + txtSymptom.localToScene(txtSymptom.getBoundsInLocal()));
            System.out.println("[HealthDebug] note bounds = "
                    + txtNote.localToScene(txtNote.getBoundsInLocal()));

            txtSymptom.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            txtNote.setStyle("-fx-border-color: blue; -fx-border-width: 2px;");

            txtSymptom.requestFocus();
            System.out.println("[HealthDebug] symptom focused after request = " + txtSymptom.isFocused());
        });
    }

    private void applyMode() {
        keepParentsInteractive();

        cbStatus.setDisable(!editMode);

        txtSymptom.setDisable(false);
        txtSymptom.setEditable(editMode);
        txtSymptom.setMouseTransparent(false);
        txtSymptom.setFocusTraversable(true);
        txtSymptom.setCursor(Cursor.TEXT);
        txtSymptom.setPickOnBounds(true);

        txtNote.setDisable(false);
        txtNote.setEditable(editMode);
        txtNote.setMouseTransparent(false);
        txtNote.setFocusTraversable(true);
        txtNote.setCursor(Cursor.TEXT);
        txtNote.setPickOnBounds(true);

        txtBookingId.setDisable(false);
        txtBookingId.setEditable(editMode);
        txtBookingId.setMouseTransparent(false);
        txtBookingId.setFocusTraversable(true);
        txtBookingId.setCursor(Cursor.TEXT);
        txtBookingId.setPickOnBounds(true);

        btnSave.setVisible(editMode);
        btnSave.setManaged(editMode);
        btnSave.setDisable(!editMode);

        if (editMode) {
            errorLabel.setText("");
        } else {
            errorLabel.setText(VIEW_ONLY_MESSAGE);
        }

        System.out.println("[HealthMode] editMode=" + editMode);
        System.out.println("[HealthMode] symptom disabled=" + txtSymptom.isDisabled()
                + ", editable=" + txtSymptom.isEditable()
                + ", mouseTransparent=" + txtSymptom.isMouseTransparent()
                + ", focusTraversable=" + txtSymptom.isFocusTraversable());
        System.out.println("[HealthMode] note disabled=" + txtNote.isDisabled()
                + ", editable=" + txtNote.isEditable()
                + ", mouseTransparent=" + txtNote.isMouseTransparent()
                + ", focusTraversable=" + txtNote.isFocusTraversable());
    }

    private void keepParentsInteractive() {
        if (rootPane != null) {
            rootPane.setDisable(false);
            rootPane.setMouseTransparent(false);
        }

        if (formContainer != null) {
            formContainer.setDisable(false);
            formContainer.setMouseTransparent(false);
        }

        if (formGrid != null) {
            formGrid.setDisable(false);
            formGrid.setMouseTransparent(false);
        }
    }

    private void prepareNewRecordInput() {
        setupDefaultValues();

        if (cbStatus != null) {
            cbStatus.setValue(STATUS_NORMAL);
        }
        if (txtSymptom != null) {
            txtSymptom.clear();
        }
        if (txtNote != null) {
            txtNote.clear();
        }
        if (txtBookingId != null) {
            txtBookingId.clear();
        }
    }

    private void populateLatestRecordForView() {
        if (pet == null || petBUS == null) {
            return;
        }

        try {
            PetHealthRecord record = petBUS.getLatestHealthRecord(pet.getPetId());
            if (record == null) {
                if (cbStatus != null) cbStatus.setValue(STATUS_NORMAL);
                if (txtSymptom != null) txtSymptom.clear();
                if (txtNote != null) txtNote.setText("Chưa có ghi nhận sức khỏe.");
                if (txtBookingId != null) txtBookingId.clear();
                return;
            }

            if (cbStatus != null) {
                cbStatus.setValue(record.isHealthy() ? STATUS_NORMAL : statusFromNote(record.getNote()));
            }
            if (txtBookingId != null) {
                txtBookingId.setText(valueOrEmpty(record.getBookingId()));
            }
            if (txtRecordedAt != null && record.getRecordedAt() != null) {
                txtRecordedAt.setText(record.getRecordedAt().format(DISPLAY_DATE_TIME));
            }

            String note = valueOrEmpty(record.getNote());
            String symptom = extractLineValue(note, "Triệu chứng:");
            String healthNote = extractLineValue(note, "Ghi chú:");

            if (txtSymptom != null) {
                txtSymptom.setText(symptom);
            }
            if (txtNote != null) {
                txtNote.setText(healthNote.isBlank() && symptom.isBlank() ? note : healthNote);
            }
        } catch (Exception e) {
            if (txtNote != null) {
                txtNote.setText("Không tải được ghi nhận sức khỏe gần nhất: " + e.getMessage());
            }
        }
    }

    private String statusFromNote(String note) {
        if (note == null) {
            return STATUS_MONITORING;
        }

        String lower = note.toLowerCase();
        if (lower.contains("bất thường")) {
            return STATUS_ABNORMAL;
        }
        return STATUS_MONITORING;
    }

    private String extractLineValue(String note, String prefix) {
        if (note == null || note.isBlank()) {
            return "";
        }

        String[] lines = note.split("\\R");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith(prefix)) {
                return trimmed.substring(prefix.length()).trim();
            }
        }
        return "";
    }

    private void validateInput() {
        if (pet == null) {
            throw new IllegalArgumentException("Không có thú cưng để ghi nhận sức khỏe.");
        }

        if (cbStatus.getValue() == null || cbStatus.getValue().isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn tình trạng tổng quát.");
        }

        if (txtBookingId.getText() == null || txtBookingId.getText().isBlank()) {
            throw new IllegalArgumentException("Mã booking không được để trống.");
        }

        boolean hasSymptom = txtSymptom.getText() != null && !txtSymptom.getText().isBlank();
        boolean hasNote = txtNote.getText() != null && !txtNote.getText().isBlank();

        if (!STATUS_NORMAL.equals(cbStatus.getValue()) && !hasSymptom && !hasNote) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập triệu chứng hoặc ghi chú khi tình trạng cần theo dõi/bất thường.");
        }
    }

    private String buildHealthNote() {
        String symptom = txtSymptom.getText() == null || txtSymptom.getText().isBlank()
                ? "Không ghi nhận"
                : txtSymptom.getText().trim();

        String note = txtNote.getText() == null || txtNote.getText().isBlank()
                ? "Không ghi chú"
                : txtNote.getText().trim();

        return "Tình trạng: " + cbStatus.getValue()
                + "\nTriệu chứng: " + symptom
                + "\nGhi chú: " + note
                + "\nNgười ghi nhận: " + txtRecorder.getText();
    }

    private int healthStatusValue() {
        return STATUS_NORMAL.equals(cbStatus.getValue()) ? 1 : 0;
    }

    private boolean isPetCareStaff() {
        return SessionManager.getInstance().getCurrentUser() != null
                && SessionManager.getInstance().getCurrentUser().getRole() == Role.PET_CARE_STAFF;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private void printFieldState(String source) {
        Role currentRole = SessionManager.getInstance().getCurrentUser() == null
                ? null
                : SessionManager.getInstance().getCurrentUser().getRole();

        System.out.println("[HealthForm] " + source
                + " role=" + currentRole
                + ", editMode=" + editMode);
        printTextFieldState("symptom", txtSymptom);
        printTextFieldState("note", txtNote);
        printTextFieldState("booking", txtBookingId);
        System.out.println("[HealthForm] save visible="
                + (btnSave == null ? "null" : btnSave.isVisible())
                + ", save managed=" + (btnSave == null ? "null" : btnSave.isManaged())
                + ", save disabled=" + (btnSave == null ? "null" : btnSave.isDisabled()));
    }

    private void printTextFieldState(String name, TextInputControl field) {
        if (field == null) {
            System.out.println("[HealthForm] " + name + " = null");
            return;
        }

        System.out.println("[HealthForm] " + name
                + " disable=" + field.isDisabled()
                + ", editable=" + field.isEditable()
                + ", mouseTransparent=" + field.isMouseTransparent()
                + ", focusTraversable=" + field.isFocusTraversable()
                + ", cursor=" + field.getCursor());
    }

    private void clearError() {
        if (errorLabel != null) {
            errorLabel.setText("");
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message == null || message.isBlank()
                    ? "Không thể lưu ghi nhận sức khỏe."
                    : message);
        }
    }

    private void close() {
        if (txtPetId != null
                && txtPetId.getScene() != null
                && txtPetId.getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }
}
