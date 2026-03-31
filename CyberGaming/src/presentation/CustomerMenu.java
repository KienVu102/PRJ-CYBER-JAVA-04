package presentation;

import dao.UserDAO;
import model.*;
import service.*;
import util.ConsoleUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

// Màn hình khách hàng (Customer).
public class CustomerMenu {

    private User currentUser;
    private final PCService pcService = new PCService();
    private final FoodService foodService = new FoodService();
    private final BookingService bookingService = new BookingService();
    private final OrderService orderService = new OrderService();
    private final WalletService walletService = new WalletService();
    private final UserDAO userDAO = new UserDAO();

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public CustomerMenu(User currentUser) {
        this.currentUser = currentUser;
    }

    public void show() {
        while (true) {
            // Reload user để có số dư mới nhất
            User refreshed = userDAO.findById(currentUser.getId());
            if (refreshed != null) currentUser = refreshed;

            ConsoleUtil.clearScreen();
            ConsoleUtil.printHeader("KHÁCH HÀNG - " + currentUser.getFullName());
            System.out.printf("  Số dư ví: %s%n%n",
                    ConsoleUtil.formatCurrency(currentUser.getBalance()));

            System.out.println("  1. Xem & Đặt máy trạm");
            System.out.println("  2. Gọi đồ ăn / thức uống (F&B)");
            System.out.println("  3. Xem lịch sử đặt máy");
            System.out.println("  4. Xem lịch sử đơn hàng F&B");
            System.out.println("  5. Nạp tiền vào ví");
            System.out.println("  6. Lịch sử giao dịch");
            System.out.println("  0. Đăng xuất");
            System.out.println();

            int ch = ConsoleUtil.readInt("Chọn: ");
            switch (ch) {
                case 1 -> bookPC();
                case 2 -> orderFood();
                case 3 -> viewMyBookings();
                case 4 -> viewMyOrders();
                case 5 -> depositMoney();
                case 6 -> viewTransactions();
                case 0 -> { return; }
                default -> { ConsoleUtil.printError("Lựa chọn không hợp lệ."); ConsoleUtil.pressEnterToContinue(); }
            }
        }
    }

