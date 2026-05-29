package PetHotel.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Product {
    private String productId;
    private String productCategoryId;
    private String productName;
    private String productCategory;
    private String categoryName;
    private String unit;
    private BigDecimal importPrice = BigDecimal.ZERO;
    private BigDecimal minQuantity = BigDecimal.ZERO;
    private boolean active = true;
    private String note;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductCategoryId() {
        return productCategoryId;
    }

    public void setProductCategoryId(String productCategoryId) {
        this.productCategoryId = productCategoryId;
    }

    public String getProductCategory() {
        return productCategory == null ? categoryName : productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
        this.categoryName = productCategory;
    }

    public String getCategoryName() {
        return categoryName == null ? productCategory : categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
        this.productCategory = categoryName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(BigDecimal importPrice) {
        this.importPrice = importPrice == null ? BigDecimal.ZERO : importPrice;
    }

    public BigDecimal getCostPrice() {
        return importPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        setImportPrice(costPrice);
    }

    public BigDecimal getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(BigDecimal minQuantity) {
        this.minQuantity = minQuantity == null ? BigDecimal.ZERO : minQuantity;
    }

    public boolean isActive() {
        return active;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getStatusText() {
        return active ? "Đang sử dụng" : "Ngừng sử dụng";
    }

    @Override
    public String toString() {
        String id = productId == null ? "" : " (" + productId + ")";
        return (productName == null ? "" : productName) + id;
    }
}
