package PetHotel.gui.controller;

import PetHotel.bus.RoomBUS;
import PetHotel.bus.TypeRoomBUS;
import PetHotel.model.Room;
import PetHotel.model.TypeRoom;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

public class EditRoomController {

    @FXML private TextField txtRoomNumber;
    @FXML private ComboBox<String> cbStatus;
    @FXML private ComboBox<TypeRoom> cbRoomType;
    @FXML private Label lblError;
    @FXML private Button btnCancel;

    private final RoomBUS roomBUS = new RoomBUS();
    private final TypeRoomBUS typeRoomBUS = new TypeRoomBUS();
    private Room currentRoom;
    private Runnable onSaveCallback;

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    public void setRoom(Room room) {
        this.currentRoom = room;
        txtRoomNumber.setText(room.getRoomNumber());
        cbStatus.setValue(room.getStatus());
        cbRoomType.getItems().forEach(t -> {
            if (t.getTypeRoomId().equals(room.getTypeRoomId()))
                cbRoomType.setValue(t);
        });
    }

    @FXML
    public void initialize() {
        cbStatus.setItems(FXCollections.observableArrayList(
            "AVAILABLE", "IN_USE", "MAINTENANCE"
        ));
        try {
            List<TypeRoom> types = typeRoomBUS.getAllTypeRooms();
            cbRoomType.setItems(FXCollections.observableArrayList(types));
            cbRoomType.setConverter(new javafx.util.StringConverter<>() {
                public String toString(TypeRoom t) {
                    return t == null ? "" : t.getTypeName();
                }
                public TypeRoom fromString(String s) { return null; }
            });
        } catch (Exception e) {
            lblError.setText("Lỗi tải loại phòng: " + e.getMessage());
            lblError.setVisible(true);
        }
    }

    @FXML
    public void onSave(ActionEvent event) {
        lblError.setVisible(false);
        if (txtRoomNumber.getText().trim().isEmpty()) {
            lblError.setText("Số phòng không được để trống.");
            lblError.setVisible(true);
            return;
        }
        if (cbRoomType.getValue() == null) {
            lblError.setText("Vui lòng chọn loại phòng.");
            lblError.setVisible(true);
            return;
        }
        try {
            currentRoom.setRoomNumber(txtRoomNumber.getText().trim());
            currentRoom.setStatus(cbStatus.getValue());
            currentRoom.setTypeRoomId(cbRoomType.getValue().getTypeRoomId());
            roomBUS.updateRoom(currentRoom);
            if (onSaveCallback != null) onSaveCallback.run();
            closeDialog();
        } catch (Exception e) {
            lblError.setText(e.getMessage());
            lblError.setVisible(true);
        }
    }

    @FXML
    public void onCancel(ActionEvent event) { closeDialog(); }

    private void closeDialog() {
        ((Stage) btnCancel.getScene().getWindow()).close();
    }
}