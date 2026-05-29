package PetHotel.gui.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import PetHotel.bus.ServiceBUS;
import PetHotel.model.AppUser;
import PetHotel.model.PetService;
import PetHotel.model.ServiceCategory;
import PetHotel.util.Role;

import java.sql.SQLException;
import java.util.List;

/**
 * ServiceController — Quản lý màn hình xem/tìm kiếm danh sách dịch vụ
 * 
 * Dành cho vai trò: Quản lý chi nhánh, Lễ tân
 * 
 * Chức năng:
 *  - Hiển thị danh sách tất cả dịch vụ
 *  - Tìm kiếm dịch vụ theo tên, loài, hoặc loại dịch vụ
 *  - Lọc dịch vụ theo loại (category)
 *  - Xem chi tiết dịch vụ
 */
public class ServiceController {

    // ── FXML Components ───────────────────────────────────────

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<ServiceCategory> filterCategory;

    @FXML
    private TableView<PetService> serviceTable;

    @FXML
    private TableColumn<PetService, String> colServiceId;

    @FXML
    private TableColumn<PetService, String> colServiceName;

    @FXML
    private TableColumn<PetService, String> colCategory;

    @FXML
    private TableColumn<PetService, String> colSpecies;

    @FXML
    private TableColumn<PetService, Double> colPrice;

    @FXML
    private TableColumn<PetService, Integer> colDuration;

    @FXML
    private TableColumn<PetService, Integer> colStatus;

    @FXML
    private javafx.scene.control.Button btnAddService;

    @FXML
    private javafx.scene.control.Button btnAddCategory;

    @FXML
    private javafx.scene.control.Button btnMaterialStandard;

    // ── Business Logic ────────────────────────────────────────

    private final ServiceBUS serviceBUS = new ServiceBUS();
    private AppUser currentUser;
    private ObservableList<PetService> serviceList;

