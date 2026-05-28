package PetHotel.gui.controller;

import java.io.IOException;
import java.util.Optional;

import PetHotel.bus.AuthBUS;
import PetHotel.model.AppUser;
import PetHotel.model.Employee;
import PetHotel.util.Role;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SidebarController {

    // --- Khai báo biến Controller & BUS ---
    private MainController mainController;
    private AuthBUS authBUS;
    private VBox activeMenuItem; // Biến lưu trạng thái menu đang được chọn

    // --- Khai báo Profile ---
    @FXML private Label avatarLabel;
    @FXML private Label usernameLabel;
    @FXML private Label roleLabel;

    // --- Khai báo Menu Items ---
    @FXML private VBox menuDashboard;
    
    @FXML private VBox menuCustomer;
    @FXML private VBox menuPet;
    @FXML private VBox menuBooking;
    @FXML private VBox menuGrooming;
    @FXML private VBox menuService;
    @FXML private VBox menuAssignedTasks;
    @FXML private VBox menuRoom;
    @FXML private VBox menuInvoice;

    @FXML private VBox groupInventory; // Tiêu đề nhóm Kho
    @FXML private VBox menuProduct;
    @FXML private VBox menuInventory;
    @FXML private VBox menuSupplier;

    @FXML private VBox groupAdmin; // Tiêu đề nhóm Quản trị
    @FXML private VBox menuStaff;
    @FXML private VBox menuAccount;
    @FXML private VBox menuReport;

    @FXML
    public void initialize() {
        // 1. Lấy thông tin user hiện tại đang đăng nhập
        hideMenu(menuPet);
        AppUser currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser != null) {
            // 2. Set thông tin Profile lên UI
            //    Profile data (fullName) thuộc Employee, không thuộc AppUser
            Employee employee = currentUser.getEmployee();
            String fullName = (employee != null && employee.getFullName() != null)
                              ? employee.getFullName()
                              : currentUser.getUserName();
            usernameLabel.setText(fullName);
            avatarLabel.setText(String.valueOf(fullName.charAt(0)).toUpperCase());
            roleLabel.setText(currentUser.getRole().getDisplayName());

            // 3. Xử lý phân quyền Sidebar (Ẩn UI)
            applyRolePermissions(currentUser.getRole());
        }
    }

    // Trong file SidebarController.java

