# PowerShell launcher for Bus Pass Management Desktop Application
Write-Host "=== Setting up Environment & Dependencies ===" -ForegroundColor Cyan
if (!(Test-Path "lib")) { New-Item -ItemType Directory -Path "lib" | Out-Null }
if (!(Test-Path "database")) { New-Item -ItemType Directory -Path "database" | Out-Null }
if (!(Test-Path "bin")) { New-Item -ItemType Directory -Path "bin" | Out-Null }

if (!(Test-Path "lib\sqlite-jdbc.jar")) {
    Write-Host "Fetching SQLite JDBC Driver..." -ForegroundColor Yellow
    Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/sqlite-jdbc-3.45.1.0.jar' -OutFile 'lib\sqlite-jdbc.jar' -UseBasicParsing
}
if (!(Test-Path "lib\slf4j-api.jar")) {
    Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar' -OutFile 'lib\slf4j-api.jar' -UseBasicParsing
}
if (!(Test-Path "lib\slf4j-simple.jar")) {
    Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.36/slf4j-simple-1.7.36.jar' -OutFile 'lib\slf4j-simple.jar' -UseBasicParsing
}

Write-Host "=== Compiling Java Source Files ===" -ForegroundColor Cyan
javac -cp "lib/*;src" -d bin src/model/*.java src/exception/*.java src/dao/*.java src/service/*.java src/util/*.java src/monitor/*.java src/gui/*.java src/web/*.java src/Main.java

if ($LASTEXITCODE -eq 0) {
    Write-Host "=== Launching Application (AWT GUI + http://localhost:8080) ===" -ForegroundColor Green
    java -cp "bin;lib/*" Main
} else {
    Write-Host "[ERROR] Compilation failed." -ForegroundColor Red
}
