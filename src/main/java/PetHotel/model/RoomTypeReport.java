package PetHotel.model;

public class RoomTypeReport {
    private String roomType;
    private int totalRoom;
    private int inUseRoom;
    private int availableRoom;
    private int maintenanceRoom;
    private int inactiveRoom;
    private double usageRate;

    public RoomTypeReport(
            String roomType,
            int totalRoom,
            int inUseRoom,
            int availableRoom,
            int maintenanceRoom,
            int inactiveRoom) {
        this.roomType = roomType;
        this.totalRoom = totalRoom;
        this.inUseRoom = inUseRoom;
        this.availableRoom = availableRoom;
        this.maintenanceRoom = maintenanceRoom;
        this.inactiveRoom = inactiveRoom;
        this.usageRate = totalRoom == 0 ? 0 : inUseRoom * 100.0 / totalRoom;
    }

    public String getRoomType() {
        return roomType;
    }

    public int getTotalRoom() {
        return totalRoom;
    }

    public int getInUseRoom() {
        return inUseRoom;
    }

    public int getAvailableRoom() {
        return availableRoom;
    }

    public int getMaintenanceRoom() {
        return maintenanceRoom;
    }

    public int getInactiveRoom() {
        return inactiveRoom;
    }

    public double getUsageRate() {
        return usageRate;
    }
}
