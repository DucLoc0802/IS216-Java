package PetHotel.bus;

import PetHotel.dao.AppUserDAO;
import PetHotel.exception.AuthenticationException;
import PetHotel.exception.AuthorizationException;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.util.Role;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

/**
 * AuthBUS — Nghiệp vụ xác thực và quản lý phiên đăng nhập.
 *
 * ═══════════════════════════════════════════════════════════════════
 * ISSUES FOUND trong schema hiện tại:
 * ═══════════════════════════════════════════════════════════════════
 *
 * ISSUE #1 — Role '0' = CUSTOMER nhưng app_user FK → employee_id:
 *   Schema cho phép role_emp = '0' (CUSTOMER) nhưng app_user.employee_id
 *   lại FK sang bảng employee. Customer không phải employee → không thể
 *   có app_user với role CUSTOMER theo schema hiện tại.
 *   Hướng xử lý: Bỏ qua role '0' trong hệ thống desktop này.
 *   Role '0' có thể dành cho app mobile của khách hàng (hệ thống riêng).
 *
 * ISSUE #2 — Không có cột user_id riêng:
 *   PK của app_user là employee_id (không phải UUID riêng).
 *   Hướng xử lý: Dùng employee_id làm định danh session.
 *
 * ISSUE #3 — password_hash không rõ thuật toán:
 *   Schema không định nghĩa thuật toán hash. Hiện dùng SHA-256.
 *   TODO: Migrate sang BCrypt hoặc Argon2 cho production.
 *
 * ISSUE #4 — Không có bảng lưu session / logout token:
 *   Logout chỉ xử lý phía ứng dụng (xóa object trong memory).
 *   TODO: Thêm bảng user_session hoặc revoked_token nếu cần.
 * ═══════════════════════════════════════════════════════════════════
 *
 * PHÂN QUYỀN:
 *   Tất cả actor: đăng nhập, đăng xuất, đổi mật khẩu của chính mình.
 *   Chỉ CEO / Admin (role 4,5): đặt lại mật khẩu của user khác.
 */
public class AuthBUS {

    private final AppUserDAO userDAO;

    /** User đang đăng nhập trong phiên hiện tại (session in-memory) */
    private AppUser currentUser;

    // TODO: Nếu chuyển sang multi-user hoặc REST API, bỏ biến session này
    //       và dùng JWT / session store thay thế.

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
     *
     * Nghiệp vụ:
     *   1. Validate input (username/password không rỗng).
     *   2. Tìm user theo username.
     *   3. Kiểm tra tài khoản tồn tại và đang ACTIVE.
     *   4. Kiểm tra password (hash và so sánh).
     *   5. Cập nhật last_login.
     *   6. Lưu vào session in-memory.
     *
     * @param username tên đăng nhập (không phân biệt hoa/thường)
     * @param rawPassword mật khẩu thô (chưa hash)
     * @return AppUser đã đăng nhập
     * @throws ValidationException      nếu username hoặc password rỗng
     * @throws AuthenticationException  nếu sai thông tin hoặc tài khoản bị khóa
     * @throws RuntimeException         nếu lỗi DB không mong đợi
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
            // Trả về thông báo chung để tránh lộ thông tin (username có tồn tại không)
            if (user == null) {
                throw new AuthenticationException("Tên đăng nhập hoặc mật khẩu không đúng.");
            }
            if (!user.isActive()) {
                throw new AuthenticationException(
                    "Tài khoản đã bị khóa. Liên hệ quản trị viên để được hỗ trợ.");
            }

            // 4. Kiểm tra password
            String hashedInput = hashPassword(rawPassword);
            if (!hashedInput.equals(user.getPasswordHash())) {
                throw new AuthenticationException("Tên đăng nhập hoặc mật khẩu không đúng.");
            }

            // 5. Cập nhật last_login (thất bại không dừng quá trình đăng nhập)
            try {
                userDAO.updateLastLogin(user.getEmployeeId());
            } catch (Exception e) {
                // TODO: Log warning, không throw exception vì không critical
                System.err.println("[AuthBUS] Không cập nhật được last_login: " + e.getMessage());
            }