private void applyRolePermissions(Role role) {
    switch (role) {
        case RECEPTIONIST:
            // 1. NHÂN VIÊN LỄ TÂN
            // Ẩn Dashboard
            hideMenu(menuDashboard); 
            
            // Ẩn các menu liên quan đến nghiệp vụ không liên quan
            hideMenu(menuAssignedTasks);

            // Về Kho hàng: Lễ tân chỉ được tra cứu sản phẩm.
            hideMenu(menuInventory);
            hideMenu(menuSupplier);
            
            // Ẩn hoàn toàn nhóm Quản trị hệ thống & Báo cáo
            hideMenu(groupAdmin);
            hideMenu(menuStaff);
            hideMenu(menuAccount);
            hideMenu(menuReport);
            break;

        case PET_CARE_STAFF:
            showMenu(menuPet);
            showMenu(menuGrooming);
            // 2. NHÂN VIÊN CHĂM SÓC
            // Chỉ tương tác với Thú cưng và Grooming (cập nhật trạng thái, sức khoẻ)
            hideMenu(menuDashboard);
            hideMenu(menuCustomer);
            hideMenu(menuBooking);
            hideMenu(menuService);
            hideMenu(menuAssignedTasks);
            hideMenu(menuRoom);
            hideMenu(menuInvoice); 

            // Ẩn hoàn toàn nhóm Kho
            showMenu(groupInventory);
            showMenu(menuInventory);
            hideMenu(menuProduct);
            hideMenu(menuSupplier);
            
            // Ẩn hoàn toàn nhóm Quản trị
            hideMenu(groupAdmin);
            hideMenu(menuStaff);
            hideMenu(menuAccount);
            hideMenu(menuReport);
            break;

        case BRANCH_MANAGER:
            // 3. QUẢN LÝ CHI NHÁNH
            // Vận hành toàn bộ chi nhánh, quản lý nhân viên, xem báo cáo nhưng KHÔNG quản lý tài khoản hệ thống
            hideMenu(menuAccount); 
            hideMenu(menuAssignedTasks);
            hideMenu(menuSupplier);
            break;

        case ADMIN:
            // 4. ADMIN (Quản trị viên hệ thống)
            // Chỉ quản lý cấu hình hệ thống: Tài khoản, Phân quyền, Danh mục Phòng. Không vận hành hàng ngày.
            hideMenu(menuDashboard);
            hideMenu(menuCustomer);
            hideMenu(menuPet);
            hideMenu(menuBooking);
            hideMenu(menuGrooming);
            hideMenu(menuService);
            hideMenu(menuInvoice);
            hideMenu(menuAssignedTasks);
            
            // Ẩn toàn bộ nhóm Kho
            hideMenu(groupInventory);
            hideMenu(menuProduct);
            hideMenu(menuInventory);
            hideMenu(menuSupplier);
            
            // Trong Quản trị, Admin không xem Báo cáo doanh thu và không quản lý Nhân sự chi nhánh
            hideMenu(menuStaff);
            hideMenu(menuReport);
            // Giữ lại: menuRoom (cấu hình loại phòng) và menuAccount
            break;

        case CEO:
            // 5. CEO (OWNER)
            // Thiên về xem báo cáo tổng quan, dashboard, kiểm kê tồn kho và giám sát (Audit / Khoá TK)
            // Ẩn các thao tác vận hành hàng ngày
            hideMenu(menuCustomer);
            hideMenu(menuPet);
            hideMenu(menuBooking);
            hideMenu(menuGrooming);
            hideMenu(menuService);
            hideMenu(menuRoom);
            hideMenu(menuInvoice);
            hideMenu(menuAssignedTasks);
            
            // Nhóm kho: Ẩn phần bán hàng/nhà cung cấp, chỉ giữ Inventory để xem thống kê
            hideMenu(menuProduct);
            hideMenu(menuSupplier);
            
            // Nhóm Quản trị: Không trực tiếp sửa nhân viên chi nhánh
            hideMenu(menuStaff);
            // Giữ lại: menuDashboard, menuInventory, menuAccount (để khoá tk), menuReport
            break;

        default:
            // Nếu không xác định được role, ẩn tất cả, chỉ để lại menu trống (người dùng chỉ có thể Đăng xuất)
            hideMenu(menuDashboard);
            hideMenu(menuCustomer);
            hideMenu(menuPet);
            hideMenu(menuBooking);
            hideMenu(menuGrooming);
            hideMenu(menuService);
            hideMenu(menuRoom);
            hideMenu(menuInvoice);
            hideMenu(menuAssignedTasks);
            
            hideMenu(groupInventory);
            hideMenu(menuProduct);
            hideMenu(menuInventory);
            hideMenu(menuSupplier);
            
            hideMenu(groupAdmin);
            hideMenu(menuStaff);
            hideMenu(menuAccount);
            hideMenu(menuReport);
            break;
    }
}

    // Hàm tiện ích giúp ẩn hoàn toàn Node khỏi Layout
    private void hideMenu(Node node) {
        if (node != null) {
            node.setVisible(false);
            node.setManaged(false); // Quan trọng: Thu hồi lại không gian của VBox này
        }
    }

    // Nhận quyền điều khiển từ MainController
    private void showMenu(Node node) {
        if (node != null) {
            node.setVisible(true);
            node.setManaged(true);
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // Nhận AuthBUS instance từ MainController
    public void setAuthBUS(AuthBUS authBUS) {
        this.authBUS = authBUS;
    }

    // --- HÀM XỬ LÝ CHUNG CHO TẤT CẢ MENU ---
    public void setActivePetMenu() {
        setActive(menuPet);
    }

    public void setActiveGroomingMenu() {
        setActive(menuGrooming);
    }

    @FXML
    public void onMenu(MouseEvent event) {
        Node source = (Node) event.getSource();
        
        if (source instanceof VBox) {
            VBox clickedMenu = (VBox) source;

            // 1. Đổi màu cho nút vừa click (Sáng lên)
            setActive(clickedMenu);

            // 2. Đọc thẻ userData đã gắn trong FXML
            String menuType = (String) clickedMenu.getUserData();

            // 3. Lấy Role hiện tại để kiểm tra bảo mật kép
            Role currentRole = SessionManager.getInstance().getCurrentUser().getRole();

            // 4. Kiểm tra xem là nút nào và Load FXML tương ứng
            if (mainController != null && menuType != null) {
                switch (menuType) {
                    case "dashboard":
                        if (currentRole == Role.ADMIN || currentRole == Role.BRANCH_MANAGER) {
                            mainController.loadView("DashboardHome.fxml");
                            mainController.getTopbarController().setTitle("Dashboard", "Trang chủ");
                        }
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
                        if (currentRole == Role.RECEPTIONIST
                                || currentRole == Role.PET_CARE_STAFF
                                || currentRole == Role.BRANCH_MANAGER) {
                            mainController.loadView("GroomingManagement.fxml");
                            mainController.getTopbarController().setTitle(
                                "Grooming",
                                "Quản lý lịch cắt tỉa"
                            );
                        }
                        break;
                    case "service":
                        if (currentRole == Role.RECEPTIONIST || currentRole == Role.BRANCH_MANAGER) {
                            mainController.loadView("ServiceManagement.fxml");
                            mainController.getTopbarController().setTitle(
                                "Danh Sách Dịch Vụ",
                                "Xem và tra cứu danh sách dịch vụ"
                            );
                        }
                        break;
                    case "assigned-tasks":
                        if (currentRole == Role.PET_CARE_STAFF || currentRole == Role.BRANCH_MANAGER) {
                            mainController.loadView("GroomingManagement.fxml");
                            mainController.getTopbarController().setTitle(
                                "Grooming",
                                "Công việc grooming được phân công"
                            );
                        }
                        break;
                    case "invoice":
                        mainController.loadView("InvoiceManagement.fxml");
                        mainController.getTopbarController().setTitle("Hóa Đơn", "Quản lý hóa đơn");
                        break;
                    case "account":
                        if (currentRole == Role.ADMIN) {
                            mainController.loadView("AccountManagement.fxml");
                            mainController.getTopbarController().setTitle("Tài Khoản", "Quản lý tài khoản");
                        }
                        break;
                    case "employee":
                        if (currentRole == Role.ADMIN || currentRole == Role.BRANCH_MANAGER) {
                            mainController.loadView("EmployeeManagement.fxml"); 
                            mainController.getTopbarController().setTitle("Nhân Viên", "Quản lý nhân viên");
                        }
                        break;
                    case "report":
                        if (currentRole == Role.ADMIN || currentRole == Role.BRANCH_MANAGER) {
                            mainController.loadView("ReportManagement.fxml");
                            mainController.getTopbarController().setTitle("Báo Cáo", "Thống kê & báo cáo");
                        }
                        break;
                    case "supplier":
                        new Alert(
                            Alert.AlertType.INFORMATION,
                            "Chức năng quản lý nhà cung cấp đã được bỏ khỏi phạm vi kho vật tư.",
                            ButtonType.OK
                        ).showAndWait();
                        break;
                    case "pet":
                        if (mainController != null) {
                            mainController.showPetManagement(null);
                            return;
                        }
                        mainController.getTopbarController().setTitle("Thú Cưng", "Quản lý hồ sơ thú cưng");
                        break;
                    case "inventory":
                        if (currentRole == Role.ADMIN
                                || currentRole == Role.BRANCH_MANAGER
                                || currentRole == Role.PET_CARE_STAFF) {
                            mainController.loadView("InventoryManagement.fxml");
                            mainController.getTopbarController().setTitle("Kho Vật Tư", "Tra cứu tồn kho và ghi nhận tiêu hao");
                        }
                        break;
                    case "product":
                        if (currentRole == Role.ADMIN || currentRole == Role.BRANCH_MANAGER) {
                            mainController.loadView("ProductManagement.fxml");
                            mainController.getTopbarController().setTitle("Sản Phẩm", "Quản lý danh mục sản phẩm");
                        }
                        break;
                    default:
                        System.out.println("⚠️ Chưa code tính năng chuyển trang cho menu: " + menuType);
                        break;
                }
            }
        }
    }

    // --- Các thao tác khác ở Footer ---
    @FXML
    public void onViewProfile(MouseEvent event) {
        openModal("Thông tin cá nhân", "/PetHotel/gui/view/ProfileDialog.fxml");
    }

    @FXML
    public void onChangePassword(MouseEvent event) {
        openModal("Đổi mật khẩu", "/PetHotel/gui/view/ChangePasswordDialog.fxml");
    }

    @FXML
    public void onLogout(MouseEvent event) {
        // Hiển thị hộp thoại xác nhận
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        setActive(event.getSource() instanceof VBox ? (VBox) event.getSource() : null); // Bôi màu menu Logout khi click
        confirmAlert.setTitle("Đăng xuất");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Bạn có chắc chắn muốn đăng xuất?");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Người dùng xác nhận → thực hiện logout
            String employeeId = SessionManager.getInstance().getUserId();
            if (authBUS != null && employeeId != null) {
                authBUS.logout(employeeId);
            }

            // Xóa SessionManager
            SessionManager.getInstance().logout();

            // Mở lại màn hình Login
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/PetHotel/gui/view/Login.fxml"));
                Parent root = loader.load();

                Stage loginStage = new Stage();
                loginStage.setTitle("PetHotel - Đăng nhập");
                loginStage.setScene(new Scene(root));
                loginStage.setResizable(false);
                loginStage.show();

                // Đóng cửa sổ Dashboard hiện tại
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.close();

            } catch (IOException e) {
                e.printStackTrace();
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Lỗi hệ thống");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("Không thể tải lại giao diện đăng nhập!");
                errorAlert.showAndWait();
            }
        }
    }

    // Hàm tiện ích: Xóa màu của menu cũ, bôi màu cho menu mới
    private void setActive(VBox item) {
        if (activeMenuItem != null) {
            activeMenuItem.getStyleClass().remove("menu-item-active");
        }
        if (item != null) {
            item.getStyleClass().add("menu-item-active");
            activeMenuItem = item;
        }
    }

    private void openModal(String title, String resourcePath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle(title);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            AppUser currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                Employee employee = currentUser.getEmployee();
                String fullName = (employee != null && employee.getFullName() != null)
                    ? employee.getFullName()
                    : currentUser.getUserName();
                usernameLabel.setText(fullName);
                avatarLabel.setText(String.valueOf(fullName.charAt(0)).toUpperCase());
            }
        } catch (IOException e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Lỗi hệ thống");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Không thể tải giao diện: " + title);
            errorAlert.showAndWait();
        }
    }
}
