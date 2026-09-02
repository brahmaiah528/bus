package dao;

import model.Faculty;
import model.Passenger;
import model.Student;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for PASSENGER entity.
 * Implements CRUD operations using JDBC PreparedStatement and try-with-resources.
 */
public class PassengerDAO {

    /**
     * Inserts a new passenger (Student or Faculty) into the database.
     * @param passenger Passenger instance
     * @return true if insertion succeeds
     * @throws SQLException on database error
     */
    public boolean insertPassenger(Passenger passenger) throws SQLException {
        String sql = "INSERT INTO PASSENGER (passenger_id, name, phone, email, passenger_type, validity_days) VALUES (?, ?, ?, ?, ?, ?);";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passenger.getPassengerId());
            ps.setString(2, passenger.getName());
            ps.setString(3, passenger.getPhone());
            ps.setString(4, passenger.getEmail());
            ps.setString(5, passenger.getPassengerType());
            ps.setInt(6, passenger.getValidityDays());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves a passenger by unique ID.
     * Returns polymorphic Student or Faculty instance depending on stored type.
     * @param passengerId ID of the passenger
     * @return Passenger instance or null if not found
     * @throws SQLException on database error
     */
    public Passenger getPassengerById(String passengerId) throws SQLException {
        String sql = "SELECT passenger_id, name, phone, email, passenger_type, validity_days FROM PASSENGER WHERE passenger_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passengerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractPassengerFromResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all registered passengers from the database.
     * @return List of Passenger objects
     * @throws SQLException on database error
     */
    public List<Passenger> getAllPassengers() throws SQLException {
        List<Passenger> list = new ArrayList<>();
        String sql = "SELECT passenger_id, name, phone, email, passenger_type, validity_days FROM PASSENGER ORDER BY passenger_id ASC;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extractPassengerFromResultSet(rs));
            }
        }
        return list;
    }

    /**
     * Updates an existing passenger record.
     * @param passenger Updated passenger data
     * @return true if record updated
     * @throws SQLException on database error
     */
    public boolean updatePassenger(Passenger passenger) throws SQLException {
        String sql = "UPDATE PASSENGER SET name = ?, phone = ?, email = ?, passenger_type = ?, validity_days = ? WHERE passenger_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passenger.getName());
            ps.setString(2, passenger.getPhone());
            ps.setString(3, passenger.getEmail());
            ps.setString(4, passenger.getPassengerType());
            ps.setInt(5, passenger.getValidityDays());
            ps.setString(6, passenger.getPassengerId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a passenger from the database.
     * @param passengerId ID to delete
     * @return true if deleted
     * @throws SQLException on database error
     */
    public boolean deletePassenger(String passengerId) throws SQLException {
        String sql = "DELETE FROM PASSENGER WHERE passenger_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passengerId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Helper factory method to map database record to appropriate polymorphic subclass.
     */
    private Passenger extractPassengerFromResultSet(ResultSet rs) throws SQLException {
        String id = rs.getString("passenger_id");
        String name = rs.getString("name");
        String phone = rs.getString("phone");
        String email = rs.getString("email");
        String type = rs.getString("passenger_type");
        int validityDays = rs.getInt("validity_days");

        if ("FACULTY".equalsIgnoreCase(type)) {
            return new Faculty(id, name, phone, email, validityDays);
        } else {
            return new Student(id, name, phone, email, validityDays);
        }
    }
}
