package PetHotel.gui.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import PetHotel.bus.GroomingBUS;
import PetHotel.model.AppUser;
import PetHotel.model.Customer;
import PetHotel.model.Employee;
import PetHotel.model.Pet;
import PetHotel.model.PetService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class GroomingBookingController {

    @FXML private ComboBox<Customer> cbCustomer;
    @FXML private ComboBox<Pet> cbPet;
    @FXML private ComboBox<PetService> cbService;
    @FXML private ComboBox<Employee> cbEmployee;
    @FXML private DatePicker dpScheduleDate;
    @FXML private TextField txtScheduleTime;
    @FXML private TextArea txtNote;

    private AppUser currentUser;
    private final GroomingBUS groomingBUS = new GroomingBUS();

    private String currentBranchId = "BR001";
    private Runnable onSuccess;

    public void setCurrentBranchId(String currentBranchId) {
        this.currentBranchId = currentBranchId;
    }

    public void setOnSuccess(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser == null) {
            showError("Chưa đăng nhập. Không thể đặt lịch grooming.");
            return;
        }

        setupComboBoxDisplay();

        dpScheduleDate.setValue(LocalDate.now());

        loadData();

        cbCustomer.setOnAction(e -> loadPetsByCustomer());
    }

    private void setupComboBoxDisplay() {
        cbCustomer.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Customer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getFullName() + " - " + item.getPhone());
            }
        });

        cbCustomer.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Customer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getFullName());
            }
        });

        cbPet.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Pet item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getPetName() + " - " + item.getSpecies());
            }
        });

        cbPet.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Pet item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getPetName());
            }
        });

        cbService.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(PetService item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getServiceName());
            }
        });

        cbService.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(PetService item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getServiceName());
            }
        });

        cbEmployee.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Employee item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getFullName());
            }
        });

        cbEmployee.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Employee item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getFullName());
            }
        });
    }

    private void loadData() {
        try {
            cbCustomer.setItems(FXCollections.observableArrayList(
                    groomingBUS.getAllCustomersForBooking(currentUser)
            ));

            cbService.setItems(FXCollections.observableArrayList(
                    groomingBUS.getGroomingServices(currentUser)
            ));

            cbEmployee.setItems(FXCollections.observableArrayList(
                    groomingBUS.getWorkingEmployeesByBranch(currentBranchId, currentUser)
            ));

        } catch (Exception e) {
            showError("Không thể tải dữ liệu: " + e.getMessage());
        }
    }

    private void loadPetsByCustomer() {
        Customer customer = cbCustomer.getValue();

        if (customer == null) {
            cbPet.getItems().clear();
            return;
        }

        try {
            cbPet.setItems(FXCollections.observableArrayList(
                    groomingBUS.getPetsByCustomer(customer.getCustomerId(), currentUser)
            ));
        } catch (Exception e) {
            showError("Không thể tải thú cưng: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        try {
            Customer customer = cbCustomer.getValue();
            Pet pet = cbPet.getValue();
            PetService service = cbService.getValue();
            Employee employee = cbEmployee.getValue();
            LocalDate date = dpScheduleDate.getValue();
            String timeText = txtScheduleTime.getText().trim();

            if (customer == null) {
                showWarning("Vui lòng chọn khách hàng.");
                return;
            }

            if (pet == null) {
                showWarning("Vui lòng chọn thú cưng.");
                return;
            }

            if (service == null) {
                showWarning("Vui lòng chọn dịch vụ grooming.");
                return;
            }

            if (employee == null) {
                showWarning("Vui lòng chọn nhân viên thực hiện.");
                return;
            }

            if (date == null) {
                showWarning("Vui lòng chọn ngày hẹn.");
                return;
            }

            if (timeText.isEmpty()) {
                showWarning("Vui lòng nhập giờ hẹn.");
                return;
            }

            LocalTime time;
            try {
                time = LocalTime.parse(timeText, DateTimeFormatter.ofPattern("H:mm"));
            } catch (Exception e) {
                showWarning("Giờ hẹn không hợp lệ. Nhập dạng HH:mm, ví dụ 09:30.");
                return;
            }

            groomingBUS.createGroomingSchedule(
                    customer.getCustomerId(),
                    pet.getPetId(),
                    service.getServiceId(),
                    employee.getEmployeeId(),
                    currentBranchId,
                    date,
                    time,
                    txtNote.getText(),
                    currentUser
            );

            showInfo("Đặt lịch grooming thành công.");

            if (onSuccess != null) {
                onSuccess.run();
            }

            closeWindow();

        } catch (Exception e) {
            showError("Không thể đặt lịch grooming: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cbCustomer.getScene().getWindow();
        stage.close();
    }

    private void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }

    private void showWarning(String message) {
        new Alert(Alert.AlertType.WARNING, message).showAndWait();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}