package PetHotel.gui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class CustomerController {
    @FXML
    public void onAddCustomer(javafx.event.ActionEvent event) {
        System.out.println("Mở Form thêm khách hàng mới...");
    }

    @FXML
    public void onSearch(javafx.event.ActionEvent event) {
        System.out.println("Đang tìm khách hàng...");
    }

    @FXML
    public void onClearFilter(javafx.event.ActionEvent event) {
        System.out.println("Xóa bộ lọc khách hàng...");
    }

    @FXML
    public void onEdit(javafx.event.ActionEvent event) {
        System.out.println("Sửa thông tin khách hàng...");
    }

    @FXML
    public void onDelete(javafx.event.ActionEvent event) {
        System.out.println("Xóa khách hàng...");
    }

    @FXML
    public void onViewPets(javafx.event.ActionEvent event) {
        System.out.println("Xem thú cưng của khách này...");
    }

    @FXML
    public void onViewHistory(javafx.event.ActionEvent event) {
        System.out.println("Xem lịch sử...");
    }
    @FXML
    public void onTableClick(MouseEvent event) {
        System.out.println("Bạn vừa click vào một dòng trong bảng Khách Hàng!");
        
        // Gợi ý cho sau này:
        // Khách hàng đang chọn = customerTable.getSelectionModel().getSelectedItem();
        // Bật sáng các nút Sửa, Xóa...
    }
    // 1. Khai báo các thành phần giao diện
    @FXML private TextField txtSearch;
    @FXML private TableView<?> tableCustomer; // TODO: Thay <?> bằng <Customer> của bạn

    // 2. Hàm khởi tạo dữ liệu
    @FXML
    public void initialize() {
        setupTableColumns();
        loadDataFromDatabase();
    }

    private void setupTableColumns() {
        // TODO: Cài đặt CellValueFactory cho các cột (ID, Tên, SĐT...)
    }

    private void loadDataFromDatabase() {
        // TODO: Gọi CustomerBUS.getAll() và đổ vào tableCustomer
        System.out.println("Đang tải danh sách khách hàng...");
    }

    // 3. Xử lý các nút chức năng (CRUD)
    @FXML
    public void handleSearch(ActionEvent event) {
        String keyword = txtSearch.getText();
        System.out.println("Tìm kiếm khách hàng: " + keyword);
        // TODO: Lọc dữ liệu trên bảng
    }

    @FXML
    public void handleAdd(ActionEvent event) {
        System.out.println("Mở form Thêm khách hàng mới");
        // TODO: Hiển thị một Dialog/Popup form
    }

    @FXML
    public void handleEdit(ActionEvent event) {
        // Lấy dòng đang được chọn trên bảng
        Object selected = tableCustomer.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            System.out.println("Vui lòng chọn 1 khách hàng để sửa!");
            return;
        }
        System.out.println("Mở form Sửa khách hàng");
    }

    @FXML
    public void handleDelete(ActionEvent event) {
         Object selected = tableCustomer.getSelectionModel().getSelectedItem();
        if (selected == null) {
             System.out.println("Vui lòng chọn 1 khách hàng để xóa!");
             return;
        }
        // TODO: Hiện thông báo Alert.AlertType.CONFIRMATION hỏi "Bạn có chắc muốn xóa?"
        System.out.println("Tiến hành xóa khách hàng");
    }
}

