package model;

import java.io.Serializable;

/**
 * Model representing a college Bus Route.
 */
public class BusRoute implements Serializable {
    private static final long serialVersionUID = 1L;

    private String routeNumber;
    private String source;
    private String destination;
    private String boardingPoint;
    private double fare;
    private boolean available;

    public BusRoute() {
    }

    public BusRoute(String routeNumber, String source, String destination, String boardingPoint, double fare, boolean available) {
        this.routeNumber = routeNumber;
        this.source = source;
        this.destination = destination;
        this.boardingPoint = boardingPoint;
        this.fare = fare;
        this.available = available;
    }

    public String getRouteNumber() {
        return routeNumber;
    }

    public void setRouteNumber(String routeNumber) {
        this.routeNumber = routeNumber;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getBoardingPoint() {
        return boardingPoint;
    }

    public void setBoardingPoint(String boardingPoint) {
        this.boardingPoint = boardingPoint;
    }

    public double getFare() {
        return fare;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s -> %s (Via: %s) | Base Fare: Rs. %.2f | Status: %s",
                routeNumber, source, destination, boardingPoint, fare, (available ? "ACTIVE" : "SUSPENDED"));
    }
}
