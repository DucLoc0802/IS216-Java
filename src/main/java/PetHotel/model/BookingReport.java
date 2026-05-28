package PetHotel.model;

public class BookingReport {
    private String period;
    private int bookingCount;
    private int newBookingCount;
    private int completedBookingCount;
    private int cancelledBookingCount;

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public int getBookingCount() {
        return bookingCount;
    }

    public void setBookingCount(int bookingCount) {
        this.bookingCount = bookingCount;
    }

    public int getNewBookingCount() {
        return newBookingCount;
    }

    public void setNewBookingCount(int newBookingCount) {
        this.newBookingCount = newBookingCount;
    }

    public int getCompletedBookingCount() {
        return completedBookingCount;
    }

    public void setCompletedBookingCount(int completedBookingCount) {
        this.completedBookingCount = completedBookingCount;
    }

    public int getCancelledBookingCount() {
        return cancelledBookingCount;
    }

    public void setCancelledBookingCount(int cancelledBookingCount) {
        this.cancelledBookingCount = cancelledBookingCount;
    }
}
