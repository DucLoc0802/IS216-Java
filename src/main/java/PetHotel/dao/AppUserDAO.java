package PetHotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import PetHotel.model.AppUser;
import PetHotel.model.Employee;
import PetHotel.util.DBConnection;
import PetHotel.util.Role;

/**
 * AppUserDAO — Thao tác database trực tiếp cho bảng APP_USER.
 *
 * Sử dụng JOIN với bảng EMPLOYEE để lấy dữ liệu hồ sơ nhân viên
 * (fullName, email, phone, branchId) ngay trong một truy vấn.
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

    /**
     * Truy vấn AppUser kèm JOIN Employee để lấy đầy đủ profile.
     *
     * Lấy tất cả cột từ APP_USER (au.*) và các cột cần thiết từ EMPLOYEE (e.*).
     * Dùng alias cho cột employee_id của EMPLOYEE để tránh xung đột với APP_USER.
     */
    private static final String SQL_FIND_BY_USERNAME =
        "SELECT " +
        "    au.employee_id, au.password_hash, au.role_emp, au.user_name, " +
        "    au.is_active, au.last_login, au.created_at, au.updated_at, " +
        "    e.employee_id   AS emp_id, " +
        "    e.branch_id, " +
        "    e.full_name, " +
        "    e.email, " +
        "    e.phone, " +
        "    e.hire_date, " +
        "    e.status_code, " +
        "    e.note " +
        "FROM app_user au " +
        "JOIN employee e ON au.employee_id = e.employee_id " +
        "WHERE LOWER(au.user_name) = LOWER(?)";

    private static final String SQL_FIND_BY_EMPLOYEE_ID =
        "SELECT " +
        "    au.employee_id, au.password_hash, au.role_emp, au.user_name, " +
        "    au.is_active, au.last_login, au.created_at, au.updated_at, " +
        "    e.employee_id   AS emp_id, " +
        "    e.branch_id, " +
        "    e.full_name, " +
        "    e.email, " +
        "    e.phone, " +
        "    e.hire_date, " +
        "    e.status_code, " +
        "    e.note " +
        "FROM app_user au " +
        "JOIN employee e ON au.employee_id = e.employee_id " +
        "WHERE au.employee_id = ?";

    private static final String SQL_UPDATE_LAST_LOGIN =
        "UPDATE app_user " +
        "SET last_login = SYSTIMESTAMP, updated_at = SYSTIMESTAMP " +
        "WHERE employee_id = ?";

    private static final String SQL_CHANGE_PASSWORD =
        "UPDATE app_user " +
        "SET password_hash = ?, updated_at = SYSTIMESTAMP " +
        "WHERE employee_id = ?";

    // ── Public Methods ───────────────────────────────────────────

    /**
     * Tìm AppUser theo username (case-insensitive), kèm Employee profile.
     * Dùng cho chức năng đăng nhập.
     *
     * @param username tên đăng nhập (không phân biệt hoa/thường)
     * @return AppUser (có Employee embedded) hoặc null nếu không tìm thấy
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
     * Tìm AppUser theo employee_id (PK), kèm Employee profile.
     *
     * @param employeeId mã nhân viên
     * @return AppUser (có Employee embedded) hoặc null nếu không tồn tại
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
     *
     * @param employeeId PK của app_user
     * @throws SQLException nếu lỗi DB
     */
    public void updateLastLogin(String employeeId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_LAST_LOGIN)) {

            ps.setString(1, employeeId);
            ps.executeUpdate();
        }
    }

    /**
     * Thay đổi mật khẩu.
     * Chỉ lưu hash mới, không kiểm tra password cũ tại đây.
     *
     * @param employeeId          PK của app_user
     * @param newHashedPassword   password đã được hash
     * @param conn                connection dùng chung nếu trong transaction (null = tự tạo connection mới)
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
        } finally {
            if (ownConnection) DBConnection.closeQuietly(c);
        }
    }

    // ── Private Helpers ──────────────────────────────────────────

    /**
     * Map một hàng ResultSet sang AppUser object, kèm Employee embedded.
     *
     * @param rs ResultSet đang trỏ vào hàng hiện tại
     * @return AppUser đã được map đầy đủ (có Employee)
     * @throws SQLException nếu lỗi khi đọc cột
     */
    private AppUser mapRow(ResultSet rs) throws SQLException {
        // ── AppUser fields ────────────────────────────────────────
        AppUser user = new AppUser();
        user.setEmployeeId(rs.getString("employee_id"));
        user.setPasswordHash(rs.getString("password_hash"));

        String roleDb = rs.getString("role_emp");
        user.setRole(Role.fromDbValue(roleDb));

        user.setUserName(rs.getString("user_name"));
        user.setActive(rs.getInt("is_active") == 1);

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

        // ── Employee fields ───────────────────────────────────────
        Employee employee = new Employee();
        employee.setEmployeeId(rs.getString("emp_id"));
        employee.setBranchId(rs.getString("branch_id"));
        employee.setFullName(rs.getString("full_name"));
        employee.setEmail(rs.getString("email"));
        employee.setPhone(rs.getString("phone"));

        Timestamp hireDate = rs.getTimestamp("hire_date");
        if (hireDate != null) {
            employee.setHireDate(hireDate.toInstant()
                .atOffset(java.time.ZoneOffset.UTC));
        }

        employee.setStatusCode(rs.getString("status_code"));
        employee.setNote(rs.getString("note"));

        // ── Liên kết Employee vào AppUser ─────────────────────────
        user.setEmployee(employee);

        return user;
    }
}