package PetHotel.model;

public class RoomUsageReport {
    private String period;
    private int totalRoom;
    private int inUseRoom;
    private int availableRoom;
    private double usageRate;

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public int getTotalRoom() {
        return totalRoom;
    }

    public void setTotalRoom(int totalRoom) {
        this.totalRoom = totalRoom;
    }

    public int getInUseRoom() {
        return inUseRoom;
    }

    public void setInUseRoom(int inUseRoom) {
        this.inUseRoom = inUseRoom;
    }

    public int getAvailableRoom() {
        return availableRoom;
    }

    public void setAvailableRoom(int availableRoom) {
        this.availableRoom = availableRoom;
    }

    public double getUsageRate() {
        return usageRate;
    }

    public void setUsageRate(double usageRate) {
        this.usageRate = usageRate;
    }
}
