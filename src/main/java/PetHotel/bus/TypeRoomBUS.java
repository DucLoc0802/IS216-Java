package PetHotel.bus;

import PetHotel.dao.TypeRoomDAO;
import PetHotel.model.TypeRoom;

import java.sql.SQLException;
import java.util.List;

public class TypeRoomBUS {
    private final TypeRoomDAO typeRoomDAO = new TypeRoomDAO();

    public List<TypeRoom> getAllTypeRooms() {
        try {
            return typeRoomDAO.findAll();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải loại phòng.", e);
        }
    }
}
