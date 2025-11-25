package com.example.util;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Random;

public class SqlDataGenerator {

    public static void main(String[] args) {
        // File này sẽ được tạo ra trong thư mục gốc của dự án
        String sqlFile = "insert_attendance.sql";
        
        try (FileWriter writer = new FileWriter(sqlFile)) {
            // 1. Viết lệnh Xóa dữ liệu cũ
            writer.append("TRUNCATE TABLE attendance_source;\n\n");
            
            // 2. Bắt đầu lệnh INSERT
            writer.append("INSERT INTO attendance_source (ma_nv, ngay_lam, gio_vao, gio_ra, gio_them) VALUES\n");

            Random rand = new Random();
            LocalDate startDate = LocalDate.of(2025, 10, 1);

            // --- SINH 295 DÒNG SẠCH ---
            for (int i = 1; i <= 295; i++) {
                String id = String.format("E%03d", i); // E001, E002...
                String date = startDate.plusDays(rand.nextInt(30)).toString(); // Random ngày trong tháng 10
                
                // Random giờ vào (07:00 - 09:00)
                int hVao = 7 + rand.nextInt(2);
                int mVao = rand.nextInt(60);
                String gioVao = String.format("%02d:%02d:00", hVao, mVao);

                // Random giờ ra (17:00 - 19:00)
                int hRa = 17 + rand.nextInt(2);
                int mRa = rand.nextInt(60);
                String gioRa = String.format("%02d:%02d:00", hRa, mRa);

                // Random OT (0.0 - 4.0)
                double ot = rand.nextInt(5) * 0.5;

                writer.append(String.format("('%s', '%s', '%s', '%s', %.1f),\n", 
                    id, date, gioVao, gioRa, ot));
            }

            // --- SINH 3 DÒNG LỖI (Để test Validate) ---
            // (Lưu ý: Lỗi ngày tháng sai format sẽ bị MySQL chặn, nên ta test lỗi ID)
            writer.append("('BAD-ID-01', '2025-10-01', '08:00:00', '17:00:00', 0.0),\n"); 
            writer.append("('E@ERROR', '2025-10-02', '08:30:00', '17:30:00', 1.5),\n");
            writer.append("('NO_USER', '2025-10-03', '09:00:00', '18:00:00', 0.0),\n");

            // --- SINH 2 DÒNG TRÙNG LẶP (Để test SQL Duplicate) ---
            // Trùng với nhân viên E001 và E002 vào ngày cố định
            writer.append("('E001', '2025-10-01', '08:00:00', '17:00:00', 2.0),\n"); // Trùng E001 ngày nào đó
            writer.append("('E002', '2025-10-05', '08:30:00', '17:30:00', 0.0);\n"); // Kết thúc bằng dấu chấm phẩy

            System.out.println("✅ Đã tạo file SQL tại: " + sqlFile);
            System.out.println("👉 Hãy mở file này, copy nội dung và chạy trong MySQL Workbench!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}