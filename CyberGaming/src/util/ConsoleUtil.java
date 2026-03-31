package util;

import java.util.Scanner;

public class ConsoleUtil {

    public static final String RESET  = "\033[0m";
    public static final String RED    = "\033[0;31m";


    private static final Scanner scanner = new Scanner(System.in);

    private ConsoleUtil() {}

    public static void printHeader(String title) {
        String line = "=".repeat(60);
        System.out.println(line);
        System.out.printf("  %-56s%n", title);
        System.out.println(line);
    }

    public static void printSubHeader(String title) {
        System.out.println("-".repeat(50));
        System.out.println("  " + title);
        System.out.println("-".repeat(50));
    }

    public static void printSuccess(String msg) {
        System.out.println("[✓] " + msg);
    }

    public static void printError(String msg) {
        System.out.println(RED + "[✗] " + msg + RESET);
    }

    public static void printWarning(String msg) {
        System.out.println("[!] " + msg);
    }

    public static void printInfo(String msg) {
        System.out.println("[i] " + msg);
    }


    public static void pressEnterToContinue() {
        System.out.print("\n[Nhấn ENTER để tiếp tục...]");
        scanner.nextLine();
    }

    public static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String line = scanner.nextLine().trim();
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                printError("Vui lòng nhập số nguyên hợp lệ.");
            }
        }
    }

    public static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String line = scanner.nextLine().trim();
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                printError("Vui lòng nhập số hợp lệ.");
            }
        }
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static String formatCurrency(double amount) {
        return String.format("%,.0f VNĐ", amount);
    }
}
