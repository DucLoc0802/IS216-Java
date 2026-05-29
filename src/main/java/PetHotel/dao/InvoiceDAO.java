package PetHotel.dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import PetHotel.model.Invoice;
import PetHotel.model.InvoiceDetail;
import PetHotel.model.Customer;
import PetHotel.model.Payment;
import PetHotel.util.DBConnection;

public class InvoiceDAO {

    private static final String SQL_SEARCH_INVOICES =
    "SELECT " +
    "o.order_id AS invoice_id, " +
    "o.customer_id, " +
    "c.full_name AS customer_name, " +
    "c.phone AS customer_phone, " +
    "o.booking_id, " +
    "o.created_by_emp, " +
    "e.full_name AS created_by_emp_name, " +
    "NVL(b.deposit_amount, 0) AS prepaid_amount, " +
    "NVL(paid.total_paid, 0) AS paid_amount, " +
    "o.grand_total AS total_amount, " +
    "CASE " +
    "  WHEN UPPER(NVL(o.status, ' ')) IN ('PAID', 'CANCELLED', 'CANCELED') THEN 0 " +
    "  ELSE GREATEST(NVL(o.grand_total, 0) - NVL(paid.total_paid, 0), 0) " +
    "END AS remaining_amount, " +
    "o.created_at, " +
    "o.status " +
    "FROM orders o " +
    "LEFT JOIN customer c ON c.customer_id = o.customer_id " +
    "LEFT JOIN employee e ON e.employee_id = o.created_by_emp " +
    "LEFT JOIN booking b ON b.booking_id = o.booking_id " +
    "LEFT JOIN ( " +
    "  SELECT order_id, SUM(NVL(amount, 0)) AS total_paid " +
    "  FROM payments " +
    "  WHERE UPPER(NVL(status, ' ')) IN ('SUCCESS', 'PAID') " +
    "  GROUP BY order_id " +
    ") paid ON paid.order_id = o.order_id " +
    "WHERE (? IS NULL OR LOWER(o.order_id) LIKE LOWER(?) OR LOWER(c.full_name) LIKE LOWER(?) OR NVL(c.phone, ' ') LIKE ?) " +
    "  AND (? IS NULL OR o.customer_id = ?) " +
    "  AND (? IS NULL OR o.created_at >= ?) " +
    "  AND (? IS NULL OR o.created_at <= ?) " +
    "  AND (? IS NULL OR o.status = ? OR (? = 'CANCELLED' AND o.status = 'CANCELED')) " +
    "ORDER BY " +
    "  CASE UPPER(NVL(o.status, ' ')) " +
    "    WHEN 'PENDING' THEN 1 " +
    "    WHEN 'PARTIAL' THEN 2 " +
    "    WHEN 'PAID' THEN 3 " +
    "    WHEN 'CANCELLED' THEN 4 " +
    "    WHEN 'CANCELED' THEN 4 " +
    "    ELSE 5 " +
    "  END, " +
    "  o.created_at DESC, o.order_id DESC";

