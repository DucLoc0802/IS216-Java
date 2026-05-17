package PetHotel.gui.controller;

import PetHotel.bus.AuthBUS;
import PetHotel.model.AppUser;
import PetHotel.util.Role;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;

public class MainController {
    private static MainController activeInstance;

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
        activeInstance = this;
        // Kiểm tra xem JavaFX đã "bơm" thành công các sub-controller vào chưa
        if (sidebarController != null) {
            System.out.println("Đã kết nối Sidebar thành công!");
            // Truyền chính MainController này cho Sidebar, để Sidebar có thể mượn hàm loadView()
            sidebarController.setMainController(this);
            // Truyền AuthBUS từ SessionManager (đã được LoginController khởi tạo) cho Sidebar
            AuthBUS sharedAuthBUS = SessionManager.getInstance().getAuthBUS();
            if (sharedAuthBUS != null) {
                sidebarController.setAuthBUS(sharedAuthBUS);
            }
        }
        
        if (topbarController != null) {
            System.out.println("Đã kết nối Topbar thành công!");
        }

        // Vừa đăng nhập vào, load ngay trang tổng quan (dashboard-home.fxml)
        AppUser currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getRole() == Role.PET_CARE_STAFF) {
            showPetManagement(null);
        } else {
            loadView("DashboardHome.fxml");
        }
    }

    // Hàm "Thay ruột" huyền thoại
    public void loadView(String fxmlFileName) {
        try {
            System.out.println("Đang tải view: " + fxmlFileName);

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/PetHotel/gui/view/" + fxmlFileName)
            );

            Node view = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

            System.out.println("Tải view thành công: " + fxmlFileName);

        } catch (Exception e) {
            System.err.println("Lỗi không tải được file: " + fxmlFileName);
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi tải giao diện");
            alert.setHeaderText("Không thể mở " + fxmlFileName);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    
    // Hàm hỗ trợ để Topbar đổi dòng chữ Tiêu đề (Ví dụ: "Quản lý khách hàng")
    public void showPetManagement(String selectedPetId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PetHotel/gui/view/PetManagement.fxml"));
            Node view = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

            if (topbarController != null) {
                topbarController.setTitle("ThÃº CÆ°ng", "Quáº£n lÃ½ há»“ sÆ¡ thÃº cÆ°ng");
            }
            if (topbarController != null) {
                topbarController.setTitle("Thú Cưng", "Danh sách thú cưng tại chi nhánh");
            }
            if (sidebarController != null) {
                sidebarController.setActivePetMenu();
            }

            PetController controller = loader.getController();
            if (selectedPetId != null && !selectedPetId.isBlank()) {
                controller.selectAndOpenPet(selectedPetId);
            }
        } catch (IOException e) {
            System.err.println("Lá»—i khÃ´ng táº£i Ä‘Æ°á»£c file: PetManagement.fxml");
            e.printStackTrace();
        }
    }

    public static MainController getActiveInstance() {
        return activeInstance;
    }

    public TopbarController getTopbarController() {
        return topbarController;
    }
}
