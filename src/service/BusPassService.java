package service;

import dao.BusPassDAO;
import dao.BusRouteDAO;
import dao.PassengerDAO;
import exception.InvalidRouteException;
import exception.PassValidityExpiredException;
import model.BusPass;
import model.BusRoute;
import model.Passenger;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

/**
 * Service orchestrating Bus Pass operations:
 * - Pass Issuance with Polymorphic Fee Calculation
 * - Custom Exceptions (InvalidRouteException, PassValidityExpiredException)
 * - Pass Renewal & Validity Extension
 * - Pass Cancellation
 * - Expiration Monitoring
 */
public class BusPassService {
    private final BusPassDAO passDAO;
    private final PassengerDAO passengerDAO;
    private final BusRouteDAO routeDAO;

    public BusPassService() {
        this.passDAO = new BusPassDAO();
        this.passengerDAO = new PassengerDAO();
        this.routeDAO = new BusRouteDAO();
    }

    public BusPassService(BusPassDAO passDAO, PassengerDAO passengerDAO, BusRouteDAO routeDAO) {
        this.passDAO = passDAO;
        this.passengerDAO = passengerDAO;
        this.routeDAO = routeDAO;
    }

    /**
     * Issues a new Bus Pass for a passenger on a specified route.
     * Demonstrates:
     * - Polymorphism (passenger.calculatePassFee)
     * - User-defined InvalidRouteException
     * - Auto date & fee calculation
     */
    public BusPass issuePass(String passengerId, String routeNumber, String passType) 
            throws IllegalArgumentException, InvalidRouteException, SQLException {

        // 1. Input Validation
        if (passengerId == null || passengerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Passenger ID cannot be empty.");
        }
        if (routeNumber == null || routeNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Route Number cannot be empty.");
        }
        if (passType == null || (!passType.equalsIgnoreCase("MONTHLY") && !passType.equalsIgnoreCase("SEMESTER"))) {
            throw new IllegalArgumentException("Pass type must be either 'MONTHLY' or 'SEMESTER'.");
        }

        String pId = passengerId.trim().toUpperCase();
        String rNum = routeNumber.trim().toUpperCase();
        String pType = passType.trim().toUpperCase();

        // 2. Verify Passenger Existence
        Passenger passenger = passengerDAO.getPassengerById(pId);
        if (passenger == null) {
            throw new IllegalArgumentException("Passenger with ID '" + pId + "' is not registered.");
        }

        // 3. Verify Route Availability (Throws User-Defined InvalidRouteException)
        BusRoute route = routeDAO.getRouteByNumber(rNum);
        if (route == null) {
            throw new InvalidRouteException("Bus Route '" + rNum + "' does not exist in the transport network.");
        }
        if (!route.isAvailable()) {
            throw new InvalidRouteException("Bus Route '" + rNum + "' is currently suspended/unavailable.");
        }

        // 4. Calculate Fee Polymorphically
        // Reference is Passenger, but concrete instance (Student/Faculty) executes its overridden method
        double calculatedFee = passenger.calculatePassFee(route.getFare(), pType);

        // 5. Calculate Issue & Expiry Dates
        LocalDate issueDate = LocalDate.now();
        LocalDate expiryDate;
        if ("SEMESTER".equalsIgnoreCase(pType)) {
            expiryDate = issueDate.plusDays(180); // 6 Months / 1 Semester
        } else {
            expiryDate = issueDate.plusDays(30);  // 1 Month
        }

        // 6. Generate Unique Pass ID
        String passId = "PASS-" + (1000 + new Random().nextInt(9000));
        while (passDAO.getPassById(passId) != null) {
            passId = "PASS-" + (1000 + new Random().nextInt(9000));
        }

        BusPass newPass = new BusPass(passId, pId, rNum, pType, issueDate, expiryDate, calculatedFee, "ACTIVE");

        // 7. Persist to Database
        boolean inserted = passDAO.insertPass(newPass);
        if (inserted) {
            return newPass;
        } else {
            throw new SQLException("Failed to record pass in database.");
        }
    }

