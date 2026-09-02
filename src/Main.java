import gui.MainFrame;
import monitor.PassValidityMonitor;
import service.BusPassService;
import service.PassengerService;
import service.RouteService;
import util.DatabaseInitializer;

import java.awt.EventQueue;

/**
 * Application Entry Point.
 * 
 * Workflow:
 * 1. Checks and initializes SQLite database schema and sample data.
 * 2. Initializes Service layer dependencies.
 * 3. Spawns asynchronous PassValidityMonitor background thread.
 * 4. Launches Java AWT GUI on the Event Dispatch Thread (EDT).
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("==============================================================");
        System.out.println("   COLLEGE BUS PASS MANAGEMENT SYSTEM (JAVA AWT & JDBC)       ");
        System.out.println("==============================================================");

        // Step 1: Initialize Database (CREATE TABLE IF NOT EXISTS & Seed sample data)
        try {
            DatabaseInitializer.initializeDatabase();
        } catch (Exception e) {
            System.err.println("Database initialization warning: " + e.getMessage());
        }

        // Step 2: Initialize Services
        PassengerService passengerService = new PassengerService();
        RouteService routeService = new RouteService();
        BusPassService passService = new BusPassService();

        // Step 3: Start Background Monitor Thread (Period: 15 seconds)
        PassValidityMonitor monitor = new PassValidityMonitor(passService, 15000);
        monitor.start();

        // Step 4: Launch GUI on Event Dispatch Thread
        EventQueue.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame(passengerService, routeService, passService, monitor);
                frame.setVisible(true);
                System.out.println("GUI successfully launched.");
            } catch (Exception e) {
                System.err.println("Fatal GUI Error: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
