package PetHotel.gui;

import java.io.IOException;
import java.sql.Connection; // Thêm thư viện Connection

import PetHotel.util.DBConnection; // Import class DBConnection của bạn
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Tải file FXML
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/PetHotel/gui/view/Login.fxml"));        
        // Khởi tạo Scene
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        
        // Cài đặt cửa sổ
        stage.setTitle("PetHotel - Đăng nhập");
        stage.setScene(scene);
        stage.setResizable(false); 
        
        stage.show();
    }

    public static void main(String[] args) {
        // 1. KIỂM TRA KẾT NỐI DATABASE TRƯỚC
        System.out.println("--- Checking ---");
        Connection conn = DBConnection.getConnection();
        
        if (conn != null) {
            System.out.println("Connected Successfully!");
            try {
                conn.close(); // Đóng kết nối test để giải phóng bộ nhớ
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Failed to connect to Database!");
        }

        
        // 2. chạy giao diện
        System.out.println("Ready to run...");
        launch(args);
    }
}