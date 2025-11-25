package com.example.util;

import java.util.regex.Pattern;

public class RegexUtil {

    // Định nghĩa các mẫu Regex
    // emp_id: Phải là chữ hoặc số, từ 3-10 ký tự
    private static final Pattern EMP_ID_PATTERN = Pattern.compile("^[A-Za-z0-9]{3,10}$");

    // date (YYYY-MM-DD)
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$");

    // time (HH:MM)
    private static final Pattern TIME_PATTERN = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");


    // Hàm để kiểm tra emp_id
    public static boolean isValidEmpId(String empId) {
        if (empId == null) return false;
        return EMP_ID_PATTERN.matcher(empId).matches();
    }

    // Hàm để kiểm tra ngày
    public static boolean isValidDate(String date) {
        if (date == null) return false;
        return DATE_PATTERN.matcher(date).matches();
    }

    // Hàm để kiểm tra thời gian
    public static boolean isValidTime(String time) {
        if (time == null) return false;
        return TIME_PATTERN.matcher(time).matches();
    }
}