@echo off
REM ============================================================
REM College Bus Pass Management Desktop Application Launcher
REM ============================================================

echo Compiling Java Sources...
if not exist bin mkdir bin
javac -cp "lib/*;src" -d bin src/model/*.java src/exception/*.java src/dao/*.java src/service/*.java src/util/*.java src/monitor/*.java src/gui/*.java src/Main.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo Launching Bus Pass Management System...
java -cp "bin;lib/*" Main
pause
