package com.example.model;

// Dùng record cho đơn giản
public record Attendance(
    String emp_id,
    String work_date,
    String check_in,
    String check_out,
    double ot_hours
) {
    // Hàm tạo chuỗi canonical để tạo hash
    public String toCanonicalString() {
        return String.format("%s|%s|%s|%s|%.2f", 
            emp_id, work_date, check_in, check_out, ot_hours);
    }
}