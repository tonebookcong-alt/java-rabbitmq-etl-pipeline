package com.example.model;

// Đây là một "Java Record" - cách đơn giản để tạo một lớp chứa dữ liệu
// Nó tự động tạo getters, equals, hashCode, toString
public record Employee(
    String emp_id,
    String name,
    String department,
    long base_salary,
    long allowance
) {
    // Hàm này dùng để tạo chuỗi "canonical" (chuẩn) để tạo hash
    // Giúp đảm bảo 2 object giống nhau sẽ có hash giống nhau
    public String toCanonicalString() {
        return String.format("%s|%s|%s|%d|%d", 
            emp_id, name, department, base_salary, allowance);
    }
}