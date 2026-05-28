package PetHotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import PetHotel.model.Invoice;
import PetHotel.model.InvoiceDetail;
import PetHotel.model.Payment;
import PetHotel.util.DBConnection;

public class InvoiceDAO {

    private static final String SQL_SEARCH_INVOICES =
    "SELECT " +
    "o.order_id AS invoice_id, " +
    "o.customer_id, " +
    "c.full_name AS customer_name, " +
    "o.booking_id, " +
    "o.grand_total AS total_amount, " +
    "o.created_at, " +
    "o.status " +
    "FROM orders o " +
    "LEFT JOIN customer c ON c.customer_id = o.customer_id " +
    "WHERE (? IS NULL OR o.order_id = ?) " +
    "  AND (? IS NULL OR o.customer_id = ?) " +
    "  AND (? IS NULL OR o.created_at >= ?) " +
    "  AND (? IS NULL OR o.created_at <= ?) " +
    "  AND (? IS NULL OR o.status = ?) " +
    "ORDER BY o.created_at DESC, o.order_id DESC";
    public List<Invoice> searchInvoices(String invoiceId, String customerId, Date fromDate, Date toDate) throws SQLException {
        return searchInvoices(invoiceId, customerId, fromDate, toDate, null);
    }

    public List<Invoice> searchInvoices(String invoiceId, String customerId, Date fromDate, Date toDate, String status) throws SQLException {
        List<Invoice> invoices = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SEARCH_INVOICES)) {

            int i = 1;

            if (invoiceId != null && !invoiceId.isEmpty()) {
                ps.setString(i++, invoiceId);
                ps.setString(i++, invoiceId);
            } else {
                ps.setNull(i++, Types.VARCHAR);
                ps.setNull(i++, Types.VARCHAR);
            }

            if (customerId != null && !customerId.isEmpty()) {
                ps.setString(i++, customerId);
                ps.setString(i++, customerId);
            } else {
                ps.setNull(i++, Types.VARCHAR);
                ps.setNull(i++, Types.VARCHAR);
            }

            if (fromDate != null) {
                ps.setTimestamp(i++, new Timestamp(fromDate.getTime()));
                ps.setTimestamp(i++, new Timestamp(fromDate.getTime()));
            } else {
                ps.setNull(i++, Types.TIMESTAMP);
                ps.setNull(i++, Types.TIMESTAMP);
            }

            if (toDate != null) {
                ps.setTimestamp(i++, new Timestamp(toDate.getTime()));
                ps.setTimestamp(i++, new Timestamp(toDate.getTime()));
            } else {
                ps.setNull(i++, Types.TIMESTAMP);
                ps.setNull(i++, Types.TIMESTAMP);
            }

            if (status != null && !status.trim().isEmpty()) {
                ps.setString(i++, status.trim());
                ps.setString(i++, status.trim());
            } else {
                ps.setNull(i++, Types.VARCHAR);
                ps.setNull(i++, Types.VARCHAR);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    invoices.add(mapRow(rs));
                }
            }
        }

        return invoices;
    }

    private Invoice mapRow(ResultSet rs) throws SQLException {
        Invoice invoice = new Invoice();
        invoice.setId(rs.getString("invoice_id"));
        invoice.setCustomerId(rs.getString("customer_id"));
        invoice.setCustomerName(rs.getString("customer_name"));
        invoice.setBookingId(rs.getString("booking_id"));
        invoice.setCreateDate(rs.getTimestamp("created_at"));
        invoice.setTotalAmount(rs.getDouble("total_amount"));
        invoice.setStatus(rs.getString("status"));
        return invoice;
    }

    public Invoice getInvoiceById(String orderId) throws SQLException {
        String sql =
            "SELECT " +
            "o.order_id AS invoice_id, " +
            "o.customer_id, " +
            "c.full_name AS customer_name, " +
            "o.booking_id, " +
            "o.grand_total AS total_amount, " +
            "o.created_at, " +
            "o.status " +
            "FROM orders o " +
            "LEFT JOIN customer c ON c.customer_id = o.customer_id " +
            "WHERE o.order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }

        return null;
    }

    public String generateNextOrderId() throws SQLException {
        String sql =
            "SELECT NVL(MAX(TO_NUMBER(SUBSTR(order_id, 4))), 0) AS max_no " +
            "FROM orders " +
            "WHERE REGEXP_LIKE(order_id, '^ORD[0-9]+$')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int next = 1;
            if (rs.next()) {
                next = rs.getInt("max_no") + 1;
            }
            return String.format("ORD%03d", next);
        }
    }

    public String generateNextOrderDetailId(Connection conn) throws SQLException {
        String sql =
            "SELECT NVL(MAX(TO_NUMBER(SUBSTR(order_detail_id, 3))), 0) AS max_no " +
            "FROM order_details " +
            "WHERE REGEXP_LIKE(order_detail_id, '^OD[0-9]+$')";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int next = 1;
            if (rs.next()) {
                next = rs.getInt("max_no") + 1;
            }
            return String.format("OD%03d", next);
        }
    }

    public String generateNextPaymentId() throws SQLException {
        String sql =
            "SELECT NVL(MAX(TO_NUMBER(SUBSTR(payment_id, 4))), 0) AS max_no " +
            "FROM payments " +
            "WHERE REGEXP_LIKE(payment_id, '^PAY[0-9]+$')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int next = 1;
            if (rs.next()) {
                next = rs.getInt("max_no") + 1;
            }
            return String.format("PAY%03d", next);
        }
    }

    public Map<String, Invoice> findBookingOptions() throws SQLException {
        Map<String, Invoice> bookings = new LinkedHashMap<>();
        String sql =
            "SELECT b.booking_id, b.customer_id, b.branch_id " +
            "FROM booking b " +
            "WHERE b.status <> 'CANCELLED' " +
            "AND NOT EXISTS (SELECT 1 FROM orders o WHERE o.booking_id = b.booking_id) " +
            "ORDER BY b.booking_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Invoice invoice = new Invoice();
                invoice.setBookingId(rs.getString("booking_id"));
                invoice.setCustomerId(rs.getString("customer_id"));
                invoice.setBranchId(rs.getString("branch_id"));
                bookings.put(invoice.getBookingId(), invoice);
            }
        }

        return bookings;
    }

    public Map<String, Double> findBookingFees() throws SQLException {
        Map<String, Double> bookingFees = new LinkedHashMap<>();
        String sql =
            "SELECT " +
            "    b.booking_id, " +
            "    NVL(SUM(NVL(tr.base_price_per_day, 0) * " +
            "        CASE " +
            "            WHEN b.checkin_expected_at IS NOT NULL AND b.checkout_expected_at IS NOT NULL " +
            "            THEN GREATEST(1, CEIL(CAST(b.checkout_expected_at AS DATE) - CAST(b.checkin_expected_at AS DATE))) " +
            "            ELSE 1 " +
            "        END), 0) AS booking_fee " +
            "FROM booking b " +
            "LEFT JOIN booking_room br ON br.booking_id = b.booking_id " +
            "LEFT JOIN room r ON r.room_id = br.room_id " +
            "LEFT JOIN type_room tr ON tr.type_room_id = r.type_room_id " +
            "WHERE b.status <> 'CANCELLED' " +
            "AND NOT EXISTS (SELECT 1 FROM orders o WHERE o.booking_id = b.booking_id) " +
            "GROUP BY b.booking_id, b.checkin_expected_at, b.checkout_expected_at " +
            "ORDER BY b.booking_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                bookingFees.put(rs.getString("booking_id"), rs.getDouble("booking_fee"));
            }
        }

        return bookingFees;
    }

    public boolean createInvoice(Invoice invoice) throws SQLException {
    String sql =
        "INSERT INTO orders " +
        "(order_id, customer_id, branch_id, booking_id, created_by_emp, status, subtotal, grand_total, created_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, invoice.getId());
        ps.setString(2, invoice.getCustomerId());
        ps.setString(3, invoice.getBranchId());
        ps.setString(4, invoice.getBookingId());
        ps.setString(5, invoice.getCreatedByEmp());
        ps.setString(6, invoice.getStatus());
        ps.setDouble(7, invoice.getSubtotal());
        ps.setDouble(8, invoice.getTotalAmount());
        if (invoice.getCreateDate() != null) {
            ps.setTimestamp(9, new Timestamp(invoice.getCreateDate().getTime()));
        } else {
            ps.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
        }

        return ps.executeUpdate() > 0;
    }
}

