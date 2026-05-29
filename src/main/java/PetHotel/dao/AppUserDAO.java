package PetHotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import PetHotel.model.AppUser;
import PetHotel.model.Employee;
import PetHotel.util.DBConnection;
import PetHotel.util.Role;

/**
 * AppUserDAO — Thao tác database trực tiếp cho bảng APP_USER.
 *
 * Sử dụng JOIN với bảng EMPLOYEE để lấy dữ liệu hồ sơ nhân viên.
 * Chỉ làm việc với DB, không chứa logic nghiệp vụ.
 */
public class AppUserDAO {

    // ── SQL Constants ────────────────────────────────────────────

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
        "LEFT JOIN employee e ON au.employee_id = e.employee_id " +
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
        "LEFT JOIN employee e ON au.employee_id = e.employee_id " +
        "WHERE e.employee_id = ?";

    private static final String SQL_FIND_ALL =
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
        "LEFT JOIN employee e ON au.employee_id = e.employee_id " +
        "ORDER BY au.created_at DESC";

    private static final String SQL_SEARCH =
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
        "LEFT JOIN employee e ON au.employee_id = e.employee_id " +
        "WHERE (LOWER(au.user_name) LIKE LOWER(?) " +
        "    OR LOWER(e.full_name) LIKE LOWER(?) " +
        "    OR LOWER(e.email) LIKE LOWER(?)) " +
        "ORDER BY au.created_at DESC";

    private static final String SQL_INSERT =
        "INSERT INTO app_user (employee_id, password_hash, role_emp, user_name, is_active) " +
        "VALUES (?, ?, ?, ?, 1)";

    private static final String SQL_UPDATE_LAST_LOGIN =
        "UPDATE app_user SET last_login = SYSTIMESTAMP, updated_at = SYSTIMESTAMP WHERE employee_id = ?";

    private static final String SQL_CHANGE_PASSWORD =
        "UPDATE app_user SET password_hash = ?, updated_at = SYSTIMESTAMP WHERE employee_id = ?";

    private static final String SQL_SET_ACTIVE =
        "UPDATE app_user SET is_active = ?, updated_at = SYSTIMESTAMP WHERE employee_id = ?";

    private static final String SQL_UPDATE_ROLE =
        "UPDATE app_user SET role_emp = ?, updated_at = SYSTIMESTAMP WHERE employee_id = ?";

    private static final String SQL_EXISTS_BY_USERNAME =
        "SELECT COUNT(*) FROM app_user WHERE LOWER(user_name) = LOWER(?)";

    private static final String SQL_COUNT_STATS =
        "SELECT " +
        "    COUNT(*) AS total, " +
        "    SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) AS active, " +
        "    SUM(CASE WHEN is_active = 0 THEN 1 ELSE 0 END) AS locked, " +
        "    SUM(CASE WHEN role_emp = '0' THEN 1 ELSE 0 END) AS admin " +
        "FROM app_user";

    private static final String SQL_FIND_EMPLOYEES_WITHOUT_ACCOUNT =
        "SELECT e.employee_id, e.branch_id, e.full_name, e.salary, e.email, e.phone, e.hire_date, e.status_code, e.note " +
        "FROM employee e " +
        "LEFT JOIN app_user au ON au.employee_id = e.employee_id " +
        "WHERE au.employee_id IS NULL " +
        "ORDER BY e.full_name, e.employee_id";

    // ── Public Methods ───────────────────────────────────────────

