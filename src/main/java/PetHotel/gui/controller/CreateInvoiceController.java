package PetHotel.gui.controller;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import PetHotel.bus.InvoiceBUS;
import PetHotel.model.Customer;
import PetHotel.model.Invoice;
import PetHotel.model.InvoiceDetail;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

public class CreateInvoiceController {

    private static final String DEFAULT_CREATED_BY_EMP = "EMP001";
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,###");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private final InvoiceBUS invoiceBUS = new InvoiceBUS();

    private Customer selectedCustomer;
    private Invoice.InvoiceSource selectedSource;
    private List<InvoiceDetail> previewDetails;

    @FXML private TextField txtCustomerSearch;

    @FXML private TableView<Customer> tableCustomer;
    @FXML private TableColumn<Customer, String> colCustomerId;
    @FXML private TableColumn<Customer, String> colCustomerName;
    @FXML private TableColumn<Customer, String> colCustomerPhone;
    @FXML private TableColumn<Customer, String> colCustomerEmail;

    @FXML private Label lblSelectedCustomer;
    @FXML private Label lblSelectedPhone;

    @FXML private TableView<Invoice.InvoiceSource> tableBookingSource;
    @FXML private TableColumn<Invoice.InvoiceSource, String> colBookingId;
    @FXML private TableColumn<Invoice.InvoiceSource, String> colBookingPet;
    @FXML private TableColumn<Invoice.InvoiceSource, String> colBookingRoom;
    @FXML private TableColumn<Invoice.InvoiceSource, String> colBookingCheckin;
    @FXML private TableColumn<Invoice.InvoiceSource, String> colBookingCheckout;
    @FXML private TableColumn<Invoice.InvoiceSource, String> colBookingStatus;
    @FXML private TableColumn<Invoice.InvoiceSource, String> colBookingTotal;
    @FXML private TableColumn<Invoice.InvoiceSource, String> colBookingPrepaid;

    @FXML private TableView<Invoice.InvoiceSource> tableServiceSource;
    @FXML private TableColumn<Invoice.InvoiceSource, String> colServiceId;
    @FXML private TableColumn<Invoice.InvoiceSource, String> colServicePet;
    @FXML private TableColumn<Invoice.InvoiceSource, String> colServiceName;
    @FXML private TableColumn<Invoice.InvoiceSource, String> colServiceDate;
    @FXML private TableColumn<Invoice.InvoiceSource, String> colServiceStatus;
    @FXML private TableColumn<Invoice.InvoiceSource, String> colServiceTotal;

    @FXML private Label lblSummaryTotal;
    @FXML private Label lblSubtotal;
    @FXML private Label lblPrepaid;

    @FXML private Button btnCreateInvoice;

    @FXML
    public void initialize() {
        setupCustomerTable();
        setupSourceTables();
        setupEmptyPlaceholders();
        applySourceTableStyles();

        txtCustomerSearch.setOnAction(this::onSearchCustomer);
        btnCreateInvoice.setDisable(true);
        tableCustomer.setVisible(false);
        tableCustomer.setManaged(false);
        clearSelectedCustomer();
        clearPreview();
    }

    @FXML
    public void onSearchCustomer(ActionEvent event) {
        try {
            String keyword = txtCustomerSearch.getText() == null ? null : txtCustomerSearch.getText().trim();
            if (keyword == null || keyword.isEmpty()) {
                tableCustomer.getItems().clear();
                tableCustomer.setVisible(false);
                tableCustomer.setManaged(false);
                clearSelectedCustomer();
                clearPreview();
                showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập mã hoặc tên khách hàng");
                return;
            }

            List<Customer> customers = invoiceBUS.searchInvoiceCustomers(keyword);
            tableCustomer.setItems(FXCollections.observableArrayList(customers));
            tableCustomer.setVisible(true);
            tableCustomer.setManaged(true);
            clearSelectedCustomer();
            clearPreview();
            if (customers.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Không có dữ liệu", "Không tìm thấy khách hàng có booking hoặc dịch vụ chưa tạo hóa đơn");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể tìm khách hàng: " + ex.getMessage());
        }
    }

