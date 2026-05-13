package PetHotel.bus;

import java.sql.SQLException;

import PetHotel.dao.AppUserDAO;
import PetHotel.exception.AuthenticationException;
import PetHotel.exception.AuthorizationException;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.util.PasswordUtil;
import PetHotel.util.Role;

/**
 * AuthBUS — Nghiệp vụ xác thực và quản lý phiên đăng nhập.
 *
 * ═══════════════════════════════════════════════════════════════════
 * PHÂN QUYỀN:
 *   Tất cả actor: đăng nhập, đăng xuất, đổi mật khẩu của chính mình.
 *   Chỉ CEO / Admin: đặt lại mật khẩu của user khác.
 * ═══════════════════════════════════════════════════════════════════
 */
public class AuthBUS {

    private final AppUserDAO userDAO;

    /** User đang đăng nhập trong phiên hiện tại (session in-memory) */
    private AppUser currentUser;

    public AuthBUS() {
        this.userDAO = new AppUserDAO();
    }

    /** Constructor cho dependency injection / unit test */
    public AuthBUS(AppUserDAO userDAO) {
        this.userDAO = userDAO;
    }

    // ── Login ────────────────────────────────────────────────────

    /**
     * Đăng nhập hệ thống.
     */
    public AppUser login(String username, String rawPassword) {
        // 1. Validate input
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Tên đăng nhập không được để trống.");
        }
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new ValidationException("Mật khẩu không được để trống.");
        }

        try {
            // 2. Tìm user
            AppUser user = userDAO.findByUsername(username.trim());

            // 3. Kiểm tra tồn tại & ACTIVE
            if (user == null) {
                throw new AuthenticationException("Tên đăng nhập hoặc mật khẩu không đúng.");
            }
            if (!user.isActive()) {
                throw new AuthenticationException("Tài khoản đã bị khóa. Liên hệ quản trị viên để được hỗ trợ.");
            }

            // 4. Kiểm tra password bằng PasswordUtil
            if (!PasswordUtil.checkPassword(rawPassword, user.getPasswordHash())) {
                throw new AuthenticationException("Tên đăng nhập hoặc mật khẩu không đúng.");
            }

            // 5. Cập nhật last_login
            try {
                userDAO.updateLastLogin(user.getEmployeeId());
            } catch (Exception e) {
                System.err.println("[AuthBUS] Không cập nhật được last_login: " + e.getMessage());
            }

            // 6. Lưu session
            this.currentUser = user;
            return user;

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi đăng nhập.", e);
        }
    }

    // ── Logout ───────────────────────────────────────────────────

    /**
     * Đăng xuất — xóa session trong memory.
     */
    public void logout(String employeeId) {
        if (currentUser == null) {
            return;
        }
        if (!currentUser.getEmployeeId().equals(employeeId)) {
            throw new AuthorizationException("Bạn chỉ có thể đăng xuất tài khoản của chính mình.");
        }
        this.currentUser = null;
    }

    // ── Change Password ───────────────────────────────────────────

    /**
     * Đổi mật khẩu (người dùng tự đổi mật khẩu của chính mình).
     */
    public void changePassword(String employeeId, String oldRawPassword, String newRawPassword) {
        requireLogin();

        if (!currentUser.getEmployeeId().equals(employeeId)) {
            throw new AuthorizationException("Bạn không có quyền đổi mật khẩu của tài khoản khác.");
        }

        validatePasswordInput(newRawPassword);

        try {
            AppUser user = userDAO.findByEmployeeId(employeeId);
            if (user == null || !user.isActive()) {
                throw new AuthenticationException("Tài khoản không tồn tại hoặc đã bị khóa.");
            }

            // Kiểm tra mật khẩu cũ
            if (!PasswordUtil.checkPassword(oldRawPassword, user.getPasswordHash())) {
                throw new AuthenticationException("Mật khẩu cũ không đúng.");
            }

            // Không được đặt lại mật khẩu giống cũ
            if (PasswordUtil.checkPassword(newRawPassword, user.getPasswordHash())) {
                throw new ValidationException("Mật khẩu mới không được trùng mật khẩu cũ.");
            }

            // Hash và lưu
            String newHash = PasswordUtil.hashPassword(newRawPassword);
            userDAO.changePassword(employeeId, newHash, null);

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi đổi mật khẩu.", e);
        }
    }

    /**
     * Reset mật khẩu cho user khác (chỉ BRANCH_MANAGER trở lên).
     */
    public void resetPassword(String targetEmployeeId, String newRawPassword) {
        requireLogin();
        requireRole(Role.BRANCH_MANAGER);

        validatePasswordInput(newRawPassword);

        try {
            AppUser target = userDAO.findByEmployeeId(targetEmployeeId);
            if (target == null) {
                throw new PetHotel.exception.NotFoundException("Không tìm thấy tài khoản: " + targetEmployeeId);
            }

            String newHash = PasswordUtil.hashPassword(newRawPassword);
            userDAO.changePassword(targetEmployeeId, newHash, null);

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi reset mật khẩu.", e);
        }
    }

    // ── Session Helpers ───────────────────────────────────────────

    public AppUser getCurrentUser() { return currentUser; }

    public void requireLogin() {
        if (currentUser == null) {
            throw new AuthorizationException("Bạn cần đăng nhập để thực hiện thao tác này.");
        }
    }

    public void requireRole(Role required) {
        requireLogin();
        if (!currentUser.hasRole(required)) {
            throw new AuthorizationException("Bạn không có quyền thực hiện thao tác này. Yêu cầu quyền: " + required.name());
        }
    }

    // ── Password Utilities ────────────────────────────────────────

    private void validatePasswordInput(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new ValidationException("Mật khẩu mới không được để trống.");
        }
        if (rawPassword.length() < 8) {
            throw new ValidationException("Mật khẩu mới phải có ít nhất 8 ký tự.");
        }
        // Kiểm tra chứa ít nhất 1 chữ hoa, 1 chữ thường, 1 số
        boolean hasUpper = false, hasLower = false, hasDigit = false;
        for (char c : rawPassword.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }
        if (!hasUpper || !hasLower || !hasDigit) {
            throw new ValidationException("Mật khẩu phải chứa ít nhất 1 chữ hoa, 1 chữ thường và 1 số.");
        }
    }
}