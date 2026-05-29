package PetHotel.bus;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import PetHotel.dao.BookingServiceDAO;
import PetHotel.dao.ServiceCategoryDAO;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.model.BookingService;
import PetHotel.model.Customer;
import PetHotel.model.Employee;
import PetHotel.model.Pet;
import PetHotel.model.PetService;
import PetHotel.model.ServiceCategory;
import PetHotel.util.Role;

/**
 * GroomingBUS — Xử lý logic quản lý lịch grooming.
 * 
 * Hỗ trợ:
 *  - Lấy lịch grooming theo ngày, nhân viên, trạng thái
 *  - Cập nhật trạng thái (PENDING → SCHEDULED → IN_PROGRESS → DONE)
 *  - Kiểm soát quyền truy cập theo role
 */
public class GroomingBUS {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final BookingServiceDAO bookingServiceDAO = new BookingServiceDAO();
    private final ServiceCategoryDAO serviceCategoryDAO = new ServiceCategoryDAO();

    /**
     * Lấy danh sách lịch grooming theo ngày
     * @param dateStr Định dạng yyyy-MM-dd
     * @param employeeId null = tất cả
     * @param status null = tất cả
     * @param currentUser Người dùng hiện tại (để kiểm tra quyền)
     */
    public List<BookingService> getGroomingScheduleByDate(
        String dateStr,
        String employeeId,
        String status,
        AppUser currentUser
    ) throws SQLException, ValidationException {

        if (currentUser == null) {
            throw new ValidationException("Chưa đăng nhập");
        }

        if (!currentUser.hasRole(Role.RECEPTIONIST)
                && !currentUser.hasRole(Role.PET_CARE_STAFF)
                && !currentUser.hasRole(Role.BRANCH_MANAGER)
                && !currentUser.hasRole(Role.ADMIN)) {
            throw new ValidationException("Bạn không có quyền xem lịch grooming");
        }

        if (!isValidDateFormat(dateStr)) {
            throw new ValidationException("Định dạng ngày không hợp lệ (yyyy-MM-dd)");
        }

        /*
        * Chỉ đúng role Nhân viên chăm sóc mới bị giới hạn xem lịch của chính mình.
        */
        if (currentUser.getRole() == Role.PET_CARE_STAFF) {
            employeeId = currentUser.getEmployeeId();
        }

        System.out.println("DEBUG BUS role = " + currentUser.getRole());
        System.out.println("DEBUG BUS employeeId after role filter = " + employeeId);
        System.out.println("DEBUG BUS status = " + status);
        System.out.println("DEBUG BUS dateStr = " + dateStr);

        return bookingServiceDAO.findByDateAndFilter(dateStr, employeeId, status);
    }

    /**
     * Lấy toàn bộ lịch grooming, không giới hạn ngày.
     * @param employeeId null = tất cả
     * @param status null = tất cả
     * @param currentUser Người dùng hiện tại (để kiểm tra quyền)
     */
    public List<BookingService> getAllGroomingSchedules(
        String employeeId,
        String status,
        AppUser currentUser
    ) throws SQLException, ValidationException {

        if (currentUser == null) {
            throw new ValidationException("Chưa đăng nhập");
        }

        if (!currentUser.hasRole(Role.RECEPTIONIST)
                && !currentUser.hasRole(Role.PET_CARE_STAFF)
                && !currentUser.hasRole(Role.BRANCH_MANAGER)
                && !currentUser.hasRole(Role.ADMIN)) {
            throw new ValidationException("Bạn không có quyền xem lịch grooming");
        }

        if (currentUser.getRole() == Role.PET_CARE_STAFF) {
            employeeId = currentUser.getEmployeeId();
        }

        return bookingServiceDAO.findAllAndFilter(employeeId, status);
    }

    /**
     * Đếm lịch grooming chờ xử lý hôm nay
     */
    public int getPendingCountToday() throws SQLException {
        return bookingServiceDAO.countPendingToday();
    }

    /**
     * Lấy lịch grooming của nhân viên trong ngày hôm nay
     */
    public List<BookingService> getEmployeeScheduleToday(String employeeId) throws SQLException {
        return bookingServiceDAO.findByEmployeeToday(employeeId);
    }

