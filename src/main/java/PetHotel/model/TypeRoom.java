package PetHotel.model;

public class TypeRoom {
    private String typeRoomId;
    private String typeName;
    private int maxPets;
    private double maxWeightKg;
    private double basePricePerDay;
    private boolean isActive;

    public String getTypeRoomId() { return typeRoomId; }
    public void setTypeRoomId(String typeRoomId) { this.typeRoomId = typeRoomId; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }

    public int getMaxPets() { return maxPets; }
    public void setMaxPets(int maxPets) { this.maxPets = maxPets; }

    public double getMaxWeightKg() { return maxWeightKg; }
    public void setMaxWeightKg(double maxWeightKg) { this.maxWeightKg = maxWeightKg; }

    public double getBasePricePerDay() { return basePricePerDay; }
    public void setBasePricePerDay(double basePricePerDay) { this.basePricePerDay = basePricePerDay; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}