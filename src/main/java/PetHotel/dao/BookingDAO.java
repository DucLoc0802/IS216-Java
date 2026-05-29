package PetHotel.dao;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import PetHotel.model.Booking;
import PetHotel.util.DBConnection;

public class BookingDAO {

    private static final String SQL_FIND_ALL =
        "SELECT b.booking_id, b.customer_id, b.branch_id, " +
        "       b.checkin_expected_at, b.checkout_expected_at, " +
        "       b.status, b.deposit_amount, b.special_note, " +
        "       b.created_at, b.updated_at, " +
        "       c.full_name AS customer_name, " +
        "       COALESCE(" +
        "           (SELECT LISTAGG(p.pet_name, ', ') WITHIN GROUP (ORDER BY p.pet_name) " +
        "               FROM booking_room_pet brp " +
        "               JOIN pet p ON brp.pet_id = p.pet_id " +
        "               JOIN booking_room br2 ON brp.booking_room_id = br2.booking_room_id " +
        "               WHERE br2.booking_id = b.booking_id), " +
        "           (SELECT LISTAGG(p3.pet_name, ', ') WITHIN GROUP (ORDER BY p3.pet_name) " +
        "               FROM pet p3 WHERE p3.customer_id = b.customer_id)" +
        "       ) AS pet_name, " +
        "       r.room_number " +
        "FROM booking b " +
        "LEFT JOIN customer c ON b.customer_id = c.customer_id " +
        "LEFT JOIN booking_room br ON b.booking_id = br.booking_id " +
        "LEFT JOIN room r ON br.room_id = r.room_id " +
        "ORDER BY b.created_at DESC";

    private static final String SQL_SEARCH =
        "SELECT b.booking_id, b.customer_id, b.branch_id, " +
        "       b.checkin_expected_at, b.checkout_expected_at, " +
        "       b.status, b.deposit_amount, b.special_note, " +
        "       b.created_at, b.updated_at, " +
        "       c.full_name AS customer_name, " +
        "       COALESCE(" +
        "           (SELECT LISTAGG(p.pet_name, ', ') WITHIN GROUP (ORDER BY p.pet_name) " +
        "               FROM booking_room_pet brp " +
        "               JOIN pet p ON brp.pet_id = p.pet_id " +
        "               JOIN booking_room br2 ON brp.booking_room_id = br2.booking_room_id " +
        "               WHERE br2.booking_id = b.booking_id), " +
        "           (SELECT LISTAGG(p3.pet_name, ', ') WITHIN GROUP (ORDER BY p3.pet_name) " +
        "               FROM pet p3 WHERE p3.customer_id = b.customer_id)" +
        "       ) AS pet_name, " +
        "       r.room_number " +
        "FROM booking b " +
        "LEFT JOIN customer c ON b.customer_id = c.customer_id " +
        "LEFT JOIN booking_room br ON b.booking_id = br.booking_id " +
        "LEFT JOIN room r ON br.room_id = r.room_id " +
        "WHERE (LOWER(b.booking_id) LIKE LOWER(?) " +
        "   OR LOWER(c.full_name) LIKE LOWER(?) " +
        "   OR LOWER(COALESCE(" +
        "       (SELECT LISTAGG(p2.pet_name, ', ') WITHIN GROUP (ORDER BY p2.pet_name) " +
        "           FROM booking_room_pet brp2 " +
        "           JOIN pet p2 ON brp2.pet_id = p2.pet_id " +
        "           JOIN booking_room br3 ON brp2.booking_room_id = br3.booking_room_id " +
        "           WHERE br3.booking_id = b.booking_id), " +
        "       (SELECT LISTAGG(p4.pet_name, ', ') WITHIN GROUP (ORDER BY p4.pet_name) " +
        "           FROM pet p4 WHERE p4.customer_id = b.customer_id)" +
        "   )) LIKE LOWER(?)) " +
        "  AND (? = 'ALL' OR b.status = ?) " +
        "ORDER BY b.created_at DESC";

    private static final String SQL_FIND_BY_ID =
        "SELECT b.booking_id, b.customer_id, b.branch_id, " +
        "       b.checkin_expected_at, b.checkout_expected_at, " +
        "       b.status, b.deposit_amount, b.special_note, " +
        "       b.created_at, b.updated_at, " +
        "       c.full_name AS customer_name, " +
        "       p.pet_name, " +
        "       r.room_number " +
        "FROM booking b " +
        "LEFT JOIN customer c ON b.customer_id = c.customer_id " +
        "LEFT JOIN booking_room br ON b.booking_id = br.booking_id " +
        "LEFT JOIN room r ON br.room_id = r.room_id " +
        "LEFT JOIN booking_room_pet brp ON br.booking_room_id = brp.booking_room_id " +
        "LEFT JOIN pet p ON brp.pet_id = p.pet_id " +
        "WHERE b.booking_id = ?";
    
