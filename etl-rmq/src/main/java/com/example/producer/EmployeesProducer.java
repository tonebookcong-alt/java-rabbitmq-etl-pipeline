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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmployeesProducer {

    private static final Logger log = LoggerFactory.getLogger(EmployeesProducer.class);

    // Lấy thông tin RabbitMQ từ class kết nối
    private static final String EXCHANGE_NAME = AppConfig.getProperty("mq.exchange");
    private static final String ROUTING_KEY = "employees"; 

    private static final String SOURCE_NAME = "csv_employees";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public void processFile(String filePath) {
        log.info("Bắt đầu xử lý file: {}", filePath);

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            Channel channel = RabbitMQConnection.getChannel();

            List<String[]> rows = reader.readAll();
            rows.remove(0); // Bỏ qua header

            int successCount = 0;
            int failCount = 0; // Đếm số bản ghi lỗi (nhưng vẫn gửi đi)

for (String[] row : rows) {
                String empId = (row.length > 0) ? row[0].trim() : "UNKNOWN";
                String status = "VALID";
                String errorMsg = "";
                Object payloadData = null;
                String businessKey = "";
                String hash = "";

                // Lấy chuỗi lương và phụ cấp để check regex
                String salaryStr = (row.length > 3) ? row[3].trim() : "";
                String allowanceStr = (row.length > 4) ? row[4].trim() : "";

                // --------- 1. VALIDATE TẬP TRUNG ---------
                if (row.length < 5) {
                    status = "INVALID";
                    errorMsg = "Du lieu thieu cot";
                    payloadData = row;
                    businessKey = "ERROR_" + System.currentTimeMillis();
                    hash = "NO_HASH";
                } 
                else if (!RegexUtil.isValidEmpId(empId)) {
                    status = "INVALID";
                    errorMsg = "Sai dinh dang ID: " + empId;
                    payloadData = row;
                    businessKey = empId;
                    hash = "NO_HASH";
                }
                // --- SỬ DỤNG REGEXUTIL ĐỂ CHECK SỐ ---
                else if (!RegexUtil.isValidNumber(salaryStr)) {
                    status = "INVALID";
                    errorMsg = "Luong khong phai la so: " + salaryStr;
                    payloadData = row;
                    businessKey = empId;
                    hash = "NO_HASH";
                }
                else if (!RegexUtil.isValidNumber(allowanceStr)) {
                    status = "INVALID";
                    errorMsg = "Phu cap khong phai la so: " + allowanceStr;
                    payloadData = row;
                    businessKey = empId;
                    hash = "NO_HASH";
                }
                else {
                    // --- HỢP LỆ (VALID) ---
                    // Lúc này đã an tâm ép kiểu (parse) vì Regex đã bảo kê rồi
                    status = "VALID";
                    Employee employee = new Employee(
                        empId, 
                        row[1].trim(), 
                        row[2].trim(), 
                        Long.parseLong(salaryStr),    // An toàn tuyệt đối
                        Long.parseLong(allowanceStr)  // An toàn tuyệt đối
                    );
                    payloadData = employee;
                    businessKey = employee.emp_id();
                    hash = HashUtil.sha256(employee.toCanonicalString());
                }

                // ... (Phần đóng gói message và gửi đi giữ nguyên) ...
                // ... (Copy đoạn cuối của file cũ vào đây) ...

                // --------- 2. ĐÓNG GÓI MESSAGE (Dùng Constructor mới 7 tham số) ---------
                PayrollMessage message = new PayrollMessage(
                    SOURCE_NAME,
                    ROUTING_KEY, 
                    businessKey,
                    hash,
                    payloadData, // Payload có thể là Employee object hoặc mảng String[]
                    status,      // "VALID" hoặc "INVALID"
                    errorMsg     // Lý do lỗi
                );

                // Chuyển sang JSON
                String messageJson = objectMapper.writeValueAsString(message);

                // --------- 3. PUBLISH (GỬI ĐI DÙ ĐÚNG HAY SAI) ---------
                channel.basicPublish(
                    EXCHANGE_NAME,
                    ROUTING_KEY,
                    MessageProperties.PERSISTENT_TEXT_PLAIN, 
                    messageJson.getBytes("UTF-8")
                );

                // Log kiểm tra
                if (status.equals("VALID")) {
                    successCount++;
                } else {
                    log.warn("⚠️ Phát hiện bản ghi lỗi nhưng vẫn gửi đi: {} - Lý do: {}", empId, errorMsg);
                    failCount++;
                }
            }

            log.info("✅ Hoàn tất xử lý file {}. Gửi thành công: {} (Sạch) + {} (Lỗi)", filePath, successCount, failCount);

        } catch (IOException | CsvException e) {
            log.error("❌ Lỗi nghiêm trọng khi đọc file CSV", e);
        } catch (Exception e) {
            log.error("❌ Lỗi không xác định khi publish message", e);
        }
    }
}