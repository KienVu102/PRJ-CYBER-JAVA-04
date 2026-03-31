package model;

// Thực thể đồ ăn / thức uống trong menu F&B.
public class Food {

    private int id;
    private String name;
    private String description;
    private double price;
    private int stock;
    private boolean available;

    public Food() {}

    public Food(int id, String name, String description, double price, int stock, boolean available) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.available = available;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return String.format("[%2d] %-25s | %,8.0f VNĐ | Tồn kho: %d",
                id, name, price, stock);
    }
}
