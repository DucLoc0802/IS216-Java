package PetHotel.bus;

import java.math.BigDecimal;
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
            throw new RuntimeException("Loi database khi tra cuu nhan vien.", e);
        }
    }

    public Employee getEmployeeDetail(String employeeId) {
        validateEmployeeId(employeeId);
        try {
            Employee employee = employeeDAO.findById(employeeId);
            if (employee == null) {
                throw new NotFoundException("Khong tim thay nhan vien: " + employeeId);
            }
            return employee;
        } catch (SQLException e) {
            throw new RuntimeException("Loi database khi tai ho so nhan vien.", e);
        }
    }

    public Employee createEmployee(String branchId, String fullName, String salaryText,
                                   String email, String phone, String note) {
        validateBranch(branchId);
        validateFullName(fullName);
        BigDecimal salary = parseAndValidateSalary(salaryText);
        validatePhone(phone);
        validateEmail(email);

        try {
            Employee employee = new Employee();
            employee.setEmployeeId(nextEmployeeId());
            employee.setBranchId(branchId.trim());
            employee.setFullName(fullName.trim());
            employee.setSalary(salary);
            employee.setEmail(normalizeNullable(email));
            employee.setPhone(phone.trim());
            employee.setStatusCode("WORKING");
            employee.setNote(normalizeNullable(note));
            employeeDAO.insert(employee);
            return employeeDAO.findById(employee.getEmployeeId());
        } catch (SQLException e) {
            throw new RuntimeException("Loi database khi them nhan vien: " + rootCauseMessage(e), e);
        }
    }

    public Employee updateEmployee(String employeeId, String branchId, String fullName,
                                   String salaryText, String email, String phone,
                                   String statusCode, String note) {
        validateEmployeeId(employeeId);
        validateBranch(branchId);
        validateFullName(fullName);
        BigDecimal salary = parseAndValidateSalary(salaryText);
        validatePhone(phone);
        validateEmail(email);
        validateStatusCode(statusCode);

        try {
            Employee employee = employeeDAO.findById(employeeId);
            if (employee == null) {
                throw new NotFoundException("Khong tim thay nhan vien: " + employeeId);
            }

            employee.setBranchId(branchId.trim());
            employee.setFullName(fullName.trim());
            employee.setSalary(salary);
            employee.setEmail(normalizeNullable(email));
            employee.setPhone(phone.trim());
            employee.setStatusCode(statusCode.trim());
            employee.setNote(normalizeNullable(note));

            employeeDAO.update(employee);
            return employeeDAO.findById(employeeId);
        } catch (SQLException e) {
            throw new RuntimeException("Loi database khi cap nhat nhan vien: " + rootCauseMessage(e), e);
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
            throw new RuntimeException("Loi database khi tai thong ke nhan vien.", e);
        }
    }

    public List<String> getBranches() {
        try {
            return employeeDAO.getDistinctBranches();
        } catch (SQLException e) {
            throw new RuntimeException("Loi database khi tai danh sach chi nhanh.", e);
        }
    }

    public int[] getPerformanceSummary(String employeeId) {
        validateEmployeeId(employeeId);
        return new int[] {0, 0};
    }

    public Employee getProfile(String employeeId) {
        return getEmployeeDetail(employeeId);
    }

    public Employee updateOwnProfile(AppUser actor, String fullName, String email, String phone, String note) {
        if (actor == null) {
            throw new ValidationException("Ban can dang nhap de cap nhat thong tin ca nhan.");
        }

        Employee currentEmployee = actor.getEmployee() != null
            ? actor.getEmployee()
            : getEmployeeDetail(actor.getEmployeeId());

        return updateEmployee(
            actor.getEmployeeId(),
            currentEmployee.getBranchId(),
            fullName,
            formatSalary(currentEmployee.getSalary()),
            email,
            phone,
            currentEmployee.getStatusCode(),
            note
        );
    }

    private void updateEmployeeStatus(String employeeId, String statusCode) {
        validateEmployeeId(employeeId);
        validateStatusCode(statusCode);
        try {
            Employee employee = employeeDAO.findById(employeeId);
            if (employee == null) {
                throw new NotFoundException("Khong tim thay nhan vien: " + employeeId);
            }
            employeeDAO.updateStatus(employeeId, statusCode);
        } catch (SQLException e) {
            throw new RuntimeException("Loi database khi cap nhat trang thai nhan vien.", e);
        }
    }

    private void validateEmployeeId(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new ValidationException("Ma nhan vien khong hop le.");
        }
    }

    private void validateBranch(String branchId) {
        if (branchId == null || branchId.trim().isEmpty()) {
            throw new ValidationException("Chi nhanh khong duoc de trong.");
        }
    }

    private void validateFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Ho ten khong duoc de trong.");
        }
        if (fullName.trim().length() > 120) {
            throw new ValidationException("Ho ten khong duoc vuot qua 120 ky tu.");
        }
    }

    private void validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new ValidationException("So dien thoai khong duoc de trong.");
        }
        if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new ValidationException("So dien thoai khong dung dinh dang.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) return;
        if (email.trim().length() > 254) {
            throw new ValidationException("Email khong duoc vuot qua 254 ky tu.");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Email khong dung dinh dang.");
        }
    }

    private void validateStatusCode(String statusCode) {
        if (!"WORKING".equalsIgnoreCase(statusCode)
            && !"ON_LEAVE".equalsIgnoreCase(statusCode)
            && !"RESIGNED".equalsIgnoreCase(statusCode)) {
            throw new ValidationException("Trang thai nhan vien khong hop le.");
        }
    }

    private BigDecimal parseAndValidateSalary(String salaryText) {
        if (salaryText == null || salaryText.trim().isEmpty()) {
            throw new ValidationException("Luong khong duoc de trong.");
        }

        String normalized = salaryText.trim().replace(",", "");
        try {
            BigDecimal salary = new BigDecimal(normalized);
            if (salary.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("Luong khong duoc am.");
            }
            if (salary.scale() > 2) {
                throw new ValidationException("Luong toi da 2 chu so thap phan.");
            }
            if (salary.precision() > 12) {
                throw new ValidationException("Luong vuot qua gioi han luu tru.");
            }
            return salary;
        } catch (NumberFormatException ex) {
            throw new ValidationException("Luong khong dung dinh dang.");
        }
    }

    private String mapStatusLabelToCode(String statusLabel) {
        if (statusLabel == null || statusLabel.isBlank() || "Tat ca".equalsIgnoreCase(statusLabel)) {
            return null;
        }
        return switch (statusLabel.trim()) {
            case "Dang hoat dong" -> "WORKING";
            case "Ngung hoat dong" -> "RESIGNED";
            default -> statusLabel;
        };
    }

    private String mapRoleLabelToCode(String roleLabel) {
        if (roleLabel == null || roleLabel.isBlank() || "Tat ca".equalsIgnoreCase(roleLabel)) {
            return null;
        }
        return switch (roleLabel.trim()) {
            case "Le Tan" -> "1";
            case "Nhan Vien Cham Soc", "Groomer", "Cham Soc Thu" -> "2";
            case "Quan Ly" -> "3";
            case "CEO" -> "4";
            case "Quan Tri Vien" -> "5";
            default -> null;
        };
    }

    private String normalizeNullable(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String nextEmployeeId() {
        return "EMP" + String.format("%07d", System.currentTimeMillis() % 10_000_000L);
    }

    private String formatSalary(BigDecimal salary) {
        return salary == null ? null : salary.stripTrailingZeros().toPlainString();
    }

    private String rootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() != null ? current.getMessage() : throwable.getMessage();
    }
}
