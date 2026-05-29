package PetHotel.model;

import java.util.Date;

public class Invoice {
    private String id;              // map với order_id
    private String customerId;      // map với customer_id
    private String customerName;
    private String customerPhone;
    private String branchId;        // map với branch_id
    private String bookingId;       // map với booking_id
    private String createdByEmp;    // map với created_by_emp
    private String createdByEmpName;
    private Date createDate;        // map với created_at
    private double subtotal;        // map với subtotal
    private double totalAmount;     // map với grand_total
    private double prepaidAmount;
    private double paidAmount;
    private double remainingAmount;
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

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
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

    public String getCreatedByEmpName() {
        return createdByEmpName;
    }

    public void setCreatedByEmpName(String createdByEmpName) {
        this.createdByEmpName = createdByEmpName;
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

    public double getPrepaidAmount() {
        return prepaidAmount;
    }

    public void setPrepaidAmount(double prepaidAmount) {
        this.prepaidAmount = prepaidAmount;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public double getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(double remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static class InvoiceSource {
        private String sourceType;
        private String sourceId;
        private String bookingId;
        private String branchId;
        private String customerId;
        private String customerName;
        private String petName;
        private String roomNumber;
        private String serviceName;
        private Date startDate;
        private Date endDate;
        private Date scheduledAt;
        private String status;
        private double totalAmount;
        private double prepaidAmount;

        public String getSourceType() { return sourceType; }
        public void setSourceType(String sourceType) { this.sourceType = sourceType; }

        public String getSourceId() { return sourceId; }
        public void setSourceId(String sourceId) { this.sourceId = sourceId; }

        public String getBookingId() { return bookingId; }
        public void setBookingId(String bookingId) { this.bookingId = bookingId; }

        public String getBranchId() { return branchId; }
        public void setBranchId(String branchId) { this.branchId = branchId; }

        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }

        public String getPetName() { return petName; }
        public void setPetName(String petName) { this.petName = petName; }

        public String getRoomNumber() { return roomNumber; }
        public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }

        public Date getStartDate() { return startDate; }
        public void setStartDate(Date startDate) { this.startDate = startDate; }

        public Date getEndDate() { return endDate; }
        public void setEndDate(Date endDate) { this.endDate = endDate; }

        public Date getScheduledAt() { return scheduledAt; }
        public void setScheduledAt(Date scheduledAt) { this.scheduledAt = scheduledAt; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

        public double getPrepaidAmount() { return prepaidAmount; }
        public void setPrepaidAmount(double prepaidAmount) { this.prepaidAmount = prepaidAmount; }
    }
}
