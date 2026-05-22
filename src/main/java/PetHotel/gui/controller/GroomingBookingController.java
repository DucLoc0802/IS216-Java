package PetHotel.gui.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.text.Normalizer;
import java.util.Locale;

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
    private List<PetService> currentCategoryServices = List.of();

    private String currentBranchId = "BR001";
    private Runnable onSuccess;

    public void setCurrentBranchId(String currentBranchId) {
        if (currentBranchId != null && !currentBranchId.trim().isEmpty()) {
            this.currentBranchId = currentBranchId.trim();
        }
        if (currentUser != null && cbEmployee != null) {
            loadEmployees();
        }
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
        cbPet.setOnAction(e -> refreshServicesForSelectedPet());
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

            loadEmployees();

        } catch (Exception e) {
            showError("Không thể tải dữ liệu: " + e.getMessage());
        }
    }

    private void loadEmployees() {
        try {
            List<Employee> employees = groomingBUS.getWorkingEmployeesByBranch(currentBranchId, currentUser);
            SearchableComboBoxUtil.setup(cbEmployee, employees, this::employeeDisplayText);
        } catch (Exception e) {
            showError("Không thể tải danh sách nhân viên chăm sóc: " + e.getMessage());
        }
    }

    private void loadPetsByCustomer() {
        Customer customer = SearchableComboBoxUtil.getSelectedOrExactTextMatch(cbCustomer);

        if (customer == null) {
            cbPet.setValue(null);
            cbPet.getEditor().clear();
            SearchableComboBoxUtil.setup(cbPet, List.of(), this::petDisplayText);
            refreshServicesForSelectedPet();
            return;
        }

        try {
            cbPet.setValue(null);
            cbPet.getEditor().clear();
            List<Pet> pets = groomingBUS.getPetsByCustomer(customer.getCustomerId(), currentUser);
            SearchableComboBoxUtil.setup(cbPet, pets, this::petDisplayText);
            refreshServicesForSelectedPet();
        } catch (Exception e) {
            showError("Không thể tải thú cưng: " + e.getMessage());
        }
    }

    private void loadServicesByCategory() {
        ServiceCategory category = SearchableComboBoxUtil.getSelectedOrExactTextMatch(cbServiceCategory);

        if (category == null) {
            currentCategoryServices = List.of();
            cbService.setValue(null);
            cbService.getEditor().clear();
            SearchableComboBoxUtil.setup(cbService, List.of(), this::serviceDisplayText);
            return;
        }

        try {
            cbService.setValue(null);
            cbService.getEditor().clear();
            currentCategoryServices = groomingBUS.getGroomingServicesByCategory(category.getServiceCategoryId(), currentUser);
            refreshServicesForSelectedPet();
        } catch (Exception e) {
            showError("Không thể tải dịch vụ grooming: " + e.getMessage());
        }
    }

    private void refreshServicesForSelectedPet() {
        Pet selectedPet = SearchableComboBoxUtil.getSelectedOrExactTextMatch(cbPet);
        PetService selectedService = SearchableComboBoxUtil.getSelectedOrExactTextMatch(cbService);

        List<PetService> filteredServices = currentCategoryServices.stream()
                .filter(service -> isServiceApplicableToPet(service, selectedPet))
                .toList();

        if (selectedService != null && !filteredServices.contains(selectedService)) {
            cbService.setValue(null);
            cbService.getEditor().clear();
        }

        SearchableComboBoxUtil.setup(cbService, filteredServices, this::serviceDisplayText);
    }

    @FXML
    private void handleSave() {
        try {
            Customer customer = SearchableComboBoxUtil.getSelectedOrExactTextMatch(cbCustomer);
            Pet pet = SearchableComboBoxUtil.getSelectedOrExactTextMatch(cbPet);
            PetService service = SearchableComboBoxUtil.getSelectedOrExactTextMatch(cbService);
            Employee employee = SearchableComboBoxUtil.getSelectedOrExactTextMatch(cbEmployee);
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

            if (!isServiceApplicableToPet(service, pet)) {
                showWarning("Dịch vụ đã chọn không phù hợp với loài của thú cưng.");
                return;
            }

            if (employee == null) {
                showWarning("Vui lòng chọn nhân viên chăm sóc.");
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
        if (service == null) {
            return "";
        }
        return joinParts(service.getServiceName(), service.getSpecies());
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

    private boolean isServiceApplicableToPet(PetService service, Pet pet) {
        if (service == null) {
            return false;
        }
        if (pet == null) {
            return true;
        }

        String serviceSpecies = normalizeSpecies(service.getSpecies());
        String petSpecies = normalizeSpecies(pet.getSpecies());

        if (serviceSpecies.isEmpty() || "ALL".equals(serviceSpecies)) {
            return true;
        }

        return serviceSpecies.equals(petSpecies);
    }

    private String normalizeSpecies(String value) {
        String normalized = normalizeText(value);
        if (normalized.isEmpty()) {
            return "";
        }
        if ("dog".equals(normalized) || "cho".equals(normalized)) {
            return "DOG";
        }
        if ("cat".equals(normalized) || "meo".equals(normalized)) {
            return "CAT";
        }
        if ("all".equals(normalized) || "tat ca".equals(normalized) || "both".equals(normalized)) {
            return "ALL";
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT);
    }
}
