package PetHotel.model;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * PetHealthRecord — Ánh xạ bảng PET_HEALTH_RECORD.
 *
 * Schema:
 *   health_record_id  VARCHAR2(10)  PK
 *   pet_id            VARCHAR2(10)  NOT NULL, FK → pet
 *   booking_id        VARCHAR2(10)  NOT NULL, FK → booking
 *   recorded_at       TIMESTAMP(6) WITH TIME ZONE  DEFAULT SYSTIMESTAMP
 *   note              CLOB
 *   status            NUMBER(1)     CHECK IN (0,1)  DEFAULT 1
 *
 * NOTE: status=1 → bình thường, status=0 → có vấn đề / cần theo dõi
 *       (cần xác nhận lại với team về nghĩa của status)
 */
public class PetHealthRecord {

    /** Mã hồ sơ sức khoẻ, PK */
    private String healthRecordId;

    /** Mã thú cưng */
    private String petId;

    /** Booking liên quan (health record được ghi trong một booking cụ thể) */
    private String bookingId;

    /** Thời điểm ghi nhận */
    private OffsetDateTime recordedAt;

    /** Nội dung ghi chú sức khoẻ (CLOB) */
    private String note;

    /**
     * Trạng thái sức khoẻ.
     * 1 = bình thường / healthy
     * 0 = có vấn đề / cần theo dõi
     */
    private int status;

    // ── Constructors ─────────────────────────────────────────────

    public PetHealthRecord() {}

    /** Constructor cho insert mới */
public PetHealthRecord(String healthRecordId, String petId, String bookingId, String note, int status) {
        this.healthRecordId = healthRecordId;
        this.petId = petId;
        this.bookingId = bookingId;
        this.note = note;
        this.status = status;
    }

    /** Constructor đầy đủ cho DAO mapping */
    public PetHealthRecord(String healthRecordId, String petId, String bookingId,
                           OffsetDateTime recordedAt, String note, int status) {
        this.healthRecordId = healthRecordId;
        this.petId          = petId;
        this.bookingId      = bookingId;
        this.recordedAt     = recordedAt;
        this.note           = note;
        this.status         = status;
    }

    // ── Getters / Setters ─────────────────────────────────────────

    public String getHealthRecordId()              { return healthRecordId; }
    public void setHealthRecordId(String v)        { this.healthRecordId = v; }

    public String getPetId()                       { return petId; }
    public void setPetId(String v)                 { this.petId = v; }

    public String getBookingId()                   { return bookingId; }
    public void setBookingId(String v)             { this.bookingId = v; }

    public OffsetDateTime getRecordedAt()          { return recordedAt; }
    public void setRecordedAt(OffsetDateTime v)    { this.recordedAt = v; }

    public String getNote()                        { return note; }
    public void setNote(String v)                  { this.note = v; }

    public int getStatus()                         { return status; }
    public void setStatus(int v)                   { this.status = v; }

    /** @return true nếu sức khoẻ bình thường (status = 1) */
    public boolean isHealthy()                     { return status == 1; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PetHealthRecord)) return false;
        return Objects.equals(healthRecordId, ((PetHealthRecord) o).healthRecordId);
    }

    @Override public int hashCode() { return Objects.hash(healthRecordId); }

    @Override
    public String toString() {
        return "PetHealthRecord{id='" + healthRecordId + "', petId='" + petId
             + "', bookingId='" + bookingId + "', status=" + status + "}";
    }
}
