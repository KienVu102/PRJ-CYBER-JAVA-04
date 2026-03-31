package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Thực thể đơn hàng F&B.
public class Order {

    public enum Status { PENDING, CONFIRMED, IN_SERVICE, COMPLETED, CANCELLED }

    private int id;
    private int userId;
    private String username;      // join
    private Integer bookingId;    // nullable
    private Status status;
    private double totalAmount;
    private LocalDateTime createdAt;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public String getStatusDisplay() {
        return switch (status) {
            case PENDING    -> "Chờ xác nhận";
            case CONFIRMED  -> "Đã xác nhận";
            case IN_SERVICE -> "Đang phục vụ";
            case COMPLETED  -> "Hoàn thành";
            case CANCELLED  -> "Đã hủy";
        };
    }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s | %s | %,.0f VNĐ",
                id,
                username != null ? username : "User-" + userId,
                createdAt != null ? createdAt.toString().replace("T"," ") : "",
                getStatusDisplay(),
                totalAmount);
    }

    // -----------------------------------------------
    // Lớp nội: Chi tiết đơn hàng
    // -----------------------------------------------
    public static class OrderItem {
        private int id;
        private int orderId;
        private int foodId;
        private String foodName;  // join
        private int quantity;
        private double unitPrice;

        public OrderItem() {}

        public OrderItem(int foodId, String foodName, int quantity, double unitPrice) {
            this.foodId = foodId;
            this.foodName = foodName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public int getOrderId() { return orderId; }
        public void setOrderId(int orderId) { this.orderId = orderId; }

        public int getFoodId() { return foodId; }
        public void setFoodId(int foodId) { this.foodId = foodId; }

        public String getFoodName() { return foodName; }
        public void setFoodName(String foodName) { this.foodName = foodName; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public double getUnitPrice() { return unitPrice; }
        public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

        public double getSubtotal() { return quantity * unitPrice; }

        @Override
        public String toString() {
            return String.format("  %-25s x%d  %,.0f VNĐ", foodName, quantity, getSubtotal());
        }
    }
}
