package presentation;

import model.*;
import service.*;
import util.ConsoleUtil;

import java.util.List;

// Màn hình quản trị viên (Admin).
public class AdminMenu {

    private final User currentUser;
    private final PCService pcService = new PCService();
    private final FoodService foodService = new FoodService();
    private final BookingService bookingService = new BookingService();
    private final OrderService orderService = new OrderService();
    private final WalletService walletService = new WalletService();

    public AdminMenu(User currentUser) {
        this.currentUser = currentUser;
    }

    public void show() {
        while (true) {
            ConsoleUtil.clearScreen();
            ConsoleUtil.printHeader("ADMIN - " + currentUser.getFullName());
            System.out.println("  1. Quản lý Máy trạm (PC)");
            System.out.println("  2. Quản lý Khu vực");
            System.out.println("  3. Quản lý Menu F&B");
            System.out.println("  4. Xem tất cả đặt máy");
            System.out.println("  5. Xem tất cả đơn hàng F&B");
            System.out.println("  6. Quản lý nhân viên");
            System.out.println("  7. Quản lý khách hàng");
            System.out.println("  8. Báo cáo doanh thu");
            System.out.println("  0. Đăng xuất");
            System.out.println();

            int ch = ConsoleUtil.readInt("Chọn: ");
            switch (ch) {
                case 1 -> managePCs();
                case 2 -> manageCategories();
                case 3 -> manageFoods();
                case 4 -> viewAllBookings();
                case 5 -> viewAllOrders();
                case 6 -> manageStaff();
                case 7 -> manageCustomers();
                case 8 -> showRevenue();
                case 0 -> { return; }
                default -> ConsoleUtil.printError("Lựa chọn không hợp lệ.");
            }
        }
    }

    // ============================================================
    // QUẢN LÝ MÁY TRẠM
    // ============================================================
    private void managePCs() {
        while (true) {
            ConsoleUtil.clearScreen();
            ConsoleUtil.printSubHeader("QUẢN LÝ MÁY TRẠM");
            System.out.println("  1. Xem danh sách máy");
            System.out.println("  2. Thêm máy mới");
            System.out.println("  3. Sửa thông tin máy");
            System.out.println("  4. Xóa máy");
            System.out.println("  5. Cập nhật trạng thái máy");
            System.out.println("  0. Quay lại");
            int ch = ConsoleUtil.readInt("Chọn: ");
            switch (ch) {
                case 1 -> listPCs();
                case 2 -> addPC();
                case 3 -> editPC();
                case 4 -> deletePC();
                case 5 -> changeStatus();
                case 0 -> { return; }
                default -> ConsoleUtil.printError("Lựa chọn không hợp lệ.");
            }
            ConsoleUtil.pressEnterToContinue();
        }
    }

    private void listPCs() {
        List<PC> pcs = pcService.getAllPCs();
        ConsoleUtil.printSubHeader("DANH SÁCH MÁY TRẠM (" + pcs.size() + " máy)");
        if (pcs.isEmpty()) { ConsoleUtil.printWarning("Chưa có máy nào."); return; }
        System.out.printf("%-4s %-8s %-12s %-40s %-12s %s%n",
                "ID", "Số máy", "Khu vực", "Cấu hình", "Giá/giờ", "Trạng thái");
        System.out.println("-".repeat(100));
        for (PC pc : pcs) {
            System.out.printf("%-4d %-8s %-12s %-40s %,10.0f  %s%n",
                    pc.getId(), pc.getPcNumber(),
                    pc.getCategoryName() != null ? pc.getCategoryName() : "",
                    pc.getConfiguration(), pc.getPricePerHour(), pc.getStatusDisplay());
        }
    }

