package PetHotel.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

public class Employee {

    private String employeeId;
    private String branchId;
    private String fullName;
    private BigDecimal salary;
    private String email;
    private String phone;
    private OffsetDateTime hireDate;
    private String statusCode;
    private String note;
    private String userName;
    private String roleCode;
    private String roleName;

    public Employee() {}

    public Employee(String employeeId, String branchId, String fullName,
                    BigDecimal salary, String email, String phone,
                    OffsetDateTime hireDate, String statusCode, String note) {
        this.employeeId = employeeId;
        this.branchId = branchId;
        this.fullName = fullName;
        this.salary = salary;
        this.email = email;
        this.phone = phone;
        this.hireDate = hireDate;
        this.statusCode = statusCode;
        this.note = note;
    }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getBranchId() { return branchId; }
    public void setBranchId(String branchId) { this.branchId = branchId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public OffsetDateTime getHireDate() { return hireDate; }
    public void setHireDate(OffsetDateTime hireDate) { this.hireDate = hireDate; }

    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public boolean isWorking() {
        return "WORKING".equalsIgnoreCase(statusCode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        Employee employee = (Employee) o;
        return Objects.equals(employeeId, employee.employeeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId);
    }

    @Override
    public String toString() {
        return "Employee{id='" + employeeId + "', name='" + fullName
            + "', branch='" + branchId + "', status='" + statusCode + "'}";
    }
}
