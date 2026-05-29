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
            throw new RuntimeException("Loi database khi lay danh sach tai khoan.", e);
        }
    }

    public List<AppUser> searchAccounts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllAccounts();
        }
        try {
            return appUserDAO.search(keyword.trim());
        } catch (SQLException e) {
            throw new RuntimeException("Loi database khi tim kiem tai khoan.", e);
        }
    }

    public AppUser getAccountDetail(String employeeId) {
        try {
            AppUser user = appUserDAO.findByEmployeeId(employeeId);
            if (user == null) {
                throw new NotFoundException("Khong tim thay tai khoan: " + employeeId);
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Loi database khi lay chi tiet tai khoan.", e);
        }
    }

    public void createAccount(AppUser actor, String employeeId, String username,
                              String rawPassword, Role role) {
        if (actor == null || (!actor.hasRole(Role.ADMIN) && !actor.hasRole(Role.CEO))) {
            throw new AuthorizationException("Ban khong co quyen tao tai khoan moi.");
        }

        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Ten dang nhap khong duoc de trong.");
        }
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new ValidationException("Mat khau khong duoc de trong.");
        }
        validatePasswordComplexity(rawPassword);
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new ValidationException("Ma nhan vien khong duoc de trong.");
        }
        if (role == null) {
            throw new ValidationException("Vai tro khong duoc de trong.");
        }

        try {
            if (appUserDAO.existsByUsername(username.trim())) {
                throw new DuplicateRecordException("Ten dang nhap '" + username + "' da ton tai.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Loi database khi kiem tra username.", e);
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
                throw new DuplicateRecordException("Ten dang nhap '" + username + "' da ton tai.");
            }
            if (msg != null && msg.toUpperCase().contains("FK_APP_USER_EMPLOYEE")) {
                throw new ValidationException("Ma nhan vien khong ton tai trong he thong.");
            }
            throw new RuntimeException("Loi database khi tao tai khoan.", e);
        }
    }

    public void lockAccount(AppUser actor, String targetEmployeeId) {
        requireAdminOrCEO(actor);

        if (actor.getEmployeeId().equals(targetEmployeeId)) {
            throw new ValidationException("Ban khong the khoa tai khoan cua chinh minh.");
        }

        try {
            AppUser target = appUserDAO.findByEmployeeId(targetEmployeeId);
            if (target == null) {
                throw new NotFoundException("Khong tim thay tai khoan: " + targetEmployeeId);
            }
            if (target.getRole() == Role.ADMIN && actor.getRole() != Role.CEO) {
                throw new AuthorizationException("Chi CEO moi co the khoa tai khoan Admin.");
            }
            appUserDAO.setActive(targetEmployeeId, false);

            // Ghi audit log
            String actorFullName = (actor.getEmployee() != null) ? actor.getEmployee().getFullName() : actor.getUserName();
            AuditLogLocalService.log(actor.getEmployeeId(), actorFullName, "Khóa tài khoản", "Đã khóa tài khoản của nhân viên: " + targetEmployeeId);

        } catch (SQLException e) {
            throw new RuntimeException("Loi database khi khoa tai khoan.", e);
        }
    }

    public void unlockAccount(AppUser actor, String targetEmployeeId) {
        requireAdminOrCEO(actor);

        try {
            AppUser target = appUserDAO.findByEmployeeId(targetEmployeeId);
            if (target == null) {
                throw new NotFoundException("Khong tim thay tai khoan: " + targetEmployeeId);
            }
            appUserDAO.setActive(targetEmployeeId, true);

            // Ghi audit log
            String actorFullName = (actor.getEmployee() != null) ? actor.getEmployee().getFullName() : actor.getUserName();
            AuditLogLocalService.log(actor.getEmployeeId(), actorFullName, "Mở khóa tài khoản", "Đã mở khóa tài khoản của nhân viên: " + targetEmployeeId);

        } catch (SQLException e) {
            throw new RuntimeException("Loi database khi mo khoa tai khoan.", e);
        }
    }

    public void resetPassword(AppUser actor, String targetEmployeeId, String newPassword) {
        requireAdminOrCEO(actor);
        validatePasswordComplexity(newPassword);

        try {
            AppUser target = appUserDAO.findByEmployeeId(targetEmployeeId);
            if (target == null) {
                throw new NotFoundException("Khong tim thay tai khoan: " + targetEmployeeId);
            }
            String newHash = PasswordUtil.hashPassword(newPassword);
            appUserDAO.changePassword(targetEmployeeId, newHash, null);

            // Ghi audit log
            String actorFullName = (actor.getEmployee() != null) ? actor.getEmployee().getFullName() : actor.getUserName();
            AuditLogLocalService.log(actor.getEmployeeId(), actorFullName, "Đặt lại mật khẩu", "Đã đặt lại mật khẩu của nhân viên: " + targetEmployeeId);

        } catch (SQLException e) {
            throw new RuntimeException("Loi database khi dat lai mat khau.", e);
        }
    }

    public void updateRole(AppUser actor, String targetEmployeeId, Role newRole) {
        requireAdminOrCEO(actor);

        if (newRole == null) {
            throw new ValidationException("Vai tro khong duoc de trong.");
        }

        try {
            AppUser target = appUserDAO.findByEmployeeId(targetEmployeeId);
            if (target == null) {
                throw new NotFoundException("Khong tim thay tai khoan: " + targetEmployeeId);
            }

            if (target.getRole() == Role.ADMIN && actor.getRole() != Role.CEO) {
                throw new AuthorizationException("Chi CEO moi co the thay doi vai tro cua Admin.");
            }
            if (actor.getEmployeeId().equals(targetEmployeeId)) {
                throw new ValidationException("Ban khong the tu thay doi vai tro cua chinh minh.");
            }

            appUserDAO.updateRole(targetEmployeeId, newRole);

            // Ghi audit log
            String actorFullName = (actor.getEmployee() != null) ? actor.getEmployee().getFullName() : actor.getUserName();
            AuditLogLocalService.log(actor.getEmployeeId(), actorFullName, "Cập nhật vai trò", "Đã cập nhật vai trò của nhân viên " + targetEmployeeId + " thành " + newRole.getDisplayName());

        } catch (SQLException e) {
            throw new RuntimeException("Loi database khi cap nhat vai tro.", e);
        }
    }

    public int[] getAccountStats() {
        try {
            return appUserDAO.getAccountStats();
        } catch (SQLException e) {
            throw new RuntimeException("Loi database khi lay thong ke tai khoan.", e);
        }
    }

    public List<Employee> getEmployeesWithoutAccount() {
        try {
            return appUserDAO.findEmployeesWithoutAccount();
        } catch (SQLException e) {
            throw new RuntimeException("Loi database khi lay danh sach nhan vien chua co tai khoan.", e);
        }
    }

    private void requireAdminOrCEO(AppUser actor) {
        if (actor == null) {
            throw new AuthorizationException("Ban can dang nhap de thuc hien thao tac nay.");
        }
        if (!actor.hasRole(Role.ADMIN) && !actor.hasRole(Role.CEO)) {
            throw new AuthorizationException("Ban khong co quyen thuc hien thao tac nay. Yeu cau quyen Admin tro len.");
        }
    }

    private void validatePasswordComplexity(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Mat khau khong duoc de trong.");
        }
        if (password.length() < 8) {
            throw new ValidationException("Mat khau phai co it nhat 8 ky tu.");
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
            throw new ValidationException("Mat khau phai chua it nhat 1 chu hoa, 1 chu thuong va 1 so.");
        }
    }
}
