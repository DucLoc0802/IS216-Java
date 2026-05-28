package PetHotel.model;

public class ChainReport {
    private String branchId;
    private String branchName;
    private double totalRevenue;
    private int bookingCount;
    private int roomInUse;

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

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public int getBookingCount() {
        return bookingCount;
    }

    public void setBookingCount(int bookingCount) {
        this.bookingCount = bookingCount;
    }

    public int getRoomInUse() {
        return roomInUse;
    }

    public void setRoomInUse(int roomInUse) {
        this.roomInUse = roomInUse;
    }
}
