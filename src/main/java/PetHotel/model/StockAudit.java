package PetHotel.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StockAudit {
    private String stockAuditId;
    private String branchId;
    private String branchName;
    private String employeeId;
    private String employeeName;
    private LocalDate auditDate;
    private String status;
    private String note;
    private final List<StockAuditDetail> details = new ArrayList<>();

    public String getStockAuditId() {
        return stockAuditId;
    }

    public void setStockAuditId(String stockAuditId) {
        this.stockAuditId = stockAuditId;
    }

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

    public LocalDate getAuditDate() {
        return auditDate;
    }

    public void setAuditDate(LocalDate auditDate) {
        this.auditDate = auditDate;
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

    public List<StockAuditDetail> getDetails() {
        return details;
    }

    public String getAuditDateText() {
        return auditDate == null ? "-" : auditDate.toString();
    }
}
