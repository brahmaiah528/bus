# Bus Pass Management Desktop Application (Java AWT & SQLite)

An institutional desktop application designed for college transport departments to automate passenger registrations, bus route cataloging, fee computations, pass issuance, validity monitoring, renewals, cancellations, and route search.

Built for B.Tech Computer Science and Engineering coursework, showcasing Object-Oriented Programming (OOP) principles, Java Collections Framework, Generics, Iterators, Custom Checked Exceptions, Multithreading, JDBC CRUD with SQLite, and Java Abstract Window Toolkit (AWT) event-driven GUI.

---

## 📌 Features

1. **Passenger Registration**:
   - Register Students and Faculty with strict data validation.
   - Encapsulated model hierarchy (`Passenger`, `Student`, `Faculty`).
2. **Route Catalog Management**:
   - Maintain route numbers, origins, destinations, boarding points, base fares, and operational statuses.
3. **Automated Polymorphic Fee Calculation**:
   - Dynamic fee calculation at runtime:
     - **Student**: 20% educational subsidy discount applied on Monthly / Semester periods.
     - **Faculty**: 10% staff benefit discount applied on Monthly / Semester periods.
4. **Pass Operations**:
   - **Issuance**: Automatic issue and expiry date calculation (+30 days Monthly, +180 days Semester) with unique Pass ID generation.
   - **Renewal**: Extends validity from current expiry or today's date.
   - **Cancellation**: Soft-deletes passes by marking status as `CANCELLED` to retain historical audit trails.
   - **Route Transfer**: Updates assigned route and adjusts fee differentials.
5. **In-Memory Collection Search**:
   - High-speed case-insensitive search across Route Number, Destination, Boarding Point, and Source.
   - Demonstrates explicit `Iterator<BusRoute>` traversal with Java Generics.
6. **Background Expiry Monitoring Thread**:
   - Asynchronous `PassValidityMonitor` background thread scans SQLite database for expired and soon-to-expire (<= 7 days) passes without blocking the AWT UI.
7. **Robust Exception Handling**:
   - **User-Defined Exceptions**: `InvalidRouteException`, `PassValidityExpiredException`.
   - **Built-in Exceptions**: `IllegalArgumentException`, `NumberFormatException`, `SQLException`.
   - Comprehensive `try-catch-finally` with user-friendly AWT modal dialogs.
8. **Embedded Database Persistence**:
   - Embedded SQLite database (`database/buspass.db`) accessed via JDBC `PreparedStatement` and try-with-resources.
   - Auto-initialization with 5+ pre-seeded passengers, routes, and passes.

---

## 🛠️ Technology Stack & Requirements

- **Language**: Java JDK 17 (or newer)
- **GUI Framework**: Java AWT (`Frame`, `Panel`, `CardLayout`, `GridLayout`, `BorderLayout`, `FlowLayout`, `Button`, `TextField`, `TextArea`, `Choice`, `Dialog`)
- **Database**: SQLite 3 (embedded)
- **Database Driver**: SQLite JDBC Driver (`sqlite-jdbc-3.45.1.0.jar`)
- **Logging Facade**: `slf4j-api.jar` & `slf4j-simple.jar`

---

## 📂 Project Structure

