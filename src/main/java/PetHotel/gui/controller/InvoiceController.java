package PetHotel.gui.controller;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

import PetHotel.bus.InvoiceBUS;
import PetHotel.model.Invoice;
import PetHotel.model.InvoiceDetail;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

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
                if (newSelection != null) {
                    showSelectedInvoiceDetail();
                }
            });
        }

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

            ObservableList<Invoice> data = FXCollections.observableArrayList(results);
            tableInvoice.setItems(data);

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
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Tạo hóa đơn");
            dialog.setHeaderText("Nhập thông tin hóa đơn mới");
            dialog.setContentText(
                "Nhập theo định dạng:\n" +
                "order_id,customer_id,branch_id,booking_id,created_by_emp,total_amount\n\n" +
                "Ví dụ: ORD999,CUS001,BR001,BK001,EMP001,500000"
            );

            var result = dialog.showAndWait();

            if (result.isEmpty()) {
                return;
            }

            String input = result.get().trim();
            String[] parts = input.split(",");

            if (parts.length != 6) {
                showAlert(
                    Alert.AlertType.WARNING,
                    "Sai định dạng",
                    "Vui lòng nhập đúng 6 thông tin:\norder_id,customer_id,branch_id,booking_id,created_by_emp,total_amount"
                );
                return;
            }

            String orderId = parts[0].trim();
            String customerId = parts[1].trim();
            String branchId = parts[2].trim();
            String bookingId = parts[3].trim();
            String createdByEmp = parts[4].trim();
            double totalAmount = Double.parseDouble(parts[5].trim());

            Invoice invoice = new Invoice();
            invoice.setId(orderId);
            invoice.setCustomerId(customerId);
            invoice.setBranchId(branchId);
            invoice.setBookingId(bookingId);
            invoice.setCreatedByEmp(createdByEmp);
            invoice.setSubtotal(totalAmount);
            invoice.setTotalAmount(totalAmount);
            invoice.setStatus("PENDING");

            boolean success = invoiceBus.createInvoice(invoice);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Tạo hóa đơn thành công: " + orderId);
                handleSearch(null);
            } else {
                showAlert(Alert.AlertType.ERROR, "Thất bại", "Không thể tạo hóa đơn.");
            }

        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.WARNING, "Sai số tiền", "Tổng tiền phải là số.");
        } catch (IllegalArgumentException ex) {
            showAlert(Alert.AlertType.WARNING, "Dữ liệu không hợp lệ", ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể tạo hóa đơn: " + ex.getMessage());
        }
    }

    @FXML
    public void onPayment(ActionEvent event) {
        Invoice selectedInvoice = tableInvoice.getSelectionModel().getSelectedItem();

        if (selectedInvoice == null) {
            showAlert(Alert.AlertType.WARNING, "Thông báo", "Vui lòng chọn một hóa đơn để thanh toán.");
            return;
        }

        System.out.println("Chức năng thanh toán cho hóa đơn: " + selectedInvoice.getId());
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng thanh toán sẽ làm ở bước sau.");
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

    invoicePreview.getChildren().clear();

    Label title = new Label("Chi Tiết Hóa Đơn");
    title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

    Label id = new Label("Mã hóa đơn: " + selectedInvoice.getId());
    Label customer = new Label("Mã khách hàng: " + selectedInvoice.getCustomerId());
    Label date = new Label("Ngày tạo: " + selectedInvoice.getCreateDate());
    Label total = new Label("Tổng tiền: " + selectedInvoice.getTotalAmount());
    Label status = new Label("Trạng thái: " + selectedInvoice.getStatus());

    invoicePreview.getChildren().addAll(title, id, customer, date, total, status);

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

    invoicePreview.getChildren().clear();

    Label title = new Label("Chi Tiết Hóa Đơn");
    title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

    Label id = new Label("Mã hóa đơn: " + selectedInvoice.getId());
    Label customer = new Label("Mã khách hàng: " + selectedInvoice.getCustomerId());
    Label date = new Label("Ngày tạo: " + selectedInvoice.getCreateDate());
    Label total = new Label("Tổng tiền: " + selectedInvoice.getTotalAmount());
    Label status = new Label("Trạng thái: " + selectedInvoice.getStatus());

    invoicePreview.getChildren().addAll(title, id, customer, date, total, status);
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

        for (InvoiceDetail detail : details) {
            String itemText =
                "- Mã chi tiết: " + detail.getDetailId()
                + "\n  Phòng booking: " + safeText(detail.getBookingRoomId())
                + "\n  Dịch vụ booking: " + safeText(detail.getBookingServiceId())
                + "\n  Ghi chú: " + safeText(detail.getNote())
                + "\n  Số lượng: " + detail.getQuantity()
                + "\n  Đơn giá: " + moneyFormat.format(detail.getUnitPrice()) + " VNĐ"
                + "\n  Thành tiền: " + moneyFormat.format(detail.getLineTotal()) + " VNĐ";

            Label itemLabel = new Label(itemText);
            itemLabel.setWrapText(true);

            invoicePreview.getChildren().addAll(itemLabel, new Separator());
        }
    }

    private String safeText(String value) {
        return value == null || value.trim().isEmpty() ? "Không có" : value;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}