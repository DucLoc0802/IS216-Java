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
        "WHERE UPPER(category_name) LIKE '%GROOMING%' OR UPPER(category_name) LIKE '%CHĂM SÓC%' " +
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
