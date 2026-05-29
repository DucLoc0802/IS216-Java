package PetHotel.bus;

import PetHotel.dao.BookingDAO;
import PetHotel.dao.RoomDAO;
import PetHotel.exception.ValidationException;
import PetHotel.model.Booking;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class BookingBUS {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    // ── UC-BOOK-03: Tra cứu booking ──────────────────────────────

    public List<Booking> getAllBookings() {
        try {
            return bookingDAO.findAll();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải danh sách booking.", e);
        }
    }

    public List<Booking> getBookingsByDateRange(java.util.Date start, java.util.Date end) {
        try {
            return bookingDAO.findByDateRange(start, end);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải danh sách booking theo khoảng thời gian.", e);
        }
    }

    public List<Booking> searchBookings(String keyword, String status) {
        try {
            return bookingDAO.search(keyword, status);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm kiếm booking.", e);
        }
    }

    // ── UC-BOOK-02: Tạo booking mới ──────────────────────────────

    public void createBooking(Booking booking, String roomId) {
        createBooking(booking, roomId, null);
    }

    public Booking createBooking(Booking booking, String roomId, String petId) {
        if (booking.getCustomerId() == null || booking.getCustomerId().trim().isEmpty())
            throw new ValidationException("Vui lòng chọn khách hàng.");
        if (roomId == null || roomId.trim().isEmpty())
            throw new ValidationException("Vui lòng chọn phòng.");
        if (booking.getCheckinExpectedAt() == null)
            throw new ValidationException("Vui lòng chọn ngày check-in.");
        if (booking.getCheckoutExpectedAt() == null)
            throw new ValidationException("Vui lòng chọn ngày check-out.");
        if (!booking.getCheckoutExpectedAt().isAfter(booking.getCheckinExpectedAt()))
            throw new ValidationException("Ngày check-out phải sau ngày check-in.");

        try {
            int nextNum = bookingDAO.getNextBookingNumber();
            booking.setBookingId("BKD" + String.format("%03d", nextNum));
        } catch (SQLException e) {
            booking.setBookingId("BKD" + String.format("%03d", (int)(Math.random() * 999)));
        }
        booking.setBranchId("BR001");
        if (booking.getDepositAmount() == null)
            booking.setDepositAmount(BigDecimal.ZERO);

        try {
            bookingDAO.insert(booking, roomId, petId, null);
            // Nếu có thú cưng → tự động cập nhật trạng thái phòng sang IN_USE
            if (petId != null && !petId.trim().isEmpty()) {
                new RoomBUS().autoUpdateRoomStatus(roomId);
            }
            return booking; 
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tạo booking.", e);
        }
    }

    // ── UC-BOOK-06: Check-in ─────────────────────────────────────

    public void checkIn(String bookingId) {
        if (bookingId == null || bookingId.trim().isEmpty())
            throw new ValidationException("Mã booking không hợp lệ.");
        try {
            bookingDAO.updateStatus(bookingId, "CHECKED_IN");
            // Tự động cập nhật trạng thái phòng thành IN_USE (có thú cưng check-in)
            String roomId = bookingDAO.findRoomIdByBookingId(bookingId);
            if (roomId != null) {
                new RoomBUS().autoUpdateRoomStatus(roomId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi check-in.", e);
        }
    }

    // ── UC-BOOK-07: Check-out ────────────────────────────────────

    public void checkOut(String bookingId) {
        if (bookingId == null || bookingId.trim().isEmpty())
            throw new ValidationException("Mã booking không hợp lệ.");
        try {
            String roomId = bookingDAO.findRoomIdByBookingId(bookingId);
            bookingDAO.updateStatus(bookingId, "CHECKED_OUT");
            // Tự động cập nhật trạng thái phòng (nếu hết thú cưng → AVAILABLE)
            if (roomId != null) {
                new RoomBUS().autoUpdateRoomStatus(roomId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi check-out.", e);
        }
    }

    // ── UC-BOOK-05: Hủy booking ──────────────────────────────────

    public void cancelBooking(String bookingId) {
        if (bookingId == null || bookingId.trim().isEmpty())
            throw new ValidationException("Mã booking không hợp lệ.");
        try {
            String roomId = bookingDAO.findRoomIdByBookingId(bookingId);
            bookingDAO.updateStatus(bookingId, "CANCELLED");
            // Tự động cập nhật trạng thái phòng (nếu hết thú cưng → AVAILABLE)
            if (roomId != null) {
                new RoomBUS().autoUpdateRoomStatus(roomId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi hủy booking.", e);
        }
    }

    // ── UC-BOOK-08: Xóa booking (xóa khỏi DB) ───────────────────

    public void deleteBooking(String bookingId) {
        if (bookingId == null || bookingId.trim().isEmpty())
            throw new ValidationException("Mã booking không hợp lệ.");
        try {
            String roomId = bookingDAO.findRoomIdByBookingId(bookingId);
            bookingDAO.delete(bookingId);
            // Tự động cập nhật trạng thái phòng (nếu hết thú cưng → AVAILABLE)
            if (roomId != null) {
                new RoomBUS().autoUpdateRoomStatus(roomId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa booking.", e);
        }
    }

    public String getBookingRoomId(String bookingId) {
        try {
            return bookingDAO.findBookingRoomId(bookingId);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy booking room id.", e);
        }
    }

    public void addPetToBookingRoom(String bookingRoomId, String petId) {
        try {
            bookingDAO.insertBookingRoomPet(bookingRoomId, petId);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm thú cưng vào booking.", e);
        }
    }
}