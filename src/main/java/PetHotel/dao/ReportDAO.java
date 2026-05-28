package PetHotel.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import PetHotel.model.BookingReport;
import PetHotel.model.ChainReport;
import PetHotel.model.InventoryReport;
import PetHotel.model.RevenueReport;
import PetHotel.model.RoomUsageReport;
import PetHotel.util.DBConnection;

public class ReportDAO {

    public Map<String, Number> getDashboardSummary() throws SQLException {
        Map<String, Number> summary = new LinkedHashMap<>();
        summary.put("todayRevenue", queryDouble(
            "SELECT NVL(SUM(amount), 0) AS value " +
            "FROM payments " +
            "WHERE status = 'SUCCESS' " +
            "AND TRUNC(CAST(paid_at AS DATE)) = TRUNC(SYSDATE)"
        ));
        summary.put("todayBooking", queryInt(
            "SELECT COUNT(*) AS value " +
            "FROM booking " +
            "WHERE TRUNC(CAST(created_at AS DATE)) = TRUNC(SYSDATE)"
        ));
        summary.put("roomInUse", queryInt(
            "SELECT COUNT(*) AS value FROM room WHERE status = 'IN_USE'"
        ));
        summary.put("roomAvailable", queryInt(
            "SELECT COUNT(*) AS value FROM room WHERE status = 'AVAILABLE'"
        ));
        summary.put("roomMaintenance", queryInt(
            "SELECT COUNT(*) AS value FROM room WHERE status = 'MAINTENANCE'"
        ));
        summary.put("roomTotal", queryInt(
            "SELECT COUNT(*) AS value FROM room"
        ));
        summary.put("lowStock", queryInt(
            "SELECT COUNT(*) AS value " +
            "FROM branch_inventory " +
            "WHERE reorder_point IS NOT NULL " +
            "AND quantity_in_stock <= reorder_point"
        ));
        summary.put("groomingPending", queryInt(
            "SELECT COUNT(*) AS value " +
            "FROM booking_services " +
            "WHERE status IN ('PENDING', 'SCHEDULED') " +
            "AND scheduled_at IS NOT NULL " +
            "AND TRUNC(CAST(scheduled_at AS DATE)) = TRUNC(SYSDATE)"
        ));
        return summary;
    }

