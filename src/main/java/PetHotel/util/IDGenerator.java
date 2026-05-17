package PetHotel.util;

import PetHotel.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * IDGenerator — Sinh primary key dạng chuỗi có prefix cho từng bảng.
 *
 * Định dạng: [PREFIX][7 chữ số zero-padded]
 * Ví dụ: CUS0000001, PET0000001, HR0000001
 *
 * Cơ chế: SELECT MAX(id) từ bảng → tăng thêm 1.
 * Thread-safety: Dùng SYNCHRONIZED để tránh race condition trong desktop app.
 *
 * TODO: Thay bằng Oracle SEQUENCE cho production đa người dùng (thread-safe ở DB).
 * TODO: Nếu dùng multi-instance, chỉ dùng Oracle SEQUENCE.
 * TODO: Có thể dùng UUID rút gọn nếu không cần ID đẹp.
 */
public class IDGenerator {

    // Ngăn khởi tạo
    private IDGenerator() {}

    /**
     * Sinh customer_id mới.
     * @return ví dụ: "CUS0000001"
     * @throws SQLException nếu lỗi DB
     */
    public static synchronized String nextCustomerId() throws SQLException {
        return nextId("CUS", "SELECT MAX(customer_id) FROM customer");
    }

    /**
     * Sinh pet_id mới.
     * @return ví dụ: "PET0000001"
     * @throws SQLException nếu lỗi DB
     */
    public static synchronized String nextPetId() throws SQLException {
        return nextId("PET", "SELECT MAX(pet_id) FROM pet");
    }

    /**
     * Sinh health_record_id mới.
     * @return ví dụ: "HR0000001"
     * @throws SQLException nếu lỗi DB
     */
    public static synchronized String nextHealthRecordId() throws SQLException {
        return nextId("HR", "SELECT MAX(health_record_id) FROM pet_health_record");
    }

    /**
     * Sinh booking_id mới.
     * @return ví dụ: "BK0000001"
     * @throws SQLException nếu lỗi DB
     */
    public static synchronized String nextBookingId() throws SQLException {
        return nextId("BK", "SELECT MAX(booking_id) FROM booking");
    }

    /**
     * Logic chung: lấy max hiện tại, tách số, +1, format lại.
     *
     * @param prefix tiền tố (CUS, PET, HR, ...)
     * @param sql    câu SELECT MAX(pk_column) FROM table
     * @return ID mới dạng [prefix][7 chữ số]
     * @throws SQLException nếu lỗi DB
     */
    private static String nextId(String prefix, String sql) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int nextNum = 1; // bảng trống → bắt đầu từ 1
            if (rs.next()) {
                String maxId = rs.getString(1);
                if (maxId != null && maxId.length() > prefix.length()) {
                    try {
                        // Bóc tách phần số phía sau prefix
                        String numPart = maxId.substring(prefix.length());
                        nextNum = Integer.parseInt(numPart) + 1;
                    } catch (NumberFormatException e) {
                        // ID format bất thường → fallback về random
                        // TODO: Log warning
                        nextNum = (int)(System.currentTimeMillis() % 9_000_000) + 1_000_000;
                    }
                }
            }
            // Format: prefix + số zero-padded 7 chữ số
            return String.format("%s%07d", prefix, nextNum);
        }
    }
}
