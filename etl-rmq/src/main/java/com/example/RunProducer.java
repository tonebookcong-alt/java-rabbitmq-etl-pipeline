package com.example;

import com.example.producer.AttendanceProducer; // <-- THÊM DÒNG NÀY
import com.example.producer.EmployeesProducer;

// 2 dòng này để đóng kết nối
import com.example.connection.MySQLConnection;
import com.example.connection.RabbitMQConnection;

public class RunProducer {

    public static void main(String[] args) {

        // ------ 1. CHẠY PRODUCER ĐỌC TỪ CSV (Employees) ------
        EmployeesProducer empProducer = new EmployeesProducer();

        String validFilePath = "data/employees.csv";
        empProducer.processFile(validFilePath);


        // ------ 2. CHẠY PRODUCER ĐỌC TỪ TABLE (Attendance) ------
        AttendanceProducer attProducer = new AttendanceProducer();
        attProducer.processDatabaseSource(); // <-- GỌI HÀM MỚI

        System.out.println("--- ĐÃ CHẠY XONG CẢ 2 PRODUCER ---");

        // Đóng kết nối để chương trình kết thúc
        RabbitMQConnection.close();
        MySQLConnection.close();
    }
}