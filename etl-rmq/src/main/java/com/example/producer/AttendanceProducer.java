package com.example.producer;

import com.example.config.AppConfig;
import com.example.connection.MySQLConnection;
import com.example.connection.RabbitMQConnection;
import com.example.model.Attendance;
import com.example.model.PayrollMessage;
import com.example.util.HashUtil;
import com.example.util.RegexUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttendanceProducer {

    private static final Logger log = LoggerFactory.getLogger(AttendanceProducer.class);

    private static final String EXCHANGE_NAME = AppConfig.getProperty("mq.exchange");
    private static final String ROUTING_KEY = "attendance"; 
    private static final String SOURCE_NAME = "db_attendance"; 
    private static final String SOURCE_TABLE = "attendance_source";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public void processDatabaseSource() {
        log.info("Bắt đầu xử lý nguồn CSDL: {}", SOURCE_TABLE);

        try {
            Channel rabbitChannel = RabbitMQConnection.getChannel();
            Connection mysqlConnection = MySQLConnection.getConnection(); 
            
            if (rabbitChannel == null || mysqlConnection == null) {
                log.error("Không thể kết nối RabbitMQ hoặc MySQL. Dừng lại.");
                return;
            }

            Statement statement = mysqlConnection.createStatement();
            // Lưu ý: Đọc tất cả dưới dạng String để dễ kiểm tra Regex
            ResultSet rs = statement.executeQuery("SELECT ma_nv, ngay_lam, gio_vao, gio_ra, gio_them FROM " + SOURCE_TABLE);

            int successCount = 0;
            int failCount = 0;

            while (rs.next()) {
                try {
                    // Lấy dữ liệu thô dưới dạng String
                    String empId = rs.getString("ma_nv");
                    if (empId != null) empId = empId.trim();
                    
                    String workDate = rs.getString("ngay_lam");
                    String checkIn = rs.getString("gio_vao");
                    String checkOut = rs.getString("gio_ra");
                    String otHoursStr = rs.getString("gio_them"); // Lấy OT dạng chuỗi để check

                    // Chuẩn bị biến cho Message
                    String status = "VALID";
                    String errorMsg = "";
                    Object payloadData = null;
                    String businessKey = "";
                    String hash = "";

                    // --------- 1. VALIDATE TẬP TRUNG (Sử dụng RegexUtil) ---------
                    if (!RegexUtil.isValidEmpId(empId)) {
                        status = "INVALID";
                        errorMsg = "Sai dinh dang ID: " + empId;
                        payloadData = String.format("%s|%s", empId, workDate); 
                        businessKey = empId;
                        hash = "NO_HASH";
                    }
                    else if (!RegexUtil.isValidDate(workDate)) {
                        status = "INVALID";
                        errorMsg = "Sai dinh dang Ngay: " + workDate;
                        payloadData = String.format("%s|%s", empId, workDate);
                        businessKey = empId;
                        hash = "NO_HASH";
                    }
                    // --- TÁI SỬ DỤNG REGEXUTIL ĐỂ CHECK SỐ THỰC ---
                    else if (!RegexUtil.isValidDecimal(otHoursStr)) {
                        status = "INVALID";
                        errorMsg = "Gio OT khong hop le: " + otHoursStr;
                        payloadData = String.format("%s|%s|OT=%s", empId, workDate, otHoursStr);
                        businessKey = empId;
                        hash = "NO_HASH";
                    }
                    else {
                        // --- HỢP LỆ ---
                        // Ép kiểu an toàn sau khi đã qua Regex
                        double otHours = Double.parseDouble(otHoursStr);
                        
                        status = "VALID";
                        Attendance attendance = new Attendance(
                            empId, workDate, checkIn, checkOut, otHours
                        );
                        payloadData = attendance;
                        businessKey = String.format("%s|%s", empId, workDate);
                        hash = HashUtil.sha256(attendance.toCanonicalString());
                    }

                    // --------- 2. ĐÓNG GÓI MESSAGE ---------
                    PayrollMessage message = new PayrollMessage(
                        SOURCE_NAME,
                        ROUTING_KEY,
                        businessKey,
                        hash,
                        payloadData,
                        status,
                        errorMsg
                    );

                    String messageJson = objectMapper.writeValueAsString(message);

                    // --------- 3. PUBLISH ---------
                    rabbitChannel.basicPublish(
                        EXCHANGE_NAME,
                        ROUTING_KEY,
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        messageJson.getBytes("UTF-8")
                    );

                    if (status.equals("VALID")) {
                        successCount++;
                    } else {
                        log.warn("⚠️ Phát hiện dòng CSDL lỗi nhưng vẫn gửi: ID={} - Lý do: {}", empId, errorMsg);
                        failCount++;
                    }

                } catch (Exception e) {
                    log.warn("Lỗi khi xử lý 1 dòng CSDL. Bỏ qua.", e);
                }
            }

            log.info("✅ Hoàn tất xử lý CSDL {}. Gửi thành công: {} (Sạch) + {} (Lỗi)", SOURCE_TABLE, successCount, failCount);
            rs.close();
            statement.close();

        } catch (Exception e) {
            log.error("❌ Lỗi nghiêm trọng khi đọc CSDL nguồn", e);
        }
    }
}