@echo off
chcp 65001 >nul
title Hệ Thống Tích Hợp Dữ Liệu
color 0E

echo =================================================
echo  Bước 1: Khởi động hạ tầng (Docker) 
echo =================================================

echo Đang kiểm tra docker...
docker info >nul 2>&1
if %errorlevel% neq 0 (
    color 0C
    echo [LOI] Docker chưa bật hãy bật docker trước
    pause
    exit
)

echo [OK] Đang bật container...
docker start rabbitmq-payroll
docker start mysql-payroll

echo.
echo =================================================
echo   Bước 2: Dọn dẹp dữ liệu cũ
echo =================================================
echo.

echo Đang xóa sạch bảng Staging để tránh trùng lặp ảo...
:: lệnh này sẽ xóa trắng bảng trung gian trước khi nạp mới
docker exec -i mysql-payroll mysql -u root -p123456 payroll -e "TRUNCATE TABLE staging_records;"

echo [OK] Kho trung gian đã sạch sẽ. Sẵn sàng nạp mới.

echo.
echo =================================================
echo   Bước 3: Chạy code Java (ETL)
echo =================================================
echo.

cd /d "%~dp0"
cd etl-rmq

:: Kiem tra file JAR
if not exist "target\etl-rmq-1.0-SNAPSHOT.jar" (
    color 0C
    echo [LOI] Không tìm thấy file JAR! Hãy chạy 'mvn package' lại.
    pause
    exit
)

echo 1. Đang khởi động CONSUMER...
start "BEN NHAN (Consumer)" java -cp target/etl-rmq-1.0-SNAPSHOT.jar com.example.consumer.StagingConsumer

echo.
echo 2. Đang khởi động PRODUCER...
java -cp target/etl-rmq-1.0-SNAPSHOT.jar com.example.RunProducer

echo.
echo    -> Cho 5 giây để dữ liệu nạp vào DB...
timeout /t 5 /nobreak >nul

echo.
echo =================================================
echo   Bước 4: Xử lý logic và xuất báo cáo
echo =================================================
echo.

:: Chạy file SQL xử lý
docker exec -i mysql-payroll mysql -u root -p123456 payroll < process_data.sql
echo [OK] Đã lọc trùng và nạp bảng đích.

:: Tạo thư mục ket_qua
if not exist "ket_qua" mkdir ket_qua

echo 1. Xuất BaoCao_NhanVien.xls ...
docker exec -i mysql-payroll mysql -u root -p123456 payroll -e "SELECT * FROM employees;" > ket_qua/BaoCao_NhanVien.xls

echo 2. Xuất BaoCao_ChamCong.xls ...
docker exec -i mysql-payroll mysql -u root -p123456 payroll -e "SELECT * FROM attendance;" > ket_qua/BaoCao_ChamCong.xls

echo 3. Xuất BaoCao_Loi_TrungLap.xls ...
docker exec -i mysql-payroll mysql -u root -p123456 payroll -e "SELECT * FROM dedup_duplicate;" > ket_qua/BaoCao_Loi_TrungLap.xls

echo.
echo =================================================
echo   ĐÃ HOÀN TẤT! KIỂM TRA THƯ MỤC 'ket_qua'
echo =================================================
pause