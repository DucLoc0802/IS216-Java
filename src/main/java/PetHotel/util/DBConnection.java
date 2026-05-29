package PetHotel.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Thông số kết nối
    private static final String HOST = "localhost";
    private static final String PORT = "1521";
    private static final String SERVICE_NAME = "orcl.lan";
    // Sửa 2 dòng dưới đây:
    private static final String USER = "PETHOTEL";
    private static final String PASS = "thienloc2006";
    // Chuỗi URL kết nối chuẩn Oracle
    private static final String URL = "jdbc:oracle:thin:@//" + HOST + ":" + PORT + "/" + SERVICE_NAME;

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");

            // 2. Tạo kết nối
            Connection conn = DriverManager.getConnection(URL, USER, PASS);
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Không tạo được kết nối Oracle.");
            }
            return conn;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Không tìm thấy JDBC Driver Oracle. Vui lòng thêm ojdbc.jar vào classpath.", e);
        } catch (SQLException e) {
            throw new SQLException(
                    "Không kết nối được database. Vui lòng kiểm tra Oracle service, JDBC URL, username/password. "
                            + "URL=" + URL + ", user=" + USER + ". " + e.getMessage(),
                    e);
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
