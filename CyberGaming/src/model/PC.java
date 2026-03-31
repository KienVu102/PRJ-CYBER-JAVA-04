package model;

public class PC {

    public enum Status { AVAILABLE, IN_USE, MAINTENANCE }

    private int id;
    private String pcNumber;
    private int categoryId;
    private String categoryName;
    private String configuration;
    private double pricePerHour;
    private Status status;

    public PC() {}

    public PC(int id, String pcNumber, int categoryId, String configuration,
              double pricePerHour, Status status) {
        this.id = id;
        this.pcNumber = pcNumber;
        this.categoryId = categoryId;
        this.configuration = configuration;
        this.pricePerHour = pricePerHour;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPcNumber() { return pcNumber; }
    public void setPcNumber(String pcNumber) { this.pcNumber = pcNumber; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getConfiguration() { return configuration; }
    public void setConfiguration(String configuration) { this.configuration = configuration; }

    public double getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(double pricePerHour) { this.pricePerHour = pricePerHour; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getStatusDisplay() {
        return switch (status) {
            case AVAILABLE   -> "Trống";
            case IN_USE      -> "Đang sử dụng";
            case MAINTENANCE -> "Bảo trì";
        };
    }

    @Override
    public String toString() {
        return String.format("%-8s | %-12s | %-40s | %,8.0f VNĐ/h | %s",
                pcNumber, categoryName != null ? categoryName : "KV-" + categoryId,
                configuration, pricePerHour, getStatusDisplay());
    }
}
