@echo off
REM ============================================================
REM College Bus Pass Management Desktop Application Launcher
REM ============================================================

if not exist lib mkdir lib
if not exist database mkdir database
if not exist bin mkdir bin

REM Check and download SQLite JDBC driver if missing
if not exist "lib\sqlite-jdbc.jar" (
    echo Downloading SQLite JDBC Driver...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/sqlite-jdbc-3.45.1.0.jar' -OutFile 'lib\sqlite-jdbc.jar' -UseBasicParsing"
)

if not exist "lib\slf4j-api.jar" (
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar' -OutFile 'lib\slf4j-api.jar' -UseBasicParsing"
)

if not exist "lib\slf4j-simple.jar" (
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.36/slf4j-simple-1.7.36.jar' -OutFile 'lib\slf4j-simple.jar' -UseBasicParsing"
)

echo Compiling Java Sources...
javac -cp "lib/*;src" -d bin src/model/*.java src/exception/*.java src/dao/*.java src/service/*.java src/util/*.java src/monitor/*.java src/gui/*.java src/web/*.java src/Main.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo Launching Bus Pass Management System (AWT Desktop + http://localhost:8080)...
java -cp "bin;lib/*" Main
pause
