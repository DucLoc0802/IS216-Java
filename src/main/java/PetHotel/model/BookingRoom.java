package PetHotel.model;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * BookingRoom — Ánh xạ bảng BOOKING_ROOM.
 *
 * Bảng trung gian: mỗi booking có thể gắn với nhiều phòng.
 *
 * Schema:
 *   booking_room_id  VARCHAR2(10)    PK
 *   booking_id       VARCHAR2(10)    NOT NULL, FK → booking
 *   room_id          VARCHAR2(10)    NOT NULL, FK → room
 *   assigned_at      TIMESTAMP(6) WITH TIME ZONE  DEFAULT SYSTIMESTAMP
 *   note             CLOB
 *
 * UNIQUE: (booking_id, room_id) — một phòng không thể gắn hai lần cho cùng booking.
 */
public class BookingRoom {

    private String bookingRoomId;
    private String bookingId;
    private String roomId;
    private OffsetDateTime assignedAt;
    private String note;

    // ── Constructors ──────────────────────────────────────────────

    public BookingRoom() {}

    public BookingRoom(String bookingRoomId, String bookingId, String roomId,
                       OffsetDateTime assignedAt, String note) {
        this.bookingRoomId = bookingRoomId;
        this.bookingId     = bookingId;
        this.roomId        = roomId;
        this.assignedAt    = assignedAt;
        this.note          = note;
    }

    // ── Getters / Setters ─────────────────────────────────────────

    public String getBookingRoomId()               { return bookingRoomId; }
    public void setBookingRoomId(String v)         { this.bookingRoomId = v; }

    public String getBookingId()                   { return bookingId; }
    public void setBookingId(String v)             { this.bookingId = v; }

    public String getRoomId()                      { return roomId; }
    public void setRoomId(String v)                { this.roomId = v; }

    public OffsetDateTime getAssignedAt()          { return assignedAt; }
    public void setAssignedAt(OffsetDateTime v)    { this.assignedAt = v; }

    public String getNote()                        { return note; }
    public void setNote(String v)                  { this.note = v; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookingRoom)) return false;
        return Objects.equals(bookingRoomId, ((BookingRoom) o).bookingRoomId);
    }

    @Override public int hashCode() { return Objects.hash(bookingRoomId); }

    @Override
    public String toString() {
        return "BookingRoom{id='" + bookingRoomId + "', bookingId='" + bookingId
             + "', roomId='" + roomId + "'}";
    }
}
