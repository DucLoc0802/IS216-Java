package PetHotel.util;

/**
 * Role — Enum ánh xạ cột role_emp trong bảng app_user.
 *
 * Schema: CONSTRAINT ck_app_user_role CHECK (role_emp IN (0,1,2,3,4,5))
 *   0 = CUSTOMER        - Khách hàng (tài khoản app mobile, không dùng trong hệ thống này)
 *   1 = RECEPTIONIST    - Nhân viên lễ tân
 *   2 = PET_CARE_STAFF  - Nhân viên chăm sóc thú cưng
 *   3 = BRANCH_MANAGER  - Quản lý chi nhánh
 *   4 = CEO             - Giám đốc / Owner
 *
 * NOTE: role_emp trong DB kiểu NVARCHAR2(20) nhưng CHECK chỉ chấp nhận '0'..'5'
 *       → Khi lưu/đọc dùng String value "0", "1", ... không phải tên enum.
 *
 * ISSUE FOUND: role_emp có giá trị '0' = CUSTOMER nhưng app_user
 *              lại FK sang employee_id — customer KHÔNG có employee.
 *              Xem thêm phần Issues Found trong AuthBUS.
 */
public enum Role {

    ADMIN("0"),
    RECEPTIONIST("1"),
    PET_CARE_STAFF("2"),
    BRANCH_MANAGER("3"),
    CEO("4");

    /** Giá trị lưu trong cột role_emp của bảng app_user */
    private final String dbValue;

    Role(String dbValue) {
        this.dbValue = dbValue;
    }

    /** @return Giá trị DB ("0".."5") */
    public String getDbValue() {
        return dbValue;
    }

    /**
     * @return Tên hiển thị bằng tiếng Việt cho role
     */
    public String getDisplayName() {
        switch (this) {
            case ADMIN:             return "Quản Trị Viên";
            case RECEPTIONIST:      return "Lễ Tân";
            case PET_CARE_STAFF:    return "Nhân Viên Chăm Sóc";
            case BRANCH_MANAGER:    return "Quản Lý Chi Nhánh";
            case CEO:               return "Giám Đốc";
            default:                return "Unknown";
        }
    }

    /**
     * @return true nếu role này là cấp quản lý (BRANCH_MANAGER hoặc CEO)
     */
    public boolean isManagement() {
        return this == BRANCH_MANAGER || this == CEO;
    }

    /**
     * Chuyển đổi từ giá trị DB sang enum.
     *
     * @param dbValue chuỗi "0".."5" lấy từ ResultSet
     * @return Role tương ứng
     * @throws IllegalArgumentException nếu giá trị không hợp lệ
     */
    public static Role fromDbValue(String dbValue) {
        for (Role r : values()) {
            if (r.dbValue.equals(dbValue)) return r;
        }
        throw new IllegalArgumentException("Unknown role value: " + dbValue);
    }
}
