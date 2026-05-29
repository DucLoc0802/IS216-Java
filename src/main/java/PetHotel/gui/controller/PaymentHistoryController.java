package PetHotel.gui.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import PetHotel.bus.InvoiceBUS;
import PetHotel.model.Payment;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class PaymentHistoryController {

    @FXML
    private TextField txtSearch;

    @FXML
    private ComboBox<String> cbbMethod;

    @FXML
    private ComboBox<String> cbbStatus;

    @FXML
    private DatePicker dpFromDate;

    @FXML
    private DatePicker dpToDate;

    @FXML
    private TableView<Payment> tablePayment;

    @FXML
    private TableColumn<Payment, String> colPaymentId;

    @FXML
    private TableColumn<Payment, String> colOrderId;

    @FXML
    private TableColumn<Payment, String> colCustomer;

    @FXML
    private TableColumn<Payment, String> colMethod;

    @FXML
    private TableColumn<Payment, Double> colAmount;

    @FXML
    private TableColumn<Payment, String> colStatus;

    @FXML
    private TableColumn<Payment, Date> colPaidAt;

    private final InvoiceBUS invoiceBUS = new InvoiceBUS();

    @FXML
    public void initialize() {
        cbbMethod.setItems(FXCollections.observableArrayList("CASH", "CARD", "BANK_TRANSFER"));
        cbbStatus.setItems(FXCollections.observableArrayList("SUCCESS", "FAILED", "PENDING", "REFUNDED"));
        cbbMethod.setValue(null);
        cbbStatus.setValue(null);

        colPaymentId.setCellValueFactory(new PropertyValueFactory<>("paymentId"));
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colMethod.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colPaidAt.setCellValueFactory(new PropertyValueFactory<>("paidAt"));

        onSearch(null);
    }

    @FXML
    public void onSearch(ActionEvent event) {
        try {
            String keyword = txtSearch.getText() == null ? null : txtSearch.getText().trim();
            String method = emptyToNull(cbbMethod.getValue());
            String status = emptyToNull(cbbStatus.getValue());
            List<Payment> payments = invoiceBUS.searchPaymentHistory(
                keyword,
                method,
                status,
                toDate(dpFromDate.getValue()),
                toDate(dpToDate.getValue())
            );
            tablePayment.setItems(FXCollections.observableArrayList(payments));
        } catch (IllegalArgumentException ex) {
            showAlert(Alert.AlertType.WARNING, "Dữ liệu không hợp lệ", ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể tải lịch sử giao dịch: " + ex.getMessage());
        }
    }

    @FXML
    public void onClear(ActionEvent event) {
        txtSearch.clear();
        cbbMethod.setValue(null);
        cbbStatus.setValue(null);
        dpFromDate.setValue(null);
        dpToDate.setValue(null);
        onSearch(null);
    }

    private Date toDate(LocalDate localDate) {
        return localDate == null ? null : Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private String emptyToNull(String value) {
        return value == null || value.trim().isEmpty() || "Tất cả".equals(value.trim()) ? null : value.trim();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
