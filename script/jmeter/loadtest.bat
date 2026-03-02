@echo off
REM Campus Platform 压力测试脚本
REM 使用方法: loadtest URL [并发数] [时间(秒)]

set URL=%1
set CONCURRENCY=%2
set TIME=%3

if "%URL%"=="" set URL=http://localhost:8080/api/boards
if "%CONCURRENCY%"=="" set CONCURRENCY=20
if "%TIME%"=="" set TIME=30

echo ==========================================
echo Campus Platform 压力测试
echo ==========================================
echo URL: %URL%
echo 并发数: %CONCURRENCY%
echo 持续时间: %TIME%秒
echo ==========================================

loadtest -t %TIME% -c %CONCURRENCY% %URL%

echo.
echo ==========================================
echo 测试完成
echo ==========================================
