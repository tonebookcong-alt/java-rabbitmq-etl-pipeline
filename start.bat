@echo off
chcp 65001 >nul
title HE THONG TICH HOP DU LIEU (FINAL VERSION)
color 0E

echo =================================================
echo   BUOC 1: KHOI DONG HA TANG (DOCKER)
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
echo   BUOC 2: DON DEP DU LIEU CU (QUAN TRONG)
echo =================================================
echo.

echo Dang xoa sach bang Staging de tranh trung lap ao...
:: Lenh nay se xoa trang bang trung gian truoc khi nap moi
docker exec -i mysql-payroll mysql -u root -p123456 payroll -e "TRUNCATE TABLE staging_records;"

echo [OK] Kho trung gian da sach se. San sang nap moi.

echo.
echo =================================================
echo   BUOC 3: CHAY CODE JAVA (ETL)
echo =================================================
echo.

cd /d "%~dp0"
cd etl-rmq

:: Kiem tra file JAR
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
echo    -> Cho 5 giay de du lieu nap vao DB...
timeout /t 5 /nobreak >nul

echo.
echo =================================================
echo   BUOC 4: XU LY LOGIC VA XUAT BAO CAO
echo =================================================
echo.

:: Chay file SQL xu ly
docker exec -i mysql-payroll mysql -u root -p123456 payroll < process_data.sql
echo [OK] Da loc trung va nap bang dich.

:: Tao thu muc ket_qua
if not exist "ket_qua" mkdir ket_qua

echo 1. Xuat BaoCao_NhanVien.xls ...
docker exec -i mysql-payroll mysql -u root -p123456 payroll -e "SELECT * FROM employees;" > ket_qua/BaoCao_NhanVien.xls

echo 2. Xuat BaoCao_ChamCong.xls ...
docker exec -i mysql-payroll mysql -u root -p123456 payroll -e "SELECT * FROM attendance;" > ket_qua/BaoCao_ChamCong.xls

echo 3. Xuat BaoCao_Loi_TrungLap.xls ...
docker exec -i mysql-payroll mysql -u root -p123456 payroll -e "SELECT * FROM dedup_duplicate;" > ket_qua/BaoCao_Loi_TrungLap.xls

echo.
echo =================================================
echo   DA HOAN TAT! KIEM TRA THU MUC 'ket_qua'
echo =================================================
pause