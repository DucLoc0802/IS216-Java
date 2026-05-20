package PetHotel.bus;

import java.sql.SQLException;
import java.util.List;

import PetHotel.dao.BookingServiceDAO;
import PetHotel.dao.ServiceCategoryDAO;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.model.PetService;
import PetHotel.model.ServiceCategory;
import PetHotel.util.Role;

/**
 * ServiceBUS — Xử lý logic quản lý danh sách dịch vụ cho vai trò Quản lý chi nhánh và Lễ tân.
 * 
 * Hỗ trợ:
 *  - Lấy danh sách tất cả dịch vụ
 *  - Tìm kiếm dịch vụ theo từ khóa
 *  - Lấy dịch vụ theo loại (category)
 *  - Kiểm soát quyền truy cập theo role
 */
public class ServiceBUS {

    private final BookingServiceDAO bookingServiceDAO = new BookingServiceDAO();
    private final ServiceCategoryDAO serviceCategoryDAO = new ServiceCategoryDAO();

    /**
     * Lấy danh sách tất cả dịch vụ đang hoạt động
     * 
     * @param currentUser Người dùng hiện tại (để kiểm tra quyền)
     * @return Danh sách các dịch vụ
     * @throws ValidationException nếu người dùng không có quyền
     * @throws SQLException nếu lỗi cơ sở dữ liệu
     */
    public List<PetService> getAllServices(AppUser currentUser)
            throws ValidationException, SQLException {
        
        // Kiểm tra quyền: Lễ tân, Quản lý chi nhánh, Admin
        if (!currentUser.hasRole(Role.RECEPTIONIST) && 
            !currentUser.hasRole(Role.BRANCH_MANAGER) &&
            !currentUser.hasRole(Role.ADMIN)) {
            throw new ValidationException("Bạn không có quyền xem danh sách dịch vụ");
        }

        return bookingServiceDAO.getAllServices();
    }

    /**
     * Tìm kiếm dịch vụ theo từ khóa (tên, loài, loại dịch vụ)
     * 
     * @param keyword Từ khóa tìm kiếm
     * @param currentUser Người dùng hiện tại
     * @return Danh sách các dịch vụ phù hợp
     * @throws ValidationException nếu người dùng không có quyền
     * @throws SQLException nếu lỗi cơ sở dữ liệu
     */
    public List<PetService> searchServices(String keyword, AppUser currentUser)
            throws ValidationException, SQLException {
        
        // Kiểm tra quyền
        if (!currentUser.hasRole(Role.RECEPTIONIST) && 
            !currentUser.hasRole(Role.BRANCH_MANAGER) &&
            !currentUser.hasRole(Role.ADMIN)) {
            throw new ValidationException("Bạn không có quyền tìm kiếm dịch vụ");
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            return bookingServiceDAO.getAllServices();
        }

        return bookingServiceDAO.searchServices(keyword.trim());
    }

    /**
     * Lấy danh sách dịch vụ theo loại (category)
     * 
     * @param serviceCategoryId Mã loại dịch vụ
     * @param currentUser Người dùng hiện tại
     * @return Danh sách các dịch vụ trong loại đó
     * @throws ValidationException nếu người dùng không có quyền hoặc dữ liệu không hợp lệ
     * @throws SQLException nếu lỗi cơ sở dữ liệu
     */
    public List<PetService> getServicesByCategory(String serviceCategoryId, AppUser currentUser)
            throws ValidationException, SQLException {
        
        // Kiểm tra quyền
        if (!currentUser.hasRole(Role.RECEPTIONIST) && 
            !currentUser.hasRole(Role.BRANCH_MANAGER) &&
            !currentUser.hasRole(Role.ADMIN)) {
            throw new ValidationException("Bạn không có quyền xem danh sách dịch vụ");
        }

        if (serviceCategoryId == null || serviceCategoryId.trim().isEmpty()) {
            throw new ValidationException("Mã loại dịch vụ không hợp lệ");
        }

        return bookingServiceDAO.getServicesByCategory(serviceCategoryId);
    }