    private static final String SQL_INSERT =
        "INSERT INTO booking (booking_id, customer_id, branch_id, " +
        "       checkin_expected_at, checkout_expected_at, status, " +
        "       deposit_amount, special_note, created_at, updated_at) " +
        "VALUES (?, ?, ?, ?, ?, 'PENDING', ?, ?, SYSTIMESTAMP, SYSTIMESTAMP)";

    private static final String SQL_UPDATE_STATUS =
        "UPDATE booking SET status = ?, updated_at = SYSTIMESTAMP " +
        "WHERE booking_id = ?";

    private static final String SQL_INSERT_BOOKING_ROOM =
        "INSERT INTO booking_room (booking_room_id, booking_id, room_id, assigned_at) " +
        "VALUES (?, ?, ?, SYSTIMESTAMP)";

    // ── Public Methods ───────────────────────────────────────────

    public List<Booking> findAll() throws SQLException {
        List<Booking> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Booking> search(String keyword, String status) throws SQLException {
        String pattern = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        String statusParam = (status == null) ? "ALL" : status;
        List<Booking> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SEARCH)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, statusParam);
            ps.setString(5, statusParam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    private static final String SQL_INSERT_BOOKING_ROOM_PET =
        "INSERT INTO booking_room_pet (booking_room_id, pet_id, assigned_at) " +
        "VALUES (?, ?, SYSTIMESTAMP)";

    public void insert(Booking booking, String roomId, String petId, Connection conn) throws SQLException {
        boolean own = (conn == null);
        Connection c = own ? DBConnection.getConnection() : conn;
        try {
            if (own) c.setAutoCommit(false);
            PreparedStatement ps = c.prepareStatement(SQL_INSERT);
            ps.setString(1, booking.getBookingId());
            ps.setString(2, booking.getCustomerId());
            ps.setString(3, booking.getBranchId());
            ps.setTimestamp(4, booking.getCheckinExpectedAt() != null ?
                Timestamp.from(booking.getCheckinExpectedAt().toInstant()) : null);
            ps.setTimestamp(5, booking.getCheckoutExpectedAt() != null ?
                Timestamp.from(booking.getCheckoutExpectedAt().toInstant()) : null);
            ps.setBigDecimal(6, booking.getDepositAmount() != null ?
                booking.getDepositAmount() : java.math.BigDecimal.ZERO);
            ps.setString(7, booking.getSpecialNote());
            ps.executeUpdate();
            ps.close();

            // Insert booking_room
            String bookingRoomId = "BR" + String.format("%08d", (int)(Math.random() * 99999999));
            PreparedStatement ps2 = c.prepareStatement(SQL_INSERT_BOOKING_ROOM);
            ps2.setString(1, bookingRoomId);
            ps2.setString(2, booking.getBookingId());
            ps2.setString(3, roomId);
            ps2.executeUpdate();
            ps2.close();

            // Insert booking_room_pet if petId is provided
            if (petId != null && !petId.trim().isEmpty()) {
                PreparedStatement ps3 = c.prepareStatement(SQL_INSERT_BOOKING_ROOM_PET);
                ps3.setString(1, bookingRoomId);
                ps3.setString(2, petId);
                ps3.executeUpdate();
                ps3.close();
            }

            if (own) c.commit();
        } catch (SQLException e) {
            if (own) try { c.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            throw e;
        } finally {
            if (own) {
                try { c.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
                DBConnection.closeQuietly(c);
            }
        }
    }

    public void insert(Booking booking, String roomId, Connection conn) throws SQLException {
        insert(booking, roomId, null, conn);
    }

    public int updateStatus(String bookingId, String newStatus) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // quản lý transaction
            try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_STATUS)) {
                ps.setString(1, newStatus);
                ps.setString(2, bookingId);
                int result = ps.executeUpdate();
                conn.commit();
                return result;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private static final String SQL_UPDATE =
        "UPDATE booking SET checkin_expected_at = ?, checkout_expected_at = ?, " +
        "       deposit_amount = ?, special_note = ?, updated_at = SYSTIMESTAMP " +
        "WHERE booking_id = ?";

    private static final String SQL_UPDATE_SPECIAL_NOTE =
        "UPDATE booking SET special_note = ?, updated_at = SYSTIMESTAMP " +
        "WHERE booking_id = ?";

    private static final String SQL_UPDATE_BOOKING_ROOM =
        "UPDATE booking_room SET room_id = ?, assigned_at = SYSTIMESTAMP " +
        "WHERE booking_id = ?";

    private static final String SQL_DELETE_BOOKING_ROOM_PETS =
        "DELETE FROM booking_room_pet WHERE booking_room_id IN " +
        "(SELECT booking_room_id FROM booking_room WHERE booking_id = ?)";

    private static final String SQL_DELETE_BOOKING_ROOMS =
        "DELETE FROM booking_room WHERE booking_id = ?";

    private static final String SQL_DELETE_BOOKING =
        "DELETE FROM booking WHERE booking_id = ?";

    public void update(Booking booking, String newRoomId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Cập nhật thông tin booking
                try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
                    ps.setTimestamp(1, booking.getCheckinExpectedAt() != null ?
                        Timestamp.from(booking.getCheckinExpectedAt().toInstant()) : null);
                    ps.setTimestamp(2, booking.getCheckoutExpectedAt() != null ?
                        Timestamp.from(booking.getCheckoutExpectedAt().toInstant()) : null);
                    ps.setBigDecimal(3, booking.getDepositAmount() != null ?
                        booking.getDepositAmount() : java.math.BigDecimal.ZERO);
                    ps.setString(4, booking.getSpecialNote());
                    ps.setString(5, booking.getBookingId());
                    ps.executeUpdate();
                }

                // 2. Cập nhật phòng nếu có thay đổi (cùng transaction)
                if (newRoomId != null) {
                    try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_BOOKING_ROOM)) {
                        ps.setString(1, newRoomId);
                        ps.setString(2, booking.getBookingId());
                        int rows = ps.executeUpdate();
                        if (rows == 0) {
                            String bookingRoomId = "BR" + String.format("%08d", (int)(Math.random() * 100000000));
                            try (PreparedStatement psInsert = conn.prepareStatement(
                                    "INSERT INTO booking_room (booking_room_id, booking_id, room_id, assigned_at) VALUES (?, ?, ?, SYSTIMESTAMP)")) {
                                psInsert.setString(1, bookingRoomId);
                                psInsert.setString(2, booking.getBookingId());
                                psInsert.setString(3, newRoomId);
                                psInsert.executeUpdate();
                            }
                        }
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void update(Booking booking) throws SQLException {
        update(booking, null);
    }

    public void updateBookingRoom(String bookingId, String newRoomId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_BOOKING_ROOM)) {
                ps.setString(1, newRoomId);
                ps.setString(2, bookingId);
                ps.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void updateSpecialNote(String bookingId, String note) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_SPECIAL_NOTE)) {
                ps.setString(1, note);
                ps.setString(2, bookingId);
                ps.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public String findRoomIdByBookingId(String bookingId) throws SQLException {
        String sql = "SELECT room_id FROM booking_room WHERE booking_id = ? AND ROWNUM = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("room_id");
                }
            }
        }
        return null;
    }

    public void delete(String bookingId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Xóa booking_room_pet
                try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE_BOOKING_ROOM_PETS)) {
                    ps.setString(1, bookingId);
                    ps.executeUpdate();
                }

                // 2. Xóa booking_room
                try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE_BOOKING_ROOMS)) {
                    ps.setString(1, bookingId);
                    ps.executeUpdate();
                }

                // 3. Xóa booking
                try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE_BOOKING)) {
                    ps.setString(1, bookingId);
                    ps.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private static final String SQL_NEXT_BOOKING_ID =
        "SELECT NVL(MAX(TO_NUMBER(SUBSTR(booking_id, 4))), 0) + 1 FROM booking " +
        "WHERE REGEXP_LIKE(booking_id, '^BKD[0-9]+$')";

    public int getNextBookingNumber() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(SQL_NEXT_BOOKING_ID);
            ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 1;
        }
    }

    public String findBookingRoomId(String bookingId) throws SQLException {
        String sql = "SELECT booking_room_id FROM booking_room WHERE booking_id = ? AND ROWNUM = 1";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("booking_room_id") : null;
            }
        }
    }

    public void insertBookingRoomPet(String bookingRoomId, String petId) throws SQLException {
        String sql = "INSERT INTO booking_room_pet (booking_room_id, pet_id, assigned_at) " +
                    "VALUES (?, ?, SYSTIMESTAMP)";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, bookingRoomId);
                ps.setString(2, petId);
                ps.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public Booking findById(String bookingId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {

            ps.setString(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }

        return null;
    }

    // ── Private Helpers ──────────────────────────────────────────

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getString("booking_id"));
        b.setCustomerId(rs.getString("customer_id"));
        b.setBranchId(rs.getString("branch_id"));
        b.setStatus(rs.getString("status").trim());
        b.setDepositAmount(rs.getBigDecimal("deposit_amount"));

        Timestamp checkin = rs.getTimestamp("checkin_expected_at");
        if (checkin != null)
            b.setCheckinExpectedAt(checkin.toInstant().atOffset(java.time.ZoneOffset.UTC));

        Timestamp checkout = rs.getTimestamp("checkout_expected_at");
        if (checkout != null)
            b.setCheckoutExpectedAt(checkout.toInstant().atOffset(java.time.ZoneOffset.UTC));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null)
            b.setCreatedAt(createdAt.toInstant().atOffset(java.time.ZoneOffset.UTC));

        b.setCustomerName(rs.getString("customer_name"));
        b.setPetName(rs.getString("pet_name"));
        b.setRoomNumber(rs.getString("room_number"));

        Clob note = rs.getClob("special_note");
        if (note != null)
            b.setSpecialNote(note.getSubString(1, (int) note.length()));

        return b;
    }
}