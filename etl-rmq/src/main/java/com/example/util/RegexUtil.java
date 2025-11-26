package com.example.util;

import java.util.regex.Pattern;

public class RegexUtil {

    // 1. ID: Chữ hoặc số, 3-10 ký tự
    private static final Pattern EMP_ID_PATTERN = Pattern.compile("^[A-Za-z0-9]{3,10}$");

    // 2. Ngày: YYYY-MM-DD
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$");

    // 3. Số nguyên dương (Dùng cho Lương) - Chỉ chứa số từ 0-9
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d+$");

    // 4. Số thực (Dùng cho Giờ OT) - Ví dụ: 1.5, 2.0, 0.5
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?$");


    // --- CÁC HÀM CHECK ---

    public static boolean isValidEmpId(String empId) {
        if (empId == null) return false;
        return EMP_ID_PATTERN.matcher(empId).matches();
    }

    public static boolean isValidDate(String date) {
        if (date == null) return false;
        return DATE_PATTERN.matcher(date).matches();
    }

    // Hàm mới: Kiểm tra lương (Phải là số nguyên)
    public static boolean isValidNumber(String value) {
        if (value == null) return false;
        return NUMBER_PATTERN.matcher(value).matches();
    }

    // Hàm mới: Kiểm tra giờ OT (Có thể là số thập phân)
    public static boolean isValidDecimal(String value) {
        if (value == null) return false;
        return DECIMAL_PATTERN.matcher(value).matches();
    }
}