            // 6. Lưu session
            this.currentUser = user;
            return user;

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kết nối database khi đăng nhập.", e);
        }
    }

    // ── Logout ───────────────────────────────────────────────────

    /**
     * Đăng xuất — xóa session trong memory.
     *
     * Không có bảng session trong DB → logout chỉ xử lý phía app.
     * TODO: Nếu cần audit log, thêm ghi vào bảng audit_log tại đây.
     * TODO: Nếu dùng token-based auth, thêm revoke token vào DB.
     *
     * @param employeeId ID của user muốn logout (phải khớp currentUser)
     * @throws AuthorizationException nếu không phải người dùng hiện tại
     */
    public void logout(String employeeId) {
        if (currentUser == null) {
            // Chưa đăng nhập, không làm gì
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
     *
     * Nghiệp vụ:
     *   1. Kiểm tra đang đăng nhập.
     *   2. Validate input.
     *   3. Xác thực mật khẩu cũ.
     *   4. Kiểm tra mật khẩu mới hợp lệ (độ dài, không trùng cũ).
     *   5. Hash và lưu vào DB.
     *
     * @param employeeId      ID user đổi mật khẩu
     * @param oldRawPassword  mật khẩu cũ (chưa hash)
     * @param newRawPassword  mật khẩu mới (chưa hash)
     * @throws AuthorizationException  nếu không có quyền
     * @throws ValidationException     nếu mật khẩu không hợp lệ
     * @throws AuthenticationException nếu mật khẩu cũ sai
     */
    public void changePassword(String employeeId, String oldRawPassword, String newRawPassword) {
        // 1. Phải đang đăng nhập
        requireLogin();

        // Chỉ được đổi mật khẩu của chính mình
        // (CEO / Admin muốn reset của người khác → dùng resetPassword())
        if (!currentUser.getEmployeeId().equals(employeeId)) {
            throw new AuthorizationException(
                "Bạn không có quyền đổi mật khẩu của tài khoản khác.");
        }

        // 2. Validate input
        validatePasswordInput(newRawPassword);

        try {
            // 3. Lấy user từ DB để xác thực mật khẩu cũ
            AppUser user = userDAO.findByEmployeeId(employeeId);
            if (user == null || !user.isActive()) {
                throw new AuthenticationException("Tài khoản không tồn tại hoặc đã bị khóa.");
            }

            if (!hashPassword(oldRawPassword).equals(user.getPasswordHash())) {
                throw new AuthenticationException("Mật khẩu cũ không đúng.");
            }

            // 4. Không được đặt lại mật khẩu giống cũ
            if (hashPassword(newRawPassword).equals(user.getPasswordHash())) {
                throw new ValidationException("Mật khẩu mới không được trùng mật khẩu cũ.");
            }

            // 5. Hash và lưu
            String newHash = hashPassword(newRawPassword);
            userDAO.changePassword(employeeId, newHash, null);

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi đổi mật khẩu.", e);
        }
    }

    /**
     * Reset mật khẩu cho user khác (chỉ CEO / BRANCH_MANAGER trở lên).
     * Không yêu cầu biết mật khẩu cũ.
     *
     * @param targetEmployeeId  ID của user bị reset
     * @param newRawPassword    mật khẩu mới
     * @throws AuthorizationException nếu người gọi không đủ quyền
     * @throws ValidationException    nếu mật khẩu mới không hợp lệ
     */
    public void resetPassword(String targetEmployeeId, String newRawPassword) {
        requireLogin();
        requireRole(Role.BRANCH_MANAGER); // BRANCH_MANAGER (4) trở lên

        validatePasswordInput(newRawPassword);

        try {
            AppUser target = userDAO.findByEmployeeId(targetEmployeeId);
            if (target == null) {
                throw new PetHotel.exception.NotFoundException(
                    "Không tìm thấy tài khoản: " + targetEmployeeId);
            }

            String newHash = hashPassword(newRawPassword);
            userDAO.changePassword(targetEmployeeId, newHash, null);

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi reset mật khẩu.", e);
        }
    }

    // ── Session Helpers ───────────────────────────────────────────

    /**
     * @return User đang đăng nhập, null nếu chưa đăng nhập
     */
    public AppUser getCurrentUser() { return currentUser; }

    /**
     * Ném AuthorizationException nếu chưa đăng nhập.
     */
    public void requireLogin() {
        if (currentUser == null) {
            throw new AuthorizationException("Bạn cần đăng nhập để thực hiện thao tác này.");
        }
    }

    /**
     * Ném AuthorizationException nếu user hiện tại không đủ role yêu cầu.
     *
     * @param required role tối thiểu cần có
     */
    public void requireRole(Role required) {
        requireLogin();
        if (!currentUser.hasRole(required)) {
            throw new AuthorizationException(
                "Bạn không có quyền thực hiện thao tác này. Yêu cầu quyền: " + required.name());
        }
    }

    // ── Password Utilities ────────────────────────────────────────

    /**
     * Hash mật khẩu bằng SHA-256.
     *
     * TODO: Thay bằng BCrypt hoặc Argon2 để chống brute-force.
     *       BCrypt example: BCrypt.hashpw(rawPassword, BCrypt.gensalt(12))
     * TODO: Thêm salt nếu tiếp tục dùng SHA-256.
     *
     * @param rawPassword mật khẩu thô
     * @return chuỗi hex của SHA-256 hash
     */
    public static String hashPassword(String rawPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(rawPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 luôn có sẵn trong Java SE → không bao giờ xảy ra
            throw new RuntimeException("SHA-256 không khả dụng.", e);
        }
    }

    /**
     * Validate mật khẩu mới.
     * Quy tắc tối thiểu: ít nhất 8 ký tự, không được toàn khoảng trắng.
     * TODO: Thêm quy tắc phức tạp hơn (chữ hoa, số, ký tự đặc biệt).
     *
     * @param rawPassword mật khẩu cần kiểm tra
     * @throws ValidationException nếu không hợp lệ
     */
    private void validatePasswordInput(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new ValidationException("Mật khẩu mới không được để trống.");
        }
        if (rawPassword.length() < 8) {
            throw new ValidationException("Mật khẩu mới phải có ít nhất 8 ký tự.");
        }
        // TODO: Kiểm tra thêm độ phức tạp (uppercase, digit, special char)
    }
}
