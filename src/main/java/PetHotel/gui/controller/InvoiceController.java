package PetHotel.gui.controller;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import PetHotel.bus.InvoiceBUS;
import PetHotel.model.Invoice;
import PetHotel.model.InvoiceDetail;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class InvoiceController {

    @FXML
    private TextField txtSearch;

    @FXML
    private TableView<Invoice> tableInvoice;

    @FXML
    private TableColumn<Invoice, String> colInvoiceId;

    @FXML
    private TableColumn<Invoice, String> colCustomerId;

    @FXML
    private TableColumn<Invoice, Date> colCreateDate;

    @FXML
    private TableColumn<Invoice, Double> colTotalAmount;

    @FXML
    private TableColumn<Invoice, String> colStatus;

    @FXML
    private VBox invoicePreview;

    @FXML
    private Button btnPay;

    @FXML
    private Button btnCancelInvoice;

    private final InvoiceBUS invoiceBus;

    public InvoiceController() {
        invoiceBus = new InvoiceBUS();
    }

    @FXML
    public void initialize() {
        System.out.println("Đã load giao diện Quản lý Hóa Đơn");

        if (colInvoiceId != null) {
            colInvoiceId.setCellValueFactory(new PropertyValueFactory<>("id"));
        }

        if (colCustomerId != null) {
            colCustomerId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        }

        if (colCreateDate != null) {
            colCreateDate.setCellValueFactory(new PropertyValueFactory<>("createDate"));
        }

        if (colTotalAmount != null) {
            colTotalAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        }

        if (colStatus != null) {
            colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        }

        if (tableInvoice != null) {
            tableInvoice.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                updateActionButtons(newSelection);
                if (newSelection != null) {
                    showSelectedInvoiceDetail();
                }
            });
        }

        updateActionButtons(null);
        handleSearch(null);
    }

    @FXML
    public void handleSearch(ActionEvent event) {
        String keyword = "";

        if (txtSearch != null && txtSearch.getText() != null) {
            keyword = txtSearch.getText().trim();
        }

        System.out.println("Tìm kiếm hóa đơn: " + keyword);

        try {
            List<Invoice> results = invoiceBus.searchInvoices(
                keyword.isEmpty() ? null : keyword,
                null,
                null,
                null
            );

            for (Invoice invoice : results) {
                invoiceBus.syncPaymentStatus(invoice);
            }

            ObservableList<Invoice> data = FXCollections.observableArrayList(results);
            tableInvoice.setItems(data);
            updateActionButtons(tableInvoice.getSelectionModel().getSelectedItem());

            if (results == null || results.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Không tìm thấy hóa đơn phù hợp.");
            }

        } catch (IllegalArgumentException ex) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Lỗi khi tìm kiếm hóa đơn: " + ex.getMessage());
        }
    }

    @FXML
    public void onCreateInvoice(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PetHotel/gui/view/CreateInvoice.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Tạo Hóa Đơn Mới");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            if (txtSearch != null) {
                txtSearch.clear();
            }
            handleSearch(null);
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể mở form tạo hóa đơn: " + ex.getMessage());
        }
    }

    @FXML
    public void onPayment(ActionEvent event) {
        Invoice selectedInvoice = tableInvoice.getSelectionModel().getSelectedItem();

        if (selectedInvoice == null) {
            showAlert(Alert.AlertType.WARNING, "Thông báo", "Vui lòng chọn một hóa đơn để thanh toán.");
            return;
        }

        if (!isPayable(selectedInvoice)) {
            showAlert(Alert.AlertType.WARNING, "Thông báo", "Không thể thanh toán hóa đơn có trạng thái " + selectedInvoice.getStatus());
            return;
        }

        try {
            double paid = invoiceBus.getTotalPaidByOrderId(selectedInvoice.getId());
            double remaining = selectedInvoice.getTotalAmount() - paid;
            if (remaining <= 0.01) {
                invoiceBus.syncPaymentStatus(selectedInvoice);
                updateActionButtons(selectedInvoice);
                showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Hóa đơn này đã thanh toán đủ.");
                handleSearch(null);
                return;
            }
            Optional<PaymentInput> input = showPaymentDialog(remaining);

            if (input.isEmpty()) {
                return;
            }

            invoiceBus.payInvoice(selectedInvoice, input.get().method, input.get().amount);
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Ghi nhận thanh toán thành công.");
            handleSearch(null);
        } catch (IllegalArgumentException ex) {
            showAlert(Alert.AlertType.WARNING, "Dữ liệu không hợp lệ", ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể thanh toán hóa đơn: " + ex.getMessage());
        }
    }

    @FXML
    public void onCancelInvoice(ActionEvent event) {
        Invoice selectedInvoice = tableInvoice.getSelectionModel().getSelectedItem();

        if (selectedInvoice == null) {
            showAlert(Alert.AlertType.WARNING, "Thông báo", "Vui lòng chọn một hóa đơn để hủy.");
            return;
        }

        if (!isCancelable(selectedInvoice)) {
            showAlert(Alert.AlertType.WARNING, "Thông báo", "Không thể hủy hóa đơn có trạng thái " + selectedInvoice.getStatus());
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận hủy hóa đơn");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn hủy hóa đơn " + selectedInvoice.getId() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            invoiceBus.cancelInvoice(selectedInvoice);
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã hủy hóa đơn " + selectedInvoice.getId() + ".");
            handleSearch(null);
        } catch (IllegalArgumentException ex) {
            showAlert(Alert.AlertType.WARNING, "Không thể hủy", ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể hủy hóa đơn: " + ex.getMessage());
        }
    }

    @FXML
    public void handleViewDetail(ActionEvent event) {
        showSelectedInvoiceDetail();
    }

    @FXML
public void onTableClick(MouseEvent event) {
    System.out.println("DEBUG 1: Da click tableInvoice");

    if (invoicePreview == null) {
        System.out.println("DEBUG 2: invoicePreview NULL");
        showAlert(Alert.AlertType.ERROR, "Lỗi", "invoicePreview đang NULL");
        return;
    }

    Invoice selectedInvoice = tableInvoice.getSelectionModel().getSelectedItem();

    if (selectedInvoice == null) {
        System.out.println("DEBUG 3: selectedInvoice NULL");
        showAlert(Alert.AlertType.WARNING, "Thông báo", "Chưa chọn hóa đơn");
        return;
    }

    System.out.println("DEBUG 4: Hoa don dang chon = " + selectedInvoice.getId());

    showSelectedInvoiceDetail();

    System.out.println("DEBUG 5: Da render chi tiet hoa don");
}

    @FXML
    public void onPrint(ActionEvent event) {
        System.out.println("Chức năng in hóa đơn sẽ làm sau.");
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng in hóa đơn sẽ làm sau.");
    }

    @FXML
    public void onFilter(ActionEvent event) {
        handleSearch(event);
    }

    @FXML
    public void handlePrintInvoice(ActionEvent event) {
        onPrint(event);
    }

    private void showSelectedInvoiceDetail() {
    System.out.println("DEBUG: Đã gọi showSelectedInvoiceDetail");

    Invoice selectedInvoice = tableInvoice.getSelectionModel().getSelectedItem();

    if (selectedInvoice == null) {
        showAlert(Alert.AlertType.WARNING, "Thông báo", "Vui lòng chọn hóa đơn để xem chi tiết.");
        return;
    }

    try {
        List<InvoiceDetail> details = invoiceBus.getInvoiceDetailsByOrderId(selectedInvoice.getId());
        renderInvoiceDetail(selectedInvoice, details);
    } catch (Exception ex) {
        ex.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể tải chi tiết hóa đơn: " + ex.getMessage());
    }
}

    private void renderInvoiceDetail(Invoice invoice, List<InvoiceDetail> details) {
        if (invoicePreview == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi giao diện", "Không tìm thấy vùng hiển thị chi tiết hóa đơn.");
            return;
        }

        DecimalFormat moneyFormat = new DecimalFormat("#,###");

        invoicePreview.getChildren().clear();

        Label title = new Label("Chi Tiết Hóa Đơn");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label orderId = new Label("Mã hóa đơn: " + invoice.getId());
        Label customerId = new Label("Mã khách hàng: " + invoice.getCustomerId());
        Label createdAt = new Label("Ngày tạo: " + invoice.getCreateDate());
        Label status = new Label("Trạng thái: " + invoice.getStatus());
        Label total = new Label("Tổng tiền: " + moneyFormat.format(invoice.getTotalAmount()) + " VNĐ");

        invoicePreview.getChildren().addAll(
            title,
            orderId,
            customerId,
            createdAt,
            status,
            total,
            new Separator()
        );

        if (details == null || details.isEmpty()) {
            Label empty = new Label("Hóa đơn này chưa có khoản phí chi tiết.");
            empty.setWrapText(true);
            invoicePreview.getChildren().add(empty);
            return;
        }

        Label detailTitle = new Label("Các khoản phí:");
        detailTitle.setStyle("-fx-font-weight: bold;");
        invoicePreview.getChildren().add(detailTitle);

        double detailTotal = 0;
        for (InvoiceDetail detail : details) {
            detailTotal += detail.getLineTotal();
            StringBuilder itemText = new StringBuilder("- Mã chi tiết: ")
                .append(detail.getDetailId());

            appendIfPresent(itemText, "Phòng booking", detail.getBookingRoomId());
            appendIfPresent(itemText, "Dịch vụ booking", detail.getBookingServiceId());
            appendIfPresent(itemText, "Mô tả", detail.getNote());

            itemText
                .append("\n  Số lượng: ").append(formatQuantity(detail.getQuantity()))
                .append("\n  Đơn giá: ").append(moneyFormat.format(detail.getUnitPrice())).append(" VNĐ")
                .append("\n  Thành tiền: ").append(moneyFormat.format(detail.getLineTotal())).append(" VNĐ");

            Label itemLabel = new Label(itemText.toString());
            itemLabel.setWrapText(true);

            invoicePreview.getChildren().addAll(itemLabel, new Separator());
        }

        if (Math.abs(detailTotal - invoice.getTotalAmount()) > 0.01) {
            Label warning = new Label("Lưu ý: Tổng chi tiết chưa khớp tổng hóa đơn.");
            warning.setWrapText(true);
            warning.setStyle("-fx-text-fill: #a15c00; -fx-font-weight: bold;");
            invoicePreview.getChildren().add(warning);
        }
    }

    private void appendIfPresent(StringBuilder builder, String label, String value) {
        if (value != null && !value.trim().isEmpty()) {
            builder.append("\n  ").append(label).append(": ").append(value.trim());
        }
    }

    private String formatQuantity(double quantity) {
        if (quantity == Math.rint(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }

    private Optional<PaymentInput> showPaymentDialog(double remaining) {
        DecimalFormat moneyFormat = new DecimalFormat("#,###");
        Dialog<PaymentInput> dialog = new Dialog<>();
        dialog.setTitle("Thanh toán hóa đơn");
        dialog.setHeaderText("Số tiền còn lại: " + moneyFormat.format(Math.max(remaining, 0)) + " VNĐ");

        ButtonType payButtonType = new ButtonType("Thanh toán", ButtonType.OK.getButtonData());
        dialog.getDialogPane().getButtonTypes().addAll(payButtonType, ButtonType.CANCEL);

        ComboBox<String> methodBox = new ComboBox<>(FXCollections.observableArrayList(
            "CASH",
            "BANK_TRANSFER",
            "CARD",
            "EWALLET"
        ));
        methodBox.setValue("CASH");
        methodBox.setMaxWidth(Double.MAX_VALUE);

        TextField amountField = new TextField();
        amountField.setPromptText("Nhập số tiền");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Phương thức:"), 0, 0);
        grid.add(methodBox, 1, 0);
        grid.add(new Label("Số tiền:"), 0, 1);
        grid.add(amountField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button == payButtonType) {
                double amount = Double.parseDouble(amountField.getText().trim());
                return new PaymentInput(methodBox.getValue(), amount);
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private void updateActionButtons(Invoice invoice) {
        boolean payable = isPayable(invoice) && hasRemainingAmount(invoice);
        boolean cancelable = isCancelable(invoice);

        if (btnPay != null) {
            btnPay.setDisable(!payable);
        }
        if (btnCancelInvoice != null) {
            btnCancelInvoice.setDisable(!cancelable);
        }
    }

    private boolean isPayable(Invoice invoice) {
        return invoice != null
            && ("PENDING".equalsIgnoreCase(invoice.getStatus())
                || "PARTIAL".equalsIgnoreCase(invoice.getStatus()));
    }

    private boolean isCancelable(Invoice invoice) {
        return isPayable(invoice);
    }

    private boolean hasRemainingAmount(Invoice invoice) {
        try {
            return invoice != null && invoiceBus.getRemainingAmount(invoice) > 0.01;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private static class PaymentInput {
        private final String method;
        private final double amount;

        private PaymentInput(String method, double amount) {
            this.method = method;
            this.amount = amount;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
