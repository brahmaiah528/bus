package model;

import java.io.Serializable;

/**
 * Abstract base class representing a generic passenger in the College Transport System.
 * Demonstrates Abstraction, Encapsulation, and Polymorphism.
 */
public abstract class Passenger implements Serializable {
    private static final long serialVersionUID = 1L;

    // Encapsulated private fields
    private String passengerId;
    private String name;
    private String phone;
    private String email;
    private String passengerType; // "STUDENT" or "FACULTY"
    private int validityDays;

    /**
     * Default constructor
     */
    public Passenger() {
    }

    /**
     * Parameterized constructor
     * @param passengerId Unique identifier for the passenger (e.g. STU101, FAC201)
     * @param name Full name of the passenger
     * @param phone Contact number
     * @param email Email address
     * @param passengerType Type: STUDENT or FACULTY
     * @param validityDays Standard validity duration (e.g. 30, 180 days)
     */
    public Passenger(String passengerId, String name, String phone, String email, String passengerType, int validityDays) {
        this.passengerId = passengerId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.passengerType = passengerType;
        this.validityDays = validityDays;
    }

    // Abstract method to be overridden by subclasses (Polymorphic behavior)
    /**
     * Calculates the pass fee based on base route fare, duration type (Monthly/Semester),
     * and passenger-specific discounts.
     * 
     * @param baseFare The standard base fare of the selected bus route
     * @param passDuration "MONTHLY" or "SEMESTER"
     * @return Calculated fee amount in INR
     */
    public abstract double calculatePassFee(double baseFare, String passDuration);

    // Getters and Setters (Encapsulation)
    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassengerType() {
        return passengerType;
    }

    public void setPassengerType(String passengerType) {
        this.passengerType = passengerType;
    }

    public int getValidityDays() {
        return validityDays;
    }

    public void setValidityDays(int validityDays) {
        this.validityDays = validityDays;
    }

    @Override
    public String toString() {
        return "Passenger [ID=" + passengerId + ", Name=" + name + ", Type=" + passengerType + 
               ", Phone=" + phone + ", Email=" + email + ", ValidityDays=" + validityDays + "]";
    }
}
