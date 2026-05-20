package PetHotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import PetHotel.model.Invoice;
import PetHotel.model.InvoiceDetail;
import PetHotel.util.DBConnection;

public class InvoiceDAO {

    private static final String SQL_SEARCH_INVOICES =
    "SELECT " +
    "o.order_id AS invoice_id, " +
    "o.customer_id, " +
    "o.booking_id, " +
    "o.grand_total AS total_amount, " +
    "o.created_at, " +
    "o.status " +
    "FROM orders o " +
    "WHERE (? IS NULL OR o.order_id = ?) " +
    "  AND (? IS NULL OR o.customer_id = ?) " +
    "  AND (? IS NULL OR o.created_at >= ?) " +
    "  AND (? IS NULL OR o.created_at <= ?) " +
    "ORDER BY o.created_at DESC";
    public List<Invoice> searchInvoices(String invoiceId, String customerId, Date fromDate, Date toDate) throws SQLException {
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
        invoice.setCreateDate(rs.getTimestamp("created_at"));
        invoice.setTotalAmount(rs.getDouble("total_amount"));
        invoice.setStatus(rs.getString("status"));
        return invoice;
    }
    public boolean createInvoice(Invoice invoice) throws SQLException {
    String sql =
        "INSERT INTO orders " +
        "(order_id, customer_id, branch_id, booking_id, created_by_emp, status, subtotal, grand_total, created_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, SYSDATE)";

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
}