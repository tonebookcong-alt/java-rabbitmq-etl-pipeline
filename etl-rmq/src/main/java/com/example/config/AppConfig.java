package com.example.config;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.out.println("Xin lỗi, không tìm thấy file application.properties");
            }
            // Tải file properties
            properties.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Hàm để lấy một giá trị từ file properties
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    // Hàm để lấy giá trị số (ví dụ: port)
    public static int getIntProperty(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }
}