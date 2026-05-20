package PetHotel.gui.controller;

import java.io.IOException;
import java.util.List;

import PetHotel.bus.AccountBUS;
import PetHotel.exception.AuthorizationException;
import PetHotel.exception.NotFoundException;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.model.Employee;
import PetHotel.util.Role;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AccountController {

    private AccountBUS accountBUS;

    @FXML private TextField searchAccount;
    @FXML private ComboBox<String> filterRole;
    @FXML private ComboBox<String> filterStatus;
    @FXML private ComboBox<String> filterBranch;

    @FXML private TableView<AppUser> accountTable;
    @FXML private TableColumn<AppUser, Void> colSelect;
    @FXML private TableColumn<AppUser, String> colAvatar;
    @FXML private TableColumn<AppUser, String> colUsername;
    @FXML private TableColumn<AppUser, String> colFullName;
    @FXML private TableColumn<AppUser, String> colRole;
    @FXML private TableColumn<AppUser, String> colBranch;
    @FXML private TableColumn<AppUser, String> colEmail;
    @FXML private TableColumn<AppUser, String> colStatus;
    @FXML private TableColumn<AppUser, String> colLastLogin;
    @FXML private TableColumn<AppUser, Void> colActions;

    @FXML private Label statTotalAccounts;
    @FXML private Label statActiveAccounts;
    @FXML private Label statLockedAccounts;
    @FXML private Label statAdminAccounts;

    @FXML private Button btnLock;
    @FXML private Button btnUnlock;
    @FXML private Button btnResetPwd;
    @FXML private Button btnPermission;
    @FXML private Label selectionInfo;
    @FXML private Pagination pagination;
    @FXML private Label pageInfo;

    @FXML private VBox detailPanel;
    @FXML private VBox accountDetailCard;
    @FXML private VBox noSelectionHint;
    @FXML private Label detailAvatar;
    @FXML private Label detailFullName;
    @FXML private Label detailRole;
    @FXML private Label detailUsername;
    @FXML private Label detailEmail;
    @FXML private Label detailBranch;
    @FXML private Label detailCreated;
    @FXML private Label detailLastLogin;
    @FXML private Label detailStatus;
    @FXML private Button btnDetailEdit;
    @FXML private Button btnDetailLock;
    @FXML private Button btnDetailReset;

    @FXML private ComboBox<String> roleSelector;
    @FXML private ComboBox<String> branchSelector;
    @FXML private Button btnSavePermission;

    @FXML private VBox auditLogList;
    @FXML private Pagination logPagination;
    @FXML private Label logPageInfo;

    private final ObservableList<AppUser> accountData = FXCollections.observableArrayList();
    private AppUser selectedUser;

    private static final int PAGE_SIZE = 15;

    @FXML
    public void initialize() {
        accountBUS = new AccountBUS();

        setupTableColumns();
        accountTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        setupFilterListeners();
        loadAccountData();
        loadStats();
        loadBranches();
    }

    private void setupTableColumns() {
        colUsername.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getUserName()));
        colFullName.setCellValueFactory(cellData -> {
            Employee emp = cellData.getValue().getEmployee();
            return new SimpleStringProperty(emp != null ? emp.getFullName() : "—");
        });
        colRole.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getRole().getDisplayName()));
        colBranch.setCellValueFactory(cellData -> {
            Employee emp = cellData.getValue().getEmployee();
            return new SimpleStringProperty(emp != null && emp.getBranchId() != null ? emp.getBranchId() : "—");
        });
        colEmail.setCellValueFactory(cellData -> {
            Employee emp = cellData.getValue().getEmployee();
            return new SimpleStringProperty(emp != null && emp.getEmail() != null ? emp.getEmail() : "—");
        });
        colStatus.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().isActive() ? "Hoạt động" : "Đã khóa"));

        colLastLogin.setCellValueFactory(cellData -> {
            if (cellData.getValue().getLastLogin() != null) {
                return new SimpleStringProperty(cellData.getValue().getLastLogin().toString());
            }
            return new SimpleStringProperty("Chưa đăng nhập");
        });

        colAvatar.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    AppUser user = getTableRow().getItem();
                    Employee emp = user.getEmployee();
                    String name = (emp != null && emp.getFullName() != null) ? emp.getFullName() : user.getUserName();
                    setText(String.valueOf(name.charAt(0)).toUpperCase());
                }
            }
        });

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    getStyleClass().removeAll("status-active", "status-inactive");
                } else {
                    setText(item);
                    getStyleClass().removeAll("status-active", "status-inactive");
                    if ("Hoạt động".equals(item)) {
                        getStyleClass().add("status-active");
                    } else {
                        getStyleClass().add("status-inactive");
                    }
                }
            }
        });
    }

    private void setupFilterListeners() {
        searchAccount.setOnAction(e -> loadAccountData());
        filterRole.setOnAction(e -> loadAccountData());
        filterStatus.setOnAction(e -> loadAccountData());
        filterBranch.setOnAction(e -> loadAccountData());
    }

    private void loadAccountData() {
        try {
            List<AppUser> allAccounts = accountBUS.getAllAccounts();
            accountData.setAll(allAccounts);
            accountTable.setItems(accountData);
            updatePageInfo();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tải danh sách tài khoản: " + e.getMessage());
        }
    }

    private void loadStats() {
        try {
            int[] stats = accountBUS.getAccountStats();
            statTotalAccounts.setText(String.valueOf(stats[0]));
            statActiveAccounts.setText(String.valueOf(stats[1]));
            statLockedAccounts.setText(String.valueOf(stats[2]));
            statAdminAccounts.setText(String.valueOf(stats[3]));
        } catch (Exception e) {
            System.err.println("Không thể tải thống kê: " + e.getMessage());
        }
    }

    private void loadBranches() {
    }

    private void updatePageInfo() {
        int total = accountData.size();
        pageInfo.setText("Hiển thị " + total + " / " + total + " tài khoản");
        pagination.setPageCount(Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE)));
    }

    @FXML
    public void onSearch(ActionEvent event) {
        String keyword = searchAccount.getText();
        try {
            List<AppUser> results = accountBUS.searchAccounts(keyword);
            accountData.setAll(results);
            updatePageInfo();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tìm kiếm: " + e.getMessage());
        }
    }

    @FXML
    public void handleSearch(ActionEvent event) {
        onSearch(event);
    }

    @FXML
    public void onApplyFilter(ActionEvent event) {
        loadAccountData();
    }

    @FXML
    public void onClearFilter(ActionEvent event) {
        searchAccount.clear();
        filterRole.getSelectionModel().clearSelection();
        filterStatus.getSelectionModel().clearSelection();
        loadAccountData();
    }

    @FXML
    public void onTableClick(MouseEvent event) {
        AppUser selected = accountTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            this.selectedUser = selected;
            showAccountDetail(selected);
            enableActionButtons(true);
        }
    }

    private void showAccountDetail(AppUser user) {
        noSelectionHint.setVisible(false);
        accountDetailCard.setVisible(true);

        Employee emp = user.getEmployee();
        String fullName = (emp != null && emp.getFullName() != null) ? emp.getFullName() : user.getUserName();

        detailAvatar.setText(String.valueOf(fullName.charAt(0)).toUpperCase());
        detailFullName.setText(fullName);
        detailRole.setText(user.getRole().getDisplayName());
        detailUsername.setText(user.getUserName());
        detailEmail.setText(emp != null && emp.getEmail() != null ? emp.getEmail() : "—");
        detailBranch.setText(emp != null && emp.getBranchId() != null ? emp.getBranchId() : "—");
        detailCreated.setText(user.getCreatedAt() != null ? user.getCreatedAt().toString() : "—");
        detailLastLogin.setText(user.getLastLogin() != null ? user.getLastLogin().toString() : "Chưa đăng nhập");

        if (user.isActive()) {
            detailStatus.setText("Hoạt động");
            detailStatus.getStyleClass().removeAll("status-inactive");
            detailStatus.getStyleClass().add("status-active");
        } else {
            detailStatus.setText("Đã khóa");
            detailStatus.getStyleClass().removeAll("status-active");
            detailStatus.getStyleClass().add("status-inactive");
        }

        btnDetailEdit.setDisable(false);
        btnDetailLock.setDisable(false);
        btnDetailReset.setDisable(false);

        roleSelector.setDisable(false);
        branchSelector.setDisable(false);
        btnSavePermission.setDisable(false);
    }

    private void enableActionButtons(boolean enable) {
        btnLock.setDisable(!enable);
        btnUnlock.setDisable(!enable);
        btnResetPwd.setDisable(!enable);
        btnPermission.setDisable(!enable);
    }

    @FXML
    public void onCreateAccount(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PetHotel/gui/view/AccountForm.fxml"));
            Parent root = loader.load();

            AccountFormController controller = loader.getController();
            controller.setParentController(this);

            Stage accountForm = new Stage();
            accountForm.setTitle("PetHotel - Tạo tài khoản mới");
            accountForm.setScene(new Scene(root));
            accountForm.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Lỗi hệ thống", "Không thể tải giao diện tạo tài khoản!");
        }
    }

    @FXML
    public void handleAdd(ActionEvent event) {
        onCreateAccount(event);
    }

    @FXML
    public void onEditAccount(ActionEvent event) {
        if (selectedUser == null) {
            showAlert(AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một tài khoản để sửa.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PetHotel/gui/view/AccountForm.fxml"));
            Parent root = loader.load();

            AccountFormController controller = loader.getController();
            controller.setParentController(this);
            controller.setEditData(selectedUser);

            Stage accountForm = new Stage();
            accountForm.setTitle("PetHotel - Sửa tài khoản");
            accountForm.setScene(new Scene(root));
            accountForm.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Lỗi hệ thống", "Không thể tải giao diện sửa tài khoản!");
        }
    }

    @FXML
    public void handleEdit(ActionEvent event) {
        onEditAccount(event);
    }

    @FXML
    public void onLockSelected(ActionEvent event) {
        if (selectedUser == null) return;
        confirmAndExecute("Khóa tài khoản",
            "Bạn có chắc muốn khóa tài khoản \"" + selectedUser.getUserName() + "\"?",
            () -> {
                AppUser currentUser = SessionManager.getInstance().getCurrentUser();
                accountBUS.lockAccount(currentUser, selectedUser.getEmployeeId());
                showAlert(AlertType.INFORMATION, "Thành công", "Đã khóa tài khoản.");
                loadAccountData();
                loadStats();
            });
    }

    @FXML
    public void onLockSingle(ActionEvent event) {
        onLockSelected(event);
    }

    @FXML
    public void onUnlockSelected(ActionEvent event) {
        if (selectedUser == null) return;
        confirmAndExecute("Mở khóa tài khoản",
            "Bạn có chắc muốn mở khóa tài khoản \"" + selectedUser.getUserName() + "\"?",
            () -> {
                AppUser currentUser = SessionManager.getInstance().getCurrentUser();
                accountBUS.unlockAccount(currentUser, selectedUser.getEmployeeId());
                showAlert(AlertType.INFORMATION, "Thành công", "Đã mở khóa tài khoản.");
                loadAccountData();
                loadStats();
            });
    }

    @FXML
    public void onResetPassword(ActionEvent event) {
        if (selectedUser == null) return;
        showResetPasswordDialog(selectedUser);
    }

    @FXML
    public void onResetSinglePwd(ActionEvent event) {
        onResetPassword(event);
    }

    @FXML
    public void handleResetPassword(ActionEvent event) {
        onResetPassword(event);
    }

    private void showResetPasswordDialog(AppUser user) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Đặt lại mật khẩu");
        dialog.setHeaderText("Đặt lại mật khẩu cho: " + user.getUserName());
        dialog.setContentText("Mật khẩu mới:");

        dialog.showAndWait().ifPresent(newPassword -> {
            try {
                AppUser currentUser = SessionManager.getInstance().getCurrentUser();
                accountBUS.resetPassword(currentUser, user.getEmployeeId(), newPassword);
                showAlert(AlertType.INFORMATION, "Thành công", "Đã đặt lại mật khẩu cho " + user.getUserName());
            } catch (ValidationException e) {
                showAlert(AlertType.WARNING, "Mật khẩu không hợp lệ", e.getMessage());
            } catch (AuthorizationException e) {
                showAlert(AlertType.ERROR, "Không có quyền", e.getMessage());
            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Lỗi", e.getMessage());
            }
        });
    }

    @FXML
    public void onManagePermission(ActionEvent event) {
        if (selectedUser == null) return;

        String selectedRoleStr = roleSelector.getValue();
        if (selectedRoleStr == null || selectedRoleStr.isEmpty()) {
            showAlert(AlertType.WARNING, "Chưa chọn", "Vui lòng chọn vai trò mới.");
            return;
        }

        Role newRole = mapDisplayNameToRole(selectedRoleStr);
        if (newRole == null) return;

        confirmAndExecute("Phân quyền",
            "Bạn có chắc muốn đổi vai trò của \"" + selectedUser.getUserName() + "\" thành " + selectedRoleStr + "?",
            () -> {
                AppUser currentUser = SessionManager.getInstance().getCurrentUser();
                accountBUS.updateRole(currentUser, selectedUser.getEmployeeId(), newRole);
                showAlert(AlertType.INFORMATION, "Thành công", "Đã cập nhật vai trò.");
                loadAccountData();
                showAccountDetail(selectedUser);
            });
    }

    @FXML
    public void onSavePermission(ActionEvent event) {
        onManagePermission(event);
    }

    @FXML
    public void onFilterLog(ActionEvent event) {
        System.out.println("Lọc Audit Log (chưa implement đầy đủ)");
    }

    @FXML
    public void onExportLog(ActionEvent event) {
        System.out.println("Xuất Audit Log (chưa implement đầy đủ)");
    }

    @FXML
    public void handleDelete(ActionEvent event) {
        showAlert(AlertType.INFORMATION, "Thông báo", "Chức năng xóa tài khoản không được hỗ trợ. Vui lòng dùng Khóa tài khoản.");
    }

    private void confirmAndExecute(String title, String message, Runnable action) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    action.run();
                } catch (ValidationException e) {
                    showAlert(AlertType.WARNING, "Lỗi", e.getMessage());
                } catch (AuthorizationException e) {
                    showAlert(AlertType.ERROR, "Không có quyền", e.getMessage());
                } catch (NotFoundException e) {
                    showAlert(AlertType.ERROR, "Không tìm thấy", e.getMessage());
                } catch (Exception e) {
                    showAlert(AlertType.ERROR, "Lỗi", e.getMessage());
                }
            }
        });
    }

    private Role mapDisplayNameToRole(String displayName) {
        for (Role r : Role.values()) {
            if (r.getDisplayName().equals(displayName)) return r;
        }
        return null;
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void refreshAccountData() {
        loadAccountData();
        loadStats();
    }
}
