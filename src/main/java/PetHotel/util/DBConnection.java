package PetHotel.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Thông số kết nối 
    private static final String HOST = "localhost";
    private static final String PORT = "1521";
    private static final String SERVICE_NAME = "xe"; 
    private static final String USER = "pethotel"; 
    private static final String PASS = "admin";

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
}