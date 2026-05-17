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

public class AddRoomController {

    @FXML private TextField txtRoomNumber;
    @FXML private ComboBox<TypeRoom> cbRoomType;
    @FXML private ComboBox<String> cbStatus;
    @FXML private Label lblError;

    private final RoomBUS roomBUS = new RoomBUS();
    private final TypeRoomBUS typeRoomBUS = new TypeRoomBUS();

    /** Callback để RoomController biết khi nào lưu xong */
    private Runnable onSaveCallback;

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    public void initialize() {
        // Load danh sách loại phòng
        try {
            List<TypeRoom> types = typeRoomBUS.getAllTypeRooms();
            cbRoomType.setItems(FXCollections.observableArrayList(types));
            cbRoomType.setConverter(new javafx.util.StringConverter<>() {
                public String toString(TypeRoom t) { return t == null ? "" : t.getTypeName(); }
                public TypeRoom fromString(String s) { return null; }
            });
        } catch (Exception e) {
            lblError.setText("Lỗi tải loại phòng: " + e.getMessage());
        }

        cbStatus.setItems(FXCollections.observableArrayList(
            "AVAILABLE", "MAINTENANCE"
        ));
        cbStatus.setValue("AVAILABLE");
    }

    @FXML
    public void onSave(ActionEvent event) {
        lblError.setText("");

        // Validate
        if (txtRoomNumber.getText().trim().isEmpty()) {
            lblError.setText("Vui lòng nhập số phòng.");
            return;
        }
        if (cbRoomType.getValue() == null) {
            lblError.setText("Vui lòng chọn loại phòng.");
            return;
        }

        try {
            Room room = new Room();
            room.setRoomNumber(txtRoomNumber.getText().trim());
            room.setTypeRoomId(cbRoomType.getValue().getTypeRoomId());
            room.setBranchId("BR001"); // TODO: lấy từ session
            room.setStatus(cbStatus.getValue());

            roomBUS.addRoom(room);

            if (onSaveCallback != null) onSaveCallback.run();
            closeDialog();

        } catch (Exception e) {
            lblError.setText(e.getMessage());
        }
    }

    @FXML
    public void onCancel(ActionEvent event) {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    @FXML private Button btnCancel;
}
