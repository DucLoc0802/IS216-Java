package PetHotel.model;

import java.time.OffsetDateTime;
import java.util.Objects;

import PetHotel.util.Role;

/**
 * AppUser — Ánh xạ bảng APP_USER.
 *
 * Schema:
 *   employee_id   VARCHAR2(10) PK / FK → employee
 *   password_hash NVARCHAR2(255)
 *   role_emp      NVARCHAR2(20)  CHECK IN ('0','1','2','3','4','5')
 *   user_name     NVARCHAR2(254) UNIQUE
 *   is_active     NUMBER(1)      CHECK IN (0,1)
 *   last_login    TIMESTAMP(6) WITH TIME ZONE
 *   created_at    TIMESTAMP(6) WITH TIME ZONE
 *   updated_at    TIMESTAMP(6) WITH TIME ZONE
 *
 * ARCHITECTURE NOTE:
 *   AppUser chỉ chứa dữ liệu xác thực / tài khoản.
 *   Dữ liệu hồ sơ nhân viên (fullName, email, phone, branchId)
 *   thuộc về Employee entity — được truy cập qua AppUser.getEmployee().
 *
 *   PK là employee_id, không có cột user_id riêng.
 *   ISSUE: role '0' = CUSTOMER nhưng bảng lại FK sang employee_id
 *          → customer app user sẽ không tồn tại trong hệ thống này.
 */
public class AppUser {

    /** FK sang bảng employee, đồng thời là PK của app_user */
    private String employeeId;

    /**
     * Mật khẩu đã được hash.
     * TODO: Dùng BCrypt hoặc Argon2 thay vì SHA-256 đơn giản.
     */
    private String passwordHash;

    /**
     * Vai trò của user, ánh xạ từ enum Role.
     * DB lưu dạng String "0".."5".
     */
    private Role role;

    /** Tên đăng nhập, unique trong toàn hệ thống */
    private String userName;

    /** Trạng thái tài khoản: true = ACTIVE (1), false = INACTIVE (0) */
    private boolean isActive;

    /** Thời điểm đăng nhập cuối cùng, null nếu chưa đăng nhập lần nào */
    private OffsetDateTime lastLogin;

    /** Thời điểm tạo tài khoản */
    private OffsetDateTime createdAt;

    /** Thời điểm cập nhật lần cuối */
    private OffsetDateTime updatedAt;

    /**
     * Employee profile — dữ liệu hồ sơ nhân viên (fullName, email, phone, ...).
     * Được JOIN và map trong AppUserDAO.
     * Có thể null nếu chỉ truy vấn auth data thuần túy.
     */
    private Employee employee;

    // ── Constructors ─────────────────────────────────────────────

    public AppUser() {}

    /**
     * Constructor đầy đủ cho DAO mapping.
     */
    public AppUser(String employeeId, String passwordHash, Role role,
                   String userName, boolean isActive,
                   OffsetDateTime lastLogin, OffsetDateTime createdAt,
                   OffsetDateTime updatedAt) {
        this.employeeId   = employeeId;
        this.passwordHash = passwordHash;
        this.role         = role;
        this.userName     = userName;
        this.isActive     = isActive;
        this.lastLogin    = lastLogin;
        this.createdAt    = createdAt;
        this.updatedAt    = updatedAt;
    }

    // ── Getters / Setters ─────────────────────────────────────────

    public String getEmployeeId()           { return employeeId; }
    public void setEmployeeId(String v)     { this.employeeId = v; }

    public String getPasswordHash()         { return passwordHash; }
    public void setPasswordHash(String v)   { this.passwordHash = v; }

    public Role getRole()                   { return role; }
    public void setRole(Role v)             { this.role = v; }

    public String getUserName()             { return userName; }
    public void setUserName(String v)       { this.userName = v; }

    public boolean isActive()              { return isActive; }
    public void setActive(boolean v)        { this.isActive = v; }

    public OffsetDateTime getLastLogin()    { return lastLogin; }
    public void setLastLogin(OffsetDateTime v) { this.lastLogin = v; }

    public OffsetDateTime getCreatedAt()    { return createdAt; }
    public void setCreatedAt(OffsetDateTime v) { this.createdAt = v; }

    public OffsetDateTime getUpdatedAt()    { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime v) { this.updatedAt = v; }

    /** Employee profile object (fullName, email, phone, branchId, ...) */
    public Employee getEmployee()           { return employee; }
    public void setEmployee(Employee v)     { this.employee = v; }

    // ── Helpers ───────────────────────────────────────────────────

/**
     * Kiểm tra quyền truy cập (Role-Based Access Control).
     * ADMIN và CEO có toàn quyền.
     * BRANCH_MANAGER có quyền giám sát các nghiệp vụ.
     * Nhân viên vận hành chỉ có quyền với đúng chuyên môn của mình.
     *
     * @param required Role yêu cầu để thực hiện hành động
     * @return true nếu user có quyền
     */
    public boolean hasRole(Role required) {
        // 1. Quyền tối cao (Superuser): Admin và CEO làm được mọi thứ
        if (this.role == Role.ADMIN || this.role == Role.CEO) {
            return true;
        }

        // 2. Quyền quản lý (Manager): Có thể xem/duyệt mọi nghiệp vụ của nhân viên (1, 2, 3)
        // Nếu required không phải là Admin hay CEO, Manager sẽ được phép pass qua.
        if (this.role == Role.BRANCH_MANAGER) {
            if (required != Role.ADMIN && required != Role.CEO) {
                return true; 
            }
        }

        // 3. Nhân viên thông thường (Horizontal Roles): Phải khớp chính xác 100% chuyên môn
        // Ví dụ: WAREHOUSE_STAFF chỉ trả về true nếu required đúng là WAREHOUSE_STAFF
        return this.role == required;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppUser)) return false;
        return Objects.equals(employeeId, ((AppUser) o).employeeId);
    }

    @Override
    public int hashCode() { return Objects.hash(employeeId); }

    @Override
    public String toString() {
        String empInfo = (employee != null) ? ", fullName='" + employee.getFullName() + "'" : "";
        return "AppUser{employeeId='" + employeeId + "', userName='" + userName
             + "', role=" + role + ", isActive=" + isActive + empInfo + "}";
    }
}