    private void addPC() {
        ConsoleUtil.printSubHeader("THÊM MÁY MỚI");
        listCategoriesShort();
        int catId  = ConsoleUtil.readInt("Khu vực (ID): ");
        String num = ConsoleUtil.readLine("Số máy (VD: PC-S05): ");
        String cfg = ConsoleUtil.readLine("Cấu hình: ");
        double price = ConsoleUtil.readDouble("Giá tiền/giờ (VNĐ): ");
        String err = pcService.addPC(num, catId, cfg, price);
        if (err != null) ConsoleUtil.printError(err);
        else ConsoleUtil.printSuccess("Thêm máy thành công!");
    }

    private void editPC() {
        ConsoleUtil.printSubHeader("SỬA THÔNG TIN MÁY");
        listPCs();
        int id = ConsoleUtil.readInt("ID máy cần sửa: ");
        PC pc = pcService.getPCById(id);
        if (pc == null) { ConsoleUtil.printError("Không tìm thấy máy."); return; }
        listCategoriesShort();
        String num   = ConsoleUtil.readLine("Số máy [" + pc.getPcNumber() + "]: ");
        int catId    = ConsoleUtil.readInt("Khu vực ID [" + pc.getCategoryId() + "]: ");
        String cfg   = ConsoleUtil.readLine("Cấu hình [" + pc.getConfiguration() + "]: ");
        double price = ConsoleUtil.readDouble("Giá/giờ [" + pc.getPricePerHour() + "]: ");
        String err = pcService.updatePC(id,
                num.isBlank() ? pc.getPcNumber() : num,
                catId == 0 ? pc.getCategoryId() : catId,
                cfg.isBlank() ? pc.getConfiguration() : cfg,
                price == 0 ? pc.getPricePerHour() : price,
                pc.getStatus());
        if (err != null) ConsoleUtil.printError(err);
        else ConsoleUtil.printSuccess("Cập nhật máy thành công!");
    }

    private void deletePC() {
        listPCs();
        int id = ConsoleUtil.readInt("ID máy cần xóa: ");
        String confirm = ConsoleUtil.readLine("Xác nhận xóa máy #" + id + "? (yes): ");
        if (!confirm.equalsIgnoreCase("yes")) { ConsoleUtil.printWarning("Đã hủy."); return; }
        String err = pcService.deletePC(id);
        if (err != null) ConsoleUtil.printError(err);
        else ConsoleUtil.printSuccess("Xóa máy thành công!");
    }

    private void changeStatus() {
        listPCs();
        int id = ConsoleUtil.readInt("ID máy: ");
        PC pc = pcService.getPCById(id);
        if (pc == null) { ConsoleUtil.printError("Không tìm thấy máy."); return; }
        System.out.println("Trạng thái hiện tại: " + pc.getStatusDisplay());
        System.out.println("  1. AVAILABLE (Trống)");
        System.out.println("  2. IN_USE (Đang dùng)");
        System.out.println("  3. MAINTENANCE (Bảo trì)");
        int ch = ConsoleUtil.readInt("Chọn trạng thái: ");
        PC.Status status = switch (ch) {
            case 1 -> PC.Status.AVAILABLE;
            case 2 -> PC.Status.IN_USE;
            case 3 -> PC.Status.MAINTENANCE;
            default -> null;
        };
        if (status == null) { ConsoleUtil.printError("Lựa chọn không hợp lệ."); return; }
        String err = pcService.updateStatus(id, status);
        if (err != null) ConsoleUtil.printError(err);
        else ConsoleUtil.printSuccess("Cập nhật trạng thái thành công!");
    }

    private void listCategoriesShort() {
        System.out.println("Các khu vực:");
        for (Category c : pcService.getAllCategories()) {
            System.out.printf("  [%d] %s%n", c.getId(), c.getName());
        }
    }

