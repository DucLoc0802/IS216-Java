package PetHotel.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class MaterialWaste {
    public static final String STATUS_ALL = "Tất cả";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    private String materialWasteId;
    private String productId;
    private String productName;
    private String unit;
    private String employeeId;
    private String employeeName;
    private String branchId;
    private String branchName;
    private BigDecimal wasteQuantity = BigDecimal.ZERO;
    private String reason;
    private OffsetDateTime recordedAt;
    private String status = STATUS_PENDING;
    private String note;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public String getMaterialWasteId() { return materialWasteId; }
    public void setMaterialWasteId(String materialWasteId) { this.materialWasteId = materialWasteId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getBranchId() { return branchId; }
    public void setBranchId(String branchId) { this.branchId = branchId; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public BigDecimal getWasteQuantity() { return wasteQuantity; }
    public void setWasteQuantity(BigDecimal wasteQuantity) {
        this.wasteQuantity = wasteQuantity == null ? BigDecimal.ZERO : wasteQuantity;
    }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public OffsetDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(OffsetDateTime recordedAt) { this.recordedAt = recordedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getQuantityText() {
        BigDecimal normalized = wasteQuantity == null ? BigDecimal.ZERO : wasteQuantity.stripTrailingZeros();
        String value = normalized.scale() <= 0 ? normalized.toPlainString() : normalized.toPlainString();
        return unit == null || unit.isBlank() ? value : value + " " + unit;
    }

    public String getRecordedAtText() {
        return recordedAt == null ? "-" : recordedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getStatusText() {
        if (STATUS_APPROVED.equals(status)) {
            return "Đã duyệt";
        }
        if (STATUS_REJECTED.equals(status)) {
            return "Đã hủy";
        }
        return "Chờ duyệt";
    }
}
