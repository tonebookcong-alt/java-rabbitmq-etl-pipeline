package com.example.producer;

import com.example.config.AppConfig;
import com.example.connection.MySQLConnection; // TÁI SỬ DỤNG KẾT NỐI CSDL
import com.example.connection.RabbitMQConnection;
import com.example.model.Attendance; // Model mới
import com.example.model.PayrollMessage;
import com.example.util.HashUtil;
import com.example.util.RegexUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

// THÊM CÁC THƯ VIỆN SQL
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttendanceProducer {

    private static final Logger log = LoggerFactory.getLogger(AttendanceProducer.class);

    // Lấy thông tin RabbitMQ từ file config
    private static final String EXCHANGE_NAME = AppConfig.getProperty("mq.exchange");
    private static final String ROUTING_KEY = "attendance"; // Key cho luồng này

    // Tên của nguồn dữ liệu này (QUAN TRỌNG: ĐỔI TÊN NGUỒN)
    private static final String SOURCE_NAME = "db_attendance"; 

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Tên bảng nguồn (giả lập)
    private static final String SOURCE_TABLE = "attendance_source";

    public void processDatabaseSource() {
        log.info("Bắt đầu xử lý nguồn CSDL: {}", SOURCE_TABLE);

        try {
            // Lấy kết nối
            Channel rabbitChannel = RabbitMQConnection.getChannel();
            // Lấy kết nối CSDL (chúng ta tái sử dụng MySQLConnection)
            Connection mysqlConnection = MySQLConnection.getConnection(); 

            if (rabbitChannel == null || mysqlConnection == null) {
                log.error("Không thể kết nối RabbitMQ hoặc MySQL. Dừng lại.");
                return;
            }

            Statement statement = mysqlConnection.createStatement();
            // 1. ĐỌC DỮ LIỆU TỪ BẢNG NGUỒN (thay vì CSV)
            ResultSet rs = statement.executeQuery("SELECT ma_nv, ngay_lam, gio_vao, gio_ra, gio_them FROM " + SOURCE_TABLE);

            int successCount = 0;
            int failCount = 0;

            // 2. LẶP QUA KẾT QUẢ SQL (thay vì lặp qua CSV row)
            while (rs.next()) {
                try {
                    // Lấy dữ liệu từ ResultSet (theo tên cột CSDL nguồn)
                    String empId = rs.getString("ma_nv").trim();
                    String workDate = rs.getString("ngay_lam").trim();
                    String checkIn = rs.getString("gio_vao").trim();
                    String checkOut = rs.getString("gio_ra").trim();
                    double otHours = rs.getDouble("gio_them");

                    // --------- 3. VALIDATE (KIỂM TRA) ---------
                    if (!RegexUtil.isValidEmpId(empId)) {
                        log.warn("emp_id sai định dạng: {}. Bỏ qua.", empId);
                        failCount++;
                        continue;
                    }
                    if (!RegexUtil.isValidDate(workDate)) {
                        log.warn("work_date sai định dạng: {}. Bỏ qua.", workDate);
                        failCount++;
                        continue;
                    }

                    // --------- 4. TRANSFORM (BIẾN ĐỔI) ---------
                    Attendance attendance = new Attendance(
                        empId, workDate, checkIn, checkOut, otHours
                    );

                    // Tạo business_key (theo kế hoạch: emp_id|date)
                    String businessKey = String.format("%s|%s", empId, workDate);

                    // Tạo hash_sha256
                    String hash = HashUtil.sha256(attendance.toCanonicalString());

                    // --------- 5. ĐÓNG GÓI MESSAGE ---------
                    PayrollMessage message = new PayrollMessage(
                        SOURCE_NAME,
                        ROUTING_KEY, // record_type (attendance)
                        businessKey,
                        hash,
                        attendance   // payload
                    );

                    String messageJson = objectMapper.writeValueAsString(message);

                    // --------- 6. PUBLISH (GỬI LÊN RABBITMQ) ---------
                    rabbitChannel.basicPublish(
                        EXCHANGE_NAME,
                        ROUTING_KEY,
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        messageJson.getBytes("UTF-8")
                    );

                    successCount++;

                } catch (Exception e) {
                    log.warn("Lỗi khi xử lý 1 dòng CSDL. Bỏ qua.", e);
                    failCount++;
                }
            }

            log.info("✅ Hoàn tất xử lý CSDL {}. Thành công: {}, Thất bại: {}", SOURCE_TABLE, successCount, failCount);
            rs.close();
            statement.close();

        } catch (Exception e) {
            log.error("❌ Lỗi nghiêm trọng khi đọc CSDL nguồn", e);
        }
    }
}