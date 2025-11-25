@echo off
chcp 65001 >nul
title HE THONG TICH HOP DU LIEU (AUTO FULL)
color 0F

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
echo   BUOC 2: CHAY CODE JAVA (GIAI DOAN 3)
echo =================================================
echo.

:: Di chuyen vao thu muc du an (Neu file bat de o ngoai)
:: Neu file bat cua ban nam cung cap voi thu muc etl-rmq thi bo dau :: o dong duoi
cd etl-rmq

echo 1. Dang khoi dong CONSUMER (Ben Nhan)...
:: Lenh nay se mo mot cua so rieng cho Consumer
start "BEN NHAN (Consumer)" java -cp target/etl-rmq-1.0-SNAPSHOT.jar com.example.consumer.StagingConsumer

echo    -> Consumer da duoc bat o cua so rieng.
echo.
echo 2. Dang khoi dong PRODUCER (Ben Gui)...
echo    -> Dang gui 600 ban ghi...
echo.

:: Chay Producer ngay tai day
java -cp target/etl-rmq-1.0-SNAPSHOT.jar com.example.RunProducer

echo.
echo =================================================
echo   DA HOAN TAT!
echo =================================================
echo   - Kiem tra cua so Consumer xem co log INSERT khong.
echo   - Vao MySQL Workbench de xem ket qua.
pause