    public List<Customer> searchInvoiceCustomers(String keyword) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql =
            "WITH eligible_booking AS ( " +
            "    SELECT b.booking_id, b.customer_id " +
            "    FROM booking b " +
            "    JOIN ( " +
            "        SELECT " +
            "            bf.booking_id, " +
            "            SUM(NVL(tr.base_price_per_day, 0) * " +
            "                CASE " +
            "                    WHEN bf.checkin_expected_at IS NOT NULL AND bf.checkout_expected_at IS NOT NULL " +
            "                    THEN GREATEST(1, CEIL(CAST(bf.checkout_expected_at AS DATE) - CAST(bf.checkin_expected_at AS DATE))) " +
            "                    ELSE 1 " +
            "                END) AS total_amount " +
            "        FROM booking bf " +
            "        JOIN booking_room br ON br.booking_id = bf.booking_id " +
            "        JOIN room r ON r.room_id = br.room_id " +
            "        JOIN type_room tr ON tr.type_room_id = r.type_room_id " +
            "        WHERE NVL(tr.base_price_per_day, 0) > 0 " +
            "          AND NOT EXISTS ( " +
            "              SELECT 1 FROM order_details od " +
            "              JOIN orders oo ON oo.order_id = od.order_id " +
            "              WHERE od.booking_room_id = br.booking_room_id " +
            "                AND UPPER(NVL(oo.status, ' ')) NOT IN ('CANCELLED', 'CANCELED') " +
            "          ) " +
            "        GROUP BY bf.booking_id, bf.checkin_expected_at, bf.checkout_expected_at " +
            "    ) fees ON fees.booking_id = b.booking_id " +
            "    WHERE UPPER(NVL(b.status, ' ')) <> 'CANCELLED' " +
            "      AND fees.total_amount > 0 " +
            "      AND fees.total_amount - NVL(b.deposit_amount, 0) > 0 " +
            "), eligible_service AS ( " +
            "    SELECT DISTINCT c.customer_id " +
            "    FROM customer c " +
            "    JOIN booking b ON b.customer_id = c.customer_id " +
            "    JOIN booking_services bs ON bs.booking_id = b.booking_id " +
            "    JOIN services s ON s.service_id = bs.service_id " +
            "    WHERE UPPER(NVL(b.status, ' ')) <> 'CANCELLED' " +
            "      AND UPPER(NVL(bs.status, ' ')) <> 'CANCELLED' " +
            "      AND NVL(s.base_price, 0) > 0 " +
            "      AND NOT EXISTS ( " +
            "          SELECT 1 " +
            "          FROM order_details od " +
            "          JOIN orders oo ON oo.order_id = od.order_id " +
            "          WHERE od.booking_service_id = bs.booking_service_id " +
            "            AND UPPER(NVL(oo.status, ' ')) NOT IN ('CANCELLED', 'CANCELED') " +
            "      ) " +
            ") " +
            "SELECT c.customer_id, c.full_name, c.email, c.phone " +
            "FROM customer c " +
            "WHERE ( " +
            "      LOWER(c.full_name) LIKE LOWER(?) " +
            "      OR NVL(c.phone, ' ') LIKE ? " +
            "      OR LOWER(c.customer_id) LIKE LOWER(?) " +
            ") " +
            "  AND ( " +
            "      EXISTS (SELECT 1 FROM eligible_booking eb WHERE eb.customer_id = c.customer_id) " +
            "      OR EXISTS (SELECT 1 FROM eligible_service es WHERE es.customer_id = c.customer_id) " +
            "  ) " +
            "ORDER BY c.full_name, c.customer_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + (keyword == null ? "" : keyword.trim()) + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Customer customer = new Customer();
                    customer.setCustomerId(rs.getString("customer_id"));
                    customer.setFullName(rs.getString("full_name"));
                    customer.setEmail(rs.getString("email"));
                    customer.setPhone(rs.getString("phone"));
                    customers.add(customer);
                }
            }
        }
        return customers;
    }
    public List<Invoice> searchInvoices(String invoiceId, String customerId, Date fromDate, Date toDate) throws SQLException {
        return searchInvoices(invoiceId, customerId, fromDate, toDate, null);
    }

    public List<Invoice> searchInvoices(String invoiceId, String customerId, Date fromDate, Date toDate, String status) throws SQLException {
        List<Invoice> invoices = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SEARCH_INVOICES)) {

            int i = 1;

            if (invoiceId != null && !invoiceId.isEmpty()) {
                String pattern = "%" + invoiceId.trim() + "%";
                ps.setString(i++, pattern);
                ps.setString(i++, pattern);
                ps.setString(i++, pattern);
                ps.setString(i++, pattern);
            } else {
                ps.setNull(i++, Types.VARCHAR);
                ps.setNull(i++, Types.VARCHAR);
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
                ps.setString(i++, status.trim());
            } else {
                ps.setNull(i++, Types.VARCHAR);
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
        invoice.setCustomerPhone(rs.getString("customer_phone"));
        invoice.setBookingId(rs.getString("booking_id"));
        invoice.setCreatedByEmp(rs.getString("created_by_emp"));
        invoice.setCreatedByEmpName(rs.getString("created_by_emp_name"));
        invoice.setPrepaidAmount(rs.getDouble("prepaid_amount"));
        invoice.setPaidAmount(rs.getDouble("paid_amount"));
        invoice.setCreateDate(rs.getTimestamp("created_at"));
        invoice.setTotalAmount(rs.getDouble("total_amount"));
        invoice.setRemainingAmount(rs.getDouble("remaining_amount"));
        invoice.setStatus(rs.getString("status"));
        return invoice;
    }

    public Invoice getInvoiceById(String orderId) throws SQLException {
        String sql =
            "SELECT " +
            "o.order_id AS invoice_id, " +
            "o.customer_id, " +
            "c.full_name AS customer_name, " +
            "c.phone AS customer_phone, " +
            "o.booking_id, " +
            "o.created_by_emp, " +
            "e.full_name AS created_by_emp_name, " +
            "NVL(b.deposit_amount, 0) AS prepaid_amount, " +
            "NVL(paid.total_paid, 0) AS paid_amount, " +
            "o.grand_total AS total_amount, " +
            "CASE " +
            "  WHEN UPPER(NVL(o.status, ' ')) IN ('PAID', 'CANCELLED', 'CANCELED') THEN 0 " +
            "  ELSE GREATEST(NVL(o.grand_total, 0) - NVL(paid.total_paid, 0), 0) " +
            "END AS remaining_amount, " +
            "o.created_at, " +
            "o.status " +
            "FROM orders o " +
            "LEFT JOIN customer c ON c.customer_id = o.customer_id " +
            "LEFT JOIN employee e ON e.employee_id = o.created_by_emp " +
            "LEFT JOIN booking b ON b.booking_id = o.booking_id " +
            "LEFT JOIN ( " +
            "  SELECT order_id, SUM(NVL(amount, 0)) AS total_paid " +
            "  FROM payments " +
            "  WHERE UPPER(NVL(status, ' ')) IN ('SUCCESS', 'PAID') " +
            "  GROUP BY order_id " +
            ") paid ON paid.order_id = o.order_id " +
            "WHERE o.order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Invoice invoice = mapRow(rs);
                    applyLatestPaymentTenderInfo(conn, invoice);
                    return invoice;
                }
            }
        }

        return null;
    }

    private void applyLatestPaymentTenderInfo(Connection conn, Invoice invoice) throws SQLException {
        if (invoice == null || invoice.getId() == null) {
            return;
        }

        String noteColumn = null;
        if (hasColumn(conn, "PAYMENTS", "NOTE")) {
            noteColumn = "note";
        } else if (hasColumn(conn, "PAYMENTS", "DESCRIPTION")) {
            noteColumn = "description";
        }

        if (noteColumn == null) {
            return;
        }

        String sql =
            "SELECT amount, payment_note " +
            "FROM ( " +
            "  SELECT amount, " + noteColumn + " AS payment_note " +
            "  FROM payments " +
            "  WHERE order_id = ? " +
            "    AND UPPER(NVL(status, ' ')) IN ('SUCCESS', 'PAID') " +
            "  ORDER BY paid_at DESC, payment_id DESC " +
            ") " +
            "WHERE ROWNUM = 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, invoice.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return;
                }

                String note = rs.getString("payment_note");
                if (note == null || note.trim().isEmpty()) {
                    return;
                }

                double[] tenderInfo = parseTenderInfo(note, rs.getDouble("amount"));
                invoice.setCustomerTenderedAmount(tenderInfo[0]);
                invoice.setChangeAmount(tenderInfo[1]);
            }
        }
    }

    private double[] parseTenderInfo(String note, double paymentAmount) {
        double customerTendered = 0;
        double changeAmount = 0;

        Matcher matcher = Pattern.compile("[-+]?\\d+(?:\\.\\d+)?").matcher(note.replace(",", ""));
        if (matcher.find()) {
            customerTendered = Double.parseDouble(matcher.group());
        }
        if (matcher.find()) {
            changeAmount = Double.parseDouble(matcher.group());
        } else if (customerTendered > 0) {
            changeAmount = Math.max(customerTendered - paymentAmount, 0);
        }

        return new double[] { Math.max(customerTendered, 0), Math.max(changeAmount, 0) };
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
            "AND NOT EXISTS ( " +
            "    SELECT 1 FROM orders o " +
            "    WHERE o.booking_id = b.booking_id " +
            "      AND UPPER(NVL(o.status, ' ')) NOT IN ('CANCELLED', 'CANCELED') " +
            ") " +
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
            "AND NOT EXISTS ( " +
            "    SELECT 1 FROM orders o " +
            "    WHERE o.booking_id = b.booking_id " +
            "      AND UPPER(NVL(o.status, ' ')) NOT IN ('CANCELLED', 'CANCELED') " +
            ") " +
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

    public List<Invoice.InvoiceSource> findUninvoicedBookingSources(String customerId) throws SQLException {
        List<Invoice.InvoiceSource> sources = new ArrayList<>();
        String sql =
            "SELECT " +
            "    b.booking_id, b.customer_id, c.full_name AS customer_name, b.branch_id, " +
            "    b.checkin_expected_at, b.checkout_expected_at, b.status, NVL(b.deposit_amount, 0) AS prepaid_amount, " +
            "    pets.pet_names, fees.room_numbers, fees.total_amount " +
            "FROM booking b " +
            "JOIN customer c ON c.customer_id = b.customer_id " +
            "JOIN ( " +
            "    SELECT " +
            "        br.booking_id, " +
            "        LISTAGG(r.room_number, ', ') WITHIN GROUP (ORDER BY r.room_number) AS room_numbers, " +
            "        SUM(NVL(tr.base_price_per_day, 0) * " +
            "            CASE " +
            "                WHEN bf.checkin_expected_at IS NOT NULL AND bf.checkout_expected_at IS NOT NULL " +
            "                THEN GREATEST(1, CEIL(CAST(bf.checkout_expected_at AS DATE) - CAST(bf.checkin_expected_at AS DATE))) " +
            "                ELSE 1 " +
            "            END) AS total_amount " +
            "    FROM booking bf " +
            "    JOIN booking_room br ON br.booking_id = bf.booking_id " +
            "    JOIN room r ON r.room_id = br.room_id " +
            "    JOIN type_room tr ON tr.type_room_id = r.type_room_id " +
            "    WHERE NVL(tr.base_price_per_day, 0) > 0 " +
            "      AND NOT EXISTS ( " +
            "          SELECT 1 FROM order_details od " +
            "          JOIN orders oo ON oo.order_id = od.order_id " +
            "          WHERE od.booking_room_id = br.booking_room_id " +
            "            AND UPPER(NVL(oo.status, ' ')) NOT IN ('CANCELLED', 'CANCELED') " +
            "      ) " +
            "    GROUP BY br.booking_id, bf.checkin_expected_at, bf.checkout_expected_at " +
            ") fees ON fees.booking_id = b.booking_id " +
            "LEFT JOIN ( " +
            "    SELECT brp_src.booking_id, LISTAGG(brp_src.pet_name, ', ') WITHIN GROUP (ORDER BY brp_src.pet_name) AS pet_names " +
            "    FROM ( " +
            "        SELECT DISTINCT br2.booking_id, p2.pet_name " +
            "        FROM booking_room br2 " +
            "        JOIN booking_room_pet brp2 ON brp2.booking_room_id = br2.booking_room_id " +
            "        JOIN pet p2 ON p2.pet_id = brp2.pet_id " +
            "    ) brp_src " +
            "    GROUP BY brp_src.booking_id " +
            ") pets ON pets.booking_id = b.booking_id " +
            "WHERE b.customer_id = ? " +
            "  AND b.status <> 'CANCELLED' " +
            "  AND fees.total_amount > 0 " +
            "  AND fees.total_amount - NVL(b.deposit_amount, 0) > 0 " +
            "ORDER BY b.checkin_expected_at DESC NULLS LAST, b.booking_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Invoice.InvoiceSource source = new Invoice.InvoiceSource();
                    source.setSourceType("BOOKING");
                    source.setSourceId(rs.getString("booking_id"));
                    source.setBookingId(rs.getString("booking_id"));
                    source.setBranchId(rs.getString("branch_id"));
                    source.setCustomerId(rs.getString("customer_id"));
                    source.setCustomerName(rs.getString("customer_name"));
                    source.setPetName(rs.getString("pet_names"));
                    source.setRoomNumber(rs.getString("room_numbers"));
                    source.setStartDate(rs.getTimestamp("checkin_expected_at"));
                    source.setEndDate(rs.getTimestamp("checkout_expected_at"));
                    source.setStatus(rs.getString("status"));
                    source.setTotalAmount(rs.getDouble("total_amount"));
                    source.setPrepaidAmount(rs.getDouble("prepaid_amount"));
                    sources.add(source);
                }
            }
        }
        return sources;
    }

    public Map<String, Double> findBookingPrepaidAmounts(String customerId) throws SQLException {
        Map<String, Double> prepaidAmounts = new LinkedHashMap<>();
        String sql =
            "SELECT b.booking_id, NVL(b.deposit_amount, 0) AS prepaid_amount " +
            "FROM booking b " +
            "WHERE b.customer_id = ? " +
            "  AND b.status <> 'CANCELLED' " +
            "  AND NOT EXISTS ( " +
            "      SELECT 1 " +
            "      FROM order_details od " +
            "      JOIN booking_room obr ON obr.booking_room_id = od.booking_room_id " +
            "      JOIN orders oo ON oo.order_id = od.order_id " +
            "      WHERE obr.booking_id = b.booking_id " +
            "        AND UPPER(NVL(oo.status, ' ')) NOT IN ('CANCELLED', 'CANCELED') " +
            "  )";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    prepaidAmounts.put(rs.getString("booking_id"), rs.getDouble("prepaid_amount"));
                }
            }
        }
        return prepaidAmounts;
    }

    public List<Invoice.InvoiceSource> findUninvoicedServiceSources(String customerId) throws SQLException {
        List<Invoice.InvoiceSource> sources = new ArrayList<>();
        String sql =
            "SELECT " +
            "    bs.booking_service_id, bs.booking_id, b.branch_id, c.customer_id, " +
            "    c.full_name AS customer_name, c.phone AS customer_phone, " +
            "    ( " +
            "        SELECT LISTAGG(p2.pet_name, ', ') WITHIN GROUP (ORDER BY p2.pet_name) " +
            "        FROM booking_room br2 " +
            "        JOIN booking_room_pet brp2 ON brp2.booking_room_id = br2.booking_room_id " +
            "        JOIN pet p2 ON p2.pet_id = brp2.pet_id " +
            "        WHERE br2.booking_id = b.booking_id " +
            "    ) AS pet_name, " +
            "    s.service_name, bs.scheduled_at, bs.status, NVL(s.base_price, 0) AS total_amount " +
            "FROM booking_services bs " +
            "JOIN booking b ON b.booking_id = bs.booking_id " +
            "JOIN customer c ON c.customer_id = b.customer_id " +
            "JOIN services s ON s.service_id = bs.service_id " +
            "WHERE c.customer_id = ? " +
            "  AND b.status <> 'CANCELLED' " +
            "  AND bs.status <> 'CANCELLED' " +
            "  AND NVL(s.base_price, 0) > 0 " +
            "  AND NOT EXISTS ( " +
            "      SELECT 1 FROM order_details od " +
            "      JOIN orders oo ON oo.order_id = od.order_id " +
            "      WHERE od.booking_service_id = bs.booking_service_id " +
            "        AND UPPER(NVL(oo.status, ' ')) NOT IN ('CANCELLED', 'CANCELED') " +
            "  ) " +
            "ORDER BY bs.scheduled_at DESC NULLS LAST, bs.booking_service_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Invoice.InvoiceSource source = new Invoice.InvoiceSource();
                    source.setSourceType("GROOMING");
                    source.setSourceId(rs.getString("booking_service_id"));
                    source.setBookingId(rs.getString("booking_id"));
                    source.setBranchId(rs.getString("branch_id"));
                    source.setCustomerId(rs.getString("customer_id"));
                    source.setCustomerName(rs.getString("customer_name"));
                    source.setPetName(rs.getString("pet_name"));
                    source.setServiceName(rs.getString("service_name"));
                    source.setScheduledAt(rs.getTimestamp("scheduled_at"));
                    source.setStatus(rs.getString("status"));
                    source.setTotalAmount(rs.getDouble("total_amount"));
                    sources.add(source);
                }
            }
        }
        return sources;
    }

    public List<InvoiceDetail> buildInvoiceDetailsForSource(String sourceType, String sourceId, String orderId)
            throws SQLException {
        if ("BOOKING".equalsIgnoreCase(sourceType)) {
            return buildBookingInvoiceDetails(sourceId, orderId);
        }
        if ("GROOMING".equalsIgnoreCase(sourceType) || "SERVICE".equalsIgnoreCase(sourceType)) {
            return buildServiceInvoiceDetails(sourceId, orderId);
        }
        throw new IllegalArgumentException("Nguồn tạo hóa đơn không hợp lệ.");
    }

    private List<InvoiceDetail> buildBookingInvoiceDetails(String bookingId, String orderId) throws SQLException {
        List<InvoiceDetail> details = new ArrayList<>();
        String sql =
            "SELECT " +
            "    br.booking_room_id, r.room_number, tr.type_name, NVL(tr.base_price_per_day, 0) AS unit_price, " +
            "    CASE " +
            "        WHEN b.checkin_expected_at IS NOT NULL AND b.checkout_expected_at IS NOT NULL " +
            "        THEN GREATEST(1, CEIL(CAST(b.checkout_expected_at AS DATE) - CAST(b.checkin_expected_at AS DATE))) " +
            "        ELSE 1 " +
            "    END AS quantity " +
            "FROM booking b " +
            "JOIN booking_room br ON br.booking_id = b.booking_id " +
            "JOIN room r ON r.room_id = br.room_id " +
            "JOIN type_room tr ON tr.type_room_id = r.type_room_id " +
            "WHERE b.booking_id = ? " +
            "  AND b.status <> 'CANCELLED' " +
            "  AND NVL(tr.base_price_per_day, 0) > 0 " +
            "  AND NOT EXISTS ( " +
            "      SELECT 1 FROM order_details od " +
            "      JOIN orders oo ON oo.order_id = od.order_id " +
            "      WHERE od.booking_room_id = br.booking_room_id " +
            "        AND UPPER(NVL(oo.status, ' ')) NOT IN ('CANCELLED', 'CANCELED') " +
            "  ) " +
            "ORDER BY br.booking_room_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double quantity = rs.getDouble("quantity");
                    double unitPrice = rs.getDouble("unit_price");
                    InvoiceDetail detail = new InvoiceDetail();
                    detail.setOrderId(orderId);
                    detail.setBookingRoomId(rs.getString("booking_room_id"));
                    detail.setNote("Tiền phòng " + rs.getString("room_number") + " - " + rs.getString("type_name"));
                    detail.setQuantity(quantity);
                    detail.setUnitPrice(unitPrice);
                    detail.setLineTotal(quantity * unitPrice);
                    details.add(detail);
                }
            }
        }
        return details;
    }

    private List<InvoiceDetail> buildServiceInvoiceDetails(String bookingServiceId, String orderId) throws SQLException {
        List<InvoiceDetail> details = new ArrayList<>();
        String sql =
            "SELECT bs.booking_service_id, s.service_name, NVL(s.base_price, 0) AS unit_price " +
            "FROM booking_services bs " +
            "JOIN booking b ON b.booking_id = bs.booking_id " +
            "JOIN services s ON s.service_id = bs.service_id " +
            "WHERE bs.booking_service_id = ? " +
            "  AND b.status <> 'CANCELLED' " +
            "  AND bs.status <> 'CANCELLED' " +
            "  AND NVL(s.base_price, 0) > 0 " +
            "  AND NOT EXISTS ( " +
            "      SELECT 1 FROM order_details od " +
            "      JOIN orders oo ON oo.order_id = od.order_id " +
            "      WHERE od.booking_service_id = bs.booking_service_id " +
            "        AND UPPER(NVL(oo.status, ' ')) NOT IN ('CANCELLED', 'CANCELED') " +
            "  )";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingServiceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double unitPrice = rs.getDouble("unit_price");
                    InvoiceDetail detail = new InvoiceDetail();
                    detail.setOrderId(orderId);
                    detail.setBookingServiceId(rs.getString("booking_service_id"));
                    detail.setNote("Dịch vụ grooming: " + valueOrDefault(rs.getString("service_name"), bookingServiceId));
                    detail.setQuantity(1);
                    detail.setUnitPrice(unitPrice);
                    detail.setLineTotal(unitPrice);
                    details.add(detail);
                }
            }
        }
        return details;
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
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
    return createPayment(orderId, paymentMethod, amount, null);
}

