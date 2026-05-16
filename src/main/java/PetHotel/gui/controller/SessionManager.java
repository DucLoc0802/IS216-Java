package PetHotel.gui.controller;

import PetHotel.bus.AuthBUS;
import PetHotel.model.AppUser;
import PetHotel.util.Role;

/**
 * ══════════════════════════════════════════════════════════
 * SessionManager — Quản lý session đăng nhập toàn cục
 * ══════════════════════════════════════════════════════════
 */
public class SessionManager {

    private static SessionManager instance;

    private static AppUser currentUser; // Lưu nguyên object AppUser sẽ tiện hơn lưu từng field rời rạc
    private String branchId;
    private String branchName;

    // AuthBUS instance dùng chung cho toàn bộ ứng dụng
    private AuthBUS authBUS;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // Đã đổi tham số thành AppUser để Clean Code hơn
    public void login(AppUser user, String branchId, String branchName) {
        this.currentUser = user;
        this.branchId = branchId;
        this.branchName = branchName;
    }

    public void logout() {
        this.currentUser = null;
        this.branchId = null;
        this.branchName = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // ─── Getters ────────────────────────────────────────
    public static AppUser getCurrentUser() { return currentUser; }
    public String getUserId()       { return currentUser != null ? currentUser.getEmployeeId() : null; }
    public String getBranchId()   { return branchId; }
    public String getBranchName() { return branchName; }

    public AuthBUS getAuthBUS() { return authBUS; }
    public void setAuthBUS(AuthBUS authBUS) { this.authBUS = authBUS; }

    public boolean hasRole(Role... roles) {
        if (currentUser == null) return false;
        for (Role r : roles) {
            if (r == currentUser.getRole()) return true;
        }
        return false;
    }
}