package PetHotel.gui.controller;

import PetHotel.bus.AuthBUS;
import PetHotel.bus.CustomerBUS;
import PetHotel.bus.PetBUS;
import PetHotel.dao.CustomerDAO;
import PetHotel.dao.PetHealthRecordDAO;
import PetHotel.model.Customer;
import PetHotel.model.Pet;
import PetHotel.model.PetHealthRecord;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PetController {

    @FXML private HBox headerBar;
    @FXML private Button btnAddPet;
    @FXML private Label statTotalPets;
    @FXML private Label statPetsStaying;
    @FXML private Label statPetsGrooming;
    @FXML private Label statPetsMonitoring;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterSpecies;
    @FXML private ComboBox<String> filterHealth;
    @FXML private Button btnHealth;
    @FXML private Button btnHistory;
    @FXML private Button btnDelete;
    @FXML private Label selectionInfo;
    @FXML private TableView<Pet> petTable;
    @FXML private TableColumn<Pet, String> colPetId;
    @FXML private TableColumn<Pet, String> colPetName;
    @FXML private TableColumn<Pet, String> colPetSpecies;
    @FXML private TableColumn<Pet, String> colPetOwner;
    @FXML private TableColumn<Pet, String> colPetHealth;
    @FXML private Pagination pagination;
    @FXML private Label pageInfo;

    private PetBUS petBUS;
    private CustomerBUS customerBUS;
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final PetHealthRecordDAO petHealthRecordDAO = new PetHealthRecordDAO();
    private final ObservableList<Pet> pets = FXCollections.observableArrayList();
    private final Map<String, String> ownerNameByCustomerId = new HashMap<>();
    private final Map<String, PetHealthRecord> latestHealthByPetId = new HashMap<>();
    private Pet selectedPet;
    private boolean dataLoaded;
    private boolean needsPetRefresh = true;
    private boolean loadingPets;

    @FXML
    public void initialize() {
        AuthBUS authBUS = SessionManager.getInstance().getAuthBUS();
        petBUS = new PetBUS(authBUS);
        customerBUS = new CustomerBUS(authBUS);
        setupTableColumns();
        petTable.setItems(pets);
        petTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedPet = newVal;
            setActionButtonsEnabled(newVal != null);
        });
        loadPetsAsync(null, true);
    }

    private void setupTableColumns() {
        petTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        petTable.setFixedCellSize(38);
        colPetId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPetId()));
        colPetName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPetName()));
        colPetSpecies.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSpecies() + " / " + valueOrDash(d.getValue().getBreed())));
        colPetOwner.setCellValueFactory(d -> new SimpleStringProperty(ownerNameByCustomerId.getOrDefault(d.getValue().getCustomerId(), "Chưa liên kết")));
        colPetHealth.setCellValueFactory(d -> new SimpleStringProperty(healthTableLabel(d.getValue().getPetId())));
    }

    private void setActionButtonsEnabled(boolean enabled) {
        if (btnDelete != null) btnDelete.setDisable(true);
        if (btnHistory != null) {
            btnHistory.setDisable(true);
            btnHistory.setTooltip(new Tooltip("Chưa triển khai"));
        }
        if (btnHealth != null) btnHealth.setDisable(!enabled);
        if (selectionInfo != null) {
            selectionInfo.setText(enabled ? "1 thú cưng được chọn - double-click để xem chi tiết" : "");
        }
    }

    public void reloadPetsFromDatabase() {
        loadPetsAsync(selectedPet == null ? null : selectedPet.getPetId(), true);
    }

    public void reloadPetsFromDatabase(String petIdToSelect) {
        loadPetsAsync(petIdToSelect, true);
    }

    public void markNeedsRefresh() {
        needsPetRefresh = true;
    }

    public void refreshIfNeeded() {
        if (!dataLoaded || needsPetRefresh) {
            loadPetsAsync(selectedPet == null ? null : selectedPet.getPetId(), true);
            return;
        }
        petTable.refresh();
        updateSummary(null);
    }

    public void prepareEmbeddedView() {
        if (headerBar != null) {
            headerBar.setVisible(false);
            headerBar.setManaged(false);
        }
        if (btnAddPet != null) {
            btnAddPet.setVisible(false);
            btnAddPet.setManaged(false);
        }
    }

    private void loadPetsAsync(String petIdToSelect, boolean forceDatabase) {
        if (loadingPets) return;
        if (!forceDatabase && dataLoaded && !needsPetRefresh) {
            updateSummary(petIdToSelect);
            return;
        }
        long start = System.currentTimeMillis();
        System.out.println("[PetTab] load data start");
        loadingPets = true;
        pageInfo.setText("Đang tải danh sách thú cưng...");

        Task<PetLoadResult> task = new Task<>() {
            @Override
            protected PetLoadResult call() throws Exception {
                List<Pet> loadedPets = petBUS.getAllPets();
                Map<String, String> owners = new HashMap<>();
                for (Customer customer : customerBUS.getAllCustomers()) {
                    owners.put(customer.getCustomerId(), customer.getFullName());
                }
                Map<String, PetHealthRecord> latest = petHealthRecordDAO.findLatestByAllPetIds();
                return new PetLoadResult(loadedPets, owners, latest);
            }
        };

        task.setOnSucceeded(event -> {
            PetLoadResult result = task.getValue();
            ownerNameByCustomerId.clear();
            ownerNameByCustomerId.putAll(result.ownerNames());
            latestHealthByPetId.clear();
            latestHealthByPetId.putAll(result.latestHealth());
            pets.setAll(result.pets());
            dataLoaded = true;
            needsPetRefresh = false;
            loadingPets = false;
            updateSummary(petIdToSelect);
            System.out.println("[PetTab] load data done in " + (System.currentTimeMillis() - start) + "ms");
        });
        task.setOnFailed(event -> {
            loadingPets = false;
            Throwable error = task.getException();
            showError("Không tải được danh sách thú cưng",
                    error instanceof Exception ex ? ex : new RuntimeException(error));
        });

        Thread thread = new Thread(task, "pet-tab-loader");
        thread.setDaemon(true);
        thread.start();
    }

    private void updateSummary(String petIdToSelect) {
        petTable.refresh();
        statTotalPets.setText(String.valueOf(pets.size()));
        statPetsStaying.setText("0");
        statPetsGrooming.setText("0");
        statPetsMonitoring.setText(String.valueOf(countMonitoringPets()));
        pagination.setPageCount(1);
        pageInfo.setText("Hiển thị " + pets.size() + " / " + pets.size() + " thú cưng");
        if (petIdToSelect != null) {
            pets.stream()
                    .filter(p -> petIdToSelect.equals(p.getPetId()))
                    .findFirst()
                    .ifPresent(p -> petTable.getSelectionModel().select(p));
        }
    }

    private long countMonitoringPets() {
        return pets.stream()
                .map(p -> healthTableLabel(p.getPetId()))
                .filter(text -> text.contains("Cần theo dõi") || text.contains("Bất thường"))
                .count();
    }

    @FXML
    public void onSearch() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadPetsAsync(null, true);
            return;
        }
        try {
            List<Pet> result = petBUS.searchPet(keyword);
            pets.setAll(result);
            pageInfo.setText("Hiển thị " + result.size() + " / " + result.size() + " thú cưng");
            if (result.isEmpty()) showInfo("Không có kết quả", "Không tìm thấy thú cưng phù hợp.");
        } catch (Exception e) {
            showError("Không tra cứu được thú cưng", e);
        }
    }

    @FXML public void onClearFilter() { searchField.clear(); filterSpecies.setValue(null); filterHealth.setValue(null); loadPetsAsync(null, true); }
    @FXML public void onAddPet(ActionEvent event) { openPetForm(); }
    @FXML public void onDeletePet(ActionEvent event) { showInfo("Ngoài phạm vi", "UC-PET-05 chưa triển khai trong lần này."); }
    @FXML public void onServiceHistory(ActionEvent event) { showInfo("Ngoài phạm vi", "UC-PET-08 chưa triển khai trong lần này."); }
    @FXML public void onHealthRecord(ActionEvent event) { if (selectedPet != null) openHealthForm(selectedPet); }

    @FXML
    public void onTableClick(MouseEvent event) {
        if (event.getClickCount() >= 2 && selectedPet != null) {
            openPetDetail(selectedPet);
        }
    }

    public void selectAndOpenPet(String petId) {
        loadPetsAsync(petId, true);
        Pet target;
        try {
            target = petBUS.getPetDetail(petId);
        } catch (Exception e) {
            target = null;
        }
        if (target != null) {
            petTable.getSelectionModel().select(target);
            petTable.scrollTo(target);
            openPetDetail(target);
        } else {
            showInfo("Không tìm thấy thú cưng", "Không tìm thấy thú cưng với mã: " + petId);
        }
    }

    private void openPetForm() {
        long start = System.currentTimeMillis();
        String fxmlPath = "/PetHotel/gui/view/PetForm.fxml";
        System.out.println("[PetForm] Click add pet");
        System.out.println("Opening pet form from pet management...");
        System.out.println("Loading FXML: " + fxmlPath);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            System.out.println("[PetForm] FXML loaded in " + (System.currentTimeMillis() - start) + "ms");
            PetFormController controller = loader.getController();
            controller.prepareForPetManagement();
            controller.setOnSaved(() -> {
                markNeedsRefresh();
                reloadPetsFromDatabase();
            });

            Stage stage = modalStage("Thêm Thú Cưng");
            preparePetFormStage(stage, root);
            System.out.println("[PetForm] Stage ready in " + (System.currentTimeMillis() - start) + "ms");
            stage.showAndWait();
            System.out.println("[PetForm] Stage closed after " + (System.currentTimeMillis() - start) + "ms");
        } catch (Exception e) {
            System.err.println("Cannot open PetForm.fxml");
            e.printStackTrace();
            showError("Không mở được form thêm thú cưng", e);
        }
    }

    private void preparePetFormStage(Stage stage, Parent root) {
        if (root instanceof Region region) {
            region.setMinSize(760, 680);
            region.setPrefSize(820, 740);
        }
        Scene scene = new Scene(root, 820, 740);
        stage.setScene(scene);
        stage.setMinWidth(760);
        stage.setMinHeight(680);
        root.applyCss();
        root.layout();
        root.snapshot(null, null);
        stage.sizeToScene();
        stage.centerOnScreen();
    }

    private void openPetDetail(Pet pet) {
        Stage stage = modalStage("Chi Tiết Thú Cưng");
        Customer owner = findCustomer(pet.getCustomerId());
        PetHealthRecord latest = latestRecord(pet.getPetId());
        String ownerText = owner == null ? "Chưa liên kết chủ sở hữu" : owner.getCustomerId() + " - " + owner.getFullName();
        String ownerPhone = owner == null ? "-" : owner.getPhone();

        GridPane petInfo = formGrid();
        addInfo(petInfo, 0, "Mã thú cưng", pet.getPetId());
        addInfo(petInfo, 1, "Tên thú cưng", pet.getPetName());
        addInfo(petInfo, 2, "Loài", pet.getSpecies());
        addInfo(petInfo, 3, "Giống", valueOrDash(pet.getBreed()));
        addInfo(petInfo, 4, "Ngày sinh", "Chưa có cột trong DB");
        addInfo(petInfo, 5, "Cân nặng", pet.getWeightKg() == null ? "Chưa ghi nhận cân nặng" : pet.getWeightKg() + " kg");
        addInfo(petInfo, 6, "Màu lông", "Chưa có cột trong DB");
        addInfo(petInfo, 7, "Trạng thái", "Đang hoạt động");
        addInfo(petInfo, 8, "Ghi chú đặc biệt", valueOrDash(pet.getSpecialNote()));

        GridPane ownerHealth = formGrid();
        addInfo(ownerHealth, 0, "Chủ sở hữu", ownerText);
        addInfo(ownerHealth, 1, "SĐT chủ", ownerPhone);
        addInfo(ownerHealth, 2, "Sức khỏe gần nhất", healthLabel(latest));
        addInfo(ownerHealth, 3, "Ngày ghi nhận", latest == null || latest.getRecordedAt() == null ? "-" : latest.getRecordedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        addInfo(ownerHealth, 4, "Bản ghi sức khỏe", latest == null ? "Chưa có ghi nhận sức khỏe." : valueOrDash(latest.getNote()));

        HBox columns = new HBox(14, card("Thông tin thú cưng", petInfo), card("Chủ sở hữu & sức khỏe", ownerHealth));
        HBox.setHgrow(columns.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(columns.getChildren().get(1), Priority.ALWAYS);

        Button health = primaryButton("Ghi nhận sức khỏe");
        Button history = secondaryButton("Lịch sử dịch vụ");
        Button close = secondaryButton("Đóng");
        history.setDisable(true);
        history.setTooltip(new Tooltip("Chưa triển khai"));
        close.setOnAction(e -> stage.close());
        health.setOnAction(e -> {
            stage.close();
            openHealthForm(pet);
        });

        VBox root = new VBox(16,
                profileHeader(pet.getPetName(), pet.getSpecies() + " / " + valueOrDash(pet.getBreed()) + " · " + healthLabel(latest), initials(pet.getPetName())),
                columns,
                footer(health, history, close));
        root.getStyleClass().add("ph-modal-root");

        stage.setScene(new Scene(root, 780, 560));
        addStylesheet(stage);
        stage.showAndWait();
    }

    private void openHealthForm(Pet pet) {
        Stage stage = modalStage("Ghi Nhận Sức Khỏe");
        Label error = errorLabel();
        TextField petId = readOnlyField(pet.getPetId());
        TextField petName = readOnlyField(pet.getPetName());
        ComboBox<String> status = new ComboBox<>(FXCollections.observableArrayList("Bình thường", "Cần theo dõi", "Bất thường"));
        status.getStyleClass().add("ph-form-input");
        status.setValue("Bình thường");
        TextField symptom = healthField("Nhập triệu chứng nếu có");
        TextField note = healthField("Nhập ghi chú sức khỏe");
        TextField recordedAt = readOnlyField(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        TextField recorder = readOnlyField(SessionManager.getInstance().getUserId() == null ? "Nhân viên" : SessionManager.getInstance().getUserId());
        TextField bookingId = formField();
        bookingId.setPromptText("Nhập mã booking hợp lệ");
        Label bookingHint = new Label("Bảng PET_HEALTH_RECORD hiện yêu cầu booking_id. Vui lòng nhập mã booking hợp lệ.");
        bookingHint.getStyleClass().add("ph-card-hint");
        bookingHint.setWrapText(true);
        VBox bookingBox = new VBox(4, bookingId, bookingHint);

        GridPane grid = formGrid();
        addRow(grid, 0, "Mã thú cưng", petId);
        addRow(grid, 1, "Tên thú cưng", petName);
        addRow(grid, 2, "Tình trạng tổng quát", status);
        addRow(grid, 3, "Triệu chứng bất thường", symptom);
        addRow(grid, 4, "Ghi chú sức khỏe", note);
        addRow(grid, 5, "Ngày ghi nhận", recordedAt);
        addRow(grid, 6, "Người ghi nhận", recorder);
        addRow(grid, 7, "Mã booking *", bookingBox);

        Button save = primaryButton("Lưu ghi nhận");
        Button cancel = secondaryButton("Hủy");
        cancel.setOnAction(e -> stage.close());
        save.setOnAction(e -> {
            try {
                validateHealthForm(status.getValue(), symptom.getText(), note.getText(), bookingId.getText());
                int value = healthStatusValue(status.getValue());
                String fullNote = buildHealthNote(status.getValue(), symptom.getText(), note.getText(), recorder.getText());
                petBUS.addHealthRecord(pet.getPetId(), bookingId.getText(), fullNote, value);
                markNeedsRefresh();
                reloadPetsFromDatabase(pet.getPetId());
                stage.close();
                openPetDetail(petBUS.getPetDetail(pet.getPetId()));
            } catch (Exception ex) {
                error.setText(ex.getMessage());
            }
        });

        stage.setScene(new Scene(formShell("Ghi Nhận Sức Khỏe", "Cập nhật tình trạng sức khỏe gần nhất", "HR", grid, error, save, cancel), 620, 720));
        addStylesheet(stage);
        Platform.runLater(status::requestFocus);
        stage.showAndWait();
    }

    private VBox formShell(String title, String subtitle, String avatar, GridPane grid, Label error, Button save, Button cancel) {
        VBox body = new VBox(16, profileHeader(title, subtitle, avatar), card(grid), error, footer(save, cancel));
        body.getStyleClass().add("ph-modal-root");
        return body;
    }

    private VBox profileHeader(String title, String subtitle, String avatarText) {
        Label line = new Label();
        line.getStyleClass().add("section-header-line");
        Label avatar = new Label(avatarText);
        avatar.getStyleClass().add("ph-profile-avatar");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("ph-profile-title");
        Label sub = new Label(subtitle);
        sub.getStyleClass().add("ph-profile-subtitle");
        HBox row = new HBox(12, line, avatar, new VBox(4, titleLabel, sub));
        row.setAlignment(Pos.CENTER_LEFT);
        VBox box = new VBox(row);
        box.getStyleClass().add("ph-profile-header");
        return box;
    }

    private VBox card(javafx.scene.Node content) {
        VBox box = new VBox(content);
        box.getStyleClass().add("ph-content-card");
        return box;
    }

    private VBox card(String title, javafx.scene.Node content) {
        VBox box = new VBox(12);
        box.getStyleClass().add("ph-content-card");
        Label label = new Label(title);
        label.getStyleClass().add("ph-card-title");
        box.getChildren().addAll(label, content);
        return box;
    }

    private HBox footer(Button... buttons) {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox box = new HBox(10, spacer);
        box.getChildren().addAll(buttons);
        box.setAlignment(Pos.CENTER_RIGHT);
        box.getStyleClass().add("ph-form-footer");
        return box;
    }

    private GridPane formGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        return grid;
    }

    private void addRow(GridPane grid, int row, String label, javafx.scene.Node field) {
        Label l = new Label(label);
        l.getStyleClass().add("ph-form-label");
        grid.add(l, 0, row);
        grid.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    private void addInfo(GridPane grid, int row, String label, String value) {
        Label l = new Label(label);
        l.getStyleClass().add("ph-info-label");
        Label v = new Label(value);
        v.getStyleClass().add("ph-info-value");
        v.setWrapText(true);
        grid.add(l, 0, row);
        grid.add(v, 1, row);
    }

    private Stage modalStage(String title) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        return stage;
    }

    private TextField formField() {
        TextField field = new TextField();
        field.getStyleClass().add("ph-form-input");
        return field;
    }

    private TextField readOnlyField(String value) {
        TextField field = formField();
        field.setText(value);
        field.setEditable(false);
        field.setFocusTraversable(false);
        field.getStyleClass().add("ph-form-readonly");
        return field;
    }

    private TextArea formArea(int rows) {
        TextArea area = new TextArea();
        area.setPrefRowCount(rows);
        area.setWrapText(true);
        area.getStyleClass().add("ph-form-input");
        return area;
    }

    private TextArea healthArea(int rows, double height) {
        TextArea area = formArea(rows);
        area.setPrefHeight(height);
        area.setMinHeight(height);
        area.setMaxHeight(height);
        area.getStyleClass().add("ph-health-text-area");
        return area;
    }

    private TextField healthField(String prompt) {
        TextField field = formField();
        field.setPromptText(prompt);
        field.setEditable(true);
        field.setDisable(false);
        field.setFocusTraversable(true);
        field.getStyleClass().add("ph-health-text-field");
        return field;
    }

    private Label errorLabel() {
        Label label = new Label();
        label.getStyleClass().add("ph-form-error");
        label.setWrapText(true);
        return label;
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().addAll("action-btn", "action-btn-primary");
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().addAll("action-btn", "action-btn-outline");
        return button;
    }

    private void addStylesheet(Stage stage) {
        stage.getScene().getStylesheets().add(getClass().getResource("/PetHotel/gui/css/style.css").toExternalForm());
    }

    private Customer findCustomer(String customerId) {
        try {
            return customerId == null ? null : customerDAO.findById(customerId);
        } catch (SQLException e) {
            return null;
        }
    }

    private String ownerDisplay(String customerId) {
        Customer owner = findCustomer(customerId);
        return owner == null ? "Chưa liên kết" : owner.getFullName();
    }

    private PetHealthRecord latestRecord(String petId) {
        try {
            return petBUS.getLatestHealthRecord(petId);
        } catch (Exception e) {
            return null;
        }
    }

    private String healthLabel(PetHealthRecord record) {
        if (record == null) return "Sức khỏe: Chưa ghi nhận";
        if (record.isHealthy()) return "Sức khỏe: Bình thường";
        String note = record.getNote() == null ? "" : record.getNote().toLowerCase();
        if (note.contains("bất thường") || note.contains("triệu chứng")) return "Sức khỏe: Có triệu chứng bất thường";
        return "Sức khỏe: Cần theo dõi";
    }

    private String healthTableLabel(String petId) {
        PetHealthRecord record = latestHealthByPetId.get(petId);
        if (record == null) return "Chưa ghi nhận";
        if (record.isHealthy()) return "Bình thường";
        String note = record.getNote() == null ? "" : record.getNote().toLowerCase();
        if (note.contains("bất thường") || note.contains("triệu chứng")) return "Bất thường";
        return "Cần theo dõi";
    }

    private String buildHealthNote(String status, String symptom, String note, String recorder) {
        String s = symptom == null || symptom.isBlank() ? "Không ghi nhận" : symptom.trim();
        String n = note == null ? "" : note.trim();
        return "Tình trạng: " + status + "\nTriệu chứng: " + s + "\nGhi chú: " + n + "\nNgười ghi nhận: " + recorder;
    }

    private int healthStatusValue(String status) {
        return "Bình thường".equals(status) ? 1 : 0;
    }

    private void validateHealthForm(String status, String symptom, String note, String bookingId) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn tình trạng tổng quát.");
        }
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("Mã booking không được để trống vì bảng PET_HEALTH_RECORD hiện yêu cầu booking_id.");
        }
        boolean hasSymptom = symptom != null && !symptom.isBlank();
        boolean hasNote = note != null && !note.isBlank();
        if (!"Bình thường".equals(status) && !hasSymptom && !hasNote) {
            throw new IllegalArgumentException("Vui lòng nhập triệu chứng hoặc ghi chú khi tình trạng cần theo dõi/bất thường.");
        }
    }

    private String initials(String value) {
        if (value == null || value.isBlank()) return "?";
        return value.trim().substring(0, 1).toUpperCase();
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showError(String title, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private record PetLoadResult(
            List<Pet> pets,
            Map<String, String> ownerNames,
            Map<String, PetHealthRecord> latestHealth) {
    }
}
