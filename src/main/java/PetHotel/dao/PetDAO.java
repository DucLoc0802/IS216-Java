package PetHotel.dao;

import PetHotel.model.Pet;
import PetHotel.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PetDAO — Thao tác DB cho bảng PET.
 *
 * TODO: Thêm pagination.
 * TODO: Externalize SQL.
 * TODO: Thêm logging.
 * TODO: Optimize JOIN query trong getPetServiceHistory.
 */
public class PetDAO {

    // ── SQL ──────────────────────────────────────────────────────

    private static final String SQL_FIND_BY_ID =
        "SELECT pet_id, customer_id, pet_name, species, breed, sex, " +
        "       weight_kg, special_note, created_at, updated_at " +
        "FROM pet WHERE pet_id = ?";

    private static final String SQL_FIND_BY_CUSTOMER =
        "SELECT pet_id, customer_id, pet_name, species, breed, sex, " +
        "       weight_kg, special_note, created_at, updated_at " +
        "FROM pet WHERE customer_id = ? ORDER BY pet_name";

    private static final String SQL_FIND_ALL =
        "SELECT pet_id, customer_id, pet_name, species, breed, sex, " +
        "       weight_kg, special_note, created_at, updated_at " +
        "FROM pet ORDER BY created_at DESC";

    private static final String SQL_FIND_IDS_BY_PREFIX =
        "SELECT pet_id FROM pet WHERE pet_id LIKE 'PET%'";

    /** Tìm kiếm: tên pet, loài, giống, hoặc tên chủ */
    private static final String SQL_SEARCH =
        "SELECT p.pet_id, p.customer_id, p.pet_name, p.species, p.breed, p.sex, " +
        "       p.weight_kg, p.special_note, p.created_at, p.updated_at " +
        "FROM pet p " +
        "JOIN customer c ON p.customer_id = c.customer_id " +
        "WHERE LOWER(p.pet_id) LIKE LOWER(?) " +
        "   OR LOWER(p.pet_name) LIKE LOWER(?) " +
        "   OR LOWER(p.species)  LIKE LOWER(?) " +
        "   OR LOWER(p.breed)    LIKE LOWER(?) " +
        "   OR LOWER(c.full_name) LIKE LOWER(?) " +
        "ORDER BY p.pet_name";

    private static final String SQL_INSERT =
        "INSERT INTO pet (pet_id, customer_id, pet_name, species, breed, sex, " +
        "                 weight_kg, special_note, created_at, updated_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP, SYSTIMESTAMP)";

    private static final String SQL_UPDATE =
        "UPDATE pet " +
        "SET pet_name = ?, species = ?, breed = ?, sex = ?, " +
        "    weight_kg = ?, special_note = ?, updated_at = SYSTIMESTAMP " +
        "WHERE pet_id = ?";

    private static final String SQL_UPDATE_OWNER =
        "UPDATE pet SET customer_id = ?, updated_at = SYSTIMESTAMP WHERE pet_id = ?";

    private static final String SQL_DELETE =
        "DELETE FROM pet WHERE pet_id = ?";

    /** Đếm health record của pet */
    private static final String SQL_COUNT_HEALTH_RECORDS =
        "SELECT COUNT(*) FROM pet_health_record WHERE pet_id = ?";

    /** Đếm booking_room_pet liên quan đến pet (dùng trước khi xóa) */
    private static final String SQL_COUNT_BOOKING_ROOM_PETS =
        "SELECT COUNT(*) FROM booking_room_pet WHERE pet_id = ?";

    /**
     * Lịch sử dịch vụ của thú cưng.
     * Lấy qua booking_room_pet → booking_room → booking → booking_services → services
     * và pet_health_record cho cùng booking.
     *
     * TODO: Refactor thành DTO PetServiceHistoryDTO.
     * TODO: Thêm filter theo ngày, loại dịch vụ.
     */
    private static final String SQL_PET_SERVICE_HISTORY =
        "SELECT b.booking_id, b.status AS booking_status, " +
        "       b.checkin_expected_at, b.checkout_expected_at, " +
        "       s.service_name, bs.status AS service_status, bs.scheduled_at, " +
        "       phr.health_record_id, phr.note AS health_note, phr.status AS health_status, " +
        "       phr.recorded_at " +
        "FROM booking_room_pet brp " +
        "JOIN booking_room br   ON brp.booking_room_id = br.booking_room_id " +
        "JOIN booking b         ON br.booking_id = b.booking_id " +
        "LEFT JOIN booking_services bs ON b.booking_id = bs.booking_id " +
        "LEFT JOIN services s           ON bs.service_id = s.service_id " +
        "LEFT JOIN pet_health_record phr " +
        "       ON phr.pet_id = brp.pet_id AND phr.booking_id = b.booking_id " +
        "WHERE brp.pet_id = ? " +
        "ORDER BY b.created_at DESC";

