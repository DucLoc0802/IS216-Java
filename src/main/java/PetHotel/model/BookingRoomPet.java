package PetHotel.model;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * BookingRoomPet — Ánh xạ bảng BOOKING_ROOM_PET.
 *
 * Bảng nhiều-nhiều: mỗi booking_room có thể chứa nhiều pet,
 * mỗi pet có thể được gán vào nhiều booking_room (qua các booking khác nhau).
 *
 * Schema:
 *   booking_room_id  VARCHAR2(10)    PK (composite với pet_id)
 *   pet_id           VARCHAR2(10)    PK (composite với booking_room_id)
 *   assigned_at      TIMESTAMP(6) WITH TIME ZONE  DEFAULT SYSTIMESTAMP
 *   note             CLOB
 */
public class BookingRoomPet {

    /** Phần 1 của composite PK */
    private String bookingRoomId;

    /** Phần 2 của composite PK */
    private String petId;

    private OffsetDateTime assignedAt;
    private String note;

    // ── Constructors ──────────────────────────────────────────────

    public BookingRoomPet() {}

    public BookingRoomPet(String bookingRoomId, String petId,
                          OffsetDateTime assignedAt, String note) {
        this.bookingRoomId = bookingRoomId;
        this.petId         = petId;
        this.assignedAt    = assignedAt;
        this.note          = note;
    }

    // ── Getters / Setters ─────────────────────────────────────────

    public String getBookingRoomId()               { return bookingRoomId; }
    public void setBookingRoomId(String v)         { this.bookingRoomId = v; }

    public String getPetId()                       { return petId; }
    public void setPetId(String v)                 { this.petId = v; }

    public OffsetDateTime getAssignedAt()          { return assignedAt; }
    public void setAssignedAt(OffsetDateTime v)    { this.assignedAt = v; }

    public String getNote()                        { return note; }
    public void setNote(String v)                  { this.note = v; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookingRoomPet)) return false;
        BookingRoomPet that = (BookingRoomPet) o;
        return Objects.equals(bookingRoomId, that.bookingRoomId)
            && Objects.equals(petId, that.petId);
    }

    @Override
    public int hashCode() { return Objects.hash(bookingRoomId, petId); }

    @Override
    public String toString() {
        return "BookingRoomPet{bookingRoomId='" + bookingRoomId
             + "', petId='" + petId + "'}";
    }
}