    // ============================================================
    // QUẢN LÝ KHU VỰC
    // ============================================================
    private void manageCategories() {
        while (true) {
            ConsoleUtil.clearScreen();
            ConsoleUtil.printSubHeader("QUẢN LÝ KHU VỰC");
            List<Category> cats = pcService.getAllCategories();
            System.out.printf("%-4s %-20s %s%n", "ID", "Tên khu vực", "Mô tả");
            System.out.println("-".repeat(60));
            for (Category c : cats) {
                System.out.printf("%-4d %-20s %s%n", c.getId(), c.getName(), c.getDescription());
            }
            System.out.println();
            System.out.println("  1. Thêm khu vực");
            System.out.println("  2. Sửa khu vực");
            System.out.println("  3. Xóa khu vực");
            System.out.println("  0. Quay lại");
            int ch = ConsoleUtil.readInt("Chọn: ");
            switch (ch) {
                case 1 -> {
                    String name = ConsoleUtil.readLine("Tên khu vực: ");
                    String desc = ConsoleUtil.readLine("Mô tả: ");
                    String err = pcService.addCategory(name, desc);
                    if (err != null) ConsoleUtil.printError(err);
                    else ConsoleUtil.printSuccess("Thêm khu vực thành công!");
                }
                case 2 -> {
                    int id = ConsoleUtil.readInt("ID khu vực cần sửa: ");
                    String name = ConsoleUtil.readLine("Tên mới: ");
                    String desc = ConsoleUtil.readLine("Mô tả mới: ");
                    String err = pcService.updateCategory(id, name, desc);
                    if (err != null) ConsoleUtil.printError(err);
                    else ConsoleUtil.printSuccess("Cập nhật thành công!");
                }
                case 3 -> {
                    int id = ConsoleUtil.readInt("ID khu vực cần xóa: ");
                    String err = pcService.deleteCategory(id);
                    if (err != null) ConsoleUtil.printError(err);
                    else ConsoleUtil.printSuccess("Xóa thành công!");
                }
                case 0 -> { return; }
            }
            ConsoleUtil.pressEnterToContinue();
        }
    }

    // ============================================================
    // QUẢN LÝ MENU F&B
    // ============================================================
    private void manageFoods() {
        while (true) {
            ConsoleUtil.clearScreen();
            ConsoleUtil.printSubHeader("QUẢN LÝ MENU F&B");
            System.out.println("  1. Xem toàn bộ menu");
            System.out.println("  2. Thêm món mới");
            System.out.println("  3. Sửa món");
            System.out.println("  4. Xóa món");
            System.out.println("  5. Cập nhật tồn kho");
            System.out.println("  0. Quay lại");
            int ch = ConsoleUtil.readInt("Chọn: ");
            switch (ch) {
                case 1 -> listFoods();
                case 2 -> addFood();
                case 3 -> editFood();
                case 4 -> deleteFood();
                case 5 -> updateStock();
                case 0 -> { return; }
            }
            ConsoleUtil.pressEnterToContinue();
        }
    }

    private void listFoods() {
        List<Food> foods = foodService.getAllFoods();
        System.out.printf("%-4s %-25s %-30s %-10s %-6s %s%n",
                "ID", "Tên món", "Mô tả", "Giá", "Tồn kho", "Trạng thái");
        System.out.println("-".repeat(100));
        for (Food f : foods) {
            System.out.printf("%-4d %-25s %-30s %,8.0f   %-6d %s%n",
                    f.getId(), f.getName(), f.getDescription(),
                    f.getPrice(), f.getStock(),
                    f.isAvailable() ? "Có sẵn" : "Hết hàng");
        }
    }

    private void addFood() {
        ConsoleUtil.printSubHeader("THÊM MÓN MỚI");
        String name  = ConsoleUtil.readLine("Tên món: ");
        String desc  = ConsoleUtil.readLine("Mô tả: ");
        double price = ConsoleUtil.readDouble("Giá (VNĐ): ");
        int stock    = ConsoleUtil.readInt("Số lượng tồn kho: ");
        String err = foodService.addFood(name, desc, price, stock);
        if (err != null) ConsoleUtil.printError(err);
        else ConsoleUtil.printSuccess("Thêm món thành công!");
    }

