@echo off
echo ==================================================
echo      SOCIAL NETWORK FRIEND SUGGESTION STARTUP
echo ==================================================
echo Creating output directories...
if not exist bin mkdir bin

echo Checking and freeing port 3001 if occupied...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :3001 ^| findstr LISTENING 2^>nul') do (
    echo Found process %%a using port 3001. Killing it...
    taskkill /f /pid %%a >nul 2>&1
)

set "JAVA_CMD=java"
set "JAVAC_CMD=javac"
if exist "C:\Program Files\Java\jdk1.8.0_202\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Java\jdk1.8.0_202\bin\java.exe"
    set "JAVAC_CMD=C:\Program Files\Java\jdk1.8.0_202\bin\javac.exe"
) else if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
    )
    if exist "%JAVA_HOME%\bin\javac.exe" (
        set "JAVAC_CMD=%JAVA_HOME%\bin\javac.exe"
    )
)

echo Compiling Java source files...
"%JAVAC_CMD%" -encoding UTF-8 -cp "lib/json.jar" -d bin src/Main.java src/BenchmarkRunner.java src/api/*.java src/benchmark/*.java src/console/*.java src/datastructures/*.java src/graph/*.java src/model/*.java src/service/*.java src/services/*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo Compilation successful! Starting Java application...

echo Opening web application and research dashboard in default browser...
start http://localhost:3001
start http://localhost:3001/research.html
chcp 65001 >nul
"%JAVA_CMD%" -Dfile.encoding=UTF-8 -cp "bin;lib/json.jar" Main
