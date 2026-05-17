package PetHotel.bus;

import java.sql.SQLException;
import java.util.List;

import PetHotel.dao.AppUserDAO;
import PetHotel.exception.AuthorizationException;
import PetHotel.exception.DuplicateRecordException;
import PetHotel.exception.NotFoundException;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.util.PasswordUtil;
import PetHotel.util.Role;

/**
 * AccountBUS — Nghiệp vụ quản lý tài khoản.
 *
 * Bao gồm: tạo tài khoản, tìm kiếm, khóa/mở khóa, đặt lại mật khẩu,
 * phân quyền, xem danh sách và thống kê.
 *
 * Luôn kiểm tra quyền trước khi thực hiện thao tác.
 */
public class AccountBUS {

    private final AppUserDAO appUserDAO;

    public AccountBUS() {
        this.appUserDAO = new AppUserDAO();
    }

    public AccountBUS(AppUserDAO appUserDAO) {
        this.appUserDAO = appUserDAO;
    }

    // ── Danh sách & Tìm kiếm ────────────────────────────────────

    /**
     * Lấy tất cả tài khoản (cần quyền ADMIN).
     */
    public List<AppUser> getAllAccounts() {
        try {
            return appUserDAO.findAll();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi lấy danh sách tài khoản.", e);
        }
    }

