package PetHotel.model;

import java.util.Date;

public class Invoice {
    private String id;              // map với order_id
    private String customerId;      // map với customer_id
    private String customerName;
    private String branchId;        // map với branch_id
    private String bookingId;       // map với booking_id
    private String createdByEmp;    // map với created_by_emp
    private Date createDate;        // map với created_at
    private double subtotal;        // map với subtotal
    private double totalAmount;     // map với grand_total
    private String status;          // PENDING, PAID, PARTIAL...

    public Invoice() {
    }

    public Invoice(String id, String customerId, String customerName,
                   String branchId, String bookingId, String createdByEmp,
                   Date createDate, double subtotal, double totalAmount, String status) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.branchId = branchId;
        this.bookingId = bookingId;
        this.createdByEmp = createdByEmp;
        this.createDate = createDate;
        this.subtotal = subtotal;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return id;
    }

    public void setOrderId(String orderId) {
        this.id = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getCreatedByEmp() {
        return createdByEmp;
    }

    public void setCreatedByEmp(String createdByEmp) {
        this.createdByEmp = createdByEmp;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getCreatedDate() {
        return createDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createDate = createdDate;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getGrandTotal() {
        return totalAmount;
    }

    public void setGrandTotal(double grandTotal) {
        this.totalAmount = grandTotal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
