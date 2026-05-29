package PetHotel.bus;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import PetHotel.dao.InvoiceDAO;
import PetHotel.model.Customer;
import PetHotel.model.Invoice;
import PetHotel.model.InvoiceDetail;
import PetHotel.model.Payment;

public class InvoiceBUS {

    private final InvoiceDAO invoiceDAO;

    public InvoiceBUS() {
        this.invoiceDAO = new InvoiceDAO();
    }

    public List<Invoice> searchInvoices(String invoiceId, String customerId, Date fromDate, Date toDate) throws SQLException {
        return searchInvoices(invoiceId, customerId, fromDate, toDate, null);
    }

    public List<Invoice> searchInvoices(String invoiceId, String customerId, Date fromDate, Date toDate, String status) throws SQLException {
        if (fromDate != null && toDate != null && fromDate.after(toDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu không được sau ngày kết thúc");
        }

        return invoiceDAO.searchInvoices(invoiceId, customerId, fromDate, toDate, status);
    }

    public boolean createInvoice(Invoice invoice) throws SQLException {
        if (invoice == null) {
            throw new IllegalArgumentException("Dữ liệu hóa đơn không được rỗng");
        }

        if (invoice.getId() == null || invoice.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã hóa đơn không được rỗng");
        }

        if (invoice.getCustomerId() == null || invoice.getCustomerId().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khách hàng không được rỗng");
        }

        if (invoice.getBranchId() == null || invoice.getBranchId().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã chi nhánh không được rỗng");
        }

        if (invoice.getCreatedByEmp() == null || invoice.getCreatedByEmp().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên tạo không được rỗng");
        }

        if (invoice.getSubtotal() < 0 || invoice.getTotalAmount() < 0) {
            throw new IllegalArgumentException("Số tiền không được âm");
        }

        if (invoice.getStatus() == null || invoice.getStatus().trim().isEmpty()) {
            invoice.setStatus("PENDING");
        }

        return invoiceDAO.createInvoice(invoice);
    }

    public boolean createInvoice(Invoice invoice, List<InvoiceDetail> details) throws SQLException {
        validateInvoice(invoice);

        if (details != null) {
            double detailTotal = 0;
            for (InvoiceDetail detail : details) {
                if (detail == null) {
                    throw new IllegalArgumentException("Chi tiết hóa đơn không hợp lệ");
                }
                if (detail.getOrderId() == null || detail.getOrderId().trim().isEmpty()) {
                    detail.setOrderId(invoice.getId());
                }
                if (detail.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Số lượng chi tiết hóa đơn phải lớn hơn 0");
                }
                if (detail.getUnitPrice() < 0 || detail.getLineTotal() < 0) {
                    throw new IllegalArgumentException("Số tiền chi tiết hóa đơn không được âm");
                }
                detailTotal += detail.getLineTotal();
            }

            if (!details.isEmpty()) {
                invoice.setSubtotal(detailTotal);
                invoice.setTotalAmount(detailTotal);
            }
        }

        return invoiceDAO.createInvoice(invoice, details);
    }

    public String generateNextOrderId() throws SQLException {
        return invoiceDAO.generateNextOrderId();
    }

    public Map<String, Invoice> findBookingOptions() throws SQLException {
        return invoiceDAO.findBookingOptions();
    }

    public Map<String, Double> findBookingFees() throws SQLException {
        return invoiceDAO.findBookingFees();
    }

    public List<Customer> searchInvoiceCustomers(String keyword) throws SQLException {
        return invoiceDAO.searchInvoiceCustomers(keyword);
    }

    public List<Invoice.InvoiceSource> findUninvoicedBookingSources(String customerId) throws SQLException {
        requireCustomerId(customerId);
        return invoiceDAO.findUninvoicedBookingSources(customerId.trim());
    }

    public Map<String, Double> findBookingPrepaidAmounts(String customerId) throws SQLException {
        requireCustomerId(customerId);
        return invoiceDAO.findBookingPrepaidAmounts(customerId.trim());
    }

    public List<Invoice.InvoiceSource> findUninvoicedServiceSources(String customerId) throws SQLException {
        requireCustomerId(customerId);
        return invoiceDAO.findUninvoicedServiceSources(customerId.trim());
    }

    public List<InvoiceDetail> buildInvoiceDetailsForSource(String sourceType, String sourceId, String orderId)
            throws SQLException {
        if (sourceType == null || sourceType.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn loại nguồn tạo hóa đơn.");
        }
        if (sourceId == null || sourceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn booking hoặc grooming cần tạo hóa đơn.");
        }
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã hóa đơn không được rỗng.");
        }

        List<InvoiceDetail> details = invoiceDAO.buildInvoiceDetailsForSource(
            sourceType.trim(),
            sourceId.trim(),
            orderId.trim()
        );
        if (details == null || details.isEmpty()) {
            if ("BOOKING".equalsIgnoreCase(sourceType)) {
                throw new IllegalArgumentException("Booking này không còn khoản phí cần tạo hóa đơn.");
            }
            if ("GROOMING".equalsIgnoreCase(sourceType) || "SERVICE".equalsIgnoreCase(sourceType)) {
                throw new IllegalArgumentException("Dịch vụ này đã có hóa đơn hoặc không còn khoản phí hợp lệ.");
            }
            throw new IllegalArgumentException("Nguồn này đã có hóa đơn hoặc không còn khoản phí hợp lệ.");
        }
        return details;
    }