public boolean createPayment(String orderId, String paymentMethod, double amount, String note) throws SQLException {
    try (Connection conn = DBConnection.getConnection()) {
        if (note != null && !note.trim().isEmpty() && hasColumn(conn, "PAYMENTS", "NOTE")) {
            String sql =
                "INSERT INTO payments " +
                "(payment_id, order_id, payment_method, amount, status, paid_at, created_at, updated_at, note) " +
                "VALUES (?, ?, ?, ?, 'SUCCESS', SYSTIMESTAMP, SYSTIMESTAMP, SYSTIMESTAMP, ?)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, generateNextPaymentId());
                ps.setString(2, orderId);
                ps.setString(3, paymentMethod);
                ps.setDouble(4, amount);
                ps.setString(5, note);
                return ps.executeUpdate() > 0;
            }
        }

        if (note != null && !note.trim().isEmpty() && hasColumn(conn, "PAYMENTS", "DESCRIPTION")) {
            String sql =
                "INSERT INTO payments " +
                "(payment_id, order_id, payment_method, amount, status, paid_at, created_at, updated_at, description) " +
                "VALUES (?, ?, ?, ?, 'SUCCESS', SYSTIMESTAMP, SYSTIMESTAMP, SYSTIMESTAMP, ?)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, generateNextPaymentId());
                ps.setString(2, orderId);
                ps.setString(3, paymentMethod);
                ps.setDouble(4, amount);
                ps.setString(5, note);
                return ps.executeUpdate() > 0;
            }
        }

    String sql =
        "INSERT INTO payments " +
        "(payment_id, order_id, payment_method, amount, status, paid_at, created_at, updated_at) " +
        "VALUES (?, ?, ?, ?, 'SUCCESS', SYSTIMESTAMP, SYSTIMESTAMP, SYSTIMESTAMP)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, generateNextPaymentId());
        ps.setString(2, orderId);
        ps.setString(3, paymentMethod);
        ps.setDouble(4, amount);
        return ps.executeUpdate() > 0;
    }
    }
}

