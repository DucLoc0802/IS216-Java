package PetHotel.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Thông số kết nối
    private static final String HOST = "localhost";
    private static final String PORT = "1521";
    private static final String SERVICE_NAME = "freepdb1";
    private static final String USER = "pet_hotel";
    private static final String PASS = "123456";

    // Chuỗi URL kết nối chuẩn Oracle
    private static final String URL = "jdbc:oracle:thin:@//" + HOST + ":" + PORT + "/" + SERVICE_NAME;

    public static Connection getConnection() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");

            // 2. Tạo kết nối
            Connection conn = DriverManager.getConnection(URL, USER, PASS);
            return conn;
        } catch (ClassNotFoundException e) {
            System.err.println("Lỗi: Không tìm thấy Driver!");
        } catch (SQLException e) {
            System.err.println("Lỗi: Không thể kết nối. Kiểm tra URL/User/Pass!");
            e.printStackTrace();
        }
        return null;
    }

    public static void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                // Bỏ qua lỗi rollback, không muốn che exception gốc
                System.err.println("[DBConnection] Rollback failed: " + e.getMessage());
            }
        }
    }

    /**
     * Đóng connection an toàn — dùng trong finally block.
     *
     * @param conn connection cần đóng (có thể null)
     */
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