    private void editFood() {
        listFoods();
        int id = ConsoleUtil.readInt("ID món cần sửa: ");
        Food f = foodService.getFoodById(id);
        if (f == null) { ConsoleUtil.printError("Không tìm thấy món."); return; }
        String name  = ConsoleUtil.readLine("Tên [" + f.getName() + "]: ");
        String desc  = ConsoleUtil.readLine("Mô tả [" + f.getDescription() + "]: ");
        double price = ConsoleUtil.readDouble("Giá [" + f.getPrice() + "]: ");
        int stock    = ConsoleUtil.readInt("Tồn kho [" + f.getStock() + "]: ");
        String avail = ConsoleUtil.readLine("Có sẵn? (y/n) [" + (f.isAvailable() ? "y" : "n") + "]: ");
        String err = foodService.updateFood(id,
                name.isBlank() ? f.getName() : name,
                desc.isBlank() ? f.getDescription() : desc,
                price == 0 ? f.getPrice() : price,
                stock == 0 ? f.getStock() : stock,
                avail.isBlank() ? f.isAvailable() : avail.equalsIgnoreCase("y"));
        if (err != null) ConsoleUtil.printError(err);
        else ConsoleUtil.printSuccess("Cập nhật món thành công!");
    }

    private void deleteFood() {
        listFoods();
        int id = ConsoleUtil.readInt("ID món cần xóa: ");
        String err = foodService.deleteFood(id);
        if (err != null) ConsoleUtil.printError(err);
        else ConsoleUtil.printSuccess("Xóa món thành công!");
    }

    private void updateStock() {
        listFoods();
        int id = ConsoleUtil.readInt("ID món: ");
        int stock = ConsoleUtil.readInt("Số lượng tồn kho mới: ");
        String err = foodService.updateStock(id, stock);
        if (err != null) ConsoleUtil.printError(err);
        else ConsoleUtil.printSuccess("Cập nhật tồn kho thành công!");
    }

    // ============================================================
    // XEM TẤT CẢ ĐẶT MÁY
    // ============================================================
    private void viewAllBookings() {
        ConsoleUtil.clearScreen();
        ConsoleUtil.printSubHeader("TẤT CẢ ĐƠN ĐẶT MÁY");
        List<Booking> bookings = bookingService.getAllBookings();
        if (bookings.isEmpty()) { ConsoleUtil.printWarning("Chưa có đơn đặt máy nào."); }
        else {
            System.out.printf("%-4s %-12s %-10s %-20s %-20s %-15s %-12s%n",
                    "ID", "Máy", "Khách", "Bắt đầu", "Kết thúc", "Trạng thái", "Tổng tiền");
            System.out.println("-".repeat(100));
            for (Booking b : bookings) {
                System.out.printf("%-4d %-12s %-10s %-20s %-20s %-15s %,.0f%n",
                        b.getId(), b.getPcNumber(), b.getUsername(),
                        b.getStartTime().toString().replace("T", " "),
                        b.getEndTime().toString().replace("T", " "),
                        b.getStatusDisplay(), b.getTotalAmount());
            }
        }
        ConsoleUtil.pressEnterToContinue();
    }

    // ============================================================
    // XEM TẤT CẢ ĐƠN HÀNG
    // ============================================================
    private void viewAllOrders() {
        ConsoleUtil.clearScreen();
        ConsoleUtil.printSubHeader("TẤT CẢ ĐƠN HÀNG F&B");
        List<Order> orders = orderService.getAllOrders();
        if (orders.isEmpty()) { ConsoleUtil.printWarning("Chưa có đơn hàng nào."); }
        else {
            for (Order o : orders) {
                System.out.printf("[%d] %s | %s | %s | %,.0f VNĐ%n",
                        o.getId(), o.getUsername(),
                        o.getCreatedAt().toString().replace("T", " "),
                        o.getStatusDisplay(), o.getTotalAmount());
                for (Order.OrderItem item : o.getItems()) {
                    System.out.println("    " + item);
                }
                System.out.println();
            }
        }
        ConsoleUtil.pressEnterToContinue();
    }

