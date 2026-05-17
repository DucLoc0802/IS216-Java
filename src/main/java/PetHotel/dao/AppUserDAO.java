package PetHotel.dao;

//import PetHotel.exception.BusinessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import PetHotel.model.AppUser;
import PetHotel.util.DBConnection;
import PetHotel.util.Role;

/**
 * AppUserDAO — Thao tác database trực tiếp cho bảng APP_USER.
 *
 * Chỉ làm việc với DB, không chứa logic nghiệp vụ.
 * Mọi validate và kiểm tra quyền → đặt trong AuthBUS.
 *
 * TODO: Thêm connection pool để tránh tạo connection mới mỗi lần gọi.
 * TODO: Externalize SQL strings ra file constants hoặc properties.
 * TODO: Thêm logging (SLF4J/Log4j) thay vì System.err.
 */
public class AppUserDAO {

    // ── SQL Constants ────────────────────────────────────────────

    private static final String SQL_FIND_BY_USERNAME =
        "SELECT employee_id, password_hash, role_emp, user_name, " +
        "       is_active, last_login, created_at, updated_at " +
        "FROM app_user " +
        "WHERE LOWER(user_name) = LOWER(?)";

    private static final String SQL_FIND_BY_EMPLOYEE_ID =
        "SELECT employee_id, password_hash, role_emp, user_name, " +
        "       is_active, last_login, created_at, updated_at " +
        "FROM app_user " +
        "WHERE employee_id = ?";

    private static final String SQL_UPDATE_LAST_LOGIN =
        "UPDATE app_user " +
        "SET last_login = SYSTIMESTAMP, updated_at = SYSTIMESTAMP " +
        "WHERE employee_id = ?";

    private static final String SQL_CHANGE_PASSWORD =
        "UPDATE app_user " +
        "SET password_hash = ?, updated_at = SYSTIMESTAMP " +
        "WHERE employee_id = ?";

    private static final String SQL_COUNT_ACTIVE_BOOKINGS_FOR_EMPLOYEE =
        // Dùng để kiểm tra trước khi khóa tài khoản (nếu cần)
        "SELECT COUNT(*) FROM booking_services " +
        "WHERE employee_id = ? AND status NOT IN ('DONE','CANCELLED')";

    // ── Public Methods ───────────────────────────────────────────

    /**
     * Tìm AppUser theo username (case-insensitive).
     * Dùng cho chức năng đăng nhập.
     *
     * @param username tên đăng nhập (không phân biệt hoa/thường)
     * @return AppUser hoặc null nếu không tìm thấy
     * @throws SQLException nếu lỗi DB
     */
    public AppUser findByUsername(String username) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_USERNAME)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Tìm AppUser theo employee_id (PK).
     *
     * @param employeeId mã nhân viên
     * @return AppUser hoặc null nếu không tồn tại
     * @throws SQLException nếu lỗi DB
     */
    public AppUser findByEmployeeId(String employeeId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_EMPLOYEE_ID)) {

            ps.setString(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Cập nhật last_login = SYSTIMESTAMP sau khi đăng nhập thành công.
     * Dùng connection riêng với autoCommit=true (update đơn lẻ).
     *
     * @param employeeId PK của app_user
     * @throws SQLException nếu lỗi DB
     * @throws NotFoundException nếu không tìm thấy user
     */
    public void updateLastLogin(String employeeId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_LAST_LOGIN)) {

            ps.setString(1, employeeId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                //throw new NotFoundException("Không tìm thấy app_user với employee_id: " + employeeId);
            }
        }
    }

    /**
     * Thay đổi mật khẩu.
     * Chỉ lưu hash mới, không kiểm tra password cũ tại đây.
     * Kiểm tra password cũ → thực hiện ở AuthBUS.
     *
     * @param employeeId   PK của app_user
     * @param newHashedPassword password đã được hash
     * @param conn connection dùng chung nếu trong transaction (null = tự tạo connection mới)
     * @throws SQLException nếu lỗi DB
     */
    public void changePassword(String employeeId, String newHashedPassword, Connection conn)
            throws SQLException {
        boolean ownConnection = (conn == null);
        Connection c = ownConnection ? DBConnection.getConnection() : conn;
        try {
            PreparedStatement ps = c.prepareStatement(SQL_CHANGE_PASSWORD);
            ps.setString(1, newHashedPassword);
            ps.setString(2, employeeId);
            ps.executeUpdate();
            ps.close();
            if (ownConnection) c.commit(); // autoCommit=true nếu dùng getConnection()
        } finally {
            if (ownConnection) DBConnection.closeQuietly(c);
        }
    }

    // ── Private Helpers ──────────────────────────────────────────

    /**
     * Map một hàng ResultSet sang AppUser object.
     * Gọi sau khi rs.next() đã trả về true.
     *
     * @param rs ResultSet đang trỏ vào hàng hiện tại
     * @return AppUser đã được map đầy đủ
     * @throws SQLException nếu lỗi khi đọc cột
     */
    private AppUser mapRow(ResultSet rs) throws SQLException {
        AppUser user = new AppUser();
        user.setEmployeeId(rs.getString("employee_id"));
        user.setPasswordHash(rs.getString("password_hash"));

        // role_emp lưu dạng String "0".."5"
        String roleDb = rs.getString("role_emp");
        user.setRole(Role.fromDbValue(roleDb));

        user.setUserName(rs.getString("user_name"));

        // is_active: NUMBER(1) → 1=true, 0=false
        user.setActive(rs.getInt("is_active") == 1);

        // TIMESTAMP WITH TIME ZONE → OffsetDateTime
        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) {
            user.setLastLogin(lastLogin.toInstant()
                .atOffset(java.time.ZoneOffset.UTC));
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toInstant()
                .atOffset(java.time.ZoneOffset.UTC));
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toInstant()
                .atOffset(java.time.ZoneOffset.UTC));
        }

        return user;
    }
}