public boolean createInvoice(Invoice invoice, List<InvoiceDetail> details) throws SQLException {
    String invoiceSql =
        "INSERT INTO orders " +
        "(order_id, customer_id, branch_id, booking_id, created_by_emp, status, subtotal, grand_total, created_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    String syncTotalSql =
        "UPDATE orders " +
        "SET subtotal = (SELECT NVL(SUM(line_total), 0) FROM order_details WHERE order_id = ?), " +
        "    grand_total = (SELECT NVL(SUM(line_total), 0) FROM order_details WHERE order_id = ?) " +
        "WHERE order_id = ?";

    Connection conn = DBConnection.getConnection();
    if (conn == null) {
        throw new SQLException("Không thể kết nối database");
    }

    boolean oldAutoCommit = conn.getAutoCommit();
    try {
        conn.setAutoCommit(false);
        double detailTotal = calculateDetailTotal(details);
        if (details != null && !details.isEmpty()) {
            invoice.setSubtotal(detailTotal);
            invoice.setTotalAmount(detailTotal);
        }

        try (PreparedStatement ps = conn.prepareStatement(invoiceSql)) {
            ps.setString(1, invoice.getId());
            ps.setString(2, invoice.getCustomerId());
            ps.setString(3, invoice.getBranchId());
            ps.setString(4, invoice.getBookingId());
            ps.setString(5, invoice.getCreatedByEmp());
            ps.setString(6, invoice.getStatus());
            ps.setDouble(7, invoice.getSubtotal());
            ps.setDouble(8, invoice.getTotalAmount());
            if (invoice.getCreateDate() != null) {
                ps.setTimestamp(9, new Timestamp(invoice.getCreateDate().getTime()));
            } else {
                ps.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
            }
            ps.executeUpdate();
        }

        if (details != null) {
            for (InvoiceDetail detail : details) {
                if (detail.getDetailId() == null || detail.getDetailId().trim().isEmpty()) {
                    detail.setDetailId(generateNextOrderDetailId(conn));
                }
                createInvoiceDetail(detail, conn);
            }
        }

        if (details != null && !details.isEmpty()) {
            try (PreparedStatement ps = conn.prepareStatement(syncTotalSql)) {
                ps.setString(1, invoice.getId());
                ps.setString(2, invoice.getId());
                ps.setString(3, invoice.getId());
                ps.executeUpdate();
            }
        }

        conn.commit();
        return true;
    } catch (SQLException e) {
        conn.rollback();
        throw e;
    } finally {
        conn.setAutoCommit(oldAutoCommit);
        DBConnection.closeQuietly(conn);
    }
}

