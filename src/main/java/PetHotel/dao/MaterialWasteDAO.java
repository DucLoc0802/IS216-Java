package PetHotel.dao;

import PetHotel.model.MaterialWaste;
import PetHotel.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class MaterialWasteDAO {
    private static final String SELECT_BASE =
        "SELECT mw.material_waste_id, mw.product_id, p.product_name, p.unit, " +
        "       mw.employee_id, e.full_name AS employee_name, mw.branch_id, b.branch_name, " +
        "       mw.waste_quantity, mw.reason, mw.recorded_at, mw.status, mw.note, " +
        "       mw.created_at, mw.updated_at " +
        "FROM material_waste mw " +
        "JOIN product p ON p.product_id = mw.product_id " +
        "JOIN employee e ON e.employee_id = mw.employee_id " +
        "JOIN branch b ON b.branch_id = mw.branch_id ";

    public List<MaterialWaste> search(String keyword, String status, String branchId, String employeeId)
            throws SQLException {
        List<MaterialWaste> wastes = new ArrayList<>();
        String normalizedKeyword = normalize(keyword);
        String keywordLike = normalizedKeyword == null ? null : "%" + normalizedKeyword.toLowerCase() + "%";

        String sql = SELECT_BASE +
            "WHERE (? IS NULL OR LOWER(mw.material_waste_id) LIKE ? " +
            "       OR LOWER(mw.product_id) LIKE ? OR LOWER(p.product_name) LIKE ? " +
            "       OR LOWER(e.full_name) LIKE ?) " +
            "  AND (? IS NULL OR mw.status = ?) " +
            "  AND (? IS NULL OR mw.branch_id = ?) " +
            "  AND (? IS NULL OR mw.employee_id = ?) " +
            "ORDER BY mw.recorded_at DESC NULLS LAST, mw.material_waste_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, keywordLike);
            ps.setString(2, keywordLike);
            ps.setString(3, keywordLike);
            ps.setString(4, keywordLike);
            ps.setString(5, keywordLike);
            ps.setString(6, normalize(status));
            ps.setString(7, normalize(status));
            ps.setString(8, normalize(branchId));
            ps.setString(9, normalize(branchId));
            ps.setString(10, normalize(employeeId));
            ps.setString(11, normalize(employeeId));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    wastes.add(mapRow(rs));
                }
            }
        }
        return wastes;
    }

    public MaterialWaste findById(String materialWasteId) throws SQLException {
        String sql = SELECT_BASE + "WHERE mw.material_waste_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, materialWasteId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public void insertPending(MaterialWaste waste) throws SQLException {
        String sql =
            "INSERT INTO material_waste (material_waste_id, product_id, employee_id, branch_id, " +
            "waste_quantity, reason, recorded_at, status, note, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, SYSTIMESTAMP, 'PENDING', ?, SYSTIMESTAMP, SYSTIMESTAMP)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, waste.getMaterialWasteId());
            ps.setString(2, waste.getProductId());
            ps.setString(3, waste.getEmployeeId());
            ps.setString(4, waste.getBranchId());
            ps.setBigDecimal(5, waste.getWasteQuantity());
            ps.setString(6, waste.getReason());
            ps.setString(7, waste.getNote());
            ps.executeUpdate();
        }
    }

    public void approve(String materialWasteId, String managerNote) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            MaterialWaste locked = lockPending(conn, materialWasteId);
            if (locked == null) {
                throw new SQLException("Không tìm thấy phiếu hao hụt đang chờ duyệt.");
            }

            BigDecimal currentStock = lockInventoryQuantity(conn, locked.getBranchId(), locked.getProductId());
            if (currentStock.compareTo(locked.getWasteQuantity()) < 0) {
                throw new SQLException("Tồn kho không đủ để duyệt phiếu hao hụt.");
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE branch_inventory SET quantity_in_stock = quantity_in_stock - ?, " +
                    "last_updated = SYSTIMESTAMP WHERE branch_id = ? AND product_id = ?")) {
                ps.setBigDecimal(1, locked.getWasteQuantity());
                ps.setString(2, locked.getBranchId());
                ps.setString(3, locked.getProductId());
                if (ps.executeUpdate() == 0) {
                    throw new SQLException("Không cập nhật được tồn kho.");
                }
            }

            updateStatus(conn, materialWasteId, MaterialWaste.STATUS_APPROVED, managerNote);
            conn.commit();
        } catch (SQLException e) {
            DBConnection.rollbackQuietly(conn);
            throw e;
        } finally {
            DBConnection.closeQuietly(conn);
        }
    }

    public void reject(String materialWasteId, String managerNote) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            updateStatus(conn, materialWasteId, MaterialWaste.STATUS_REJECTED, managerNote);
        }
    }

    private MaterialWaste lockPending(Connection conn, String materialWasteId) throws SQLException {
        String sql =
            "SELECT material_waste_id, product_id, employee_id, branch_id, waste_quantity, " +
            "reason, recorded_at, status, note, created_at, updated_at " +
            "FROM material_waste " +
            "WHERE material_waste_id = ? AND status = 'PENDING' FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, materialWasteId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapWasteRow(rs) : null;
            }
        }
    }

    private BigDecimal lockInventoryQuantity(Connection conn, String branchId, String productId) throws SQLException {
        String sql = "SELECT quantity_in_stock FROM branch_inventory WHERE branch_id = ? AND product_id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, branchId);
            ps.setString(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Không tìm thấy tồn kho cho sản phẩm trong chi nhánh.");
                }
                BigDecimal quantity = rs.getBigDecimal("quantity_in_stock");
                return quantity == null ? BigDecimal.ZERO : quantity;
            }
        }
    }

    private void updateStatus(Connection conn, String materialWasteId, String status, String managerNote)
            throws SQLException {
        String normalizedNote = normalize(managerNote);
        String sql = normalizedNote == null
            ? "UPDATE material_waste SET status = ?, updated_at = SYSTIMESTAMP WHERE material_waste_id = ? AND status = 'PENDING'"
            : "UPDATE material_waste SET status = ?, " +
              "note = CASE WHEN note IS NULL THEN ? ELSE note || CHR(10) || ? END, " +
              "updated_at = SYSTIMESTAMP WHERE material_waste_id = ? AND status = 'PENDING'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            if (normalizedNote == null) {
                ps.setString(2, materialWasteId);
            } else {
                String reviewNote = "Phản hồi quản lý: " + normalizedNote;
                ps.setString(2, reviewNote);
                ps.setString(3, reviewNote);
                ps.setString(4, materialWasteId);
            }
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Phiếu hao hụt không còn ở trạng thái chờ duyệt.");
            }
        }
    }

    private MaterialWaste mapRow(ResultSet rs) throws SQLException {
        MaterialWaste waste = new MaterialWaste();
        waste.setMaterialWasteId(rs.getString("material_waste_id"));
        waste.setProductId(rs.getString("product_id"));
        waste.setProductName(rs.getString("product_name"));
        waste.setUnit(rs.getString("unit"));
        waste.setEmployeeId(rs.getString("employee_id"));
        waste.setEmployeeName(rs.getString("employee_name"));
        waste.setBranchId(rs.getString("branch_id"));
        waste.setBranchName(rs.getString("branch_name"));
        waste.setWasteQuantity(rs.getBigDecimal("waste_quantity"));
        waste.setReason(rs.getString("reason"));
        waste.setRecordedAt(getOffsetDateTime(rs, "recorded_at"));
        waste.setStatus(rs.getString("status"));
        waste.setNote(rs.getString("note"));
        waste.setCreatedAt(getOffsetDateTime(rs, "created_at"));
        waste.setUpdatedAt(getOffsetDateTime(rs, "updated_at"));
        return waste;
    }

    private MaterialWaste mapWasteRow(ResultSet rs) throws SQLException {
        MaterialWaste waste = new MaterialWaste();
        waste.setMaterialWasteId(rs.getString("material_waste_id"));
        waste.setProductId(rs.getString("product_id"));
        waste.setEmployeeId(rs.getString("employee_id"));
        waste.setBranchId(rs.getString("branch_id"));
        waste.setWasteQuantity(rs.getBigDecimal("waste_quantity"));
        waste.setReason(rs.getString("reason"));
        waste.setRecordedAt(getOffsetDateTime(rs, "recorded_at"));
        waste.setStatus(rs.getString("status"));
        waste.setNote(rs.getString("note"));
        waste.setCreatedAt(getOffsetDateTime(rs, "created_at"));
        waste.setUpdatedAt(getOffsetDateTime(rs, "updated_at"));
        return waste;
    }

    private OffsetDateTime getOffsetDateTime(ResultSet rs, String column) throws SQLException {
        try {
            return rs.getObject(column, OffsetDateTime.class);
        } catch (SQLException | AbstractMethodError e) {
            Timestamp timestamp = rs.getTimestamp(column);
            return timestamp == null ? null : timestamp.toInstant().atOffset(ZoneOffset.UTC);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
