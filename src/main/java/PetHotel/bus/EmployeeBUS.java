package PetHotel.bus;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

import PetHotel.dao.EmployeeDAO;
import PetHotel.exception.NotFoundException;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.model.Employee;

public class EmployeeBUS {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
        Pattern.compile("^[+\\d][\\d\\s\\-]{6,18}$");

    private final EmployeeDAO employeeDAO;

    public EmployeeBUS() {
        this.employeeDAO = new EmployeeDAO();
    }

    public EmployeeBUS(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }

    public List<Employee> searchEmployees(String keyword, String roleName, String statusLabel, String branchId) {
        try {
            return employeeDAO.search(keyword, mapRoleLabelToCode(roleName), mapStatusLabelToCode(statusLabel), branchId);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi tra cứu nhân viên.", e);
        }
    }

    public Employee getEmployeeDetail(String employeeId) {
        validateEmployeeId(employeeId);
        try {
            Employee employee = employeeDAO.findById(employeeId);
            if (employee == null) {
                throw new NotFoundException("Không tìm thấy nhân viên: " + employeeId);
            }
            return employee;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi tải hồ sơ nhân viên.", e);
        }
    }

    public Employee createEmployee(String branchId, String fullName, String email, String phone, String note) {
        validateBranch(branchId);
        validateFullName(fullName);
        validatePhone(phone);
        validateEmail(email);

        try {
            Employee employee = new Employee();
            employee.setEmployeeId(nextEmployeeId());
            employee.setBranchId(branchId.trim());
            employee.setFullName(fullName.trim());
            employee.setEmail(normalizeNullable(email));
            employee.setPhone(phone.trim());
            employee.setStatusCode("WORKING");
            employee.setNote(normalizeNullable(note));
            employeeDAO.insert(employee);
            return employeeDAO.findById(employee.getEmployeeId());
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi thêm nhân viên.", e);
        }
    }

    public Employee updateEmployee(String employeeId, String branchId, String fullName,
                                   String email, String phone, String statusCode, String note) {
        validateEmployeeId(employeeId);
        validateBranch(branchId);
        validateFullName(fullName);
        validatePhone(phone);
        validateEmail(email);
        validateStatusCode(statusCode);

        try {
            Employee employee = employeeDAO.findById(employeeId);
            if (employee == null) {
                throw new NotFoundException("Không tìm thấy nhân viên: " + employeeId);
            }

            employee.setBranchId(branchId.trim());
            employee.setFullName(fullName.trim());
            employee.setEmail(normalizeNullable(email));
            employee.setPhone(phone.trim());
            employee.setStatusCode(statusCode.trim());
            employee.setNote(normalizeNullable(note));

            employeeDAO.update(employee);
            return employeeDAO.findById(employeeId);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi cập nhật nhân viên.", e);
        }
    }

    public void deactivateEmployee(String employeeId) {
        updateEmployeeStatus(employeeId, "RESIGNED");
    }

    public void activateEmployee(String employeeId) {
        updateEmployeeStatus(employeeId, "WORKING");
    }

    public int[] getStats() {
        try {
            return employeeDAO.getStats();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi tải thống kê nhân viên.", e);
        }
    }

    public List<String> getBranches() {
        try {
            return employeeDAO.getDistinctBranches();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi tải danh sách chi nhánh.", e);
        }
    }

    public int[] getPerformanceSummary(String employeeId) {
        validateEmployeeId(employeeId);
        // Schema hiện tại chưa có bảng đánh giá rõ ràng để tính đủ KPI.
        // Tạm thời trả về số liệu 0 để UI vẫn hoạt động ổn định.
        return new int[] {0, 0};
    }

    public Employee getProfile(String employeeId) {
        return getEmployeeDetail(employeeId);
    }

    public Employee updateOwnProfile(AppUser actor, String fullName, String email, String phone, String note) {
        if (actor == null) {
            throw new ValidationException("Bạn cần đăng nhập để cập nhật thông tin cá nhân.");
        }
        return updateEmployee(
            actor.getEmployeeId(),
            actor.getEmployee() != null ? actor.getEmployee().getBranchId() : getEmployeeDetail(actor.getEmployeeId()).getBranchId(),
            fullName,
            email,
            phone,
            actor.getEmployee() != null ? actor.getEmployee().getStatusCode() : getEmployeeDetail(actor.getEmployeeId()).getStatusCode(),
            note
        );
    }

    private void updateEmployeeStatus(String employeeId, String statusCode) {
        validateEmployeeId(employeeId);
        validateStatusCode(statusCode);
        try {
            Employee employee = employeeDAO.findById(employeeId);
            if (employee == null) {
                throw new NotFoundException("Không tìm thấy nhân viên: " + employeeId);
            }
            employeeDAO.updateStatus(employeeId, statusCode);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi cập nhật trạng thái nhân viên.", e);
        }
    }

    private void validateEmployeeId(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new ValidationException("Mã nhân viên không hợp lệ.");
        }
    }

    private void validateBranch(String branchId) {
        if (branchId == null || branchId.trim().isEmpty()) {
            throw new ValidationException("Chi nhánh không được để trống.");
        }
    }

    private void validateFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Họ tên không được để trống.");
        }
        if (fullName.trim().length() > 120) {
            throw new ValidationException("Họ tên không được vượt quá 120 ký tự.");
        }
    }

    private void validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new ValidationException("Số điện thoại không được để trống.");
        }
        if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new ValidationException("Số điện thoại không đúng định dạng.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) return;
        if (email.trim().length() > 254) {
            throw new ValidationException("Email không được vượt quá 254 ký tự.");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Email không đúng định dạng.");
        }
    }

    private void validateStatusCode(String statusCode) {
        if (!"WORKING".equalsIgnoreCase(statusCode)
            && !"ON_LEAVE".equalsIgnoreCase(statusCode)
            && !"RESIGNED".equalsIgnoreCase(statusCode)) {
            throw new ValidationException("Trạng thái nhân viên không hợp lệ.");
        }
    }

    private String mapStatusLabelToCode(String statusLabel) {
        if (statusLabel == null || statusLabel.isBlank() || "Tất cả".equalsIgnoreCase(statusLabel)) {
            return null;
        }
        return switch (statusLabel.trim()) {
            case "Đang hoạt động" -> "WORKING";
            case "Ngưng hoạt động" -> "RESIGNED";
            default -> statusLabel;
        };
    }

    private String mapRoleLabelToCode(String roleLabel) {
        if (roleLabel == null || roleLabel.isBlank() || "Tất cả".equalsIgnoreCase(roleLabel)) {
            return null;
        }
        return switch (roleLabel.trim()) {
            case "Lễ Tân" -> "1";
            case "Nhân Viên Chăm Sóc", "Groomer", "Chăm Sóc Thú" -> "2";
            case "Quản Lý" -> "3";
            case "CEO" -> "4";
            case "Quản Trị Viên" -> "5";
            default -> null;
        };
    }

    private String normalizeNullable(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String nextEmployeeId() {
        return "EMP" + (System.currentTimeMillis() % 1_000_000_000L);
    }
}
