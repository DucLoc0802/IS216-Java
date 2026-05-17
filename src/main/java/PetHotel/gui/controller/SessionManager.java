package PetHotel.gui.controller;

/**
 * ══════════════════════════════════════════════════════════
 *  SessionManager — Quản lý session đăng nhập toàn cục
 * ══════════════════════════════════════════════════════════
 *  Singleton. Giữ thông tin user đang đăng nhập.
 *  Dùng trong Controller để lấy quyền và thông tin hiển thị.
 */
public class SessionManager {

    public enum Role {
        RECEPTIONIST,     // Lễ Tân
        BRANCH_MANAGER,   // Quản Lý Chi Nhánh
        ADMIN,            // Admin Hệ Thống
        CEO               // CEO
    }

    private static SessionManager instance;

    private String userId;
    private String fullName;
    private Role   role;
    private String branchId;
    private String branchName;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void login(String userId, String fullName, Role role,
                      String branchId, String branchName) {
        this.userId     = userId;
        this.fullName   = fullName;
        this.role       = role;
        this.branchId   = branchId;
        this.branchName = branchName;
    }

    public void logout() {
        userId = fullName = branchId = branchName = null;
        role = null;
    }

    public boolean isLoggedIn() { return userId != null; }

    // ─── Getters ────────────────────────────────────────
    public String getUserId()     { return userId; }
    public String getFullName()   { return fullName; }
    public Role   getRole()       { return role; }
    public String getBranchId()   { return branchId; }
    public String getBranchName() { return branchName; }

    public boolean hasRole(Role... roles) {
        for (Role r : roles) if (r == this.role) return true;
        return false;
    }
}


// ══════════════════════════════════════════════════════════════════
// ──  Reusable Component: StatCard (programmatic)
// ══════════════════════════════════════════════════════════════════
// Tạo một stat card lập trình, dùng trong code Java thay vì FXML
// khi cần render động.
//
// Ví dụ sử dụng:
//   StatCard card = new StatCard("📅", "Tổng Booking", "24", "+3 mới", "stat-card-blue");
//   statsRow.getChildren().add(card);
// ══════════════════════════════════════════════════════════════════

class StatCard extends javafx.scene.layout.VBox {

    private final javafx.scene.control.Label valueLabel;

    public StatCard(String icon, String label, String value, String delta, String styleClass) {
        this.getStyleClass().addAll("stat-card", styleClass);

        javafx.scene.layout.HBox top = new javafx.scene.layout.HBox(12);
        top.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        javafx.scene.control.Label iconLbl = new javafx.scene.control.Label(icon);
        iconLbl.getStyleClass().add("stat-icon");

        javafx.scene.layout.VBox info = new javafx.scene.layout.VBox(2);
        javafx.scene.control.Label labelLbl = new javafx.scene.control.Label(label);
        labelLbl.getStyleClass().add("stat-label");

        valueLabel = new javafx.scene.control.Label(value);
        valueLabel.getStyleClass().add("stat-value");
        info.getChildren().addAll(labelLbl, valueLabel);

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        javafx.scene.control.Label deltaLbl = new javafx.scene.control.Label(delta);
        deltaLbl.getStyleClass().addAll("stat-delta", "stat-delta-up");

        top.getChildren().addAll(iconLbl, info, spacer, deltaLbl);
        this.getChildren().add(top);
    }

    public void setValue(String v) { valueLabel.setText(v); }
}


// ══════════════════════════════════════════════════════════════════
// ──  Reusable Component: SidebarMenuItem
// ══════════════════════════════════════════════════════════════════
class SidebarMenuItem extends javafx.scene.layout.VBox {

    private final javafx.scene.layout.HBox row;

    public SidebarMenuItem(String icon, String label) {
        this.getStyleClass().add("menu-item");
        this.setCursor(javafx.scene.Cursor.HAND);

        row = new javafx.scene.layout.HBox(12);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        javafx.scene.control.Label iconLbl = new javafx.scene.control.Label(icon);
        iconLbl.getStyleClass().add("menu-icon");

        javafx.scene.control.Label labelLbl = new javafx.scene.control.Label(label);
        labelLbl.getStyleClass().add("menu-label");

        row.getChildren().addAll(iconLbl, labelLbl);
        this.getChildren().add(row);
    }

    public void addBadge(String text, String badgeStyle) {
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        javafx.scene.control.Label badge = new javafx.scene.control.Label(text);
        badge.getStyleClass().addAll("menu-badge", badgeStyle);

        row.getChildren().addAll(spacer, badge);
    }

    public void setActive(boolean active) {
        if (active) this.getStyleClass().add("menu-item-active");
        else        this.getStyleClass().remove("menu-item-active");
    }
}


// ══════════════════════════════════════════════════════════════════
// ──  Reusable Component: StatusBadge (TableCell renderer)
// ══════════════════════════════════════════════════════════════════
class StatusBadgeCell<T> extends javafx.scene.control.TableCell<T, String> {

    @Override
    protected void updateItem(String status, boolean empty) {
        super.updateItem(status, empty);
        if (empty || status == null) {
            setGraphic(null);
            return;
        }
        javafx.scene.control.Label badge = new javafx.scene.control.Label(status);
        badge.getStyleClass().add("status-badge");

        switch (status.toLowerCase()) {
            case "chờ xác nhận": case "pending":    badge.getStyleClass().add("status-pending");    break;
            case "đang xử lý":  case "inprogress":  badge.getStyleClass().add("status-inprogress"); break;
            case "hoàn thành":  case "done":        badge.getStyleClass().add("status-done");       break;
            case "đã hủy":      case "cancelled":   badge.getStyleClass().add("status-cancelled");  break;
        }
        setGraphic(badge);
        setText(null);
    }
}
