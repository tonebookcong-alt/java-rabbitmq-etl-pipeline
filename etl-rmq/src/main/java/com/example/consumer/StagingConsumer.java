package com.example.consumer;

import com.example.connection.MySQLConnection;
import com.example.connection.RabbitMQConnection;
import com.example.config.AppConfig;
import com.example.model.PayrollMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StagingConsumer {

    private static final Logger log = LoggerFactory.getLogger(StagingConsumer.class);

    // Lấy tên queue từ file config
    private static final String QUEUE_NAME = AppConfig.getProperty("mq.queue");

    // ObjectMapper để chuyển JSON
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Hàm main để chạy Consumer
    public static void main(String[] args) {
        try {
            // Lấy kết nối
            Channel rabbitChannel = RabbitMQConnection.getChannel();
            Connection mysqlConnection = MySQLConnection.getConnection();

            if (rabbitChannel == null || mysqlConnection == null) {
                log.error("Không thể khởi động Consumer, kết nối thất bại.");
                return;
            }

            // Chuẩn bị câu lệnh SQL
            String sql = "INSERT INTO staging_records (source_name, record_type, business_key, hash_sha256, payload_json, status, error_msg) VALUES (?, ?, ?, ?, ?, ?, ?)";

            // PreparedStatement giúp chạy SQL an toàn và hiệu quả
            PreparedStatement pStatement = mysqlConnection.prepareStatement(sql);

            log.info("▶️ Consumer đang chạy. Đang chờ message từ queue: {} ...", QUEUE_NAME);
            log.info("Nhấn (Ctrl+C) để dừng.");

            // Đây là logic "Khi nhận được message thì làm gì?"
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String messageJson = new String(delivery.getBody(), "UTF-8");
                long deliveryTag = delivery.getEnvelope().getDeliveryTag();

                try {
                    // 1. Chuyển JSON (String) về PayrollMessage (Object)
                    PayrollMessage message = objectMapper.readValue(messageJson, PayrollMessage.class);

                    // 2. Chuẩn bị dữ liệu cho câu lệnh SQL
                    pStatement.setString(1, message.getSourceName());
                    pStatement.setString(2, message.getRecordType());
                    pStatement.setString(3, message.getBusinessKey());
                    pStatement.setString(4, message.getHashSha256());

                    // 3. Chuyển phần payload (Object) thành JSON (String) để lưu vào CSDL
                    String payloadJson = objectMapper.writeValueAsString(message.getPayload());
                    pStatement.setString(5, payloadJson);
                    pStatement.setString(6, message.getStatus());
                    pStatement.setString(7, message.getErrorMsg());
                    // 4. Thực thi INSERT
                    pStatement.executeUpdate();

                    // 5. Gửi báo cáo "Đã xử lý xong" (ACK) cho RabbitMQ
                    // RabbitMQ sẽ xóa message này khỏi queue
                    rabbitChannel.basicAck(deliveryTag, false);
                    log.info("✅ Đã xử lý và INSERT thành công message (Key: {})", message.getBusinessKey());

                } catch (Exception e) {
                    log.error("❌ Lỗi khi xử lý message: {}", messageJson, e);
                    // Gửi báo cáo "Xử lý lỗi" (NACK)
                    // false = không requeue (không trả lại hàng đợi, vứt đi luôn)
                    rabbitChannel.basicNack(deliveryTag, false, false);
                }
            };

            // Bắt đầu lắng nghe queue
            // autoAck = false (Chúng ta sẽ tự gửi ACK/NACK ở trên)
            rabbitChannel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {});

        } catch (Exception e) {
            log.error("❌ Lỗi nghiêm trọng, Consumer bị dừng:", e);
        }
    }
}