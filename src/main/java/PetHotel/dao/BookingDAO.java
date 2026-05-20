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
        "       p.pet_name, " +
        "       r.room_number " +
        "FROM booking b " +
        "LEFT JOIN customer c ON b.customer_id = c.customer_id " +
        "LEFT JOIN booking_room br ON b.booking_id = br.booking_id " +
        "LEFT JOIN room r ON br.room_id = r.room_id " +
        "LEFT JOIN booking_room_pet brp ON br.booking_room_id = brp.booking_room_id " +
        "LEFT JOIN pet p ON brp.pet_id = p.pet_id " +
        "ORDER BY b.created_at DESC";

    private static final String SQL_SEARCH =
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
        "WHERE (LOWER(b.booking_id) LIKE LOWER(?) " +
        "   OR LOWER(c.full_name) LIKE LOWER(?) " +
        "   OR LOWER(p.pet_name) LIKE LOWER(?)) " +
        "  AND (? = 'ALL' OR b.status = ?) " +
        "ORDER BY b.created_at DESC";

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

    public void insert(Booking booking, String roomId, Connection conn) throws SQLException {
        boolean own = (conn == null);
        Connection c = own ? DBConnection.getConnection() : conn;
        try {
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

            PreparedStatement ps2 = c.prepareStatement(SQL_INSERT_BOOKING_ROOM);
            ps2.setString(1, "BR" + String.format("%08d", (int)(Math.random() * 99999999)));
            ps2.setString(2, booking.getBookingId());
            ps2.setString(3, roomId);
            ps2.executeUpdate();
            ps2.close();

            if (own) c.commit();
        } finally {
            if (own) DBConnection.closeQuietly(c);
        }
    }

    public int updateStatus(String bookingId, String newStatus) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_STATUS)) {
            ps.setString(1, newStatus);
            ps.setString(2, bookingId);
            return ps.executeUpdate();
        }
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