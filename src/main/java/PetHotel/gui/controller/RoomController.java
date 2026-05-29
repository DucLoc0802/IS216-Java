package PetHotel.gui.controller;

import PetHotel.bus.RoomBUS;
import PetHotel.model.Room;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class RoomController {

    // ── FXML fields — tên phải khớp fx:id trong FXML ────────────
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatus;
    @FXML private ComboBox<String> filterType;
    @FXML private TableView<Room> roomTable;

    @FXML private TableColumn<Room, String> colRoomId;
    @FXML private TableColumn<Room, String> colRoomNumber;
    @FXML private TableColumn<Room, String> colRoomType;
    @FXML private TableColumn<Room, String> colRoomFloor;
    @FXML private TableColumn<Room, Double> colRoomPrice;
    @FXML private TableColumn<Room, Integer> colRoomCap;
    @FXML private TableColumn<Room, String> colRoomStatus;
    @FXML private TableColumn<Room, String> colRoomPet;
    @FXML private TableColumn<Room, String> colRoomAction;

    @FXML private Button btnClearFilter;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Label countOccupied;
    @FXML private Label countAvailable;
    @FXML private Label countCleaning;
    @FXML private Label countMaintenance;

    @FXML private Pagination pagination;

    private final RoomBUS roomBUS = new RoomBUS();

    @FXML
    public void initialize() {
        setupColumns();
        setupComboBoxes();
        loadRooms();

        roomTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            boolean selected = newVal != null;
            btnEdit.setDisable(!selected);
            btnDelete.setDisable(!selected);
        });
    }

    private void setupColumns() {
        roomTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        roomTable.setFixedCellSize(36);
        colRoomId.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getRoomId()));
        colRoomNumber.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getRoomNumber()));
        colRoomType.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getTypeName()));
        colRoomFloor.setCellValueFactory(d ->
            new SimpleStringProperty("—"));
        colRoomPrice.setCellValueFactory(d ->
            new SimpleObjectProperty<>(d.getValue().getBasePricePerDay()));
        colRoomCap.setCellValueFactory(d ->
            new SimpleObjectProperty<>(d.getValue().getMaxPets()));
        colRoomStatus.setCellValueFactory(d -> {
            Room room = d.getValue();
            // Tự động suy luận trạng thái dựa vào thú cưng hiện tại
            String petNames = room.getCurrentPetNames();
            if (petNames != null && !petNames.isEmpty() && !"AVAILABLE".equals(room.getStatus())) {
                // Giữ nguyên IN_USE nếu có thú cưng
            } else if (petNames != null && !petNames.isEmpty()) {
                room.setStatus("IN_USE");
            }
            return new SimpleStringProperty(room.getStatus());
        });
        colRoomPet.setCellValueFactory(d -> {
            String petNames = d.getValue().getCurrentPetNames();
            return new SimpleStringProperty(petNames != null && !petNames.isEmpty() ? petNames : "—");
        });

    // Cột Thao Tác — có nút Cập nhật trạng thái
    colRoomAction.setCellFactory(col -> new TableCell<>() {
        private final Button btnStatus = new Button("Trạng thái");

        {
            btnStatus.setOnAction(e -> {
                Room room = getTableView().getItems().get(getIndex());
                showStatusDialog(room);
            });
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : btnStatus);
        }
    });
}
    private void setupComboBoxes() {
        filterStatus.setItems(FXCollections.observableArrayList(
            "Tất cả", "AVAILABLE", "IN_USE", "MAINTENANCE"
        ));
        filterStatus.setValue("Tất cả");

        filterType.setItems(FXCollections.observableArrayList(
            "Tất cả", "STANDARD", "PREMIUM", "SUITE"
        ));
        filterType.setValue("Tất cả");
    }

    private void loadRooms() {
        try {
            List<Room> rooms = roomBUS.getAllRooms();
            roomTable.setItems(FXCollections.observableArrayList(rooms));
            updateStatusCounts(rooms);
        } catch (Exception e) {
            showAlert("Lỗi", "Không thể tải danh sách phòng: " + e.getMessage());
        }
    }

    private void updateStatusCounts(List<Room> rooms) {
        long inUse     = rooms.stream().filter(r -> "IN_USE".equals(r.getStatus())).count();
        long available = rooms.stream().filter(r -> "AVAILABLE".equals(r.getStatus())).count();
        long maintain  = rooms.stream().filter(r -> "MAINTENANCE".equals(r.getStatus())).count();

        countOccupied.setText(String.valueOf(inUse));
        countAvailable.setText(String.valueOf(available));
        countCleaning.setText("0"); // DB không có CLEANING
        countMaintenance.setText(String.valueOf(maintain));
    }

    @FXML
    public void onSearch(ActionEvent event) { onFilter(event); }

    @FXML
    public void onFilter(ActionEvent event) {
        String keyword = searchField.getText();
        String status  = "Tất cả".equals(filterStatus.getValue()) ? null : filterStatus.getValue();
        String type    = "Tất cả".equals(filterType.getValue())   ? null : filterType.getValue();

        try {
            List<Room> rooms = roomBUS.searchRooms(keyword, status, type);
            roomTable.setItems(FXCollections.observableArrayList(rooms));
            updateStatusCounts(rooms);
        } catch (Exception e) {
            showAlert("Lỗi", "Lỗi tìm kiếm: " + e.getMessage());
        }
    }

    @FXML
    public void onAddRoom(ActionEvent event) {
    try {
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
            getClass().getResource("/PetHotel/gui/view/AddRoomDialog.fxml"));
        javafx.scene.Parent root = loader.load();

        AddRoomController controller = loader.getController();
        controller.setOnSaveCallback(this::loadRooms);

        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.setTitle("Thêm Phòng Mới");
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setScene(new javafx.scene.Scene(root));
        dialog.showAndWait();

        } catch (Exception e) {
        showAlert("Lỗi", "Không thể mở form: " + e.getMessage());
        }
    }   

    @FXML
    public void onRoomTypes(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/PetHotel/gui/view/RoomTypeDialog.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage dialog = new javafx.stage.Stage();
            dialog.setTitle("Loại Phòng & Giá");
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialog.setScene(new javafx.scene.Scene(root));
            dialog.showAndWait();

        } catch (Exception e) {
        showAlert("Lỗi", "Không thể mở: " + e.getMessage());
        }
    }

    @FXML
    public void handleSearch(ActionEvent event) { onFilter(event); }

    @FXML
    public void handleAdd(ActionEvent event) { onAddRoom(event); }

    @FXML
    public void handleEdit(ActionEvent event) {
        Room selected = roomTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Thông báo", "Vui lòng chọn phòng cần sửa."); return; }
        showAlert("Thông báo", "Sửa phòng: " + selected.getRoomNumber());
    }

    @FXML
    public void handleDelete(ActionEvent event) {
        Room selected = roomTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Thông báo", "Vui lòng chọn phòng cần xóa."); return; }
        try {
            roomBUS.deleteRoom(selected.getRoomId());
            loadRooms();
            showAlert("Thành công", "Đã xóa phòng " + selected.getRoomNumber());
        } catch (Exception e) {
            showAlert("Lỗi", e.getMessage());
        }
    }

    // Các phương thức cho nút mới trong action-toolbar và filter-bar
    @FXML public void onClearFilter(ActionEvent event) {
        searchField.clear();
        filterStatus.setValue("Tất cả");
        filterType.setValue("Tất cả");
        loadRooms();
    }

    @FXML public void onEditRoom(ActionEvent event) {
        Room selected = roomTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Thông báo", "Vui lòng chọn phòng."); return; }
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/PetHotel/gui/view/EditRoomDialog.fxml"));
            javafx.scene.Parent root = loader.load();

            EditRoomController controller = loader.getController();
            controller.setRoom(selected);
            controller.setOnSaveCallback(this::loadRooms);

            javafx.stage.Stage dialog = new javafx.stage.Stage();
            dialog.setTitle("Sửa Phòng - " + selected.getRoomNumber());
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialog.setScene(new javafx.scene.Scene(root));
            dialog.showAndWait();
        } catch (Exception e) {
            showAlert("Lỗi", "Không thể mở form: " + e.getMessage());
        }
    }

    @FXML public void onDeleteRoom(ActionEvent event) {
        Room selected = roomTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Thông báo", "Vui lòng chọn phòng."); return; }
        try {
            roomBUS.deleteRoom(selected.getRoomId());
            loadRooms();
            showAlert("Thành công", "Đã xóa phòng " + selected.getRoomNumber());
        } catch (Exception e) {
            showAlert("Lỗi", e.getMessage());
        }
    }

    @FXML
    public void handleMarkAsCleaned(ActionEvent event) {
        Room selected = roomTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Thông báo", "Vui lòng chọn phòng."); return; }
        try {
            roomBUS.updateRoomStatus(selected.getRoomId(), "AVAILABLE");
            loadRooms();
            showAlert("Thành công", "Đã cập nhật trạng thái phòng.");
        } catch (Exception e) {
            showAlert("Lỗi", e.getMessage());
        }
    }

    private void showStatusDialog(Room room) {
    List<String> statuses = List.of("AVAILABLE", "IN_USE", "MAINTENANCE");

    ChoiceDialog<String> dialog = new ChoiceDialog<>(room.getStatus(), statuses);
    dialog.setTitle("Cập nhật trạng thái");
    dialog.setHeaderText("Phòng: " + room.getRoomNumber());
    dialog.setContentText("Chọn trạng thái mới:");

    dialog.showAndWait().ifPresent(newStatus -> {
        try {
            roomBUS.updateRoomStatus(room.getRoomId(), newStatus);
            loadRooms();
            showAlert("Thành công", "Đã cập nhật trạng thái phòng " + room.getRoomNumber());
        } catch (Exception e) {
            showAlert("Lỗi", e.getMessage());
            }
        });
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}