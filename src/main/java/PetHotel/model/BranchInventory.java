package PetHotel.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class BranchInventory {
    public static final String STATUS_ALL = "Tất cả";
    public static final String STATUS_IN_STOCK = "Còn hàng";
    public static final String STATUS_LOW = "Sắp hết";
    public static final String STATUS_OUT = "Hết hàng";

    private String branchId;
    private String branchName;
    private String productId;
    private String productName;
    private String productCategoryId;
    private String categoryName;
    private String unit;
    private BigDecimal quantityInStock = BigDecimal.ZERO;
    private BigDecimal reorderPoint;
    private OffsetDateTime lastUpdated;

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
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

    public BigDecimal getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(BigDecimal quantityInStock) {
        this.quantityInStock = quantityInStock == null ? BigDecimal.ZERO : quantityInStock;
    }

    public BigDecimal getReorderPoint() {
        return reorderPoint;
    }

    public void setReorderPoint(BigDecimal reorderPoint) {
        this.reorderPoint = reorderPoint;
    }

    public OffsetDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(OffsetDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getStockStatus() {
        if (quantityInStock.compareTo(BigDecimal.ZERO) == 0) {
            return STATUS_OUT;
        }
        if (reorderPoint != null && quantityInStock.compareTo(reorderPoint) <= 0) {
            return STATUS_LOW;
        }
        return STATUS_IN_STOCK;
    }

    public String getQuantityText() {
        return numberText(quantityInStock);
    }

    public String getReorderPointText() {
        return reorderPoint == null ? "-" : numberText(reorderPoint);
    }

    public String getLastUpdatedText() {
        return lastUpdated == null ? "-" : lastUpdated.toLocalDateTime().toString();
    }

    private String numberText(BigDecimal value) {
        BigDecimal normalized = (value == null ? BigDecimal.ZERO : value).stripTrailingZeros();
        return normalized.scale() <= 0 ? normalized.toPlainString() : normalized.toPlainString();
    }
}