    // ── Initialize ────────────────────────────────────────────

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser == null) {
            showError("Chưa đăng nhập. Không thể hiển thị danh sách dịch vụ.");
            return;
        }

        // Chỉ hiển thị nút "Thêm Dịch Vụ Mới" và "Thêm Loại Dịch Vụ" cho Quản lý chi nhánh
        if (btnAddService != null) {
        btnAddService.setVisible(currentUser.hasRole(Role.BRANCH_MANAGER));
    }

    if (btnAddCategory != null) {
        btnAddCategory.setVisible(currentUser.hasRole(Role.BRANCH_MANAGER));
    }

    if (btnMaterialStandard != null) {
        boolean canManageMaterial =
                currentUser.hasRole(Role.BRANCH_MANAGER)
                || currentUser.hasRole(Role.ADMIN);

        btnMaterialStandard.setVisible(canManageMaterial);
        btnMaterialStandard.setManaged(canManageMaterial);
    }

        setupTableColumns();
        setupCategoryFilter();
        setupEventHandlers();
        loadAllServices();
    }

    /**
     * Cấu hình các cột của bảng
     */
    private void setupTableColumns() {
        colServiceId.setCellValueFactory(new PropertyValueFactory<>("serviceId"));
        colServiceName.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        colSpecies.setCellValueFactory(new PropertyValueFactory<>("species"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("basePrice"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));

        // Cột trạng thái với màu xanh (hoạt động) / đỏ (ngừng)
        colStatus.setCellValueFactory(new PropertyValueFactory<>("isActive"));
        colStatus.setCellFactory(column -> new javafx.scene.control.TableCell<PetService, Integer>() {
            @Override
            protected void updateItem(Integer isActive, boolean empty) {
                super.updateItem(isActive, empty);
                if (empty || isActive == null) {
                    setText(null);
                    setStyle("");
                } else {
                    if (isActive == 1) {
                        setText("✓ Đang hoạt động");
                        setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                    } else {
                        setText("✗ Ngừng cung cấp");
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Cột loại dịch vụ sẽ được set sau khi load category
        colCategory.setCellValueFactory(cellData -> {
            String categoryId = cellData.getValue().getServiceCategoryId();
            try {
                List<ServiceCategory> categories = serviceBUS.getAllServiceCategories(currentUser);
                for (ServiceCategory cat : categories) {
                    if (cat.getServiceCategoryId().equals(categoryId)) {
                        return javafx.beans.binding.Bindings.createStringBinding(() -> cat.getCategoryName());
                    }
                }
            } catch (Exception e) {
                // Silent failure
            }
            return javafx.beans.binding.Bindings.createStringBinding(() -> categoryId);
        });
    }

    /**
     * Cấu hình ComboBox lọc theo loại dịch vụ
     */
    private void setupCategoryFilter() {
        try {
            List<ServiceCategory> categories = serviceBUS.getAllServiceCategories(currentUser);
            
            // Thêm tùy chọn "Tất cả"
            ServiceCategory allCategory = new ServiceCategory("", "Tất cả dịch vụ");
            ObservableList<ServiceCategory> categoryList = FXCollections.observableArrayList();
            categoryList.add(allCategory);
            categoryList.addAll(categories);

            filterCategory.setItems(categoryList);
            filterCategory.setCellFactory(param -> new ListCell<ServiceCategory>() {
                @Override
                protected void updateItem(ServiceCategory item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getCategoryName());
                }
            });

            filterCategory.setButtonCell(new ListCell<ServiceCategory>() {
                @Override
                protected void updateItem(ServiceCategory item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getCategoryName());
                }
            });

            filterCategory.setValue(allCategory);

        } catch (Exception e) {
            showError("Không thể tải danh sách loại dịch vụ: " + e.getMessage());
        }
    }

    /**
     * Cấu hình event handlers
     */
    private void setupEventHandlers() {
        // Tìm kiếm khi nhấn Enter hoặc click nút tìm
        searchField.setOnAction(e -> handleSearch());

        // Lọc khi thay đổi category
        filterCategory.setOnAction(e -> applyFilters());
    }

    /**
     * Tải tất cả dịch vụ từ CSDL
     * - Branch Manager/Admin: Tải tất cả dịch vụ (active + inactive) để quản lý
     * - Lễ tân: Chỉ tải dịch vụ đang hoạt động
     */
    private void loadAllServices() {
        try {
            List<PetService> services;
            
            // Kiểm tra quyền: Branch Manager/Admin được phép xem tất cả dịch vụ
            if (currentUser.hasRole(Role.BRANCH_MANAGER) || currentUser.hasRole(Role.ADMIN)) {
                services = serviceBUS.getAllServicesForManagement(currentUser);
            } else {
                services = serviceBUS.getAllServices(currentUser);
            }
            
            serviceList = FXCollections.observableArrayList(services);
            serviceTable.setItems(serviceList);
        } catch (Exception e) {
            showError("Không thể tải danh sách dịch vụ: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện tìm kiếm dịch vụ
     */
    @FXML
    public void handleSearch() {
        String keyword = searchField.getText().trim();
        
        try {
            if (keyword.isEmpty()) {
                loadAllServices();
            } else {
                List<PetService> results = serviceBUS.searchServices(keyword, currentUser);
                serviceList = FXCollections.observableArrayList(results);
                serviceTable.setItems(serviceList);
            }
            applyFilters();
        } catch (Exception e) {
            showError("Lỗi khi tìm kiếm: " + e.getMessage());
        }
    }

    @FXML
    private void onOpenMaterialStandard() {
        PetService selected = serviceTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Vui lòng chọn một dịch vụ.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/PetHotel/gui/view/ServiceProductStandardDialog.fxml")
            );

            Parent root = loader.load();

            ServiceProductStandardController controller = loader.getController();
            controller.setCurrentUser(SessionManager.getInstance().getCurrentUser());
            controller.setService(selected);

            Stage stage = new Stage();
            stage.setTitle("Cấu hình vật tư tiêu hao");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Không thể mở màn hình vật tư tiêu hao: " + e.getMessage());
        }
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Áp dụng bộ lọc category
     */
    private void applyFilters() {
        ServiceCategory selectedCategory = filterCategory.getValue();
        
        if (selectedCategory != null && !selectedCategory.getServiceCategoryId().isEmpty()) {
            // Lọc dịch vụ theo category
            ObservableList<PetService> filtered = FXCollections.observableArrayList();
            for (PetService service : serviceList) {
                if (service.getServiceCategoryId().equals(selectedCategory.getServiceCategoryId())) {
                    filtered.add(service);
                }
            }
            serviceTable.setItems(filtered);
        } else {
            serviceTable.setItems(serviceList);
        }
    }

    /**
     * Xóa bộ lọc tìm kiếm và category
     */
    @FXML
    public void handleClearFilter() {
        searchField.clear();
        ServiceCategory allCategory = filterCategory.getItems().get(0);
        filterCategory.setValue(allCategory);
        loadAllServices();
    }

    /**
     * Xem chi tiết dịch vụ khi click vào dòng bảng
     */
    @FXML
    public void handleTableClick(MouseEvent event) {
        PetService selectedService = serviceTable.getSelectionModel().getSelectedItem();
        if (selectedService != null && event.getClickCount() == 2) {
            showServiceDetails(selectedService);
        }
    }

    /**
     * Hiển thị thông tin chi tiết của dịch vụ (trong dialog có chức năng sửa/xóa)
     */
    private void showServiceDetails(PetService service) {
        ServiceDetailController.openServiceDetail(service, (unused) -> {
            // Refresh danh sách khi dịch vụ được cập nhật hoặc xóa
            loadAllServices();
        });
    }

    /**
     * Hiển thị thông báo lỗi
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Mở dialog thêm dịch vụ mới
     */
    @FXML
    public void handleAddService() {
        AddServiceController.openAddServiceDialog((unused) -> {
            // Refresh danh sách dịch vụ và bộ lọc category khi thêm dịch vụ mới
            loadAllServices();
            setupCategoryFilter();
        });
    }

    /**
     * Mở dialog thêm loại dịch vụ mới
     */
    @FXML
    public void handleAddCategory() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/PetHotel/gui/view/AddServiceCategoryDialog.fxml")
            );
            javafx.scene.Parent root = loader.load();

            AddServiceCategoryController controller = loader.getController();

            // Set callback để refresh danh sách sau khi thêm
            controller.setOnCategoryAdded(categoryName -> {
                loadAllServices(); // Reload danh sách
                setupCategoryFilter(); // Reload category filter
            });

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Thêm Loại Dịch Vụ Mới");
            stage.setScene(new javafx.scene.Scene(root));
            stage.setResizable(false);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (java.io.IOException e) {
            showError("Lỗi khi mở dialog: " + e.getMessage());
        }
    }
}
