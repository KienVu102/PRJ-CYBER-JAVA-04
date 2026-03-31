package model;

import java.time.LocalDateTime;

// Lịch sử giao dịch ví điện tử.
public class Transaction {

    public enum Type { DEPOSIT, PAYMENT, REFUND }

    private int id;
    private int userId;
    private double amount;
    private Type type;
    private String description;
    private LocalDateTime createdAt;

    public Transaction() {}

    public Transaction(int userId, double amount, Type type, String description) {
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getTypeDisplay() {
        return switch (type) {
            case DEPOSIT -> "Nạp tiền";
            case PAYMENT -> "Thanh toán";
            case REFUND  -> "Hoàn tiền";
        };
    }

    @Override
    public String toString() {
        String sign = (type == Type.DEPOSIT || type == Type.REFUND) ? "+" : "-";
        return String.format("[%s] %s%,.0f VNĐ | %s | %s",
                createdAt != null ? createdAt.toString().replace("T"," ") : "",
                sign, amount, getTypeDisplay(), description);
    }
}