    /**
     * Cập nhật trạng thái lịch grooming
     * @param bookingServiceId Mã lịch dịch vụ
     * @param newStatus Trạng thái mới (PENDING, SCHEDULED, IN_PROGRESS, DONE, CANCELLED)
     * @param currentUser Người dùng hiện tại
     */
    public void updateGroomingStatus(String bookingServiceId, String newStatus, AppUser currentUser)
            throws SQLException, ValidationException {
        
        if (currentUser == null) {
            throw new ValidationException("Chưa đăng nhập");
        }

        // Kiểm tra quyền
        if (!isValidStatusTransition(newStatus)) {
            throw new ValidationException("Trạng thái không hợp lệ");
        }

        if (BookingService.STATUS_CANCELLED.equals(newStatus)) {
            cancelGroomingSchedule(bookingServiceId, currentUser);
            return;
        }

        // Lễ tân: có thể tạo/hủy
        // Nhân viên chăm sóc: có thể cập nhật trạng thái (PENDING → IN_PROGRESS → DONE)
        // Quản lý chi nhánh: quản lý tất cả
        if (!currentUser.hasRole(Role.RECEPTIONIST) && 
            !currentUser.hasRole(Role.PET_CARE_STAFF) &&
            !currentUser.hasRole(Role.BRANCH_MANAGER) &&
            !currentUser.hasRole(Role.ADMIN)) {
            throw new ValidationException("Bạn không có quyền cập nhật lịch grooming");
        }

        if (bookingServiceId == null || bookingServiceId.trim().isEmpty()) {
            throw new ValidationException("Mã lịch grooming không hợp lệ");
        }

        BookingService schedule = bookingServiceDAO.findById(bookingServiceId.trim());
        if (schedule == null) {
            throw new ValidationException("Không tìm thấy lịch grooming cần cập nhật");
        }

        if (BookingService.STATUS_IN_PROGRESS.equals(newStatus)
                && (schedule.getEmployeeId() == null || schedule.getEmployeeId().trim().isEmpty())) {
            throw new ValidationException("Chỉ có thể bắt đầu lịch grooming sau khi đã phân công nhân viên chăm sóc");
        }

        bookingServiceDAO.updateStatus(bookingServiceId.trim(), newStatus);
    }

    /**
     * Hủy lịch grooming khi khách thay đổi yêu cầu.
     * Chỉ lễ tân, quản lý chi nhánh và admin được hủy lịch.
     */
    public void cancelGroomingSchedule(String bookingServiceId, AppUser currentUser)
            throws SQLException, ValidationException {

        if (currentUser == null) {
            throw new ValidationException("Chưa đăng nhập");
        }

        if (!currentUser.hasRole(Role.RECEPTIONIST)
                && !currentUser.hasRole(Role.BRANCH_MANAGER)
                && !currentUser.hasRole(Role.ADMIN)) {
            throw new ValidationException("Bạn không có quyền hủy lịch grooming");
        }

        if (bookingServiceId == null || bookingServiceId.trim().isEmpty()) {
            throw new ValidationException("Mã lịch grooming không hợp lệ");
        }

        BookingService schedule = bookingServiceDAO.findById(bookingServiceId.trim());
        if (schedule == null) {
            throw new ValidationException("Không tìm thấy lịch grooming cần hủy");
        }

        if (BookingService.STATUS_DONE.equals(schedule.getStatus())) {
            throw new ValidationException("Lịch grooming đã hoàn thành, không thể hủy");
        }

        if (BookingService.STATUS_CANCELLED.equals(schedule.getStatus())) {
            throw new ValidationException("Lịch grooming đã được hủy trước đó");
        }

        bookingServiceDAO.updateStatus(bookingServiceId.trim(), BookingService.STATUS_CANCELLED);
    }

    /**
     * Kiểm tra trạng thái hợp lệ
     */
    private boolean isValidStatusTransition(String status) {
        return BookingService.STATUS_PENDING.equals(status) ||
               BookingService.STATUS_SCHEDULED.equals(status) ||
               BookingService.STATUS_IN_PROGRESS.equals(status) ||
               BookingService.STATUS_DONE.equals(status) ||
               BookingService.STATUS_CANCELLED.equals(status);
    }

