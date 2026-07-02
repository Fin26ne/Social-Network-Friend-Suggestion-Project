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

echo [1/3] Bien dich Java...
echo.

echo Kiem tra port 3003...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :3003 2^>nul') do (
    if not "%%a"=="0" (
        taskkill /PID %%a /F > nul 2>&1
    )
)
ping -n 2 127.0.0.1 > nul
echo Port 3003 san sang.
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

echo [1/3] Bien dich thanh cong!
echo.
echo [2/3] Mo browser sau khi server san sang...
echo.

start "" cmd /c "ping -n 4 127.0.0.1 > nul & start http://localhost:3003/home.html & ping -n 1 127.0.0.1 > nul & start http://localhost:3003/research.html & exit"

echo [3/3] Khoi dong Java Server...
echo.
echo ===================================================
echo   Dashboard  : http://localhost:3003/home.html
echo   Research   : http://localhost:3003/research.html
echo   API Test   : http://localhost:3003/api/users
echo ===================================================
echo.
echo   Nhan Ctrl+C de dung server.
echo.

"%JAVA_CMD%" -Dfile.encoding=UTF-8 -cp backend-java\bin;backend-java\lib\json-20240303.jar Main

echo.
echo Server da dung.
pause