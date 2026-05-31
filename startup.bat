@echo off
echo ==================================================
echo      SOCIAL NETWORK FRIEND SUGGESTION STARTUP
echo ==================================================
echo Creating output directories...
if not exist bin mkdir bin

echo Compiling Java source files...
powershell -ExecutionPolicy Bypass -File compile.ps1

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo Compilation successful! Starting Java application...

set "JAVA_CMD=java"
if exist "C:\Program Files\Java\jdk1.8.0_202\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Java\jdk1.8.0_202\bin\java.exe"
) else if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
    )
)

echo Opening web application and research dashboard in default browser...
start http://localhost:3001
start http://localhost:3001/research.html
"%JAVA_CMD%" -cp "bin;lib/json.jar" Main
