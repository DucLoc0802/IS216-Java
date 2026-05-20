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

public class AppUserDAO {

    private static final String SQL_FIND_BY_USERNAME =
        "SELECT " +
        "    au.user_id, au.password_hash, au.role_emp, au.user_name, " +
        "    au.is_active, au.last_login, au.created_at, au.updated_at, " +
        "    e.employee_id AS emp_id, " +
        "    e.branch_id, " +
        "    e.full_name, " +
        "    e.email, " +
        "    e.phone, " +
        "    e.hire_date, " +
        "    e.status_code, " +
        "    e.note " +
        "FROM app_user au " +
        "LEFT JOIN employee e ON au.user_id = e.user_id " +
        "WHERE LOWER(au.user_name) = LOWER(?)";

    private static final String SQL_FIND_BY_EMPLOYEE_ID =
        "SELECT " +
        "    au.user_id, au.password_hash, au.role_emp, au.user_name, " +
        "    au.is_active, au.last_login, au.created_at, au.updated_at, " +
        "    e.employee_id AS emp_id, " +
        "    e.branch_id, " +
        "    e.full_name, " +
        "    e.email, " +
        "    e.phone, " +
        "    e.hire_date, " +
        "    e.status_code, " +
        "    e.note " +
        "FROM app_user au " +
        "LEFT JOIN employee e ON au.user_id = e.user_id " +
        "WHERE e.employee_id = ?";

    private static final String SQL_UPDATE_LAST_LOGIN =
        "UPDATE app_user " +
        "SET last_login = SYSTIMESTAMP, updated_at = SYSTIMESTAMP " +
        "WHERE user_id = ?";

    private static final String SQL_CHANGE_PASSWORD =
        "UPDATE app_user " +
        "SET password_hash = ?, updated_at = SYSTIMESTAMP " +
        "WHERE user_id = ?";

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

    public void updateLastLogin(String userId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_LAST_LOGIN)) {

            ps.setString(1, userId);
            ps.executeUpdate();
        }
    }

    public void changePassword(String userId, String newHashedPassword, Connection conn)
            throws SQLException {

        boolean ownConnection = (conn == null);
        Connection c = ownConnection ? DBConnection.getConnection() : conn;

        try {
            try (PreparedStatement ps = c.prepareStatement(SQL_CHANGE_PASSWORD)) {
                ps.setString(1, newHashedPassword);
                ps.setString(2, userId);
                ps.executeUpdate();
            }
        } finally {
            if (ownConnection) {
                DBConnection.closeQuietly(c);
            }
        }
    }

    private AppUser mapRow(ResultSet rs) throws SQLException {
        AppUser user = new AppUser();

        user.setEmployeeId(rs.getString("user_id"));
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

        String empId = rs.getString("emp_id");

        if (empId != null) {
            Employee employee = new Employee();

            employee.setEmployeeId(empId);
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

            user.setEmployee(employee);
        }

        return user;
    }
}