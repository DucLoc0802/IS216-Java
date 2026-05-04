package PetHotel.gui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class InvoiceController {

    @FXML private TextField txtSearch;
    @FXML private TableView<?> tableInvoice;

    @FXML
    public void initialize() {
        System.out.println("Đã load giao diện Quản lý Hóa Đơn");
        // TODO: Cấu hình các cột (Mã HĐ, Khách hàng, Tổng tiền, Ngày lập, Người lập)
    }

    @FXML public void onCreateInvoice(ActionEvent event) { System.out.println("Mở form Tạo hóa đơn thủ công..."); }
    @FXML public void onPayment(ActionEvent event) { System.out.println("Thực hiện Thanh toán hóa đơn..."); }
    @FXML public void onPrint(ActionEvent event) { System.out.println("In hóa đơn / Xuất PDF..."); }
    @FXML public void onFilter(ActionEvent event) { System.out.println("Lọc danh sách Hóa đơn..."); }

    @FXML 
    public void onTableClick(MouseEvent event) { 
        System.out.println("Vừa click chọn Hóa đơn để xem chi tiết!"); 
    }

    @FXML
    public void handleSearch(ActionEvent event) {
        System.out.println("Tìm kiếm hóa đơn: " + txtSearch.getText());
    }

    @FXML
    public void handleViewDetail(ActionEvent event) {
        System.out.println("Mở Popup Xem chi tiết hóa đơn (Các dịch vụ, tiền phòng...)");
    }

    @FXML
    public void handlePrintInvoice(ActionEvent event) {
        System.out.println("Tiến hành in hóa đơn ra máy in (hoặc xuất file PDF)");
    }
}