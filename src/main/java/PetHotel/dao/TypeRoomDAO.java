package PetHotel.dao;

import PetHotel.model.TypeRoom;
import PetHotel.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TypeRoomDAO {

    private static final String SQL_FIND_ALL =
        "SELECT type_room_id, type_name, max_pets, max_weight_kg, base_price_per_day, is_active " +
        "FROM type_room WHERE is_active = 1 ORDER BY type_room_id";

    public List<TypeRoom> findAll() throws SQLException {
        List<TypeRoom> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TypeRoom t = new TypeRoom();
                t.setTypeRoomId(rs.getString("type_room_id"));
                t.setTypeName(rs.getString("type_name"));
                t.setMaxPets(rs.getInt("max_pets"));
                t.setMaxWeightKg(rs.getDouble("max_weight_kg"));
                t.setBasePricePerDay(rs.getDouble("base_price_per_day"));
                t.setActive(rs.getInt("is_active") == 1);
                list.add(t);
            }
        }
        return list;
    }
}