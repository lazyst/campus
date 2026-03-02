@echo off
REM JMeter 压力测试 - Docker运行脚本 (Windows)

REM 配置参数
set THREADS=%1
if "%THREADS%"=="" set THREADS=10
set RAMP_UP=%2
if "%RAMP_UP%"=="" set RAMP_UP=5
set DURATION=%3
if "%DURATION%"=="" set DURATION=60
set BASE_URL=%4
if "%BASE_URL%"=="" set BASE_URL=host.docker.internal:8080

REM 获取脚本目录
set SCRIPT_DIR=%~dp0
set JMX_FILE=%SCRIPT_DIR%campus-platform-test.jmx

REM 创建报告目录
for /f "tokens=1-4 delims=/ " %%a in ('date /t') do (
    set REPORT_DIR=%SCRIPT_DIR%reports\%%a%%b%%c_%%d
)
mkdir "%REPORT_DIR%" 2>nul

echo ==========================================
echo Campus Platform JMeter Pressure Test
echo ==========================================
echo Threads: %THREADS%
echo Ramp-up: %RAMP_UP% sec
echo Duration: %DURATION% sec
echo Base URL: %BASE_URL%
echo Report Dir: %REPORT_DIR%
echo ==========================================

REM 检查Docker
docker --version >nul 2>&1
if errorlevel 1 (
    echo Error: Docker not found
    exit /b 1
)

REM 运行测试
docker run --rm ^
    -v "%SCRIPT_DIR%:/workspace" ^
    -v "%REPORT_DIR%:/reports" ^
    justb4/jmeter:latest ^
    -n ^
    -t /workspace/campus-platform-test.jmx ^
    -JTHREADS=%THREADS% ^
    -JRAMP_UP=%RAMP_UP% ^
    -JDURATION=%DURATION% ^
    -JBASE_URL=%BASE_URL% ^
    -l /reports/results.jtl ^
    -e ^
    -o /reports/html

echo.
echo ==========================================
echo Test Complete!
echo ==========================================
echo HTML Report: %REPORT_DIR%\html\index.html
echo Results: %REPORT_DIR%\results.jtl
echo ==========================================

REM 打开报告
start "" "%REPORT_DIR%\html\index.html"
