package service;

import dao.PassengerDAO;
import dao.UserDAO;
import model.Passenger;
import model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

/**
 * Authentication and User Management Service.
 * Implements password hashing (SHA-256) and registration/login workflows.
 */
public class AuthService {
    private final UserDAO userDAO;
    private final PassengerDAO passengerDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
        this.passengerDAO = new PassengerDAO();
    }

    public AuthService(UserDAO userDAO, PassengerDAO passengerDAO) {
        this.userDAO = userDAO;
        this.passengerDAO = passengerDAO;
    }

    /**
     * Authenticates a user by username and plain-text password.
     * @param username Username (case-insensitive)
     * @param plainPassword Raw password
     * @return User object if authentication succeeds, null otherwise
     * @throws SQLException on database error
     */
    public User login(String username, String plainPassword) throws SQLException, IllegalArgumentException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        String uname = username.trim().toLowerCase();
        User user = userDAO.getUserByUsername(uname);
        if (user == null) {
            return null; // User not found
        }

        String computedHash = hashPassword(plainPassword);
        if (computedHash.equals(user.getPasswordHash())) {
            return user; // Success
        }
        return null; // Invalid password
    }

    /**
     * Registers a new User account.
     */
    public User register(String username, String plainPassword, String fullName, String email, String role, String passengerId) 
            throws IllegalArgumentException, SQLException {
        
        if (username == null || username.trim().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters.");
        }
        if (plainPassword == null || plainPassword.length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters.");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full Name cannot be empty.");
        }
        if (email == null || !email.trim().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Please provide a valid email address.");
        }

        String uname = username.trim().toLowerCase();
        if (userDAO.getUserByUsername(uname) != null) {
            throw new IllegalArgumentException("Username '" + uname + "' is already taken.");
        }

        String r = (role != null && !role.trim().isEmpty()) ? role.trim().toUpperCase() : "STUDENT";
        String pHash = hashPassword(plainPassword);

        User newUser = new User(uname, pHash, fullName.trim(), email.trim(), r, passengerId);
        boolean ok = userDAO.insertUser(newUser);
        if (ok) {
            return newUser;
        } else {
            throw new SQLException("Failed to save user account in database.");
        }
    }

    public static String hashPassword(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm missing", e);
        }
    }
}
