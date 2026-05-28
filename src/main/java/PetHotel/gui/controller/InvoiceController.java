package PetHotel.gui.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import PetHotel.bus.InvoiceBUS;
import PetHotel.model.Invoice;
import PetHotel.model.InvoiceDetail;
import PetHotel.model.Payment;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class InvoiceController {

    @FXML
    private TextField txtSearch;

    @FXML
    private TableView<Invoice> tableInvoice;

    @FXML
    private TableColumn<Invoice, String> colOrderId;

    @FXML
    private TableColumn<Invoice, String> colCustomer;

    @FXML
    private TableColumn<Invoice, Date> colCreatedDate;

    @FXML
    private TableColumn<Invoice, Double> colGrandTotal;

    @FXML
    private TableColumn<Invoice, String> colStatus;

    private VBox invoicePreview;

    @FXML
    private ComboBox<String> cbbStatusFilter;

    @FXML
    private Button btnPay;

    @FXML
    private Button btnCancelInvoice;

    @FXML
    private Button btnPrint;

    @FXML
    private Button btnPaymentHistory;

    private final InvoiceBUS invoiceBus;

    public InvoiceController() {
        invoiceBus = new InvoiceBUS();
    }

    @FXML
    public void initialize() {
        System.out.println("Đã load giao diện Quản lý Hóa Đơn");

        if (colOrderId != null) {
            colOrderId.setPrefWidth(110);
            colOrderId.setCellValueFactory(new PropertyValueFactory<>("id"));
        }

        if (colCustomer != null) {
            colCustomer.setPrefWidth(170);
            colCustomer.setCellValueFactory(cellData -> {
                Invoice invoice = cellData.getValue();
                String customerName = invoice.getCustomerName();
                String customer = customerName == null || customerName.trim().isEmpty()
                    ? invoice.getCustomerId()
                    : customerName;
                return new ReadOnlyStringWrapper(customer);
            });
        }

        if (colCreatedDate != null) {
            colCreatedDate.setPrefWidth(180);
            colCreatedDate.setCellValueFactory(new PropertyValueFactory<>("createDate"));
            colCreatedDate.setCellFactory(column -> new TableCell<>() {
                private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                @Override
                protected void updateItem(Date item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : dateFormat.format(item));
                }
            });
        }

        if (colGrandTotal != null) {
            colGrandTotal.setPrefWidth(150);
            colGrandTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
            colGrandTotal.setCellFactory(column -> new TableCell<>() {
                private final DecimalFormat moneyFormat = new DecimalFormat("#,###");

                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : moneyFormat.format(item));
                }
            });
        }

        if (colStatus != null) {
            colStatus.setPrefWidth(150);
            colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        }

        if (tableInvoice != null) {
            tableInvoice.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
            tableInvoice.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    updateActionButtons(newSelection);
                } else {
                    updateActionButtons(null);
                }
            });
            tableInvoice.setRowFactory(tv -> {
                TableRow<Invoice> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        showInvoiceDetailDialog(row.getItem());
                    }
                });
                return row;
            });
        }

        if (cbbStatusFilter != null) {
            cbbStatusFilter.setItems(FXCollections.observableArrayList(
                "Trạng thái",
                "PENDING",
                "PARTIAL",
                "PAID",
                "CANCELLED"
            ));
            cbbStatusFilter.setValue("Trạng thái");
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
        String status = getSelectedStatusFilter();

        System.out.println("Tìm kiếm hóa đơn: " + keyword);

        try {
            List<Invoice> results = invoiceBus.searchInvoices(
                keyword.isEmpty() ? null : keyword,
                null,
                null,
                null,
                status
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

    private String getSelectedStatusFilter() {
        if (cbbStatusFilter == null || cbbStatusFilter.getValue() == null) {
            return null;
        }

        String status = cbbStatusFilter.getValue().trim();
        return status.isEmpty() || "Trạng thái".equals(status) ? null : status;
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
            showAlert(Alert.AlertType.WARNING, "Thông báo", "Vui lòng chọn hóa đơn cần hủy.");
            return;
        }

        String status = selectedInvoice.getStatus();
        if ("PAID".equalsIgnoreCase(status)) {
            showAlert(Alert.AlertType.WARNING, "Thông báo", "Không thể hủy hóa đơn đã thanh toán.");
            return;
        }
        if ("CANCELLED".equalsIgnoreCase(status)) {
            showAlert(Alert.AlertType.WARNING, "Thông báo", "Hóa đơn này đã bị hủy.");
            return;
        }
        if ("REFUNDED".equalsIgnoreCase(status)) {
            showAlert(Alert.AlertType.WARNING, "Thông báo", "Không thể hủy hóa đơn đã hoàn tiền.");
            return;
        }
        if (!isCancelable(selectedInvoice)) {
            showAlert(Alert.AlertType.WARNING, "Thông báo", "Không thể hủy hóa đơn có trạng thái " + status);
            return;
        }

        boolean confirm = showCancelInvoiceConfirm(selectedInvoice.getId());
        if (!confirm) {
            return;
        }

        try {
            invoiceBus.cancelInvoice(selectedInvoice.getId());
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã hủy hóa đơn " + selectedInvoice.getId() + ".");
            handleSearch(null);
            tableInvoice.getSelectionModel().clearSelection();
        } catch (IllegalArgumentException ex) {
            showAlert(Alert.AlertType.WARNING, "Không thể hủy", ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể hủy hóa đơn: " + ex.getMessage());
        }
    }

    @FXML
    public void handleViewDetail(ActionEvent event) {
        Invoice selectedInvoice = tableInvoice.getSelectionModel().getSelectedItem();
        if (selectedInvoice == null) {
            showAlert(Alert.AlertType.WARNING, "Thông báo", "Vui lòng chọn hóa đơn cần xem chi tiết.");
            return;
        }
        showInvoiceDetailDialog(selectedInvoice);
    }

    @FXML
    public void onPrint(ActionEvent event) {
        Invoice selectedInvoice = tableInvoice.getSelectionModel().getSelectedItem();

        if (selectedInvoice == null) {
            showAlert(Alert.AlertType.WARNING, "Thông báo", "Vui lòng chọn hóa đơn cần in/xuất.");
            return;
        }

        try {
            List<InvoiceDetail> details = invoiceBus.getInvoiceDetailsByOrderId(selectedInvoice.getId());
            List<Payment> payments = invoiceBus.getPaymentsByOrderId(selectedInvoice.getId());
            Path exportFile = exportInvoiceText(selectedInvoice, details, payments);

            System.out.println("Exported invoice: " + exportFile.toAbsolutePath());
            showExportSuccessDialog();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể xuất hóa đơn: " + ex.getMessage());
        }
    }

    @FXML
    public void onFilter(ActionEvent event) {
        handleSearch(event);
    }

    @FXML
    public void onPaymentHistory(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PetHotel/gui/view/PaymentHistory.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Lịch Sử Giao Dịch");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể mở lịch sử giao dịch: " + ex.getMessage());
        }
    }

    @FXML
    public void handlePrintInvoice(ActionEvent event) {
        onPrint(event);
    }

    private void showInvoiceDetailDialog(Invoice invoice) {
        if (invoice == null) {
            return;
        }

        List<InvoiceDetail> details;
        try {
            details = invoiceBus.getInvoiceDetailsByOrderId(invoice.getId());
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể tải chi tiết hóa đơn: " + ex.getMessage());
            return;
        }

        DecimalFormat moneyFormat = new DecimalFormat("#,###");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chi tiết hóa đơn");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes().add(new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE));
        dialog.getDialogPane().setStyle("-fx-background-color: #fffaf5; -fx-padding: 18;");

        GridPane summary = new GridPane();
        summary.setHgap(14);
        summary.setVgap(8);
        summary.setStyle(
            "-fx-background-color: #fff8f1;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #ead8c7;" +
            "-fx-border-radius: 12;" +
            "-fx-padding: 16;"
        );
        summary.add(summaryItem("Mã hóa đơn", valueOrDash(invoice.getId())), 0, 0);
        summary.add(summaryItem("Khách hàng", valueOrDash(customerDisplay(invoice))), 1, 0);
        summary.add(summaryItem("Ngày tạo", invoice.getCreateDate() == null ? "-" : dateFormat.format(invoice.getCreateDate())), 2, 0);
        summary.add(summaryItem("Trạng thái", valueOrDash(invoice.getStatus())), 0, 1);
        summary.add(summaryItem("Tổng tiền", moneyFormat.format(invoice.getTotalAmount()) + " VND"), 1, 1);

        Label title = new Label("Chi tiết hóa đơn");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c1a0e;");

        Label subtitle = new Label("Double click một hóa đơn để xem các khoản phí chi tiết trong bảng này.");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #8b7665;");

        Label detailTitle = new Label("Các khoản phí");
        detailTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #7b3f22;");

        VBox header = new VBox(4, title, subtitle);
        VBox content = new VBox(14, header, summary, detailTitle);
        content.setPrefWidth(860);
        content.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 22;");

        if (details == null || details.isEmpty()) {
            Label empty = new Label("Hóa đơn này chưa có khoản phí chi tiết.");
            empty.setWrapText(true);
            empty.setStyle(
                "-fx-text-fill: #7a6355;" +
                "-fx-background-color: #fff8f1;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #ead8c7;" +
                "-fx-border-radius: 10;" +
                "-fx-padding: 18;"
            );
            content.getChildren().add(empty);
        } else {
            TableView<InvoiceDetail> detailTable = new TableView<>();
            detailTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
            detailTable.setPrefHeight(320);
            detailTable.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #ead8c7;" +
                "-fx-border-radius: 10;" +
                "-fx-border-width: 1;"
            );

            TableColumn<InvoiceDetail, String> detailIdCol = new TableColumn<>("Mã chi tiết");
            detailIdCol.setPrefWidth(110);
            detailIdCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(valueOrDash(cell.getValue().getDetailId())));

            TableColumn<InvoiceDetail, String> descriptionCol = new TableColumn<>("Mô tả");
            descriptionCol.setPrefWidth(250);
            descriptionCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(detailDescription(cell.getValue())));

            TableColumn<InvoiceDetail, String> quantityCol = new TableColumn<>("Số lượng");
            quantityCol.setPrefWidth(100);
            quantityCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(formatQuantity(cell.getValue().getQuantity())));

            TableColumn<InvoiceDetail, Double> unitPriceCol = new TableColumn<>("Đơn giá");
            unitPriceCol.setPrefWidth(130);
            unitPriceCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getUnitPrice()));
            unitPriceCol.setCellFactory(column -> moneyCell(moneyFormat));

            TableColumn<InvoiceDetail, Double> lineTotalCol = new TableColumn<>("Thành tiền");
            lineTotalCol.setPrefWidth(150);
            lineTotalCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getLineTotal()));
            lineTotalCol.setCellFactory(column -> moneyCell(moneyFormat));

            detailTable.getColumns().addAll(detailIdCol, descriptionCol, quantityCol, unitPriceCol, lineTotalCol);
            detailTable.setItems(FXCollections.observableArrayList(details));
            content.getChildren().add(detailTable);
        }

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private TableCell<InvoiceDetail, Double> moneyCell(DecimalFormat moneyFormat) {
        return new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : moneyFormat.format(item));
            }
        };
    }

    private VBox summaryItem(String labelText, String valueText) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 11px; -fx-text-fill: #8b7665; -fx-font-weight: bold;");

        Label value = new Label(valueOrDash(valueText));
        value.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c1a0e; -fx-font-weight: bold;");

        VBox box = new VBox(4, label, value);
        box.setMinWidth(220);
        box.setStyle("-fx-padding: 6 10 6 10;");
        return box;
    }

    private String customerDisplay(Invoice invoice) {
        if (invoice == null) {
            return "";
        }
        String customerName = invoice.getCustomerName();
        return customerName == null || customerName.trim().isEmpty()
            ? invoice.getCustomerId()
            : customerName;
    }

    private String detailDescription(InvoiceDetail detail) {
        if (detail == null) {
            return "-";
        }
        if (detail.getNote() != null && !detail.getNote().trim().isEmpty()) {
            return detail.getNote().trim();
        }
        if (detail.getBookingRoomId() != null && !detail.getBookingRoomId().trim().isEmpty()) {
            return "Phí phòng " + detail.getBookingRoomId().trim();
        }
        if (detail.getBookingServiceId() != null && !detail.getBookingServiceId().trim().isEmpty()) {
            return "Dịch vụ " + detail.getBookingServiceId().trim();
        }
        return "Khoản phí";
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    private void showSelectedInvoiceDetail() {
    System.out.println("DEBUG: Đã gọi showSelectedInvoiceDetail");

    Invoice selectedInvoice = tableInvoice.getSelectionModel().getSelectedItem();

    if (selectedInvoice == null) {
        resetInvoicePreview();
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

    private void resetInvoicePreview() {
        if (invoicePreview == null) {
            return;
        }

        invoicePreview.getChildren().clear();

        Label title = new Label("Chi Tiết Hóa Đơn");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label empty = new Label("Double click vào hóa đơn để xem chi tiết.");
        empty.setWrapText(true);
        empty.setStyle("-fx-text-fill: -ph-text-muted; -fx-font-size: 12px;");

        invoicePreview.getChildren().addAll(title, empty);
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

    private boolean showCancelInvoiceConfirm(String orderId) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Xác nhận hủy hóa đơn");
        dialog.setHeaderText(null);

        ButtonType confirmButtonType = new ButtonType("Xác nhận hủy", ButtonBar.ButtonData.OK_DONE);
        ButtonType exitButtonType = new ButtonType("Thoát", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, exitButtonType);

        Label title = new Label("Hủy hóa đơn");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #991b1b;");

        Label question = new Label("Bạn có chắc muốn hủy hóa đơn " + orderId + " không?");
        question.setWrapText(true);
        question.setStyle("-fx-font-size: 13px; -fx-text-fill: #1f2937;");

        Label warning = new Label("Hành động này sẽ chuyển trạng thái hóa đơn sang CANCELLED.");
        warning.setWrapText(true);
        warning.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f1d1d;");

        VBox content = new VBox(10, title, question, warning);
        content.setStyle(
            "-fx-background-color: #fff7f7;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 18;" +
            "-fx-border-color: #fecaca;" +
            "-fx-border-radius: 10;"
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setStyle("-fx-background-color: white; -fx-padding: 14;");

        Button confirmButton = (Button) dialog.getDialogPane().lookupButton(confirmButtonType);
        confirmButton.setStyle(
            "-fx-background-color: #dc2626;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 16;"
        );

        Button exitButton = (Button) dialog.getDialogPane().lookupButton(exitButtonType);
        exitButton.setStyle(
            "-fx-background-color: #f3f4f6;" +
            "-fx-text-fill: #6b7280;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 16;"
        );

        dialog.setResultConverter(button -> button == confirmButtonType);
        return dialog.showAndWait().orElse(false);
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
        if (btnPrint != null) {
            btnPrint.setDisable(invoice == null);
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

    private void showExportSuccessDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Xuất hóa đơn thành công");
        dialog.setHeaderText(null);

        ButtonType closeButtonType = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);

        Label icon = new Label("✓");
        icon.setStyle(
            "-fx-font-size: 34px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #16a34a;"
        );

        Label title = new Label("Xuất hóa đơn thành công");
        title.setStyle(
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #8b5a2b;"
        );

        Label message = new Label("Hóa đơn đã được xuất thành công.");
        message.setWrapText(true);
        message.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        VBox content = new VBox(10, icon, title, message);
        content.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 22;" +
            "-fx-min-width: 420;"
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setStyle("-fx-background-color: white; -fx-padding: 12;");

        Button closeButton = (Button) dialog.getDialogPane().lookupButton(closeButtonType);
        closeButton.setStyle(
            "-fx-background-color: #b86b2b;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 18;"
        );

        dialog.showAndWait();
    }

    private Path exportInvoiceText(Invoice invoice, List<InvoiceDetail> details, List<Payment> payments)
            throws IOException {
        DecimalFormat moneyFormat = new DecimalFormat("#,###");
        double totalPaid = 0;
        StringBuilder paymentMethods = new StringBuilder();

        if (payments != null) {
            for (Payment payment : payments) {
                if ("SUCCESS".equalsIgnoreCase(payment.getStatus())) {
                    totalPaid += payment.getAmount();
                    if (paymentMethods.length() > 0) {
                        paymentMethods.append(", ");
                    }
                    paymentMethods.append(payment.getPaymentMethod());
                }
            }
        }

        StringBuilder content = new StringBuilder();
        content.append("HOA DON PET HOTEL\n");
        content.append("=================\n\n");
        content.append("Ma hoa don: ").append(invoice.getId()).append("\n");
        content.append("Ma khach hang: ").append(invoice.getCustomerId()).append("\n");
        content.append("Ngay tao: ").append(invoice.getCreateDate()).append("\n");
        content.append("Trang thai: ").append(invoice.getStatus()).append("\n\n");
        content.append("DANH SACH KHOAN PHI\n");
        content.append("-------------------\n");

        if (details == null || details.isEmpty()) {
            content.append("Khong co chi tiet phi.\n");
        } else {
            for (InvoiceDetail detail : details) {
                content.append("- Mo ta: ").append(emptyToDefault(detail.getNote(), "Khoan phi")).append("\n");
                content.append("  So luong: ").append(formatQuantity(detail.getQuantity())).append("\n");
                content.append("  Don gia: ").append(moneyFormat.format(detail.getUnitPrice())).append(" VND\n");
                content.append("  Thanh tien: ").append(moneyFormat.format(detail.getLineTotal())).append(" VND\n");
            }
        }

        content.append("\nTONG TIEN: ").append(moneyFormat.format(invoice.getTotalAmount())).append(" VND\n");
        content.append("DA THANH TOAN: ").append(moneyFormat.format(totalPaid)).append(" VND\n");
        content.append("CON LAI: ").append(moneyFormat.format(Math.max(invoice.getTotalAmount() - totalPaid, 0))).append(" VND\n");
        content.append("PHUONG THUC THANH TOAN: ")
            .append(paymentMethods.length() == 0 ? "Chua co" : paymentMethods)
            .append("\n\n");
        content.append("Cam on quy khach da su dung dich vu Pet Hotel.\n");

        Path exportDir = Path.of("exports");
        Files.createDirectories(exportDir);
        Path exportFile = exportDir.resolve("invoice_" + invoice.getId() + ".txt");
        Files.writeString(exportFile, content.toString(), StandardCharsets.UTF_8);
        return exportFile;
    }

    private String emptyToDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
