package PetHotel.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * ServiceProductStandard — Ánh xạ bảng SERVICE_PRODUCT_STANDARD.
 *
 * Schema:
 *   service_product_standard_id VARCHAR2(10) PK
 *   service_id                  VARCHAR2(10) FK → services
 *   product_id                  VARCHAR2(10) FK → product
 *   species                     VARCHAR2(20) CHECK IN ('DOG', 'CAT')
 *   min_weight_kg               NUMBER(5,2)
 *   max_weight_kg               NUMBER(5,2)
 *   usage_amount                NUMBER(10,2)
 *   usage_unit                  VARCHAR2(10) CHECK IN ('ML', 'L', 'G', 'KG')
 *   note                        CLOB
 *   created_at                  TIMESTAMP(6) WITH TIME ZONE
 *   updated_at                  TIMESTAMP(6) WITH TIME ZONE
 */
public class ServiceProductStandard {

    private String serviceProductStandardId;
    private String serviceId;
    private String productId;
    private String species;           // DOG, CAT
    private BigDecimal minWeightKg;
    private BigDecimal maxWeightKg;
    private BigDecimal usageAmount;   // Định mức sử dụng
    private String usageUnit;         // ML, L, G, KG
    private String note;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // Additional fields for convenience (from joined queries)
    private String productName;
    private String productUnit;       // PRODUCT.unit
    private BigDecimal inventoryQuantity; // From BRANCH_INVENTORY

    // ── Constructors ──────────────────────────────────────────────

    public ServiceProductStandard() {}

    public ServiceProductStandard(String serviceProductStandardId, String serviceId, String productId,
                                   String species, BigDecimal minWeightKg, BigDecimal maxWeightKg,
                                   BigDecimal usageAmount, String usageUnit, String note) {
        this.serviceProductStandardId = serviceProductStandardId;
        this.serviceId = serviceId;
        this.productId = productId;
        this.species = species;
        this.minWeightKg = minWeightKg;
        this.maxWeightKg = maxWeightKg;
        this.usageAmount = usageAmount;
        this.usageUnit = usageUnit;
        this.note = note;
    }

    public ServiceProductStandard(String serviceProductStandardId, String serviceId, String productId,
                                   String species, BigDecimal minWeightKg, BigDecimal maxWeightKg,
                                   BigDecimal usageAmount, String usageUnit, String note,
                                   OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.serviceProductStandardId = serviceProductStandardId;
        this.serviceId = serviceId;
        this.productId = productId;
        this.species = species;
        this.minWeightKg = minWeightKg;
        this.maxWeightKg = maxWeightKg;
        this.usageAmount = usageAmount;
        this.usageUnit = usageUnit;
        this.note = note;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ── Getters / Setters ─────────────────────────────────────────

    public String getServiceProductStandardId()    { return serviceProductStandardId; }
    public void setServiceProductStandardId(String v) { this.serviceProductStandardId = v; }

    public String getServiceId()                   { return serviceId; }
    public void setServiceId(String v)             { this.serviceId = v; }

    public String getProductId()                   { return productId; }
    public void setProductId(String v)             { this.productId = v; }

    public String getSpecies()                     { return species; }
    public void setSpecies(String v)               { this.species = v; }

    public BigDecimal getMinWeightKg()             { return minWeightKg; }
    public void setMinWeightKg(BigDecimal v)       { this.minWeightKg = v; }

    public BigDecimal getMaxWeightKg()             { return maxWeightKg; }
    public void setMaxWeightKg(BigDecimal v)       { this.maxWeightKg = v; }

    public BigDecimal getUsageAmount()             { return usageAmount; }
    public void setUsageAmount(BigDecimal v)       { this.usageAmount = v; }

    public String getUsageUnit()                   { return usageUnit; }
    public void setUsageUnit(String v)             { this.usageUnit = v; }

    public String getNote()                        { return note; }
    public void setNote(String v)                  { this.note = v; }

    public OffsetDateTime getCreatedAt()           { return createdAt; }
    public void setCreatedAt(OffsetDateTime v)     { this.createdAt = v; }

    public OffsetDateTime getUpdatedAt()           { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime v)     { this.updatedAt = v; }

    public String getProductName()                 { return productName; }
    public void setProductName(String v)           { this.productName = v; }

    public String getProductUnit()                 { return productUnit; }
    public void setProductUnit(String v)           { this.productUnit = v; }

    public BigDecimal getInventoryQuantity()       { return inventoryQuantity; }
    public void setInventoryQuantity(BigDecimal v) { this.inventoryQuantity = v; }

    @Override
    public String toString() {
        return "ServiceProductStandard{" +
                "id='" + serviceProductStandardId + '\'' +
                ", product='" + productName + '\'' +
                ", species='" + species + '\'' +
                ", weight=" + minWeightKg + "-" + maxWeightKg + "kg" +
                ", usage=" + usageAmount + usageUnit +
                '}';
    }
}
