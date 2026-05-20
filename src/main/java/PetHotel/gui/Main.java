package PetHotel.gui;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import PetHotel.util.DBConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/PetHotel/gui/view/Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);

        stage.setTitle("PetHotel - Đăng nhập");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        System.out.println("--- Checking ---");
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("Connected Successfully!");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        System.out.println("Ready to run...");
        launch(args);
    }
}