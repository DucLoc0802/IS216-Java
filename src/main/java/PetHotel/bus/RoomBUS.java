package PetHotel.bus;

import PetHotel.dao.RoomDAO;
import PetHotel.exception.ValidationException;
import PetHotel.model.Room;

import java.sql.SQLException;
import java.util.List;

public class RoomBUS {

    private final RoomDAO roomDAO;

    public RoomBUS() {
        this.roomDAO = new RoomDAO();
    }

    // ── UC-ROOM-01: Tra cứu phòng ────────────────────────────────

    public List<Room> getAllRooms() {
        try {
            return roomDAO.findAll();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải danh sách phòng.", e);
        }
    }

    public List<Room> searchRooms(String keyword, String status, String typeRoomId) {
        try {
            return roomDAO.search(keyword, status, typeRoomId);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm kiếm phòng.", e);
        }
    }

    // ── UC-ROOM-02: Thêm phòng ───────────────────────────────────

    public void addRoom(Room room) {
        // Validate
        if (room.getRoomNumber() == null || room.getRoomNumber().trim().isEmpty())
            throw new ValidationException("Số phòng không được để trống.");
        if (room.getTypeRoomId() == null || room.getTypeRoomId().trim().isEmpty())
            throw new ValidationException("Loại phòng không được để trống.");
        if (room.getBranchId() == null || room.getBranchId().trim().isEmpty())
            throw new ValidationException("Chi nhánh không được để trống.");

        try {
            // Kiểm tra trùng số phòng
            if (roomDAO.existsByRoomNumber(room.getRoomNumber(), null))
                throw new ValidationException("Số phòng '" + room.getRoomNumber() + "' đã tồn tại.");

            // Tạo room_id tự động
            String newId = "ROOM" + String.format("%03d", (int)(Math.random() * 999));
            room.setRoomId(newId);
            room.setStatus("AVAILABLE");

            roomDAO.insert(room, null);

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm phòng.", e);
        }
    }

    // ── UC-ROOM-06: Cập nhật trạng thái phòng ───────────────────

    public void updateRoomStatus(String roomId, String newStatus) {
        if (roomId == null || roomId.trim().isEmpty())
            throw new ValidationException("Mã phòng không hợp lệ.");

        List<String> validStatuses = List.of("AVAILABLE", "IN_USE", "MAINTENANCE");
        if (!validStatuses.contains(newStatus))
            throw new ValidationException("Trạng thái không hợp lệ: " + newStatus);

        try {
            roomDAO.updateStatus(roomId, newStatus, null);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật trạng thái phòng.", e);
        }
    }

    // ── UC-ROOM-04/05: Sửa / Xóa phòng ─────────────────────────

    public void updateRoom(Room room) {
        if (room.getRoomNumber() == null || room.getRoomNumber().trim().isEmpty())
            throw new ValidationException("Số phòng không được để trống.");

        try {
            if (roomDAO.existsByRoomNumber(room.getRoomNumber(), room.getRoomId()))
                throw new ValidationException("Số phòng đã tồn tại.");

            roomDAO.update(room, null);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật phòng.", e);
        }
    }

    public void deleteRoom(String roomId) {
        try {
            if (roomDAO.countActiveBookings(roomId) > 0)
                throw new ValidationException("Không thể xóa phòng đang có booking hoạt động.");

            roomDAO.delete(roomId, null);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa phòng.", e);
        }
    }
}