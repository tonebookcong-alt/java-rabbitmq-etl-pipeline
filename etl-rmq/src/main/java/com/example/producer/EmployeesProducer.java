package com.example.producer;

import com.example.config.AppConfig;
import com.example.connection.RabbitMQConnection;
import com.example.model.Employee;
import com.example.model.PayrollMessage;
import com.example.util.HashUtil;
import com.example.util.RegexUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

// Thêm các thư viện logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmployeesProducer {

    // Tạo một logger để ghi log, dễ theo dõi hơn System.out.println
    private static final Logger log = LoggerFactory.getLogger(EmployeesProducer.class);

    // Lấy thông tin RabbitMQ từ class kết nối
private static final String EXCHANGE_NAME = AppConfig.getProperty("mq.exchange");
private static final String ROUTING_KEY = "employees"; // Key cho luồng này

    // Tên của nguồn dữ liệu này (để ghi vào message)
    private static final String SOURCE_NAME = "csv_employees";

    // ObjectMapper dùng để chuyển Java Object sang JSON
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public void processFile(String filePath) {
        log.info("Bắt đầu xử lý file: {}", filePath);

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            // Lấy channel RabbitMQ
            Channel channel = RabbitMQConnection.getChannel();

            // Đọc tất cả các dòng (bỏ qua dòng header)
            List<String[]> rows = reader.readAll();
            // Bỏ qua dòng 1 (header)
            rows.remove(0); 

            int successCount = 0;
            int failCount = 0;

            for (String[] row : rows) {
                // --------- 1. VALIDATE (KIỂM TRA) ---------
                // Kiểm tra số lượng cột
                if (row.length < 5) {
                    log.warn("Dòng bị thiếu dữ liệu: {}. Bỏ qua.", String.join(",", row));
                    failCount++;
                    continue;
                }

                String empId = row[0].trim();
                // Kiểm tra Regex
                if (!RegexUtil.isValidEmpId(empId)) {
                    log.warn("emp_id sai định dạng: {}. Bỏ qua.", empId);
                    failCount++;
                    continue;
                }

                // --------- 2. TRANSFORM (BIẾN ĐỔI) ---------
                // Tạo đối tượng Employee
                Employee employee = new Employee(
                    empId,
                    row[1].trim(), // name
                    row[2].trim(), // department
                    Long.parseLong(row[3].trim()), // base_salary
                    Long.parseLong(row[4].trim())  // allowance
                );

                // Tạo business_key (theo kế hoạch: emp_id)
                String businessKey = employee.emp_id();

                // Tạo hash_sha256 (từ chuỗi canonical)
                String hash = HashUtil.sha256(employee.toCanonicalString());

                // --------- 3. ĐÓNG GÓI MESSAGE ---------
                PayrollMessage message = new PayrollMessage(
                    SOURCE_NAME,
                    ROUTING_KEY, // record_type (employees)
                    businessKey,
                    hash,
                    employee     // payload
                );

                // Chuyển message (Java Object) sang chuỗi JSON (String)
                String messageJson = objectMapper.writeValueAsString(message);

                // --------- 4. PUBLISH (GỬI LÊN RABBITMQ) ---------
                channel.basicPublish(
                    EXCHANGE_NAME,
                    ROUTING_KEY,
                    MessageProperties.PERSISTENT_TEXT_PLAIN, // Đảm bảo message bền bỉ (persistent)
                    messageJson.getBytes("UTF-8")
                );

                successCount++;
            }

            log.info("✅ Hoàn tất xử lý file {}. Thành công: {}, Thất bại: {}", filePath, successCount, failCount);

        } catch (IOException | CsvException e) {
            log.error("❌ Lỗi nghiêm trọng khi đọc file CSV", e);
        } catch (Exception e) {
            log.error("❌ Lỗi không xác định khi publish message", e);
        }
    }
}