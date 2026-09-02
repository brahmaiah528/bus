# PowerShell launcher for Bus Pass Management Desktop Application
Write-Host "=== Compiling College Bus Pass Management System ===" -ForegroundColor Cyan
if (!(Test-Path "bin")) { New-Item -ItemType Directory -Path "bin" | Out-Null }

javac -cp "lib/*;src" -d bin src/model/*.java src/exception/*.java src/dao/*.java src/service/*.java src/util/*.java src/monitor/*.java src/gui/*.java src/Main.java

if ($LASTEXITCODE -eq 0) {
    Write-Host "=== Launching AWT GUI Application ===" -ForegroundColor Green
    java -cp "bin;lib/*" Main
} else {
    Write-Host "[ERROR] Compilation failed." -ForegroundColor Red
}
