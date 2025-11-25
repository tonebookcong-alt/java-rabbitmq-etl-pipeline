Payroll Data Integration Pipeline

Project Topic: Automated ETL Data Integration System using Java, RabbitMQ, MySQL, and Docker.

1. INTRODUCTION

This project implements a data integration system that ingests Employee data (from CSV files) and Attendance data (from a SQL Database Source) into a centralized MySQL Data Warehouse.

The system follows an Event-Driven Architecture to ensure scalability and reliability.

Key Features:

Multi-Source Ingestion: Handles both flat files (.csv) and relational databases.

Message Broker: Utilizes RabbitMQ for asynchronous data buffering.

Data Validation: Java-based Regex validation filters out invalid records before ingestion.

Deduplication Logic: Advanced SQL strategies identify and separate duplicate records.

Automated Workflow: A single start.bat script orchestrates the entire pipeline.

2. TECHNOLOGY STACK

Java 17+: Core processing logic (Producers & Consumers).

RabbitMQ: Message Broker for decoupling components.

MySQL 8.0: Relational Database for Staging and Production data.

Docker: Containerization for infrastructure (RabbitMQ & MySQL).

Maven: Dependency management and build tool.

3. SYSTEM ARCHITECTURE

The system pipeline consists of 4 main stages:

Extraction & Validation:

Producers read raw data from sources.

Invalid records (e.g., bad ID format) are logged and rejected immediately.

Transformation & Queuing:

Valid records are hashed (SHA-256) and wrapped in JSON.

Messages are published to a RabbitMQ queue (staging_queue).

Loading (Staging):

A Consumer listens to the queue and inserts raw JSON into the staging_records table.

Processing (Deduplication & Final Load):

SQL scripts analyze the staging table.

Unique records are moved to final tables (employees, attendance).

Duplicate records are moved to an error report table (dedup_duplicate).

4. PROJECT STRUCTURE

etl-rmq/
├── data/                   # Input Data (CSV files)
├── src/
│   ├── main/java/com/example/
│   │   ├── config/         # Configuration classes
│   │   ├── connection/     # DB & RabbitMQ Connection logic
│   │   ├── consumer/       # StagingConsumer (Data Loader)
│   │   ├── producer/       # Producers (Data Extractors)
│   │   ├── model/          # Data Models (POJOs)
│   │   └── util/           # Utility classes (Regex, Hash)
│   └── resources/
│       └── application.properties
├── process_data.sql        # SQL Logic for ETL transformation
├── start.bat               # ONE-CLICK Automation Script
└── pom.xml                 # Maven Configuration


5. HOW TO RUN

Prerequisites

Docker Desktop must be installed and running.

Java JDK 17+ installed.

Execution Steps

Clone this repository.

Navigate to the project root folder.

Double-click start.bat.

The script will automatically:

Start Docker containers.

Compile the Java code.

Run Producers and Consumers.

Execute SQL processing scripts.

Export final reports to the ket_qua directory.

6. OUTPUTS

After execution, the system generates the following reports in the ket_qua folder:

BaoCao_NhanVien.csv: Clean list of Employees.

BaoCao_ChamCong.csv: Clean list of Attendance records.

BaoCao_Loi_TrungLap.csv: Report of duplicate records detected.

BaoCao_TongHop_Full.csv: A joined report combining Employee and Attendance data.

---------------------------------------------------------------------------------------
Payroll Data Integration Pipeline

Đề tài: Hệ thống Tích hợp Dữ liệu ETL Tự động sử dụng Java, RabbitMQ, MySQL và Docker.

1. GIỚI THIỆU (INTRODUCTION)

Dự án này triển khai một hệ thống tích hợp dữ liệu (ETL Pipeline), có khả năng thu thập và đồng bộ hóa dữ liệu từ hai nguồn khác nhau:

Dữ liệu Nhân viên (Employee Data): Từ file văn bản (CSV).

Dữ liệu Chấm công (Attendance Data): Từ một bảng trong Cơ sở dữ liệu SQL nguồn.

Dữ liệu sau khi xử lý sẽ được lưu trữ tập trung tại Kho Dữ liệu MySQL (Data Warehouse). Hệ thống áp dụng kiến trúc Hướng sự kiện (Event-Driven Architecture) để đảm bảo khả năng mở rộng và độ tin cậy cao.

Tính năng Nổi bật (Key Features):

Thu thập Đa nguồn (Multi-Source Ingestion): Xử lý đồng thời cả file văn bản (.csv) và cơ sở dữ liệu quan hệ.

Môi giới Tin nhắn (Message Broker): Sử dụng RabbitMQ để đệm dữ liệu bất đồng bộ, giúp hệ thống không bị quá tải.

Kiểm tra Dữ liệu (Data Validation): Sử dụng Java Regex để phát hiện và lọc bỏ ngay lập tức các bản ghi lỗi (sai định dạng, thiếu cột...).

