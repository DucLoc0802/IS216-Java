package PetHotel.model;

import java.time.OffsetDateTime;

public class AuditLog {
    private String logId;
    private String employeeId;
    private String employeeName;
    private String action;
    private String details;
    private OffsetDateTime createdAt;

    public AuditLog() {}

    public AuditLog(String logId, String employeeId, String employeeName, String action, String details, OffsetDateTime createdAt) {
        this.logId = logId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.action = action;
        this.details = details;
        this.createdAt = createdAt;
    }

    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
