package PetHotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import PetHotel.model.ServiceCategory;
import PetHotel.util.DBConnection;
import PetHotel.util.IDGenerator;

/**
 * ServiceCategoryDAO — Quản lý dữ liệu loại dịch vụ từ bảng CATEGORY_SERVICES.
 */
public class ServiceCategoryDAO {

    // ── SQL Queries ───────────────────────────────────────────────

    private static final String SQL_FIND_ALL =
        "SELECT service_category_id, category_name, note, created_at, updated_at " +
        "FROM category_services " +
        "ORDER BY category_name";

    private static final String SQL_FIND_BY_ID =
        "SELECT service_category_id, category_name, note, created_at, updated_at " +
        "FROM category_services " +
        "WHERE service_category_id = ?";

    private static final String SQL_FIND_GROOMING =
        "SELECT service_category_id, category_name, note, created_at, updated_at " +
        "FROM category_services " +
        "ORDER BY category_name";

    /**
     * Lấy tất cả loại dịch vụ
     */
    public List<ServiceCategory> findAll() throws SQLException {
        List<ServiceCategory> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_FIND_ALL)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Lấy loại dịch vụ theo ID
     */
    public ServiceCategory findById(String serviceCategoryId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setString(1, serviceCategoryId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /**
     * Lấy loại dịch vụ Grooming
     */
    public List<ServiceCategory> findGroomingCategories() throws SQLException {
        List<ServiceCategory> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_FIND_GROOMING)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Thêm loại dịch vụ mới
     * @param categoryId Mã loại dịch vụ (VD: SC001)
     * @param categoryName Tên loại dịch vụ
     * @param note Ghi chú
     * @throws SQLException nếu lỗi DB
     */
    public void insert(String categoryId, String categoryName, String note) throws SQLException {
        String sql =
            "INSERT INTO category_services (service_category_id, category_name, note, created_at, updated_at) " +
            "VALUES (?, ?, ?, SYSTIMESTAMP, SYSTIMESTAMP)";

        if(categoryId == null || categoryId.trim().isEmpty()) {
            categoryId = IDGenerator.nextServiceCategoryId();
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoryId);
            ps.setString(2, categoryName);
            if (note != null && !note.trim().isEmpty()) {
                ps.setString(3, note);
            } else {
                ps.setNull(3, java.sql.Types.CLOB);
            }
            ps.executeUpdate();
        }
    }

    /**
     * Kiểm tra xem loại dịch vụ đã tồn tại chưa (theo tên)
     * @param categoryName Tên loại dịch vụ
     * @return true nếu đã tồn tại
     * @throws SQLException nếu lỗi DB
     */
    public boolean existsByName(String categoryName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM category_services WHERE UPPER(category_name) = UPPER(?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoryName.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Kiểm tra xem loại dịch vụ đã tồn tại chưa (theo ID)
     * @param categoryId Mã loại dịch vụ
     * @return true nếu đã tồn tại
     * @throws SQLException nếu lỗi DB
     */
    public boolean existsById(String categoryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM category_services WHERE service_category_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // ── Helper Methods ────────────────────────────────────────────

    private ServiceCategory mapRow(ResultSet rs) throws SQLException {
        ServiceCategory category = new ServiceCategory();
        category.setServiceCategoryId(rs.getString("service_category_id"));
        category.setCategoryName(rs.getString("category_name"));
        category.setNote(rs.getString("note"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            category.setCreatedAt(createdAt.toInstant().atOffset(java.time.ZoneOffset.UTC));
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            category.setUpdatedAt(updatedAt.toInstant().atOffset(java.time.ZoneOffset.UTC));
        }
        
        return category;
    }

}