    // ============================================================
    // QUẢN LÝ NHÂN VIÊN
    // ============================================================
    private void manageStaff() {
        ConsoleUtil.clearScreen();
        ConsoleUtil.printSubHeader("QUẢN LÝ NHÂN VIÊN");
        System.out.println("  1. Tạo tài khoản nhân viên");
        System.out.println("  0. Quay lại");
        int ch = ConsoleUtil.readInt("Chọn: ");
        if (ch == 1) {
            String fullName = ConsoleUtil.readLine("Họ tên: ");
            String username = ConsoleUtil.readLine("Tên đăng nhập: ");
            String password = ConsoleUtil.readLine("Mật khẩu: ");
            String phone    = ConsoleUtil.readLine("Số điện thoại: ");
            AuthService authService = new AuthService();
            String err = authService.register(fullName, username, password, phone, User.Role.STAFF);
            if (err != null) ConsoleUtil.printError(err);
            else ConsoleUtil.printSuccess("Tạo tài khoản nhân viên thành công!");
        }
        ConsoleUtil.pressEnterToContinue();
    }

    // ============================================================
    // QUẢN LÝ KHÁCH HÀNG
    // ============================================================
    private void manageCustomers() {
        while (true) {
            ConsoleUtil.clearScreen();
            ConsoleUtil.printSubHeader("QUẢN LÝ KHÁCH HÀNG");
            System.out.println("  1. Xem danh sách khách hàng");
            System.out.println("  2. Sửa thông tin khách hàng");
            System.out.println("  3. Xóa khách hàng");
            System.out.println("  4. Nạp tiền cho khách hàng");
            System.out.println("  0. Quay lại");
            int ch = ConsoleUtil.readInt("Chọn: ");
            switch (ch) {
                case 1 -> listCustomers();
                case 2 -> editCustomer();
                case 3 -> deleteCustomer();
                case 4 -> addBalanceToCustomer();
                case 0 -> { return; }
                default -> ConsoleUtil.printError("Lựa chọn không hợp lệ.");
            }
            ConsoleUtil.pressEnterToContinue();
        }
    }

    private void listCustomers() {
        AuthService authService = new AuthService();
        List<User> customers = authService.userDAO.findByRole(User.Role.CUSTOMER);
        ConsoleUtil.printSubHeader("DANH SÁCH KHÁCH HÀNG (" + customers.size() + " khách)");
        if (customers.isEmpty()) { 
            ConsoleUtil.printWarning("Chưa có khách hàng nào."); 
            return; 
        }
        System.out.printf("%-4s %-20s %-15s %-15s %-15s %s%n",
                "ID", "Họ tên", "Tên đăng nhập", "Số điện thoại", "Số dư", "Trạng thái");
        System.out.println("-".repeat(100));
        for (User customer : customers) {
            System.out.printf("%-4d %-20s %-15s %-15s %,15.0f  %s%n",
                    customer.getId(), customer.getFullName(), customer.getUsername(),
                    customer.getPhone(), customer.getBalance(), "Hoạt động");
        }
    }

    private void editCustomer() {
        listCustomers();
        int id = ConsoleUtil.readInt("ID khách hàng cần sửa: ");
        AuthService authService = new AuthService();
        User customer = authService.userDAO.findById(id);
        if (customer == null || customer.getRole() != User.Role.CUSTOMER) { 
            ConsoleUtil.printError("Không tìm thấy khách hàng."); 
            return; 
        }
        
        String fullName = ConsoleUtil.readLine("Họ tên [" + customer.getFullName() + "]: ");
        String phone = ConsoleUtil.readLine("Số điện thoại [" + customer.getPhone() + "]: ");
        
        // Validate inputs if provided
        if (!fullName.isBlank() && !util.ValidationUtil.isValidFullName(fullName)) {
            ConsoleUtil.printError("Họ tên phải từ 2-100 ký tự.");
            return;
        }
        if (!phone.isBlank() && !util.ValidationUtil.isValidPhone(phone)) {
            ConsoleUtil.printError("Số điện thoại không hợp lệ.");
            return;
        }
        
        // Update customer
        if (!fullName.isBlank()) customer.setFullName(fullName.trim());
        if (!phone.isBlank()) customer.setPhone(phone.trim());
        
        boolean success = authService.userDAO.update(customer);
        if (success) {
            ConsoleUtil.printSuccess("Cập nhật thông tin khách hàng thành công!");
        } else {
            ConsoleUtil.printError("Cập nhật thất bại.");
        }
    }

