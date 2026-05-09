package PetHotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import PetHotel.model.AppUser;
import PetHotel.model.Employee;
import PetHotel.util.DBConnection;
import PetHotel.util.Role;

public class AuthDAO {

    /**
     * Tìm kiếm người dùng qua username để phục vụ logic đăng nhập.
     * Sử dụng JOIN để lấy luôn thông tin nhân viên kèm theo.
     */
    public AppUser findByUsername(String username) {
        // Query lấy thông tin từ APP_USER (U) và JOIN với EMPLOYEE (E)
        String sql = "SELECT u.*, e.full_name, e.email, e.phone, e.branch_id " +
                     "FROM app_user u " +
                     "JOIN employee e ON u.employee_id = e.employee_id " +
                     "WHERE u.user_name = ? AND u.is_active = 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAppUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Trong thực tế nên dùng Logger
        }
        return null;
    }

    /**
     * Cập nhật thời gian đăng nhập cuối cùng
     */
    public void updateLastLogin(String employeeId) {
        String sql = "UPDATE app_user SET last_login = CURRENT_TIMESTAMP WHERE employee_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, employeeId);
            ps.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Đổi mật khẩu
     */
    public boolean updatePassword(String employeeId, String newPasswordHash) {
        String sql = "UPDATE app_user SET password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE employee_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, newPasswordHash);
            ps.setString(2, employeeId);
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Helper: Chuyển đổi ResultSet thành Object AppUser hoàn chỉnh
    private AppUser mapResultSetToAppUser(ResultSet rs) throws SQLException {
        AppUser user = new AppUser();
        user.setEmployeeId(rs.getString("employee_id"));
        user.setUserName(rs.getString("user_name"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setActive(rs.getInt("is_active") == 1);
        
        // Map Role từ String dbValue sang Enum
        user.setRole(Role.fromDbValue(rs.getString("role_emp")));

        // Map thông tin Employee đi kèm
        Employee emp = new Employee();
        emp.setEmployeeId(rs.getString("employee_id"));
        emp.setFullName(rs.getString("full_name"));
        emp.setEmail(rs.getString("email"));
        emp.setPhone(rs.getString("phone"));
        emp.setBranchId(rs.getString("branch_id"));
        
        user.setEmployee(emp);
        
        return user;
    }
}