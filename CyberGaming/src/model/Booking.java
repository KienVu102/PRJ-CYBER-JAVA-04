package model;

import java.time.LocalDateTime;
import java.time.Duration;

// Thực thể đặt máy trạm.
public class Booking {

    public enum Status { PENDING, CONFIRMED, IN_SERVICE, COMPLETED, CANCELLED }

    private int id;
    private int userId;
    private String username;     // join
    private int pcId;
    private String pcNumber;     // join
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Status status;
    private double totalAmount;

    public Booking() {}

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getPcId() { return pcId; }
    public void setPcId(int pcId) { this.pcId = pcId; }

    public String getPcNumber() { return pcNumber; }
    public void setPcNumber(String pcNumber) { this.pcNumber = pcNumber; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatusDisplay() {
        return switch (status) {
            case PENDING    -> "Chờ xác nhận";
            case CONFIRMED  -> "Đã xác nhận";
            case IN_SERVICE -> "Đang phục vụ";
            case COMPLETED  -> "Hoàn thành";
            case CANCELLED  -> "Đã hủy";
        };
    }

    // Tính số giờ sử dụng
    public double getHours() {
        Duration duration = Duration.between(startTime, endTime);
        return duration.toMinutes() / 60.0;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s | %s → %s | %s | %,.0f VNĐ",
                id,
                pcNumber != null ? pcNumber : "PC-" + pcId,
                username != null ? username : "User-" + userId,
                startTime.toString().replace("T", " "),
                endTime.toString().replace("T", " "),
                getStatusDisplay(),
                totalAmount);
    }
}
