package PetHotel.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String HOST = "localhost";
    private static final String PORT = "1521";
    private static final String SERVICE_NAME = "orcldb"; 
    private static final String USER = "pethotel"; 
    private static final String PASS = "admin";

    // Chuỗi URL kết nối Oracle dùng SID (orcldb là SID)
    private static final String URL = "jdbc:oracle:thin:@" + HOST + ":" + PORT + "/" + SERVICE_NAME;
    
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Không tìm thấy Oracle JDBC Driver.", e);
        }

        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASS);
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Không tạo được kết nối Oracle.");
            }
            return conn;
        } catch (SQLException e) {
            throw new SQLException(
                    "Không kết nối được database. Vui lòng kiểm tra Oracle service, JDBC URL, username/password. "
                            + "URL=" + URL + ", user=" + USER + ". " + e.getMessage(),
                    e);
        }
    }

    public static boolean testConnection() throws SQLException {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        }
    }

    public static void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                System.err.println("[DBConnection] Rollback failed: " + e.getMessage());
            }
        }
    }

    public static void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("[DBConnection] Close failed: " + e.getMessage());
            }
        }
    }

}