    public void payInvoice(Invoice invoice, String paymentMethod, double amount) throws SQLException {
        if (invoice == null || invoice.getId() == null || invoice.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn hóa đơn cần thanh toán");
        }

        syncPaymentStatus(invoice);

        String status = invoice.getStatus();
        if (!isPayable(status)) {
            throw new IllegalArgumentException("Không thể thanh toán hóa đơn có trạng thái " + status);
        }

        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn phương thức thanh toán");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0");
        }

        double paid = invoiceDAO.getTotalPaidByOrderId(invoice.getId());
        double remaining = invoice.getTotalAmount() - paid;
        if (remaining <= 0.01) {
            invoiceDAO.updateOrderStatus(invoice.getId(), "PAID");
            invoice.setStatus("PAID");
            throw new IllegalArgumentException("Hóa đơn này đã thanh toán đủ.");
        }
        if (amount - remaining > 0.01) {
            throw new IllegalArgumentException("Số tiền thanh toán không được vượt quá số tiền còn lại");
        }

        invoiceDAO.createPayment(invoice.getId(), paymentMethod.trim(), amount);
        syncPaymentStatus(invoice);
    }

    public double getTotalPaidByOrderId(String orderId) throws SQLException {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã hóa đơn không được rỗng");
        }
        return invoiceDAO.getTotalPaidByOrderId(orderId);
    }

    public double getRemainingAmount(Invoice invoice) throws SQLException {
        if (invoice == null || invoice.getId() == null || invoice.getId().trim().isEmpty()) {
            return 0;
        }
        return invoice.getTotalAmount() - invoiceDAO.getTotalPaidByOrderId(invoice.getId());
    }

    public String syncPaymentStatus(Invoice invoice) throws SQLException {
        if (invoice == null || invoice.getId() == null || invoice.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã hóa đơn không được rỗng");
        }

        String currentStatus = invoice.getStatus();
        if ("CANCELLED".equalsIgnoreCase(currentStatus) || "REFUNDED".equalsIgnoreCase(currentStatus)) {
            return currentStatus;
        }

        double totalPaid = invoiceDAO.getTotalPaidByOrderId(invoice.getId());
        String newStatus;
        if (totalPaid + 0.01 >= invoice.getTotalAmount()) {
            newStatus = "PAID";
        } else if (totalPaid > 0) {
            newStatus = "PARTIAL";
        } else {
            newStatus = "PENDING";
        }

        if (!newStatus.equalsIgnoreCase(currentStatus)) {
            invoiceDAO.updateOrderStatus(invoice.getId(), newStatus);
            invoice.setStatus(newStatus);
        }
        return newStatus;
    }

    public boolean cancelInvoice(Invoice invoice) throws SQLException {
        if (invoice == null) {
            throw new IllegalArgumentException("Vui lòng chọn hóa đơn cần hủy");
        }
        return cancelInvoice(invoice.getId(), null);
    }

    public boolean cancelInvoice(String orderId) throws SQLException {
        return cancelInvoice(orderId, null);
    }

    public boolean cancelInvoice(String orderId, String reason) throws SQLException {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã hóa đơn không được rỗng");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do hủy hóa đơn.");
        }

        Invoice invoice = invoiceDAO.getInvoiceById(orderId.trim());
        if (invoice == null) {
            throw new IllegalArgumentException("Không tìm thấy hóa đơn " + orderId);
        }

        String status = invoice.getStatus();
        if ("PAID".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("Hóa đơn đã thanh toán, không thể hủy.");
        }
        if ("PARTIAL".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("Hóa đơn đã thanh toán một phần, không thể hủy trực tiếp. Vui lòng thanh toán tiếp hoặc liên hệ quản lý.");
        }
        if ("CANCELLED".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("Hóa đơn này đã bị hủy.");
        }
        if ("REFUNDED".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("Không thể hủy hóa đơn đã hoàn tiền.");
        }
        if (!isCancelable(status)) {
            throw new IllegalArgumentException("Không thể hủy hóa đơn có trạng thái " + status);
        }

        boolean cancelled = invoiceDAO.cancelInvoice(orderId.trim());
        if (!cancelled) {
            throw new IllegalArgumentException("Không thể hủy hóa đơn. Hóa đơn có thể đã đổi trạng thái.");
        }
        return true;
    }

    public Invoice getInvoiceById(String orderId) throws SQLException {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã hóa đơn không được rỗng");
        }
        return invoiceDAO.getInvoiceById(orderId.trim());
    }

    public List<InvoiceDetail> getInvoiceDetailsByOrderId(String orderId) throws SQLException {
    if (orderId == null || orderId.trim().isEmpty()) {
        throw new IllegalArgumentException("Mã hóa đơn không được rỗng");
    }

    return invoiceDAO.getInvoiceDetailsByOrderId(orderId);
}

    public List<Payment> getPaymentsByOrderId(String orderId) throws SQLException {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã hóa đơn không được rỗng");
        }
        return invoiceDAO.getPaymentsByOrderId(orderId);
    }

    public List<Payment> searchPaymentHistory(String keyword, String method, String status, Date fromDate, Date toDate)
            throws SQLException {
        if (fromDate != null && toDate != null && fromDate.after(toDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu không được sau ngày kết thúc");
        }
        return invoiceDAO.searchPaymentHistory(keyword, method, status, fromDate, toDate);
    }

    private void validateInvoice(Invoice invoice) {
        if (invoice == null) {
            throw new IllegalArgumentException("Dữ liệu hóa đơn không được rỗng");
        }

        if (invoice.getId() == null || invoice.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã hóa đơn không được rỗng");
        }

        if (invoice.getCustomerId() == null || invoice.getCustomerId().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khách hàng không được rỗng");
        }

        if (invoice.getBranchId() == null || invoice.getBranchId().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã chi nhánh không được rỗng");
        }

        if (invoice.getBookingId() == null || invoice.getBookingId().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã booking không được rỗng");
        }

        if (invoice.getCreatedByEmp() == null || invoice.getCreatedByEmp().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên tạo không được rỗng");
        }

        if (invoice.getSubtotal() < 0 || invoice.getTotalAmount() < 0) {
            throw new IllegalArgumentException("Số tiền không được âm");
        }

        if (invoice.getStatus() == null || invoice.getStatus().trim().isEmpty()) {
            invoice.setStatus("PENDING");
        }
    }

    private void requireCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn khách hàng.");
        }
    }

    private boolean isPayable(String status) {
        return "PENDING".equalsIgnoreCase(status) || "PARTIAL".equalsIgnoreCase(status);
    }

    private boolean isCancelable(String status) {
        return "PENDING".equalsIgnoreCase(status);
    }

    public List<Invoice> getBranchInvoices(String branchId) {
        try {
            return invoiceDAO.getBranchInvoices(branchId);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi lấy danh sách hóa đơn chi nhánh.", e);
        }
    }
}