    /**
     * Renews an existing bus pass.
     * Extends validity by 30 or 180 days and recalculates fee.
     * Throws PassValidityExpiredException if pass was cancelled or expired past grace period.
     */
    public BusPass renewPass(String passId, String newPassType) 
            throws IllegalArgumentException, PassValidityExpiredException, InvalidRouteException, SQLException {

        if (passId == null || passId.trim().isEmpty()) {
            throw new IllegalArgumentException("Pass ID cannot be empty.");
        }

        String pId = passId.trim().toUpperCase();
        BusPass pass = passDAO.getPassById(pId);
        if (pass == null) {
            throw new IllegalArgumentException("Pass #" + pId + " not found.");
        }

        if ("CANCELLED".equalsIgnoreCase(pass.getStatus())) {
            throw new PassValidityExpiredException("Cannot renew a CANCELLED pass. A new pass must be issued.");
        }

        Passenger passenger = passengerDAO.getPassengerById(pass.getPassengerId());
        if (passenger == null) {
            throw new IllegalArgumentException("Associated passenger record not found.");
        }

        BusRoute route = routeDAO.getRouteByNumber(pass.getRouteNumber());
        if (route == null || !route.isAvailable()) {
            throw new InvalidRouteException("Cannot renew: Route " + pass.getRouteNumber() + " is inactive.");
        }

        String typeToUse = (newPassType != null && !newPassType.trim().isEmpty()) ? newPassType.trim().toUpperCase() : pass.getPassType();
        double updatedFee = passenger.calculatePassFee(route.getFare(), typeToUse);

        // Extend expiry date from either today or previous expiry (whichever is later)
        LocalDate baseDate = LocalDate.now();
        if (pass.getExpiryDate() != null && pass.getExpiryDate().isAfter(baseDate)) {
            baseDate = pass.getExpiryDate();
        }

        LocalDate newExpiry;
        if ("SEMESTER".equalsIgnoreCase(typeToUse)) {
            newExpiry = baseDate.plusDays(180);
        } else {
            newExpiry = baseDate.plusDays(30);
        }

        pass.setPassType(typeToUse);
        pass.setFee(updatedFee);
        pass.setExpiryDate(newExpiry);
        pass.setStatus("ACTIVE");

        boolean updated = passDAO.updatePass(pass);
        if (updated) {
            return pass;
        } else {
            throw new SQLException("Failed to update renewed pass in database.");
        }
    }

    /**
     * Cancels a bus pass.
     */
    public boolean cancelPass(String passId) throws IllegalArgumentException, SQLException {
        if (passId == null || passId.trim().isEmpty()) {
            throw new IllegalArgumentException("Pass ID cannot be empty.");
        }
        String pId = passId.trim().toUpperCase();
        BusPass pass = passDAO.getPassById(pId);
        if (pass == null) {
            throw new IllegalArgumentException("Pass #" + pId + " not found.");
        }
        return passDAO.cancelPass(pId);
    }

    /**
     * Changes the route of an existing pass.
     */
    public boolean changePassRoute(String passId, String newRouteNumber) 
            throws IllegalArgumentException, InvalidRouteException, SQLException {
        if (passId == null || passId.trim().isEmpty()) {
            throw new IllegalArgumentException("Pass ID cannot be empty.");
        }
        BusPass pass = passDAO.getPassById(passId.trim().toUpperCase());
        if (pass == null) {
            throw new IllegalArgumentException("Pass not found.");
        }

        BusRoute newRoute = routeDAO.getRouteByNumber(newRouteNumber.trim().toUpperCase());
        if (newRoute == null || !newRoute.isAvailable()) {
            throw new InvalidRouteException("Target route is invalid or suspended.");
        }

        Passenger passenger = passengerDAO.getPassengerById(pass.getPassengerId());
        double newFee = passenger.calculatePassFee(newRoute.getFare(), pass.getPassType());

        pass.setRouteNumber(newRoute.getRouteNumber());
        pass.setFee(newFee);
        return passDAO.updatePass(pass);
    }

    public BusPass getPassById(String passId) throws SQLException {
        if (passId == null) return null;
        return passDAO.getPassById(passId.trim().toUpperCase());
    }

    public List<BusPass> getAllPasses() throws SQLException {
        return passDAO.getAllPasses();
    }

    public List<BusPass> getExpiredPasses() throws SQLException {
        return passDAO.getExpiredPasses(LocalDate.now());
    }

    public List<BusPass> getPassesExpiringSoon(int daysAhead) throws SQLException {
        return passDAO.getPassesExpiringSoon(LocalDate.now(), daysAhead);
    }
}
