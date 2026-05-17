package PetHotel.gui.controller;

import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * PetController — Quản Lý Thú Cưng
 * ─────────────────────────────────────────────────────────────────
 * Xử lý: PetManagement.fxml
 * Use cases:
 *   - Tra cứu / tìm kiếm thú cưng
 *   - Thêm / Sửa / Xóa thú cưng
 *   - Ghi nhận tình trạng sức khoẻ
 *   - Xem lịch sử dịch vụ thú cưng
 *   - Hiển thị chi tiết hồ sơ thú cưng ở panel phải
 */
public class PetController {

    // ── FXML Injections ──────────────────────────────────────────

    // Stat cards
    @FXML private Label statTotalPets;
    @FXML private Label statPetsStaying;
    @FXML private Label statPetsGrooming;
    @FXML private Label statPetsMonitoring;

    // Filter
    @FXML private TextField  searchField;
    @FXML private ComboBox<String> filterSpecies;
    @FXML private ComboBox<String> filterHealth;

    // Toolbar buttons
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnHealth;
    @FXML private Button btnHistory;
    @FXML private Label  selectionInfo;

    // Table
    @FXML private TableView<Object>          petTable;
    @FXML private TableColumn<Object,String> colPetIcon;
    @FXML private TableColumn<Object,String> colPetId;
    @FXML private TableColumn<Object,String> colPetName;
    @FXML private TableColumn<Object,String> colPetSpecies;
    @FXML private TableColumn<Object,String> colPetAge;
    @FXML private TableColumn<Object,String> colPetOwner;
    @FXML private TableColumn<Object,String> colPetHealth;
    @FXML private TableColumn<Object,String> colPetStatus;
    @FXML private TableColumn<Object,String> colPetActions;

    // Pagination
    @FXML private Pagination pagination;
    @FXML private Label      pageInfo;

    // Detail panel
    @FXML private VBox   noSelectionHint;
    @FXML private Label  detailPetIcon;
    @FXML private Label  detailPetName;
    @FXML private Label  detailPetSpecies;
    @FXML private Label  detailPetHealth;
    @FXML private Label  detailPetId;
    @FXML private Label  detailPetDob;
    @FXML private Label  detailPetWeight;
    @FXML private Label  detailPetColor;
    @FXML private Label  detailOwner;
    @FXML private Label  detailOwnerPhone;
    @FXML private Label  detailPetNote;
    @FXML private Button btnDetailEdit;
    @FXML private Button btnDetailHealth;
    @FXML private Button btnDetailHistory;

    // Health log
    @FXML private VBox  healthLogList;
    @FXML private Label healthEmptyHint;
    @FXML private Button btnAddHealth;

    // ── Internal state ───────────────────────────────────────────
    private Object selectedPet = null;   // thay bằng model Pet thực tế

    // ── Lifecycle ────────────────────────────────────────────────

    @FXML
    public void initialize() {
        setupTableColumns();
        setupTableSelectionListener();
        loadStats();
        loadPets();
        showNoSelection();
    }

    // ── Setup ────────────────────────────────────────────────────

