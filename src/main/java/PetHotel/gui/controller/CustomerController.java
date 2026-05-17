package PetHotel.gui.controller;

import PetHotel.bus.AuthBUS;
import PetHotel.bus.CustomerBUS;
import PetHotel.bus.PetBUS;
import PetHotel.dao.CustomerDAO;
import PetHotel.dao.PetDAO;
import PetHotel.dao.PetHealthRecordDAO;
import PetHotel.model.Customer;
import PetHotel.model.Pet;
import PetHotel.model.PetHealthRecord;
import PetHotel.util.Role;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CustomerController {

    private static final int CUSTOMER_PAGE_SIZE = 10;

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private ToggleButton tabCustomer;
    @FXML private ToggleButton tabPet;
    @FXML private StackPane tabContent;
    @FXML private VBox customerTabPane;
    @FXML private Button btnAddCustomer;
    @FXML private TextField searchField;
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> colId;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colPhone;
    @FXML private TableColumn<Customer, String> colEmail;
    @FXML private TableColumn<Customer, String> colPetCount;
    @FXML private TableColumn<Customer, String> colJoined;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnHistory;
    @FXML private Pagination pagination;
    @FXML private Label customerPageInfo;

    private CustomerBUS customerBUS;
    private PetBUS petBUS;
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final PetDAO petDAO = new PetDAO();
    private final PetHealthRecordDAO petHealthRecordDAO = new PetHealthRecordDAO();
    private final ObservableList<Customer> allCustomers = FXCollections.observableArrayList();
    private final ObservableList<Customer> customers = FXCollections.observableArrayList();
    private final ObservableList<Customer> pagedCustomers = FXCollections.observableArrayList();
    private Customer selectedCustomer;
    private Parent petTabPane;
    private PetController petTabController;
    private int currentCustomerPageIndex;
    private boolean updatingPagination;

    @FXML
    public void initialize() {
        AuthBUS authBUS = SessionManager.getInstance().getAuthBUS();
        customerBUS = new CustomerBUS(authBUS);
        petBUS = new PetBUS(authBUS);
        setupTableColumns();
        customerTable.setItems(pagedCustomers);
        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedCustomer = newVal;
            setActionButtons(newVal != null);
        });
        pagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> {
            if (!updatingPagination) {
                updateCustomerPage(newVal.intValue(), null);
            }
        });
        loadCustomers(null);
    }

    private void setupTableColumns() {
        customerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        customerTable.setFixedCellSize(36);
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCustomerId()));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));
        colPhone.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPhone()));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(valueOrDash(d.getValue().getEmail())));
        colPetCount.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(countPets(d.getValue().getCustomerId()))));
        colJoined.setCellValueFactory(d -> new SimpleStringProperty(formatDate(d.getValue())));
        configureCustomerColumn(colId, 110, 120);
        configureCustomerColumn(colName, 220, 260);
        configureCustomerColumn(colPhone, 140, 160);
        configureCustomerColumn(colEmail, 240, 300);
        configureCustomerColumn(colPetCount, 90, 100);
        configureCustomerColumn(colJoined, 140, 160);
    }

    private void configureCustomerColumn(TableColumn<Customer, String> column, double minWidth, double prefWidth) {
        column.setMinWidth(minWidth);
        column.setPrefWidth(prefWidth);
        column.setResizable(true);
    }

    private void setActionButtons(boolean enabled) {
        btnEdit.setDisable(true);
        btnDelete.setDisable(true);
        btnHistory.setDisable(true);
    }

    private void loadCustomers(String customerIdToSelect) {
        try {
            allCustomers.setAll(customerBUS.getAllCustomers());
            customers.setAll(allCustomers);
            refreshCustomerPagination(customerIdToSelect, customerIdToSelect == null);
        } catch (Exception e) {
            showError("Không tải được danh sách khách hàng", e);
        }
    }

    private void refreshCustomerPagination(String customerIdToSelect, boolean resetToFirstPage) {
        int total = customers.size();
        int pageCount = Math.max(1, (int) Math.ceil(total / (double) CUSTOMER_PAGE_SIZE));
        int pageIndex = resetToFirstPage ? 0 : Math.min(currentCustomerPageIndex, pageCount - 1);
        if (customerIdToSelect != null) {
            int index = indexOfCustomer(customers, customerIdToSelect);
            if (index >= 0) {
                pageIndex = index / CUSTOMER_PAGE_SIZE;
            }
        }
        updatingPagination = true;
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(pageIndex);
        updatingPagination = false;
        updateCustomerPage(pageIndex, customerIdToSelect);
    }

    private void updateCustomerPage(int pageIndex, String customerIdToSelect) {
        int total = customers.size();
        int pageCount = Math.max(1, (int) Math.ceil(total / (double) CUSTOMER_PAGE_SIZE));
        currentCustomerPageIndex = Math.max(0, Math.min(pageIndex, pageCount - 1));
        int from = currentCustomerPageIndex * CUSTOMER_PAGE_SIZE;
        int to = Math.min(from + CUSTOMER_PAGE_SIZE, total);
        pagedCustomers.setAll(from < to ? customers.subList(from, to) : List.of());
        customerTable.refresh();
        if (customerPageInfo != null) {
            customerPageInfo.setText(total == 0
                    ? "Hiển thị 0 / 0 khách hàng"
                    : "Hiển thị " + (from + 1) + "-" + to + " / " + total + " khách hàng");
        }
        if (customerIdToSelect != null) {
            pagedCustomers.stream()
                    .filter(c -> customerIdToSelect.equals(c.getCustomerId()))
                    .findFirst()
                    .ifPresent(c -> customerTable.getSelectionModel().select(c));
        } else if (selectedCustomer != null && pagedCustomers.stream().noneMatch(c -> c.getCustomerId().equals(selectedCustomer.getCustomerId()))) {
            customerTable.getSelectionModel().clearSelection();
        }
    }

    private int indexOfCustomer(ObservableList<Customer> list, String customerId) {
        for (int i = 0; i < list.size(); i++) {
            if (customerId.equals(list.get(i).getCustomerId())) {
                return i;
            }
        }
        return -1;
    }

    @FXML
    public void onSearch(ActionEvent event) {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadCustomers(null);
            return;
        }
        try {
            List<Customer> result = customerBUS.searchCustomer(keyword);
            customers.setAll(result);
            refreshCustomerPagination(null, true);
            if (result.isEmpty()) showInfo("Không có kết quả", "Không tìm thấy khách hàng phù hợp.");
        } catch (Exception e) {
            showError("Không tra cứu được khách hàng", e);
        }
    }

    @FXML public void onClearFilter(ActionEvent event) { searchField.clear(); loadCustomers(null); }
    @FXML public void onAddCustomer(ActionEvent event) { openCustomerForm(); }
    @FXML public void onEdit(ActionEvent event) { showInfo("Ngoài phạm vi", "UC-CUS-04 chưa triển khai trong lần này."); }
    @FXML public void onDelete(ActionEvent event) { showInfo("Ngoài phạm vi", "UC-CUS-05 chưa triển khai trong lần này."); }
    @FXML public void onViewHistory(ActionEvent event) { showInfo("Ngoài phạm vi", "UC-CUS-06 chưa triển khai trong lần này."); }

    @FXML
    public void onShowCustomerTab(ActionEvent event) {
        showCustomerTab();
    }

    @FXML
    public void onShowPetTab(ActionEvent event) {
        long start = System.currentTimeMillis();
        System.out.println("[PetTab] switch start");
        try {
            if (petTabPane == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/PetHotel/gui/view/PetManagement.fxml"));
                petTabPane = loader.load();
                petTabController = loader.getController();
                petTabController.prepareEmbeddedView();
                petTabPane.getStyleClass().remove("main-content");
                petTabPane.getStyleClass().add("ph-embedded-pet-view");
                petTabPane.setVisible(false);
                petTabPane.setManaged(false);
                tabContent.getChildren().add(petTabPane);
            }
            titleLabel.setText("Quản Lý Thú Cưng");
            subtitleLabel.setText("Danh sách thú cưng tại chi nhánh");
            btnAddCustomer.setVisible(false);
            btnAddCustomer.setManaged(false);
            customerTabPane.setVisible(false);
            customerTabPane.setManaged(false);
            petTabPane.setVisible(true);
            petTabPane.setManaged(true);
            petTabController.refreshIfNeeded();
            tabPet.setSelected(true);
            System.out.println("[PetTab] render done in " + (System.currentTimeMillis() - start) + "ms");
        } catch (Exception e) {
            tabCustomer.setSelected(true);
            showCustomerTab();
            showError("Không mở được tab Danh sách thú cưng", e);
        }
    }

    private void showCustomerTab() {
        titleLabel.setText("Quản Lý Khách Hàng");
        subtitleLabel.setText("Quản lý thông tin khách hàng");
        btnAddCustomer.setVisible(true);
        btnAddCustomer.setManaged(true);
        customerTabPane.setVisible(true);
        customerTabPane.setManaged(true);
        if (petTabPane != null) {
            petTabPane.setVisible(false);
            petTabPane.setManaged(false);
        }
        tabCustomer.setSelected(true);
    }

    @FXML
    public void onViewPets(ActionEvent event) {
        if (selectedCustomer == null) {
            showInfo("Chưa chọn khách hàng", "Vui lòng chọn một khách hàng trước.");
            return;
        }
        openPetForCustomerForm(selectedCustomer);
    }

    @FXML
    public void onTableClick(MouseEvent event) {
        if (event.getClickCount() == 2 && selectedCustomer != null) {
            openCustomerDetail(selectedCustomer);
        }
    }

    private void openCustomerForm() {
        Stage stage = modalStage("Thêm Khách Hàng");
        Label error = errorLabel();
        TextField id = readOnlyField(previewNextCustomerId());
        TextField name = formField();
        TextField phone = formField();
        TextField cccd = formField();
        TextField email = formField();
        TextField address = formField();
        TextField joined = readOnlyField(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        TextArea note = formArea(3);
        cccd.setPromptText("12 chữ số");

        GridPane grid = formGrid();
        addRow(grid, 0, "Mã KH", id);
        addRow(grid, 1, "Họ tên *", name);
        addRow(grid, 2, "Số điện thoại *", phone);
        addRow(grid, 3, "Căn cước công dân *", cccd);
        addRow(grid, 4, "Email", email);
        addRow(grid, 5, "Địa chỉ", address);
        addRow(grid, 6, "Ghi chú", note);
        addRow(grid, 7, "Ngày tham gia", joined);

        Button save = primaryButton("Lưu khách hàng");
        Button cancel = secondaryButton("Hủy");
        cancel.setOnAction(e -> stage.close());
        save.setOnAction(e -> {
            try {
                Customer created = customerBUS.createCustomer(name.getText(), phone.getText(), cccd.getText(), email.getText(), address.getText(), note.getText());
                Customer refreshed = findCustomerById(created.getCustomerId());
                loadCustomers(created.getCustomerId());
                stage.close();
                openCustomerDetail(refreshed != null ? refreshed : created);
            } catch (Exception ex) {
                error.setText(ex.getMessage());
            }
        });

        stage.setScene(new Scene(formShell("Thêm Khách Hàng", "Tạo hồ sơ khách hàng mới", initials("?"), grid, error, save, cancel), 600, 700));
        addStylesheet(stage);
        stage.showAndWait();
    }

    private void openPetForCustomerForm(Customer customer) {
        openPetForCustomerForm(customer, null);
    }

    private void openPetForCustomerForm(Customer customer, Runnable afterSaved) {
        long start = System.currentTimeMillis();
        System.out.println("[PetForm] Click add/link pet");
        System.out.println("Opening pet form from customer detail...");
        System.out.println("customer_id=" + customer.getCustomerId() + ", customer_name=" + customer.getFullName());
        String fxmlPath = "/PetHotel/gui/view/PetForm.fxml";
        System.out.println("Loading FXML: " + fxmlPath);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            System.out.println("[PetForm] FXML loaded in " + (System.currentTimeMillis() - start) + "ms");
            PetFormController controller = loader.getController();
            controller.setOwner(customer);
            System.out.println("[PetForm] Owner set in " + (System.currentTimeMillis() - start) + "ms");
            controller.setOnSaved(() -> {
                updateCustomerPage(currentCustomerPageIndex, customer.getCustomerId());
                refreshPetTabIfLoaded();
                if (afterSaved != null) afterSaved.run();
            });

            Stage stage = modalStage("Thêm thú cưng mới");
            preparePetFormStage(stage, root);
            System.out.println("[PetForm] Stage ready in " + (System.currentTimeMillis() - start) + "ms");
            stage.showAndWait();
            System.out.println("[PetForm] Stage closed after " + (System.currentTimeMillis() - start) + "ms");
        } catch (Exception e) {
            System.err.println("Cannot open PetForm.fxml");
            e.printStackTrace();
            showError("Không mở được form thêm thú cưng", e);
        }
    }

    private void preparePetFormStage(Stage stage, Parent root) {
        if (root instanceof Region region) {
            region.setMinSize(760, 680);
            region.setPrefSize(820, 740);
        }
        Scene scene = new Scene(root, 820, 740);
        stage.setScene(scene);
        stage.setMinWidth(760);
        stage.setMinHeight(680);
        root.applyCss();
        root.layout();
        root.snapshot(null, null);
        stage.sizeToScene();
        stage.centerOnScreen();
    }

    private void openCustomerDetail(Customer customer) {
        Stage stage = modalStage("Hồ Sơ Khách Hàng");
        List<Pet> pets = loadPetsOfCustomer(customer);
        VBox petList = new VBox(8);
        populateLinkedPetList(customer, petList, pets);

        GridPane info = formGrid();
        addInfo(info, 0, "Mã KH", customer.getCustomerId());
        addInfo(info, 1, "Họ tên", customer.getFullName());
        addInfo(info, 2, "SĐT", customer.getPhone());
        addInfo(info, 3, "CCCD", valueOrNotUpdated(customer.getCccd()));
        addInfo(info, 4, "Email", valueOrNotUpdated(customer.getEmail()));
        addInfo(info, 5, "Địa chỉ", valueOrNotUpdated(customer.getAddress()));
        addInfo(info, 6, "Ngày tham gia", formatDateOrDash(customer.getCreatedAt()));
        addInfo(info, 7, "Cập nhật lần cuối", formatDateTime(customer.getUpdatedAt()));
        Label petCountValue = addInfoValue(info, 8, "Số thú cưng", String.valueOf(pets.size()));
        addInfo(info, 9, "Ghi chú", valueOrDash(customer.getNote()));

        Button edit = primaryButton("Cập nhật thông tin");
        Button addPet = secondaryButton("Thêm thú cưng mới");
        Button close = neutralButton("Đóng");
        addPet.setOnAction(e -> openPetForCustomerForm(customer, () -> {
            Customer refreshed = findCustomerById(customer.getCustomerId());
            Customer display = refreshed != null ? refreshed : customer;
            updateCustomerInLists(display);
            refreshLinkedPetList(display, petList, petCountValue);
        }));
        edit.setOnAction(e -> openEditCustomerForm(customer, stage));
        close.setOnAction(e -> stage.close());

        VBox content = new VBox(16,
                card("Thông tin khách hàng", info),
                petListCard(petList));
        content.getStyleClass().add("ph-detail-content");

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("ph-detail-scroll");

        BorderPane root = new BorderPane();
        root.getStyleClass().add("ph-modal-root");
        root.setTop(profileHeader("Hồ Sơ Khách Hàng", customer.getFullName(), customer.getCustomerId() + " · " + customer.getPhone() + " · Đang hoạt động", initials(customer.getFullName())));
        root.setCenter(scroll);
        root.setBottom(footer(edit, addPet, close));

        stage.setScene(new Scene(root, 820, 760));
        stage.setWidth(820);
        stage.setHeight(760);
        stage.setMinWidth(760);
        stage.setMinHeight(650);
        addStylesheet(stage);
        stage.showAndWait();
    }

    private VBox formShell(String title, String subtitle, String avatar, GridPane grid, Label error, Button save, Button cancel) {
        VBox body = new VBox(16, profileHeader(title, title, subtitle, avatar), card(null, grid), error, footer(save, cancel));
        body.getStyleClass().add("ph-modal-root");
        return body;
    }

    private void openEditCustomerForm(Customer customer, Stage detailStage) {
        Stage stage = modalStage("Cập Nhật Thông Tin Khách Hàng");
        Label error = errorLabel();
        TextField id = readOnlyField(customer.getCustomerId());
        TextField name = formField();
        TextField phone = formField();
        TextField cccd = formField();
        TextField email = formField();
        TextField address = formField();
        TextField joined = readOnlyField(formatDate(customer));
        TextField updatedAt = readOnlyField(formatDateTime(customer.getUpdatedAt()));
        TextArea note = formArea(3);

        name.setText(customer.getFullName());
        phone.setText(customer.getPhone());
        cccd.setText(customer.getCccd());
        cccd.setPromptText("12 chữ số");
        email.setText(customer.getEmail());
        address.setText(customer.getAddress());
        note.setText(customer.getNote());

        GridPane grid = formGrid();
        addRow(grid, 0, "Mã KH", id);
        addRow(grid, 1, "Họ tên *", name);
        addRow(grid, 2, "Số điện thoại *", phone);
        addRow(grid, 3, "Căn cước công dân *", cccd);
        addRow(grid, 4, "Email", email);
        addRow(grid, 5, "Địa chỉ", address);
        addRow(grid, 6, "Ghi chú", note);
        addRow(grid, 7, "Ngày tham gia", joined);
        addRow(grid, 8, "Cập nhật lần cuối", updatedAt);

        Button save = primaryButton("Lưu thay đổi");
        Button cancel = secondaryButton("Hủy");
        cancel.setOnAction(e -> stage.close());
        save.setOnAction(e -> {
            try {
                Customer updated = customerBUS.updateCustomer(
                        customer.getCustomerId(),
                        name.getText(),
                        phone.getText(),
                        cccd.getText(),
                        email.getText(),
                        address.getText(),
                        note.getText());
                Customer refreshed = findCustomerById(updated.getCustomerId());
                Customer display = refreshed != null ? refreshed : updated;
                updateCustomerInLists(display);
                stage.close();
                detailStage.close();
                openCustomerDetail(display);
            } catch (Exception ex) {
                error.setText(ex.getMessage());
            }
        });

        stage.setScene(new Scene(formShell("Cập Nhật Thông Tin", "Chỉnh sửa hồ sơ khách hàng", initials(customer.getFullName()), grid, error, save, cancel), 600, 720));
        addStylesheet(stage);
        stage.showAndWait();
    }

    private void updateCustomerInLists(Customer updated) {
        replaceCustomer(allCustomers, updated);
        replaceCustomer(customers, updated);
        updateCustomerPage(currentCustomerPageIndex, updated.getCustomerId());
        selectedCustomer = updated;
    }

    private void replaceCustomer(ObservableList<Customer> list, Customer updated) {
        for (int i = 0; i < list.size(); i++) {
            if (updated.getCustomerId().equals(list.get(i).getCustomerId())) {
                list.set(i, updated);
                return;
            }
        }
    }

    private VBox petListCard(VBox petList) {
        Label title = new Label("Danh sách thú cưng");
        title.getStyleClass().add("ph-card-title");
        Label hint = new Label("Double-click hoặc bấm Xem hồ sơ để xem chi tiết thú cưng");
        hint.getStyleClass().add("ph-card-hint");
        return card(null, new VBox(6, title, hint, petList));
    }

    private void populateLinkedPetList(Customer customer, VBox petList, List<Pet> pets) {
        petList.getChildren().clear();
        if (pets.isEmpty()) {
            Label empty = new Label("Chưa có thú cưng liên kết");
            empty.getStyleClass().add("ph-empty-state");
            petList.getChildren().add(empty);
            return;
        }
        for (Pet pet : pets) {
            petList.getChildren().add(linkedPetRow(pet, customer, petList));
        }
    }

    private HBox linkedPetRow(Pet pet, Customer customer, VBox petList) {
        Label line1 = new Label(pet.getPetId() + " - " + pet.getPetName());
        line1.getStyleClass().add("ph-pet-row-title");

        Label line2 = new Label(pet.getSpecies() + " / " + valueOrDash(pet.getBreed())
                + " · " + linkedPetHealthLabel(pet.getPetId())
                + " · Đang hoạt động");
        line2.getStyleClass().add("ph-pet-row-meta");
        line2.setWrapText(true);

        VBox text = new VBox(3, line1, line2);
        HBox.setHgrow(text, Priority.ALWAYS);

        Button open = secondaryButton("Xem hồ sơ");
        open.setMinWidth(112);
        open.setPrefWidth(112);
        open.setOnAction(e -> openLinkedPetDetail(pet, customer, petList));

        HBox row = new HBox(12, text, open);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("ph-list-row");
        row.setOnMouseClicked(e -> {
            if (e.getClickCount() >= 2) {
                openLinkedPetDetail(pet, customer, petList);
            }
        });
        return row;
    }

    private void openLinkedPetDetail(Pet pet, Customer customer, VBox petList) {
        try {
            Pet detail = petDAO.findById(pet.getPetId());
            openPetDetailDialog(detail != null ? detail : pet, () -> refreshLinkedPetList(customer, petList));
        } catch (Exception e) {
            showError("Không mở được hồ sơ thú cưng", e);
        }
    }

    private void refreshLinkedPetList(Customer customer, VBox petList) {
        refreshLinkedPetList(customer, petList, null);
    }

    private void refreshLinkedPetList(Customer customer, VBox petList, Label petCountLabel) {
        List<Pet> pets = loadPetsOfCustomer(customer);
        populateLinkedPetList(customer, petList, pets);
        if (petCountLabel != null) {
            petCountLabel.setText(String.valueOf(pets.size()));
        }
        updateCustomerPage(currentCustomerPageIndex, customer.getCustomerId());
        refreshPetTabIfLoaded();
    }

    private void refreshPetTabIfLoaded() {
        if (petTabController != null) {
            petTabController.markNeedsRefresh();
        }
    }

    private void openLinkedPetInPetScreen(String petId, Stage customerDetailStage) {
        MainController main = MainController.getActiveInstance();
        if (main == null) {
            showInfo("Không mở được màn Thú Cưng", "Không tìm thấy MainController hiện tại.");
            return;
        }
        customerDetailStage.close();
        showInfo("Thông báo", "Flow xem thú cưng hiện mở trực tiếp trong Hồ Sơ Khách Hàng.");
    }

    private void openPetDetailDialog(Pet pet, Runnable afterChanged) {
        Stage stage = modalStage("Chi Tiết Thú Cưng");
        Customer owner = findCustomerById(pet.getCustomerId());
        PetHealthRecord latest = latestHealthRecord(pet.getPetId());

        GridPane petInfo = formGrid();
        addInfo(petInfo, 0, "Mã thú cưng", pet.getPetId());
        addInfo(petInfo, 1, "Tên thú cưng", pet.getPetName());
        addInfo(petInfo, 2, "Loài", pet.getSpecies());
        addInfo(petInfo, 3, "Giống", valueOrDash(pet.getBreed()));
        addInfo(petInfo, 4, "Ngày sinh", "Chưa có cột trong DB");
        addInfo(petInfo, 5, "Cân nặng", pet.getWeightKg() == null ? "Chưa ghi nhận" : pet.getWeightKg() + " kg");
        addInfo(petInfo, 6, "Màu lông", "Chưa có cột trong DB");
        addInfo(petInfo, 7, "Trạng thái", "Đang hoạt động");
        addInfo(petInfo, 8, "Ghi chú", valueOrDash(pet.getSpecialNote()));

        GridPane ownerHealth = formGrid();
        addInfo(ownerHealth, 0, "Chủ sở hữu", owner == null ? "Chưa liên kết chủ sở hữu" : owner.getCustomerId() + " - " + owner.getFullName());
        addInfo(ownerHealth, 1, "SĐT chủ", owner == null ? "-" : owner.getPhone());
        addInfo(ownerHealth, 2, "Sức khỏe gần nhất", petHealthLabel(latest));
        addInfo(ownerHealth, 3, "Ngày ghi nhận", latest == null || latest.getRecordedAt() == null ? "-" : latest.getRecordedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        addInfo(ownerHealth, 4, "Bản ghi sức khỏe", latest == null ? "Chưa có ghi nhận sức khỏe." : valueOrDash(latest.getNote()));

        HBox columns = new HBox(14, card("Thông tin thú cưng", petInfo), card("Chủ sở hữu & sức khỏe", ownerHealth));
        HBox.setHgrow(columns.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(columns.getChildren().get(1), Priority.ALWAYS);

        Button edit = secondaryButton("Sửa thông tin");
        Button health = primaryButton(isPetCareStaff() ? "Ghi nhận sức khỏe" : "Xem ghi nhận");
        Button close = secondaryButton("Đóng");
        edit.setDisable(true);
        edit.setTooltip(new Tooltip("Chưa triển khai form sửa trong đợt này"));
        if (!isPetCareStaff()) {
            health.setTooltip(new Tooltip("Mở ghi nhận sức khỏe ở chế độ chỉ xem."));
        }
        close.setOnAction(e -> stage.close());
        health.setOnAction(e -> {
            stage.close();
            openCustomerPetHealthForm(pet, afterChanged);
        });

        VBox root = new VBox(16,
                profileHeader("Hồ Sơ Thú Cưng", pet.getPetName(), pet.getSpecies() + " / " + valueOrDash(pet.getBreed()) + " - " + petHealthLabel(latest), initials(pet.getPetName())),
                columns,
                footer(health, edit, close));
        root.getStyleClass().add("ph-modal-root");

        stage.setScene(new Scene(root, 780, 560));
        addStylesheet(stage);
        stage.showAndWait();
    }

    private void openCustomerPetHealthForm(Pet pet, Runnable afterChanged) {
        System.out.println("[HealthForm] open start");
        String fxmlPath = "/PetHotel/gui/view/HealthRecordForm.fxml";
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IllegalStateException("Không tìm thấy HealthRecordForm.fxml trong /PetHotel/gui/view/");
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            System.out.println("[HealthForm] fxml loaded");
            HealthRecordFormController controller = loader.getController();
            controller.setPet(pet);
            Role role = SessionManager.getInstance().getCurrentUser() == null
                    ? null
                    : SessionManager.getInstance().getCurrentUser().getRole();
            boolean editMode = role == Role.PET_CARE_STAFF;
            controller.setEditMode(editMode);
            controller.setOnSaved(() -> {
                if (afterChanged != null) afterChanged.run();
            });

            Stage stage = modalStage(editMode ? "Ghi Nhận Sức Khỏe" : "Xem Ghi Nhận Sức Khỏe");
            prepareHealthFormStage(stage, root);
            stage.setOnShown(e -> System.out.println("[HealthForm] shown"));
            stage.showAndWait();
            if (controller.isSaved()) {
                Pet refreshed = petDAO.findById(pet.getPetId());
                openPetDetailDialog(refreshed != null ? refreshed : pet, afterChanged);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Không mở được form ghi nhận sức khỏe", e);
        }
    }

    private void prepareHealthFormStage(Stage stage, Parent root) {
        if (root instanceof Region region) {
            region.setMinSize(720, 620);
            region.setPrefSize(760, 660);
        }
        Scene scene = new Scene(root, 760, 660);
        stage.setScene(scene);
        System.out.println("[HealthForm] scene set");
        stage.setMinWidth(720);
        stage.setMinHeight(620);
        root.applyCss();
        root.layout();
        root.snapshot(null, null);
        stage.sizeToScene();
        stage.centerOnScreen();
    }

    private PetHealthRecord latestHealthRecord(String petId) {
        try {
            return petBUS.getLatestHealthRecord(petId);
        } catch (Exception e) {
            return null;
        }
    }

    private String petHealthLabel(PetHealthRecord record) {
        if (record == null) return "Sức khỏe: Chưa ghi nhận";
        if (record.isHealthy()) return "Sức khỏe: Bình thường";
        String note = record.getNote() == null ? "" : record.getNote().toLowerCase();
        if (note.contains("bất thường") || note.contains("triệu chứng")) return "Sức khỏe: Bất thường";
        return "Sức khỏe: Cần theo dõi";
    }

    private String buildHealthNote(String status, String symptom, String note, String recorder) {
        String s = symptom == null || symptom.isBlank() ? "Không ghi nhận" : symptom.trim();
        String n = note == null ? "" : note.trim();
        return "Tình trạng: " + status + "\nTriệu chứng: " + s + "\nGhi chú: " + n + "\nNgười ghi nhận: " + recorder;
    }

    private int healthStatusValue(String status) {
        return "Bình thường".equals(status) ? 1 : 0;
    }

    private void validateHealthForm(String status, String symptom, String note, String bookingId) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn tình trạng tổng quát.");
        }
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("Mã booking không được để trống vì bảng PET_HEALTH_RECORD hiện yêu cầu booking_id.");
        }
        boolean hasSymptom = symptom != null && !symptom.isBlank();
        boolean hasNote = note != null && !note.isBlank();
        if (!"Bình thường".equals(status) && !hasSymptom && !hasNote) {
            throw new IllegalArgumentException("Vui lòng nhập triệu chứng hoặc ghi chú khi tình trạng cần theo dõi/bất thường.");
        }
    }

    private String linkedPetHealthLabel(String petId) {
        try {
            PetHealthRecord record = petHealthRecordDAO.findLatestByPetId(petId);
            if (record == null) return "Chưa ghi nhận";
            if (record.isHealthy()) return "Bình thường";
            String note = record.getNote() == null ? "" : record.getNote().toLowerCase();
            if (note.contains("bất thường") || note.contains("triệu chứng")) return "Bất thường";
            return "Cần theo dõi";
        } catch (Exception e) {
            return "Chưa ghi nhận";
        }
    }

    private VBox profileHeader(String eyebrow, String title, String subtitle, String avatarText) {
        Label line = new Label();
        line.getStyleClass().add("section-header-line");
        Label avatar = new Label(avatarText);
        avatar.getStyleClass().add("ph-profile-avatar");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("ph-profile-title");
        Label sub = new Label(subtitle);
        sub.getStyleClass().add("ph-profile-subtitle");
        VBox text = new VBox(4, new Label(eyebrow), titleLabel, sub);
        text.getChildren().get(0).getStyleClass().add("ph-eyebrow");
        HBox row = new HBox(12, line, avatar, text);
        row.setAlignment(Pos.CENTER_LEFT);
        VBox box = new VBox(row);
        box.getStyleClass().add("ph-profile-header");
        return box;
    }

    private VBox card(String title, javafx.scene.Node content) {
        VBox box = new VBox(12);
        box.getStyleClass().add("ph-content-card");
        if (title != null) {
            Label label = new Label(title);
            label.getStyleClass().add("ph-card-title");
            box.getChildren().add(label);
        }
        box.getChildren().add(content);
        return box;
    }

    private HBox footer(Button... buttons) {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox box = new HBox(10, spacer);
        box.getChildren().addAll(buttons);
        box.setAlignment(Pos.CENTER_RIGHT);
        box.getStyleClass().add("ph-form-footer");
        return box;
    }

    private GridPane formGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        return grid;
    }

    private void addRow(GridPane grid, int row, String label, javafx.scene.Node field) {
        Label l = new Label(label);
        l.getStyleClass().add("ph-form-label");
        grid.add(l, 0, row);
        grid.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    private void addInfo(GridPane grid, int row, String label, String value) {
        addInfoValue(grid, row, label, value);
    }

    private Label addInfoValue(GridPane grid, int row, String label, String value) {
        Label l = new Label(label);
        l.getStyleClass().add("ph-info-label");
        Label v = new Label(value);
        v.getStyleClass().add("ph-info-value");
        v.setWrapText(true);
        grid.add(l, 0, row);
        grid.add(v, 1, row);
        return v;
    }

    private Stage modalStage(String title) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        return stage;
    }

    private TextField formField() {
        TextField field = new TextField();
        field.getStyleClass().add("ph-form-input");
        return field;
    }

    private TextField readOnlyField(String value) {
        TextField field = formField();
        field.setText(value);
        field.setEditable(false);
        field.setFocusTraversable(false);
        field.getStyleClass().add("ph-form-readonly");
        return field;
    }

    private TextArea formArea(int rows) {
        TextArea area = new TextArea();
        area.setPrefRowCount(rows);
        area.setWrapText(true);
        area.getStyleClass().add("ph-form-input");
        return area;
    }

    private TextArea healthArea(int rows, double height) {
        TextArea area = formArea(rows);
        area.setPrefHeight(height);
        area.setMinHeight(height);
        area.setMaxHeight(height);
        area.getStyleClass().add("ph-health-text-area");
        return area;
    }

    private TextField healthField(String prompt) {
        TextField field = formField();
        field.setPromptText(prompt);
        field.setEditable(true);
        field.setDisable(false);
        field.setFocusTraversable(true);
        field.getStyleClass().add("ph-health-text-field");
        return field;
    }

    private Label errorLabel() {
        Label label = new Label();
        label.getStyleClass().add("ph-form-error");
        label.setWrapText(true);
        return label;
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().addAll("action-btn", "action-btn-primary");
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().addAll("action-btn", "action-btn-outline");
        return button;
    }

    private Button neutralButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().addAll("action-btn", "action-btn-neutral");
        return button;
    }

    private void addStylesheet(Stage stage) {
        stage.getScene().getStylesheets().add(getClass().getResource("/PetHotel/gui/css/style.css").toExternalForm());
    }

    private int countPets(String customerId) {
        try {
            return customerDAO.countPets(customerId);
        } catch (SQLException e) {
            return 0;
        }
    }

    private List<Pet> loadPetsOfCustomer(Customer customer) {
        try {
            return customerBUS.getPetsOfCustomer(customer.getCustomerId());
        } catch (Exception e) {
            return List.of();
        }
    }

    private Customer findCustomerById(String customerId) {
        try {
            return customerDAO.findById(customerId);
        } catch (SQLException e) {
            return null;
        }
    }

    private String previewNextCustomerId() {
        try {
            return customerDAO.generateNextCustomerId();
        } catch (SQLException e) {
            return "CUS001";
        }
    }

    private String formatDate(Customer customer) {
        return formatDateOrDash(customer.getCreatedAt());
    }

    private String formatDateOrDash(OffsetDateTime value) {
        return value == null ? "-" : value.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private String formatDateTime(OffsetDateTime value) {
        return value == null ? "-" : value.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private String initials(String value) {
        if (value == null || value.isBlank()) return "?";
        return value.trim().substring(0, 1).toUpperCase();
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String valueOrNotUpdated(String value) {
        return value == null || value.isBlank() ? "Chưa cập nhật" : value;
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private boolean isPetCareStaff() {
        return SessionManager.getInstance().hasRole(Role.PET_CARE_STAFF);
    }

    private void showCareStaffLimitedMessage() {
        showInfo("Không đủ quyền", "Chỉ nhân viên chăm sóc được ghi nhận sức khỏe thú cưng.");
    }

    private void showError(String title, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
