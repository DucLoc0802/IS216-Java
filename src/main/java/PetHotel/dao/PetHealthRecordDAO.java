package PetHotel.dao;

import PetHotel.model.PetHealthRecord;
import PetHotel.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PetHealthRecordDAO — Thao tác DB cho bảng PET_HEALTH_RECORD.
 *
 * TODO: Thêm update() nếu cần chỉnh sửa ghi chú sức khoẻ.
 * TODO: Thêm phân trang.
 */
public class PetHealthRecordDAO {

    private static final String SQL_INSERT =
        "INSERT INTO pet_health_record " +
        "  (health_record_id, pet_id, booking_id, recorded_at, note, status) " +
        "VALUES (?, ?, ?, SYSTIMESTAMP, ?, ?)";

    private static final String SQL_FIND_BY_PET =
        "SELECT health_record_id, pet_id, booking_id, recorded_at, note, status " +
        "FROM pet_health_record " +
        "WHERE pet_id = ? ORDER BY recorded_at DESC";

    private static final String SQL_FIND_BY_BOOKING =
        "SELECT health_record_id, pet_id, booking_id, recorded_at, note, status " +
        "FROM pet_health_record " +
        "WHERE booking_id = ? ORDER BY recorded_at DESC";

    private static final String SQL_FIND_LATEST_BY_PET =
        "SELECT health_record_id, pet_id, booking_id, recorded_at, note, status " +
        "FROM pet_health_record " +
        "WHERE pet_id = ? AND recorded_at = (" +
        "   SELECT MAX(recorded_at) FROM pet_health_record WHERE pet_id = ?)";

    // ── Public Methods ───────────────────────────────────────────

    /**
     * Thêm mới health record.
     *
     * @param record đối tượng PetHealthRecord đã có healthRecordId, petId, bookingId
     * @param conn   connection dùng chung (null = tự tạo)
     * @throws SQLException nếu lỗi DB
     */
    public void insert(PetHealthRecord record, Connection conn) throws SQLException {
        boolean own = (conn == null);
        Connection c = own ? DBConnection.getConnection() : conn;
        try {
            PreparedStatement ps = c.prepareStatement(SQL_INSERT);
            ps.setString(1, record.getHealthRecordId());
            ps.setString(2, record.getPetId());
            ps.setString(3, record.getBookingId());
            ps.setString(4, record.getNote());
            ps.setInt(5, record.getStatus());
            ps.executeUpdate();
            ps.close();
        } finally {
            if (own) DBConnection.closeQuietly(c);
        }
    }

    /**
     * Lấy tất cả health record của một thú cưng, mới nhất trước.
     *
     * @param petId mã thú cưng
     * @return List<PetHealthRecord>
     * @throws SQLException nếu lỗi DB
     */
    public List<PetHealthRecord> findByPetId(String petId) throws SQLException {
        List<PetHealthRecord> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_PET)) {
            ps.setString(1, petId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Lấy health record theo booking.
     *
     * @param bookingId mã booking
     * @return List<PetHealthRecord>
     * @throws SQLException nếu lỗi DB
     */
    public List<PetHealthRecord> findByBookingId(String bookingId) throws SQLException {
        List<PetHealthRecord> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_BOOKING)) {
            ps.setString(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Lấy health record mới nhất của một thú cưng.
     *
     * @param petId mã thú cưng
     * @return PetHealthRecord mới nhất, hoặc null nếu không có
     * @throws SQLException nếu lỗi DB
     */
    public PetHealthRecord findLatestByPetId(String petId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_LATEST_BY_PET)) {
            ps.setString(1, petId);
            ps.setString(2, petId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    // ── Private Helpers ──────────────────────────────────────────

    private PetHealthRecord mapRow(ResultSet rs) throws SQLException {
        PetHealthRecord r = new PetHealthRecord();
        r.setHealthRecordId(rs.getString("health_record_id"));
        r.setPetId(rs.getString("pet_id"));
        r.setBookingId(rs.getString("booking_id"));
        r.setStatus(rs.getInt("status"));

        Timestamp rec = rs.getTimestamp("recorded_at");
        if (rec != null)
            r.setRecordedAt(rec.toInstant().atOffset(java.time.ZoneOffset.UTC));

        Clob noteClob = rs.getClob("note");
        if (noteClob != null)
            r.setNote(noteClob.getSubString(1, (int) noteClob.length()));

        return r;
    }
}
