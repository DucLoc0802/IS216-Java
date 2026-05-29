package PetHotel.bus;

import java.sql.SQLException;
import java.util.List;

import PetHotel.dao.AppUserDAO;
import PetHotel.exception.AuthorizationException;
import PetHotel.exception.DuplicateRecordException;
import PetHotel.exception.NotFoundException;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.model.Employee;
import PetHotel.util.PasswordUtil;
import PetHotel.util.Role;

public class AccountBUS {

    private final AppUserDAO appUserDAO;

    public AccountBUS() {
        this.appUserDAO = new AppUserDAO();
    }

    public AccountBUS(AppUserDAO appUserDAO) {
        this.appUserDAO = appUserDAO;
    }

    public List<AppUser> getAllAccounts() {
        try {
            return appUserDAO.findAll();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi lấy danh sách tài khoản.", e);
        }
    }

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

    public void createAccount(AppUser actor, String employeeId, String username,
                              String rawPassword, Role role) {
        if (actor == null || (!actor.hasRole(Role.ADMIN) && !actor.hasRole(Role.CEO))) {
            throw new AuthorizationException("Bạn không có quyền tạo tài khoản mới.");
        }

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

        try {
            if (appUserDAO.existsByUsername(username.trim())) {
                throw new DuplicateRecordException("Tên đăng nhập '" + username + "' đã tồn tại.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi kiểm tra tên đăng nhập.", e);
        }

        String hashedPassword = PasswordUtil.hashPassword(rawPassword);

        AppUser newUser = new AppUser();
        newUser.setEmployeeId(employeeId);
        newUser.setUserName(username.trim());
        newUser.setPasswordHash(hashedPassword);
        newUser.setRole(role);
        newUser.setActive(true);

        try {
            appUserDAO.insert(newUser);

            // Ghi audit log
            String actorFullName = (actor.getEmployee() != null) ? actor.getEmployee().getFullName() : actor.getUserName();
            AuditLogLocalService.log(actor.getEmployeeId(), actorFullName, "Tạo tài khoản", "Đã tạo tài khoản mới '" + username + "' cho nhân viên " + employeeId + " với vai trò " + role.getDisplayName());

        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg != null && msg.toUpperCase().contains("UQ_APP_USER_USERNAME")) {
                throw new DuplicateRecordException("Tên đăng nhập '" + username + "' đã tồn tại.");
            }
            if (msg != null && msg.toUpperCase().contains("FK_APP_USER_EMPLOYEE")) {
                throw new ValidationException("Mã nhân viên không tồn tại trong hệ thống.");
            }
            throw new RuntimeException("Lỗi database khi tạo tài khoản.", e);
        }
    }

    public void lockAccount(AppUser actor, String targetEmployeeId) {
        requireAdminOrCEO(actor);

        if (actor.getEmployeeId().equals(targetEmployeeId)) {
            throw new ValidationException("Bạn không thể khóa tài khoản của chính mình.");
        }

        try {
            AppUser target = appUserDAO.findByEmployeeId(targetEmployeeId);
            if (target == null) {
                throw new NotFoundException("Không tìm thấy tài khoản: " + targetEmployeeId);
            }
            if (target.getRole() == Role.ADMIN && actor.getRole() != Role.CEO) {
                throw new AuthorizationException("Chỉ CEO mới có thể khóa tài khoản Admin.");
            }
            appUserDAO.setActive(targetEmployeeId, false);

            // Ghi audit log
            String actorFullName = (actor.getEmployee() != null) ? actor.getEmployee().getFullName() : actor.getUserName();
            AuditLogLocalService.log(actor.getEmployeeId(), actorFullName, "Khóa tài khoản", "Đã khóa tài khoản của nhân viên: " + targetEmployeeId);

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi khóa tài khoản.", e);
        }
    }

    public void unlockAccount(AppUser actor, String targetEmployeeId) {
        requireAdminOrCEO(actor);

        try {
            AppUser target = appUserDAO.findByEmployeeId(targetEmployeeId);
            if (target == null) {
                throw new NotFoundException("Không tìm thấy tài khoản: " + targetEmployeeId);
            }
            appUserDAO.setActive(targetEmployeeId, true);

            // Ghi audit log
            String actorFullName = (actor.getEmployee() != null) ? actor.getEmployee().getFullName() : actor.getUserName();
            AuditLogLocalService.log(actor.getEmployeeId(), actorFullName, "Mở khóa tài khoản", "Đã mở khóa tài khoản của nhân viên: " + targetEmployeeId);

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi mở khóa tài khoản.", e);
        }
    }

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

            // Ghi audit log
            String actorFullName = (actor.getEmployee() != null) ? actor.getEmployee().getFullName() : actor.getUserName();
            AuditLogLocalService.log(actor.getEmployeeId(), actorFullName, "Đặt lại mật khẩu", "Đã đặt lại mật khẩu của nhân viên: " + targetEmployeeId);

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi đặt lại mật khẩu.", e);
        }
    }

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

            if (target.getRole() == Role.ADMIN && actor.getRole() != Role.CEO) {
                throw new AuthorizationException("Chỉ CEO mới có thể thay đổi vai trò của Admin.");
            }
            if (actor.getEmployeeId().equals(targetEmployeeId)) {
                throw new ValidationException("Bạn không thể tự thay đổi vai trò của chính mình.");
            }

            appUserDAO.updateRole(targetEmployeeId, newRole);

            // Ghi audit log
            String actorFullName = (actor.getEmployee() != null) ? actor.getEmployee().getFullName() : actor.getUserName();
            AuditLogLocalService.log(actor.getEmployeeId(), actorFullName, "Cập nhật vai trò", "Đã cập nhật vai trò của nhân viên " + targetEmployeeId + " thành " + newRole.getDisplayName());

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi cập nhật vai trò.", e);
        }
    }

    public int[] getAccountStats() {
        try {
            return appUserDAO.getAccountStats();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi lấy thống kê tài khoản.", e);
        }
    }

    public List<Employee> getEmployeesWithoutAccount() {
        try {
            return appUserDAO.findEmployeesWithoutAccount();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi lấy danh sách nhân viên chưa có tài khoản.", e);
        }
    }

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
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
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