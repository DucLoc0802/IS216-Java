package PetHotel.gui.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import PetHotel.bus.GroomingBUS;
import PetHotel.model.AppUser;
import PetHotel.model.Customer;
import PetHotel.model.Employee;
import PetHotel.model.Pet;
import PetHotel.model.PetService;
import PetHotel.model.ServiceCategory;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GroomingBookingController {

    @FXML private ComboBox<Customer> cbCustomer;
    @FXML private ComboBox<Pet> cbPet;
    @FXML private ComboBox<ServiceCategory> cbServiceCategory;
    @FXML private ComboBox<PetService> cbService;
    @FXML private ComboBox<Employee> cbEmployee;
    @FXML private DatePicker dpScheduleDate;
    @FXML private TextField txtScheduleTime;
    @FXML private TextArea txtNote;

    private AppUser currentUser;
    private final GroomingBUS groomingBUS = new GroomingBUS();
    private ServiceCategory selectedServiceCategory;

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
        cbServiceCategory.setOnAction(e -> loadServicesByCategory());
    }

    private void setupComboBoxDisplay() {
        SearchableComboBoxUtil.setup(cbCustomer, List.of(), this::customerDisplayText);
        SearchableComboBoxUtil.setup(cbPet, List.of(), this::petDisplayText);
        SearchableComboBoxUtil.setup(cbServiceCategory, List.of(), this::categoryDisplayText);
        SearchableComboBoxUtil.setup(cbService, List.of(), this::serviceDisplayText);
        SearchableComboBoxUtil.setup(cbEmployee, List.of(), this::employeeDisplayText);
    }

    private void loadData() {
        try {
            List<Customer> customers = groomingBUS.getAllCustomersForBooking(currentUser);
            SearchableComboBoxUtil.setup(cbCustomer, customers, this::customerDisplayText);

            List<ServiceCategory> categories = groomingBUS.getGroomingServiceCategories(currentUser);
            SearchableComboBoxUtil.setup(cbServiceCategory, categories, this::categoryDisplayText);

            List<Employee> employees = groomingBUS.getWorkingEmployeesByBranch(currentBranchId, currentUser);
            SearchableComboBoxUtil.setup(cbEmployee, employees, this::employeeDisplayText);

        } catch (Exception e) {
            showError("Không thể tải dữ liệu: " + e.getMessage());
        }
    }

    private void loadPetsByCustomer() {
        Customer customer = cbCustomer.getValue();

        if (customer == null) {
            cbPet.setValue(null);
            cbPet.getEditor().clear();
            SearchableComboBoxUtil.setup(cbPet, List.of(), this::petDisplayText);
            return;
        }

        try {
            cbPet.setValue(null);
            cbPet.getEditor().clear();
            List<Pet> pets = groomingBUS.getPetsByCustomer(customer.getCustomerId(), currentUser);
            SearchableComboBoxUtil.setup(cbPet, pets, this::petDisplayText);
        } catch (Exception e) {
            showError("Không thể tải thú cưng: " + e.getMessage());
        }
    }

    private void loadServicesByCategory() {
        ServiceCategory category = cbServiceCategory.getValue();

        if (category == null) {
            cbService.setValue(null);
            cbService.getEditor().clear();
            SearchableComboBoxUtil.setup(cbService, List.of(), this::serviceDisplayText);
            return;
        }

        try {
            cbService.setValue(null);
            cbService.getEditor().clear();
            List<PetService> services = groomingBUS.getGroomingServicesByCategory(category.getServiceCategoryId(), currentUser);
            SearchableComboBoxUtil.setup(cbService, services, this::serviceDisplayText);
        } catch (Exception e) {
            showError("Không thể tải dịch vụ grooming: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        try {
            Customer customer = cbCustomer.getValue();
            Pet pet = cbPet.getValue();
            PetService service = cbService.getValue();
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
                    null,
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

    private String customerDisplayText(Customer customer) {
        if (customer == null) {
            return "";
        }
        return joinParts(customer.getFullName(), customer.getPhone());
    }

    private String petDisplayText(Pet pet) {
        if (pet == null) {
            return "";
        }
        return joinParts(pet.getPetName(), pet.getSpecies());
    }

    private String categoryDisplayText(ServiceCategory category) {
        return category == null ? "" : valueOrEmpty(category.getCategoryName());
    }

    private String serviceDisplayText(PetService service) {
        return service == null ? "" : valueOrEmpty(service.getServiceName());
    }

    private String employeeDisplayText(Employee employee) {
        if (employee == null) {
            return "";
        }
        return joinParts(employee.getEmployeeId(), employee.getFullName());
    }

    private String joinParts(String first, String second) {
        String firstValue = valueOrEmpty(first);
        String secondValue = valueOrEmpty(second);
        if (secondValue.isEmpty()) {
            return firstValue;
        }
        if (firstValue.isEmpty()) {
            return secondValue;
        }
        return firstValue + " - " + secondValue;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