    // ============================================================
    // ĐẶT MÁY TRẠM
    // ============================================================
    private void bookPC() {
        ConsoleUtil.clearScreen();
        ConsoleUtil.printSubHeader("ĐẶT MÁY TRẠM");

        // Nhập thời gian trước
        System.out.println();
        ConsoleUtil.printInfo("Định dạng thời gian: dd/MM/yyyy HH:mm (VD: 25/06/2025 14:00)");
        LocalDateTime startTime = readDateTime("Thời gian bắt đầu: ");
        LocalDateTime endTime   = readDateTime("Thời gian kết thúc: ");

        // Hiển thị khu vực
        List<Category> categories = pcService.getAllCategories();
        if (categories.isEmpty()) {
            ConsoleUtil.printWarning("Chưa có khu vực nào được tạo.");
            ConsoleUtil.pressEnterToContinue();
            return;
        }
        
        System.out.println("Chọn khu vực:");
        for (Category c : categories) {
            System.out.printf("  [%d] %-15s - %s%n", c.getId(), c.getName(), c.getDescription());
        }
        System.out.println("  [0] Xem tất cả máy trống");
        System.out.println();

        int catChoice = ConsoleUtil.readInt("Khu vực (0 = tất cả): ");

        // Hiển thị máy trống CÓ THỂ ĐẶT TRONG KHUNG GIỜ ĐÃ CHỌN
        List<PC> availPCs = (catChoice == 0)
                ? pcService.getAvailablePCsForTimeSlot(startTime, endTime)
                : pcService.getAvailablePCsForTimeSlot(catChoice, startTime, endTime);

        if (availPCs.isEmpty()) {
            ConsoleUtil.printWarning("Không có máy trống trong khu vực này cho khung giờ đã chọn.");
            ConsoleUtil.pressEnterToContinue();
            return;
        }

        System.out.println();
        System.out.printf("%-4s %-8s %-12s %-40s %s%n",
                "ID", "Số máy", "Khu vực", "Cấu hình", "Giá/giờ");
        System.out.println("-".repeat(90));
        for (PC pc : availPCs) {
            System.out.printf("%-4d %-8s %-12s %-40s %,.0f VNĐ%n",
                    pc.getId(), pc.getPcNumber(), pc.getCategoryName(),
                    pc.getConfiguration(), pc.getPricePerHour());
        }

        System.out.println();
        int pcId = ConsoleUtil.readInt("Chọn ID máy (0 = hủy): ");
        if (pcId == 0) return;

        PC selectedPC = pcService.getPCById(pcId);
        if (selectedPC == null) {
            ConsoleUtil.printError("Máy không tồn tại.");
            ConsoleUtil.pressEnterToContinue();
            return;
        }

        // Tính tiền và xác nhận
        double hours = java.time.Duration.between(startTime, endTime).toMinutes() / 60.0;
        double total = hours * selectedPC.getPricePerHour();
        System.out.println();
        System.out.printf("  Máy       : %s (%s)%n", selectedPC.getPcNumber(), selectedPC.getCategoryName());
        System.out.printf("  Thời gian : %.1f giờ%n", hours);
        System.out.printf("  Chi phí   : %s%n", ConsoleUtil.formatCurrency(total));
        System.out.printf("  Số dư ví  : %s%n", ConsoleUtil.formatCurrency(currentUser.getBalance()));
        System.out.println();

        if (currentUser.getBalance() < total) {
            ConsoleUtil.printWarning("Số dư ví không đủ! Vui lòng nạp thêm tiền.");
            ConsoleUtil.pressEnterToContinue();
            return;
        }

        String confirm = ConsoleUtil.readLine("Xác nhận đặt máy? (y/n): ");
        if (!confirm.equalsIgnoreCase("y")) {
            ConsoleUtil.printWarning("Đã hủy đặt máy.");
            ConsoleUtil.pressEnterToContinue();
            return;
        }

        String err = bookingService.createBooking(currentUser.getId(), pcId, startTime, endTime);
        if (err != null) ConsoleUtil.printError(err);
        else {
            ConsoleUtil.printSuccess("Đặt máy thành công! Đơn của bạn đang chờ xác nhận từ nhân viên.");
            System.out.println();
            String orderFoodNow = ConsoleUtil.readLine("Bạn có muốn gọi đồ ăn/uống ngay? (y/n): ");
            if (orderFoodNow.equalsIgnoreCase("y")) {
                // Lấy bookingId vừa tạo
                List<Booking> myBookings = bookingService.getBookingsByUser(currentUser.getId());
                if (!myBookings.isEmpty()) {
                    orderFoodWithBooking(myBookings.get(0).getId());
                }
            }
        }
        ConsoleUtil.pressEnterToContinue();
    }


    //Gọi đồ ăn thức uống
    private void orderFood() {
        ConsoleUtil.clearScreen();
        ConsoleUtil.printSubHeader("MENU ĐỒ ĂN & THỨC UỐNG");

        // Lấy booking hiện tại (nếu có)
        Integer bookingId = null;
        List<Booking> myBookings = bookingService.getBookingsByUser(currentUser.getId());
        List<Booking> activeBookings = myBookings.stream()
                .filter(b -> b.getStatus() == Booking.Status.CONFIRMED
                        || b.getStatus() == Booking.Status.IN_SERVICE
                        || b.getStatus() == Booking.Status.PENDING)
                .toList();

        if (!activeBookings.isEmpty()) {
            System.out.println("Đơn đặt máy đang hoạt động:");
            for (Booking b : activeBookings) {
                System.out.printf("  [%d] %s - %s%n", b.getId(), b.getPcNumber(), b.getStatusDisplay());
            }
            String linkBooking = ConsoleUtil.readLine("Liên kết đơn hàng với đặt máy? Nhập ID (Enter để bỏ qua): ");
            if (!linkBooking.isBlank()) {
                try { bookingId = Integer.parseInt(linkBooking); }
                catch (NumberFormatException ignored) {}
            }
        }

        orderFoodWithBooking(bookingId);
    }

