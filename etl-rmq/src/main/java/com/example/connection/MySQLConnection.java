package com.example.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.example.config.AppConfig;

public class MySQLConnection {

    private static Connection connection;

    // Lấy thông tin từ file config
    private static final String DB_URL = AppConfig.getProperty("db.url");
    private static final String DB_USER = AppConfig.getProperty("db.user");
    private static final String DB_PASS = AppConfig.getProperty("db.pass");

    // Hàm để lấy kết nối
    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Nạp driver JDBC
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Tạo kết nối
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                System.out.println("✅ Đã kết nối tới MySQL database thành công!");
            } catch (SQLException | ClassNotFoundException e) {
                System.err.println("❌ Lỗi khi kết nối tới MySQL:");
                e.printStackTrace();
            }
        }
        return connection;
    }

    // (Tùy chọn) Hàm để đóng kết nối khi tắt ứng dụng
    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}