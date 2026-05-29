package PetHotel.dao;

import PetHotel.model.GoodsReceipt;
import PetHotel.model.GoodsReceiptDetail;
import PetHotel.util.DBConnection;
import PetHotel.util.IDGenerator;

import java.math.BigDecimal;
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

public class GoodsReceiptDAO {
    private static final String RECEIPT_SUMMARY_SELECT =
        "       (SELECT LISTAGG(p2.product_name, ', ') WITHIN GROUP (ORDER BY p2.product_name) " +
        "          FROM goods_receipt_detail grd2 JOIN product p2 ON p2.product_id = grd2.product_id " +
        "         WHERE grd2.goods_receipt_id = gr.goods_receipt_id) AS product_summary, " +
        "       (SELECT LISTAGG(TO_CHAR(grd3.quantity) || ' ' || grd3.unit, ', ') WITHIN GROUP (ORDER BY grd3.product_id) " +
        "          FROM goods_receipt_detail grd3 " +
        "         WHERE grd3.goods_receipt_id = gr.goods_receipt_id) AS quantity_summary, " +
        "       (SELECT LISTAGG(DISTINCT grd4.unit, ', ') WITHIN GROUP (ORDER BY grd4.unit) " +
        "          FROM goods_receipt_detail grd4 " +
        "         WHERE grd4.goods_receipt_id = gr.goods_receipt_id) AS unit_summary, " +
        "       (SELECT NVL(SUM(grd5.line_total), 0) " +
        "          FROM goods_receipt_detail grd5 " +
        "         WHERE grd5.goods_receipt_id = gr.goods_receipt_id) AS total_amount ";

    private final InventoryDAO inventoryDAO = new InventoryDAO();