private boolean hasColumn(Connection conn, String tableName, String columnName) throws SQLException {
    DatabaseMetaData metaData = conn.getMetaData();
    try (ResultSet rs = metaData.getColumns(null, null, tableName.toUpperCase(), columnName.toUpperCase())) {
        if (rs.next()) {
            return true;
        }
    }
    try (ResultSet rs = metaData.getColumns(null, metaData.getUserName(), tableName.toUpperCase(), columnName.toUpperCase())) {
        return rs.next();
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
        "ORDER BY " +
        "  CASE UPPER(NVL(p.status, ' ')) " +
        "    WHEN 'PENDING' THEN 1 " +
        "    WHEN 'SUCCESS' THEN 2 " +
        "    WHEN 'PAID' THEN 2 " +
        "    WHEN 'COMPLETED' THEN 2 " +
        "    WHEN 'REFUNDED' THEN 3 " +
        "    WHEN 'FAILED' THEN 4 " +
        "    WHEN 'CANCELLED' THEN 4 " +
        "    WHEN 'CANCELED' THEN 4 " +
        "    ELSE 5 " +
        "  END, " +
        "  p.paid_at DESC, p.payment_id DESC";

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
        "AND status = 'PENDING'";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, orderId);
        return ps.executeUpdate() > 0;
    }
}

public List<Invoice> getBranchInvoices(String branchId) throws SQLException {
    List<Invoice> list = new ArrayList<>();
    String sql = "SELECT order_id AS invoice_id, customer_id, grand_total AS total_amount, created_at, status, branch_id " +
                 "FROM orders " +
                 "WHERE branch_id = ? AND status != 'CANCELLED'";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, branchId);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Invoice inv = new Invoice();
                inv.setId(rs.getString("invoice_id"));
                inv.setCustomerId(rs.getString("customer_id"));
                inv.setTotalAmount(rs.getDouble("total_amount"));
                inv.setCreateDate(rs.getTimestamp("created_at"));
                inv.setStatus(rs.getString("status"));
                inv.setBranchId(rs.getString("branch_id"));
                list.add(inv);
            }
        }
    }
    return list;
}
}