Khử Trùng lặp (Deduplication Logic): Áp dụng thuật toán SQL nâng cao để phát hiện và tách riêng các bản ghi bị nhập trùng.

Quy trình Tự động (Automated Workflow): Một kịch bản start.bat duy nhất điều phối toàn bộ quá trình từ bật hạ tầng đến xuất báo cáo.

2. CÔNG NGHỆ SỬ DỤNG (TECHNOLOGY STACK)

Công nghệ

Vai trò chính

Lý do lựa chọn

Java 17+

Ngôn ngữ xử lý chính (Core Logic)

Mạnh mẽ, ổn định, hỗ trợ đa luồng tốt cho Producer/Consumer.

RabbitMQ

Môi giới tin nhắn (Message Broker)

Giúp tách biệt (decouple) việc đọc và ghi dữ liệu, đảm bảo an toàn dữ liệu.

MySQL 8.0

Cơ sở dữ liệu (Database)

Lưu trữ dữ liệu trung gian (Staging) và dữ liệu đích (Production). Hỗ trợ SQL mạnh.

Docker

Container hóa (Infrastructure)

Đóng gói RabbitMQ và MySQL giúp chạy ngay không cần cài đặt phức tạp.

Maven

Quản lý dự án (Build Tool)

Quản lý thư viện và đóng gói code Java thành file chạy .jar.

3. KIẾN TRÚC HỆ THỐNG (SYSTEM ARCHITECTURE)

Quy trình xử lý dữ liệu bao gồm 4 giai đoạn chính:

Thu thập & Kiểm tra (Extraction & Validation):

Các Producer đọc dữ liệu thô từ nguồn.

Các bản ghi lỗi (ví dụ: sai định dạng ID) bị loại bỏ ngay lập tức và ghi log cảnh báo.

Biến đổi & Vận chuyển (Transformation & Queuing):

Dữ liệu sạch được mã hóa (Hash SHA-256) và đóng gói thành JSON.

Tin nhắn được gửi vào hàng đợi RabbitMQ (staging_queue).

Tải vào Kho tạm (Loading - Staging):

Một Consumer lắng nghe hàng đợi và chèn dữ liệu JSON thô vào bảng staging_records.

Xử lý Logic (Processing - Deduplication & Final Load):

Các kịch bản SQL phân tích bảng Staging.

Bản ghi Duy nhất: Được chuyển vào bảng đích (employees, attendance).

Bản ghi Trùng lặp: Được chuyển vào bảng báo cáo lỗi (dedup_duplicate).

4. CẤU TRÚC DỰ ÁN (PROJECT STRUCTURE)

etl-rmq/
├── data/                   # Chứa file dữ liệu đầu vào (CSV files)
├── src/
│   ├── main/java/com/example/
│   │   ├── config/         # Cấu hình hệ thống (Config)
│   │   ├── connection/     # Kết nối Database & RabbitMQ
│   │   ├── consumer/       # StagingConsumer (Người nhận dữ liệu)
│   │   ├── producer/       # Producers (Người đọc dữ liệu)
│   │   ├── model/          # Các đối tượng dữ liệu (POJOs)
│   │   └── util/           # Tiện ích (Regex, Hash)
│   └── resources/
│       └── application.properties
├── process_data.sql        # Kịch bản SQL xử lý logic ETL
├── start.bat               # Script chạy tự động (ONE-CLICK)
└── pom.xml                 # Cấu hình Maven


5. HƯỚNG DẪN CHẠY (HOW TO RUN)

Yêu cầu tiên quyết (Prerequisites)

Docker Desktop phải được cài đặt và đang chạy.

Java JDK 17+ đã được cài đặt.

Các bước thực hiện (Execution Steps)

Clone (tải) dự án này về máy.

Vào thư mục gốc của dự án.

Nhấp đúp chuột vào file start.bat.

Kịch bản sẽ tự động thực hiện các việc sau:

Bật các container Docker (RabbitMQ, MySQL).

Biên dịch và chạy code Java (Producer/Consumer).

Thực thi các kịch bản xử lý SQL.

Xuất các báo cáo kết quả ra thư mục ket_qua.

6. KẾT QUẢ ĐẦU RA (OUTPUTS)

Sau khi chạy xong, hệ thống sẽ tự động sinh ra các báo cáo trong thư mục ket_qua:

BaoCao_NhanVien.csv: Danh sách nhân viên chuẩn (Sạch).

BaoCao_ChamCong.csv: Danh sách chấm công chuẩn (Sạch).

BaoCao_Loi_TrungLap.csv: Báo cáo các bản ghi bị phát hiện trùng lặp.

BaoCao_TongHop_Full.csv: Bảng tổng hợp thông tin nhân viên kết hợp với chấm công.
