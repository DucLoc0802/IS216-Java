package PetHotel.model;

import PetHotel.util.Role;
import java.time.OffsetDateTime;
import java.util.Objects;

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
 * NOTE: PK là employee_id, không có cột user_id riêng.
 *       ISSUE: role '0' = CUSTOMER nhưng bảng lại FK sang employee_id
 *              → customer app user sẽ không tồn tại trong hệ thống này.
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

    // ── Helpers ───────────────────────────────────────────────────

    /** @return true nếu user có quyền bằng hoặc cao hơn role được chỉ định */
    public boolean hasRole(Role required) {
        // So sánh theo thứ tự số của dbValue: CEO (5) > BRANCH_MANAGER (4) > ...
        return Integer.parseInt(this.role.getDbValue())
            >= Integer.parseInt(required.getDbValue());
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
        return "AppUser{employeeId='" + employeeId + "', userName='" + userName
             + "', role=" + role + ", isActive=" + isActive + "}";
    }
}
