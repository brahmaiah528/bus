package dao;

import model.BusRoute;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for BUS_ROUTE entity.
 * Implements CRUD operations for routes.
 */
public class BusRouteDAO {

    /**
     * Inserts a new bus route.
     * @param route BusRoute instance
     * @return true if successful
     * @throws SQLException on database error
     */
    public boolean insertRoute(BusRoute route) throws SQLException {
        String sql = "INSERT INTO BUS_ROUTE (route_number, source, destination, boarding_point, fare, available) VALUES (?, ?, ?, ?, ?, ?);";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, route.getRouteNumber());
            ps.setString(2, route.getSource());
            ps.setString(3, route.getDestination());
            ps.setString(4, route.getBoardingPoint());
            ps.setDouble(5, route.getFare());
            ps.setInt(6, route.isAvailable() ? 1 : 0);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves a route by route number.
     * @param routeNumber Route identifier
     * @return BusRoute instance or null
     * @throws SQLException on database error
     */
    public BusRoute getRouteByNumber(String routeNumber) throws SQLException {
        String sql = "SELECT route_number, source, destination, boarding_point, fare, available FROM BUS_ROUTE WHERE route_number = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, routeNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractRouteFromResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all bus routes.
     * @return List of BusRoute
     * @throws SQLException on database error
     */
    public List<BusRoute> getAllRoutes() throws SQLException {
        List<BusRoute> list = new ArrayList<>();
        String sql = "SELECT route_number, source, destination, boarding_point, fare, available FROM BUS_ROUTE ORDER BY route_number ASC;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extractRouteFromResultSet(rs));
            }
        }
        return list;
    }

    /**
     * Updates an existing bus route.
     * @param route Updated route details
     * @return true if updated
     * @throws SQLException on database error
     */
    public boolean updateRoute(BusRoute route) throws SQLException {
        String sql = "UPDATE BUS_ROUTE SET source = ?, destination = ?, boarding_point = ?, fare = ?, available = ? WHERE route_number = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, route.getSource());
            ps.setString(2, route.getDestination());
            ps.setString(3, route.getBoardingPoint());
            ps.setDouble(4, route.getFare());
            ps.setInt(5, route.isAvailable() ? 1 : 0);
            ps.setString(6, route.getRouteNumber());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a bus route by route number.
     * @param routeNumber Route number to delete
     * @return true if deleted
     * @throws SQLException on database error
     */
    public boolean deleteRoute(String routeNumber) throws SQLException {
        String sql = "DELETE FROM BUS_ROUTE WHERE route_number = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, routeNumber);
            return ps.executeUpdate() > 0;
        }
    }

    private BusRoute extractRouteFromResultSet(ResultSet rs) throws SQLException {
        return new BusRoute(
                rs.getString("route_number"),
                rs.getString("source"),
                rs.getString("destination"),
                rs.getString("boarding_point"),
                rs.getDouble("fare"),
                rs.getInt("available") == 1
        );
    }
}
