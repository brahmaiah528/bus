package dao;

import model.User;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for USERS entity.
 */
public class UserDAO {

    public boolean insertUser(User user) throws SQLException {
        String sql = "INSERT INTO USERS (username, password_hash, full_name, email, role, passenger_id, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername().toLowerCase());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getRole().toUpperCase());
            ps.setString(6, user.getPassengerId());
            ps.setString(7, user.getCreatedAt() != null ? user.getCreatedAt().toString() : java.time.LocalDateTime.now().toString());
            return ps.executeUpdate() > 0;
        }
    }

    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT username, password_hash, full_name, email, role, passenger_id, created_at FROM USERS WHERE username = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT username, password_hash, full_name, email, role, passenger_id, created_at FROM USERS ORDER BY username ASC;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extractUserFromResultSet(rs));
            }
        }
        return list;
    }

    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setRole(rs.getString("role"));
        u.setPassengerId(rs.getString("passenger_id"));
        String dt = rs.getString("created_at");
        if (dt != null) {
            try {
                u.setCreatedAt(java.time.LocalDateTime.parse(dt));
            } catch (Exception ignored) {
                u.setCreatedAt(java.time.LocalDateTime.now());
            }
        }
        return u;
    }
}