    /**
     * Lấy danh sách tất cả loại dịch vụ (categories)
     * 
     * @param currentUser Người dùng hiện tại
     * @return Danh sách các loại dịch vụ
     * @throws ValidationException nếu người dùng không có quyền
     * @throws SQLException nếu lỗi cơ sở dữ liệu
     */
    public List<ServiceCategory> getAllServiceCategories(AppUser currentUser)
            throws ValidationException, SQLException {
        
        // Kiểm tra quyền
        if (!currentUser.hasRole(Role.RECEPTIONIST) && 
            !currentUser.hasRole(Role.BRANCH_MANAGER) &&
            !currentUser.hasRole(Role.ADMIN)) {
            throw new ValidationException("Bạn không có quyền xem loại dịch vụ");
        }

        return serviceCategoryDAO.findAll();
    }

    /**
     * Lấy thông tin chi tiết một dịch vụ theo ID
     * 
     * @param serviceId Mã dịch vụ
     * @param currentUser Người dùng hiện tại
     * @return Thông tin dịch vụ hoặc null nếu không tìm thấy
     * @throws ValidationException nếu người dùng không có quyền
     */
    public PetService getServiceById(String serviceId, AppUser currentUser)
            throws ValidationException, SQLException {
        
        // Kiểm tra quyền
        if (!currentUser.hasRole(Role.RECEPTIONIST) && 
            !currentUser.hasRole(Role.BRANCH_MANAGER) &&
            !currentUser.hasRole(Role.ADMIN)) {
            throw new ValidationException("Bạn không có quyền xem thông tin dịch vụ");
        }

        if (serviceId == null || serviceId.trim().isEmpty()) {
            throw new ValidationException("Mã dịch vụ không hợp lệ");
        }

        List<PetService> services = bookingServiceDAO.getAllServices();
        for (PetService service : services) {
            if (service.getServiceId().equals(serviceId)) {
                return service;
            }
        }

        return null;
    }

    /**
     * Tạo loại dịch vụ (category) mới
     * Chỉ cho phép Quản lý chi nhánh thực hiện
     * 
     * @param categoryName Tên loại dịch vụ
     * @param note Ghi chú/mô tả
     * @param currentUser Người dùng hiện tại
     * @throws ValidationException nếu dữ liệu không hợp lệ hoặc người dùng không có quyền
     * @throws SQLException nếu lỗi cơ sở dữ liệu
     */
    public void createNewServiceCategory(String categoryName, String note, AppUser currentUser)
            throws ValidationException, SQLException {
        
        // Chỉ Quản lý chi nhánh được phép tạo loại dịch vụ mới
        if (!currentUser.hasRole(Role.BRANCH_MANAGER) && !currentUser.hasRole(Role.ADMIN)) {
            throw new ValidationException("Bạn không có quyền tạo loại dịch vụ mới. Chỉ Quản lý chi nhánh được phép.");
        }

        // Validate tên loại dịch vụ
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new ValidationException("Tên loại dịch vụ không được để trống");
        }

        categoryName = categoryName.trim();

        if (categoryName.length() < 3) {
            throw new ValidationException("Tên loại dịch vụ phải có ít nhất 3 ký tự");
        }

        if (categoryName.length() > 80) {
            throw new ValidationException("Tên loại dịch vụ không được vượt quá 80 ký tự");
        }

        // Kiểm tra xem loại dịch vụ đã tồn tại chưa
        if (serviceCategoryDAO.existsByName(categoryName)) {
            throw new ValidationException("Loại dịch vụ '" + categoryName + "' đã tồn tại trong hệ thống");
        }

        // Validate note (nếu có)
        if (note != null && note.length() > 4000) {
            throw new ValidationException("Ghi chú không được vượt quá 4000 ký tự");
        }

        // Tạo mã loại dịch vụ tự động (SC + số ngẫu nhiên 6 chữ số)
        String categoryId = generateCategoryId();

