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
import javafx.animation.PauseTransition;
import javafx.application.Platform;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

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
    private TableColumn<Invoice, String> colCustomerPhone;

    @FXML
    private TableColumn<Invoice, Date> colCreatedDate;

    @FXML
    private TableColumn<Invoice, String> colCreatedByEmp;

    @FXML
    private TableColumn<Invoice, Double> colGrandTotal;

    @FXML
    private TableColumn<Invoice, Double> colRemainingAmount;

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
            colOrderId.setPrefWidth(90);
            colOrderId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colOrderId.setCellFactory(column -> stringCell(Pos.CENTER));
        }

        if (colCustomer != null) {
            colCustomer.setPrefWidth(180);
            colCustomer.setCellValueFactory(cellData -> {
                Invoice invoice = cellData.getValue();
                String customerName = invoice.getCustomerName();
                String customer = customerName == null || customerName.trim().isEmpty()
                    ? invoice.getCustomerId()
                    : customerName;
                return new ReadOnlyStringWrapper(customer);
            });
            colCustomer.setCellFactory(column -> stringCell(Pos.CENTER_LEFT));
        }

        if (colCustomerPhone != null) {
            colCustomerPhone.setPrefWidth(120);
            colCustomerPhone.setCellValueFactory(new PropertyValueFactory<>("customerPhone"));
            colCustomerPhone.setCellFactory(column -> stringCell(Pos.CENTER));
        }

        if (colCreatedDate != null) {
            colCreatedDate.setPrefWidth(150);
            colCreatedDate.setCellValueFactory(new PropertyValueFactory<>("createDate"));
            colCreatedDate.setCellFactory(column -> new TableCell<>() {
                private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                @Override
                protected void updateItem(Date item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : dateFormat.format(item));
                    setAlignment(Pos.CENTER);
                }
            });
        }

        if (colCreatedByEmp != null) {
            colCreatedByEmp.setPrefWidth(110);
            colCreatedByEmp.setCellValueFactory(new PropertyValueFactory<>("createdByEmp"));
            colCreatedByEmp.setCellFactory(column -> stringCell(Pos.CENTER));
        }

        if (colGrandTotal != null) {
            colGrandTotal.setPrefWidth(120);
            colGrandTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
            colGrandTotal.setCellFactory(column -> new TableCell<>() {
                private final DecimalFormat moneyFormat = new DecimalFormat("#,###");

                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : moneyFormat.format(item));
                    setAlignment(Pos.CENTER_RIGHT);
                }
            });
        }

        if (colRemainingAmount != null) {
            colRemainingAmount.setPrefWidth(120);
            colRemainingAmount.setCellValueFactory(new PropertyValueFactory<>("remainingAmount"));
            colRemainingAmount.setCellFactory(column -> new TableCell<>() {
                private final DecimalFormat moneyFormat = new DecimalFormat("#,###");

                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : moneyFormat.format(item));
                    setAlignment(Pos.CENTER_RIGHT);
                }
            });
        }

        if (colStatus != null) {
            colStatus.setPrefWidth(160);
            colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
            colStatus.setCellFactory(column -> new TableCell<>() {
                private final Label badge = new Label();

                {
                    badge.getStyleClass().add("invoice-status-badge");
                    setAlignment(Pos.CENTER);
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    badge.getStyleClass().removeAll(
                        "status-paid",
                        "status-cancelled",
                        "status-partial",
                        "status-pending",
                        "status-default"
                    );

                    if (empty || item == null || item.trim().isEmpty()) {
                        setGraphic(null);
                        setText(null);
                        return;
                    }

                    String databaseStatus = item.trim().toUpperCase();
                    badge.setText(toVietnameseStatus(databaseStatus));
                    badge.getStyleClass().add(invoiceStatusClass(databaseStatus));
                    setText(null);
                    setGraphic(badge);
                }
            });
        }

        if (tableInvoice != null) {
            tableInvoice.getStyleClass().remove("data-table");
            if (!tableInvoice.getStyleClass().contains("invoice-table")) {
                tableInvoice.getStyleClass().add("invoice-table");
            }
            tableInvoice.setStyle("");
            tableInvoice.setFocusTraversable(false);
            tableInvoice.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
            tableInvoice.setPlaceholder(new Label(""));
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
                "Tất cả trạng thái",
                "Chờ thanh toán",
                "Thanh toán một phần",
                "Đã thanh toán",
                "Đã hủy"
            ));
            cbbStatusFilter.setValue("Tất cả trạng thái");
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
        return status.isEmpty() || "Tất cả trạng thái".equals(status) || "Tất cả".equals(status)
            ? null
            : toDatabaseStatus(status);
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
            showAlert(Alert.AlertType.WARNING, "Thông báo", "Không thể thanh toán hóa đơn có trạng thái " + toVietnameseStatus(selectedInvoice.getStatus()));
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
            showAutoCloseSuccess("Thanh toán thành công");
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
            showAlert(Alert.AlertType.WARNING, "Thông báo", "Hóa đơn đã thanh toán, không thể hủy.");
            return;
        }
        if ("PARTIAL".equalsIgnoreCase(status)) {
            showAlert(Alert.AlertType.WARNING, "Thông báo", "Hóa đơn đã thanh toán một phần, không thể hủy trực tiếp. Vui lòng thanh toán tiếp hoặc liên hệ quản lý.");
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
            showAlert(Alert.AlertType.WARNING, "Thông báo", "Không thể hủy hóa đơn có trạng thái " + toVietnameseStatus(status));
            return;
        }

        Optional<String> cancelReason = showCancelInvoiceDialog(selectedInvoice.getId());
        if (cancelReason.isEmpty()) {
            return;
        }

        try {
            invoiceBus.cancelInvoice(selectedInvoice.getId(), cancelReason.get());
            showAutoCloseSuccess("Đã hủy hóa đơn thành công");
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
            Invoice latestInvoice = invoiceBus.getInvoiceById(invoice.getId());
            if (latestInvoice != null) {
                invoice = latestInvoice;
            }
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
        java.net.URL stylesheet = getClass().getResource("/PetHotel/gui/css/style.css");
        if (stylesheet != null) {
            dialog.getDialogPane().getStylesheets().add(stylesheet.toExternalForm());
        }
        dialog.getDialogPane().setStyle("-fx-background-color: #fffaf5; -fx-padding: 18;");

        double prepaidAmount = Math.max(invoice.getPrepaidAmount(), 0);
        double paidAmount = Math.max(invoice.getPaidAmount(), 0);
        double remainingAmount = remainingForDetail(invoice);
        String remainingColor = remainingAmount > 0 ? "#A64B2A" : "#166534";

        VBox summary = new VBox(10);
        summary.setStyle(
            "-fx-background-color: #fff8f1;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #ead8c7;" +
            "-fx-border-radius: 12;" +
            "-fx-padding: 16;"
        );

        GridPane invoiceInfo = summaryGrid();
        invoiceInfo.add(summaryItem("Mã hóa đơn", valueOrDash(invoice.getId())), 0, 0);
        invoiceInfo.add(summaryItem("Ngày tạo", invoice.getCreateDate() == null ? "-" : dateFormat.format(invoice.getCreateDate())), 1, 0);
        invoiceInfo.add(summaryItem("Trạng thái", valueOrDash(toVietnameseStatus(invoice.getStatus()))), 2, 0);

        GridPane peopleInfo = summaryGrid();
        peopleInfo.add(summaryItem("Mã khách hàng", valueOrDash(invoice.getCustomerId())), 0, 0);
        peopleInfo.add(summaryItem("Khách hàng", valueOrDash(customerDisplay(invoice))), 1, 0);
        peopleInfo.add(summaryItem("Số điện thoại", valueOrDash(invoice.getCustomerPhone())), 2, 0);
        peopleInfo.add(summaryItem("Mã nhân viên lập", valueOrDash(invoice.getCreatedByEmp())), 0, 1);
        peopleInfo.add(summaryItem("Tên nhân viên lập", valueOrDash(invoice.getCreatedByEmpName())), 1, 1);

        GridPane paymentInfo = summaryGrid();
        paymentInfo.add(summaryItem("Tổng tiền", formatMoneyVnd(invoice.getTotalAmount())), 0, 0);
        paymentInfo.add(summaryItem("Số tiền trả trước", formatMoneyVnd(prepaidAmount)), 1, 0);
        paymentInfo.add(summaryItem("Số tiền đã thanh toán", formatMoneyVnd(paidAmount)), 2, 0);
        paymentInfo.add(summaryItem("Số tiền còn lại", formatMoneyVnd(remainingAmount),
            "-fx-font-size: 16px; -fx-text-fill: " + remainingColor + "; -fx-font-weight: bold;"), 0, 1);

        summary.getChildren().addAll(
            summaryGroup("Thông tin hóa đơn", invoiceInfo),
            summaryGroup("Khách hàng & nhân viên", peopleInfo),
            summaryGroup("Thanh toán", paymentInfo)
        );

        Label title = new Label("Chi tiết hóa đơn");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c1a0e;");

        Label subtitle = new Label("Xem thông tin hóa đơn, khách hàng, thanh toán và các khoản phí.");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #8b7665;");

        Button closeButton = new Button("Đóng");
        closeButton.setStyle(
            "-fx-background-color: #fffdf9;" +
            "-fx-text-fill: #6B5B4D;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: #E5D3C3;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 7 16 7 16;"
        );
        closeButton.setOnAction(event -> {
            if (closeButton.getScene() != null && closeButton.getScene().getWindow() != null) {
                closeButton.getScene().getWindow().hide();
            } else {
                dialog.setResult(null);
                dialog.close();
            }
        });

        Label detailTitle = new Label("Các khoản phí");
        detailTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #7b3f22;");

        VBox headerText = new VBox(4, title, subtitle);
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, javafx.scene.layout.Priority.ALWAYS);
        HBox header = new HBox(12, headerText, headerSpacer, closeButton);
        header.setAlignment(Pos.CENTER_LEFT);
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
            detailTable.getStyleClass().add("invoice-detail-table");
            detailTable.setFocusTraversable(false);

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
            detailTable.setPlaceholder(new Label(""));
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
                setAlignment(Pos.CENTER_RIGHT);
            }
        };
    }

    private TableCell<Invoice, String> stringCell(Pos alignment) {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null || item.trim().isEmpty() ? null : item);
                setAlignment(alignment);
            }
        };
    }

    private String invoiceStatusClass(String status) {
        return switch (status) {
            case "PAID", "COMPLETED" -> "status-paid";
            case "CANCELLED", "CANCELED" -> "status-cancelled";
            case "PARTIAL" -> "status-partial";
            case "PENDING" -> "status-pending";
            default -> "status-default";
        };
    }

    private String toVietnameseStatus(String status) {
        if (status == null) {
            return "";
        }
        return switch (status.trim().toUpperCase()) {
            case "PENDING" -> "Chờ thanh toán";
            case "PAID" -> "Đã thanh toán";
            case "PARTIAL" -> "Thanh toán một phần";
            case "CANCELLED", "CANCELED" -> "Đã hủy";
            case "COMPLETED" -> "Hoàn tất";
            case "CONFIRMED" -> "Đã xác nhận";
            case "CHECKED_IN" -> "Đã nhận phòng";
            case "CHECKED_OUT" -> "Đã trả phòng";
            default -> status.trim();
        };
    }

    private String toDatabaseStatus(String vietnameseStatus) {
        if (vietnameseStatus == null) {
            return null;
        }
        return switch (vietnameseStatus.trim()) {
            case "Chờ thanh toán" -> "PENDING";
            case "Đã thanh toán" -> "PAID";
            case "Thanh toán một phần" -> "PARTIAL";
            case "Đã hủy" -> "CANCELLED";
            default -> vietnameseStatus.trim();
        };
    }

    private VBox summaryItem(String labelText, String valueText) {
        return summaryItem(labelText, valueText, "-fx-font-size: 14px; -fx-text-fill: #2c1a0e; -fx-font-weight: bold;");
    }

    private VBox summaryItem(String labelText, String valueText, String valueStyle) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 11px; -fx-text-fill: #8b7665; -fx-font-weight: bold;");

        Label value = new Label(valueOrDash(valueText));
        value.setStyle(valueStyle);

        VBox box = new VBox(4, label, value);
        box.setMinWidth(220);
        box.setStyle("-fx-padding: 6 10 6 10;");
        return box;
    }

    private GridPane summaryGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(8);
        return grid;
    }

    private VBox summaryGroup(String titleText, GridPane grid) {
        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 12px; -fx-text-fill: #7b3f22; -fx-font-weight: bold;");
        VBox group = new VBox(6, title, grid);
        group.setStyle(
            "-fx-background-color: #fffdf9;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #f0e1d3;" +
            "-fx-border-radius: 10;" +
            "-fx-padding: 10;"
        );
        return group;
    }

    private String formatMoneyVnd(double amount) {
        return new DecimalFormat("#,###").format(Math.max(amount, 0)) + " VNĐ";
    }

    private double remainingForDetail(Invoice invoice) {
        if (invoice == null) {
            return 0;
        }
        String status = invoice.getStatus() == null ? "" : invoice.getStatus().trim().toUpperCase();
        if ("PAID".equals(status) || "CANCELLED".equals(status) || "CANCELED".equals(status)) {
            return 0;
        }
        return Math.max(invoice.getTotalAmount() - invoice.getPaidAmount(), 0);
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
        Label status = new Label("Trạng thái: " + toVietnameseStatus(invoice.getStatus()));
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

        Label empty = new Label("Nhấp đúp vào hóa đơn để xem chi tiết.");
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
        if (vietnameseMethod == null || vietnameseMethod.trim().isEmpty()) {
            return "CASH";
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

    private Optional<PaymentInput> showPaymentDialog(double remaining) {
        DecimalFormat moneyFormat = new DecimalFormat("#,###");
        double remainingAmount = Math.max(remaining, 0);
        Dialog<PaymentInput> dialog = new Dialog<>();
        dialog.setTitle("Thanh toán hóa đơn");
        dialog.setHeaderText(null);

        ButtonType payButtonType = new ButtonType("Thanh toán", ButtonType.OK.getButtonData());
        ButtonType cancelButtonType = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(payButtonType, cancelButtonType);

        ComboBox<String> methodBox = new ComboBox<>(FXCollections.observableArrayList(
            "Tiền mặt",
            "Chuyển khoản",
            "Thẻ",
            "Ví điện tử"
        ));
        methodBox.setValue("Tiền mặt");
        methodBox.setMaxWidth(Double.MAX_VALUE);
        methodBox.setPrefHeight(38);
        methodBox.setPrefWidth(260);

        TextField amountField = new TextField();
        amountField.setPromptText("Nhập số tiền thanh toán");
        amountField.setPrefHeight(38);
        amountField.setPrefWidth(260);

        Label title = new Label("Thanh toán hóa đơn");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2B2118;");

        Label subtitle = new Label("Nhập số tiền và phương thức thanh toán cho hóa đơn đã chọn.");
        subtitle.setWrapText(true);
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B5B4D;");

        Label remainingCaption = new Label("Số tiền còn lại");
        remainingCaption.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #6B5B4D;");

        Label remainingValue = new Label(moneyFormat.format(remainingAmount) + " VNĐ");
        remainingValue.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #A64B2A;");

        VBox amountCard = new VBox(6, remainingCaption, remainingValue);
        amountCard.setStyle(
            "-fx-background-color: #FFF7F0;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #E5D3C3;" +
            "-fx-border-radius: 12;" +
            "-fx-padding: 16 18;"
        );

        Label methodLabel = new Label("Phương thức");
        methodLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2B2118;");

        Label amountLabel = new Label("Số tiền");
        amountLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2B2118;");

        Label errorLabel = new Label("");
        errorLabel.setWrapText(true);
        errorLabel.setMinHeight(18);
        errorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #B91C1C;");

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(10);
        grid.add(methodLabel, 0, 0);
        grid.add(methodBox, 1, 0);
        grid.add(amountLabel, 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(errorLabel, 1, 2);

        VBox content = new VBox(16, new VBox(4, title, subtitle), amountCard, grid);
        content.setPrefWidth(430);
        content.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 22;");

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(480);
        dialog.getDialogPane().setStyle("-fx-background-color: #FFFFFF;");

        Button payButton = (Button) dialog.getDialogPane().lookupButton(payButtonType);
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);
        payButton.setDisable(true);
        payButton.setStyle(
            "-fx-background-color: #A64B2A;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 18;" +
            "-fx-min-width: 112;"
        );
        cancelButton.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: #6B5B4D;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: #E5D3C3;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 18;" +
            "-fx-min-width: 112;"
        );

        amountField.textProperty().addListener((obs, oldValue, newValue) -> {
            String error = validatePaymentAmount(newValue, remainingAmount);
            errorLabel.setText(error == null ? "" : error);
            payButton.setDisable(error != null);
        });

        payButton.addEventFilter(ActionEvent.ACTION, event -> {
            String error = validatePaymentAmount(amountField.getText(), remainingAmount);
            if (error != null) {
                errorLabel.setText(error);
                event.consume();
            }
        });

        dialog.setResultConverter(button -> {
            if (button == payButtonType) {
                double amount = parsePaymentAmount(amountField.getText());
                return new PaymentInput(toDatabasePaymentMethod(methodBox.getValue()), amount);
            }
            return null;
        });

        Platform.runLater(amountField::requestFocus);
        return dialog.showAndWait();
    }

    private String validatePaymentAmount(String rawValue, double remainingAmount) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return "Vui lòng nhập số tiền thanh toán";
        }
        try {
            double amount = parsePaymentAmount(rawValue);
            if (amount <= 0) {
                return "Số tiền phải lớn hơn 0";
            }
            if (amount - remainingAmount > 0.01) {
                return "Số tiền thanh toán không được vượt quá số tiền còn lại";
            }
            return null;
        } catch (NumberFormatException ex) {
            return "Số tiền phải là số hợp lệ";
        }
    }

    private double parsePaymentAmount(String rawValue) {
        return Double.parseDouble(rawValue.trim().replace(",", ""));
    }

    private Optional<String> showCancelInvoiceDialog(String orderId) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Hủy hóa đơn");
        dialog.setHeaderText(null);

        ButtonType confirmButtonType = new ButtonType("Xác nhận hủy", ButtonBar.ButtonData.OK_DONE);
        ButtonType exitButtonType = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, exitButtonType);

        Label title = new Label("Hủy hóa đơn");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #991b1b;");

        Label question = new Label("Vui lòng nhập lý do hủy hóa đơn " + orderId + ".");
        question.setWrapText(true);
        question.setStyle("-fx-font-size: 13px; -fx-text-fill: #1f2937;");

        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Nhập lý do hủy hóa đơn");
        reasonArea.setPrefRowCount(4);
        reasonArea.setWrapText(true);
        reasonArea.setStyle("-fx-background-radius: 8; -fx-border-radius: 8;");

        Label errorLabel = new Label("");
        errorLabel.setMinHeight(18);
        errorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #b91c1c;");

        Label warning = new Label("Lý do hủy là bắt buộc. Hóa đơn sẽ chuyển sang trạng thái Đã hủy.");
        warning.setWrapText(true);
        warning.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f1d1d;");

        VBox content = new VBox(10, title, question, reasonArea, errorLabel, warning);
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

        confirmButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (reasonArea.getText() == null || reasonArea.getText().trim().isEmpty()) {
                errorLabel.setText("Vui lòng nhập lý do hủy hóa đơn.");
                event.consume();
            }
        });

        Button exitButton = (Button) dialog.getDialogPane().lookupButton(exitButtonType);
        exitButton.setStyle(
            "-fx-background-color: #f3f4f6;" +
            "-fx-text-fill: #6b7280;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 16;"
        );

        dialog.setResultConverter(button -> button == confirmButtonType ? reasonArea.getText().trim() : null);
        Platform.runLater(reasonArea::requestFocus);
        return dialog.showAndWait();
    }

    private void updateActionButtons(Invoice invoice) {
        boolean payable = isPayable(invoice) && hasRemainingAmount(invoice);
        boolean canRequestCancel = invoice != null;

        if (btnPay != null) {
            btnPay.setDisable(!payable);
        }
        if (btnCancelInvoice != null) {
            btnCancelInvoice.setDisable(!canRequestCancel);
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
        return invoice != null && "PENDING".equalsIgnoreCase(invoice.getStatus());
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
                    paymentMethods.append(toVietnamesePaymentMethod(payment.getPaymentMethod()));
                }
            }
        }

        StringBuilder content = new StringBuilder();
        content.append("HOA DON PET HOTEL\n");
        content.append("=================\n\n");
        content.append("Ma hoa don: ").append(invoice.getId()).append("\n");
        content.append("Ma khach hang: ").append(invoice.getCustomerId()).append("\n");
        content.append("Ngay tao: ").append(invoice.getCreateDate()).append("\n");
        content.append("Trang thai: ").append(toVietnameseStatus(invoice.getStatus())).append("\n\n");
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

    private void showAutoCloseSuccess(String message) {
        Window owner = tableInvoice == null || tableInvoice.getScene() == null
            ? null
            : tableInvoice.getScene().getWindow();
        if (owner == null) {
            return;
        }

        Label toast = new Label(message);
        toast.setStyle(
            "-fx-background-color: #E8F6EE;" +
            "-fx-text-fill: #166534;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #B8DFC6;" +
            "-fx-border-radius: 12;" +
            "-fx-padding: 12 18;"
        );

        Popup popup = new Popup();
        popup.setAutoFix(true);
        popup.getContent().add(toast);
        popup.show(owner);
        popup.setX(owner.getX() + owner.getWidth() - toast.getWidth() - 32);
        popup.setY(owner.getY() + 32);

        PauseTransition delay = new PauseTransition(Duration.seconds(1.15));
        delay.setOnFinished(event -> popup.hide());
        delay.play();
    }
}
