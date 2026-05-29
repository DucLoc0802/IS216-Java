package PetHotel.model;

import java.math.BigDecimal;

/**
 * MaterialUsageConfirmRow — Dòng vật tư để hiển thị trong dialog xác nhận hoàn thành dịch vụ.
 * 
 * Chứa thông tin định mức từ SERVICE_PRODUCT_STANDARD cộng với lượng thực tế sử dụng do nhân viên nhập.
 */
public class MaterialUsageConfirmRow {

    private String serviceProductStandardId;
    private String productId;
    private String productName;
    private String productUnit;          // Đơn vị tính của PRODUCT
    private BigDecimal standardAmount;   // Định mức từ SERVICE_PRODUCT_STANDARD.usage_amount
    private String standardUnit;         // Đơn vị định mức từ SERVICE_PRODUCT_STANDARD.usage_unit
    private BigDecimal actualAmount;     // Số lượng thực tế sử dụng (do nhân viên nhập)
    private BigDecimal inventoryQuantity; // Tồn kho hiện tại
    private String note;                 // Ghi chú từ SERVICE_PRODUCT_STANDARD hoặc dialog

    // ── Constructors ──────────────────────────────────────────────

    public MaterialUsageConfirmRow() {}

    public MaterialUsageConfirmRow(String serviceProductStandardId, String productId, String productName,
                                   String productUnit, BigDecimal standardAmount, String standardUnit,
                                   BigDecimal actualAmount, BigDecimal inventoryQuantity) {
        this.serviceProductStandardId = serviceProductStandardId;
        this.productId = productId;
        this.productName = productName;
        this.productUnit = productUnit;
        this.standardAmount = standardAmount;
        this.standardUnit = standardUnit;
        this.actualAmount = actualAmount;
        this.inventoryQuantity = inventoryQuantity;
    }

    // ── Getters / Setters ─────────────────────────────────────────

    public String getServiceProductStandardId()     { return serviceProductStandardId; }
    public void setServiceProductStandardId(String v) { this.serviceProductStandardId = v; }

    public String getProductId()                    { return productId; }
    public void setProductId(String v)              { this.productId = v; }

    public String getProductName()                  { return productName; }
    public void setProductName(String v)            { this.productName = v; }

    public String getProductUnit()                  { return productUnit; }
    public void setProductUnit(String v)            { this.productUnit = v; }

    public BigDecimal getStandardAmount()           { return standardAmount; }
    public void setStandardAmount(BigDecimal v)     { this.standardAmount = v; }

    public String getStandardUnit()                 { return standardUnit; }
    public void setStandardUnit(String v)           { this.standardUnit = v; }

    public BigDecimal getActualAmount()             { return actualAmount; }
    public void setActualAmount(BigDecimal v)       { this.actualAmount = v; }

    public BigDecimal getInventoryQuantity()        { return inventoryQuantity; }
    public void setInventoryQuantity(BigDecimal v)  { this.inventoryQuantity = v; }

    public String getNote()                         { return note; }
    public void setNote(String v)                   { this.note = v; }

    public void setUsageUnit(String usageUnit) {
        this.standardUnit = usageUnit;
    }
}
