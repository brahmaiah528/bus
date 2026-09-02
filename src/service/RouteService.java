package service;

import dao.BusRouteDAO;
import model.BusRoute;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Service class for Bus Route management.
 * Maintains an in-memory route catalogue (HashMap and ArrayList) to demonstrate:
 * - Collections Framework
 * - Generics
 * - Explicit Iterator traversal
 * - Case-insensitive multi-field search
 */
public class RouteService {
    private final BusRouteDAO routeDAO;
    // In-memory cache using Java Collections with Generics
    private final Map<String, BusRoute> routeMap;
    private final List<BusRoute> routeList;

    public RouteService() {
        this.routeDAO = new BusRouteDAO();
        this.routeMap = new HashMap<>();
        this.routeList = new ArrayList<>();
        refreshCache();
    }

    public RouteService(BusRouteDAO routeDAO) {
        this.routeDAO = routeDAO;
        this.routeMap = new HashMap<>();
        this.routeList = new ArrayList<>();
        refreshCache();
    }

    /**
     * Synchronizes in-memory collection cache with the database.
     */
    public synchronized void refreshCache() {
        try {
            List<BusRoute> routesFromDB = routeDAO.getAllRoutes();
            routeMap.clear();
            routeList.clear();
            for (BusRoute r : routesFromDB) {
                routeMap.put(r.getRouteNumber().toUpperCase(), r);
                routeList.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Error refreshing route cache: " + e.getMessage());
        }
    }

    /**
     * Adds a new route to database and in-memory catalogue.
     */
    public boolean addRoute(String routeNumber, String source, String dest, String boarding, double fare, boolean available) 
            throws IllegalArgumentException, SQLException {
        if (routeNumber == null || routeNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Route number cannot be empty.");
        }
        if (source == null || source.trim().isEmpty() || dest == null || dest.trim().isEmpty()) {
            throw new IllegalArgumentException("Source and Destination cannot be empty.");
        }
        if (fare < 0) {
            throw new IllegalArgumentException("Fare amount cannot be negative.");
        }

        String rNum = routeNumber.trim().toUpperCase();
        if (routeDAO.getRouteByNumber(rNum) != null) {
            throw new IllegalArgumentException("Route " + rNum + " already exists.");
        }

        BusRoute route = new BusRoute(rNum, source.trim(), dest.trim(), boarding.trim(), fare, available);
        boolean success = routeDAO.insertRoute(route);
        if (success) {
            refreshCache();
        }
        return success;
    }

    /**
     * Case-insensitive route search using explicit Java Iterator and Generics.
     * Searches across Route Number, Destination, and Boarding Point.
     * 
     * @param query Search query string
     * @return List of matching BusRoute instances
     */
    public List<BusRoute> searchRoutes(String query) {
        List<BusRoute> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(routeList);
        }

        String q = query.trim().toLowerCase();

        // Explicit demonstration of Iterator<BusRoute> on Collection
        Iterator<BusRoute> iterator = routeList.iterator();
        while (iterator.hasNext()) {
            BusRoute route = iterator.next();
            if (route != null) {
                boolean matchesRouteNumber = route.getRouteNumber() != null && route.getRouteNumber().toLowerCase().contains(q);
                boolean matchesDestination = route.getDestination() != null && route.getDestination().toLowerCase().contains(q);
                boolean matchesBoarding = route.getBoardingPoint() != null && route.getBoardingPoint().toLowerCase().contains(q);
                boolean matchesSource = route.getSource() != null && route.getSource().toLowerCase().contains(q);

                if (matchesRouteNumber || matchesDestination || matchesBoarding || matchesSource) {
                    results.add(route);
                }
            }
        }
        return results;
    }

    public BusRoute getRouteByNumber(String routeNumber) {
        if (routeNumber == null) return null;
        return routeMap.get(routeNumber.trim().toUpperCase());
    }

    public List<BusRoute> getAllRoutes() {
        return new ArrayList<>(routeList);
    }

    public boolean updateRoute(BusRoute route) throws SQLException {
        if (route == null) return false;
        boolean ok = routeDAO.updateRoute(route);
        if (ok) refreshCache();
        return ok;
    }

    public boolean deleteRoute(String routeNumber) throws SQLException {
        if (routeNumber == null) return false;
        boolean ok = routeDAO.deleteRoute(routeNumber.trim().toUpperCase());
        if (ok) refreshCache();
        return ok;
    }
}
