package PetHotel.model;

public class InventoryStats {
    private int totalSku;
    private int okStock;
    private int lowStock;
    private int criticalStock;
    private int monthImport;

    public int getTotalSku() {
        return totalSku;
    }

    public void setTotalSku(int totalSku) {
        this.totalSku = totalSku;
    }

    public int getOkStock() {
        return okStock;
    }

    public void setOkStock(int okStock) {
        this.okStock = okStock;
    }

    public int getLowStock() {
        return lowStock;
    }

    public void setLowStock(int lowStock) {
        this.lowStock = lowStock;
    }

    public int getCriticalStock() {
        return criticalStock;
    }

    public void setCriticalStock(int criticalStock) {
        this.criticalStock = criticalStock;
    }

    public int getMonthImport() {
        return monthImport;
    }

    public void setMonthImport(int monthImport) {
        this.monthImport = monthImport;
    }
}