    public List<RevenueReport> getRevenueReport(String type, java.util.Date fromDate, java.util.Date toDate)
            throws SQLException {
        List<RevenueReport> reports = new ArrayList<>();
        String periodExpression = resolvePeriodExpression(type);

        String sql =
            "SELECT period_label, " +
            "       COUNT(order_id) AS invoice_count, " +
            "       NVL(SUM(period_paid), 0) AS total_revenue, " +
            "       NVL(SUM(period_paid), 0) AS total_paid, " +
            "       NVL(SUM(GREATEST(grand_total - order_paid, 0)), 0) AS remaining " +
            "FROM ( " +
            "    SELECT p.order_id, " + periodExpression + " AS period_label, " +
            "           MIN(CAST(p.paid_at AS DATE)) AS first_paid_at, " +
            "           SUM(p.amount) AS period_paid, " +
            "           MAX(o.grand_total) AS grand_total, " +
            "           MAX(paid_summary.total_paid) AS order_paid " +
            "    FROM payments p " +
            "    JOIN orders o ON p.order_id = o.order_id " +
            "    JOIN ( " +
            "        SELECT order_id, SUM(amount) AS total_paid " +
            "        FROM payments " +
            "        WHERE status = 'SUCCESS' " +
            "        GROUP BY order_id " +
            "    ) paid_summary ON paid_summary.order_id = p.order_id " +
            "    WHERE p.status = 'SUCCESS' " +
            "      AND (? IS NULL OR TRUNC(CAST(p.paid_at AS DATE)) >= ?) " +
            "      AND (? IS NULL OR TRUNC(CAST(p.paid_at AS DATE)) <= ?) " +
            "    GROUP BY p.order_id, " + periodExpression + " " +
            ") " +
            "GROUP BY period_label " +
            "ORDER BY MIN(first_paid_at)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            Date fromSqlDate = fromDate == null ? null : new Date(fromDate.getTime());
            Date toSqlDate = toDate == null ? null : new Date(toDate.getTime());

            ps.setDate(1, fromSqlDate);
            ps.setDate(2, fromSqlDate);
            ps.setDate(3, toSqlDate);
            ps.setDate(4, toSqlDate);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RevenueReport report = new RevenueReport();
                    report.setPeriod(rs.getString("period_label"));
                    report.setInvoiceCount(rs.getInt("invoice_count"));
                    report.setTotalRevenue(rs.getDouble("total_revenue"));
                    report.setTotalPaid(rs.getDouble("total_paid"));
                    report.setRemaining(rs.getDouble("remaining"));
                    reports.add(report);
                }
            }
        }

        return reports;
    }

    public List<BookingReport> getBookingReport(String type, java.util.Date fromDate, java.util.Date toDate)
            throws SQLException {
        List<BookingReport> reports = new ArrayList<>();
        String periodExpression = resolvePeriodExpression(type, "b.created_at");
        String sql =
            "SELECT " + periodExpression + " AS period_label, " +
            "       COUNT(*) AS booking_count, " +
            "       SUM(CASE WHEN b.status IN ('PENDING', 'CONFIRMED') THEN 1 ELSE 0 END) AS new_count, " +
            "       SUM(CASE WHEN b.status = 'CHECKED_OUT' THEN 1 ELSE 0 END) AS completed_count, " +
            "       SUM(CASE WHEN b.status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled_count " +
            "FROM booking b " +
            "WHERE (? IS NULL OR TRUNC(CAST(b.created_at AS DATE)) >= ?) " +
            "  AND (? IS NULL OR TRUNC(CAST(b.created_at AS DATE)) <= ?) " +
            "GROUP BY " + periodExpression + " " +
            "ORDER BY MIN(CAST(b.created_at AS DATE))";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindDateRange(ps, fromDate, toDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookingReport report = new BookingReport();
                    report.setPeriod(rs.getString("period_label"));
                    report.setBookingCount(rs.getInt("booking_count"));
                    report.setNewBookingCount(rs.getInt("new_count"));
                    report.setCompletedBookingCount(rs.getInt("completed_count"));
                    report.setCancelledBookingCount(rs.getInt("cancelled_count"));
                    reports.add(report);
                }
            }
        }
        return reports;
    }

    public List<RoomUsageReport> getRoomUsageReport(java.util.Date fromDate, java.util.Date toDate)
            throws SQLException {
        List<RoomUsageReport> reports = new ArrayList<>();
        String sql =
            "SELECT COUNT(*) AS total_room, " +
            "       SUM(CASE WHEN status = 'IN_USE' THEN 1 ELSE 0 END) AS in_use_room, " +
            "       SUM(CASE WHEN status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available_room " +
            "FROM room";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int total = rs.getInt("total_room");
                int inUse = rs.getInt("in_use_room");
                RoomUsageReport report = new RoomUsageReport();
                report.setPeriod("Hiện tại");
                report.setTotalRoom(total);
                report.setInUseRoom(inUse);
                report.setAvailableRoom(rs.getInt("available_room"));
                report.setUsageRate(total == 0 ? 0 : inUse * 100.0 / total);
                reports.add(report);
            }
        }
        return reports;
    }

    public List<InventoryReport> getInventoryReport() throws SQLException {
        List<InventoryReport> reports = new ArrayList<>();
        String sql =
            "SELECT b.branch_id, b.branch_name, " +
            "       COUNT(bi.product_id) AS total_sku, " +
            "       NVL(SUM(bi.quantity_in_stock), 0) AS total_stock, " +
            "       SUM(CASE WHEN bi.reorder_point IS NOT NULL AND bi.quantity_in_stock <= bi.reorder_point THEN 1 ELSE 0 END) AS low_stock_count, " +
            "       SUM(CASE WHEN bi.quantity_in_stock = 0 THEN 1 ELSE 0 END) AS out_of_stock_count " +
            "FROM branch b " +
            "LEFT JOIN branch_inventory bi ON b.branch_id = bi.branch_id " +
            "GROUP BY b.branch_id, b.branch_name " +
            "ORDER BY b.branch_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                InventoryReport report = new InventoryReport();
                report.setScope(rs.getString("branch_id") + " - " + rs.getString("branch_name"));
                report.setTotalSku(rs.getInt("total_sku"));
                report.setTotalStock(rs.getDouble("total_stock"));
                report.setLowStockCount(rs.getInt("low_stock_count"));
                report.setOutOfStockCount(rs.getInt("out_of_stock_count"));
                reports.add(report);
            }
        }
        return reports;
    }

    public List<ChainReport> getChainReport(String type, java.util.Date fromDate, java.util.Date toDate)
            throws SQLException {
        List<ChainReport> reports = new ArrayList<>();
        String sql =
            "SELECT br.branch_id, br.branch_name, " +
            "       NVL(pay.total_revenue, 0) AS total_revenue, " +
            "       NVL(bk.booking_count, 0) AS booking_count, " +
            "       NVL(r.room_in_use, 0) AS room_in_use " +
            "FROM branch br " +
            "LEFT JOIN ( " +
            "    SELECT o.branch_id, SUM(p.amount) AS total_revenue " +
            "    FROM payments p JOIN orders o ON p.order_id = o.order_id " +
            "    WHERE p.status = 'SUCCESS' " +
            "      AND (? IS NULL OR TRUNC(CAST(p.paid_at AS DATE)) >= ?) " +
            "      AND (? IS NULL OR TRUNC(CAST(p.paid_at AS DATE)) <= ?) " +
            "    GROUP BY o.branch_id " +
            ") pay ON pay.branch_id = br.branch_id " +
            "LEFT JOIN ( " +
            "    SELECT branch_id, COUNT(*) AS booking_count " +
            "    FROM booking " +
            "    WHERE (? IS NULL OR TRUNC(CAST(created_at AS DATE)) >= ?) " +
            "      AND (? IS NULL OR TRUNC(CAST(created_at AS DATE)) <= ?) " +
            "    GROUP BY branch_id " +
            ") bk ON bk.branch_id = br.branch_id " +
            "LEFT JOIN ( " +
            "    SELECT branch_id, COUNT(*) AS room_in_use " +
            "    FROM room " +
            "    WHERE status = 'IN_USE' " +
            "    GROUP BY branch_id " +
            ") r ON r.branch_id = br.branch_id " +
            "ORDER BY br.branch_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindDateRange(ps, fromDate, toDate);
            bindDateRange(ps, fromDate, toDate, 5);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChainReport report = new ChainReport();
                    report.setBranchId(rs.getString("branch_id"));
                    report.setBranchName(rs.getString("branch_name"));
                    report.setTotalRevenue(rs.getDouble("total_revenue"));
                    report.setBookingCount(rs.getInt("booking_count"));
                    report.setRoomInUse(rs.getInt("room_in_use"));
                    reports.add(report);
                }
            }
        }
        return reports;
    }

    private String resolvePeriodExpression(String type) {
        return resolvePeriodExpression(type, "p.paid_at");
    }

    private String resolvePeriodExpression(String type, String dateColumn) {
        if ("Theo tuần".equalsIgnoreCase(type)) {
            return "TO_CHAR(CAST(" + dateColumn + " AS DATE), 'IYYY-IW')";
        }
        if ("Theo tháng".equalsIgnoreCase(type)) {
            return "TO_CHAR(CAST(" + dateColumn + " AS DATE), 'YYYY-MM')";
        }
        return "TO_CHAR(CAST(" + dateColumn + " AS DATE), 'YYYY-MM-DD')";
    }

    private void bindDateRange(PreparedStatement ps, java.util.Date fromDate, java.util.Date toDate)
            throws SQLException {
        bindDateRange(ps, fromDate, toDate, 1);
    }

    private void bindDateRange(PreparedStatement ps, java.util.Date fromDate, java.util.Date toDate, int startIndex)
            throws SQLException {
        Date fromSqlDate = fromDate == null ? null : new Date(fromDate.getTime());
        Date toSqlDate = toDate == null ? null : new Date(toDate.getTime());
        ps.setDate(startIndex, fromSqlDate);
        ps.setDate(startIndex + 1, fromSqlDate);
        ps.setDate(startIndex + 2, toSqlDate);
        ps.setDate(startIndex + 3, toSqlDate);
    }

    private int queryInt(String sql) throws SQLException {
        return (int) queryDouble(sql);
    }

    private double queryDouble(String sql) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("value");
            }
        }
        return 0;
    }
}
