package PetHotel.model;

public class InventoryItemReport {
    private String productName;
    private double currentStock;
    private double minimumStock;
    private String unit;
    private String status;
    private int usageCount;

    public InventoryItemReport(
            String productName,
            double currentStock,
            double minimumStock,
            String unit,
            String status,
            int usageCount) {
        this.productName = productName;
        this.currentStock = currentStock;
        this.minimumStock = minimumStock;
        this.unit = unit;
        this.status = status;
        this.usageCount = usageCount;
    }

    public String getProductName() {
        return productName;
    }

    public double getCurrentStock() {
        return currentStock;
    }

    public double getMinimumStock() {
        return minimumStock;
    }

    public String getUnit() {
        return unit;
    }

    public String getStatus() {
        return status;
    }

    public int getUsageCount() {
        return usageCount;
    }
}
