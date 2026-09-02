package util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class to manage SQLite JDBC Database connections.
 * Implements Singleton connection factory pattern with auto-directory creation.
 */
public class DatabaseConnection {
    // Relative path to SQLite database file
    private static final String DB_DIR = "database";
    private static final String DB_FILE = "database" + File.separator + "buspass.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    static {
        try {
            // Explicitly load SQLite JDBC driver class
            Class.forName("org.sqlite.JDBC");
            
            // Ensure parent directory exists
            File dir = new File(DB_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } catch (ClassNotFoundException e) {
            System.err.println("CRITICAL: SQLite JDBC Driver not found in classpath!");
            e.printStackTrace();
        }
    }

    /**
     * Obtains a new JDBC connection to the SQLite database.
     * @return active Connection instance
     * @throws SQLException if database connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Closes a database connection safely if not null and not already closed.
     * @param conn Connection object to close
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
}
