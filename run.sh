#!/usr/bin/env bash
# Shell script for Linux / macOS environments
echo "=== Compiling College Bus Pass Management System ==="
mkdir -p bin
javac -cp "lib/*:src" -d bin src/model/*.java src/exception/*.java src/dao/*.java src/service/*.java src/util/*.java src/monitor/*.java src/gui/*.java src/Main.java

if [ $? -eq 0 ]; then
    echo "=== Launching AWT GUI Application ==="
    java -cp "bin:lib/*" Main
else
    echo "[ERROR] Compilation failed."
    exit 1
fi
