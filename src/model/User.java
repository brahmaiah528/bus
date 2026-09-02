package model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Model representing an Authenticated System User.
 * Supports roles: ADMIN, STUDENT, FACULTY.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String role; // "ADMIN", "STUDENT", "FACULTY"
    private String passengerId; // Optional link to PASSENGER(passenger_id)
    private LocalDateTime createdAt;

    public User() {}

    public User(String username, String passwordHash, String fullName, String email, String role, String passengerId) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.passengerId = passengerId;
        this.createdAt = LocalDateTime.now();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return "User [username=" + username + ", fullName=" + fullName + ", role=" + role + ", email=" + email + "]";
    }
}