    private void deleteCustomer() {
        listCustomers();
        int id = ConsoleUtil.readInt("ID khách hàng cần xóa: ");
        AuthService authService = new AuthService();
        User customer = authService.userDAO.findById(id);
        if (customer == null || customer.getRole() != User.Role.CUSTOMER) { 
            ConsoleUtil.printError("Không tìm thấy khách hàng."); 
            return; 
        }
        
        String confirm = ConsoleUtil.readLine("Xác nhận xóa khách hàng '" + customer.getFullName() + "'? (yes): ");
        if (!confirm.equalsIgnoreCase("yes")) { 
            ConsoleUtil.printWarning("Đã hủy."); 
            return; 
        }
        
        boolean success = authService.userDAO.delete(id);
        if (success) {
            ConsoleUtil.printSuccess("Xóa khách hàng thành công!");
        } else {
            ConsoleUtil.printError("Xóa thất bại.");
        }
    }

    private void addBalanceToCustomer() {
        listCustomers();
        int id = ConsoleUtil.readInt("ID khách hàng cần nạp tiền: ");
        AuthService authService = new AuthService();
        User customer = authService.userDAO.findById(id);
        if (customer == null || customer.getRole() != User.Role.CUSTOMER) { 
            ConsoleUtil.printError("Không tìm thấy khách hàng."); 
            return; 
        }
        
        System.out.println("Số dư hiện tại: " + ConsoleUtil.formatCurrency(customer.getBalance()));
        double amount = ConsoleUtil.readDouble("Số tiền cần nạp (VNĐ): ");
        if (amount <= 0) {
            ConsoleUtil.printError("Số tiền phải lớn hơn 0.");
            return;
        }
        
        customer.setBalance(customer.getBalance() + amount);
        boolean success = authService.userDAO.update(customer);
        if (success) {
            ConsoleUtil.printSuccess("Nạp tiền thành công! Số dư mới: " + 
                ConsoleUtil.formatCurrency(customer.getBalance()));
        } else {
            ConsoleUtil.printError("Nạp tiền thất bại.");
        }
    }

    // ============================================================
    // BÁO CÁO DOANH THU
    // ============================================================
    private void showRevenue() {
        ConsoleUtil.clearScreen();
        ConsoleUtil.printSubHeader("BÁO CÁO DOANH THU");
        double total = walletService.getTotalRevenue();
        System.out.printf("  Tổng doanh thu: %s%n", ConsoleUtil.formatCurrency(total));

        int totalBookings = bookingService.getAllBookings().size();
        int totalOrders   = orderService.getAllOrders().size();
        System.out.printf("  Tổng số đặt máy: %d%n", totalBookings);
        System.out.printf("  Tổng số đơn F&B: %d%n", totalOrders);

        // Top 3 món bán chạy
        System.out.println();
        ConsoleUtil.printInfo("Top món bán chạy:");
        java.util.Map<String, Integer> foodCount = new java.util.LinkedHashMap<>();
        for (Order o : orderService.getAllOrders()) {
            for (Order.OrderItem item : o.getItems()) {
                foodCount.merge(item.getFoodName(), item.getQuantity(), Integer::sum);
            }
        }
        foodCount.entrySet().stream()
                .sorted(java.util.Map.Entry.<String,Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> System.out.printf("  %-25s: %d phần%n", e.getKey(), e.getValue()));

        ConsoleUtil.pressEnterToContinue();
    }
}
