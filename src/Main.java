import gui.MainFrame;
import monitor.PassValidityMonitor;
import service.BusPassService;
import service.PassengerService;
import service.RouteService;
import util.DatabaseInitializer;
import web.WebServer;

import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;

/**
 * Application Entry Point.
 * 
 * Workflow:
 * 1. Checks and initializes SQLite database schema and sample data.
 * 2. Initializes Service layer dependencies.
 * 3. Starts Localhost Web Server on http://localhost:8080
 * 4. Spawns asynchronous PassValidityMonitor background thread.
 * 5. Launches Java AWT GUI on Event Dispatch Thread (if not headless).
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("==============================================================");
        System.out.println("   COLLEGE BUS PASS MANAGEMENT SYSTEM (AWT & LOCALHOST WEB)   ");
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

        // Step 3: Start Localhost Web Server on Port 8080
        try {
            WebServer webServer = new WebServer(passengerService, routeService, passService);
            webServer.start(8080);
            System.out.println(" Web Portal running at: http://localhost:8080");
        } catch (Exception e) {
            System.err.println("Web Server warning: " + e.getMessage());
        }

        // Step 4: Start Background Monitor Thread (Period: 15 seconds)
        PassValidityMonitor monitor = new PassValidityMonitor(passService, 15000);
        monitor.start();

        // Step 5: Launch AWT Desktop GUI (if display environment available)
        if (!GraphicsEnvironment.isHeadless()) {
            EventQueue.invokeLater(() -> {
                try {
                    MainFrame frame = new MainFrame(passengerService, routeService, passService, monitor);
                    frame.setVisible(true);
                    System.out.println("Desktop AWT GUI successfully launched.");
                } catch (Exception e) {
                    System.err.println("GUI Launch Error: " + e.getMessage());
                }
            });
        } else {
            System.out.println("Running in headless environment. Web server active on http://localhost:8080");
        }
    }
}
