package presentation;

import model.User;
import service.AuthService;
import util.ConsoleUtil;

public class AuthMenu {

    private final AuthService authService = new AuthService();

    public User show() {
        while (true) {
            ConsoleUtil.clearScreen();
            ConsoleUtil.printHeader("  CYBER GAMING & F&B MANAGEMENT SYSTEM");
            System.out.println();
            System.out.println("  1. Đăng nhập");
            System.out.println("  2. Đăng ký tài khoản");
            System.out.println("  0. Thoát");
            System.out.println();

            int choice = ConsoleUtil.readInt("Chọn: ");
            switch (choice) {
                case 1 -> {
                    User u = doLogin();
                    if (u != null) return u;
                }
                case 2 -> doRegister();
                case 0 -> { return null; }
                default -> ConsoleUtil.printError("Lựa chọn không hợp lệ.");
            }
            ConsoleUtil.pressEnterToContinue();
        }
    }

    private User doLogin() {
        ConsoleUtil.printSubHeader("ĐĂNG NHẬP");
        String username = ConsoleUtil.readLine("Tên đăng nhập: ");
        String password = ConsoleUtil.readLine("Mật khẩu: ");

        User user = authService.login(username, password);
        if (user == null) {
            ConsoleUtil.printError("Tên đăng nhập hoặc mật khẩu không đúng!");
            return null;
        }
        ConsoleUtil.printSuccess("Đăng nhập thành công! Xin chào, " + user.getFullName());
        return user;
    }

    private void doRegister() {
        ConsoleUtil.printSubHeader("ĐĂNG KÝ TÀI KHOẢN");
        
        // Validate and get full name
        String fullName = "";
        while (true) {
            fullName = ConsoleUtil.readLine("Họ và tên: ");
            if (util.ValidationUtil.isValidFullName(fullName)) {
                break;
            }
            ConsoleUtil.printError("Họ tên phải từ 2-100 ký tự, không có khoảng trắng ở đầu/cuối và chỉ cách 1 khoảng trắng giữa các từ. Vui lòng nhập lại!");
        }
        
        // Validate and get username
        String username = "";
        while (true) {
            username = ConsoleUtil.readLine("Tên đăng nhập: ");
            if (!util.ValidationUtil.isValidUsername(username)) {
                ConsoleUtil.printError("Tên đăng nhập phải từ 4+ ký tự (chữ, số, dấu gạch dưới). Vui lòng nhập lại!");
                continue;
            };
            if (authService.userDAO.existsByUsername(username.trim().toLowerCase())) {
                ConsoleUtil.printError("Tên đăng nhập '" + username.trim() + "' đã tồn tại. Vui lòng nhập lại!");
                continue;
            }
            break;
        }
        
        // Validate and get password
        String password = "";
        while (true) {
            password = ConsoleUtil.readLine("Mật khẩu (tối thiểu 6 ký tự): ");
            if (util.ValidationUtil.isValidPassword(password)) {
                break;
            }
            ConsoleUtil.printError("Mật khẩu phải từ 6+ ký tự và không chứa khoảng trắng. Vui lòng nhập lại!");
        }
        
        // Validate and get phone
        String phone = "";
        while (true) {
            phone = ConsoleUtil.readLine("Số điện thoại: ");
            if (util.ValidationUtil.isValidPhone(phone)) {
                break;
            }
            ConsoleUtil.printError("Số điện thoại không được để trống, phải bắt đầu từ 03, 07, 08, 09 và có đúng 10 số. Vui lòng nhập lại!");
        }

        // Register the user
        String err = authService.register(fullName, username, password, phone, User.Role.CUSTOMER);
        if (err != null) {
            ConsoleUtil.printError(err);
        } else {
            ConsoleUtil.printSuccess("Đăng ký thành công! Bạn có thể đăng nhập ngay bây giờ.");
        }
    }
}
