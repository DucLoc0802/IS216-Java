package PetHotel.model;

public class InvoiceDetail {
    private String detailId;
    private String orderId;
    private String bookingRoomId;
    private String bookingServiceId;
    private String note;
    private double quantity;
    private double unitPrice;
    private double lineTotal;

    public InvoiceDetail() {
    }

    public InvoiceDetail(String detailId, String orderId, String bookingRoomId,
                         String bookingServiceId, String note,
                         double quantity, double unitPrice, double lineTotal) {
        this.detailId = detailId;
        this.orderId = orderId;
        this.bookingRoomId = bookingRoomId;
        this.bookingServiceId = bookingServiceId;
        this.note = note;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
    }

    public String getDetailId() {
        return detailId;
    }

    public void setDetailId(String detailId) {
        this.detailId = detailId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getBookingRoomId() {
        return bookingRoomId;
    }

    public void setBookingRoomId(String bookingRoomId) {
        this.bookingRoomId = bookingRoomId;
    }

    public String getBookingServiceId() {
        return bookingServiceId;
    }

    public void setBookingServiceId(String bookingServiceId) {
        this.bookingServiceId = bookingServiceId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(double lineTotal) {
        this.lineTotal = lineTotal;
    }
}