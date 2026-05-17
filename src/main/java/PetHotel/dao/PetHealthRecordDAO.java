package PetHotel.dao;

import PetHotel.model.PetHealthRecord;
import PetHotel.util.DBConnection;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PetHealthRecordDAO {

    private static final String DB_CONNECTION_ERROR =
            "Không kết nối được database. Vui lòng kiểm tra Oracle service, JDBC URL, username/password.";

    private static final String SQL_INSERT =
            "INSERT INTO pet_health_record "
                    + "  (health_record_id, pet_id, booking_id, recorded_at, note, status) "
                    + "VALUES (?, ?, ?, SYSTIMESTAMP, ?, ?)";

    private static final String SQL_FIND_BY_PET =
            "SELECT health_record_id, pet_id, booking_id, recorded_at, note, status "
                    + "FROM pet_health_record "
                    + "WHERE pet_id = ? ORDER BY recorded_at DESC";

    private static final String SQL_FIND_BY_BOOKING =
            "SELECT health_record_id, pet_id, booking_id, recorded_at, note, status "
                    + "FROM pet_health_record "
                    + "WHERE booking_id = ? ORDER BY recorded_at DESC";

    private static final String SQL_FIND_LATEST_BY_PET =
            "SELECT health_record_id, pet_id, booking_id, recorded_at, note, status "
                    + "FROM pet_health_record "
                    + "WHERE pet_id = ? "
                    + "ORDER BY recorded_at DESC FETCH FIRST 1 ROW ONLY";

    private static final String SQL_FIND_LATEST_ALL =
            "SELECT health_record_id, pet_id, booking_id, recorded_at, note, status "
                    + "FROM ("
                    + "    SELECT phr.*, ROW_NUMBER() OVER ("
                    + "        PARTITION BY pet_id ORDER BY recorded_at DESC, health_record_id DESC"
                    + "    ) rn "
                    + "    FROM pet_health_record phr"
                    + ") WHERE rn = 1";

    private static final String SQL_BOOKING_EXISTS =
            "SELECT COUNT(*) FROM booking WHERE booking_id = ?";

    public void insert(PetHealthRecord record, Connection conn) throws SQLException {
        if (conn == null) {
            try (Connection ownConn = DBConnection.getConnection()) {
                insertWithConnection(record, ownConn);
            }
            return;
        }

        ensureUsableConnection(conn);
        insertWithConnection(record, conn);
    }

    public void insertHealthRecord(PetHealthRecord record) throws SQLException {
        insert(record, null);
    }

    public List<PetHealthRecord> findByPetId(String petId) throws SQLException {
        List<PetHealthRecord> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_PET)) {
            ps.setString(1, petId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<PetHealthRecord> getHealthRecordsByPetId(String petId) throws SQLException {
        return findByPetId(petId);
    }

    public List<PetHealthRecord> findByBookingId(String bookingId) throws SQLException {
        List<PetHealthRecord> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_BOOKING)) {
            ps.setString(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public PetHealthRecord findLatestByPetId(String petId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_LATEST_BY_PET)) {
            ps.setString(1, petId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public PetHealthRecord getLatestHealthRecordByPetId(String petId) throws SQLException {
        return findLatestByPetId(petId);
    }

    public Map<String, PetHealthRecord> findLatestByAllPetIds() throws SQLException {
        Map<String, PetHealthRecord> result = new HashMap<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_LATEST_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PetHealthRecord record = mapRow(rs);
                result.put(record.getPetId(), record);
            }
        }
        return result;
    }

    public boolean bookingExists(String bookingId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_BOOKING_EXISTS)) {
            ps.setString(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void insertWithConnection(PetHealthRecord record, Connection conn) throws SQLException {
        ensureUsableConnection(conn);
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
            ps.setString(1, record.getHealthRecordId());
            ps.setString(2, record.getPetId());
            ps.setString(3, record.getBookingId());
            ps.setString(4, record.getNote());
            ps.setInt(5, record.getStatus());
            ps.executeUpdate();
        }
    }

    private void ensureUsableConnection(Connection conn) throws SQLException {
        if (conn == null || conn.isClosed()) {
            throw new SQLException(DB_CONNECTION_ERROR);
        }
    }

    private PetHealthRecord mapRow(ResultSet rs) throws SQLException {
        PetHealthRecord r = new PetHealthRecord();
        r.setHealthRecordId(rs.getString("health_record_id"));
        r.setPetId(rs.getString("pet_id"));
        r.setBookingId(rs.getString("booking_id"));
        r.setStatus(rs.getInt("status"));

        Timestamp rec = rs.getTimestamp("recorded_at");
        if (rec != null) {
            r.setRecordedAt(rec.toInstant().atOffset(java.time.ZoneOffset.UTC));
        }

        Clob noteClob = rs.getClob("note");
        if (noteClob != null) {
            r.setNote(noteClob.getSubString(1, (int) noteClob.length()));
        }

        return r;
    }
}
