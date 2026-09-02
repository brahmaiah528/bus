#!/usr/bin/env bash
# Shell script for Linux / macOS environments
echo "=== Setting up Environment & Dependencies ==="
mkdir -p lib database bin

if [ ! -f "lib/sqlite-jdbc.jar" ]; then
    echo "Downloading SQLite JDBC Driver..."
    curl -s -L "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/sqlite-jdbc-3.45.1.0.jar" -o "lib/sqlite-jdbc.jar"
fi
if [ ! -f "lib/slf4j-api.jar" ]; then
    curl -s -L "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar" -o "lib/slf4j-api.jar"
fi
if [ ! -f "lib/slf4j-simple.jar" ]; then
    curl -s -L "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.36/slf4j-simple-1.7.36.jar" -o "lib/slf4j-simple.jar"
fi

echo "=== Compiling Java Source Files ==="
javac -cp "lib/*:src" -d bin src/model/*.java src/exception/*.java src/dao/*.java src/service/*.java src/util/*.java src/monitor/*.java src/gui/*.java src/web/*.java src/Main.java

if [ $? -eq 0 ]; then
    echo "=== Launching Application (AWT GUI + http://localhost:8080) ==="
    java -cp "bin:lib/*" Main
else
    echo "[ERROR] Compilation failed."
    exit 1
fi