private double calculateDetailTotal(List<InvoiceDetail> details) {
    double total = 0;
    if (details != null) {
        for (InvoiceDetail detail : details) {
            total += detail.getLineTotal();
        }
    }
    return total;
}

public boolean createInvoiceDetail(InvoiceDetail detail) throws SQLException {
    try (Connection conn = DBConnection.getConnection()) {
        if (detail.getDetailId() == null || detail.getDetailId().trim().isEmpty()) {
            detail.setDetailId(generateNextOrderDetailId(conn));
        }
        return createInvoiceDetail(detail, conn);
    }
}

private boolean createInvoiceDetail(InvoiceDetail detail, Connection conn) throws SQLException {
    String sql =
        "INSERT INTO order_details " +
        "(order_detail_id, booking_service_id, booking_room_id, order_id, note, quantity, unit_price, line_total, created_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP)";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, detail.getDetailId());
        ps.setString(2, detail.getBookingServiceId());
        ps.setString(3, detail.getBookingRoomId());
        ps.setString(4, detail.getOrderId());
        ps.setString(5, detail.getNote());
        ps.setDouble(6, detail.getQuantity());
        ps.setDouble(7, detail.getUnitPrice());
        ps.setDouble(8, detail.getLineTotal());
        return ps.executeUpdate() > 0;
    }
}

public List<InvoiceDetail> getInvoiceDetailsByOrderId(String orderId) throws SQLException {
    List<InvoiceDetail> details = new ArrayList<>();

    String sql =
        "SELECT " +
        "order_detail_id, " +
        "order_id, " +
        "booking_room_id, " +
        "booking_service_id, " +
        "note, " +
        "quantity, " +
        "unit_price, " +
        "line_total " +
        "FROM order_details " +
        "WHERE order_id = ? " +
        "ORDER BY order_detail_id";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, orderId);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                InvoiceDetail detail = new InvoiceDetail();

                detail.setDetailId(rs.getString("order_detail_id"));
                detail.setOrderId(rs.getString("order_id"));
                detail.setBookingRoomId(rs.getString("booking_room_id"));
                detail.setBookingServiceId(rs.getString("booking_service_id"));
                detail.setNote(rs.getString("note"));
                detail.setQuantity(rs.getDouble("quantity"));
                detail.setUnitPrice(rs.getDouble("unit_price"));
                detail.setLineTotal(rs.getDouble("line_total"));

                details.add(detail);
            }
        }
    }

    return details;
}

