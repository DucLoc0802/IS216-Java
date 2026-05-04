package PetHotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import PetHotel.model.Account;
import PetHotel.util.DBConnection;

public class AccountDAO {

    /**
     * Hàm kiểm tra đăng nhập
     * Trả về đối tượng Account nếu đăng nhập thành công, trả về null nếu thất bại
     */
    public Account checkLogin(String username, String password) {
        Account account = null;
        
        // Câu lệnh SQL JOIN 2 bảng để lấy thông tin đăng nhập và Tên đầy đủ
        String sql = "SELECT a.user_name, a.password_hash, a.role_emp, e.full_name "
                   + "FROM app_user a "
                   + "JOIN employee e ON a.employee_id = e.employee_id "
                   + "WHERE a.user_name = ? AND a.password_hash = ? AND a.is_active = 1";

        // Sử dụng try-with-resources để tự động đóng kết nối (Connection, PreparedStatement, ResultSet)
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            // Truyền tham số username và password vào các dấu chấm hỏi (?)
            pst.setString(1, username);
            pst.setString(2, password);
            
            try (ResultSet rs = pst.executeQuery()) {
                // Nếu có kết quả trả về -> Tài khoản và mật khẩu đúng
                if (rs.next()) {
                    account = new Account();
                    account.setUsername(rs.getString("user_name"));
                    account.setPassword(rs.getString("password_hash"));
                    account.setRole(rs.getString("role_emp"));
                    account.setFullName(rs.getString("full_name"));
                }
            }
            
        } catch (Exception e) {
            System.err.println("Lỗi khi kiểm tra đăng nhập AccountDAO: " + e.getMessage());
            e.printStackTrace();
        }
        
        return account; // Nếu sai thông tin, sẽ trả về null
    }
}