    /**
     * Validate định dạng ngày
     */
    private boolean isValidDateFormat(String dateStr) {
        try {
            LocalDate.parse(dateStr, DATE_FORMAT);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
/**
 * Lấy danh sách khách hàng để chọn khi đặt lịch grooming.
 * Role dùng: Lễ tân, Quản lý chi nhánh, Admin.
 */
public List<Customer> getAllCustomersForBooking(AppUser currentUser)
        throws SQLException, ValidationException {

    if (!currentUser.hasRole(Role.RECEPTIONIST)
            && !currentUser.hasRole(Role.BRANCH_MANAGER)
            && !currentUser.hasRole(Role.ADMIN)) {
        throw new ValidationException("Bạn không có quyền tạo lịch grooming");
    }

    return bookingServiceDAO.getAllCustomers();
}

/**
 * Lấy danh sách thú cưng theo khách hàng.
 */
public List<Pet> getPetsByCustomer(String customerId, AppUser currentUser)
        throws SQLException, ValidationException {

    if (!currentUser.hasRole(Role.RECEPTIONIST)
            && !currentUser.hasRole(Role.BRANCH_MANAGER)
            && !currentUser.hasRole(Role.ADMIN)) {
        throw new ValidationException("Bạn không có quyền xem thú cưng của khách hàng");
    }

    if (customerId == null || customerId.trim().isEmpty()) {
        throw new ValidationException("Mã khách hàng không hợp lệ");
    }

    return bookingServiceDAO.getPetsByCustomer(customerId);
}

/**
 * Lấy danh sách dịch vụ grooming đang hoạt động.
 */
public List<PetService> getGroomingServices(AppUser currentUser)
        throws SQLException, ValidationException {

    if (!currentUser.hasRole(Role.RECEPTIONIST)
            && !currentUser.hasRole(Role.BRANCH_MANAGER)
            && !currentUser.hasRole(Role.ADMIN)) {
        throw new ValidationException("Bạn không có quyền xem dịch vụ grooming");
    }

    return bookingServiceDAO.getGroomingServices();
}

/**
 * Lấy danh sách nhân viên đang làm việc tại chi nhánh.
 */
public List<Employee> getWorkingEmployeesByBranch(String branchId, AppUser currentUser)
        throws SQLException, ValidationException {

    if (!currentUser.hasRole(Role.RECEPTIONIST)
            && !currentUser.hasRole(Role.BRANCH_MANAGER)
            && !currentUser.hasRole(Role.ADMIN)) {
        throw new ValidationException("Bạn không có quyền xem danh sách nhân viên");
    }

    if (branchId == null || branchId.trim().isEmpty()) {
        throw new ValidationException("Mã chi nhánh không hợp lệ");
    }

    return bookingServiceDAO.getWorkingEmployeesByBranch(branchId);
}

/**
 * Tạo lịch grooming mới.
 * Luồng xử lý:
 * 1. Tạo booking
 * 2. Tạo booking_services
 */
public void createGroomingSchedule(
        String customerId,
        String petId,
        String serviceId,
        String employeeId,
        String branchId,
        LocalDate scheduleDate,
        LocalTime scheduleTime,
        String note,
        AppUser currentUser
) throws SQLException, ValidationException {

    // Chỉ lễ tân/quản lý/admin được tạo lịch
    if (!currentUser.hasRole(Role.RECEPTIONIST)
            && !currentUser.hasRole(Role.BRANCH_MANAGER)
            && !currentUser.hasRole(Role.ADMIN)) {
        throw new ValidationException("Bạn không có quyền đặt lịch grooming");
    }

    if (customerId == null || customerId.trim().isEmpty()) {
        throw new ValidationException("Vui lòng chọn khách hàng");
    }

    if (petId == null || petId.trim().isEmpty()) {
        throw new ValidationException("Vui lòng chọn thú cưng");
    }

    if (serviceId == null || serviceId.trim().isEmpty()) {
        throw new ValidationException("Vui lòng chọn dịch vụ grooming");
    }

    if (branchId == null || branchId.trim().isEmpty()) {
        throw new ValidationException("Không xác định được chi nhánh");
    }

    if (scheduleDate == null) {
        throw new ValidationException("Vui lòng chọn ngày hẹn");
    }

    if (scheduleTime == null) {
        throw new ValidationException("Vui lòng nhập giờ hẹn");
    }

    if (scheduleDate.isBefore(LocalDate.now())) {
        throw new ValidationException("Không thể đặt lịch grooming ở ngày quá khứ");
    }

    // Kiểm tra xem dịch vụ đã được kích hoạt chưa
    if (!bookingServiceDAO.isServiceActive(serviceId.trim())) {
        throw new ValidationException("Dịch vụ này hiện không khả dụng. Vui lòng chọn dịch vụ khác hoặc liên hệ quản lý chi nhánh.");
    }

    bookingServiceDAO.createGroomingSchedule(
            customerId,
            petId,
            serviceId,
            employeeId,
            branchId,
            scheduleDate,
            scheduleTime,
            note
    );
}

/**
 * Lấy danh sách loại dịch vụ grooming
 */
public List<ServiceCategory> getGroomingServiceCategories(AppUser currentUser)
        throws SQLException, ValidationException {

    if (!currentUser.hasRole(Role.RECEPTIONIST)
            && !currentUser.hasRole(Role.BRANCH_MANAGER)
            && !currentUser.hasRole(Role.ADMIN)) {
        throw new ValidationException("Bạn không có quyền xem loại dịch vụ grooming");
    }

    return serviceCategoryDAO.findGroomingCategories();
}

/**
 * Lấy danh sách dịch vụ grooming theo loại dịch vụ
 */
public List<PetService> getGroomingServicesByCategory(String serviceCategoryId, AppUser currentUser)
        throws SQLException, ValidationException {

    if (!currentUser.hasRole(Role.RECEPTIONIST)
            && !currentUser.hasRole(Role.BRANCH_MANAGER)
            && !currentUser.hasRole(Role.ADMIN)) {
        throw new ValidationException("Bạn không có quyền xem dịch vụ grooming");
    }

    if (serviceCategoryId == null || serviceCategoryId.trim().isEmpty()) {
        throw new ValidationException("Loại dịch vụ không hợp lệ");
    }

    return bookingServiceDAO.getGroomingServicesByCategory(serviceCategoryId);
}

/**
 * Lấy danh sách công việc grooming chưa phân công
 */
public List<BookingService> getUnassignedGroomingTasks(
        String dateStr,
        String status,
        String keyword,
        AppUser currentUser
) throws SQLException, ValidationException {

    if (!currentUser.hasRole(Role.BRANCH_MANAGER)
            && !currentUser.hasRole(Role.ADMIN)) {
        throw new ValidationException("Bạn không có quyền xem danh sách công việc chưa phân công");
    }

    if (!isValidDateFormat(dateStr)) {
        throw new ValidationException("Định dạng ngày không hợp lệ (yyyy-MM-dd)");
    }

    return bookingServiceDAO.findUnassignedGroomingTasks(dateStr, status, keyword);
}

/**
 * Phân công nhân viên cho công việc grooming
 */
public void assignEmployeeToTask(
        String bookingServiceId,
        String employeeId,
        String note,
        AppUser currentUser
) throws SQLException, ValidationException {

    if (!currentUser.hasRole(Role.BRANCH_MANAGER)
            && !currentUser.hasRole(Role.ADMIN)) {
        throw new ValidationException("Bạn không có quyền phân công nhân viên");
    }

    if (bookingServiceId == null || bookingServiceId.trim().isEmpty()) {
        throw new ValidationException("Mã công việc không hợp lệ");
    }

    if (employeeId == null || employeeId.trim().isEmpty()) {
        throw new ValidationException("Mã nhân viên không hợp lệ");
    }

    bookingServiceDAO.assignEmployeeToTask(bookingServiceId, employeeId, note);
}

/**
 * Đếm số công việc của nhân viên trong một ngày
 */
public int getEmployeeTaskCount(String employeeId, String dateStr, AppUser currentUser)
        throws SQLException, ValidationException {

    if (employeeId == null || employeeId.trim().isEmpty()) {
        throw new ValidationException("Mã nhân viên không hợp lệ");
    }

    if (!isValidDateFormat(dateStr)) {
        throw new ValidationException("Định dạng ngày không hợp lệ (yyyy-MM-dd)");
    }

    return bookingServiceDAO.countEmployeeTasksByDate(employeeId, dateStr);
}

/**
 * Lấy danh sách công việc được phân công cho nhân viên
 */
public List<BookingService> getEmployeeAssignedTasks(
        String employeeId,
        String dateStr,
        String status,
        String keyword,
        AppUser currentUser
) throws SQLException, ValidationException {

    if (!currentUser.hasRole(Role.PET_CARE_STAFF)
            && !currentUser.hasRole(Role.BRANCH_MANAGER)
            && !currentUser.hasRole(Role.ADMIN)) {
        throw new ValidationException("Bạn không có quyền xem danh sách công việc được phân công");
    }

    // Nhân viên chăm sóc chỉ xem công việc của mình
    if (currentUser.getRole() == Role.PET_CARE_STAFF
        && !employeeId.equals(currentUser.getEmployeeId())) {
    throw new ValidationException("Bạn chỉ có thể xem công việc của mình");
    }

    if (employeeId == null || employeeId.trim().isEmpty()) {
        throw new ValidationException("Mã nhân viên không hợp lệ");
    }

    if (!isValidDateFormat(dateStr)) {
        throw new ValidationException("Định dạng ngày không hợp lệ (yyyy-MM-dd)");
    }

    return bookingServiceDAO.findEmployeeAssignedTasks(employeeId, dateStr, status, keyword);
}

/**
 * Đếm số công việc của nhân viên theo trạng thái trong một ngày
 */
public int getEmployeeTaskCountByStatus(
        String employeeId,
        String dateStr,
        String status,
        AppUser currentUser
) throws SQLException, ValidationException {

    if (employeeId == null || employeeId.trim().isEmpty()) {
        throw new ValidationException("Mã nhân viên không hợp lệ");
    }

    if (!isValidDateFormat(dateStr)) {
        throw new ValidationException("Định dạng ngày không hợp lệ (yyyy-MM-dd)");
    }

    return bookingServiceDAO.countEmployeeTasksByDateAndStatus(employeeId, dateStr, status);
}

/**
 * Xác nhận hoàn thành dịch vụ grooming với trừ tồn kho.
 *
 * Luồng xử lý trong transaction:
 * 1. Lấy thông tin booking_service, pet, service
 * 2. Kiểm tra quyền (chỉ PET_CARE_STAFF/BRANCH_MANAGER/ADMIN, và PET_CARE_STAFF chỉ hoàn thành công việc của mình)
 * 3. Kiểm tra status = IN_PROGRESS
 * 4. Lấy danh sách vật tư tiêu hao từ SERVICE_PRODUCT_STANDARD
 * 5. Kiểm tra tồn kho cho tất cả vật tư có actualAmount > 0
 * 6. Nếu đủ tồn: trừ inventory + update status DONE + ghi note
 * 7. Nếu không đủ: rollback + throw exception
 *
 * @param bookingServiceId     Mã công việc dịch vụ
 * @param materialRows         Danh sách vật tư với số lượng thực tế (từ dialog)
 * @param completionNote       Ghi chú bổ sung từ dialog (nếu có)
 * @param currentUser          Người dùng đang đăng nhập
 * @throws ValidationException Nếu vi phạm quy tắc nghiệp vụ
 * @throws SQLException        Nếu lỗi database hoặc không đủ tồn kho
 */
public void completeGroomingServiceWithMaterials(
        String bookingServiceId,
        java.util.List<PetHotel.model.MaterialUsageConfirmRow> materialRows,
        String completionNote,
        AppUser currentUser
) throws ValidationException, SQLException {

    if (currentUser == null) {
        throw new ValidationException("Chưa đăng nhập");
    }

    // Kiểm tra quyền
    if (!currentUser.hasRole(Role.PET_CARE_STAFF)
            && !currentUser.hasRole(Role.BRANCH_MANAGER)
            && !currentUser.hasRole(Role.ADMIN)) {
        throw new ValidationException("Bạn không có quyền xác nhận hoàn thành dịch vụ");
    }

    if (bookingServiceId == null || bookingServiceId.trim().isEmpty()) {
        throw new ValidationException("Mã công việc không hợp lệ");
    }

    // Lấy thông tin chi tiết để validate
    PetHotel.dao.BookingServiceDAO bsDAO = new PetHotel.dao.BookingServiceDAO();
    PetHotel.model.BookingService bs = bsDAO.findCompletionContext(bookingServiceId.trim());

    if (bs == null) {
        throw new ValidationException("Không tìm thấy công việc dịch vụ");
    }

    // Nhân viên chăm sóc chỉ hoàn thành công việc của mình
    if (currentUser.getRole() == Role.PET_CARE_STAFF
            && !bs.getEmployeeId().equals(currentUser.getEmployeeId())) {
        throw new ValidationException("Bạn chỉ có thể hoàn thành công việc của mình");
    }

    // Chỉ hoàn thành nếu status = IN_PROGRESS
    if (!BookingService.STATUS_IN_PROGRESS.equals(bs.getStatus())) {
        throw new ValidationException("Chỉ công việc đang thực hiện (IN_PROGRESS) mới có thể hoàn thành");
    }

    // Tham chiếu branch_id từ booking
    String branchId = bs.getBranchId();
    if ((branchId == null || branchId.trim().isEmpty()) && bs instanceof PetHotel.model.BookingService) {
        // Sử dụng reflection hoặc getter để lấy branchId
        // Vì BookingService có thể không có field branchId trực tiếp
        // ta cần lấy từ booking
        PetHotel.dao.BookingDAO bookingDAO = new PetHotel.dao.BookingDAO();
        PetHotel.model.Booking booking = bookingDAO.findById(bs.getBookingId());
        if (booking != null) {
            branchId = booking.getBranchId();
        }
    }

    if (branchId == null || branchId.trim().isEmpty()) {
        throw new ValidationException("Không xác định được chi nhánh");
    }

    // Kiểm tra tồn kho và trừ kho trong transaction
    branchId = branchId.trim();

    java.sql.Connection conn = null;
    try {
        conn = PetHotel.util.DBConnection.getConnection();
        conn.setAutoCommit(false);

        PetHotel.dao.InventoryDAO invDAO = new PetHotel.dao.InventoryDAO();

        // Kiểm tra tồn kho cho tất cả vật tư có actualAmount > 0
        java.util.List<String> missingProducts = new java.util.ArrayList<>();
        if (materialRows != null) {
            for (PetHotel.model.MaterialUsageConfirmRow row : materialRows) {
                validateMaterialRow(row);
                java.math.BigDecimal actualAmount = toInventoryQuantity(row);
                if (actualAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
                    java.math.BigDecimal currentQty = invDAO.getQuantity(branchId, row.getProductId(), conn);
                    if (currentQty.compareTo(actualAmount) < 0) {
                        missingProducts.add(row.getProductName() + " (tồn: " + currentQty + ", cần: " + actualAmount + ")");
                    }
                }
            }
        }

        if (!missingProducts.isEmpty()) {
            throw new SQLException("Tồn kho không đủ cho sản phẩm: " + String.join(", ", missingProducts));
        }

        // Trừ kho
        if (materialRows != null) {
            for (PetHotel.model.MaterialUsageConfirmRow row : materialRows) {
                java.math.BigDecimal actualAmount = toInventoryQuantity(row);
                if (actualAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
                    int updated = invDAO.subtractInventory(branchId, row.getProductId(), actualAmount, conn);
                    if (updated == 0) {
                        throw new SQLException("Không thể trừ tồn sản phẩm: " + row.getProductName());
                    }
                }
            }
        }

        // Ghi thông tin vật tư vào note
        String noteContent = buildMaterialNote(bs, materialRows, completionNote);

        // Update booking_services: status = DONE, append note
        bsDAO.completeServiceAndAppendNote(bookingServiceId.trim(), noteContent, conn);

        conn.commit();

    } catch (SQLException e) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {}
        }
        throw e;
    } finally {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException ignored) {}
        }
    }
}

