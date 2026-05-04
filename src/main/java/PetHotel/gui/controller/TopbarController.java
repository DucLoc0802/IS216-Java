package PetHotel.gui.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class TopbarController {

    @FXML private Label pageTitle;
    @FXML private Label pageBreadcrumb;
    @FXML private Label currentDate;
    @FXML private Label currentTime;
    @FXML private javafx.scene.control.TextField globalSearch;
    @FXML
    public void initialize() {
        setupClock();
    }

    // Hàm này sẽ được SidebarController gọi mượn thông qua MainController
// Bổ sung hàm này vào TopbarController.java
    public void setTitle(String title, String breadcrumb) {
        if (pageTitle != null && pageBreadcrumb != null) {
            pageTitle.setText(title);
            pageBreadcrumb.setText(breadcrumb);
      }
    }

    // Thiết lập đồng hồ thời gian thực
    private void setupClock() {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

        // Set ngày hiện tại
        currentDate.setText(LocalDate.now().format(dateFormat));

        // Tạo một Timeline chạy liên tục mỗi giây để cập nhật giờ
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            currentTime.setText(LocalTime.now().format(timeFormat));
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    // ... (Giữ nguyên các khai báo và hàm initialize() cũ)

    // --- BỔ SUNG CÁC HÀM XỬ LÝ SỰ KIỆN CHO TOPBAR ---

    @FXML
    public void onSearch() {
        // Lấy từ khóa từ ô tìm kiếm (nếu bạn đã khai báo TextField globalSearch)
        // String keyword = globalSearch.getText();
        System.out.println("Thực hiện tìm kiếm toàn cục...");
        // TODO: Viết logic tìm kiếm sau
    }

    @FXML
    public void onNotification() {
        System.out.println("Mở danh sách thông báo...");
        // TODO: Hiện Popup danh sách thông báo
    }

    @FXML
    public void onBranchSwitch() {
        System.out.println("Mở danh sách chọn chi nhánh...");
        // TODO: Hiện Popup cho phép đổi chi nhánh làm việc
    }
}