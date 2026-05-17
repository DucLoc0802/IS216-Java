package PetHotel.model;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Customer — Ánh xạ bảng CUSTOMER.
 *
 * Schema:
 *   customer_id  VARCHAR2(10)    PK
 *   full_name    NVARCHAR2(120)  NOT NULL
 *   email        NVARCHAR2(254)  UNIQUE
 *   phone        NVARCHAR2(20)   NOT NULL, UNIQUE
 *   address      NVARCHAR2(120)
 *   note         CLOB
 *   created_at   TIMESTAMP(6) WITH TIME ZONE
 *   updated_at   TIMESTAMP(6) WITH TIME ZONE
 */
public class Customer {

    /** Mã khách hàng, PK dạng "CUS0000001" */
    private String customerId;

    /** Họ tên đầy đủ */
    private String fullName;

    /** Email, có thể null, unique */
    private String email;

    /** Số điện thoại, NOT NULL, unique */
    private String phone;

    /** Địa chỉ, có thể null */
    private String address;

    /** Ghi chú thêm về khách hàng (CLOB) */
    private String note;

    /** Thời điểm tạo bản ghi */
    private OffsetDateTime createdAt;

    /** Thời điểm cập nhật lần cuối */
    private OffsetDateTime updatedAt;

    // ── Constructors ─────────────────────────────────────────────

    public Customer() {}

    /** Constructor cho insert mới (chưa có ID, DB tự sinh hoặc sequence) */
/** Constructor cho insert mới khi đã sinh ID từ BUS */
    public Customer(String customerId, String fullName, String email, 
                    String phone, String address, String note) {
        this.customerId = customerId;
        this.fullName   = fullName;
        this.email      = email;
        this.phone      = phone;
        this.address    = address;
        this.note       = note;
    }

    /** Constructor đầy đủ cho DAO mapping */
    public Customer(String customerId, String fullName, String email,
                    String phone, String address, String note,
                    OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.customerId = customerId;
        this.fullName   = fullName;
        this.email      = email;
        this.phone      = phone;
        this.address    = address;
        this.note       = note;
        this.createdAt  = createdAt;
        this.updatedAt  = updatedAt;
    }

    // ── Getters / Setters ─────────────────────────────────────────

    public String getCustomerId()              { return customerId; }
    public void setCustomerId(String v)        { this.customerId = v; }

    public String getFullName()                { return fullName; }
    public void setFullName(String v)          { this.fullName = v; }

    public String getEmail()                   { return email; }
    public void setEmail(String v)             { this.email = v; }

    public String getPhone()                   { return phone; }
    public void setPhone(String v)             { this.phone = v; }

    public String getAddress()                 { return address; }
    public void setAddress(String v)           { this.address = v; }

    public String getNote()                    { return note; }
    public void setNote(String v)              { this.note = v; }

    public OffsetDateTime getCreatedAt()       { return createdAt; }
    public void setCreatedAt(OffsetDateTime v) { this.createdAt = v; }

    public OffsetDateTime getUpdatedAt()       { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime v) { this.updatedAt = v; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;
        return Objects.equals(customerId, ((Customer) o).customerId);
    }

    @Override public int hashCode() { return Objects.hash(customerId); }

    @Override
    public String toString() {
        return "Customer{id='" + customerId + "', name='" + fullName
             + "', phone='" + phone + "'}";
    }
}
