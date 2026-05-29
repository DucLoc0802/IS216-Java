package PetHotel.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Thong so ket noi
    private static final String HOST = "localhost";
    private static final String PORT = "1521";
    private static final String SERVICE_NAME = "xe";
    private static final String USER = "pethotel";
    private static final String PASS = "admin";
    private static final String URL = "jdbc:oracle:thin:@//" + HOST + ":" + PORT + "/" + SERVICE_NAME;

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");

            Connection conn = DriverManager.getConnection(URL, USER, PASS);
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Khong tao duoc ket noi Oracle.");
            }
            return conn;
        } catch (ClassNotFoundException e) {
            throw new SQLException("Khong tim thay Oracle JDBC Driver trong classpath.", e);
        } catch (SQLException e) {
            throw new SQLException(
                "Khong ket noi duoc database. Vui long kiem tra Oracle service, JDBC URL, username/password. "
                    + "URL=" + URL + ", user=" + USER + ". " + e.getMessage(),
                e
            );
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


