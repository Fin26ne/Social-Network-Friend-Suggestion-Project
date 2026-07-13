@echo off
chcp 65001 > nul
cls

set "JAVAC_CMD=javac"
set "JAVA_CMD=java"

where javac >nul 2>nul
if %errorlevel% neq 0 (
    for /d %%d in ("C:\Program Files\Java\jdk*") do (
        if exist "%%d\bin\javac.exe" (
            set "JAVAC_CMD=%%d\bin\javac.exe"
            set "JAVA_CMD=%%d\bin\java.exe"
        )
    )
)

echo ===================================================
echo   Social Network Friend Suggestion System
echo   CSD201 - FPT University
echo ===================================================
echo.

echo [1/4] Bien dich Java...
echo.

echo Kiem tra port 3003 (Backend) va 3004 (Frontend)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :3003 2^>nul') do (
    if not "%%a"=="0" (
        taskkill /PID %%a /F > nul 2>&1
    )
)
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :3004 2^>nul') do (
    if not "%%a"=="0" (
        taskkill /PID %%a /F > nul 2>&1
    )
)
ping -n 2 127.0.0.1 > nul
echo Port 3003 va 3004 san sang.
echo.

if not exist backend-java\bin mkdir backend-java\bin

"%JAVAC_CMD%" -encoding UTF-8 -cp backend-java\lib\json-20240303.jar -d backend-java\bin backend-java\src\Main.java backend-java\src\api\*.java backend-java\src\benchmark\*.java backend-java\src\console\*.java backend-java\src\datastructures\*.java backend-java\src\graph\*.java backend-java\src\model\*.java backend-java\src\service\*.java backend-java\src\services\*.java

if %errorlevel% neq 0 (
    echo.
    echo LOI BIEN DICH - Kiem tra lai code
    echo.
    pause
    exit /b 1
)

echo [1/4] Bien dich thanh cong!
echo.
echo [2/4] Khoi dong Next.js Frontend...
echo.

start "Frontend (Next.js)" cmd /c "cd frontend & npm run dev"

echo [3/4] Mo browser sau khi server san sang...
echo.

start "" cmd /c "ping -n 8 127.0.0.1 > nul & start http://localhost:3004/ & exit"

echo [4/4] Khoi dong Java Backend...
echo.
echo ===================================================
echo   Frontend (Next.js): http://localhost:3004
echo   Backend (Java API): http://localhost:3003/api/users
echo ===================================================
echo.
echo   Nhan Ctrl+C tai cua so nay de dung Java Backend.
echo   Dong cua so Frontend rieng de dung Next.js.
echo.

"%JAVA_CMD%" -Xmx5g -Dfile.encoding=UTF-8 -cp backend-java\bin;backend-java\lib\json-20240303.jar Main

echo.
echo Server da dung.
pause