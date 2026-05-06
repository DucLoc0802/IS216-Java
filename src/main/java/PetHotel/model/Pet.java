package PetHotel.model;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Pet — Ánh xạ bảng PET.
 *
 * Schema:
 *   pet_id       VARCHAR2(10)   PK
 *   customer_id  VARCHAR2(10)   NOT NULL, FK → customer
 *   pet_name     NVARCHAR2(20)  NOT NULL
 *   species      NVARCHAR2(30)  NOT NULL  (ví dụ: 'DOG', 'CAT')
 *   breed        NVARCHAR2(60)
 *   sex          NVARCHAR2(10)  CHECK IN ('Male','Female') hoặc NULL
 *   weight_kg    NUMBER(5,2)    CHECK > 0 hoặc NULL
 *   special_note CLOB
 *   created_at   TIMESTAMP(6) WITH TIME ZONE
 *   updated_at   TIMESTAMP(6) WITH TIME ZONE
 */
public class Pet {

    /** Mã thú cưng, PK */
    private String petId;

    /** Mã khách hàng sở hữu thú cưng */
    private String customerId;

    /** Tên thú cưng */
    private String petName;

    /** Loài: DOG, CAT, hoặc loài khác */
    private String species;

    /** Giống, có thể null */
    private String breed;

    /**
     * Giới tính: "Male" hoặc "Female", có thể null.
     * Schema CHECK: sex IN ('Male','Female') — chú ý viết hoa chữ đầu.
     */
    private String sex;

    /** Cân nặng tính bằng kg, có thể null, phải > 0 nếu có */
    private Double weightKg;

    /** Ghi chú đặc biệt về thú cưng (CLOB) */
    private String specialNote;

    /** Thời điểm tạo bản ghi */
    private OffsetDateTime createdAt;

    /** Thời điểm cập nhật lần cuối */
    private OffsetDateTime updatedAt;

    // ── Constructors ─────────────────────────────────────────────

    public Pet() {}

    /** Constructor cho insert mới */
    public Pet(String petId, String customerId, String petName, String species, 
               String breed, String sex, Double weightKg, String specialNote) {
        this.petId = petId;
        this.customerId = customerId;
        this.petName = petName;
        this.species = species;
        this.breed = breed;
        this.sex = sex;
        this.weightKg = weightKg;
        this.specialNote = specialNote;
    }

    /** Constructor đầy đủ cho DAO mapping */
    public Pet(String petId, String customerId, String petName, String species,
               String breed, String sex, Double weightKg, String specialNote,
               OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.petId       = petId;
        this.customerId  = customerId;
        this.petName     = petName;
        this.species     = species;
        this.breed       = breed;
        this.sex         = sex;
        this.weightKg    = weightKg;
        this.specialNote = specialNote;
        this.createdAt   = createdAt;
        this.updatedAt   = updatedAt;
    }

    // ── Getters / Setters ─────────────────────────────────────────

    public String getPetId()                   { return petId; }
    public void setPetId(String v)             { this.petId = v; }

    public String getCustomerId()              { return customerId; }
    public void setCustomerId(String v)        { this.customerId = v; }

    public String getPetName()                 { return petName; }
    public void setPetName(String v)           { this.petName = v; }

    public String getSpecies()                 { return species; }
    public void setSpecies(String v)           { this.species = v; }

    public String getBreed()                   { return breed; }
    public void setBreed(String v)             { this.breed = v; }

    public String getSex()                     { return sex; }
    public void setSex(String v)               { this.sex = v; }

    public Double getWeightKg()                { return weightKg; }
    public void setWeightKg(Double v)          { this.weightKg = v; }

    public String getSpecialNote()             { return specialNote; }
    public void setSpecialNote(String v)       { this.specialNote = v; }

    public OffsetDateTime getCreatedAt()       { return createdAt; }
    public void setCreatedAt(OffsetDateTime v) { this.createdAt = v; }

    public OffsetDateTime getUpdatedAt()       { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime v) { this.updatedAt = v; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pet)) return false;
        return Objects.equals(petId, ((Pet) o).petId);
    }

    @Override public int hashCode() { return Objects.hash(petId); }

    @Override
    public String toString() {
        return "Pet{id='" + petId + "', name='" + petName
             + "', species='" + species + "', customerId='" + customerId + "'}";
    }
}