    private void orderFoodWithBooking(Integer bookingId) {
        List<Food> foods = foodService.getAvailableFoods();
        if (foods.isEmpty()) {
            ConsoleUtil.printWarning("Hiện tại menu trống.");
            ConsoleUtil.pressEnterToContinue();
            return;
        }

        System.out.println();
        System.out.printf("%-4s %-25s %-30s %-12s %s%n", "ID", "Tên món", "Mô tả", "Giá", "Còn lại");
        System.out.println("-".repeat(90));
        for (Food f : foods) {
            System.out.printf("%-4d %-25s %-30s %,8.0f    %d%n",
                    f.getId(), f.getName(), f.getDescription(), f.getPrice(), f.getStock());
        }

        // Giỏ hàng
        Map<Integer, Integer> cart = new LinkedHashMap<>();
        System.out.println();
        ConsoleUtil.printInfo("Thêm món vào giỏ hàng (nhập 0 để hoàn tất).");

        while (true) {
            int foodId = ConsoleUtil.readInt("ID món (0 = hoàn tất): ");
            if (foodId == 0) break;
            Food f = foodService.getFoodById(foodId);
            if (f == null) { ConsoleUtil.printError("Món không tồn tại."); continue; }
            int qty = ConsoleUtil.readInt("Số lượng: ");
            if (qty <= 0) continue;
            cart.merge(foodId, qty, Integer::sum);
            ConsoleUtil.printInfo("Đã thêm: " + f.getName() + " x" + qty);
        }

        if (cart.isEmpty()) {
            ConsoleUtil.printWarning("Giỏ hàng trống, đã hủy.");
            ConsoleUtil.pressEnterToContinue();
            return;
        }

        // Hiển thị tóm tắt đơn
        System.out.println();
        ConsoleUtil.printSubHeader("TÓM TẮT ĐƠN HÀNG");
        double total = 0;
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Food f = foodService.getFoodById(entry.getKey());
            double sub = f.getPrice() * entry.getValue();
            total += sub;
            System.out.printf("  %-25s x%d  %,.0f VNĐ%n", f.getName(), entry.getValue(), sub);
        }
        System.out.printf("  %-28s %,.0f VNĐ%n", "TỔNG CỘNG:", total);

        if (currentUser.getBalance() < total) {
            ConsoleUtil.printWarning("Số dư ví không đủ! Cần " + ConsoleUtil.formatCurrency(total)
                    + ", hiện có " + ConsoleUtil.formatCurrency(currentUser.getBalance()));
            ConsoleUtil.pressEnterToContinue();
            return;
        }

        String confirm = ConsoleUtil.readLine("Xác nhận đặt hàng? (y/n): ");
        if (!confirm.equalsIgnoreCase("y")) {
            ConsoleUtil.printWarning("Đã hủy đặt hàng.");
            ConsoleUtil.pressEnterToContinue();
            return;
        }

