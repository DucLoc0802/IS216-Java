package PetHotel.bus;

import PetHotel.dao.AccountDAO;
import PetHotel.model.Account;

public class AccountBUS {
    
    // Khai báo đối tượng DAO để gọi hàm kết nối DB
    private AccountDAO accountDAO;

    public AccountBUS() {
        // Khởi tạo DAO khi lớp BUS được gọi
        this.accountDAO = new AccountDAO();
    }

    /**
     * Hàm xử lý logic đăng nhập
     * Nhận dữ liệu từ Giao diện (Controller), kiểm tra tính hợp lệ rồi mới đẩy xuống DAO
     */
    public Account login(String username, String password) {
        
        // 1. KIỂM TRA DỮ LIỆU ĐẦU VÀO (Validation)
        if (username == null || username.trim().isEmpty()) {
            System.err.println("BUS từ chối: Tên đăng nhập đang bị trống!");
            // Trả về null để giao diện biết là thất bại
            return null; 
        }
        
        if (password == null || password.trim().isEmpty()) {
            System.err.println("BUS từ chối: Mật khẩu đang bị trống!");
            return null;
        }

        // 2. GỌI XUỐNG TẦNG DAO NẾU DỮ LIỆU HỢP LỆ
        // Lúc này username và password đã chắc chắn có chữ, đưa cho DAO đi hỏi Oracle
        Account loginResult = (Account) accountDAO.checkLogin(username, password);
        
        // (Tùy chọn) Ghi log hệ thống
        if (loginResult != null) {
            System.out.println("BUS xác nhận: Đăng nhập thành công với quyền " + loginResult.getRole());
        } else {
            System.out.println("BUS xác nhận: Đăng nhập thất bại (Sai user hoặc pass)!");
        }

        return loginResult;
    }
}