public boolean createPayment(String orderId, String paymentMethod, double amount) throws SQLException {
    String sql =
        "INSERT INTO payments " +
        "(payment_id, order_id, payment_method, amount, status, paid_at, created_at, updated_at) " +
        "VALUES (?, ?, ?, ?, 'SUCCESS', SYSTIMESTAMP, SYSTIMESTAMP, SYSTIMESTAMP)";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, generateNextPaymentId());
        ps.setString(2, orderId);
        ps.setString(3, paymentMethod);
        ps.setDouble(4, amount);
        return ps.executeUpdate() > 0;
    }
}

public double getTotalPaidByOrderId(String orderId) throws SQLException {
    String sql =
        "SELECT NVL(SUM(amount), 0) AS total_paid " +
        "FROM payments " +
        "WHERE order_id = ? AND status = 'SUCCESS'";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, orderId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("total_paid");
            }
        }
    }
    return 0;
}

public List<Payment> getPaymentsByOrderId(String orderId) throws SQLException {
    return searchPaymentHistory(orderId, null, null, null, null);
}

public List<Payment> searchPaymentHistory(String keyword, String method, String status, Date fromDate, Date toDate)
        throws SQLException {
    List<Payment> payments = new ArrayList<>();
    String sql =
        "SELECT " +
        "p.payment_id, p.order_id, o.customer_id, c.full_name AS customer_name, " +
        "p.payment_method, p.amount, p.status, p.paid_at " +
        "FROM payments p " +
        "LEFT JOIN orders o ON o.order_id = p.order_id " +
        "LEFT JOIN customer c ON c.customer_id = o.customer_id " +
        "WHERE (? IS NULL OR LOWER(p.payment_id) LIKE LOWER(?) OR LOWER(p.order_id) LIKE LOWER(?)) " +
        "  AND (? IS NULL OR p.payment_method = ?) " +
        "  AND (? IS NULL OR p.status = ?) " +
        "  AND (? IS NULL OR p.paid_at >= ?) " +
        "  AND (? IS NULL OR p.paid_at <= ?) " +
        "ORDER BY p.paid_at DESC, p.payment_id DESC";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        int i = 1;
        if (keyword != null && !keyword.trim().isEmpty()) {
            String pattern = "%" + keyword.trim() + "%";
            ps.setString(i++, pattern);
            ps.setString(i++, pattern);
            ps.setString(i++, pattern);
        } else {
            ps.setNull(i++, Types.VARCHAR);
            ps.setNull(i++, Types.VARCHAR);
            ps.setNull(i++, Types.VARCHAR);
        }

        if (method != null && !method.trim().isEmpty()) {
            ps.setString(i++, method.trim());
            ps.setString(i++, method.trim());
        } else {
            ps.setNull(i++, Types.VARCHAR);
            ps.setNull(i++, Types.VARCHAR);
        }

        if (status != null && !status.trim().isEmpty()) {
            ps.setString(i++, status.trim());
            ps.setString(i++, status.trim());
        } else {
            ps.setNull(i++, Types.VARCHAR);
            ps.setNull(i++, Types.VARCHAR);
        }

        if (fromDate != null) {
            ps.setTimestamp(i++, new Timestamp(fromDate.getTime()));
            ps.setTimestamp(i++, new Timestamp(fromDate.getTime()));
        } else {
            ps.setNull(i++, Types.TIMESTAMP);
            ps.setNull(i++, Types.TIMESTAMP);
        }

        if (toDate != null) {
            ps.setTimestamp(i++, new Timestamp(toDate.getTime()));
            ps.setTimestamp(i++, new Timestamp(toDate.getTime()));
        } else {
            ps.setNull(i++, Types.TIMESTAMP);
            ps.setNull(i++, Types.TIMESTAMP);
        }

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Payment payment = new Payment();
                payment.setPaymentId(rs.getString("payment_id"));
                payment.setOrderId(rs.getString("order_id"));
                payment.setCustomerId(rs.getString("customer_id"));
                payment.setCustomerName(rs.getString("customer_name"));
                payment.setPaymentMethod(rs.getString("payment_method"));
                payment.setAmount(rs.getDouble("amount"));
                payment.setStatus(rs.getString("status"));
                payment.setPaidAt(rs.getTimestamp("paid_at"));
                payments.add(payment);
            }
        }
    }

    return payments;
}

public boolean updateOrderStatus(String orderId, String status) throws SQLException {
    String sql = "UPDATE orders SET status = ? WHERE order_id = ?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, status);
        ps.setString(2, orderId);
        return ps.executeUpdate() > 0;
    }
}

public boolean cancelInvoice(String orderId) throws SQLException {
    String sql =
        "UPDATE orders " +
        "SET status = 'CANCELLED' " +
        "WHERE order_id = ? " +
        "AND status IN ('PENDING', 'PARTIAL')";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, orderId);
        return ps.executeUpdate() > 0;
    }
}
}
