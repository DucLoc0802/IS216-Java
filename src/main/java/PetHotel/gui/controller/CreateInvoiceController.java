package PetHotel.gui.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import PetHotel.bus.InvoiceBUS;
import PetHotel.model.Invoice;
import PetHotel.model.InvoiceDetail;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CreateInvoiceController {

    private static final String DEFAULT_BRANCH_ID = "BR001";
    // TODO: replace with employee_id from login session after invoice flow is wired to auth/session.
    private static final String DEFAULT_CREATED_BY_EMP = "EMP001";
    private static final DecimalFormat DISPLAY_MONEY_FORMAT = new DecimalFormat("#,###");

    private final InvoiceBUS invoiceBUS = new InvoiceBUS();
    private final Map<String, Invoice> bookingOptions = new LinkedHashMap<>();
    private final Map<String, Double> bookingFees = new LinkedHashMap<>();
    private final Map<String, String> displayToBookingId = new LinkedHashMap<>();
    private final List<String> bookingDisplayOptions = new ArrayList<>();

    @FXML
    private ComboBox<String> cbbBooking;

    @FXML
    private TextField txtCustomer;

    @FXML
    private TextField txtBookingFee;

    @FXML
    private ComboBox<String> cbbGrooming;

    @FXML
    private TextField txtGroomingFee;

    @FXML
    private TextField txtExtraName;

    @FXML
    private TextField txtExtraFee;

    @FXML
    private DatePicker dpCreatedDate;

    @FXML
    private ComboBox<String> cbbStatus;

    @FXML
    private TextField txtTotalAmount;

    @FXML
    private TextArea txtNote;

    @FXML
    public void initialize() {
        dpCreatedDate.setValue(LocalDate.now());

        cbbStatus.setItems(FXCollections.observableArrayList(
            "PENDING",
            "PAID",
            "PARTIAL",
            "CANCELLED"
        ));
        cbbStatus.setValue("PENDING");

        loadBookingOptions();

        cbbGrooming.setItems(FXCollections.observableArrayList(
            "Khong co",
            "Tam thu cung",
            "Cat tia long",
            "Spa thu cung"
        ));
        cbbGrooming.setValue("Khong co");

        txtCustomer.setEditable(false);
        txtBookingFee.setEditable(false);
        txtTotalAmount.setEditable(false);

        cbbBooking.valueProperty().addListener((obs, oldValue, newValue) -> fillBookingInfo(selectedBookingId()));
        txtGroomingFee.textProperty().addListener((obs, oldValue, newValue) -> refreshTotalSilently());
        txtExtraFee.textProperty().addListener((obs, oldValue, newValue) -> refreshTotalSilently());
        refreshTotalSilently();
    }

    @FXML
    public void onCreateInvoice(ActionEvent event) {
        String bookingId = selectedBookingId();
        String customerId = txtCustomer.getText() == null ? "" : txtCustomer.getText().trim();

        if (bookingId == null || bookingId.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thieu thong tin", "Vui long chon booking.");
            return;
        }

        if (customerId.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thieu thong tin", "Khach hang khong duoc de trong.");
            return;
        }

        try {
            Invoice booking = bookingOptions.get(bookingId);
            String branchId = booking != null && booking.getBranchId() != null
                ? booking.getBranchId()
                : DEFAULT_BRANCH_ID;

            String orderId = invoiceBUS.generateNextOrderId();
            List<InvoiceDetail> details = buildInvoiceDetails(orderId);
            double totalAmount = calculateDetailTotal(details);
            txtTotalAmount.setText(formatMoney(totalAmount));

            Invoice invoice = new Invoice();
            invoice.setId(orderId);
            invoice.setCustomerId(customerId);
            invoice.setBranchId(branchId);
            invoice.setBookingId(bookingId);
            invoice.setCreatedByEmp(DEFAULT_CREATED_BY_EMP);
            invoice.setStatus(cbbStatus.getValue() == null ? "PENDING" : cbbStatus.getValue());
            invoice.setSubtotal(totalAmount);
            invoice.setTotalAmount(totalAmount);
            invoice.setCreateDate(toDate(dpCreatedDate.getValue()));

            boolean success = invoiceBUS.createInvoice(invoice, details);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thanh cong", "Da tao hoa don " + invoice.getId() + " tu booking " + bookingId + ".");
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "That bai", "Khong the tao hoa don.");
            }
        } catch (IllegalArgumentException ex) {
            showAlert(Alert.AlertType.WARNING, "Du lieu khong hop le", ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Loi he thong", "Khong the tao hoa don: " + ex.getMessage());
        }
    }

    @FXML
    public void onClose(ActionEvent event) {
        closeWindow();
    }

    private void loadBookingOptions() {
        try {
            bookingOptions.clear();
            bookingOptions.putAll(invoiceBUS.findBookingOptions());
            bookingFees.clear();
            bookingFees.putAll(invoiceBUS.findBookingFees());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        rebuildBookingDisplayOptions();
        cbbBooking.setItems(FXCollections.observableArrayList(bookingDisplayOptions));
    }

    private void rebuildBookingDisplayOptions() {
        displayToBookingId.clear();
        bookingDisplayOptions.clear();

        for (Map.Entry<String, Invoice> entry : bookingOptions.entrySet()) {
            String bookingId = entry.getKey();
            Invoice booking = entry.getValue();
            String display = bookingId
                + " | " + valueOrDash(booking.getCustomerId())
                + " | " + DISPLAY_MONEY_FORMAT.format(bookingFeeForBooking(bookingId));
            displayToBookingId.put(display, bookingId);
            bookingDisplayOptions.add(display);
        }
    }

    private String selectedBookingId() {
        String selected = cbbBooking.getValue();
        if (selected == null || selected.trim().isEmpty()) {
            return null;
        }
        return displayToBookingId.getOrDefault(selected, selected);
    }

    private void fillBookingInfo(String bookingId) {
        Invoice booking = bookingOptions.get(bookingId);
        if (booking != null) {
            txtCustomer.setText(booking.getCustomerId());
            txtBookingFee.setText(formatMoney(bookingFeeForBooking(bookingId)));
        } else {
            txtCustomer.clear();
            txtBookingFee.clear();
            txtTotalAmount.clear();
        }
        refreshTotalSilently();
    }

    private double bookingFeeForBooking(String bookingId) {
        double queriedFee = bookingFees.getOrDefault(bookingId, 0.0);
        if (queriedFee > 0) {
            return queriedFee;
        }
        return 0;
    }

    private List<InvoiceDetail> buildInvoiceDetails(String orderId) {
        List<InvoiceDetail> details = new ArrayList<>();

        double bookingFee = parseOptionalMoney(txtBookingFee.getText(), "Phí booking/phòng");
        if (bookingFee > 0) {
            InvoiceDetail detail = new InvoiceDetail();
            detail.setOrderId(orderId);
            detail.setNote("Phí booking/phòng");
            detail.setQuantity(1);
            detail.setUnitPrice(bookingFee);
            detail.setLineTotal(bookingFee);
            details.add(detail);
        }

        double groomingFee = parseOptionalMoney(txtGroomingFee.getText(), "Phi grooming");
        if (groomingFee > 0) {
            String groomingName = cbbGrooming.getValue() == null ? "Grooming" : cbbGrooming.getValue();
            InvoiceDetail detail = new InvoiceDetail();
            detail.setOrderId(orderId);
            detail.setNote("Phi grooming: " + groomingName);
            detail.setQuantity(1);
            detail.setUnitPrice(groomingFee);
            detail.setLineTotal(groomingFee);
            details.add(detail);
        }

        double extraFee = parseOptionalMoney(txtExtraFee.getText(), "Chi phi phat sinh");
        if (extraFee > 0) {
            String extraName = txtExtraName.getText() == null || txtExtraName.getText().trim().isEmpty()
                ? "Chi phi phat sinh"
                : txtExtraName.getText().trim();
            InvoiceDetail detail = new InvoiceDetail();
            detail.setOrderId(orderId);
            detail.setNote(extraName);
            detail.setQuantity(1);
            detail.setUnitPrice(extraFee);
            detail.setLineTotal(extraFee);
            details.add(detail);
        }

        return details;
    }

    private double calculateDetailTotal(List<InvoiceDetail> details) {
        double total = 0;
        for (InvoiceDetail detail : details) {
            total += detail.getLineTotal();
        }
        return total;
    }

    public double calculateTotal() {
        double bookingFee = parseOptionalMoney(txtBookingFee.getText(), "Phí booking/phòng");
        double groomingFee = parseOptionalMoney(txtGroomingFee.getText(), "Phi grooming");
        double extraFee = parseOptionalMoney(txtExtraFee.getText(), "Chi phi phat sinh");
        double total = bookingFee + groomingFee + extraFee;
        txtTotalAmount.setText(formatMoney(total));
        return total;
    }

    private void refreshTotalSilently() {
        try {
            calculateTotal();
        } catch (IllegalArgumentException ex) {
            txtTotalAmount.setText("");
        }
    }

    private double parseOptionalMoney(String rawValue, String fieldName) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return 0;
        }

        try {
            double value = Double.parseDouble(rawValue.trim());
            if (value < 0) {
                throw new IllegalArgumentException(fieldName + " khong duoc am.");
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " phai la so.");
        }
    }

    private Date toDate(LocalDate localDate) {
        LocalDate value = localDate == null ? LocalDate.now() : localDate;
        return Date.from(value.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private String formatMoney(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    private void closeWindow() {
        Stage stage = (Stage) cbbBooking.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
