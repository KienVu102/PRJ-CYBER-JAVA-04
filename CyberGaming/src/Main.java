import model.User;
import presentation.AdminMenu;
import presentation.AuthMenu;
import presentation.CustomerMenu;
import presentation.StaffMenu;
import util.ConsoleUtil;
import util.DBConnection;



public class Main {

    public static void main(String[] args) {
        printBanner();

        AuthMenu authMenu = new AuthMenu();

        while (true) {
            User user = authMenu.show();
            if (user == null) {

                ConsoleUtil.printInfo("Cảm ơn bạn đã sử dụng hệ thống. Tạm biệt!");
                DBConnection.closeConnection();
                System.exit(0);
            }

            switch (user.getRole()) {
                case ADMIN    -> new AdminMenu(user).show();
                case STAFF    -> new StaffMenu(user).show();
                case CUSTOMER -> new CustomerMenu(user).show();
            }

            ConsoleUtil.printSuccess("Đã đăng xuất thành công.");
        }
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("  ========================================================");
        System.out.println("  |     CYBER GAMING & F&B MANAGEMENT SYSTEM             |");
        System.out.println("  |     Phiên bản 1.0 | Java Core + JDBC + MySQL         |");
        System.out.println("  ========================================================");
        System.out.println("  |    Tài khoản demo:                                   |");
        System.out.println("  |    Admin    : admin    / admin                       |");
        System.out.println("  |    Staff    : staff1   / staff123                    |");
        System.out.println("  ========================================================");
        System.out.println();
    }
}
