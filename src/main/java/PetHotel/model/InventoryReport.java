package PetHotel.model;

public class InventoryReport {
    private String scope;
    private int totalSku;
    private double totalStock;
    private int lowStockCount;
    private int outOfStockCount;

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public int getTotalSku() {
        return totalSku;
    }

    public void setTotalSku(int totalSku) {
        this.totalSku = totalSku;
    }

    public double getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(double totalStock) {
        this.totalStock = totalStock;
    }

    public int getLowStockCount() {
        return lowStockCount;
    }

    public void setLowStockCount(int lowStockCount) {
        this.lowStockCount = lowStockCount;
    }

    public int getOutOfStockCount() {
        return outOfStockCount;
    }

    public void setOutOfStockCount(int outOfStockCount) {
        this.outOfStockCount = outOfStockCount;
    }
}
