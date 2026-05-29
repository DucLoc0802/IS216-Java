package PetHotel.model;

public class ServiceReport {
    private String serviceName;
    private int usageCount;
    private double revenue;
    private double usageRate;

    public ServiceReport(String serviceName, int usageCount, double revenue, double usageRate) {
        this.serviceName = serviceName;
        this.usageCount = usageCount;
        this.revenue = revenue;
        this.usageRate = usageRate;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public double getRevenue() {
        return revenue;
    }

    public double getUsageRate() {
        return usageRate;
    }
}
