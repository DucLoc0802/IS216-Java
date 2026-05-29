package PetHotel.util;

public enum Role {

    ADMIN("0"),
    RECEPTIONIST("1"),
    PET_CARE_STAFF("2"),
    BRANCH_MANAGER("3"),
    CEO("4");

    private final String dbValue;

    Role(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public String getDisplayName() {
        return switch (this) {
            case ADMIN -> "Quản Trị Viên";
            case RECEPTIONIST -> "Lễ Tân";
            case PET_CARE_STAFF -> "Nhân Viên Chăm Sóc";
            case BRANCH_MANAGER -> "Quản Lý Chi Nhánh";
            case CEO -> "Giám Đốc";
        };
    }

    public boolean isManagement() {
        return this == BRANCH_MANAGER || this == CEO;
    }

    public static Role fromDbValue(String dbValue) {
        for (Role role : values()) {
            if (role.dbValue.equals(dbValue)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role value: " + dbValue);
    }
}