/**
 * Xây dựng nội dung ghi chú vật tư sử dụng.
 */
private void validateMaterialRow(PetHotel.model.MaterialUsageConfirmRow row)
        throws ValidationException {
    if (row == null) {
        throw new ValidationException("Du lieu vat tu khong hop le.");
    }

    if (row.getProductId() == null || row.getProductId().trim().isEmpty()) {
        throw new ValidationException("Vat tu chua co ma san pham.");
    }

    java.math.BigDecimal actualAmount = row.getActualAmount();
    if (actualAmount == null) {
        row.setActualAmount(java.math.BigDecimal.ZERO);
        return;
    }

    if (actualAmount.compareTo(java.math.BigDecimal.ZERO) < 0) {
        throw new ValidationException("So luong vat tu khong duoc nho hon 0.");
    }
}

private java.math.BigDecimal toInventoryQuantity(PetHotel.model.MaterialUsageConfirmRow row) {
    java.math.BigDecimal amount = row.getActualAmount();
    if (amount == null) {
        return java.math.BigDecimal.ZERO;
    }

    String fromUnit = normalizeUnit(row.getStandardUnit());
    String toUnit = normalizeUnit(row.getProductUnit());
    if (toUnit == null) {
        toUnit = baseUnit(fromUnit);
    }

    if (fromUnit == null || fromUnit.equals(toUnit)) {
        return amount;
    }

    if ("KG".equals(fromUnit) && "G".equals(toUnit)) {
        return amount.multiply(java.math.BigDecimal.valueOf(1000));
    }
    if ("G".equals(fromUnit) && "KG".equals(toUnit)) {
        return amount.divide(java.math.BigDecimal.valueOf(1000));
    }
    if ("L".equals(fromUnit) && "ML".equals(toUnit)) {
        return amount.multiply(java.math.BigDecimal.valueOf(1000));
    }
    if ("ML".equals(fromUnit) && "L".equals(toUnit)) {
        return amount.divide(java.math.BigDecimal.valueOf(1000));
    }

    return amount;
}

