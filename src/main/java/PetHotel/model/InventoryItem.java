package PetHotel.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

public class InventoryItem {
    public static final String STATUS_OK = "Đủ hàng";
    public static final String STATUS_LOW = "Tồn kho thấp";
    public static final String STATUS_CRITICAL = "Nguy hiểm";
    public static final String STATUS_OUT = "Hết hàng";

    private String branchId;
    private String productId;
    private String productName;
    private String productCategoryId;
    private String categoryName;
    private String unit;
    private BigDecimal costPrice = BigDecimal.ZERO;
    private BigDecimal quantityInStock = BigDecimal.ZERO;
    private BigDecimal reorderPoint = BigDecimal.ZERO;
    private OffsetDateTime lastUpdated;

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCategoryId() {
        return productCategoryId;
    }

    public void setProductCategoryId(String productCategoryId) {
        this.productCategoryId = productCategoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = normalize(costPrice);
    }

    public BigDecimal getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(BigDecimal quantityInStock) {
        this.quantityInStock = normalize(quantityInStock);
    }

    public BigDecimal getReorderPoint() {
        return reorderPoint;
    }

    public void setReorderPoint(BigDecimal reorderPoint) {
        this.reorderPoint = normalize(reorderPoint);
    }

    public OffsetDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(OffsetDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getStatus() {
        if (quantityInStock.compareTo(BigDecimal.ZERO) == 0) {
            return STATUS_OUT;
        }
        if (reorderPoint.compareTo(BigDecimal.ZERO) > 0
                && quantityInStock.compareTo(reorderPoint.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP)) <= 0) {
            return STATUS_CRITICAL;
        }
        if (reorderPoint.compareTo(BigDecimal.ZERO) > 0 && quantityInStock.compareTo(reorderPoint) <= 0) {
            return STATUS_LOW;
        }
        return STATUS_OK;
    }

    public boolean isLowOrOut() {
        return STATUS_LOW.equals(getStatus()) || STATUS_CRITICAL.equals(getStatus()) || STATUS_OUT.equals(getStatus());
    }

    public String getQuantityText() {
        return numberText(quantityInStock);
    }

    public String getReorderPointText() {
        return numberText(reorderPoint);
    }

    public String getCostPriceText() {
        return numberText(costPrice);
    }

    public String getStockWithUnit() {
        return getQuantityText() + " " + (unit == null ? "" : unit);
    }

    private BigDecimal normalize(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String numberText(BigDecimal value) {
        BigDecimal normalized = normalize(value).stripTrailingZeros();
        return normalized.scale() <= 0 ? normalized.toPlainString() : normalized.toPlainString();
    }
}
