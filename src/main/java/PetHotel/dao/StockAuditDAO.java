package PetHotel.dao;

import PetHotel.model.StockAudit;
import PetHotel.model.StockAuditDetail;
import PetHotel.util.DBConnection;
import PetHotel.util.IDGenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StockAuditDAO {
    public List<StockAudit> search(String branchId, String status, LocalDate fromDate, LocalDate toDate)
            throws SQLException {
        String sql =
            "SELECT sa.stock_audit_id, sa.branch_id, b.branch_name, sa.employee_id, e.full_name AS employee_name, " +
            "       sa.audit_date, sa.status, sa.note " +
            "FROM stock_audit sa " +
            "JOIN branch b ON b.branch_id = sa.branch_id " +
            "LEFT JOIN employee e ON e.employee_id = sa.employee_id " +
            "WHERE (? IS NULL OR sa.branch_id = ?) " +
            "  AND (? IS NULL OR sa.status = ?) " +
            "  AND (? IS NULL OR TRUNC(sa.audit_date) >= ?) " +
            "  AND (? IS NULL OR TRUNC(sa.audit_date) <= ?) " +
            "ORDER BY sa.audit_date DESC, sa.stock_audit_id DESC";

        String normalizedBranchId = normalize(branchId);
        String normalizedStatus = normalizeStatus(status);
        List<StockAudit> audits = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizedBranchId);
            ps.setString(2, normalizedBranchId);
            ps.setString(3, normalizedStatus);
            ps.setString(4, normalizedStatus);
            setDateRange(ps, 5, 6, fromDate);
            setDateRange(ps, 7, 8, toDate);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    audits.add(mapAudit(rs));
                }
            }
        }
        return audits;
    }

    public StockAudit findById(String stockAuditId) throws SQLException {
        String sql =
            "SELECT sa.stock_audit_id, sa.branch_id, b.branch_name, sa.employee_id, e.full_name AS employee_name, " +
            "       sa.audit_date, sa.status, sa.note " +
            "FROM stock_audit sa " +
            "JOIN branch b ON b.branch_id = sa.branch_id " +
            "LEFT JOIN employee e ON e.employee_id = sa.employee_id " +
            "WHERE sa.stock_audit_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stockAuditId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                StockAudit audit = mapAudit(rs);
                audit.getDetails().addAll(findDetails(conn, stockAuditId));
                return audit;
            }
        }
    }

    public List<StockAuditDetail> findDetails(String stockAuditId) throws SQLException {
        try (Connection conn = getConnection()) {
            return findDetails(conn, stockAuditId);
        }
    }

    public void createAuditFromInventory(String branchId, String employeeId, String note) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            String auditId = generateNextStockAuditId();
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO stock_audit (stock_audit_id, branch_id, employee_id, audit_date, status, note, created_at, updated_at) " +
                    "VALUES (?, ?, ?, SYSTIMESTAMP, 'DRAFT', ?, SYSTIMESTAMP, SYSTIMESTAMP)")) {
                ps.setString(1, auditId);
                ps.setString(2, branchId);
                ps.setString(3, employeeId);
                setClobString(ps, 4, note);
                ps.executeUpdate();
            }

            int detailCount = insertInventorySnapshot(conn, auditId, branchId);
            if (detailCount == 0) {
                throw new SQLException("Chi nhánh chưa có dữ liệu tồn kho để kiểm kê.");
            }

            conn.commit();
        } catch (SQLException e) {
            DBConnection.rollbackQuietly(conn);
            throw e;
        } finally {
            closeTxConnection(conn);
        }
    }

    public void updateDraftAudit(StockAudit audit, List<StockAuditDetail> details) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            StockAudit current = findByIdForUpdate(conn, audit.getStockAuditId());
            if (current == null) {
                throw new SQLException("Không tìm thấy phiếu kiểm kê.");
            }
            if (!"DRAFT".equals(current.getStatus())) {
                throw new SQLException("Chỉ được sửa phiếu kiểm kê DRAFT.");
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE stock_audit SET note = ?, updated_at = SYSTIMESTAMP WHERE stock_audit_id = ?")) {
                setClobString(ps, 1, audit.getNote());
                ps.setString(2, audit.getStockAuditId());
                ps.executeUpdate();
            }

            deleteDetails(conn, audit.getStockAuditId());
            insertDetails(conn, audit.getStockAuditId(), details);

            conn.commit();
        } catch (SQLException e) {
            DBConnection.rollbackQuietly(conn);
            throw e;
        } finally {
            closeTxConnection(conn);
        }
    }

    public void completeAudit(String stockAuditId) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            StockAudit audit = findByIdForUpdate(conn, stockAuditId);
            if (audit == null) {
                throw new SQLException("Không tìm thấy phiếu kiểm kê.");
            }
            if (!"DRAFT".equals(audit.getStatus())) {
                throw new SQLException("Chỉ được hoàn tất phiếu kiểm kê DRAFT.");
            }

            List<StockAuditDetail> details = findDetails(conn, stockAuditId);
            for (StockAuditDetail detail : details) {
                updateInventoryQuantity(conn, audit.getBranchId(), detail.getProductId(), detail.getActualQuantity());
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE stock_audit SET status = 'COMPLETED', updated_at = SYSTIMESTAMP WHERE stock_audit_id = ?")) {
                ps.setString(1, stockAuditId);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            DBConnection.rollbackQuietly(conn);
            throw e;
        } finally {
            closeTxConnection(conn);
        }
    }

    public void cancelAudit(String stockAuditId) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            StockAudit audit = findByIdForUpdate(conn, stockAuditId);
            if (audit == null) {
                throw new SQLException("Không tìm thấy phiếu kiểm kê.");
            }
            if (!"DRAFT".equals(audit.getStatus())) {
                throw new SQLException("Chỉ được hủy phiếu kiểm kê DRAFT.");
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE stock_audit SET status = 'CANCELLED', updated_at = SYSTIMESTAMP WHERE stock_audit_id = ?")) {
                ps.setString(1, stockAuditId);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            DBConnection.rollbackQuietly(conn);
            throw e;
        } finally {
            closeTxConnection(conn);
        }
    }

    public String generateNextStockAuditId() throws SQLException {
        return IDGenerator.nextStockAuditId();
    }

    private int insertInventorySnapshot(Connection conn, String auditId, String branchId) throws SQLException {
        String sql =
            "SELECT product_id, quantity_in_stock " +
            "FROM branch_inventory " +
            "WHERE branch_id = ? " +
            "ORDER BY product_id";

        int count = 0;
        try (PreparedStatement select = conn.prepareStatement(sql)) {
            select.setString(1, branchId);
            try (ResultSet rs = select.executeQuery()) {
                while (rs.next()) {
                    StockAuditDetail detail = new StockAuditDetail();
                    detail.setProductId(rs.getString("product_id"));
                    detail.setSystemQuantity(rs.getBigDecimal("quantity_in_stock"));
                    detail.setActualQuantity(rs.getBigDecimal("quantity_in_stock"));
                    insertDetail(conn, auditId, detail);
                    count++;
                }
            }
        }
        return count;
    }

    private void insertDetails(Connection conn, String auditId, List<StockAuditDetail> details) throws SQLException {
        for (StockAuditDetail detail : details) {
            insertDetail(conn, auditId, detail);
        }
    }

    private void insertDetail(Connection conn, String auditId, StockAuditDetail detail) throws SQLException {
        detail.recalculateDifference();
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO stock_audit_detail (stock_audit_id, product_id, system_quantity, actual_quantity, " +
                "difference_quantity, difference_rate, note, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP, SYSTIMESTAMP)")) {
            ps.setString(1, auditId);
            ps.setString(2, detail.getProductId());
            ps.setBigDecimal(3, valueOrZero(detail.getSystemQuantity()));
            ps.setBigDecimal(4, valueOrZero(detail.getActualQuantity()));
            ps.setBigDecimal(5, valueOrZero(detail.getDifferenceQuantity()));
            ps.setBigDecimal(6, valueOrZero(detail.getDifferenceRate()).setScale(2, RoundingMode.HALF_UP));
            setClobString(ps, 7, detail.getNote());
            ps.executeUpdate();
        }
    }

    private List<StockAuditDetail> findDetails(Connection conn, String stockAuditId) throws SQLException {
        String sql =
            "SELECT sad.stock_audit_id, sad.product_id, p.product_name, p.unit, sad.system_quantity, " +
            "       sad.actual_quantity, sad.difference_quantity, sad.difference_rate, sad.note " +
            "FROM stock_audit_detail sad " +
            "JOIN product p ON p.product_id = sad.product_id " +
            "WHERE sad.stock_audit_id = ? " +
            "ORDER BY p.product_name";

        List<StockAuditDetail> details = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stockAuditId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StockAuditDetail detail = new StockAuditDetail();
                    detail.setStockAuditId(rs.getString("stock_audit_id"));
                    detail.setProductId(rs.getString("product_id"));
                    detail.setProductName(rs.getString("product_name"));
                    detail.setUnit(rs.getString("unit"));
                    detail.setSystemQuantity(rs.getBigDecimal("system_quantity"));
                    detail.setActualQuantity(rs.getBigDecimal("actual_quantity"));
                    detail.setDifferenceQuantity(rs.getBigDecimal("difference_quantity"));
                    detail.setDifferenceRate(rs.getBigDecimal("difference_rate"));
                    detail.setNote(readClob(rs, "note"));
                    details.add(detail);
                }
            }
        }
        return details;
    }

    private void deleteDetails(Connection conn, String stockAuditId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM stock_audit_detail WHERE stock_audit_id = ?")) {
            ps.setString(1, stockAuditId);
            ps.executeUpdate();
        }
    }

    private void updateInventoryQuantity(Connection conn, String branchId, String productId, BigDecimal actualQuantity)
            throws SQLException {
        if (actualQuantity == null || actualQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new SQLException("Số lượng thực tế không được nhỏ hơn 0.");
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "MERGE INTO branch_inventory bi " +
                "USING (SELECT ? AS branch_id, ? AS product_id, ? AS quantity_in_stock FROM dual) src " +
                "ON (bi.branch_id = src.branch_id AND bi.product_id = src.product_id) " +
                "WHEN MATCHED THEN UPDATE SET bi.quantity_in_stock = src.quantity_in_stock, bi.last_updated = SYSTIMESTAMP " +
                "WHEN NOT MATCHED THEN INSERT (branch_id, product_id, quantity_in_stock, reorder_point, last_updated) " +
                "VALUES (src.branch_id, src.product_id, src.quantity_in_stock, 0, SYSTIMESTAMP)")) {
            ps.setString(1, branchId);
            ps.setString(2, productId);
            ps.setBigDecimal(3, actualQuantity);
            ps.executeUpdate();
        }
    }

    private StockAudit findByIdForUpdate(Connection conn, String stockAuditId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT stock_audit_id, branch_id, employee_id, audit_date, status, note " +
                "FROM stock_audit WHERE stock_audit_id = ? FOR UPDATE")) {
            ps.setString(1, stockAuditId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                StockAudit audit = new StockAudit();
                audit.setStockAuditId(rs.getString("stock_audit_id"));
                audit.setBranchId(rs.getString("branch_id"));
                audit.setEmployeeId(rs.getString("employee_id"));
                Timestamp auditDate = rs.getTimestamp("audit_date");
                if (auditDate != null) {
                    audit.setAuditDate(auditDate.toLocalDateTime().toLocalDate());
                }
                audit.setStatus(rs.getString("status"));
                audit.setNote(readClob(rs, "note"));
                return audit;
            }
        }
    }

    private StockAudit mapAudit(ResultSet rs) throws SQLException {
        StockAudit audit = new StockAudit();
        audit.setStockAuditId(rs.getString("stock_audit_id"));
        audit.setBranchId(rs.getString("branch_id"));
        audit.setBranchName(rs.getString("branch_name"));
        audit.setEmployeeId(rs.getString("employee_id"));
        audit.setEmployeeName(rs.getString("employee_name"));
        Timestamp auditDate = rs.getTimestamp("audit_date");
        if (auditDate != null) {
            audit.setAuditDate(auditDate.toLocalDateTime().toLocalDate());
        }
        audit.setStatus(rs.getString("status"));
        audit.setNote(readClob(rs, "note"));
        return audit;
    }

    private void setDateRange(PreparedStatement ps, int nullIndex, int dateIndex, LocalDate date)
            throws SQLException {
        if (date == null) {
            ps.setNull(nullIndex, Types.DATE);
            ps.setNull(dateIndex, Types.DATE);
        } else {
            ps.setDate(nullIndex, Date.valueOf(date));
            ps.setDate(dateIndex, Date.valueOf(date));
        }
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String readClob(ResultSet rs, String columnName) throws SQLException {
        Clob clob = rs.getClob(columnName);
        return clob == null ? null : clob.getSubString(1, (int) clob.length());
    }

    private void setClobString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            ps.setNull(index, Types.CLOB);
        } else {
            ps.setString(index, value.trim());
        }
    }

    private String normalizeStatus(String status) {
        String normalized = normalize(status);
        return normalized == null || "Tất cả".equals(normalized) ? null : normalized;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Connection getConnection() throws SQLException {
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu.");
        }
        return conn;
    }

    private void closeTxConnection(Connection conn) throws SQLException {
        if (conn != null) {
            conn.setAutoCommit(true);
            conn.close();
        }
    }
}
