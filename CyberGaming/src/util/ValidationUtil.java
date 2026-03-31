package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ValidationUtil {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private ValidationUtil() {}

    // ============================================================
    // STRING VALIDATION
    // ============================================================

    // Kiểm tra tên đăng nhập: phải có 4+ ký tự, chữ số + dấu gạch dưới
    public static boolean isValidUsername(String username) {
        if (username == null || username.length() < 4) return false;
        return username.matches("^[a-zA-Z0-9_]{4,}$");
    }

    //Validate mk phai > 6
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) return false;
        return !password.contains(" ");
    }

    //Validate sdt phải dau 03 07 08 09 va co 10 so
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isBlank()) return false; // Required field
        return phone.matches("^(03|07|08|09)\\d{8}$");
    }

    //Validate ten
    public static boolean isValidFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) return false;
        
        // Check length
        String trimmed = fullName.trim();
        if (trimmed.length() < 2 || fullName.length() > 100) return false;
        
        // Check for multiple consecutive spaces
        if (fullName.contains("  ")) return false;
        
        // Check for leading or trailing spaces
        if (!fullName.equals(trimmed)) return false;
        
        return true;
    }

    // Kiểm tra số máy: định dạng như PC-S01, PC-V02, v.v.
    public static boolean isValidPCNumber(String pcNumber) {
        if (pcNumber == null || pcNumber.isBlank()) return false;
        return pcNumber.matches("^PC-[A-Z]+\\d{2,}$");
    }

    // Kiểm tra tên món/máy: không rỗng, độ dài hợp lý
    public static boolean isValidName(String name) {
        if (name == null || name.isBlank()) return false;
        return name.length() >= 2 && name.length() <= 100;
    }

    // ============================================================
    // NUMERIC VALIDATION
    // ============================================================

    // Kiểm tra giá: phải là số dương
    public static boolean isValidPrice(double price) {
        return price > 0 && price < 1_000_000_000; // Reasonable upper limit
    }

    // Kiểm tra số lượng: phải là số nguyên dương
    public static boolean isValidQuantity(int quantity) {
        return quantity > 0 && quantity <= 10_000; // Reasonable upper limit
    }

    // Kiểm tra số tiền: phải không âm
    public static boolean isValidAmount(double amount) {
        return amount >= 0 && amount < 1_000_000_000;
    }

    // Kiểm tra ID: phải là số nguyên dương
    public static boolean isValidId(int id) {
        return id > 0;
    }

    // Kiểm tra phần trăm (0-100)
    public static boolean isValidPercentage(int percentage) {
        return percentage >= 0 && percentage <= 100;
    }

    // ============================================================
    // DATETIME VALIDATION
    // ============================================================

    // Kiểm tra định dạng ngày giờ: dd/MM/yyyy HH:mm
    public static boolean isValidDateTimeFormat(String dateTimeStr) {
        try {
            LocalDateTime.parse(dateTimeStr, DT_FMT);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    //Phân tích cú pháp ngày giờ
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr, DT_FMT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    //Thời gian hết muốn chơi phải sau thời gian mới tạo máy chơi
    public static boolean isValidTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) return false;
        return endTime.isAfter(startTime);
    }


    public static boolean isValidBookingTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (!isValidTimeRange(startTime, endTime)) return false;
        
        // Thời gian đặt máy ko được quá 5p trước
        if (startTime.isBefore(LocalDateTime.now().minusMinutes(5))) return false;
        
        // Tối thiểu chới từ 15p
        long minutes = java.time.Duration.between(startTime, endTime).toMinutes();
        return minutes >= 15 && minutes <= 2880;
    }



    //Validate tên đăng nhập
    public static String validateRegistration(String fullName, String username, 
                                             String password, String phone) {
        if (!isValidFullName(fullName))
            return "Họ tên phải từ 2-100 ký tự, không có khoảng trắng ở đầu/cuối và chỉ cách 1 khoảng trắng giữa các từ.";
        
        if (!isValidUsername(username))
            return "Tên đăng nhập phải từ 4+ ký tự (chữ, số, dấu gạch dưới).";
        
        if (!isValidPassword(password))
            return "Mật khẩu phải từ 6+ ký tự và không chứa khoảng trắng.";
        
        if (!isValidPhone(phone))
            return "Số điện thoại không được để trống, phải bắt đầu từ 03, 07, 08, 09 và có đúng 10 số.";
        
        return null;
    }


    public static String validatePCCreation(String pcNumber, int categoryId, 
                                           String configuration, double pricePerHour) {
        if (!isValidPCNumber(pcNumber))
            return "Số máy không hợp lệ (VD: PC-S01).";
        
        if (!isValidId(categoryId))
            return "Khu vực không hợp lệ.";
        
        if (configuration == null || configuration.isBlank() || configuration.length() > 255)
            return "Cấu hình không hợp lệ.";
        
        if (!isValidPrice(pricePerHour))
            return "Giá phải > 0.";
        
        return null;
    }

    //Validate tên món ăn
    public static String validateFoodCreation(String name, String description, 
                                             double price, int stock) {
        if (!isValidName(name))
            return "Tên món phải từ 2-100 ký tự.";
        
        if (description != null && description.length() > 255)
            return "Mô tả không được vượt quá 255 ký tự.";
        
        if (!isValidPrice(price))
            return "Giá phải > 0.";
        
        if (stock < 0 || stock > 100_000)
            return "Số lượng không hợp lệ.";
        
        return null;
    }

    //Validate đặt máy
    public static String validateBookingCreation(int pcId, LocalDateTime startTime, 
                                                 LocalDateTime endTime) {
        if (!isValidId(pcId))
            return "Máy không hợp lệ.";
        
        if (!isValidBookingTime(startTime, endTime))
            return "Thời gian không hợp lệ (15 phút - 48 giờ, không quá khứ).";
        
        return null;
    }

    //Validate số lượng món ăn
    public static String validateOrderItem(int foodId, int quantity) {
        if (!isValidId(foodId))
            return "Món ăn không hợp lệ.";
        
        if (!isValidQuantity(quantity))
            return "Số lượng phải từ 1-10,000.";
        
        return null;
    }
}
