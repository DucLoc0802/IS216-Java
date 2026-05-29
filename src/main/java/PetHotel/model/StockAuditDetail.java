package PetHotel.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class StockAuditDetail {
    private String stockAuditId;
    private String productId;
    private String productName;
    private String unit;
    private BigDecimal systemQuantity = BigDecimal.ZERO;
    private BigDecimal actualQuantity = BigDecimal.ZERO;
    private BigDecimal differenceQuantity = BigDecimal.ZERO;
    private BigDecimal differenceRate = BigDecimal.ZERO;
    private String note;

    public String getStockAuditId() {
        return stockAuditId;
    }

    public void setStockAuditId(String stockAuditId) {
        this.stockAuditId = stockAuditId;
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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getSystemQuantity() {
        return systemQuantity;
    }

    public void setSystemQuantity(BigDecimal systemQuantity) {
        this.systemQuantity = valueOrZero(systemQuantity);
        recalculateDifference();
    }

    public BigDecimal getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(BigDecimal actualQuantity) {
        this.actualQuantity = valueOrZero(actualQuantity);
        recalculateDifference();
    }

    public BigDecimal getDifferenceQuantity() {
        return differenceQuantity;
    }

    public void setDifferenceQuantity(BigDecimal differenceQuantity) {
        this.differenceQuantity = valueOrZero(differenceQuantity);
    }

    public BigDecimal getDifferenceRate() {
        return differenceRate;
    }

    public void setDifferenceRate(BigDecimal differenceRate) {
        this.differenceRate = valueOrZero(differenceRate);
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getSystemQuantityText() {
        return numberText(systemQuantity);
    }

    public String getActualQuantityText() {
        return numberText(actualQuantity);
    }

    public String getDifferenceQuantityText() {
        return numberText(differenceQuantity);
    }

    public String getDifferenceRateText() {
        return differenceRate == null ? "-" : numberText(differenceRate) + "%";
    }

    public void recalculateDifference() {
        differenceQuantity = actualQuantity.subtract(systemQuantity);
        if (systemQuantity.compareTo(BigDecimal.ZERO) == 0) {
            differenceRate = BigDecimal.ZERO;
        } else {
            differenceRate = differenceQuantity.abs()
                .multiply(BigDecimal.valueOf(100))
                .divide(systemQuantity, 2, RoundingMode.HALF_UP);
        }
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String numberText(BigDecimal value) {
        BigDecimal normalized = valueOrZero(value).stripTrailingZeros();
        return normalized.scale() <= 0 ? normalized.toPlainString() : normalized.toPlainString();
    }
}