    public List<GoodsReceipt> search(String keyword, String status, LocalDate fromDate, LocalDate toDate)
            throws SQLException {
        String sql =
            "SELECT gr.goods_receipt_id, gr.branch_id, gr.employee_id, e.full_name AS employee_name, " +
            "       gr.supplier_name, gr.receipt_date, gr.total_quantity, gr.total_item_count, " +
            "       gr.status, gr.note, " +
            RECEIPT_SUMMARY_SELECT +
            "FROM goods_receipt gr " +
            "LEFT JOIN employee e ON e.employee_id = gr.employee_id " +
            "WHERE (? IS NULL OR LOWER(gr.goods_receipt_id) LIKE LOWER(?) " +
            "       OR LOWER(gr.supplier_name) LIKE LOWER(?)) " +
            "  AND (? IS NULL OR gr.status = ?) " +
            "  AND (? IS NULL OR TRUNC(gr.receipt_date) >= ?) " +
            "  AND (? IS NULL OR TRUNC(gr.receipt_date) <= ?) " +
            "ORDER BY gr.receipt_date DESC, gr.goods_receipt_id DESC";

        String normalizedKeyword = normalize(keyword);
        String pattern = normalizedKeyword == null ? null : "%" + normalizedKeyword + "%";
        String normalizedStatus = normalizeStatus(status);

        List<GoodsReceipt> receipts = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizedKeyword);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, normalizedStatus);
            ps.setString(5, normalizedStatus);
            setDateRange(ps, 6, 7, fromDate);
            setDateRange(ps, 8, 9, toDate);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    receipts.add(mapReceipt(rs));
                }
            }
        }
        return receipts;
    }

    public GoodsReceipt findById(String goodsReceiptId) throws SQLException {
        String sql =
            "SELECT gr.goods_receipt_id, gr.branch_id, gr.employee_id, e.full_name AS employee_name, " +
            "       gr.supplier_name, gr.receipt_date, gr.total_quantity, gr.total_item_count, " +
            "       gr.status, gr.note, " +
            RECEIPT_SUMMARY_SELECT +
            "FROM goods_receipt gr " +
            "LEFT JOIN employee e ON e.employee_id = gr.employee_id " +
            "WHERE gr.goods_receipt_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, goodsReceiptId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                GoodsReceipt receipt = mapReceipt(rs);
                receipt.getDetails().addAll(findDetails(conn, goodsReceiptId));
                return receipt;
            }
        }
    }

    public List<GoodsReceiptDetail> findDetails(String goodsReceiptId) throws SQLException {
        try (Connection conn = getConnection()) {
            return findDetails(conn, goodsReceiptId);
        }
    }

    public void insertReceipt(GoodsReceipt receipt, List<GoodsReceiptDetail> details) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            if (normalize(receipt.getGoodsReceiptId()) == null) {
                receipt.setGoodsReceiptId(generateNextGoodsReceiptId());
            }
            receipt.setStatus("DRAFT");
            applyTotals(receipt, details);
            insertHeader(conn, receipt);
            insertDetails(conn, receipt.getGoodsReceiptId(), details);

            conn.commit();
        } catch (SQLException e) {
            DBConnection.rollbackQuietly(conn);
            throw e;
        } finally {
            closeTxConnection(conn);
        }
    }

    public void updateReceipt(GoodsReceipt receipt, List<GoodsReceiptDetail> details) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            GoodsReceipt current = findByIdForUpdate(conn, receipt.getGoodsReceiptId());
            if (current == null) {
                throw new SQLException("Không tìm thấy phiếu nhập.");
            }
            if (!"DRAFT".equals(current.getStatus())) {
                throw new SQLException("Chỉ được sửa phiếu nhập DRAFT.");
            }

            receipt.setStatus("DRAFT");
            applyTotals(receipt, details);
            updateHeader(conn, receipt);
            deleteDetails(conn, receipt.getGoodsReceiptId());
            insertDetails(conn, receipt.getGoodsReceiptId(), details);

            conn.commit();
        } catch (SQLException e) {
            DBConnection.rollbackQuietly(conn);
            throw e;
        } finally {
            closeTxConnection(conn);
        }
    }

    public void approveReceipt(String goodsReceiptId, String employeeId) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            GoodsReceipt receipt = findByIdForUpdate(conn, goodsReceiptId);
            if (receipt == null) {
                throw new SQLException("Không tìm thấy phiếu nhập.");
            }
            if (!"DRAFT".equals(receipt.getStatus())) {
                throw new SQLException("Chỉ được duyệt phiếu nhập DRAFT.");
            }

            List<GoodsReceiptDetail> details = findDetails(conn, goodsReceiptId);
            if (details.isEmpty()) {
                throw new SQLException("Phiếu nhập phải có ít nhất một sản phẩm.");
            }
            for (GoodsReceiptDetail detail : details) {
                inventoryDAO.upsertInventory(conn, receipt.getBranchId(), detail.getProductId(), detail.getQuantity());
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE goods_receipt SET employee_id = ?, status = 'APPROVED', updated_at = SYSTIMESTAMP " +
                    "WHERE goods_receipt_id = ?")) {
                ps.setString(1, employeeId);
                ps.setString(2, goodsReceiptId);
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

    public void cancelReceipt(String goodsReceiptId) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            GoodsReceipt receipt = findByIdForUpdate(conn, goodsReceiptId);
            if (receipt == null) {
                throw new SQLException("Không tìm thấy phiếu nhập.");
            }
            if (!"DRAFT".equals(receipt.getStatus())) {
                throw new SQLException("Chỉ được hủy phiếu nhập DRAFT.");
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE goods_receipt SET status = 'CANCELLED', updated_at = SYSTIMESTAMP WHERE goods_receipt_id = ?")) {
                ps.setString(1, goodsReceiptId);
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

    public String generateNextGoodsReceiptId() throws SQLException {
        return IDGenerator.nextGoodsReceiptId();
    }

    private void insertHeader(Connection conn, GoodsReceipt receipt) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO goods_receipt (goods_receipt_id, branch_id, employee_id, supplier_name, receipt_date, " +
                "total_quantity, total_item_count, status, note, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP, SYSTIMESTAMP)")) {
            bindHeader(ps, receipt);
            ps.executeUpdate();
        }
    }

    private void updateHeader(Connection conn, GoodsReceipt receipt) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE goods_receipt SET branch_id = ?, employee_id = ?, supplier_name = ?, receipt_date = ?, " +
                "total_quantity = ?, total_item_count = ?, status = ?, note = ?, updated_at = SYSTIMESTAMP " +
                "WHERE goods_receipt_id = ?")) {
            ps.setString(1, receipt.getBranchId());
            ps.setString(2, receipt.getEmployeeId());
            ps.setString(3, receipt.getSupplierName());
            ps.setTimestamp(4, toTimestamp(receipt.getReceiptDate()));
            ps.setBigDecimal(5, receipt.getTotalQuantity());
            ps.setInt(6, receipt.getTotalItemCount());
            ps.setString(7, receipt.getStatus());
            setClobString(ps, 8, receipt.getNote());
            ps.setString(9, receipt.getGoodsReceiptId());
            ps.executeUpdate();
        }
    }

    private void bindHeader(PreparedStatement ps, GoodsReceipt receipt) throws SQLException {
        ps.setString(1, receipt.getGoodsReceiptId());
        ps.setString(2, receipt.getBranchId());
        ps.setString(3, receipt.getEmployeeId());
        ps.setString(4, receipt.getSupplierName());
        ps.setTimestamp(5, toTimestamp(receipt.getReceiptDate()));
        ps.setBigDecimal(6, receipt.getTotalQuantity());
        ps.setInt(7, receipt.getTotalItemCount());
        ps.setString(8, receipt.getStatus());
        setClobString(ps, 9, receipt.getNote());
    }

    private void insertDetails(Connection conn, String receiptId, List<GoodsReceiptDetail> details) throws SQLException {
        for (GoodsReceiptDetail detail : details) {
            BigDecimal lineTotal = calculateLineTotal(conn, detail);
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO goods_receipt_detail (goods_receipt_id, product_id, quantity, unit, line_total, note, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, SYSTIMESTAMP, SYSTIMESTAMP)")) {
                ps.setString(1, receiptId);
                ps.setString(2, detail.getProductId());
                ps.setBigDecimal(3, detail.getQuantity());
                ps.setString(4, detail.getUnit());
                ps.setBigDecimal(5, lineTotal);
                setClobString(ps, 6, detail.getNote());
                ps.executeUpdate();
            }
        }
    }

    private List<GoodsReceiptDetail> findDetails(Connection conn, String goodsReceiptId) throws SQLException {
        String sql =
            "SELECT grd.goods_receipt_id, grd.product_id, p.product_name, grd.quantity, grd.unit, grd.line_total, grd.note " +
            "FROM goods_receipt_detail grd " +
            "JOIN product p ON p.product_id = grd.product_id " +
            "WHERE grd.goods_receipt_id = ? " +
            "ORDER BY p.product_name";

        List<GoodsReceiptDetail> details = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, goodsReceiptId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GoodsReceiptDetail detail = new GoodsReceiptDetail();
                    detail.setGoodsReceiptId(rs.getString("goods_receipt_id"));
                    detail.setProductId(rs.getString("product_id"));
                    detail.setProductName(rs.getString("product_name"));
                    detail.setQuantity(rs.getBigDecimal("quantity"));
                    detail.setUnit(rs.getString("unit"));
                    detail.setLineTotal(rs.getBigDecimal("line_total"));
                    detail.setNote(readClob(rs, "note"));
                    details.add(detail);
                }
            }
        }
        return details;
    }

    private void deleteDetails(Connection conn, String goodsReceiptId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM goods_receipt_detail WHERE goods_receipt_id = ?")) {
            ps.setString(1, goodsReceiptId);
            ps.executeUpdate();
        }
    }

    private GoodsReceipt findByIdForUpdate(Connection conn, String goodsReceiptId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT goods_receipt_id, branch_id, employee_id, supplier_name, receipt_date, " +
                "total_quantity, total_item_count, status, note " +
                "FROM goods_receipt WHERE goods_receipt_id = ? FOR UPDATE")) {
            ps.setString(1, goodsReceiptId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                GoodsReceipt receipt = new GoodsReceipt();
                receipt.setGoodsReceiptId(rs.getString("goods_receipt_id"));
                receipt.setBranchId(rs.getString("branch_id"));
                receipt.setEmployeeId(rs.getString("employee_id"));
                receipt.setSupplierName(rs.getString("supplier_name"));
                Timestamp receiptDate = rs.getTimestamp("receipt_date");
                if (receiptDate != null) {
                    receipt.setReceiptDate(receiptDate.toLocalDateTime().toLocalDate());
                }
                receipt.setTotalQuantity(rs.getBigDecimal("total_quantity"));
                receipt.setTotalItemCount(rs.getInt("total_item_count"));
                receipt.setStatus(rs.getString("status"));
                receipt.setNote(readClob(rs, "note"));
                return receipt;
            }
        }
    }

    private GoodsReceipt mapReceipt(ResultSet rs) throws SQLException {
        GoodsReceipt receipt = new GoodsReceipt();
        receipt.setGoodsReceiptId(rs.getString("goods_receipt_id"));
        receipt.setBranchId(rs.getString("branch_id"));
        receipt.setEmployeeId(rs.getString("employee_id"));
        receipt.setEmployeeName(rs.getString("employee_name"));
        receipt.setSupplierName(rs.getString("supplier_name"));
        Timestamp receiptDate = rs.getTimestamp("receipt_date");
        if (receiptDate != null) {
            receipt.setReceiptDate(receiptDate.toLocalDateTime().toLocalDate());
        }
        receipt.setTotalQuantity(rs.getBigDecimal("total_quantity"));
        receipt.setTotalItemCount(rs.getInt("total_item_count"));
        receipt.setStatus(rs.getString("status"));
        receipt.setNote(readClob(rs, "note"));
        receipt.setProductSummary(rs.getString("product_summary"));
        receipt.setQuantitySummary(rs.getString("quantity_summary"));
        receipt.setUnitSummary(rs.getString("unit_summary"));
        receipt.setTotalAmount(rs.getBigDecimal("total_amount"));
        return receipt;
    }

    private void applyTotals(GoodsReceipt receipt, List<GoodsReceiptDetail> details) {
        BigDecimal totalQuantity = BigDecimal.ZERO;
        for (GoodsReceiptDetail detail : details) {
            totalQuantity = totalQuantity.add(detail.getQuantity() == null ? BigDecimal.ZERO : detail.getQuantity());
        }
        receipt.setTotalQuantity(totalQuantity);
        receipt.setTotalItemCount(details.size());
    }

    private BigDecimal calculateLineTotal(Connection conn, GoodsReceiptDetail detail) throws SQLException {
        if (detail.getLineTotal() != null && detail.getLineTotal().compareTo(BigDecimal.ZERO) > 0) {
            return detail.getLineTotal();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT cost_price FROM product WHERE product_id = ?")) {
            ps.setString(1, detail.getProductId());
            try (ResultSet rs = ps.executeQuery()) {
                BigDecimal costPrice = rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
                return costPrice.multiply(detail.getQuantity() == null ? BigDecimal.ZERO : detail.getQuantity());
            }
        }
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

    private Timestamp toTimestamp(LocalDate date) {
        LocalDate value = date == null ? LocalDate.now() : date;
        return Timestamp.valueOf(value.atStartOfDay());
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
