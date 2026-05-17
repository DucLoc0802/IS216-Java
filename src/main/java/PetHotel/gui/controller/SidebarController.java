package PetHotel.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class SidebarController {

    @FXML private Label avatarLabel;
    @FXML private Label usernameLabel;
    @FXML private Label roleLabel;

    // Biến lưu trữ menu đang được bôi màu để tắt nó đi khi chọn menu khác
    private VBox activeMenuItem;
    
    // Nút Dashboard mặc định được gắn fx:id để tô màu lúc vừa mở app
    @FXML private VBox menuDashboard; 

    // Biến liên kết đến Controller gốc (MainController) để gọi hàm loadView()
    private MainController mainController;

    @FXML
    public void initialize() {
        // Mặc định thông tin user
        usernameLabel.setText("Nguyễn Văn A");
        roleLabel.setText("Admin Hệ Thống");
        avatarLabel.setText("A");

        // Set nút Dashboard làm nút được bôi màu mặc định ban đầu
        activeMenuItem = menuDashboard;
    }

    // Nhận quyền điều khiển từ MainController
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // --- HÀM XỬ LÝ CHUNG CHO TẤT CẢ MENU ---
    @FXML
    public void onMenu(MouseEvent event) {
        // 1. Lấy ra cái nút (VBox) mà người dùng vừa click vào
        Node source = (Node) event.getSource();
        
        if (source instanceof VBox) {
            VBox clickedMenu = (VBox) source;

            // 2. Đổi màu cho nút vừa click (Sáng lên)
            setActive(clickedMenu);

            // 3. Đọc thẻ userData đã gắn trong FXML
            String menuType = (String) clickedMenu.getUserData();

            // 4. Kiểm tra xem là nút nào và Load FXML tương ứng
            if (mainController != null && menuType != null) {
                switch (menuType) {
                    case "dashboard":
                        mainController.loadView("DashboardHome.fxml");
                        mainController.getTopbarController().setTitle("Dashboard", "Trang chủ");
                        break;
                    case "customer":
                        mainController.loadView("CustomerManagement.fxml");
                        mainController.getTopbarController().setTitle("Khách Hàng", "Quản lý khách hàng");
                        break;
                    case "booking":
                        mainController.loadView("BookingManagement.fxml");
                        mainController.getTopbarController().setTitle("Booking", "Quản lý đặt phòng");
                        break;
                    case "room":
                        mainController.loadView("RoomManagement.fxml");
                        mainController.getTopbarController().setTitle("Phòng", "Quản lý phòng");
                        break;
                    case "grooming":
                        mainController.loadView("GroomingManagement.fxml");
                        mainController.getTopbarController().setTitle("Grooming", "Quản lý lịch cắt tỉa");
                        break;
                    case "invoice":
                        mainController.loadView("InvoiceManagement.fxml");
                        mainController.getTopbarController().setTitle("Hóa Đơn", "Quản lý hóa đơn");
                        break;
                    case "account":
                        mainController.loadView("AccountManagement.fxml");
                        mainController.getTopbarController().setTitle("Tài Khoản", "Quản lý tài khoản");
                        break;
                    case "employee":
                        mainController.loadView("EmployeeManagement.fxml"); // (Hoặc staff-management.fxml tùy bạn đặt)
                        mainController.getTopbarController().setTitle("Nhân Viên", "Quản lý nhân viên");
                        break;
                    case "report":
                        mainController.loadView("ReportManagement.fxml");
                        mainController.getTopbarController().setTitle("Báo Cáo", "Thống kê & báo cáo");
                        break;
                    case "supplier":
                        mainController.loadView("SupplierManagement.fxml");
                        mainController.getTopbarController().setTitle("Nhà Cung Cấp", "Quản lý đối tác & nhà cung cấp");
                        break;
                    case "pet":
                        mainController.loadView("PetManagement.fxml");
                        mainController.getTopbarController().setTitle("Thú Cưng", "Quản lý hồ sơ thú cưng");
                        break;
                    case "inventory":
                        mainController.loadView("InventoryManagement.fxml");
                        mainController.getTopbarController().setTitle("Kho Hàng", "Quản lý nhập xuất tồn kho");
                        break;
                    case "product":
                        mainController.loadView("ProductManagement.fxml");
                        mainController.getTopbarController().setTitle("Sản Phẩm", "Quản lý danh mục sản phẩm");
                        break;
                    // Bạn có thể tự bổ sung các case "pet", "product", "inventory"... vào đây
                    default:
                        System.out.println("⚠️ Chưa code tính năng chuyển trang cho menu: " + menuType);
                        break;
                }
            }
        }
    }

    // --- Các thao tác khác ở Footer ---
    @FXML
    public void onChangePassword(MouseEvent event) {
        System.out.println("Mở Form Đổi Mật Khẩu");
    }

    @FXML
    public void onLogout(MouseEvent event) {
        System.out.println("Tiến hành đăng xuất...");
    }

    // Hàm tiện ích: Xóa màu của menu cũ, bôi màu cho menu mới
    private void setActive(VBox item) {
        if (activeMenuItem != null) {
            activeMenuItem.getStyleClass().remove("menu-item-active");
        }
        item.getStyleClass().add("menu-item-active");
        activeMenuItem = item;
    }
}