        String err = orderService.createOrder(currentUser.getId(), bookingId, cart);
        if (err != null) ConsoleUtil.printError(err);
        else ConsoleUtil.printSuccess("Đặt hàng thành công! Nhân viên sẽ chuẩn bị ngay.");
        ConsoleUtil.pressEnterToContinue();
    }

    //Lịch sử đặt máy
    private void viewMyBookings() {
        ConsoleUtil.clearScreen();
        ConsoleUtil.printSubHeader("LỊCH SỬ ĐẶT MÁY CỦA BẠN");
        List<Booking> bookings = bookingService.getBookingsByUser(currentUser.getId());
        if (bookings.isEmpty()) {
            ConsoleUtil.printInfo("Bạn chưa có đơn đặt máy nào.");
        } else {
            System.out.printf("%-4s %-10s %-20s %-20s %-15s %-12s%n",
                    "ID", "Máy", "Bắt đầu", "Kết thúc", "Trạng thái", "Tiền");
            System.out.println("-".repeat(90));
            for (Booking b : bookings) {
                System.out.printf("%-4d %-10s %-20s %-20s %-15s %,.0f%n",
                        b.getId(), b.getPcNumber(),
                        b.getStartTime().toString().replace("T", " "),
                        b.getEndTime().toString().replace("T", " "),
                        b.getStatusDisplay(), b.getTotalAmount());
            }
            System.out.println();
            System.out.println("  1. Hủy đơn đặt máy");
            System.out.println("  0. Quay lại");
            int ch = ConsoleUtil.readInt("Chọn: ");
            if (ch == 1) {
                int bid = ConsoleUtil.readInt("ID đơn cần hủy: ");
                String err = bookingService.cancelBooking(bid, currentUser.getId(), false);
                if (err != null) ConsoleUtil.printError(err);
                else ConsoleUtil.printSuccess("Hủy đơn thành công!");
            }
        }
        ConsoleUtil.pressEnterToContinue();
    }

    //Lịch sử đặt đồ ăn
    private void viewMyOrders() {
        ConsoleUtil.clearScreen();
        ConsoleUtil.printSubHeader("LỊCH SỬ ĐẶT ĐỒ ĂN");
        List<Order> orders = orderService.getOrdersByUser(currentUser.getId());
        if (orders.isEmpty()) {
            ConsoleUtil.printInfo("Bạn chưa có đơn hàng nào.");
        } else {
            for (Order o : orders) {
                System.out.printf("[%d] %s | %s | %,.0f VNĐ%n",
                        o.getId(),
                        o.getCreatedAt().toString().replace("T", " "),
                        o.getStatusDisplay(),
                        o.getTotalAmount());
                for (Order.OrderItem item : o.getItems()) {
                    System.out.println("     " + item);
                }
                System.out.println();
            }
        }
        ConsoleUtil.pressEnterToContinue();
    }

    //Nạp tiền
    private void depositMoney() {
        ConsoleUtil.clearScreen();
        ConsoleUtil.printSubHeader("NẠP TIỀN VÀO VÍ");
        System.out.printf("Số dư hiện tại: %s%n", ConsoleUtil.formatCurrency(currentUser.getBalance()));
        double amount = ConsoleUtil.readDouble("Số tiền muốn nạp (VNĐ): ");
        if (amount <= 0) {
            ConsoleUtil.printError("Số tiền không hợp lệ.");
            ConsoleUtil.pressEnterToContinue();
            return;
        }
        String err = walletService.deposit(currentUser.getId(), amount, "Nạp tiền thủ công");
        if (err != null) ConsoleUtil.printError(err);
        else {
            currentUser.setBalance(currentUser.getBalance() + amount);
            ConsoleUtil.printSuccess("Nạp tiền thành công! Số dư mới: "
                    + ConsoleUtil.formatCurrency(currentUser.getBalance()));
        }
        ConsoleUtil.pressEnterToContinue();
    }

    //Lichj suwr giao dịch
    private void viewTransactions() {
        ConsoleUtil.clearScreen();
        ConsoleUtil.printSubHeader("LỊCH SỬ GIAO DỊCH");
        var transactions = walletService.getHistory(currentUser.getId());
        if (transactions.isEmpty()) {
            ConsoleUtil.printInfo("Chưa có giao dịch nào.");
        } else {
            System.out.printf("%-20s %-12s %-12s %s%n", "Thời gian", "Loại", "Số tiền", "Mô tả");
            System.out.println("-".repeat(80));
            for (var t : transactions) {
                String sign = (t.getType() == Transaction.Type.PAYMENT) ? "-" : "+";
                System.out.printf("%-20s %-12s %s%,.0f     %s%n",
                        t.getCreatedAt().toString().replace("T", " "),
                        t.getTypeDisplay(),
                        sign, t.getAmount(),
                        t.getDescription());
            }
        }
        System.out.printf("%nSố dư hiện tại: %s%n",
                ConsoleUtil.formatCurrency(currentUser.getBalance()));
        ConsoleUtil.pressEnterToContinue();
    }


    private LocalDateTime readDateTime(String prompt) {
        while (true) {
            String input = ConsoleUtil.readLine(prompt);
            
            // Validate input not empty
            if (input == null || input.isBlank()) {
                ConsoleUtil.printError("Vui lòng nhập thời gian.");
                continue;
            }
            
            try {
                LocalDateTime dateTime = LocalDateTime.parse(input, DT_FMT);
                
                // Validate not in past (allow 5 minutes buffer)
                if (dateTime.isBefore(LocalDateTime.now().minusMinutes(5))) {
                    ConsoleUtil.printError("Thời gian không được trong quá khứ.");
                    continue;
                }
                
                return dateTime;
            } catch (DateTimeParseException e) {
                ConsoleUtil.printError("Định dạng thời gian không đúng. Ví dụ: 25/06/2025 14:00");
            }
        }
    }
}