    private void setupTableColumns() {
        // TODO: cellValueFactory cho từng cột khi có model Pet
        // colPetId.setCellValueFactory(new PropertyValueFactory<>("id"));
        // colPetName.setCellValueFactory(new PropertyValueFactory<>("name"));
        // ...

        // CellFactory cho cột Status → hiển thị badge màu
        colPetStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) { setGraphic(null); return; }
                Label badge = new Label(value);
                badge.getStyleClass().add("status-badge");
                switch (value) {
                    case "Đang ở khách sạn" -> badge.getStyleClass().add("status-inprogress");
                    case "Đã check-out"     -> badge.getStyleClass().add("status-done");
                    default                 -> badge.getStyleClass().add("status-pending");
                }
                setGraphic(badge);
                setText(null);
            }
        });

        // CellFactory cho cột Health
        colPetHealth.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) { setGraphic(null); return; }
                Label badge = new Label(value);
                badge.getStyleClass().add("status-badge");
                switch (value) {
                    case "Bình thường"  -> badge.getStyleClass().add("status-healthy");
                    case "Cần theo dõi" -> badge.getStyleClass().add("status-monitoring");
                    case "Đang bệnh"    -> badge.getStyleClass().add("status-sick");
                    default             -> badge.getStyleClass().add("status-pending");
                }
                setGraphic(badge);
                setText(null);
            }
        });
    }

    private void setupTableSelectionListener() {
        petTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                boolean hasSelection = (newVal != null);
                selectedPet = newVal;
                setActionButtonsEnabled(hasSelection);
                if (hasSelection) showPetDetail(newVal);
                else              showNoSelection();
            }
        );
    }

    private void setActionButtonsEnabled(boolean enabled) {
        btnEdit.setDisable(!enabled);
        btnDelete.setDisable(!enabled);
        btnHealth.setDisable(!enabled);
        btnHistory.setDisable(!enabled);
        btnDetailEdit.setDisable(!enabled);
        btnDetailHealth.setDisable(!enabled);
        btnDetailHistory.setDisable(!enabled);
        btnAddHealth.setDisable(!enabled);
        selectionInfo.setText(enabled ? "1 thú cưng được chọn" : "");
    }

    // ── Data loading ─────────────────────────────────────────────

    private void loadStats() {
        // TODO: gọi PetBUS.getStats()
        statTotalPets.setText("0");
        statPetsStaying.setText("0");
        statPetsGrooming.setText("0");
        statPetsMonitoring.setText("0");
    }

    private void loadPets() {
        // TODO: gọi PetBUS.getAll() hoặc PetBUS.search(keyword, species, health, page)
        ObservableList<Object> data = FXCollections.observableArrayList();
        petTable.setItems(data);
        updatePageInfo(0, 0);
    }

    private void updatePageInfo(int current, int total) {
        pageInfo.setText("Hiển thị " + current + " / " + total + " thú cưng");
    }

    // ── Detail panel ─────────────────────────────────────────────

    private void showPetDetail(Object pet) {
        noSelectionHint.setVisible(false);
        noSelectionHint.setManaged(false);
        // TODO: bind từng label với dữ liệu thực của pet
        // detailPetName.setText(pet.getName());
        // detailPetSpecies.setText(pet.getSpecies() + " / " + pet.getBreed());
        // ...
        detailPetName.setText("—");
        detailPetSpecies.setText("—");
        detailPetId.setText("—");
        detailPetDob.setText("—");
        detailPetWeight.setText("—");
        detailPetColor.setText("—");
        detailOwner.setText("—");
        detailOwnerPhone.setText("—");
        detailPetNote.setText("—");
        loadHealthLog();
    }

    private void showNoSelection() {
        noSelectionHint.setVisible(true);
        noSelectionHint.setManaged(true);
    }

    private void loadHealthLog() {
        // TODO: gọi HealthBUS.getByPet(selectedPet.getId())
        healthLogList.getChildren().clear();
        healthEmptyHint.setVisible(true);
    }

    // ── FXML Event Handlers ──────────────────────────────────────

    @FXML
    public void onSearch() {
        String keyword = searchField.getText().trim();
        String species = filterSpecies.getValue();
        String health  = filterHealth.getValue();
        // TODO: gọi PetBUS.search(keyword, species, health, currentPage)
        System.out.println("Tìm thú cưng: keyword=" + keyword
                + " | species=" + species + " | health=" + health);
        loadPets();
    }

    @FXML
    public void onClearFilter() {
        searchField.clear();
        filterSpecies.setValue(null);
        filterHealth.setValue(null);
        loadPets();
    }

    @FXML
    public void onTableClick(MouseEvent event) {
        // double-click → mở form sửa
        if (event.getClickCount() == 2 && selectedPet != null) {
            openPetForm(selectedPet);
        }
    }

    @FXML
    public void onAddPet(ActionEvent event) {
        openPetForm(null);
    }

    @FXML
    public void onEditPet(ActionEvent event) {
        if (selectedPet == null) return;
        openPetForm(selectedPet);
    }

    @FXML
    public void onDeletePet(ActionEvent event) {
        if (selectedPet == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Bạn có chắc muốn xóa thú cưng này không?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác Nhận Xóa");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                // TODO: PetBUS.delete(selectedPet.getId())
                loadPets();
                showNoSelection();
            }
        });
    }

    @FXML
    public void onHealthRecord(ActionEvent event) {
        if (selectedPet == null) return;
        // TODO: mở form ghi nhận sức khoẻ
        System.out.println("Mở form ghi nhận sức khoẻ...");
    }

    @FXML
    public void onServiceHistory(ActionEvent event) {
        if (selectedPet == null) return;
        // TODO: mở panel / dialog lịch sử dịch vụ
        System.out.println("Mở lịch sử dịch vụ thú cưng...");
    }

    // ── Helpers ──────────────────────────────────────────────────

    /**
     * Mở form thêm / sửa thú cưng.
     * @param pet null = thêm mới, object = sửa
     */
    private void openPetForm(Object pet) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/PetHotel/gui/view/PetForm.fxml")
            );
            Parent root = loader.load();
            // PetFormController formCtrl = loader.getController();
            // formCtrl.setPet(pet);
            // formCtrl.setOnSave(this::loadPets);

            Stage dialog = new Stage();
            dialog.setTitle(pet == null ? "Thêm Thú Cưng" : "Sửa Thông Tin Thú Cưng");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
            loadPets();
        } catch (IOException e) {
            System.err.println("Không mở được PetForm.fxml: " + e.getMessage());
        }
    }
}
