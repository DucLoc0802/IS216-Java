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
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
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
        cbbMethod.setItems(FXCollections.observableArrayList(
            "Tất cả",
            "Tiền mặt",
            "Thẻ",
            "Chuyển khoản",
            "Ví điện tử"
        ));
        cbbStatus.setItems(FXCollections.observableArrayList(
            "Tất cả",
            "Thành công",
            "Đang xử lý",
            "Thanh toán một phần",
            "Thất bại",
            "Đã hủy",
            "Đã hoàn tiền"
        ));
        cbbMethod.setValue("Tất cả");
        cbbStatus.setValue("Tất cả");

        colPaymentId.setCellValueFactory(new PropertyValueFactory<>("paymentId"));
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colMethod.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyStringWrapper(
            toVietnamesePaymentMethod(cell.getValue().getPaymentMethod())
        ));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(column -> new TableCell<>() {
            private final Label badge = new Label();

            {
                badge.getStyleClass().add("payment-status-badge");
                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                badge.getStyleClass().removeAll(
                    "payment-status-success",
                    "payment-status-pending",
                    "payment-status-partial",
                    "payment-status-failed",
                    "payment-status-refunded",
                    "payment-status-default"
                );

                if (empty || item == null || item.trim().isEmpty()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                String databaseStatus = item.trim().toUpperCase();
                badge.setText(toVietnamesePaymentStatus(databaseStatus));
                badge.getStyleClass().add(paymentStatusClass(databaseStatus));
                setText(null);
                setGraphic(badge);
            }
        });
        colPaidAt.setCellValueFactory(new PropertyValueFactory<>("paidAt"));
        tablePayment.setPlaceholder(new Label(""));

        onSearch(null);
    }

    @FXML
    public void onSearch(ActionEvent event) {
        try {
            String keyword = txtSearch.getText() == null ? null : txtSearch.getText().trim();
            String method = toDatabasePaymentMethod(emptyToNull(cbbMethod.getValue()));
            String status = toDatabasePaymentStatus(emptyToNull(cbbStatus.getValue()));
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
        cbbMethod.setValue("Tất cả");
        cbbStatus.setValue("Tất cả");
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

    private String toVietnamesePaymentStatus(String status) {
        if (status == null) {
            return "";
        }
        return switch (status.trim().toUpperCase()) {
            case "PAID", "SUCCESS", "COMPLETED" -> "Thành công";
            case "PENDING" -> "Đang xử lý";
            case "PARTIAL" -> "Thanh toán một phần";
            case "FAILED" -> "Thất bại";
            case "CANCELLED", "CANCELED" -> "Đã hủy";
            case "REFUNDED" -> "Đã hoàn tiền";
            default -> status.trim();
        };
    }

    private String toDatabasePaymentStatus(String vietnameseStatus) {
        if (vietnameseStatus == null) {
            return null;
        }
        return switch (vietnameseStatus.trim()) {
            case "Thành công" -> "SUCCESS";
            case "Đang xử lý" -> "PENDING";
            case "Thanh toán một phần" -> "PARTIAL";
            case "Thất bại" -> "FAILED";
            case "Đã hủy" -> "CANCELLED";
            case "Đã hoàn tiền" -> "REFUNDED";
            default -> vietnameseStatus.trim();
        };
    }

    private String toVietnamesePaymentMethod(String method) {
        if (method == null || method.trim().isEmpty()) {
            return "";
        }
        return switch (method.trim().toUpperCase()) {
            case "CASH" -> "Tiền mặt";
            case "CARD" -> "Thẻ";
            case "BANK_TRANSFER", "TRANSFER" -> "Chuyển khoản";
            case "MOMO" -> "MoMo";
            case "VNPAY" -> "VNPay";
            case "EWALLET" -> "Ví điện tử";
            default -> method.trim();
        };
    }

    private String toDatabasePaymentMethod(String vietnameseMethod) {
        if (vietnameseMethod == null) {
            return null;
        }
        return switch (vietnameseMethod.trim()) {
            case "Tiền mặt" -> "CASH";
            case "Thẻ" -> "CARD";
            case "Chuyển khoản" -> "BANK_TRANSFER";
            case "Ví điện tử" -> "EWALLET";
            case "MoMo" -> "MOMO";
            case "VNPay" -> "VNPAY";
            default -> vietnameseMethod.trim();
        };
    }

    private String paymentStatusClass(String status) {
        if (status == null) {
            return "payment-status-default";
        }
        return switch (status.trim().toUpperCase()) {
            case "PAID", "SUCCESS", "COMPLETED" -> "payment-status-success";
            case "PENDING" -> "payment-status-pending";
            case "PARTIAL" -> "payment-status-partial";
            case "FAILED", "CANCELLED", "CANCELED" -> "payment-status-failed";
            case "REFUNDED" -> "payment-status-refunded";
            default -> "payment-status-default";
        };
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
