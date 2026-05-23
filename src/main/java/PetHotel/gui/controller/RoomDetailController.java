package PetHotel.gui.controller;

import PetHotel.model.Room;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * UC-ROOM-03: Xem chi tiết phòng
 * Hiển thị thông tin đầy đủ của một phòng (read-only).
 */
public class RoomDetailController {

    @FXML private Label lblRoomId;
    @FXML private Label lblRoomNumber;
    @FXML private Label lblTypeName;
    @FXML private Label lblStatus;
    @FXML private Label lblPrice;
    @FXML private Label lblMaxPets;
    @FXML private Label lblMaxWeight;
    @FXML private Label lblBranchId;
    @FXML private Button btnClose;

    /**
     * Điền dữ liệu phòng vào các label.
     * Gọi từ RoomController sau khi load FXML.
     */
    public void setRoom(Room room) {
        if (room == null) return;

        lblRoomId.setText(nvl(room.getRoomId()));
        lblRoomNumber.setText(nvl(room.getRoomNumber()));
        lblTypeName.setText(nvl(room.getTypeName()));
        lblStatus.setText(translateStatus(room.getStatus()));

        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        lblPrice.setText(nf.format(room.getBasePricePerDay()) + " VNĐ");
        lblMaxPets.setText(String.valueOf(room.getMaxPets()) + " thú");
        lblMaxWeight.setText(room.getMaxWeightKg() > 0
                ? room.getMaxWeightKg() + " kg" : "—");
        lblBranchId.setText(nvl(room.getBranchId()));
    }

    // ── FXML handlers ────────────────────────────────────────────

    @FXML
    public void onClose(ActionEvent event) {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    // ── Helpers ──────────────────────────────────────────────────

    private String nvl(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }

    private String translateStatus(String status) {
        if (status == null) return "—";
        return switch (status.trim()) {
            case "AVAILABLE"   -> "Trống";
            case "IN_USE"      -> "Đang sử dụng";
            case "MAINTENANCE" -> "Bảo trì";
            case "CLEANING"    -> "Đang dọn";
            default            -> status;
        };
    }
}
