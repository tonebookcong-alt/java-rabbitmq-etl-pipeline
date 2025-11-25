package com.example.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class DataGenerator {

    public static void main(String[] args) {
        // Đường dẫn file (Lưu vào thư mục data của dự án)
        String csvFile = "data/employees.csv";
        
        try (FileWriter writer = new FileWriter(csvFile)) {
            // 1. Viết Header
            writer.append("emp_id,name,department,base_salary,allowance\n");

            // 2. Viết 295 dòng dữ liệu SẠCH
            Random rand = new Random();
            String[] depts = {"IT", "HR", "Sales", "Marketing", "Finance"};
            
            for (int i = 1; i <= 295; i++) {
                String id = String.format("E%03d", i); // E001, E002...
                String name = "Nhan Vien " + i;
                String dept = depts[rand.nextInt(depts.length)];
                long salary = 10000000 + rand.nextInt(20000000);
                long allowance = 1000000 + rand.nextInt(5000000);

                writer.append(String.join(",", 
                    id, name, dept, String.valueOf(salary), String.valueOf(allowance)
                ));
                writer.append("\n");
            }

            // 3. Viết 3 dòng LỖI (Để test tính năng Validate code Java)
            System.out.println("Generating Invalid Data...");
            writer.append("BAD_ID_123,Ke Gian,Hack,999999,0\n"); // Lỗi: ID quá dài/sai format
            writer.append("E998,Bi Thieu Cot Luong\n");           // Lỗi: Thiếu cột
            writer.append("E@#$,Ky Tu La,IT,10000000,500000\n");  // Lỗi: Ký tự đặc biệt

            // 4. Viết 2 dòng TRÙNG LẶP (Để test tính năng khử trùng SQL)
            // Trùng với nhân viên E001 và E002 đã tạo ở trên
            System.out.println("Generating Duplicate Data...");
            writer.append("E001,Nguyen Van A (Clone),IT,15000000,1000000\n");
            writer.append("E002,Tran Thi B (Clone),HR,12000000,800000\n");

            System.out.println("✅ Đã tạo thành công file: " + csvFile);
            System.out.println("- Tổng: ~300 dòng");
            System.out.println("- Bao gồm: Dữ liệu sạch, Dữ liệu lỗi (BAD_ID), Dữ liệu trùng (E001, E002)");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}