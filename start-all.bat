@echo off

echo [1/4] Cleaning ports...

REM Kill port 8080
for /f "tokens=5" %%i in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do (
    taskkill /F /PID %%i 2>nul
)

REM Kill port 3000
for /f "tokens=5" %%i in ('netstat -ano ^| findstr ":3000" ^| findstr "LISTENING"') do (
    taskkill /F /PID %%i 2>nul
)

REM Kill port 3001
for /f "tokens=5" %%i in ('netstat -ano ^| findstr ":3001" ^| findstr "LISTENING"') do (
    taskkill /F /PID %%i 2>nul
)

REM Kill port 5173
for /f "tokens=5" %%i in ('netstat -ano ^| findstr ":5173" ^| findstr "LISTENING"') do (
    taskkill /F /PID %%i 2>nul
)

timeout /t 2 >nul
echo OK
echo.

echo [2/4] Starting backend (port 8080)...
cd /d %~dp0backend
start "Backend" cmd /c "mvn spring-boot:run"

echo [3/4] Starting frontend user (port 3000)...
cd /d %~dp0frontend-user
start "FrontendUser" cmd /c "npm run dev"

echo [4/4] Starting frontend admin (port 3001)...
cd /d %~dp0frontend-admin
start "FrontendAdmin" cmd /c "npm run dev"

echo =================================
echo Done!
echo =================================
echo Backend API: http://localhost:8080
echo Swagger: http://localhost:8080/swagger-ui.html
echo User Frontend: http://localhost:3000
echo Admin Frontend: http://localhost:3001
echo Admin account: admin / admin123
echo =================================

pause
