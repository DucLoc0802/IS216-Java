package PetHotel.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class MainController {

    // Đây là cái khung trống để "thay ruột"
    @FXML
    private StackPane contentArea;

    // --- QUAN TRỌNG: Cơ chế tự động Inject (Bơm) Controller của JavaFX ---
    // Tên biến BẮT BUỘC phải là: [giá trị của fx:id] + "Controller"
    
    @FXML 
    private SidebarController sidebarController; 
    
    @FXML 
    private TopbarController topbarController;

    @FXML
    public void initialize() {
        // Kiểm tra xem JavaFX đã "bơm" thành công các sub-controller vào chưa
        if (sidebarController != null) {
            System.out.println("Đã kết nối Sidebar thành công!");
            // Truyền chính MainController này cho Sidebar, để Sidebar có thể mượn hàm loadView()
            sidebarController.setMainController(this);
        }
        
        if (topbarController != null) {
            System.out.println("Đã kết nối Topbar thành công!");
        }

        // Vừa đăng nhập vào, load ngay trang tổng quan (dashboard-home.fxml)
        loadView("DashboardHome.fxml"); 
    }

    // Hàm "Thay ruột" huyền thoại
    public void loadView(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PetHotel/gui/view/" + fxmlFileName));
            Node view = loader.load();
            
            // Xóa nội dung cũ
            contentArea.getChildren().clear();
            // Đắp nội dung mới vào
            contentArea.getChildren().add(view);
            
        } catch (IOException e) {
            System.err.println("Lỗi không tải được file: " + fxmlFileName);
            e.printStackTrace();
        }
    }
    
    // Hàm hỗ trợ để Topbar đổi dòng chữ Tiêu đề (Ví dụ: "Quản lý khách hàng")
    public TopbarController getTopbarController() {
        return topbarController;
    }
}