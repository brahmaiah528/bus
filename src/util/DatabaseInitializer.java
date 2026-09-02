package util;

import service.AuthService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initializeDatabase() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = ON;");

            // 1. Create USERS table
            stmt.execute("CREATE TABLE IF NOT EXISTS USERS (" +
                    "username TEXT PRIMARY KEY, " +
                    "password_hash TEXT NOT NULL, " +
                    "full_name TEXT NOT NULL, " +
                    "email TEXT NOT NULL, " +
                    "role TEXT NOT NULL, " +
                    "passenger_id TEXT, " +
                    "created_at TEXT NOT NULL" +
                    ");");

            // 2. Create PASSENGER table
            stmt.execute("CREATE TABLE IF NOT EXISTS PASSENGER (" +
                    "passenger_id TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "phone TEXT NOT NULL, " +
                    "email TEXT NOT NULL, " +
                    "passenger_type TEXT NOT NULL, " +
                    "validity_days INTEGER NOT NULL" +
                    ");");

            // 3. Create BUS_ROUTE table
            stmt.execute("CREATE TABLE IF NOT EXISTS BUS_ROUTE (" +
                    "route_number TEXT PRIMARY KEY, " +
                    "source TEXT NOT NULL, " +
                    "destination TEXT NOT NULL, " +
                    "boarding_point TEXT NOT NULL, " +
                    "fare REAL NOT NULL, " +
                    "available INTEGER NOT NULL DEFAULT 1" +
                    ");");

            // 4. Create BUS_PASS table
            stmt.execute("CREATE TABLE IF NOT EXISTS BUS_PASS (" +
                    "pass_id TEXT PRIMARY KEY, " +
                    "passenger_id TEXT NOT NULL, " +
                    "route_number TEXT NOT NULL, " +
                    "pass_type TEXT NOT NULL, " +
                    "issue_date TEXT NOT NULL, " +
                    "expiry_date TEXT NOT NULL, " +
                    "fee REAL NOT NULL, " +
                    "status TEXT NOT NULL, " +
                    "FOREIGN KEY(passenger_id) REFERENCES PASSENGER(passenger_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(route_number) REFERENCES BUS_ROUTE(route_number) ON DELETE CASCADE" +
                    ");");

            // Seed initial data if tables are empty
            seedInitialData(conn);

            System.out.println("Database tables checked/initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedInitialData(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Check users count
            ResultSet rsUsers = stmt.executeQuery("SELECT COUNT(*) FROM USERS;");
            if (rsUsers.next() && rsUsers.getInt(1) == 0) {
                System.out.println("Seeding initial user accounts...");
                String adminHash = AuthService.hashPassword("admin123");
                String studentHash = AuthService.hashPassword("student123");
                String facultyHash = AuthService.hashPassword("faculty123");
                String now = java.time.LocalDateTime.now().toString();

                stmt.executeUpdate("INSERT INTO USERS (username, password_hash, full_name, email, role, passenger_id, created_at) VALUES " +
                        "('admin', '" + adminHash + "', 'System Administrator', 'admin@college.edu', 'ADMIN', NULL, '" + now + "'), " +
                        "('aarav', '" + studentHash + "', 'Aarav Sharma', 'aarav.sharma@college.edu', 'STUDENT', 'STU101', '" + now + "'), " +
                        "('ramesh', '" + facultyHash + "', 'Dr. Ramesh Kumar', 'dr.ramesh@college.edu', 'FACULTY', 'FAC201', '" + now + "');");
            }

            // Check passenger count
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM PASSENGER;");
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Seeding initial dataset into database...");

                stmt.executeUpdate("INSERT INTO PASSENGER (passenger_id, name, phone, email, passenger_type, validity_days) VALUES " +
                        "('STU101', 'Aarav Sharma', '9876543210', 'aarav.sharma@college.edu', 'STUDENT', 180), " +
                        "('STU102', 'Priya Patel', '9812345678', 'priya.patel@college.edu', 'STUDENT', 30), " +
                        "('STU103', 'Rohan Verma', '9988776655', 'rohan.verma@college.edu', 'STUDENT', 180), " +
                        "('FAC201', 'Dr. Ramesh Kumar', '9123456780', 'dr.ramesh@college.edu', 'FACULTY', 180), " +
                        "('FAC202', 'Prof. Sunita Menon', '9345678901', 'sunita.menon@college.edu', 'FACULTY', 30);");

                stmt.executeUpdate("INSERT INTO BUS_ROUTE (route_number, source, destination, boarding_point, fare, available) VALUES " +
                        "('R-101', 'Central Station', 'College Campus', 'Clock Tower Gate', 25.0, 1), " +
                        "('R-102', 'Tech Park Circle', 'College Campus', 'North Gate Metro', 30.0, 1), " +
                        "('R-103', 'Green Valley Residency', 'College Campus', 'Main Arch', 20.0, 1), " +
                        "('R-104', 'Airport Junction', 'College Campus', 'Highway Tollway', 45.0, 1), " +
                        "('R-105', 'East Hill Colony', 'College Campus', 'Cross Road 4', 28.0, 0);");

                stmt.executeUpdate("INSERT INTO BUS_PASS (pass_id, passenger_id, route_number, pass_type, issue_date, expiry_date, fee, status) VALUES " +
                        "('PASS-1001', 'STU101', 'R-101', 'SEMESTER', '2026-07-01', '2026-12-31', 1800.0, 'ACTIVE'), " +
                        "('PASS-1002', 'STU102', 'R-102', 'MONTHLY', '2026-08-05', '2026-09-04', 528.0, 'ACTIVE'), " +
                        "('PASS-1003', 'FAC201', 'R-101', 'SEMESTER', '2026-07-01', '2026-12-31', 2475.0, 'ACTIVE'), " +
                        "('PASS-1004', 'STU103', 'R-103', 'MONTHLY', '2026-01-01', '2026-01-31', 352.0, 'EXPIRED'), " +
                        "('PASS-1005', 'FAC202', 'R-104', 'MONTHLY', '2026-08-08', '2026-09-07', 1012.5, 'ACTIVE');");

                System.out.println("Initial sample dataset seeded successfully.");
            }
        }
    }
}
