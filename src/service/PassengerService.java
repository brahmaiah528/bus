package service;

import dao.PassengerDAO;
import model.Faculty;
import model.Passenger;
import model.Student;

import java.sql.SQLException;
import java.util.List;

/**
 * Service class for handling Passenger registration and business logic.
 */
public class PassengerService {
    private final PassengerDAO passengerDAO;

    public PassengerService() {
        this.passengerDAO = new PassengerDAO();
    }

    public PassengerService(PassengerDAO passengerDAO) {
        this.passengerDAO = passengerDAO;
    }

    /**
     * Registers a new passenger with comprehensive validation.
     * @param id Passenger ID
     * @param name Full name
     * @param phone Phone number
     * @param email Email ID
     * @param type "STUDENT" or "FACULTY"
     * @param validityDays Validity days (e.g. 30 or 180)
     * @return true if registered successfully
     * @throws IllegalArgumentException if validation fails
     * @throws SQLException if database error occurs
     */
    public boolean registerPassenger(String id, String name, String phone, String email, String type, int validityDays) 
            throws IllegalArgumentException, SQLException {
        
        // Validation checks demonstrating built-in IllegalArgumentException
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Passenger ID cannot be empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Passenger Name cannot be empty.");
        }
        if (phone == null || !phone.trim().matches("\\d{10}")) {
            throw new IllegalArgumentException("Phone number must be a valid 10-digit number.");
        }
        if (email == null || !email.trim().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format entered.");
        }
        if (validityDays <= 0) {
            throw new IllegalArgumentException("Validity days must be greater than zero.");
        }

        // Check if passenger already exists
        if (passengerDAO.getPassengerById(id.trim().toUpperCase()) != null) {
            throw new IllegalArgumentException("Passenger with ID '" + id + "' already exists.");
        }

        // Polymorphic instantiation based on type
        Passenger passenger;
        String pId = id.trim().toUpperCase();
        String pName = name.trim();
        String pPhone = phone.trim();
        String pEmail = email.trim();

        if ("FACULTY".equalsIgnoreCase(type)) {
            passenger = new Faculty(pId, pName, pPhone, pEmail, validityDays);
        } else {
            passenger = new Student(pId, pName, pPhone, pEmail, validityDays);
        }

        return passengerDAO.insertPassenger(passenger);
    }

    public Passenger getPassenger(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) return null;
        return passengerDAO.getPassengerById(id.trim().toUpperCase());
    }

    public List<Passenger> getAllPassengers() throws SQLException {
        return passengerDAO.getAllPassengers();
    }

    public boolean updatePassenger(Passenger passenger) throws SQLException {
        if (passenger == null) return false;
        return passengerDAO.updatePassenger(passenger);
    }

    public boolean deletePassenger(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) return false;
        return passengerDAO.deletePassenger(id.trim().toUpperCase());
    }
}
