package presentation;

import model.*;
import service.*;
import util.ConsoleUtil;

import java.util.List;

public class StaffMenu {

    private final User currentUser;
    private final BookingService bookingService = new BookingService();
    private final OrderService orderService = new OrderService();
    private final PCService pcService = new PCService();

    public StaffMenu(User currentUser) {
        this.currentUser = currentUser;
    }

    public void show() {
        while (true) {
            ConsoleUtil.clearScreen();
            ConsoleUtil.printHeader("NHÂN VIÊN - " + currentUser.getFullName());
            System.out.println("  1. Xem & xử lý đơn đặt máy");
            System.out.println("  2. Xem & xử lý đơn hàng F&B");
            System.out.println("  3. Xem trạng thái tất cả máy");
            System.out.println("  0. Đăng xuất");
            System.out.println();

            int ch = ConsoleUtil.readInt("Chọn: ");
            switch (ch) {
                case 1 -> manageBookings();
                case 2 -> manageOrders();
                case 3 -> viewAllPCs();
                case 0 -> { return; }
                default -> ConsoleUtil.printError("Lựa chọn không hợp lệ.");
            }
        }
    }

    // ============================================================
    // XỬ LÝ ĐẶT MÁY
    // ============================================================
    private void manageBookings() {
        while (true) {
            ConsoleUtil.clearScreen();
            ConsoleUtil.printSubHeader("DANH SÁCH ĐƠN ĐẶT MÁY ĐANG CHỜ XỬ LÝ");

            List<Booking> bookings = bookingService.getPendingBookings();
            if (bookings.isEmpty()) {
                ConsoleUtil.printInfo("Không có đơn đặt máy nào cần xử lý.");
                ConsoleUtil.pressEnterToContinue();
                return;
            }

            System.out.printf("%-4s %-10s %-12s %-20s %-20s %-15s %-12s%n",
                    "ID", "Khách", "Máy", "Bắt đầu", "Kết thúc", "Trạng thái", "Tổng tiền");
            System.out.println("-".repeat(100));
            for (Booking b : bookings) {
                System.out.printf("%-4d %-10s %-12s %-20s %-20s %-15s %,.0f%n",
                        b.getId(),
                        b.getUsername(),
                        b.getPcNumber(),
                        b.getStartTime().toString().replace("T", " "),
                        b.getEndTime().toString().replace("T", " "),
                        b.getStatusDisplay(),
                        b.getTotalAmount());
            }

            System.out.println();
            System.out.println("  1. Xác nhận đơn (PENDING → CONFIRMED)");
            System.out.println("  2. Bắt đầu phục vụ (CONFIRMED → IN_SERVICE)");
            System.out.println("  3. Hoàn thành & Thu tiền (IN_SERVICE → COMPLETED)");
            System.out.println("  4. Hủy đơn");
            System.out.println("  0. Quay lại");

            int ch = ConsoleUtil.readInt("Chọn: ");
            if (ch == 0) return;

            int bookingId = ConsoleUtil.readInt("Nhập ID đơn đặt máy: ");
            String err = switch (ch) {
                case 1 -> bookingService.confirmBooking(bookingId);
                case 2 -> bookingService.startService(bookingId);
                case 3 -> bookingService.completeBooking(bookingId);
                case 4 -> bookingService.cancelBooking(bookingId, currentUser.getId(), true);
                default -> "Lựa chọn không hợp lệ.";
            };

            if (err != null) ConsoleUtil.printError(err);
            else ConsoleUtil.printSuccess("Cập nhật đơn đặt máy #" + bookingId + " thành công!");
            ConsoleUtil.pressEnterToContinue();
        }
    }

    // ============================================================
    // XỬ LÝ ĐƠN HÀNG F&B
    // ============================================================
    private void manageOrders() {
        while (true) {
            ConsoleUtil.clearScreen();
            ConsoleUtil.printSubHeader("DANH SÁCH ĐƠN HÀNG F&B ĐANG CHỜ XỬ LÝ");

            List<Order> orders = orderService.getPendingOrders();
            if (orders.isEmpty()) {
                ConsoleUtil.printInfo("Không có đơn hàng nào cần xử lý.");
                ConsoleUtil.pressEnterToContinue();
                return;
            }

            for (Order o : orders) {
                System.out.printf("[%d] %s | %s | %,.0f VNĐ%n",
                        o.getId(), o.getUsername(), o.getStatusDisplay(), o.getTotalAmount());
                for (Order.OrderItem item : o.getItems()) {
                    System.out.println("     " + item);
                }
                System.out.println();
            }

            System.out.println("  1. Xác nhận đơn hàng (PENDING → CONFIRMED)");
            System.out.println("  2. Bắt đầu chuẩn bị (CONFIRMED → IN_SERVICE)");
            System.out.println("  3. Hoàn thành & Thu tiền (IN_SERVICE → COMPLETED)");
            System.out.println("  4. Hủy đơn hàng");
            System.out.println("  0. Quay lại");

            int ch = ConsoleUtil.readInt("Chọn: ");
            if (ch == 0) return;

            int orderId = ConsoleUtil.readInt("Nhập ID đơn hàng: ");
            String err = switch (ch) {
                case 1 -> orderService.confirmOrder(orderId);
                case 2 -> orderService.startPreparing(orderId);
                case 3 -> orderService.completeOrder(orderId);
                case 4 -> orderService.cancelOrder(orderId, currentUser.getId(), true);
                default -> "Lựa chọn không hợp lệ.";
            };

            if (err != null) ConsoleUtil.printError(err);
            else ConsoleUtil.printSuccess("Cập nhật đơn hàng #" + orderId + " thành công!");
            ConsoleUtil.pressEnterToContinue();
        }
    }

    // ============================================================
    // XEM TRẠNG THÁI MÁY
    // ============================================================
    private void viewAllPCs() {
        ConsoleUtil.clearScreen();
        ConsoleUtil.printSubHeader("TRẠNG THÁI TẤT CẢ MÁY TRẠM");
        List<PC> pcs = pcService.getAllPCs();
        System.out.printf("%-8s %-14s %-12s %-40s %s%n",
                "Số máy", "Khu vực", "Giá/giờ", "Cấu hình", "Trạng thái");
        System.out.println("-".repeat(100));
        for (PC pc : pcs) {
            System.out.printf("%-8s %-14s %,10.0f   %-40s %s%n",
                    pc.getPcNumber(),
                    pc.getCategoryName(),
                    pc.getPricePerHour(),
                    pc.getConfiguration(),
                    pc.getStatusDisplay());
        }
        ConsoleUtil.pressEnterToContinue();
    }
}