        // Chèn vào database
        serviceCategoryDAO.insert(categoryId, categoryName, note);
    }

    /**
     * Tạo mã loại dịch vụ tự động
     * Format: SC + 6 chữ số (VD: SC123456)
     */
    private String generateCategoryId() throws SQLException {
        int maxAttempts = 10;
        String categoryId;

        for (int i = 0; i < maxAttempts; i++) {
            int randomNumber = 100000 + new java.util.Random().nextInt(900000);
            categoryId = "SC" + randomNumber;

            if (!serviceCategoryDAO.existsById(categoryId)) {
                return categoryId;
            }
        }

        throw new SQLException("Không thể tạo mã loại dịch vụ mới. Vui lòng thử lại.");
    }

    /**
     * Cập nhật thông tin dịch vụ
     * Chỉ cho phép Quản lý chi nhánh thực hiện
     * 
     * @param service Đối tượng dịch vụ cần cập nhật
     * @param currentUser Người dùng hiện tại
     * @throws ValidationException nếu dữ liệu không hợp lệ hoặc người dùng không có quyền
     * @throws SQLException nếu lỗi cơ sở dữ liệu
     */
    public void updateService(PetService service, AppUser currentUser)
            throws ValidationException, SQLException {
        
        // Chỉ Quản lý chi nhánh và Admin được phép cập nhật dịch vụ
        if (!currentUser.hasRole(Role.BRANCH_MANAGER) && !currentUser.hasRole(Role.ADMIN)) {
            throw new ValidationException("Bạn không có quyền sửa dịch vụ. Chỉ Quản lý chi nhánh được phép.");
        }

        // Validate dữ liệu
        if (service == null || service.getServiceId() == null || service.getServiceId().trim().isEmpty()) {
            throw new ValidationException("Mã dịch vụ không hợp lệ");
        }

        if (service.getServiceName() == null || service.getServiceName().trim().isEmpty()) {
            throw new ValidationException("Tên dịch vụ không được để trống");
        }

        String serviceName = service.getServiceName().trim();
        if (serviceName.length() < 3) {
            throw new ValidationException("Tên dịch vụ phải có ít nhất 3 ký tự");
        }

        if (serviceName.length() > 100) {
            throw new ValidationException("Tên dịch vụ không được vượt quá 100 ký tự");
        }

        if (service.getBasePrice() < 0) {
            throw new ValidationException("Giá dịch vụ không được âm");
        }

        if (service.getDurationMinutes() <= 0) {
            throw new ValidationException("Thời gian dịch vụ phải lớn hơn 0 phút");
        }

        if (service.getDurationMinutes() > 1440) { // 24 hours
            throw new ValidationException("Thời gian dịch vụ không được vượt quá 1440 phút (24 giờ)");
        }

        service.setServiceName(serviceName);
        
        // Cập nhật vào database
        bookingServiceDAO.updateService(service);
    }

    /**
     * Xóa dịch vụ (đánh dấu là không hoạt động)
     * Chỉ cho phép Quản lý chi nhánh thực hiện
     * 
     * @param serviceId Mã dịch vụ cần xóa
     * @param currentUser Người dùng hiện tại
     * @throws ValidationException nếu dữ liệu không hợp lệ hoặc người dùng không có quyền
     * @throws SQLException nếu lỗi cơ sở dữ liệu
     */
    public void deleteService(String serviceId, AppUser currentUser)
            throws ValidationException, SQLException {
        
        // Chỉ Quản lý chi nhánh và Admin được phép xóa dịch vụ
        if (!currentUser.hasRole(Role.BRANCH_MANAGER) && !currentUser.hasRole(Role.ADMIN)) {
            throw new ValidationException("Bạn không có quyền xóa dịch vụ. Chỉ Quản lý chi nhánh được phép.");
        }

        // Validate mã dịch vụ
        if (serviceId == null || serviceId.trim().isEmpty()) {
            throw new ValidationException("Mã dịch vụ không hợp lệ");
        }

        // Kiểm tra xem dịch vụ có tồn tại không
        PetService service = bookingServiceDAO.getServiceById(serviceId.trim());
        if (service == null) {
            throw new ValidationException("Dịch vụ không tồn tại trong hệ thống");
        }

        // Xóa dịch vụ (đánh dấu là không hoạt động)
        bookingServiceDAO.deleteService(serviceId.trim());
    }

    /**
     * Tạo dịch vụ mới
     * Chỉ cho phép Quản lý chi nhánh thực hiện
     * 
     * @param serviceName Tên dịch vụ
     * @param serviceCategoryId Mã loại dịch vụ
     * @param species Loài thú cưng
     * @param basePrice Giá cơ bản
     * @param durationMinutes Thời gian (phút)
     * @param currentUser Người dùng hiện tại
     * @throws ValidationException nếu dữ liệu không hợp lệ hoặc người dùng không có quyền
     * @throws SQLException nếu lỗi cơ sở dữ liệu
     */
    public void createNewService(String serviceName, String serviceCategoryId, String species,
                                 double basePrice, int durationMinutes, AppUser currentUser)
            throws ValidationException, SQLException {
        
        // Chỉ Quản lý chi nhánh và Admin được phép tạo dịch vụ mới
        if (!currentUser.hasRole(Role.BRANCH_MANAGER) && !currentUser.hasRole(Role.ADMIN)) {
            throw new ValidationException("Bạn không có quyền tạo dịch vụ mới. Chỉ Quản lý chi nhánh được phép.");
        }

        // Validate tên dịch vụ
        if (serviceName == null || serviceName.trim().isEmpty()) {
            throw new ValidationException("Tên dịch vụ không được để trống");
        }

        serviceName = serviceName.trim();
        if (serviceName.length() < 3) {
            throw new ValidationException("Tên dịch vụ phải có ít nhất 3 ký tự");
        }

        if (serviceName.length() > 100) {
            throw new ValidationException("Tên dịch vụ không được vượt quá 100 ký tự");
        }

        // Validate loại dịch vụ
        if (serviceCategoryId == null || serviceCategoryId.trim().isEmpty()) {
            throw new ValidationException("Loại dịch vụ không được để trống");
        }

        serviceCategoryId = serviceCategoryId.trim();
        
        // Kiểm tra loại dịch vụ tồn tại không
        if (serviceCategoryDAO.findById(serviceCategoryId) == null) {
            throw new ValidationException("Loại dịch vụ không tồn tại trong hệ thống");
        }

        // Validate species (nếu có)
        if (species != null && species.length() > 50) {
            throw new ValidationException("Loài không được vượt quá 50 ký tự");
        }

        // Validate giá
        if (basePrice < 0) {
            throw new ValidationException("Giá dịch vụ không được âm");
        }

        if (basePrice > 999999999) {
            throw new ValidationException("Giá dịch vụ quá lớn");
        }

        // Validate thời gian
        if (durationMinutes <= 0) {
            throw new ValidationException("Thời gian dịch vụ phải lớn hơn 0 phút");
        }

        if (durationMinutes > 1440) { // 24 hours
            throw new ValidationException("Thời gian dịch vụ không được vượt quá 1440 phút (24 giờ)");
        }

        // Tạo mã dịch vụ tự động
        String serviceId = generateServiceId();

        bookingServiceDAO.createService(serviceName, serviceCategoryId, 
                                       species, basePrice, durationMinutes);
    }

    /**
     * Tạo mã dịch vụ tự động
     * Format: SV + 6 chữ số (VD: SV123456)
     */
    private String generateServiceId() throws SQLException {
        int maxAttempts = 10;
        String serviceId;

        for (int i = 0; i < maxAttempts; i++) {
            int randomNumber = 100000 + new java.util.Random().nextInt(900000);
            serviceId = "SV" + randomNumber;

            if (!bookingServiceDAO.existsServiceById(serviceId)) {
                return serviceId;
            }
        }

        throw new SQLException("Không thể tạo mã dịch vụ mới. Vui lòng thử lại.");
    }

    /**
     * Lấy tất cả dịch vụ (bao gồm cả inactive) cho quản lý
     * Chỉ Quản lý chi nhánh và Admin được phép
     * 
     * @param currentUser Người dùng hiện tại
     * @return Danh sách tất cả dịch vụ
     * @throws ValidationException nếu người dùng không có quyền
     * @throws SQLException nếu lỗi cơ sở dữ liệu
     */
    public List<PetService> getAllServicesForManagement(AppUser currentUser)
            throws ValidationException, SQLException {
        
        // Chỉ Quản lý chi nhánh và Admin được phép xem quản lý dịch vụ
        if (!currentUser.hasRole(Role.BRANCH_MANAGER) && !currentUser.hasRole(Role.ADMIN)) {
            throw new ValidationException("Bạn không có quyền quản lý dịch vụ. Chỉ Quản lý chi nhánh được phép.");
        }

        return bookingServiceDAO.getAllServicesForManagement();
    }

    /**
     * Kích hoạt dịch vụ
     * Chỉ Quản lý chi nhánh và Admin được phép
     * 
     * @param serviceId Mã dịch vụ cần kích hoạt
     * @param currentUser Người dùng hiện tại
     * @throws ValidationException nếu dữ liệu không hợp lệ hoặc người dùng không có quyền
     * @throws SQLException nếu lỗi cơ sở dữ liệu
     */
    public void activateService(String serviceId, AppUser currentUser)
            throws ValidationException, SQLException {
        
        // Kiểm tra quyền
        if (!currentUser.hasRole(Role.BRANCH_MANAGER) && !currentUser.hasRole(Role.ADMIN)) {
            throw new ValidationException("Bạn không có quyền kích hoạt dịch vụ. Chỉ Quản lý chi nhánh được phép.");
        }

        // Validate mã dịch vụ
        if (serviceId == null || serviceId.trim().isEmpty()) {
            throw new ValidationException("Mã dịch vụ không hợp lệ");
        }

        serviceId = serviceId.trim();

        // Kiểm tra dịch vụ tồn tại không
        if (!bookingServiceDAO.existsServiceById(serviceId)) {
            throw new ValidationException("Dịch vụ với mã '" + serviceId + "' không tồn tại");
        }

        // Cập nhật trạng thái thành hoạt động
        bookingServiceDAO.updateServiceStatus(serviceId, 1);
    }

    /**
     * Vô hiệu hóa dịch vụ (không thể đặt lịch này nữa)
     * Chỉ Quản lý chi nhánh và Admin được phép
     * 
     * @param serviceId Mã dịch vụ cần vô hiệu hóa
     * @param currentUser Người dùng hiện tại
     * @throws ValidationException nếu dữ liệu không hợp lệ hoặc người dùng không có quyền
     * @throws SQLException nếu lỗi cơ sở dữ liệu
     */
    public void deactivateService(String serviceId, AppUser currentUser)
            throws ValidationException, SQLException {
        
        // Kiểm tra quyền
        if (!currentUser.hasRole(Role.BRANCH_MANAGER) && !currentUser.hasRole(Role.ADMIN)) {
            throw new ValidationException("Bạn không có quyền vô hiệu hóa dịch vụ. Chỉ Quản lý chi nhánh được phép.");
        }

        // Validate mã dịch vụ
        if (serviceId == null || serviceId.trim().isEmpty()) {
            throw new ValidationException("Mã dịch vụ không hợp lệ");
        }

        serviceId = serviceId.trim();

        // Kiểm tra dịch vụ tồn tại không
        if (!bookingServiceDAO.existsServiceById(serviceId)) {
            throw new ValidationException("Dịch vụ với mã '" + serviceId + "' không tồn tại");
        }

        // Cập nhật trạng thái thành không hoạt động
        bookingServiceDAO.updateServiceStatus(serviceId, 0);
    }

    /**
     * Kiểm tra xem dịch vụ có đang hoạt động không
     * 
     * @param serviceId Mã dịch vụ
     * @return true nếu dịch vụ đang hoạt động, false nếu không
     * @throws SQLException nếu lỗi cơ sở dữ liệu
     */
    public boolean isServiceActive(String serviceId) throws SQLException {
        if (serviceId == null || serviceId.trim().isEmpty()) {
            return false;
        }
        return bookingServiceDAO.isServiceActive(serviceId.trim());
    }
}
