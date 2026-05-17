package PetHotel.model;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Employee — Ánh xạ bảng EMPLOYEE.
 *
 * Schema:
 *   employee_id  VARCHAR2(10)    PK
 *   branch_id    VARCHAR2(10)    NOT NULL, FK → branch
 *   full_name    NVARCHAR2(120)  NOT NULL
 *   email        VARCHAR2(254)   UNIQUE
 *   phone        VARCHAR2(20)    NOT NULL, UNIQUE
 *   hire_date    TIMESTAMP(6) WITH TIME ZONE
 *   status_code  NVARCHAR2(20)   CHECK IN ('WORKING','ON_LEAVE','RESIGNED')
 *   note         CLOB
 */
public class Employee {

    /** Mã nhân viên, PK */
    private String employeeId;

    /** Chi nhánh nhân viên thuộc về */
    private String branchId;

    /** Họ tên đầy đủ */
    private String fullName;

    /** Email, có thể null, unique */
    private String email;

    /** Số điện thoại, NOT NULL, unique */
    private String phone;

    /** Ngày vào làm */
    private OffsetDateTime hireDate;

    /**
     * Trạng thái làm việc.
     * CHECK IN ('WORKING', 'ON_LEAVE', 'RESIGNED')
     */
    private String statusCode;

    /** Ghi chú thêm (CLOB) */
    private String note;

    // ── Constructors ──────────────────────────────────────────────

    public Employee() {}

    /** Constructor đầy đủ cho DAO mapping */
    public Employee(String employeeId, String branchId, String fullName,
                    String email, String phone, OffsetDateTime hireDate,
                    String statusCode, String note) {
        this.employeeId = employeeId;
        this.branchId   = branchId;
        this.fullName   = fullName;
        this.email      = email;
        this.phone      = phone;
        this.hireDate   = hireDate;
        this.statusCode = statusCode;
        this.note       = note;
    }

    // ── Getters / Setters ─────────────────────────────────────────

    public String getEmployeeId()              { return employeeId; }
    public void setEmployeeId(String v)        { this.employeeId = v; }

    public String getBranchId()                { return branchId; }
    public void setBranchId(String v)          { this.branchId = v; }

    public String getFullName()                { return fullName; }
    public void setFullName(String v)          { this.fullName = v; }

    public String getEmail()                   { return email; }
    public void setEmail(String v)             { this.email = v; }

    public String getPhone()                   { return phone; }
    public void setPhone(String v)             { this.phone = v; }

    public OffsetDateTime getHireDate()        { return hireDate; }
    public void setHireDate(OffsetDateTime v)  { this.hireDate = v; }

    public String getStatusCode()              { return statusCode; }
    public void setStatusCode(String v)        { this.statusCode = v; }

    public String getNote()                    { return note; }
    public void setNote(String v)              { this.note = v; }

    /** @return true nếu nhân viên đang làm việc (WORKING) */
    public boolean isWorking() { return "WORKING".equalsIgnoreCase(statusCode); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        return Objects.equals(employeeId, ((Employee) o).employeeId);
    }

    @Override public int hashCode() { return Objects.hash(employeeId); }

    @Override
    public String toString() {
        return "Employee{id='" + employeeId + "', name='" + fullName
             + "', branch='" + branchId + "', status='" + statusCode + "'}";
    }
}