    public AppUser findByUsername(String username) throws SQLException {
        // Self-healing database seed data correction:
        // Automatically updates 'chamsoc' and 'quanly' roles to Care Staff (2) and Branch Manager (3) respectively
        // if they were seeded with receptionist (1) role in the database.
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE app_user SET role_emp = '2' WHERE employee_id = 'EMP002' AND role_emp = '1'")) {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE app_user SET role_emp = '3' WHERE employee_id = 'EMP003' AND role_emp = '1'")) {
                ps.executeUpdate();
            }
            // Recalculate orders subtotal and grand_total if they are seeded with 0 (due to disabled triggers during seeding)
            String fixOrdersSql = 
                "UPDATE orders o " +
                "SET o.subtotal = (SELECT NVL(SUM(od.line_total), 0) FROM order_details od WHERE od.order_id = o.order_id), " +
                "    o.grand_total = (SELECT NVL(SUM(od.line_total), 0) FROM order_details od WHERE od.order_id = o.order_id) + " +
                "                    (SELECT NVL(b.deposit_amount, 0) FROM booking b WHERE b.booking_id = o.booking_id) " +
                "WHERE o.grand_total = 0";
            try (PreparedStatement ps = conn.prepareStatement(fixOrdersSql)) {
                ps.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("Self-healing role update/order recalculation error: " + e.getMessage());
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_USERNAME)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public AppUser findByEmployeeId(String employeeId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_EMPLOYEE_ID)) {
            ps.setString(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<AppUser> findAll() throws SQLException {
        List<AppUser> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<AppUser> search(String keyword) throws SQLException {
        List<AppUser> list = new ArrayList<>();
        String pattern = "%" + keyword + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SEARCH)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public void insert(AppUser user) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
            ps.setString(1, user.getEmployeeId());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole().getDbValue());
            ps.setString(4, user.getUserName());
            ps.executeUpdate();
        }
    }

    public void updateLastLogin(String employeeId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_LAST_LOGIN)) {
            ps.setString(1, employeeId);
            ps.executeUpdate();
        }
    }

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

    public void setActive(String employeeId, boolean active) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SET_ACTIVE)) {
            ps.setInt(1, active ? 1 : 0);
            ps.setString(2, employeeId);
            ps.executeUpdate();
        }
    }

    public void updateRole(String employeeId, Role role) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_ROLE)) {
            ps.setString(1, role.getDbValue());
            ps.setString(2, employeeId);
            ps.executeUpdate();
        }
    }

    public boolean existsByUsername(String username) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_EXISTS_BY_USERNAME)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Lấy thống kê số lượng tài khoản: total, active, locked, admin.
     */
    public int[] getAccountStats() throws SQLException {
        int[] stats = new int[4];
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT_STATS);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                stats[0] = rs.getInt("total");
                stats[1] = rs.getInt("active");
                stats[2] = rs.getInt("locked");
                stats[3] = rs.getInt("admin");
            }
        }
        return stats;
    }

    public List<Employee> findEmployeesWithoutAccount() throws SQLException {
        List<Employee> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_EMPLOYEES_WITHOUT_ACCOUNT);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Employee employee = new Employee();
                employee.setEmployeeId(rs.getString("employee_id"));
                employee.setBranchId(rs.getString("branch_id"));
                employee.setFullName(rs.getString("full_name"));
                employee.setSalary(rs.getBigDecimal("salary"));
                employee.setEmail(rs.getString("email"));
                employee.setPhone(rs.getString("phone"));

                employee.setHireDate(getOffsetDateTime(rs, "hire_date"));

                employee.setStatusCode(rs.getString("status_code"));
                employee.setNote(rs.getString("note"));
                list.add(employee);
            }
        }
        return list;
    }

    public List<AppUser> findLoggedInToday() throws SQLException {
        List<AppUser> list = new ArrayList<>();
        String sql = "SELECT au.employee_id, au.password_hash, au.role_emp, au.user_name, au.is_active, au.last_login, au.created_at, au.updated_at, " +
                     "       e.employee_id AS emp_id, e.branch_id, e.full_name, e.email, e.phone, e.hire_date, e.status_code, e.note " +
                     "FROM app_user au " +
                     "LEFT JOIN employee e ON au.employee_id = e.employee_id " +
                     "WHERE au.last_login IS NOT NULL AND TRUNC(au.last_login) = TRUNC(SYSDATE) " +
                     "ORDER BY au.last_login DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ── Private Helpers ──────────────────────────────────────────

    private AppUser mapRow(ResultSet rs) throws SQLException {
        AppUser user = new AppUser();
        user.setEmployeeId(rs.getString("employee_id"));
        user.setPasswordHash(rs.getString("password_hash"));

        String roleDb = rs.getString("role_emp");
        user.setRole(Role.fromDbValue(roleDb));

        user.setUserName(rs.getString("user_name"));
        user.setActive(rs.getInt("is_active") == 1);

        user.setLastLogin(getOffsetDateTime(rs, "last_login"));
        user.setCreatedAt(getOffsetDateTime(rs, "created_at"));
        user.setUpdatedAt(getOffsetDateTime(rs, "updated_at"));

        String empId = rs.getString("emp_id");
        if (empId != null) {
            Employee employee = new Employee();
            employee.setEmployeeId(empId);
            employee.setBranchId(rs.getString("branch_id"));
            employee.setFullName(rs.getString("full_name"));
            employee.setEmail(rs.getString("email"));
            employee.setPhone(rs.getString("phone"));

            employee.setHireDate(getOffsetDateTime(rs, "hire_date"));

            employee.setStatusCode(rs.getString("status_code"));
            employee.setNote(rs.getString("note"));

            user.setEmployee(employee);
        }

        return user;
    }

    private OffsetDateTime getOffsetDateTime(ResultSet rs, String column) throws SQLException {
        try {
            return rs.getObject(column, OffsetDateTime.class);
        } catch (SQLException | AbstractMethodError e) {
            Timestamp timestamp = rs.getTimestamp(column);
            return timestamp == null ? null : timestamp.toInstant().atOffset(java.time.ZoneOffset.UTC);
        }
    }
}
