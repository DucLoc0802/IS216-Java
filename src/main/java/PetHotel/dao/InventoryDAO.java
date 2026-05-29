package PetHotel.dao;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import PetHotel.model.BranchInventory;
import PetHotel.model.CategoryProduct;
import PetHotel.model.GoodsReceipt;
import PetHotel.model.GoodsReceiptDetail;
import PetHotel.model.InventoryItem;
import PetHotel.model.Product;
import PetHotel.util.DBConnection;
import PetHotel.util.IDGenerator;

public class InventoryDAO {
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

    public List<CategoryProduct> findCategories() throws SQLException {
        String sql =
            "SELECT product_category_id, category_name " +
            "FROM category_product " +
            "ORDER BY category_name";

        List<CategoryProduct> categories = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categories.add(new CategoryProduct(
                    rs.getString("product_category_id"),
                    rs.getString("category_name")
                ));
            }
        }
        return categories;
    }

    public List<String> findBranchIds() throws SQLException {
        String sql = "SELECT branch_id FROM branch WHERE is_active = 1 ORDER BY branch_id";

        List<String> branchIds = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                branchIds.add(rs.getString("branch_id"));
            }
        }
        return branchIds;
    }

    public List<Product> findProducts() throws SQLException {
        String sql =
            "SELECT p.product_id, p.product_category_id, cp.category_name, p.product_name, p.unit, p.cost_price " +
            "FROM product p " +
            "JOIN category_product cp ON cp.product_category_id = p.product_category_id " +
            "ORDER BY cp.category_name, p.product_name";

        List<Product> products = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                products.add(mapProduct(rs));
            }
        }
        return products;
    }

    public Product findProductById(String productId) throws SQLException {
        String sql =
            "SELECT p.product_id, p.product_category_id, cp.category_name, p.product_name, p.unit, p.cost_price " +
            "FROM product p " +
            "JOIN category_product cp ON cp.product_category_id = p.product_category_id " +
            "WHERE p.product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapProduct(rs) : null;
            }
        }
    }

    public List<BranchInventory> searchInventory(String branchId, String keyword, String stockStatus)
            throws SQLException {
        String sql =
            "SELECT bi.branch_id, b.branch_name, bi.product_id, p.product_name, p.product_category_id, " +
            "       cp.category_name, p.unit, bi.quantity_in_stock, bi.reorder_point, bi.last_updated " +
            "FROM branch_inventory bi " +
            "JOIN branch b ON b.branch_id = bi.branch_id " +
            "JOIN product p ON p.product_id = bi.product_id " +
            "LEFT JOIN category_product cp ON cp.product_category_id = p.product_category_id " +
            "WHERE (? IS NULL OR bi.branch_id = ?) " +
            "  AND (? IS NULL OR LOWER(p.product_id) LIKE LOWER(?) " +
            "       OR LOWER(p.product_name) LIKE LOWER(?) OR LOWER(cp.category_name) LIKE LOWER(?)) " +
            "ORDER BY b.branch_id, p.product_name";

        List<BranchInventory> items = new ArrayList<>();
        String normalizedBranchId = normalize(branchId);
        String normalizedKeyword = normalize(keyword);
        String pattern = normalizedKeyword == null ? null : "%" + normalizedKeyword + "%";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizedBranchId);
            ps.setString(2, normalizedBranchId);
            ps.setString(3, normalizedKeyword);
            ps.setString(4, pattern);
            ps.setString(5, pattern);
            ps.setString(6, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BranchInventory item = mapBranchInventory(rs);
                    if (matchesBranchInventoryStatus(item, stockStatus)) {
                        items.add(item);
                    }
                }
            }
        }
        return items;
    }

    public BranchInventory findByBranchAndProduct(String branchId, String productId) throws SQLException {
        String sql =
            "SELECT bi.branch_id, b.branch_name, bi.product_id, p.product_name, p.product_category_id, " +
            "       cp.category_name, p.unit, bi.quantity_in_stock, bi.reorder_point, bi.last_updated " +
            "FROM branch_inventory bi " +
            "JOIN branch b ON b.branch_id = bi.branch_id " +
            "JOIN product p ON p.product_id = bi.product_id " +
            "LEFT JOIN category_product cp ON cp.product_category_id = p.product_category_id " +
            "WHERE bi.branch_id = ? AND bi.product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, branchId);
            ps.setString(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapBranchInventory(rs) : null;
            }
        }
    }

    public void upsertInventory(String branchId, String productId, BigDecimal quantityDelta) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            upsertInventory(conn, branchId, productId, quantityDelta);
            conn.commit();
        } catch (SQLException e) {
            DBConnection.rollbackQuietly(conn);
            throw e;
        } finally {
            closeTxConnection(conn);
        }
    }

    public void updateInventoryQuantity(String branchId, String productId, BigDecimal actualQuantity)
            throws SQLException {
        if (actualQuantity == null || actualQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new SQLException("Số lượng tồn không được nhỏ hơn 0.");
        }

        String sql =
            "MERGE INTO branch_inventory bi " +
            "USING (SELECT ? AS branch_id, ? AS product_id, ? AS quantity_in_stock FROM dual) src " +
            "ON (bi.branch_id = src.branch_id AND bi.product_id = src.product_id) " +
            "WHEN MATCHED THEN UPDATE SET bi.quantity_in_stock = src.quantity_in_stock, bi.last_updated = SYSTIMESTAMP " +
            "WHEN NOT MATCHED THEN INSERT (branch_id, product_id, quantity_in_stock, reorder_point, last_updated) " +
            "VALUES (src.branch_id, src.product_id, src.quantity_in_stock, 0, SYSTIMESTAMP)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, branchId);
            ps.setString(2, productId);
            ps.setBigDecimal(3, actualQuantity);
            ps.executeUpdate();
        }
    }

    public void updateReorderPoint(String branchId, String productId, BigDecimal reorderPoint) throws SQLException {
        if (reorderPoint != null && reorderPoint.compareTo(BigDecimal.ZERO) < 0) {
            throw new SQLException("Điểm đặt hàng lại không được nhỏ hơn 0.");
        }

        String sql =
            "MERGE INTO branch_inventory bi " +
            "USING (SELECT ? AS branch_id, ? AS product_id, ? AS reorder_point FROM dual) src " +
            "ON (bi.branch_id = src.branch_id AND bi.product_id = src.product_id) " +
            "WHEN MATCHED THEN UPDATE SET bi.reorder_point = src.reorder_point, bi.last_updated = SYSTIMESTAMP " +
            "WHEN NOT MATCHED THEN INSERT (branch_id, product_id, quantity_in_stock, reorder_point, last_updated) " +
            "VALUES (src.branch_id, src.product_id, 0, src.reorder_point, SYSTIMESTAMP)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, branchId);
            ps.setString(2, productId);
            if (reorderPoint == null) {
                ps.setNull(3, Types.NUMERIC);
            } else {
                ps.setBigDecimal(3, reorderPoint);
            }
            ps.executeUpdate();
        }
    }

    public void upsertInventory(Connection conn, String branchId, String productId, BigDecimal quantityDelta)
            throws SQLException {
        BigDecimal delta = quantityDelta == null ? BigDecimal.ZERO : quantityDelta;
        BigDecimal currentQuantity = null;

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT quantity_in_stock FROM branch_inventory " +
                "WHERE branch_id = ? AND product_id = ? FOR UPDATE")) {
            ps.setString(1, branchId);
            ps.setString(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    currentQuantity = rs.getBigDecimal(1);
                }
            }
        }

        if (currentQuantity == null) {
            if (delta.compareTo(BigDecimal.ZERO) < 0) {
                throw new SQLException("Không thể tạo tồn kho âm cho sản phẩm " + productId + ".");
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO branch_inventory (branch_id, product_id, quantity_in_stock, reorder_point, last_updated) " +
                    "VALUES (?, ?, ?, 0, SYSTIMESTAMP)")) {
                ps.setString(1, branchId);
                ps.setString(2, productId);
                ps.setBigDecimal(3, delta);
                ps.executeUpdate();
            }
            return;
        }

        BigDecimal newQuantity = currentQuantity.add(delta);
        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new SQLException("Tồn kho không được nhỏ hơn 0 cho sản phẩm " + productId + ".");
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE branch_inventory SET quantity_in_stock = ?, last_updated = SYSTIMESTAMP " +
                "WHERE branch_id = ? AND product_id = ?")) {
            ps.setBigDecimal(1, newQuantity);
            ps.setString(2, branchId);
            ps.setString(3, productId);
            ps.executeUpdate();
        }
    }

    public List<InventoryItem> findInventory(
            String branchId,
            String keyword,
            String categoryId,
            String status
    ) throws SQLException {
        String sql =
            "SELECT ? AS branch_id, p.product_id, p.product_name, p.product_category_id, cp.category_name, " +
            "       p.unit, p.cost_price, NVL(bi.quantity_in_stock, 0) AS quantity_in_stock, " +
            "       NVL(bi.reorder_point, 0) AS reorder_point, bi.last_updated " +
            "FROM product p " +
            "JOIN category_product cp ON cp.product_category_id = p.product_category_id " +
            "LEFT JOIN branch_inventory bi ON bi.product_id = p.product_id AND bi.branch_id = ? " +
            "WHERE (? IS NULL OR LOWER(p.product_id) LIKE LOWER(?) " +
            "       OR LOWER(p.product_name) LIKE LOWER(?) OR LOWER(cp.category_name) LIKE LOWER(?)) " +
            "  AND (? IS NULL OR p.product_category_id = ?) " +
            "ORDER BY CASE WHEN NVL(bi.quantity_in_stock, 0) = 0 THEN 0 " +
            "              WHEN NVL(bi.reorder_point, 0) > 0 AND NVL(bi.quantity_in_stock, 0) <= NVL(bi.reorder_point, 0) THEN 1 " +
            "              ELSE 2 END, cp.category_name, p.product_name";

        List<InventoryItem> items = new ArrayList<>();
        String normalizedKeyword = normalize(keyword);
        String pattern = normalizedKeyword == null ? null : "%" + normalizedKeyword + "%";
        String normalizedCategory = normalize(categoryId);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, branchId);
            ps.setString(2, branchId);
            ps.setString(3, normalizedKeyword);
            ps.setString(4, pattern);
            ps.setString(5, pattern);
            ps.setString(6, pattern);
            ps.setString(7, normalizedCategory);
            ps.setString(8, normalizedCategory);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InventoryItem item = mapInventoryItem(rs);
                    if (matchesStatus(item, status)) {
                        items.add(item);
                    }
                }
            }
        }
        return items;
    }

    public InventoryItem findInventoryItem(String branchId, String productId) throws SQLException {
        List<InventoryItem> items = findInventory(branchId, productId, null, null);
        for (InventoryItem item : items) {
            if (item.getProductId().equals(productId)) {
                return item;
            }
        }
        return null;
    }

    public List<InventoryItem> findLowStockItems(String branchId) throws SQLException {
        List<InventoryItem> items = findInventory(branchId, null, null, null);
        List<InventoryItem> lowItems = new ArrayList<>();
        for (InventoryItem item : items) {
            if (item.isLowOrOut()) {
                lowItems.add(item);
            }
        }
        return lowItems;
    }

    public int countApprovedReceiptsInMonth(String branchId) throws SQLException {
        String sql =
            "SELECT COUNT(*) FROM goods_receipt " +
            "WHERE branch_id = ? AND status = 'APPROVED' " +
            "  AND TRUNC(receipt_date, 'MM') = TRUNC(SYSDATE, 'MM')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public List<GoodsReceipt> findReceipts(
            String branchId,
            String keyword,
            LocalDate fromDate,
            LocalDate toDate
    ) throws SQLException {
        String sql =
            "SELECT gr.goods_receipt_id, gr.branch_id, gr.employee_id, e.full_name AS employee_name, " +
            "       gr.supplier_name, gr.receipt_date, gr.total_quantity, gr.total_item_count, " +
            "       gr.status, gr.note, " +
            RECEIPT_SUMMARY_SELECT +
            "FROM goods_receipt gr " +
            "LEFT JOIN employee e ON e.employee_id = gr.employee_id " +
            "WHERE gr.branch_id = ? " +
            "  AND (? IS NULL OR LOWER(gr.goods_receipt_id) LIKE LOWER(?) " +
            "       OR LOWER(gr.supplier_name) LIKE LOWER(?) " +
            "       OR EXISTS (SELECT 1 FROM goods_receipt_detail grd JOIN product p ON p.product_id = grd.product_id " +
            "                  WHERE grd.goods_receipt_id = gr.goods_receipt_id " +
            "                    AND (LOWER(p.product_id) LIKE LOWER(?) OR LOWER(p.product_name) LIKE LOWER(?)))) " +
            "  AND (? IS NULL OR TRUNC(gr.receipt_date) >= ?) " +
            "  AND (? IS NULL OR TRUNC(gr.receipt_date) <= ?) " +
            "ORDER BY gr.receipt_date DESC, gr.goods_receipt_id DESC";

        String normalizedKeyword = normalize(keyword);
        String pattern = normalizedKeyword == null ? null : "%" + normalizedKeyword + "%";
        List<GoodsReceipt> receipts = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, branchId);
            ps.setString(2, normalizedKeyword);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            ps.setString(5, pattern);
            ps.setString(6, pattern);
            if (fromDate == null) {
                ps.setNull(7, Types.DATE);
                ps.setNull(8, Types.DATE);
            } else {
                ps.setDate(7, Date.valueOf(fromDate));
                ps.setDate(8, Date.valueOf(fromDate));
            }
            if (toDate == null) {
                ps.setNull(9, Types.DATE);
                ps.setNull(10, Types.DATE);
            } else {
                ps.setDate(9, Date.valueOf(toDate));
                ps.setDate(10, Date.valueOf(toDate));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    receipts.add(mapGoodsReceipt(rs));
                }
            }
        }
        return receipts;
    }

    public GoodsReceipt findReceiptById(String receiptId) throws SQLException {
        String sql =
            "SELECT gr.goods_receipt_id, gr.branch_id, gr.employee_id, e.full_name AS employee_name, " +
            "       gr.supplier_name, gr.receipt_date, gr.total_quantity, gr.total_item_count, " +
            "       gr.status, gr.note, " +
            RECEIPT_SUMMARY_SELECT +
            "FROM goods_receipt gr " +
            "LEFT JOIN employee e ON e.employee_id = gr.employee_id " +
            "WHERE gr.goods_receipt_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, receiptId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                GoodsReceipt receipt = mapGoodsReceipt(rs);
                receipt.getDetails().addAll(findReceiptDetails(conn, receiptId));
                return receipt;
            }
        }
    }

    public GoodsReceipt createApprovedReceipt(
            String branchId,
            String employeeId,
            String supplierName,
            LocalDate receiptDate,
            List<GoodsReceiptDetail> details,
            String note
    ) throws SQLException {
        String receiptId = IDGenerator.nextGoodsReceiptId();
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            insertReceiptHeader(conn, receiptId, branchId, employeeId, supplierName, receiptDate, note);
            insertReceiptDetails(conn, receiptId, details);
            approveReceipt(conn, receiptId);
            conn.commit();
            return findReceiptById(receiptId);
        } catch (SQLException e) {
            DBConnection.rollbackQuietly(conn);
            throw e;
        } finally {
            closeTxConnection(conn);
        }
    }

    public void updateApprovedReceipt(
            String receiptId,
            String branchId,
            String employeeId,
            String supplierName,
            LocalDate receiptDate,
            List<GoodsReceiptDetail> newDetails,
            String note
    ) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            GoodsReceipt current = findReceiptByIdForUpdate(conn, receiptId);
            if (current == null) {
                throw new SQLException("Không tìm thấy phiếu nhập: " + receiptId);
            }
            if (!branchId.equals(current.getBranchId())) {
                throw new SQLException("Không được sửa phiếu nhập của chi nhánh khác.");
            }
            if ("CANCELLED".equals(current.getStatus())) {
                throw new SQLException("Phiếu nhập đã hủy, không thể sửa.");
            }

            if ("APPROVED".equals(current.getStatus())) {
                reverseReceiptStock(conn, branchId, current.getDetails());
            }

            deleteReceiptDetails(conn, receiptId);
            updateReceiptHeader(conn, receiptId, employeeId, supplierName, receiptDate, note, "DRAFT");
            insertReceiptDetails(conn, receiptId, newDetails);
            approveReceipt(conn, receiptId);
            conn.commit();
        } catch (SQLException e) {
            DBConnection.rollbackQuietly(conn);
            throw e;
        } finally {
            closeTxConnection(conn);
        }
    }

    public void cancelReceipt(String receiptId, String branchId) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            GoodsReceipt current = findReceiptByIdForUpdate(conn, receiptId);
            if (current == null) {
                throw new SQLException("Không tìm thấy phiếu nhập: " + receiptId);
            }
            if (!branchId.equals(current.getBranchId())) {
                throw new SQLException("Không được hủy phiếu nhập của chi nhánh khác.");
            }
            if ("CANCELLED".equals(current.getStatus())) {
                throw new SQLException("Phiếu nhập đã được hủy trước đó.");
            }
            if ("APPROVED".equals(current.getStatus())) {
                reverseReceiptStock(conn, branchId, current.getDetails());
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE goods_receipt SET status = 'CANCELLED', updated_at = SYSTIMESTAMP WHERE goods_receipt_id = ?")) {
                ps.setString(1, receiptId);
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

    public void adjustStock(
            String branchId,
            String employeeId,
            String productId,
            BigDecimal actualQuantity,
            BigDecimal reorderPoint,
            String note
    ) throws SQLException {
        String auditId = IDGenerator.nextStockAuditId();
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            BigDecimal systemQuantity = findSystemQuantity(conn, branchId, productId);

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO stock_audit (stock_audit_id, branch_id, employee_id, audit_date, status, note, created_at, updated_at) " +
                    "VALUES (?, ?, ?, SYSTIMESTAMP, 'DRAFT', ?, SYSTIMESTAMP, SYSTIMESTAMP)")) {
                ps.setString(1, auditId);
                ps.setString(2, branchId);
                ps.setString(3, employeeId);
                setClobString(ps, 4, note);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO stock_audit_detail (stock_audit_id, product_id, system_quantity, actual_quantity, note, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, SYSTIMESTAMP, SYSTIMESTAMP)")) {
                ps.setString(1, auditId);
                ps.setString(2, productId);
                ps.setBigDecimal(3, systemQuantity);
                ps.setBigDecimal(4, actualQuantity);
                setClobString(ps, 5, note);
                ps.executeUpdate();
            }

            try (CallableStatement cs = conn.prepareCall("{call sp_complete_stock_audit(?)}")) {
                cs.setString(1, auditId);
                cs.execute();
            }

            if (reorderPoint != null) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE branch_inventory SET reorder_point = ?, last_updated = SYSTIMESTAMP " +
                        "WHERE branch_id = ? AND product_id = ?")) {
                    ps.setBigDecimal(1, reorderPoint);
                    ps.setString(2, branchId);
                    ps.setString(3, productId);
                    ps.executeUpdate();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            DBConnection.rollbackQuietly(conn);
            throw e;
        } finally {
            closeTxConnection(conn);
        }
    }

    public void recordMaterialWaste(
            String materialWasteId,
            String productId,
            String employeeId,
            String branchId,
            BigDecimal quantity,
            String reason,
            String note
    ) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_record_material_waste(?, ?, ?, ?, ?, ?, ?)}")) {
            cs.setString(1, materialWasteId);
            cs.setString(2, productId);
            cs.setString(3, employeeId);
            cs.setString(4, branchId);
            cs.setBigDecimal(5, quantity);
            setClobString(cs, 6, reason);
            setClobString(cs, 7, note);
            cs.execute();
        }
    }

    private void insertReceiptHeader(
            Connection conn,
            String receiptId,
            String branchId,
            String employeeId,
            String supplierName,
            LocalDate receiptDate,
            String note
    ) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO goods_receipt (goods_receipt_id, branch_id, employee_id, supplier_name, receipt_date, " +
                "total_quantity, total_item_count, status, note, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, 0, 0, 'DRAFT', ?, SYSTIMESTAMP, SYSTIMESTAMP)")) {
            ps.setString(1, receiptId);
            ps.setString(2, branchId);
            ps.setString(3, employeeId);
            ps.setString(4, supplierName);
            ps.setTimestamp(5, toTimestamp(receiptDate));
            setClobString(ps, 6, note);
            ps.executeUpdate();
        }
    }

    private void updateReceiptHeader(
            Connection conn,
            String receiptId,
            String employeeId,
            String supplierName,
            LocalDate receiptDate,
            String note,
            String status
    ) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE goods_receipt SET employee_id = ?, supplier_name = ?, receipt_date = ?, note = ?, " +
                "status = ?, updated_at = SYSTIMESTAMP WHERE goods_receipt_id = ?")) {
            ps.setString(1, employeeId);
            ps.setString(2, supplierName);
            ps.setTimestamp(3, toTimestamp(receiptDate));
            setClobString(ps, 4, note);
            ps.setString(5, status);
            ps.setString(6, receiptId);
            ps.executeUpdate();
        }
    }

    private void insertReceiptDetails(Connection conn, String receiptId, List<GoodsReceiptDetail> details)
            throws SQLException {
        for (GoodsReceiptDetail detail : details) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO goods_receipt_detail (goods_receipt_id, product_id, quantity, unit, line_total, note, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, (SELECT cost_price FROM product WHERE product_id = ?) * ?, ?, SYSTIMESTAMP, SYSTIMESTAMP)")) {
                ps.setString(1, receiptId);
                ps.setString(2, detail.getProductId());
                ps.setBigDecimal(3, detail.getQuantity());
                ps.setString(4, detail.getUnit());
                ps.setString(5, detail.getProductId());
                ps.setBigDecimal(6, detail.getQuantity());
                setClobString(ps, 7, detail.getNote());
                ps.executeUpdate();
            }
        }
    }

    private void approveReceipt(Connection conn, String receiptId) throws SQLException {
        try (CallableStatement cs = conn.prepareCall("{call sp_approve_goods_receipt(?)}")) {
            cs.setString(1, receiptId);
            cs.execute();
        }
    }

    private GoodsReceipt findReceiptByIdForUpdate(Connection conn, String receiptId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT gr.goods_receipt_id, gr.branch_id, gr.employee_id, gr.supplier_name, " +
                "       gr.receipt_date, gr.total_quantity, gr.total_item_count, gr.status, gr.note " +
                "FROM goods_receipt gr " +
                "WHERE gr.goods_receipt_id = ? FOR UPDATE")) {
            ps.setString(1, receiptId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                GoodsReceipt receipt = mapGoodsReceiptForUpdate(rs);
                receipt.getDetails().addAll(findReceiptDetails(conn, receiptId));
                return receipt;
            }
        }
    }

    private List<GoodsReceiptDetail> findReceiptDetails(Connection conn, String receiptId) throws SQLException {
        String sql =
            "SELECT grd.goods_receipt_id, grd.product_id, p.product_name, grd.quantity, grd.unit, grd.line_total, grd.note " +
            "FROM goods_receipt_detail grd " +
            "JOIN product p ON p.product_id = grd.product_id " +
            "WHERE grd.goods_receipt_id = ? " +
            "ORDER BY p.product_name";

        List<GoodsReceiptDetail> details = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, receiptId);
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

    private void deleteReceiptDetails(Connection conn, String receiptId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM goods_receipt_detail WHERE goods_receipt_id = ?")) {
            ps.setString(1, receiptId);
            ps.executeUpdate();
        }
    }

    private void reverseReceiptStock(Connection conn, String branchId, List<GoodsReceiptDetail> details)
            throws SQLException {
        for (GoodsReceiptDetail detail : details) {
            BigDecimal currentQty;
            try (PreparedStatement lock = conn.prepareStatement(
                    "SELECT quantity_in_stock FROM branch_inventory " +
                    "WHERE branch_id = ? AND product_id = ? FOR UPDATE")) {
                lock.setString(1, branchId);
                lock.setString(2, detail.getProductId());
                try (ResultSet rs = lock.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Không tìm thấy tồn kho để đảo phiếu nhập cho vật tư " + detail.getProductId());
                    }
                    currentQty = rs.getBigDecimal(1);
                }
            }

            if (currentQty.compareTo(detail.getQuantity()) < 0) {
                throw new SQLException("Không thể sửa/hủy phiếu nhập vì tồn kho hiện tại của "
                    + detail.getProductName() + " nhỏ hơn số lượng cần đảo.");
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE branch_inventory SET quantity_in_stock = quantity_in_stock - ?, last_updated = SYSTIMESTAMP " +
                    "WHERE branch_id = ? AND product_id = ?")) {
                ps.setBigDecimal(1, detail.getQuantity());
                ps.setString(2, branchId);
                ps.setString(3, detail.getProductId());
                ps.executeUpdate();
            }
        }
    }

    private BigDecimal findSystemQuantity(Connection conn, String branchId, String productId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT NVL(quantity_in_stock, 0) FROM branch_inventory WHERE branch_id = ? AND product_id = ?")) {
            ps.setString(1, branchId);
            ps.setString(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getString("product_id"));
        product.setProductCategoryId(rs.getString("product_category_id"));
        product.setCategoryName(rs.getString("category_name"));
        product.setProductName(rs.getString("product_name"));
        product.setUnit(rs.getString("unit"));
        product.setCostPrice(rs.getBigDecimal("cost_price"));
        return product;
    }

    private InventoryItem mapInventoryItem(ResultSet rs) throws SQLException {
        InventoryItem item = new InventoryItem();
        item.setBranchId(rs.getString("branch_id"));
        item.setProductId(rs.getString("product_id"));
        item.setProductName(rs.getString("product_name"));
        item.setProductCategoryId(rs.getString("product_category_id"));
        item.setCategoryName(rs.getString("category_name"));
        item.setUnit(rs.getString("unit"));
        item.setCostPrice(rs.getBigDecimal("cost_price"));
        item.setQuantityInStock(rs.getBigDecimal("quantity_in_stock"));
        item.setReorderPoint(rs.getBigDecimal("reorder_point"));
        Timestamp lastUpdated = rs.getTimestamp("last_updated");
        if (lastUpdated != null) {
            item.setLastUpdated(lastUpdated.toInstant().atOffset(ZoneOffset.UTC));
        }
        return item;
    }

    private BranchInventory mapBranchInventory(ResultSet rs) throws SQLException {
        BranchInventory item = new BranchInventory();
        item.setBranchId(rs.getString("branch_id"));
        item.setBranchName(rs.getString("branch_name"));
        item.setProductId(rs.getString("product_id"));
        item.setProductName(rs.getString("product_name"));
        item.setProductCategoryId(rs.getString("product_category_id"));
        item.setCategoryName(rs.getString("category_name"));
        item.setUnit(rs.getString("unit"));
        item.setQuantityInStock(rs.getBigDecimal("quantity_in_stock"));
        item.setReorderPoint(rs.getBigDecimal("reorder_point"));
        Timestamp lastUpdated = rs.getTimestamp("last_updated");
        if (lastUpdated != null) {
            item.setLastUpdated(lastUpdated.toInstant().atOffset(ZoneOffset.UTC));
        }
        return item;
    }

    private GoodsReceipt mapGoodsReceipt(ResultSet rs) throws SQLException {
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

    private GoodsReceipt mapGoodsReceiptForUpdate(ResultSet rs) throws SQLException {
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

    private boolean matchesStatus(InventoryItem item, String status) {
        String normalized = normalize(status);
        if (normalized == null || "Tất cả".equalsIgnoreCase(normalized)) {
            return true;
        }
        return item.getStatus().equals(normalized);
    }

    private boolean matchesBranchInventoryStatus(BranchInventory item, String status) {
        String normalized = normalize(status);
        if (normalized == null || BranchInventory.STATUS_ALL.equalsIgnoreCase(normalized)) {
            return true;
        }
        return item.getStockStatus().equals(normalized);
    }

    private Timestamp toTimestamp(LocalDate date) {
        LocalDate value = date == null ? LocalDate.now() : date;
        return Timestamp.valueOf(value.atStartOfDay());
    }

    private String readClob(ResultSet rs, String columnName) throws SQLException {
        Clob clob = rs.getClob(columnName);
        if (clob == null) {
            return null;
        }
        return clob.getSubString(1, (int) clob.length());
    }

    private void setClobString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            ps.setNull(index, Types.CLOB);
        } else {
            ps.setString(index, value.trim());
        }
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void closeTxConnection(Connection conn) throws SQLException {
        if (conn != null) {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    /**
     * Lấy số lượng tồn kho từ BRANCH_INVENTORY.
     * Nếu không có bản ghi thì trả về 0.
     * 
     * @param branchId
     * @param productId
     * @param conn Connection được sử dụng trong transaction
     * @return Số lượng tồn kho
     * @throws SQLException
     */
    public BigDecimal getQuantity(String branchId, String productId, Connection conn) throws SQLException {
        String sql =
            "SELECT quantity_in_stock FROM branch_inventory " +
            "WHERE branch_id = ? AND product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, branchId);
            ps.setString(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal qty = rs.getBigDecimal("quantity_in_stock");
                    return qty != null ? qty : BigDecimal.ZERO;
                }
            }
        }

        return BigDecimal.ZERO;
    }

    /**
     * Trừ tồn kho trong BRANCH_INVENTORY.
     * 
     * SQL:
     *   UPDATE branch_inventory
     *   SET quantity_in_stock = quantity_in_stock - ?,
     *       last_updated = SYSTIMESTAMP
     *   WHERE branch_id = ? AND product_id = ?
     *   AND quantity_in_stock >= ?
     *
     * Nếu executeUpdate trả về 0 thì không đủ tồn hoặc không có bản ghi.
     *
     * @param branchId
     * @param productId
     * @param amount    Số lượng trừ (phải > 0)
     * @param conn      Connection được sử dụng trong transaction
     * @return Số hàng bị update (1 = thành công, 0 = không đủ tồn)
     * @throws SQLException
     */
    public int subtractInventory(String branchId, String productId, BigDecimal amount, Connection conn)
            throws SQLException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new SQLException("Số lượng trừ phải lớn hơn 0");
        }

        String sql =
            "UPDATE branch_inventory " +
            "SET quantity_in_stock = quantity_in_stock - ?, " +
            "    last_updated = SYSTIMESTAMP " +
            "WHERE branch_id = ? AND product_id = ? " +
            "  AND quantity_in_stock >= ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setString(2, branchId);
            ps.setString(3, productId);
            ps.setBigDecimal(4, amount);
            return ps.executeUpdate();
        }
    }
}
