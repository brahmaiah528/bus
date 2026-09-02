package model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Model representing an issued Bus Pass.
 */
public class BusPass implements Serializable {
    private static final long serialVersionUID = 1L;

    private String passId;
    private String passengerId;
    private String routeNumber;
    private String passType;     // "MONTHLY" or "SEMESTER"
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private double fee;
    private String status;       // "ACTIVE", "EXPIRED", "CANCELLED", "RENEWED"

    public BusPass() {
    }

    public BusPass(String passId, String passengerId, String routeNumber, String passType,
                   LocalDate issueDate, LocalDate expiryDate, double fee, String status) {
        this.passId = passId;
        this.passengerId = passengerId;
        this.routeNumber = routeNumber;
        this.passType = passType;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.fee = fee;
        this.status = status;
    }

    public String getPassId() {
        return passId;
    }

    public void setPassId(String passId) {
        this.passId = passId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public String getRouteNumber() {
        return routeNumber;
    }

    public void setRouteNumber(String routeNumber) {
        this.routeNumber = routeNumber;
    }

    public String getPassType() {
        return passType;
    }

    public void setPassType(String passType) {
        this.passType = passType;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isExpired() {
        if (expiryDate == null) return false;
        return LocalDate.now().isAfter(expiryDate);
    }

    @Override
    public String toString() {
        return String.format("Pass #%s | Passenger: %s | Route: %s | Type: %s | Fee: Rs. %.2f | Issued: %s | Expires: %s | Status: %s",
                passId, passengerId, routeNumber, passType, fee, issueDate, expiryDate, status);
    }
}
