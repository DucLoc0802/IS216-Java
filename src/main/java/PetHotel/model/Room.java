package PetHotel.model;

import java.time.OffsetDateTime;

public class Room {
    private String roomId;
    private String branchId;
    private String typeRoomId;
    private String roomNumber;
    private String status; // AVAILABLE, IN_USE, CLEANING, MAINTENANCE
    private OffsetDateTime createdAt;

    // Thông tin JOIN từ type_room (dùng để hiển thị)
    private String typeName;
    private double basePricePerDay;
    private int maxPets;
    private double maxWeightKg;

    // ── Getters & Setters ────────────────────────────────────────

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getBranchId() { return branchId; }
    public void setBranchId(String branchId) { this.branchId = branchId; }

    public String getTypeRoomId() { return typeRoomId; }
    public void setTypeRoomId(String typeRoomId) { this.typeRoomId = typeRoomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }

    public double getBasePricePerDay() { return basePricePerDay; }
    public void setBasePricePerDay(double basePricePerDay) { this.basePricePerDay = basePricePerDay; }

    public int getMaxPets() { return maxPets; }
    public void setMaxPets(int maxPets) { this.maxPets = maxPets; }

    public double getMaxWeightKg() { return maxWeightKg; }
    public void setMaxWeightKg(double maxWeightKg) { this.maxWeightKg = maxWeightKg; }
}
