package PetHotel.dao;

import PetHotel.model.Room;
import PetHotel.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    private static final String SQL_FIND_ALL =
        "SELECT r.room_id, r.branch_id, r.type_room_id, r.room_number, r.status, r.created_at, " +
        "       t.type_name, t.base_price_per_day, t.max_pets, t.max_weight_kg " +
        "FROM room r " +
        "JOIN type_room t ON r.type_room_id = t.type_room_id " +
        "ORDER BY r.room_number";

    private static final String SQL_SEARCH =
        "SELECT r.room_id, r.branch_id, r.type_room_id, r.room_number, r.status, r.created_at, " +
        "       t.type_name, t.base_price_per_day, t.max_pets, t.max_weight_kg " +
        "FROM room r " +
        "JOIN type_room t ON r.type_room_id = t.type_room_id " +
        "WHERE (LOWER(r.room_number) LIKE LOWER(?) OR LOWER(r.room_id) LIKE LOWER(?) OR LOWER(t.type_name) LIKE LOWER(?)) " +
        "  AND (? = 'ALL' OR r.status = ?) " +
        "  AND (? = 'ALL' OR t.type_name = ?) " +
        "ORDER BY r.room_number";

    private static final String SQL_FIND_BY_BOOKING =
        "SELECT r.room_id, r.branch_id, r.type_room_id, r.room_number, r.status, r.created_at, " +
        "       t.type_name, t.base_price_per_day, t.max_pets, t.max_weight_kg " +
        "FROM booking_room br " +
        "JOIN room r ON br.room_id = r.room_id " +
        "JOIN type_room t ON r.type_room_id = t.type_room_id " +
        "WHERE br.booking_id = ?";

    private static final String SQL_INSERT =
        "INSERT INTO room (room_id, branch_id, type_room_id, room_number, status, created_at) " +
        "VALUES (?, ?, ?, ?, ?, SYSTIMESTAMP)";

    private static final String SQL_UPDATE_STATUS =
        "UPDATE room SET status = ? WHERE room_id = ?";

    private static final String SQL_UPDATE =
        "UPDATE room SET type_room_id = ?, room_number = ?, status = ? WHERE room_id = ?";

    private static final String SQL_DELETE =
        "DELETE FROM room WHERE room_id = ?";

    private static final String SQL_COUNT_ACTIVE_BOOKINGS =
        "SELECT COUNT(*) FROM booking b " +
        "JOIN booking_room br ON b.booking_id = br.booking_id " +
        "WHERE br.room_id = ? AND b.status NOT IN ('CHECKED_OUT','CANCELLED')";

    private static final String SQL_EXISTS_ROOM_NUMBER =
        "SELECT COUNT(*) FROM room WHERE room_number = ? AND room_id != ?";

    private static final String SQL_COUNT_BY_STATUS =
        "SELECT status, COUNT(*) as cnt FROM room GROUP BY status";

    private static final String SQL_FIND_CURRENT_PETS_BY_ROOM =
        "SELECT LISTAGG(p.pet_name, ', ') WITHIN GROUP (ORDER BY p.pet_name) AS pet_names " +
        "FROM booking_room_pet brp " +
        "JOIN booking_room br ON brp.booking_room_id = br.booking_room_id " +
        "JOIN booking b ON br.booking_id = b.booking_id " +
        "JOIN pet p ON brp.pet_id = p.pet_id " +
        "WHERE br.room_id = ? " +
        "  AND b.status IN ('CHECKED_IN', 'PENDING', 'CONFIRMED')";

    // ── Public Methods ───────────────────────────────────────────

    public Room findByBookingId(String bookingId) throws SQLException {
    String sql = "SELECT r.room_id, r.room_number, r.status, " +
                 "       tr.type_name, tr.base_price_per_day " +
                 "FROM room r " +
                 "JOIN booking_room br ON r.room_id = br.room_id " +
                 "JOIN type_room tr ON r.type_room_id = tr.type_room_id " +
                 "WHERE br.booking_id = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, bookingId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                Room room = new Room();
                room.setRoomId(rs.getString("room_id"));
                room.setRoomNumber(rs.getString("room_number"));
                room.setStatus(rs.getString("status"));
                room.setTypeName(rs.getString("type_name"));
                room.setBasePricePerDay(rs.getDouble("base_price_per_day"));
                return room;
            }
        }
    }
    return null;
}

    public String findCurrentPetNamesByRoomId(String roomId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_CURRENT_PETS_BY_ROOM)) {
            ps.setString(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String names = rs.getString("pet_names");
                    return names != null ? names.trim() : null;
                }
            }
        }
        return null;
    }

    public List<Room> findAll() throws SQLException {
        List<Room> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Room room = mapRow(rs);
                room.setCurrentPetNames(findCurrentPetNamesByRoomId(room.getRoomId()));
                list.add(room);
            }
        }
        return list;
    }

    public List<Room> search(String keyword, String status, String typeRoomId) throws SQLException {
        String pattern = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        String statusParam = (status == null) ? "ALL" : status;
        String typeParam = (typeRoomId == null) ? "ALL" : typeRoomId;
        List<Room> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(SQL_SEARCH)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, statusParam);
            ps.setString(5, statusParam);
            ps.setString(6, typeParam);
            ps.setString(7, typeParam);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Room room = mapRow(rs);
                    room.setCurrentPetNames(findCurrentPetNamesByRoomId(room.getRoomId()));
                    list.add(room);
                }
            }
        }
        return list;
    }

    public void insert(Room room, Connection conn) throws SQLException {
        boolean own = (conn == null);
        Connection c = own ? DBConnection.getConnection() : conn;
        try {
            PreparedStatement ps = c.prepareStatement(SQL_INSERT);
            ps.setString(1, room.getRoomId());
            ps.setString(2, room.getBranchId());
            ps.setString(3, room.getTypeRoomId());
            ps.setString(4, room.getRoomNumber());
            ps.setString(5, room.getStatus());
            ps.executeUpdate();
            ps.close();
        } finally {
            if (own) DBConnection.closeQuietly(c);
        }
    }

    public int updateStatus(String roomId, String newStatus, Connection conn) throws SQLException {
        boolean own = (conn == null);
        Connection c = own ? DBConnection.getConnection() : conn;
        try {
            PreparedStatement ps = c.prepareStatement(SQL_UPDATE_STATUS);
            ps.setString(1, newStatus);
            ps.setString(2, roomId);
            int rows = ps.executeUpdate();
            ps.close();
            return rows;
        } finally {
            if (own) DBConnection.closeQuietly(c);
        }
    }

    public int update(Room room, Connection conn) throws SQLException {
        boolean own = (conn == null);
        Connection c = own ? DBConnection.getConnection() : conn;
        try {
            PreparedStatement ps = c.prepareStatement(SQL_UPDATE);
            ps.setString(1, room.getTypeRoomId());
            ps.setString(2, room.getRoomNumber());
            ps.setString(3, room.getStatus());
            ps.setString(4, room.getRoomId());
            int rows = ps.executeUpdate();
            ps.close();
            return rows;
        } finally {
            if (own) DBConnection.closeQuietly(c);
        }
    }

    public int delete(String roomId, Connection conn) throws SQLException {
        boolean own = (conn == null);
        Connection c = own ? DBConnection.getConnection() : conn;
        try {
            PreparedStatement ps = c.prepareStatement(SQL_DELETE);
            ps.setString(1, roomId);
            int rows = ps.executeUpdate();
            ps.close();
            return rows;
        } finally {
            if (own) DBConnection.closeQuietly(c);
        }
    }

    public int countActiveBookings(String roomId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT_ACTIVE_BOOKINGS)) {
            ps.setString(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public boolean existsByRoomNumber(String roomNumber, String excludeId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_EXISTS_ROOM_NUMBER)) {
            ps.setString(1, roomNumber);
            ps.setString(2, excludeId != null ? excludeId : "");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // ── Private Helpers ──────────────────────────────────────────

    private Room mapRow(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setRoomId(rs.getString("room_id"));
        r.setBranchId(rs.getString("branch_id"));
        r.setTypeRoomId(rs.getString("type_room_id"));
        r.setRoomNumber(rs.getString("room_number"));
        r.setStatus(rs.getString("status").trim());
        r.setTypeName(rs.getString("type_name"));
        r.setBasePricePerDay(rs.getDouble("base_price_per_day"));
        r.setMaxPets(rs.getInt("max_pets"));
        r.setMaxWeightKg(rs.getDouble("max_weight_kg"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null)
            r.setCreatedAt(createdAt.toInstant().atOffset(java.time.ZoneOffset.UTC));

        return r;
    }

    // ── Next ID ──────────────────────────────────────────────────

    private static final String SQL_NEXT_ID =
        "SELECT NVL(MAX(TO_NUMBER(SUBSTR(room_id, 5))), 0) + 1 FROM room " +
        "WHERE REGEXP_LIKE(room_id, '^ROOM[0-9]+$')";

    public int getNextRoomNumber() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_NEXT_ID);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 1;
        }
    }

}