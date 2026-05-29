package PetHotel.model;

public class BookingReport {
    private String period;
    private int bookingCount;
    private int newBookingCount;
    private int completedBookingCount;
    private int cancelledBookingCount;
    private int pendingBookingCount;
    private int confirmedBookingCount;
    private int checkedInBookingCount;
    private int checkedOutBookingCount;

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

    public int getPendingBookingCount() {
        return pendingBookingCount;
    }

    public void setPendingBookingCount(int pendingBookingCount) {
        this.pendingBookingCount = pendingBookingCount;
    }

    public int getConfirmedBookingCount() {
        return confirmedBookingCount;
    }

    public void setConfirmedBookingCount(int confirmedBookingCount) {
        this.confirmedBookingCount = confirmedBookingCount;
    }

    public int getCheckedInBookingCount() {
        return checkedInBookingCount;
    }

    public void setCheckedInBookingCount(int checkedInBookingCount) {
        this.checkedInBookingCount = checkedInBookingCount;
    }

    public int getCheckedOutBookingCount() {
        return checkedOutBookingCount;
    }

    public void setCheckedOutBookingCount(int checkedOutBookingCount) {
        this.checkedOutBookingCount = checkedOutBookingCount;
    }
}
