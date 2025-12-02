package com.example;

import com.example.producer.AttendanceProducer;
import com.example.producer.EmployeesProducer;
import com.example.connection.MySQLConnection;
import com.example.connection.RabbitMQConnection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RunProducer {

    public static void main(String[] args) {
        // Sử dụng một ExecutorService để quản lý các luồng chạy song song
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            // Tác vụ 1: Xử lý file employees.csv
            CompletableFuture<Void> employeeTask = CompletableFuture.runAsync(() -> {
                               new EmployeesProducer().processFile("data/employees.csv");
            }, executor);

            // Tác vụ 2: Xử lý bảng attendance_source
            CompletableFuture<Void> attendanceTask = CompletableFuture.runAsync(() -> {
                new AttendanceProducer().processDatabaseSource();
            }, executor);

            // Đợi cả hai tác vụ hoàn thành
            CompletableFuture.allOf(employeeTask, attendanceTask).join();

            System.out.println("--- DA CHAY XONG CA 2 PRODUCER ---");

        } finally {
            // Tắt executor và đóng các kết nối chung
            executor.shutdown();
            RabbitMQConnection.close();
            MySQLConnection.close();
        }
    }
}