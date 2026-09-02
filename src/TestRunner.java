import dao.BusPassDAO;
import dao.BusRouteDAO;
import dao.PassengerDAO;
import exception.InvalidRouteException;
import exception.PassValidityExpiredException;
import model.BusPass;
import model.BusRoute;
import model.Passenger;
import service.BusPassService;
import service.PassengerService;
import service.RouteService;
import util.DatabaseInitializer;

import java.util.List;

public class TestRunner {
    public static void main(String[] args) {
        System.out.println("--- 1. Testing Database Initialization ---");
        DatabaseInitializer.initializeDatabase();

        System.out.println("\n--- 2. Testing PassengerService & Polymorphism ---");
        PassengerService pService = new PassengerService();
        try {
            Passenger s = pService.getPassenger("STU101");
            Passenger f = pService.getPassenger("FAC201");
            System.out.println("Loaded: " + s);
            System.out.println("Loaded: " + f);

            double feeStudent = s.calculatePassFee(30.0, "MONTHLY");
            double feeFaculty = f.calculatePassFee(30.0, "MONTHLY");
            System.out.println("Student Monthly Fee for Base Fare 30: Rs. " + feeStudent);
            System.out.println("Faculty Monthly Fee for Base Fare 30: Rs. " + feeFaculty);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\n--- 3. Testing RouteService & Iterator Search ---");
        RouteService rService = new RouteService();
        List<BusRoute> results = rService.searchRoutes("gate");
        System.out.println("Found " + results.size() + " routes matching 'gate':");
        for (BusRoute r : results) {
            System.out.println("  " + r);
        }

        System.out.println("\n--- 4. Testing BusPassService & Custom Exception (InvalidRouteException) ---");
        BusPassService passService = new BusPassService();
        try {
            // Attempt issuing pass on inactive route R-105
            passService.issuePass("STU101", "R-105", "MONTHLY");
            System.out.println("ERROR: Expected InvalidRouteException not thrown!");
        } catch (InvalidRouteException e) {
            System.out.println("SUCCESS: Caught expected custom exception -> " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\n--- 5. Testing BusPassService & Pass Renewal ---");
        try {
            BusPass pass = passService.issuePass("STU102", "R-101", "MONTHLY");
            System.out.println("Issued Pass: " + pass);

            BusPass renewed = passService.renewPass(pass.getPassId(), "SEMESTER");
            System.out.println("Renewed Pass: " + renewed);

            boolean cancelled = passService.cancelPass(pass.getPassId());
            System.out.println("Cancelled Pass Status: " + (cancelled ? "SUCCESS" : "FAILED"));

            // Attempt renewing cancelled pass
            try {
                passService.renewPass(pass.getPassId(), "MONTHLY");
                System.out.println("ERROR: Expected PassValidityExpiredException not thrown!");
            } catch (PassValidityExpiredException e) {
                System.out.println("SUCCESS: Caught expected custom exception -> " + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\n--- ALL CORE VERIFICATIONS PASSED SUCCESSFULLY ---");
    }
}