```
BusPassManagement/
│
├── src/
│   ├── model/
│   │   ├── Passenger.java          # Abstract base class
│   │   ├── Student.java            # Subclass with 20% discount logic
│   │   ├── Faculty.java            # Subclass with 10% discount logic
│   │   ├── BusRoute.java           # Route entity
│   │   └── BusPass.java            # Pass entity
│   │
│   ├── exception/
│   │   ├── InvalidRouteException.java          # User-defined checked exception
│   │   └── PassValidityExpiredException.java   # User-defined checked exception
│   │
│   ├── dao/
│   │   ├── PassengerDAO.java       # JDBC CRUD for Passenger table
│   │   ├── BusRouteDAO.java        # JDBC CRUD for Bus Route table
│   │   └── BusPassDAO.java         # JDBC CRUD for Bus Pass table
│   │
│   ├── service/
│   │   ├── PassengerService.java   # Passenger business logic & validation
│   │   ├── RouteService.java       # Route cache, Iterator & Generics search
│   │   └── BusPassService.java     # Pass issuance, renewal, cancellation logic
│   │
│   ├── util/
│   │   ├── DatabaseConnection.java # SQLite JDBC connection manager
│   │   └── DatabaseInitializer.java# Auto-DDL & data seeding
│   │
│   ├── monitor/
│   │   └── PassValidityMonitor.java# Asynchronous background thread
│   │
│   ├── gui/
│   │   ├── MainFrame.java          # Main window & navigation sidebar
│   │   ├── PassengerPanel.java     # Passenger registration UI
│   │   ├── RoutePanel.java         # Route catalog UI
│   │   ├── PassPanel.java          # Issue / Renew / Cancel UI
│   │   ├── SearchPanel.java        # Collection search UI
│   │   └── ExpiryPanel.java        # Expiry audit & monitor UI
│   │
│   ├── Main.java                   # Main Entry Point
│   └── TestRunner.java             # Automated verification suite
│
├── lib/
│   ├── sqlite-jdbc.jar
│   ├── slf4j-api.jar
│   └── slf4j-simple.jar
│
├── database/
│   └── buspass.db                  # Auto-created SQLite database file
│
├── run.bat                         # Windows Batch launcher
├── run.ps1                         # PowerShell launcher
├── run.sh                          # Linux / macOS shell launcher
└── README.md
```

---

## 🚀 How to Compile and Run

### Option 1: Using One-Click Scripts

- **Windows (Command Prompt)**:
  ```cmd
  run.bat
  ```
- **Windows (PowerShell)**:
  ```powershell
  .\run.ps1
  ```
- **Linux / macOS**:
  ```bash
  chmod +x run.sh
  ./run.sh
  ```

### Option 2: Manual Compilation via Command Line

1. **Compile all Java source files into `bin/` directory**:
   - **Windows**:
     ```cmd
     if not exist bin mkdir bin
     javac -cp "lib/*;src" -d bin src/model/*.java src/exception/*.java src/dao/*.java src/service/*.java src/util/*.java src/monitor/*.java src/gui/*.java src/Main.java
     ```
   - **Linux / macOS**:
     ```bash
     mkdir -p bin
     javac -cp "lib/*:src" -d bin src/model/*.java src/exception/*.java src/dao/*.java src/service/*.java src/util/*.java src/monitor/*.java src/gui/*.java src/Main.java
     ```

2. **Run the Application**:
   - **Windows**:
     ```cmd
     java -cp "bin;lib/*" Main
     ```
   - **Linux / macOS**:
     ```bash
     java -cp "bin:lib/*" Main
     ```

3. **Run Automated Test Suite**:
   ```cmd
   javac -cp "lib/*;src" -d bin src/TestRunner.java
   java -cp "bin;lib/*" TestRunner
   ```

---

## 🎓 Academic OOP Highlights

1. **Encapsulation**: Private fields across all entity models with public getters, setters, and validation bounds.
2. **Abstraction**: `Passenger` abstract class defining contract method `calculatePassFee(...)`.
3. **Inheritance**: `Student extends Passenger` and `Faculty extends Passenger`.
4. **Polymorphism**: Dynamic method dispatch invoking `calculatePassFee` on `Passenger` base references.
5. **Collections & Generics**: Type-safe `HashMap<String, BusRoute>` and `ArrayList<BusRoute>`.
6. **Iterator**: Explicit `Iterator<BusRoute>` traversal in search query evaluation.
7. **Custom Exceptions**: Handled `InvalidRouteException` and `PassValidityExpiredException`.
8. **Concurrency**: Dedicated `PassValidityMonitor` background daemon thread.
