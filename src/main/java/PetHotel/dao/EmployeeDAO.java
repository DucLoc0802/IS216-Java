package PetHotel.dao;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import PetHotel.model.Employee;
import PetHotel.util.DBConnection;

public class EmployeeDAO {

    private static final String SELECT_BASE =
        "SELECT e.employee_id, e.branch_id, e.full_name, e.salary, e.email, e.phone, e.hire_date, e.status_code, e.note, " +
        "       au.user_name, au.role_emp " +
        "FROM employee e " +
        "LEFT JOIN app_user au ON au.employee_id = e.employee_id ";

    private static final String SQL_FIND_BY_ID =
        SELECT_BASE + "WHERE e.employee_id = ?";

    private static final String SQL_FIND_ALL =
        SELECT_BASE + "ORDER BY e.hire_date DESC NULLS LAST, e.full_name";

    private static final String SQL_SEARCH =
        SELECT_BASE +
        "WHERE (? IS NULL OR LOWER(e.full_name) LIKE LOWER(?) OR LOWER(e.employee_id) LIKE LOWER(?) OR e.phone LIKE ?) " +
        "  AND (? IS NULL OR au.role_emp = ?) " +
        "  AND (? IS NULL OR e.status_code = ?) " +
        "  AND (? IS NULL OR e.branch_id = ?) " +
        "ORDER BY e.hire_date DESC NULLS LAST, e.full_name";

    private static final String SQL_INSERT =
        "INSERT INTO employee (employee_id, branch_id, full_name, salary, email, phone, hire_date, status_code, note) " +
        "VALUES (?, ?, ?, ?, ?, ?, SYSTIMESTAMP, ?, ?)";

    private static final String SQL_UPDATE =
        "UPDATE employee SET branch_id = ?, full_name = ?, salary = ?, email = ?, phone = ?, status_code = ?, note = ? " +
        "WHERE employee_id = ?";

    private static final String SQL_UPDATE_PROFILE =
        "UPDATE employee SET full_name = ?, email = ?, phone = ?, note = ? WHERE employee_id = ?";

    private static final String SQL_UPDATE_STATUS =
        "UPDATE employee SET status_code = ? WHERE employee_id = ?";

    private static final String SQL_COUNT_STATS =
        "SELECT COUNT(*) AS total, " +
        "       SUM(CASE WHEN e.status_code = 'WORKING' THEN 1 ELSE 0 END) AS active, " +
        "       SUM(CASE WHEN e.status_code <> 'WORKING' THEN 1 ELSE 0 END) AS inactive, " +
        "       SUM(CASE WHEN au.role_emp = '2' THEN 1 ELSE 0 END) AS groomers " +
        "FROM employee e LEFT JOIN app_user au ON au.employee_id = e.employee_id";

    private static final String SQL_DISTINCT_BRANCH =
        "SELECT branch_id FROM branch WHERE is_active = 1 ORDER BY branch_id";

    public Employee findById(String employeeId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setString(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<Employee> findAll() throws SQLException {
        List<Employee> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Employee> search(String keyword, String roleCode, String statusCode, String branchId) throws SQLException {
        List<Employee> list = new ArrayList<>();
        String normalizedKeyword = normalizeKeyword(keyword);
        String pattern = normalizedKeyword == null ? null : "%" + normalizedKeyword + "%";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SEARCH)) {
            ps.setString(1, normalizedKeyword);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            ps.setString(5, normalizeNullable(roleCode));
            ps.setString(6, normalizeNullable(roleCode));
            ps.setString(7, normalizeNullable(statusCode));
            ps.setString(8, normalizeNullable(statusCode));
            ps.setString(9, normalizeNullable(branchId));
            ps.setString(10, normalizeNullable(branchId));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }

        return list;
    }

    public void insert(Employee employee) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
            ps.setString(1, employee.getEmployeeId());
            ps.setString(2, employee.getBranchId());
            ps.setString(3, employee.getFullName());
            ps.setBigDecimal(4, employee.getSalary());
            ps.setString(5, employee.getEmail());
            ps.setString(6, employee.getPhone());
            ps.setString(7, employee.getStatusCode());
            ps.setString(8, employee.getNote());
            ps.executeUpdate();
        }
    }

    public int update(Employee employee) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
            ps.setString(1, employee.getBranchId());
            ps.setString(2, employee.getFullName());
            ps.setBigDecimal(3, employee.getSalary());
            ps.setString(4, employee.getEmail());
            ps.setString(5, employee.getPhone());
            ps.setString(6, employee.getStatusCode());
            ps.setString(7, employee.getNote());
            ps.setString(8, employee.getEmployeeId());
            return ps.executeUpdate();
        }
    }

    public int updateProfile(Employee employee) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_PROFILE)) {
            ps.setString(1, employee.getFullName());
            ps.setString(2, employee.getEmail());
            ps.setString(3, employee.getPhone());
            ps.setString(4, employee.getNote());
            ps.setString(5, employee.getEmployeeId());
            return ps.executeUpdate();
        }
    }

    public int updateStatus(String employeeId, String statusCode) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_STATUS)) {
            ps.setString(1, statusCode);
            ps.setString(2, employeeId);
            return ps.executeUpdate();
        }
    }

    public int[] getStats() throws SQLException {
        int[] stats = new int[4];
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT_STATS);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                stats[0] = rs.getInt("total");
                stats[1] = rs.getInt("active");
                stats[2] = rs.getInt("inactive");
                stats[3] = rs.getInt("groomers");
            }
        }
        return stats;
    }

    public List<String> getDistinctBranches() throws SQLException {
        List<String> branches = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DISTINCT_BRANCH);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                branches.add(rs.getString(1));
            }
        }
        return branches;
    }

    private Employee mapRow(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setEmployeeId(rs.getString("employee_id"));
        employee.setBranchId(rs.getString("branch_id"));
        employee.setFullName(rs.getString("full_name"));
        employee.setSalary(rs.getBigDecimal("salary"));
        employee.setEmail(rs.getString("email"));
        employee.setPhone(rs.getString("phone"));
        employee.setStatusCode(rs.getString("status_code"));
        employee.setUserName(rs.getString("user_name"));
        employee.setRoleCode(rs.getString("role_emp"));

        Timestamp hireDate = rs.getTimestamp("hire_date");
        if (hireDate != null) {
            employee.setHireDate(hireDate.toInstant().atOffset(java.time.ZoneOffset.UTC));
        }

        Clob noteClob = rs.getClob("note");
        if (noteClob != null) {
            employee.setNote(noteClob.getSubString(1, (int) noteClob.length()));
        }

        return employee;
    }

    private String normalizeKeyword(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeNullable(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() || "Tất cả".equalsIgnoreCase(trimmed) || "Tất cả CN".equalsIgnoreCase(trimmed)
            ? null
            : trimmed;
    }

    public int getBookingCountByEmployee(String employeeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE created_by_emp = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int getGroomingCountByEmployee(String employeeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM booking_services WHERE employee_id = ? AND status = 'DONE'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
