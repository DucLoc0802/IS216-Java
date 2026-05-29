package PetHotel.model;

public class RevenueReport {
    private String period;
    private int invoiceCount;
    private double totalRevenue;
    private double totalPaid;
    private double remaining;

    public RevenueReport() {
    }

    public RevenueReport(String period, int invoiceCount, double totalRevenue, double totalPaid, double remaining) {
        this.period = period;
        this.invoiceCount = invoiceCount;
        this.totalRevenue = totalRevenue;
        this.totalPaid = totalPaid;
        this.remaining = remaining;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public int getInvoiceCount() {
        return invoiceCount;
    }

    public void setInvoiceCount(int invoiceCount) {
        this.invoiceCount = invoiceCount;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(double totalPaid) {
        this.totalPaid = totalPaid;
    }

    public double getRemaining() {
        return remaining;
    }

    public void setRemaining(double remaining) {
        this.remaining = remaining;
    }
}
