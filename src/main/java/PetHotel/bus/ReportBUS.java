package PetHotel.bus;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import PetHotel.dao.ReportDAO;
import PetHotel.model.BookingReport;
import PetHotel.model.ChainReport;
import PetHotel.model.InventoryReport;
import PetHotel.model.RevenueReport;
import PetHotel.model.RoomUsageReport;
import PetHotel.util.Role;

public class ReportBUS {
    private final ReportDAO reportDAO = new ReportDAO();

    public Map<String, Number> getDashboardSummary() throws SQLException {
        return reportDAO.getDashboardSummary();
    }

    public List<RevenueReport> getRevenueReport(String type, Date fromDate, Date toDate) throws SQLException {
        validateDateRange(fromDate, toDate);
        type = normalizeType(type);
        return reportDAO.getRevenueReport(type.trim(), fromDate, toDate);
    }

    public List<BookingReport> getBookingReport(String type, Date fromDate, Date toDate) throws SQLException {
        validateDateRange(fromDate, toDate);
        return reportDAO.getBookingReport(normalizeType(type), fromDate, toDate);
    }

    public List<RoomUsageReport> getRoomUsageReport(Date fromDate, Date toDate) throws SQLException {
        validateDateRange(fromDate, toDate);
        return reportDAO.getRoomUsageReport(fromDate, toDate);
    }

    public List<InventoryReport> getInventoryReport() throws SQLException {
        return reportDAO.getInventoryReport();
    }

    public List<ChainReport> getChainReport(String type, Date fromDate, Date toDate, Role role) throws SQLException {
        validateDateRange(fromDate, toDate);
        if (role != Role.CEO && role != Role.ADMIN) {
            throw new IllegalArgumentException("Bạn không có quyền xem báo cáo toàn chuỗi.");
        }
        return reportDAO.getChainReport(normalizeType(type), fromDate, toDate);
    }

    private void validateDateRange(Date fromDate, Date toDate) {
        if (fromDate != null && toDate != null && fromDate.after(toDate)) {
            throw new IllegalArgumentException("Từ ngày không được lớn hơn đến ngày.");
        }
    }

    private String normalizeType(String type) {
        return type == null || type.trim().isEmpty() ? "Theo tháng" : type.trim();
    }
}