private String normalizeUnit(String unit) {
    if (unit == null || unit.trim().isEmpty()) {
        return null;
    }
    return unit.trim().toUpperCase(java.util.Locale.ROOT);
}

private String baseUnit(String unit) {
    if ("KG".equals(unit)) {
        return "G";
    }
    if ("L".equals(unit)) {
        return "ML";
    }
    return unit;
}

private String buildMaterialNote(
        BookingService bs,
        java.util.List<PetHotel.model.MaterialUsageConfirmRow> materialRows,
        String completionNote) {

    StringBuilder note = new StringBuilder();
    note.append("[Hoàn thành dịch vụ]\n");
    
    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    String timestamp = java.time.OffsetDateTime.now().format(formatter);
    note.append("Thời gian: ").append(timestamp).append("\n");

    if (bs.getEmployeeId() != null) {
        note.append("Nhân viên: ").append(bs.getEmployeeId()).append("\n");
    }

    // Nếu có vật tư
    if (materialRows != null && !materialRows.isEmpty()) {
        note.append("Vật tư sử dụng:\n");
        for (PetHotel.model.MaterialUsageConfirmRow row : materialRows) {
            java.math.BigDecimal actualAmount = row.getActualAmount();
            if (actualAmount != null && actualAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
                note.append("* ").append(row.getProductId())
                    .append(" | ").append(row.getProductName())
                    .append(" | Định mức: ").append(row.getStandardAmount()).append(row.getStandardUnit())
                    .append(" | Thực tế: ").append(actualAmount).append(row.getStandardUnit())
                    .append("\n");
            }
        }
    } else {
        note.append("Dịch vụ chưa cấu hình vật tư tiêu hao.\n");
    }

    if (completionNote != null && !completionNote.trim().isEmpty()) {
        note.append("Ghi chú: ").append(completionNote.trim()).append("\n");
    }

    return note.toString();
}
}
