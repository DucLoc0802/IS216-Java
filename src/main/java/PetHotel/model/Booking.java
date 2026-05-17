package PetHotel.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Booking — Ánh xạ bảng BOOKING.
 *
 * Schema:
 *   booking_id             VARCHAR2(10)    PK
 *   customer_id            VARCHAR2(10)    NOT NULL, FK → customer
 *   branch_id              VARCHAR2(10)    NOT NULL, FK → branch
 *   checkin_expected_at    TIMESTAMP(6) WITH TIME ZONE
 *   checkout_expected_at   TIMESTAMP(6) WITH TIME ZONE
 *   status                 NVARCHAR2(20)   CHECK IN ('PENDING','CONFIRMED','CHECKED_IN','CHECKED_OUT','CANCELLED')
 *   deposit_amount         NUMBER(12,2)    >= 0
 *   special_note           CLOB
 *   created_at             TIMESTAMP(6) WITH TIME ZONE
 *   updated_at             TIMESTAMP(6) WITH TIME ZONE
 *
 * Ràng buộc thời gian: checkout_expected_at > checkin_expected_at (nếu cả hai đều có giá trị).
 */
public class Booking {

    /** Các giá trị hợp lệ của trường status */
    public static final String STATUS_PENDING    = "PENDING";
    public static final String STATUS_CONFIRMED  = "CONFIRMED";
    public static final String STATUS_CHECKED_IN = "CHECKED_IN";
    public static final String STATUS_CHECKED_OUT= "CHECKED_OUT";
    public static final String STATUS_CANCELLED  = "CANCELLED";

    private String bookingId;
    private String customerId;
    private String branchId;
    private OffsetDateTime checkinExpectedAt;
    private OffsetDateTime checkoutExpectedAt;

    /**
     * Trạng thái booking.
     * CHECK IN ('PENDING','CONFIRMED','CHECKED_IN','CHECKED_OUT','CANCELLED')
     */
    private String status;

    /** Số tiền đặt cọc, có thể null */
    private BigDecimal depositAmount;

    /** Ghi chú đặc biệt (CLOB) */
    private String specialNote;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // ── Constructors ──────────────────────────────────────────────

    public Booking() {}

    /** Constructor đầy đủ cho DAO mapping */
    public Booking(String bookingId, String customerId, String branchId,
                   OffsetDateTime checkinExpectedAt, OffsetDateTime checkoutExpectedAt,
                   String status, BigDecimal depositAmount, String specialNote,
                   OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.bookingId          = bookingId;
        this.customerId         = customerId;
        this.branchId           = branchId;
        this.checkinExpectedAt  = checkinExpectedAt;
        this.checkoutExpectedAt = checkoutExpectedAt;
        this.status             = status;
        this.depositAmount      = depositAmount;
        this.specialNote        = specialNote;
        this.createdAt          = createdAt;
        this.updatedAt          = updatedAt;
    }

    // ── Getters / Setters ─────────────────────────────────────────

    public String getBookingId()                        { return bookingId; }
    public void setBookingId(String v)                  { this.bookingId = v; }

    public String getCustomerId()                       { return customerId; }
    public void setCustomerId(String v)                 { this.customerId = v; }

    public String getBranchId()                         { return branchId; }
    public void setBranchId(String v)                   { this.branchId = v; }

    public OffsetDateTime getCheckinExpectedAt()        { return checkinExpectedAt; }
    public void setCheckinExpectedAt(OffsetDateTime v)  { this.checkinExpectedAt = v; }

    public OffsetDateTime getCheckoutExpectedAt()       { return checkoutExpectedAt; }
    public void setCheckoutExpectedAt(OffsetDateTime v) { this.checkoutExpectedAt = v; }

    public String getStatus()                           { return status; }
    public void setStatus(String v)                     { this.status = v; }

    public BigDecimal getDepositAmount()                { return depositAmount; }
    public void setDepositAmount(BigDecimal v)          { this.depositAmount = v; }

    public String getSpecialNote()                      { return specialNote; }
    public void setSpecialNote(String v)                { this.specialNote = v; }

    public OffsetDateTime getCreatedAt()                { return createdAt; }
    public void setCreatedAt(OffsetDateTime v)          { this.createdAt = v; }

    public OffsetDateTime getUpdatedAt()                { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime v)          { this.updatedAt = v; }

    /** @return true nếu booking đang hoạt động (chưa kết thúc) */
    public boolean isActive() {
        return STATUS_PENDING.equals(status)
            || STATUS_CONFIRMED.equals(status)
            || STATUS_CHECKED_IN.equals(status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking)) return false;
        return Objects.equals(bookingId, ((Booking) o).bookingId);
    }

    @Override public int hashCode() { return Objects.hash(bookingId); }

    // ── Display fields (JOIN từ các bảng khác) ────────────────────
    private String customerName;
    private String petName;
    private String roomNumber;

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String v) { this.customerName = v; }

    public String getPetName() { return petName; }
    public void setPetName(String v) { this.petName = v; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String v) { this.roomNumber = v; }
    @Override
    public String toString() {
        return "Booking{id='" + bookingId + "', customer='" + customerId
             + "', status='" + status + "'}";
    }
}
