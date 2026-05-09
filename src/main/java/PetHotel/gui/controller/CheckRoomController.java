package PetHotel.gui.controller;

import PetHotel.bus.RoomBUS;
import PetHotel.dao.RoomDAO;
import PetHotel.model.Room;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CheckRoomController {

    @FXML private DatePicker dateCheckin;
    @FXML private DatePicker dateCheckout;
    @FXML private ComboBox<String> cbRoomType;
    @FXML private TableView<Room> availableRoomTable;
    @FXML private TableColumn<Room, String> colRoomNumber;
    @FXML private TableColumn<Room, String> colRoomType;
    @FXML private TableColumn<Room, Double> colPrice;
    @FXML private TableColumn<Room, Integer> colMaxPets;
    @FXML private TableColumn<Room, String> colStatus;
    @FXML private Label lblResult;

    private final RoomBUS roomBUS = new RoomBUS();

    @FXML
    public void initialize() {
        // Setup columns
        colRoomNumber.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getRoomNumber()));
        colRoomType.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getTypeName()));
        colPrice.setCellValueFactory(d ->
            new SimpleObjectProperty<>(d.getValue().getBasePricePerDay()));
        colMaxPets.setCellValueFactory(d ->
            new SimpleObjectProperty<>(d.getValue().getMaxPets()));
        colStatus.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getStatus()));

        // Setup combobox loại phòng
        cbRoomType.setItems(FXCollections.observableArrayList(
            "Tất cả", "STANDARD", "PREMIUM", "SUITE"
        ));
        cbRoomType.setValue("Tất cả");

        // Mặc định ngày hôm nay và ngày mai
        dateCheckin.setValue(LocalDate.now());
        dateCheckout.setValue(LocalDate.now().plusDays(1));
    }

    @FXML
    public void onSearch(ActionEvent event) {
        // Validate ngày
        if (dateCheckin.getValue() == null || dateCheckout.getValue() == null) {
            lblResult.setText("⚠ Vui lòng chọn ngày check-in và check-out.");
            return;
        }
        if (!dateCheckout.getValue().isAfter(dateCheckin.getValue())) {
            lblResult.setText("⚠ Ngày check-out phải sau ngày check-in.");
            return;
        }

        try {
            String typeFilter = "Tất cả".equals(cbRoomType.getValue()) ? null : cbRoomType.getValue();
            List<Room> availableRooms = getAvailableRooms(
                dateCheckin.getValue(),
                dateCheckout.getValue(),
                typeFilter
            );

            availableRoomTable.setItems(FXCollections.observableArrayList(availableRooms));

            if (availableRooms.isEmpty()) {
                lblResult.setText("❌ Không có phòng trống trong khoảng thời gian này.");
            } else {
                lblResult.setText("✅ Tìm thấy " + availableRooms.size() + " phòng trống.");
            }

        } catch (Exception e) {
            lblResult.setText("Lỗi: " + e.getMessage());
        }
    }

    private List<Room> getAvailableRooms(LocalDate checkin, LocalDate checkout, String typeFilter)
            throws Exception {

        String sql =
        "SELECT r.room_id, r.branch_id, r.type_room_id, r.room_number, r.status, r.created_at, " +
        "       t.type_name, t.base_price_per_day, t.max_pets, t.max_weight_kg " +
        "FROM room r " +
        "JOIN type_room t ON r.type_room_id = t.type_room_id " +
        "WHERE r.status = 'AVAILABLE' " +
        "  AND (? IS NULL OR t.type_name = ?) " +
        "  AND r.room_id NOT IN ( " +
        "      SELECT br.room_id FROM booking_room br " +
        "      JOIN booking b ON br.booking_id = b.booking_id " +
        "      WHERE b.status NOT IN ('CANCELLED','CHECKED_OUT') " +
        "        AND b.checkin_expected_at < ? AND b.checkout_expected_at > ? " +
        "  ) " +
        "ORDER BY r.room_number";

        List<Room> list = new ArrayList<>();
        try (Connection conn = PetHotel.util.DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, typeFilter);
            ps.setString(2, typeFilter);
            ps.setDate(3, java.sql.Date.valueOf(checkout));
            ps.setDate(4, java.sql.Date.valueOf(checkin));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Room r = new Room();
                    r.setRoomId(rs.getString("room_id"));
                    r.setRoomNumber(rs.getString("room_number"));
                    r.setStatus(rs.getString("status").trim());
                    r.setTypeName(rs.getString("type_name"));
                    r.setBasePricePerDay(rs.getDouble("base_price_per_day"));
                    r.setMaxPets(rs.getInt("max_pets"));
                    r.setTypeRoomId(rs.getString("type_room_id"));
                    r.setBranchId(rs.getString("branch_id"));
                    list.add(r);
                }
            }
        }
        return list;
    }

    @FXML
    public void onClose(ActionEvent event) {
        Stage stage = (Stage) availableRoomTable.getScene().getWindow();
        stage.close();
    }
}