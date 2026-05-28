package PetHotel.gui.controller;

import PetHotel.bus.AuthBUS;
import PetHotel.model.AppUser;
import PetHotel.util.Role;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class MainController {
    private static MainController activeInstance;

    // ÄÃ¢y lÃ  cÃ¡i khung trá»‘ng Ä‘á»ƒ "thay ruá»™t"
    @FXML
    private StackPane contentArea;

    // --- QUAN TRá»ŒNG: CÆ¡ cháº¿ tá»± Ä‘á»™ng Inject (BÆ¡m) Controller cá»§a JavaFX ---
    // TÃªn biáº¿n Báº®T BUá»˜C pháº£i lÃ : [giÃ¡ trá»‹ cá»§a fx:id] + "Controller"
    
    @FXML 
    private SidebarController sidebarController; 
    
    @FXML 
    private TopbarController topbarController;

    @FXML
    public void initialize() {
        activeInstance = this;
        // Kiá»ƒm tra xem JavaFX Ä‘Ã£ "bÆ¡m" thÃ nh cÃ´ng cÃ¡c sub-controller vÃ o chÆ°a
        if (sidebarController != null) {
            System.out.println("ÄÃ£ káº¿t ná»‘i Sidebar thÃ nh cÃ´ng!");
            // Truyá»n chÃ­nh MainController nÃ y cho Sidebar, Ä‘á»ƒ Sidebar cÃ³ thá»ƒ mÆ°á»£n hÃ m loadView()
            sidebarController.setMainController(this);
            // Truyá»n AuthBUS tá»« SessionManager (Ä‘Ã£ Ä‘Æ°á»£c LoginController khá»Ÿi táº¡o) cho Sidebar
            AuthBUS sharedAuthBUS = SessionManager.getInstance().getAuthBUS();
            if (sharedAuthBUS != null) {
                sidebarController.setAuthBUS(sharedAuthBUS);
            }
        }
        
        if (topbarController != null) {
            System.out.println("ÄÃ£ káº¿t ná»‘i Topbar thÃ nh cÃ´ng!");
        }

        // Vá»«a Ä‘Äƒng nháº­p vÃ o, load mÃ n hÃ¬nh máº·c Ä‘á»‹nh theo role.
        AppUser currentUser = SessionManager.getInstance().getCurrentUser();
        loadDefaultViewByRole(currentUser);
    }

    private void loadDefaultViewByRole(AppUser currentUser) {
        if (currentUser == null || currentUser.getRole() == null) {
            loadView("DashboardHome.fxml");
            return;
        }

        Role role = currentUser.getRole();
        switch (role) {
            case RECEPTIONIST:
                loadView("InvoiceManagement.fxml");
                setTopbarTitle("HÃ³a ÄÆ¡n", "Quáº£n lÃ½ hÃ³a Ä‘Æ¡n");
                break;
            case PET_CARE_STAFF:
                showPetManagement(null);
                break;
            case ADMIN:
                loadView("AccountManagement.fxml");
                setTopbarTitle("TÃ i Khoáº£n", "Quáº£n lÃ½ tÃ i khoáº£n");
                break;
            case CEO:
            case BRANCH_MANAGER:
            default:
                loadView("DashboardHome.fxml");
                setTopbarTitle("Dashboard", "Trang chá»§");
                break;
        }
    }

    private void setTopbarTitle(String title, String subtitle) {
        if (topbarController != null) {
            topbarController.setTitle(title, subtitle);
        }
    }

    // HÃ m "Thay ruá»™t" huyá»n thoáº¡i
    public void loadView(String fxmlFileName) {
        try {
            System.out.println("Äang táº£i view: " + fxmlFileName);

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/PetHotel/gui/view/" + fxmlFileName)
            );

            Node view = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

            System.out.println("Táº£i view thÃ nh cÃ´ng: " + fxmlFileName);

        } catch (Exception e) {
            System.err.println("Lá»—i khÃ´ng táº£i Ä‘Æ°á»£c file: " + fxmlFileName);
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lá»—i táº£i giao diá»‡n");
            alert.setHeaderText("KhÃ´ng thá»ƒ má»Ÿ " + fxmlFileName);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    
    // HÃ m há»— trá»£ Ä‘á»ƒ Topbar Ä‘á»•i dÃ²ng chá»¯ TiÃªu Ä‘á» (VÃ­ dá»¥: "Quáº£n lÃ½ khÃ¡ch hÃ ng")
    public void showPetManagement(String selectedPetId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PetHotel/gui/view/PetManagement.fxml"));
            Node view = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

            if (topbarController != null) {
                topbarController.setTitle("ThÃƒÂº CÃ†Â°ng", "QuÃ¡ÂºÂ£n lÃƒÂ½ hÃ¡Â»â€œ sÃ†Â¡ thÃƒÂº cÃ†Â°ng");
            }
            if (topbarController != null) {
                topbarController.setTitle("ThÃº CÆ°ng", "Danh sÃ¡ch thÃº cÆ°ng táº¡i chi nhÃ¡nh");
            }
            if (sidebarController != null) {
                sidebarController.setActivePetMenu();
            }

            PetController controller = loader.getController();
            if (selectedPetId != null && !selectedPetId.isBlank()) {
                controller.selectAndOpenPet(selectedPetId);
            }
        } catch (IOException e) {
            System.err.println("LÃ¡Â»â€”i khÃƒÂ´ng tÃ¡ÂºÂ£i Ã„â€˜Ã†Â°Ã¡Â»Â£c file: PetManagement.fxml");
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
