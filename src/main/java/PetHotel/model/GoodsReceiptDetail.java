package PetHotel.model;

import java.math.BigDecimal;

public class GoodsReceiptDetail {
    private String goodsReceiptId;
    private String productId;
    private String productName;
    private BigDecimal quantity = BigDecimal.ZERO;
    private String unit;
    private BigDecimal lineTotal = BigDecimal.ZERO;
    private String note;

    public String getGoodsReceiptId() {
        return goodsReceiptId;
    }

    public void setGoodsReceiptId(String goodsReceiptId) {
        this.goodsReceiptId = goodsReceiptId;
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

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity == null ? BigDecimal.ZERO : quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal == null ? BigDecimal.ZERO : lineTotal;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getQuantityText() {
        BigDecimal normalized = quantity.stripTrailingZeros();
        return normalized.scale() <= 0 ? normalized.toPlainString() : normalized.toPlainString();
    }
}
