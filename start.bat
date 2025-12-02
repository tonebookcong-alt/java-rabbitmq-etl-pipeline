@echo off
chcp 65001 >nul
title HE THONG TICH HOP DU LIEU (ETL Pipeline)
color 0E

echo =================================================
echo   GIAI DOAN 1: KHOI DONG HA TANG (DOCKER)
echo =================================================

echo Dang kiem tra Docker...
docker info >nul 2>&1
if %errorlevel% neq 0 (
    color 0C
    echo [LOI] Docker chua bat! Hay bat Docker Desktop truoc.
    pause
    exit
)

echo [OK] Dang bat Container...
docker start rabbitmq-payroll
docker start mysql-payroll

echo.
echo =================================================
echo   GIAI DOAN 2: DON DEP DU LIEU CU
echo =================================================
echo.

echo 1. Dang xoa sach du lieu trong CSDL...
docker exec -i mysql-payroll mysql -u root -p123456 payroll -e "SET FOREIGN_KEY_CHECKS = 0; TRUNCATE TABLE staging_records; TRUNCATE TABLE dedup_unique; TRUNCATE TABLE dedup_duplicate; TRUNCATE TABLE employees; TRUNCATE TABLE attendance; SET FOREIGN_KEY_CHECKS = 1;"
echo [OK] CSDL da sach se.

echo 2. Dang xoa sach tin nhan trong RabbitMQ...
docker exec rabbitmq-payroll rabbitmqadmin purge queue name=staging_queue
echo [OK] Hang doi (Queue) da sach se.

echo.
echo =================================================
echo   GIAI DOAN 3: CHAY CODE JAVA (EXTRACT ^& LOAD TO STAGING)
echo =================================================
echo.

cd /d "%~dp0"
cd etl-rmq

if not exist "target\etl-rmq-1.0-SNAPSHOT.jar" (
    color 0C
    echo [LOI] Khong tim thay file JAR! Hay chay 'mvn package' lai.
    pause
    exit
)

echo 1. Dang khoi dong CONSUMER...
start "BEN NHAN (Consumer)" java -cp target/etl-rmq-1.0-SNAPSHOT.jar com.example.consumer.StagingConsumer

echo.
echo 2. Dang khoi dong PRODUCER...
java -cp target/etl-rmq-1.0-SNAPSHOT.jar com.example.RunProducer

echo.
echo    -> Cho 10 giay de du lieu nap vao DB...
timeout /t 10 /nobreak >nul
echo.
echo =================================================
echo   GIAI DOAN 4: XU LY LOGIC VA XUAT BAO CAO
echo =================================================
echo.

echo Dang chay SQL de loc trung va nap du lieu...
docker exec -i mysql-payroll mysql -u root -p123456 payroll < process_data.sql
echo [OK] Da xu ly xong du lieu trong CSDL.

echo.
echo Dang xuat cac file bao cao...
if not exist "ket_qua" mkdir ket_qua

echo   1. Xuat BaoCao_NhanVien.xls ...
docker exec -i mysql-payroll mysql -N -B -u root -p123456 payroll -e "SELECT * FROM employees;" > ket_qua/BaoCao_NhanVien.xls

echo   2. Xuat BaoCao_ChamCong.xls ...
docker exec -i mysql-payroll mysql -N -B -u root -p123456 payroll -e "SELECT * FROM attendance;" > ket_qua/BaoCao_ChamCong.xls

echo   3. Xuat BaoCao_Loi_TrungLap.xls ...
docker exec -i mysql-payroll mysql -N -B -u root -p123456 payroll -e "SELECT * FROM dedup_duplicate;" > ket_qua/BaoCao_Loi_TrungLap.xls

echo   4. Xuat BaoCao_Loi_DinhDang.xls ...
docker exec -i mysql-payroll mysql -N -B -u root -p123456 payroll -e "SELECT * FROM staging_records WHERE status = 'INVALID';" > ket_qua/BaoCao_Loi_DinhDang.xls

echo   5. Xuat BaoCao_TongHop_Full.xls ...
docker exec -i mysql-payroll mysql -u root -p123456 payroll -e "SELECT 'Ma NV', 'Ho Ten', 'Phong Ban', 'Luong Co Ban', 'Ngay Lam', 'Gio Vao', 'Gio Ra', 'Gio Tang Ca' UNION ALL SELECT e.emp_id, e.name, e.department, e.base_salary, a.work_date, a.check_in, a.check_out, a.ot_hours FROM employees e JOIN attendance a ON e.emp_id = a.emp_id;" > ket_qua/BaoCao_TongHop_Full.xls

echo.
echo =================================================
echo   DA HOAN TAT! KIEM TRA THU MUC 'ket_qua'
echo =================================================
pause