package dao;

import model.BusPass;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for BUS_PASS entity.
 * Supports CRUD, renewals, route modifications, and expiry monitoring queries.
 */
public class BusPassDAO {

    /**
     * Inserts a new bus pass.
     * @param pass BusPass instance
     * @return true if inserted
     * @throws SQLException on database error
     */
    public boolean insertPass(BusPass pass) throws SQLException {
        String sql = "INSERT INTO BUS_PASS (pass_id, passenger_id, route_number, pass_type, issue_date, expiry_date, fee, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pass.getPassId());
            ps.setString(2, pass.getPassengerId());
            ps.setString(3, pass.getRouteNumber());
            ps.setString(4, pass.getPassType());
            ps.setString(5, pass.getIssueDate().toString());
            ps.setString(6, pass.getExpiryDate().toString());
            ps.setDouble(7, pass.getFee());
            ps.setString(8, pass.getStatus());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves a bus pass by unique Pass ID.
     * @param passId Pass identifier
     * @return BusPass instance or null
     * @throws SQLException on database error
     */
    public BusPass getPassById(String passId) throws SQLException {
        String sql = "SELECT pass_id, passenger_id, route_number, pass_type, issue_date, expiry_date, fee, status FROM BUS_PASS WHERE pass_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractPassFromResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves the latest active bus pass for a specific passenger ID.
     * @param passengerId Passenger identifier
     * @return BusPass instance or null
     * @throws SQLException on database error
     */
    public BusPass getActivePassByPassengerId(String passengerId) throws SQLException {
        String sql = "SELECT pass_id, passenger_id, route_number, pass_type, issue_date, expiry_date, fee, status " +
                     "FROM BUS_PASS WHERE passenger_id = ? AND status = 'ACTIVE' ORDER BY expiry_date DESC LIMIT 1;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passengerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractPassFromResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all passes in the system.
     * @return List of BusPass
     * @throws SQLException on database error
     */
    public List<BusPass> getAllPasses() throws SQLException {
        List<BusPass> list = new ArrayList<>();
        String sql = "SELECT pass_id, passenger_id, route_number, pass_type, issue_date, expiry_date, fee, status FROM BUS_PASS ORDER BY issue_date DESC;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extractPassFromResultSet(rs));
            }
        }
        return list;
    }

    /**
     * Updates an existing bus pass (e.g. renewal, route change, status update).
     * @param pass Updated BusPass
     * @return true if updated
     * @throws SQLException on database error
     */
    public boolean updatePass(BusPass pass) throws SQLException {
        String sql = "UPDATE BUS_PASS SET route_number = ?, pass_type = ?, issue_date = ?, expiry_date = ?, fee = ?, status = ? " +
                     "WHERE pass_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pass.getRouteNumber());
            ps.setString(2, pass.getPassType());
            ps.setString(3, pass.getIssueDate().toString());
            ps.setString(4, pass.getExpiryDate().toString());
            ps.setDouble(5, pass.getFee());
            ps.setString(6, pass.getStatus());
            ps.setString(7, pass.getPassId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cancels a pass by setting its status to CANCELLED.
     * @param passId Pass identifier
     * @return true if cancelled
     * @throws SQLException on database error
     */
    public boolean cancelPass(String passId) throws SQLException {
        String sql = "UPDATE BUS_PASS SET status = 'CANCELLED' WHERE pass_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Hard deletes a pass from database.
     * @param passId Pass identifier
     * @return true if deleted
     * @throws SQLException on database error
     */
    public boolean deletePass(String passId) throws SQLException {
        String sql = "DELETE FROM BUS_PASS WHERE pass_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves all expired passes (expiry_date < currentDate OR status = 'EXPIRED').
     * @param currentDate Reference date
     * @return List of expired BusPass objects
     * @throws SQLException on database error
     */
    public List<BusPass> getExpiredPasses(LocalDate currentDate) throws SQLException {
        List<BusPass> list = new ArrayList<>();
        String sql = "SELECT pass_id, passenger_id, route_number, pass_type, issue_date, expiry_date, fee, status " +
                     "FROM BUS_PASS WHERE expiry_date < ? OR status = 'EXPIRED' ORDER BY expiry_date ASC;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currentDate.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractPassFromResultSet(rs));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves passes expiring within the specified number of days (e.g. within 7 days from currentDate).
     * @param currentDate Current date
     * @param daysAhead Window in days
     * @return List of expiring BusPass objects
     * @throws SQLException on database error
     */
    public List<BusPass> getPassesExpiringSoon(LocalDate currentDate, int daysAhead) throws SQLException {
        List<BusPass> list = new ArrayList<>();
        LocalDate thresholdDate = currentDate.plusDays(daysAhead);
        String sql = "SELECT pass_id, passenger_id, route_number, pass_type, issue_date, expiry_date, fee, status " +
                     "FROM BUS_PASS WHERE expiry_date >= ? AND expiry_date <= ? AND status = 'ACTIVE' ORDER BY expiry_date ASC;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currentDate.toString());
            ps.setString(2, thresholdDate.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractPassFromResultSet(rs));
                }
            }
        }
        return list;
    }

    private BusPass extractPassFromResultSet(ResultSet rs) throws SQLException {
        return new BusPass(
                rs.getString("pass_id"),
                rs.getString("passenger_id"),
                rs.getString("route_number"),
                rs.getString("pass_type"),
                LocalDate.parse(rs.getString("issue_date")),
                LocalDate.parse(rs.getString("expiry_date")),
                rs.getDouble("fee"),
                rs.getString("status")
        );
    }
}
