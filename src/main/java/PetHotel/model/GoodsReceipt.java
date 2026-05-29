package PetHotel.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GoodsReceipt {
    private String goodsReceiptId;
    private String branchId;
    private String employeeId;
    private String employeeName;
    private String supplierName;
    private LocalDate receiptDate;
    private BigDecimal totalQuantity = BigDecimal.ZERO;
    private int totalItemCount;
    private String status;
    private String note;
    private String productSummary;
    private String quantitySummary;
    private String unitSummary;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private final List<GoodsReceiptDetail> details = new ArrayList<>();

    public String getGoodsReceiptId() {
        return goodsReceiptId;
    }

    public void setGoodsReceiptId(String goodsReceiptId) {
        this.goodsReceiptId = goodsReceiptId;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public LocalDate getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(LocalDate receiptDate) {
        this.receiptDate = receiptDate;
    }

    public BigDecimal getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(BigDecimal totalQuantity) {
        this.totalQuantity = totalQuantity == null ? BigDecimal.ZERO : totalQuantity;
    }

    public int getTotalItemCount() {
        return totalItemCount;
    }

    public void setTotalItemCount(int totalItemCount) {
        this.totalItemCount = totalItemCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getProductSummary() {
        return productSummary == null || productSummary.isBlank() ? "—" : productSummary;
    }

    public void setProductSummary(String productSummary) {
        this.productSummary = productSummary;
    }

    public String getQuantitySummary() {
        return quantitySummary == null || quantitySummary.isBlank() ? numberText(totalQuantity) : quantitySummary;
    }

    public void setQuantitySummary(String quantitySummary) {
        this.quantitySummary = quantitySummary;
    }

    public String getUnitSummary() {
        return unitSummary == null || unitSummary.isBlank() ? "—" : unitSummary;
    }

    public void setUnitSummary(String unitSummary) {
        this.unitSummary = unitSummary;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount == null ? BigDecimal.ZERO : totalAmount;
    }

    public List<GoodsReceiptDetail> getDetails() {
        return details;
    }

    public String getReceiptDateText() {
        return receiptDate == null ? "—" : receiptDate.toString();
    }

    public String getStatusDisplay() {
        if ("APPROVED".equals(status)) return "Đã nhập kho";
        if ("DRAFT".equals(status)) return "Nháp";
        if ("CANCELLED".equals(status)) return "Đã hủy";
        return status == null ? "—" : status;
    }

    public String getTotalText() {
        return numberText(totalAmount);
    }

    public String getUnitPriceText() {
        if (details.isEmpty()) return "—";
        GoodsReceiptDetail detail = details.get(0);
        if (detail.getQuantity().compareTo(BigDecimal.ZERO) <= 0) return "—";
        return numberText(detail.getLineTotal().divide(detail.getQuantity(), 2, RoundingMode.HALF_UP));
    }

    private String numberText(BigDecimal value) {
        BigDecimal normalized = (value == null ? BigDecimal.ZERO : value).stripTrailingZeros();
        return normalized.scale() <= 0 ? normalized.toPlainString() : normalized.toPlainString();
    }
}
