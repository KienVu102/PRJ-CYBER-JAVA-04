package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/cyber_gaming";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "cocailon$10206";

    private static Connection connection = null;

    private DBConnection() {}

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[LỖI] Không tìm thấy MySQL JDBC Driver!");
            System.err.println("Hãy thêm mysql-connector-java vào classpath.");
            System.exit(1);
        } catch (SQLException e) {
            System.err.println("[LỖI] Không thể kết nối đến database: " + e.getMessage());
            System.err.println("Kiểm tra MySQL đang chạy và thông tin kết nối trong DBConnection.java");
            System.exit(1);
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                System.err.println("[LỖI] Không thể đóng kết nối: " + e.getMessage());
            }
        }
    }
}
