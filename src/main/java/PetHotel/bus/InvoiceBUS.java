package PetHotel.bus;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import PetHotel.dao.InvoiceDAO;
import PetHotel.model.Invoice;
import PetHotel.model.InvoiceDetail;

public class InvoiceBUS {

    private final InvoiceDAO invoiceDAO;

    public InvoiceBUS() {
        this.invoiceDAO = new InvoiceDAO();
    }

    public List<Invoice> searchInvoices(String invoiceId, String customerId, Date fromDate, Date toDate) throws SQLException {
        if (fromDate != null && toDate != null && fromDate.after(toDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu không được sau ngày kết thúc");
        }

        return invoiceDAO.searchInvoices(invoiceId, customerId, fromDate, toDate);
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
    public List<InvoiceDetail> getInvoiceDetailsByOrderId(String orderId) throws SQLException {
    if (orderId == null || orderId.trim().isEmpty()) {
        throw new IllegalArgumentException("Mã hóa đơn không được rỗng");
    }

    return invoiceDAO.getInvoiceDetailsByOrderId(orderId);
}
}