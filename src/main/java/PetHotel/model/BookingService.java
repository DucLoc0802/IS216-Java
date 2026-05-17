package PetHotel.model;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * BookingService — Ánh xạ bảng BOOKING_SERVICES.
 *
 * Schema:
 *   booking_service_id  VARCHAR2(10)    PK
 *   booking_id          VARCHAR2(10)    NOT NULL, FK → booking
 *   service_id          VARCHAR2(10)    FK → services (nullable)
 *   employee_id         VARCHAR2(10)    FK → employee (nullable — nhân viên thực hiện)
 *   scheduled_at        TIMESTAMP(6) WITH TIME ZONE
 *   status              NVARCHAR2(20)   CHECK IN ('PENDING','SCHEDULED','IN_PROGRESS','DONE','CANCELLED')
 *   note                CLOB
 *   created_at          TIMESTAMP(6) WITH TIME ZONE
 *   updated_at          TIMESTAMP(6) WITH TIME ZONE
 */
public class BookingService {

    public static final String STATUS_PENDING     = "PENDING";
    public static final String STATUS_SCHEDULED   = "SCHEDULED";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_DONE        = "DONE";
    public static final String STATUS_CANCELLED   = "CANCELLED";

    private String bookingServiceId;
    private String bookingId;
    private String serviceId;      // nullable
    private String employeeId;     // nullable
    private OffsetDateTime scheduledAt;
    private String status;
    private String note;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // ── Constructors ──────────────────────────────────────────────

    public BookingService() {}

    public BookingService(String bookingServiceId, String bookingId, String serviceId,
                          String employeeId, OffsetDateTime scheduledAt, String status,
                          String note, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.bookingServiceId = bookingServiceId;
        this.bookingId        = bookingId;
        this.serviceId        = serviceId;
        this.employeeId       = employeeId;
        this.scheduledAt      = scheduledAt;
        this.status           = status;
        this.note             = note;
        this.createdAt        = createdAt;
        this.updatedAt        = updatedAt;
    }

    // ── Getters / Setters ─────────────────────────────────────────

    public String getBookingServiceId()                  { return bookingServiceId; }
    public void setBookingServiceId(String v)            { this.bookingServiceId = v; }

    public String getBookingId()                         { return bookingId; }
    public void setBookingId(String v)                   { this.bookingId = v; }

    public String getServiceId()                         { return serviceId; }
    public void setServiceId(String v)                   { this.serviceId = v; }

    public String getEmployeeId()                        { return employeeId; }
    public void setEmployeeId(String v)                  { this.employeeId = v; }

    public OffsetDateTime getScheduledAt()               { return scheduledAt; }
    public void setScheduledAt(OffsetDateTime v)         { this.scheduledAt = v; }

    public String getStatus()                            { return status; }
    public void setStatus(String v)                      { this.status = v; }

    public String getNote()                              { return note; }
    public void setNote(String v)                        { this.note = v; }

    public OffsetDateTime getCreatedAt()                 { return createdAt; }
    public void setCreatedAt(OffsetDateTime v)           { this.createdAt = v; }

    public OffsetDateTime getUpdatedAt()                 { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime v)           { this.updatedAt = v; }

    /** @return true nếu dịch vụ chưa hoàn tất */
    public boolean isActive() {
        return STATUS_PENDING.equals(status)
            || STATUS_SCHEDULED.equals(status)
            || STATUS_IN_PROGRESS.equals(status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookingService)) return false;
        return Objects.equals(bookingServiceId, ((BookingService) o).bookingServiceId);
    }

    @Override public int hashCode() { return Objects.hash(bookingServiceId); }

    @Override
    public String toString() {
        return "BookingService{id='" + bookingServiceId + "', bookingId='" + bookingId
             + "', serviceId='" + serviceId + "', status='" + status + "'}";
    }
}