    @FXML
    public void onCreateInvoice(ActionEvent event) {
        if (selectedCustomer == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn khách hàng");
            return;
        }
        if (selectedSource == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn booking hoặc dịch vụ");
            return;
        }

        try {
            String orderId = invoiceBUS.generateNextOrderId();
            List<InvoiceDetail> details = invoiceBUS.buildInvoiceDetailsForSource(
                selectedSource.getSourceType(),
                selectedSource.getSourceId(),
                orderId
            );
            double subtotal = totalOf(details);
            double prepaid = prepaidFor(selectedSource);
            double remaining = remainingAmount(subtotal, prepaid);
            if (remaining <= 0) {
                showAlert(Alert.AlertType.WARNING, "Dữ liệu không hợp lệ", "Booking này không còn số tiền cần thanh toán");
                return;
            }
            applyPrepaidToDetails(details, prepaid);

            Invoice invoice = new Invoice();
            invoice.setId(orderId);
            invoice.setCustomerId(selectedCustomer.getCustomerId());
            invoice.setCustomerName(selectedCustomer.getFullName());
            invoice.setBranchId(selectedSource.getBranchId());
            invoice.setBookingId(selectedSource.getBookingId());
            invoice.setCreatedByEmp(DEFAULT_CREATED_BY_EMP);
            invoice.setStatus("PENDING");
            invoice.setSubtotal(remaining);
            invoice.setTotalAmount(remaining);
            invoice.setCreateDate(new Date());

            invoiceBUS.createInvoice(invoice, details);
            showAutoCloseSuccess("Đã tạo hóa đơn " + orderId, this::closeWindow);
        } catch (IllegalArgumentException ex) {
            showAlert(Alert.AlertType.WARNING, "Dữ liệu không hợp lệ", ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể tạo hóa đơn: " + ex.getMessage());
        }
    }

    @FXML
    public void onClose(ActionEvent event) {
        closeWindow();
    }

    private void setupCustomerTable() {
        colCustomerId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colCustomerName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colCustomerPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colCustomerEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        tableCustomer.getSelectionModel().selectedItemProperty().addListener((obs, oldCustomer, newCustomer) -> {
            if (newCustomer != null) {
                selectCustomer(newCustomer);
            }
        });
    }

    private void setupEmptyPlaceholders() {
        tableCustomer.setPlaceholder(new Label(""));
        tableBookingSource.setPlaceholder(new Label(""));
        tableServiceSource.setPlaceholder(new Label(""));
    }

    private void setupSourceTables() {
        colBookingId.setCellValueFactory(new PropertyValueFactory<>("sourceId"));
        colBookingPet.setCellValueFactory(new PropertyValueFactory<>("petName"));
        colBookingRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colBookingCheckin.setCellValueFactory(cell -> new ReadOnlyStringWrapper(formatDate(cell.getValue().getStartDate())));
        colBookingCheckout.setCellValueFactory(cell -> new ReadOnlyStringWrapper(formatDate(cell.getValue().getEndDate())));
        colBookingStatus.setCellValueFactory(cell -> new ReadOnlyStringWrapper(toVietnameseWorkStatus(cell.getValue().getStatus())));
        colBookingTotal.setCellValueFactory(cell -> new ReadOnlyStringWrapper(formatMoney(cell.getValue().getTotalAmount())));
        colBookingPrepaid.setCellValueFactory(cell -> new ReadOnlyStringWrapper(formatMoney(cell.getValue().getPrepaidAmount())));

        colServiceId.setCellValueFactory(new PropertyValueFactory<>("sourceId"));
        colServicePet.setCellValueFactory(new PropertyValueFactory<>("petName"));
        colServiceName.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        colServiceDate.setCellValueFactory(cell -> new ReadOnlyStringWrapper(formatDate(cell.getValue().getScheduledAt())));
        colServiceStatus.setCellValueFactory(cell -> new ReadOnlyStringWrapper(toVietnameseWorkStatus(cell.getValue().getStatus())));
        colServiceTotal.setCellValueFactory(cell -> new ReadOnlyStringWrapper(formatMoney(cell.getValue().getTotalAmount())));

        tableBookingSource.getSelectionModel().selectedItemProperty().addListener((obs, oldSource, newSource) -> {
            if (newSource != null) {
                tableServiceSource.getSelectionModel().clearSelection();
                selectSource(newSource);
            }
        });
        tableServiceSource.getSelectionModel().selectedItemProperty().addListener((obs, oldSource, newSource) -> {
            if (newSource != null) {
                tableBookingSource.getSelectionModel().clearSelection();
                selectSource(newSource);
            }
        });
        installToggleSelection(tableBookingSource);
        installToggleSelection(tableServiceSource);
    }

    private void applySourceTableStyles() {
        applyCreateInvoiceTableStyle(tableBookingSource);
        applyCreateInvoiceTableStyle(tableServiceSource);
    }

    private void applyCreateInvoiceTableStyle(TableView<?> table) {
        table.getStyleClass().remove("management-table");
        if (!table.getStyleClass().contains("create-invoice-table")) {
            table.getStyleClass().add("create-invoice-table");
        }
        table.setStyle("");
        table.setFocusTraversable(false);
    }

    private void installToggleSelection(TableView<Invoice.InvoiceSource> table) {
        table.setRowFactory(tableView -> {
            TableRow<Invoice.InvoiceSource> row = new TableRow<>();
            row.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                Invoice.InvoiceSource clickedSource = row.getItem();
                if (!row.isEmpty() && isSameSource(clickedSource, selectedSource)) {
                    tableView.getSelectionModel().clearSelection();
                    clearPreview();
                    event.consume();
                }
            });
            return row;
        });
    }

    private boolean isSameSource(Invoice.InvoiceSource first, Invoice.InvoiceSource second) {
        if (first == null || second == null) {
            return false;
        }
        return valueOrDash(first.getSourceType()).equals(valueOrDash(second.getSourceType()))
            && valueOrDash(first.getSourceId()).equals(valueOrDash(second.getSourceId()));
    }

    private void selectCustomer(Customer customer) {
        selectedCustomer = customer;
        lblSelectedCustomer.setText(valueOrDash(customer.getCustomerId()) + " - " + valueOrDash(customer.getFullName()));
        lblSelectedPhone.setText(valueOrDash(customer.getPhone()));
        tableCustomer.setVisible(false);
        tableCustomer.setManaged(false);
        loadSources(customer.getCustomerId());
    }

    private void loadSources(String customerId) {
        try {
            tableBookingSource.setItems(FXCollections.observableArrayList(invoiceBUS.findUninvoicedBookingSources(customerId)));
            tableServiceSource.setItems(FXCollections.observableArrayList(invoiceBUS.findUninvoicedServiceSources(customerId)));
            clearPreview();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể tải nguồn tạo hóa đơn: " + ex.getMessage());
        }
    }

    private void selectSource(Invoice.InvoiceSource source) {
        selectedSource = source;
        try {
            String tempOrderId = "PREVIEW";
            previewDetails = invoiceBUS.buildInvoiceDetailsForSource(source.getSourceType(), source.getSourceId(), tempOrderId);

            double subtotal = totalOf(previewDetails);
            double prepaid = prepaidFor(source);
            double remaining = remainingAmount(subtotal, prepaid);

            lblSummaryTotal.setText(formatMoney(remaining));
            lblSubtotal.setText(formatMoney(subtotal));
            lblPrepaid.setText(formatMoney(prepaid));
            btnCreateInvoice.setDisable(remaining <= 0);
            if (remaining <= 0) {
                showAlert(Alert.AlertType.WARNING, "Dữ liệu không hợp lệ", "Booking này không còn số tiền cần thanh toán");
            }
        } catch (Exception ex) {
            clearPreview();
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể tải chi tiết hóa đơn: " + ex.getMessage());
        }
    }

    private void clearSelectedCustomer() {
        selectedCustomer = null;
        tableBookingSource.getItems().clear();
        tableServiceSource.getItems().clear();
        lblSelectedCustomer.setText("-");
        lblSelectedPhone.setText("-");
    }

    private void clearPreview() {
        selectedSource = null;
        previewDetails = null;
        lblSummaryTotal.setText("0");
        lblSubtotal.setText("0");
        lblPrepaid.setText("0");
        btnCreateInvoice.setDisable(true);
    }

    private double totalOf(List<InvoiceDetail> details) {
        double total = 0;
        if (details != null) {
            for (InvoiceDetail detail : details) {
                total += detail.getLineTotal();
            }
        }
        return total;
    }

    private double prepaidFor(Invoice.InvoiceSource source) {
        return source != null && "BOOKING".equalsIgnoreCase(source.getSourceType())
            ? Math.max(source.getPrepaidAmount(), 0)
            : 0;
    }

    private double remainingAmount(double subtotal, double prepaid) {
        return Math.max(subtotal - prepaid, 0);
    }

    private void applyPrepaidToDetails(List<InvoiceDetail> details, double prepaid) {
        if (details == null || details.isEmpty() || prepaid <= 0) {
            return;
        }

        double remainingPrepaid = prepaid;
        for (InvoiceDetail detail : details) {
            if (remainingPrepaid <= 0) {
                break;
            }

            double lineTotal = Math.max(detail.getLineTotal(), 0);
            double deduction = Math.min(lineTotal, remainingPrepaid);
            double adjustedLineTotal = lineTotal - deduction;
            detail.setLineTotal(adjustedLineTotal);
            if (detail.getQuantity() > 0) {
                detail.setUnitPrice(adjustedLineTotal / detail.getQuantity());
            }
            remainingPrepaid -= deduction;
        }
    }

    private String formatDate(Date date) {
        return date == null ? "-" : DATE_FORMAT.format(date);
    }

    private String formatMoney(double value) {
        return MONEY_FORMAT.format(value);
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    private String toVietnameseWorkStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "";
        }
        return switch (status.trim().toUpperCase()) {
            case "PENDING" -> "Đang chờ";
            case "CONFIRMED" -> "Đã xác nhận";
            case "CHECKED_IN" -> "Đã nhận phòng";
            case "CHECKED_OUT" -> "Đã trả phòng";
            case "SCHEDULED" -> "Đã lên lịch";
            case "IN_PROGRESS" -> "Đang thực hiện";
            case "DONE", "COMPLETED" -> "Hoàn tất";
            case "CANCELLED", "CANCELED" -> "Đã hủy";
            default -> status.trim();
        };
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCreateInvoice.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showAutoCloseSuccess(String message, Runnable afterHidden) {
        Window owner = btnCreateInvoice == null || btnCreateInvoice.getScene() == null
            ? null
            : btnCreateInvoice.getScene().getWindow();
        if (owner == null) {
            if (afterHidden != null) {
                afterHidden.run();
            }
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
        delay.setOnFinished(event -> {
            popup.hide();
            if (afterHidden != null) {
                afterHidden.run();
            }
        });
        delay.play();
    }
}
