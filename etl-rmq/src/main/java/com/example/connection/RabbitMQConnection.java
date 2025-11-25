package com.example.connection;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import com.example.config.AppConfig;

public class RabbitMQConnection {

    private static Connection connection;
    private static Channel channel;

    // Lấy thông tin từ file config
    private static final String HOST = AppConfig.getProperty("mq.host");
    private static final int PORT = AppConfig.getIntProperty("mq.port");
    private static final String USER = AppConfig.getProperty("mq.user");
    private static final String PASS = AppConfig.getProperty("mq.pass");
    private static final String EXCHANGE_NAME = AppConfig.getProperty("mq.exchange");
    private static final String QUEUE_NAME = AppConfig.getProperty("mq.queue");

    // Khối static này sẽ chạy 1 lần duy nhất khi class được gọi
    static {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(HOST);
            factory.setPort(PORT);
            factory.setUsername(USER);
            factory.setPassword(PASS);

            connection = factory.newConnection();
            channel = connection.createChannel();

            // 1. Khai báo Exchange
            // Loại "direct", durable=true (bền bỉ)
            channel.exchangeDeclare(EXCHANGE_NAME, "direct", true);

            // 2. Khai báo Queue
            // durable=true (bền bỉ), exclusive=false, autoDelete=false
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);

            // 3. Tạo Binding (liên kết) giữa Exchange và Queue
            // Chúng ta sẽ bind 3 routing key cho queue này
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "employees");
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "attendance");
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "payroll");

            System.out.println("✅ Đã kết nối và thiết lập RabbitMQ (Exchange, Queue, Bindings) thành công!");

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi thiết lập RabbitMQ:");
            e.printStackTrace();
        }
    }

    // Hàm public để các class khác (Producer, Consumer) có thể lấy channel
    public static Channel getChannel() {
        return channel;
    }

    // (Tùy chọn) Hàm để đóng kết nối khi tắt ứng dụng
    public static void close() {
        try {
            if (channel != null) channel.close();
            if (connection != null) connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}