    // ── Public Methods ───────────────────────────────────────────

    /**
     * Tìm Pet theo PK.
     *
     * @param petId mã thú cưng
     * @return Pet hoặc null nếu không tồn tại
     * @throws SQLException nếu lỗi DB
     */
    public Pet findById(String petId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setString(1, petId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /**
     * Lấy danh sách pet của một khách hàng.
     *
     * @param customerId mã khách hàng
     * @return List<Pet>, rỗng nếu không có pet
     * @throws SQLException nếu lỗi DB
     */
    public List<Pet> findByCustomerId(String customerId) throws SQLException {
        List<Pet> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_CUSTOMER)) {
            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Pet> findAll() throws SQLException {
        List<Pet> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /**
     * Tìm kiếm pet theo keyword (tên, loài, giống, tên chủ).
     *
     * @param keyword từ khoá
     * @return List<Pet> phù hợp
     * @throws SQLException nếu lỗi DB
     */
    public List<Pet> search(String keyword) throws SQLException {
        String pattern = "%" + keyword.trim() + "%";
        List<Pet> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SEARCH)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            ps.setString(5, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public String generateNextPetId() throws SQLException {
        synchronized (PetDAO.class) {
            int max = 0;
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(SQL_FIND_IDS_BY_PREFIX);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("pet_id");
                    if (isValidGeneratedPetId(id)) {
                        max = Math.max(max, Integer.parseInt(id.substring(3)));
                    }
                }
            }
            return String.format("PET%03d", max + 1);
        }
    }

    private boolean isValidGeneratedPetId(String id) {
        if (id == null || !id.matches("^PET\\d+$")) return false;
        int number = Integer.parseInt(id.substring(3));
        return number < 1_000_000;
    }

    /**
     * Thêm mới pet.
     *
     * @param pet  đối tượng Pet đã có petId, customerId, petName, species
     * @param conn connection dùng chung (null = tự tạo)
     * @throws SQLException nếu lỗi DB
     */
    public void insert(Pet pet, Connection conn) throws SQLException {
        boolean own = (conn == null);
        Connection c = own ? DBConnection.getConnection() : conn;
        try {
            PreparedStatement ps = c.prepareStatement(SQL_INSERT);
            ps.setString(1, pet.getPetId());
            ps.setString(2, pet.getCustomerId());
            ps.setString(3, pet.getPetName());
            ps.setString(4, pet.getSpecies());
            ps.setString(5, pet.getBreed());
            ps.setString(6, pet.getSex());
            if (pet.getWeightKg() != null)
                ps.setDouble(7, pet.getWeightKg());
            else
                ps.setNull(7, Types.NUMERIC);
            ps.setString(8, pet.getSpecialNote());
            ps.executeUpdate();
            ps.close();
        } finally {
            if (own) DBConnection.closeQuietly(c);
        }
    }

    /**
     * Cập nhật thông tin pet.
     *
     * @param pet  đối tượng Pet đã có petId và dữ liệu mới
     * @param conn connection dùng chung (null = tự tạo)
     * @return số dòng bị ảnh hưởng
     * @throws SQLException nếu lỗi DB
     */
    public int update(Pet pet, Connection conn) throws SQLException {
        boolean own = (conn == null);
        Connection c = own ? DBConnection.getConnection() : conn;
        try {
            PreparedStatement ps = c.prepareStatement(SQL_UPDATE);
            ps.setString(1, pet.getPetName());
            ps.setString(2, pet.getSpecies());
            ps.setString(3, pet.getBreed());
            ps.setString(4, pet.getSex());
            if (pet.getWeightKg() != null)
                ps.setDouble(5, pet.getWeightKg());
            else
                ps.setNull(5, Types.NUMERIC);
            ps.setString(6, pet.getSpecialNote());
            ps.setString(7, pet.getPetId());
            int rows = ps.executeUpdate();
            ps.close();
            return rows;
        } finally {
            if (own) DBConnection.closeQuietly(c);
        }
    }

    public int updateOwner(String petId, String customerId, Connection conn) throws SQLException {
        boolean own = (conn == null);
        Connection c = own ? DBConnection.getConnection() : conn;
        try {
            PreparedStatement ps = c.prepareStatement(SQL_UPDATE_OWNER);
            ps.setString(1, customerId);
            ps.setString(2, petId);
            int rows = ps.executeUpdate();
            ps.close();
            return rows;
        } finally {
            if (own) DBConnection.closeQuietly(c);
        }
    }

    /**
     * Xóa pet.
     * BUS phải kiểm tra ràng buộc trước khi gọi.
     *
     * @param petId mã thú cưng
     * @param conn  connection dùng chung (null = tự tạo)
     * @return số dòng bị ảnh hưởng
     * @throws SQLException nếu lỗi DB
     */
    public int delete(String petId, Connection conn) throws SQLException {
        boolean own = (conn == null);
        Connection c = own ? DBConnection.getConnection() : conn;
        try {
            PreparedStatement ps = c.prepareStatement(SQL_DELETE);
            ps.setString(1, petId);
            int rows = ps.executeUpdate();
            ps.close();
            return rows;
        } finally {
            if (own) DBConnection.closeQuietly(c);
        }
    }

    /**
     * Đếm health record của pet. Dùng để kiểm tra trước khi xóa.
     *
     * @param petId mã thú cưng
     * @return số lượng health record
     * @throws SQLException nếu lỗi DB
     */
    public int countHealthRecords(String petId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT_HEALTH_RECORDS)) {
            ps.setString(1, petId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Đếm số lần pet xuất hiện trong booking_room_pet. Dùng để kiểm tra trước khi xóa.
     *
     * @param petId mã thú cưng
     * @return số lần được gán vào phòng
     * @throws SQLException nếu lỗi DB
     */
    public int countBookingRoomPets(String petId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT_BOOKING_ROOM_PETS)) {
            ps.setString(1, petId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Lấy lịch sử dịch vụ của một thú cưng.
     * Trả về List<Object[]> thô, mỗi phần tử là một hàng.
     *
     * TODO: Chuyển sang PetServiceHistoryDTO.
     * TODO: Thêm filter/pagination.
     *
     * @param petId mã thú cưng
     * @return lịch sử dịch vụ
     * @throws SQLException nếu lỗi DB
     */
    public List<Object[]> getPetServiceHistory(String petId) throws SQLException {
        List<Object[]> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_PET_SERVICE_HISTORY)) {
            ps.setString(1, petId);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                while (rs.next()) {
                    Object[] row = new Object[colCount];
                    for (int i = 1; i <= colCount; i++) row[i-1] = rs.getObject(i);
                    result.add(row);
                }
            }
        }
        return result;
    }

    // ── Private Helpers ──────────────────────────────────────────

    private Pet mapRow(ResultSet rs) throws SQLException {
        Pet p = new Pet();
        p.setPetId(rs.getString("pet_id"));
        p.setCustomerId(rs.getString("customer_id"));
        p.setPetName(rs.getString("pet_name"));
        p.setSpecies(rs.getString("species"));
        p.setBreed(rs.getString("breed"));
        p.setSex(rs.getString("sex"));

        double weight = rs.getDouble("weight_kg");
        p.setWeightKg(rs.wasNull() ? null : weight);

        Clob noteClob = rs.getClob("special_note");
        if (noteClob != null)
            p.setSpecialNote(noteClob.getSubString(1, (int) noteClob.length()));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null)
            p.setCreatedAt(createdAt.toInstant().atOffset(java.time.ZoneOffset.UTC));

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null)
            p.setUpdatedAt(updatedAt.toInstant().atOffset(java.time.ZoneOffset.UTC));

        return p;
    }
}