    /**
     * Tìm kiếm tài khoản theo từ khóa (username, fullName, email).
     */
    public List<AppUser> searchAccounts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllAccounts();
        }
        try {
            return appUserDAO.search(keyword.trim());
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi tìm kiếm tài khoản.", e);
        }
    }

    /**
     * Lấy thông tin chi tiết tài khoản theo employeeId.
     */
    public AppUser getAccountDetail(String employeeId) {
        try {
            AppUser user = appUserDAO.findByEmployeeId(employeeId);
            if (user == null) {
                throw new NotFoundException("Không tìm thấy tài khoản: " + employeeId);
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi lấy chi tiết tài khoản.", e);
        }
    }

    // ── Tạo tài khoản ───────────────────────────────────────────

    /**
     * Tạo tài khoản mới.
     *
     * @param actor      Người thực hiện (cần quyền ADMIN hoặc CEO)
     * @param employeeId Mã nhân viên (PK, FK → employee)
     * @param username   Tên đăng nhập
     * @param rawPassword Mật khẩu thô
     * @param role       Vai trò
     */
    public void createAccount(AppUser actor, String employeeId, String username,
                               String rawPassword, Role role) {
        // 1. Kiểm tra quyền — chỉ ADMIN/CEO mới được tạo
        if (actor == null || (!actor.hasRole(Role.ADMIN) && !actor.hasRole(Role.CEO))) {
            throw new AuthorizationException("Bạn không có quyền tạo tài khoản mới.");
        }

        // 2. Validate input
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Tên đăng nhập không được để trống.");
        }
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new ValidationException("Mật khẩu không được để trống.");
        }
        validatePasswordComplexity(rawPassword);
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new ValidationException("Mã nhân viên không được để trống.");
        }
        if (role == null) {
            throw new ValidationException("Vai trò không được để trống.");
        }

        // 3. Kiểm tra username đã tồn tại chưa
        try {
            if (appUserDAO.existsByUsername(username.trim())) {
                throw new DuplicateRecordException("Tên đăng nhập '" + username + "' đã tồn tại.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi kiểm tra username.", e);
        }

        // 4. Hash password
        String hashedPassword = PasswordUtil.hashPassword(rawPassword);

        // 5. Tạo AppUser và lưu
        AppUser newUser = new AppUser();
        newUser.setEmployeeId(employeeId);
        newUser.setUserName(username.trim());
        newUser.setPasswordHash(hashedPassword);
        newUser.setRole(role);
        newUser.setActive(true);

        try {
            appUserDAO.insert(newUser);
        } catch (SQLException e) {
            // Kiểm tra vi phạm unique constraint (username)
            String msg = e.getMessage();
            if (msg != null && msg.toUpperCase().contains("UQ_APP_USER_USERNAME")) {
                throw new DuplicateRecordException("Tên đăng nhập '" + username + "' đã tồn tại.");
            }
            // Kiểm tra vi phạm FK (employee_id không tồn tại)
            if (msg != null && msg.toUpperCase().contains("FK_APP_USER_EMPLOYEE")) {
                throw new ValidationException("Mã nhân viên không tồn tại trong hệ thống.");
            }
            throw new RuntimeException("Lỗi database khi tạo tài khoản.", e);
        }
    }

    // ── Khóa / Mở khóa ──────────────────────────────────────────

    /**
     * Khóa tài khoản (đặt is_active = 0).
     * Không thể khóa tài khoản của chính ADMIN đang đăng nhập.
     */
    public void lockAccount(AppUser actor, String targetEmployeeId) {
        requireAdminOrCEO(actor);

        // Không thể khóa chính mình
        if (actor.getEmployeeId().equals(targetEmployeeId)) {
            throw new ValidationException("Bạn không thể khóa tài khoản của chính mình.");
        }

        try {
            AppUser target = appUserDAO.findByEmployeeId(targetEmployeeId);
            if (target == null) {
                throw new NotFoundException("Không tìm thấy tài khoản: " + targetEmployeeId);
            }
            // ADMIN không thể khóa ADMIN khác (chỉ CEO mới được)
            if (target.getRole() == Role.ADMIN && actor.getRole() != Role.CEO) {
                throw new AuthorizationException("Chỉ CEO mới có thể khóa tài khoản Admin.");
            }
            appUserDAO.setActive(targetEmployeeId, false);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi khóa tài khoản.", e);
        }
    }

    /**
     * Mở khóa tài khoản (đặt is_active = 1).
     */
    public void unlockAccount(AppUser actor, String targetEmployeeId) {
        requireAdminOrCEO(actor);

        try {
            AppUser target = appUserDAO.findByEmployeeId(targetEmployeeId);
            if (target == null) {
                throw new NotFoundException("Không tìm thấy tài khoản: " + targetEmployeeId);
            }
            appUserDAO.setActive(targetEmployeeId, true);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi mở khóa tài khoản.", e);
        }
    }

    // ── Đặt lại mật khẩu ────────────────────────────────────────

    /**
     * Đặt lại mật khẩu cho tài khoản khác.
     * Chỉ ADMIN / CEO mới có quyền này.
     */
    public void resetPassword(AppUser actor, String targetEmployeeId, String newPassword) {
        requireAdminOrCEO(actor);

        validatePasswordComplexity(newPassword);

        try {
            AppUser target = appUserDAO.findByEmployeeId(targetEmployeeId);
            if (target == null) {
                throw new NotFoundException("Không tìm thấy tài khoản: " + targetEmployeeId);
            }
            String newHash = PasswordUtil.hashPassword(newPassword);
            appUserDAO.changePassword(targetEmployeeId, newHash, null);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi đặt lại mật khẩu.", e);
        }
    }

    // ── Phân quyền ──────────────────────────────────────────────

    /**
     * Thay đổi vai trò của tài khoản.
     * Chỉ ADMIN hoặc CEO mới được phân quyền.
     * ADMIN không thể thay đổi role của ADMIN khác (chỉ CEO).
     */
    public void updateRole(AppUser actor, String targetEmployeeId, Role newRole) {
        requireAdminOrCEO(actor);

        if (newRole == null) {
            throw new ValidationException("Vai trò không được để trống.");
        }

        try {
            AppUser target = appUserDAO.findByEmployeeId(targetEmployeeId);
            if (target == null) {
                throw new NotFoundException("Không tìm thấy tài khoản: " + targetEmployeeId);
            }

            // ADMIN không thể đổi role của ADMIN khác
            if (target.getRole() == Role.ADMIN && actor.getRole() != Role.CEO) {
                throw new AuthorizationException("Chỉ CEO mới có thể thay đổi vai trò của Admin.");
            }
            // Không thể tự hạ quyền của chính mình
            if (actor.getEmployeeId().equals(targetEmployeeId)) {
                throw new ValidationException("Bạn không thể tự thay đổi vai trò của chính mình.");
            }

            appUserDAO.updateRole(targetEmployeeId, newRole);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi cập nhật vai trò.", e);
        }
    }

    // ── Thống kê ────────────────────────────────────────────────

    /**
     * Lấy thống kê tài khoản: [total, active, locked, admin].
     */
    public int[] getAccountStats() {
        try {
            return appUserDAO.getAccountStats();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi lấy thống kê tài khoản.", e);
        }
    }

    // ── Helpers ─────────────────────────────────────────────────

    private void requireAdminOrCEO(AppUser actor) {
        if (actor == null) {
            throw new AuthorizationException("Bạn cần đăng nhập để thực hiện thao tác này.");
        }
        if (!actor.hasRole(Role.ADMIN) && !actor.hasRole(Role.CEO)) {
            throw new AuthorizationException("Bạn không có quyền thực hiện thao tác này. Yêu cầu quyền Admin trở lên.");
        }
    }

    private void validatePasswordComplexity(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Mật khẩu không được để trống.");
        }
        if (password.length() < 8) {
            throw new ValidationException("Mật khẩu phải có ít nhất 8 ký tự.");
        }
        boolean hasUpper = false, hasLower = false, hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }
        if (!hasUpper || !hasLower || !hasDigit) {
            throw new ValidationException("Mật khẩu phải chứa ít nhất 1 chữ hoa, 1 chữ thường và 1 số.");
        }
    }
}