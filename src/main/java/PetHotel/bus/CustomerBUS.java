package PetHotel.bus;

import PetHotel.dao.CustomerDAO;
import PetHotel.dao.PetDAO;
import PetHotel.exception.*;
import PetHotel.model.Customer;
import PetHotel.model.Pet;
import PetHotel.util.Role;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * CustomerBUS — Nghiệp vụ Quản Lý Khách Hàng.
 *
 * ═══════════════════════════════════════════════════════════════════
 * PHÂN QUYỀN:
 *   createCustomer   → RECEPTIONIST (1) trở lên
 *   updateCustomer   → RECEPTIONIST (1) trở lên
 *   deleteCustomer   → BRANCH_MANAGER (4) trở lên
 *   getCustomerDetail    → RECEPTIONIST (1) trở lên
 *   searchCustomer       → RECEPTIONIST (1) trở lên
 *   getServiceHistory    → RECEPTIONIST (1) trở lên
 *   linkPetsToCustomer   → RECEPTIONIST (1) trở lên
 * ═══════════════════════════════════════════════════════════════════
 *
 * Ràng buộc nghiệp vụ khi xóa:
 *   - Không xóa nếu còn booking đang hoạt động.
 *   - Không xóa nếu còn thú cưng được đăng ký.
 *
 * TODO: Thêm audit log mỗi khi tạo/sửa/xóa.
 * TODO: Thêm cache cho getCustomerDetail khi có nhiều request đồng thời.
 * TODO: Thêm pagination cho searchCustomer và getAllCustomers.
 */
public class CustomerBUS {

    // Pattern kiểm tra email cơ bản
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Pattern kiểm tra phone: cho phép +, -, khoảng trắng, chữ số
    private static final Pattern PHONE_PATTERN =
        Pattern.compile("^[+\\d][\\d\\s\\-]{6,18}$");

    private static final Pattern CCCD_PATTERN =
        Pattern.compile("^\\d{12}$");

    private final CustomerDAO customerDAO;
    private final PetDAO      petDAO;
    private final AuthBUS     authBUS;

    public CustomerBUS(AuthBUS authBUS) {
        this.customerDAO = new CustomerDAO();
        this.petDAO      = new PetDAO();
        this.authBUS     = authBUS;
    }

    /** Constructor cho dependency injection / unit test */
    public CustomerBUS(CustomerDAO customerDAO, PetDAO petDAO, AuthBUS authBUS) {
        this.customerDAO = customerDAO;
        this.petDAO      = petDAO;
        this.authBUS     = authBUS;
    }

    // ─────────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────────

    /**
     * Tạo khách hàng mới.
     *
     * Quyền: RECEPTIONIST trở lên.
     *
     * Nghiệp vụ:
     *   1. Kiểm tra quyền.
     *   2. Validate dữ liệu đầu vào.
     *   3. Kiểm tra phone / email chưa tồn tại.
     *   4. Sinh ID mới.
     *   5. Insert vào DB.
     *
     * @param fullName  họ tên đầy đủ (bắt buộc)
     * @param phone     số điện thoại (bắt buộc, unique)
     * @param email     email (không bắt buộc, unique nếu có)
     * @param address   địa chỉ (không bắt buộc)
     * @param note      ghi chú (không bắt buộc)
     * @return Customer vừa tạo (có customerId)
     * @throws ValidationException      nếu dữ liệu không hợp lệ
     * @throws DuplicateRecordException nếu phone/email đã tồn tại
     * @throws AuthorizationException   nếu không đủ quyền
     */
    public Customer createCustomer(String fullName, String phone,
                                   String cccd, String email, String address, String note) {
        // 1. Kiểm tra quyền
        authBUS.requireRole(Role.RECEPTIONIST);

        // 2. Validate
        validateFullName(fullName);
        validatePhone(phone, null);
        String normalizedCccd = normalizeOptional(cccd);
        validateCccd(normalizedCccd, null);
        String normalizedEmail = normalizeOptional(email);
        if (normalizedEmail != null) {
            validateEmail(normalizedEmail, null);
        }

        try {
            // 3. Kiểm tra unique
            if (customerDAO.existsByPhone(phone.trim(), null)) {
                throw new DuplicateRecordException("Số điện thoại đã tồn tại trong hệ thống.");
            }
            if (normalizedEmail != null && customerDAO.existsByEmail(normalizedEmail, null)) {
                throw new DuplicateRecordException("Email đã tồn tại trong hệ thống.");
            }
            if (normalizedCccd != null && customerDAO.existsByCccd(normalizedCccd, null)) {
                throw new DuplicateRecordException("CCCD đã tồn tại trong hệ thống.");
            }

            // 4. Sinh ID
            String newId = customerDAO.generateNextCustomerId();

            // 5. Insert
            Customer customer = new Customer(
                newId,
                fullName.trim(),
                normalizedEmail,
                normalizedCccd,
                phone.trim(),
                normalizeOptional(address),
                normalizeOptional(note)
            );
            customerDAO.insert(customer, null);
            return customer;

        } catch (DuplicateRecordException | ValidationException e) {
            throw e; // re-throw exception nghiệp vụ
        } catch (SQLException e) {
            throw mapCustomerSaveException(e);
        }
    }

    /** Overload giữ tương thích với controller/code cũ chưa truyền CCCD. */
    public Customer createCustomer(String fullName, String phone,
                                   String email, String address, String note) {
        return createCustomer(fullName, phone, null, email, address, note);
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────

    /**
     * Cập nhật thông tin khách hàng.
     *
     * Quyền: RECEPTIONIST trở lên.
     *
     * Nghiệp vụ:
     *   1. Kiểm tra quyền.
     *   2. Validate dữ liệu.
     *   3. Kiểm tra customer tồn tại.
     *   4. Kiểm tra phone/email không trùng với customer khác.
     *   5. Update DB.
     *
     * @param customerId mã khách hàng cần cập nhật
     * @param fullName   họ tên mới
     * @param phone      SĐT mới
     * @param email      email mới (có thể null)
     * @param address    địa chỉ mới (có thể null)
     * @param note       ghi chú mới (có thể null)
     * @return Customer sau khi cập nhật
     * @throws NotFoundException        nếu customer không tồn tại
     * @throws ValidationException      nếu dữ liệu không hợp lệ
     * @throws DuplicateRecordException nếu phone/email trùng với customer khác
     */
    public Customer updateCustomer(String customerId, String fullName, String phone,
                                   String cccd, String email, String address, String note) {
        // 1. Quyền
        authBUS.requireRole(Role.RECEPTIONIST);

        // 2. Validate
        validateFullName(fullName);
        validatePhone(phone, customerId);
        String normalizedCccd = normalizeOptional(cccd);
        validateCccd(normalizedCccd, customerId);
        String normalizedEmail = normalizeOptional(email);
        if (normalizedEmail != null) {
            validateEmail(normalizedEmail, customerId);
        }

        try {
            // 3. Kiểm tra tồn tại
            Customer existing = customerDAO.findById(customerId);
            if (existing == null) {
                throw new NotFoundException("Không tìm thấy khách hàng với ID: " + customerId);
            }

            // 4. Unique check (loại trừ chính customer đang cập nhật)
            if (customerDAO.existsByPhone(phone.trim(), customerId)) {
                throw new DuplicateRecordException("Số điện thoại đã tồn tại trong hệ thống.");
            }
            if (normalizedEmail != null && customerDAO.existsByEmail(normalizedEmail, customerId)) {
                throw new DuplicateRecordException("Email đã tồn tại trong hệ thống.");
            }
            if (normalizedCccd != null && customerDAO.existsByCccd(normalizedCccd, customerId)) {
                throw new DuplicateRecordException("CCCD đã tồn tại trong hệ thống.");
            }

            // 5. Update
            existing.setFullName(fullName.trim());
            existing.setPhone(phone.trim());
            existing.setCccd(normalizedCccd);
            existing.setEmail(normalizedEmail);
            existing.setAddress(normalizeOptional(address));
            existing.setNote(normalizeOptional(note));

            int rows = customerDAO.update(existing, null);
            if (rows == 0) {
                throw new NotFoundException("Cập nhật thất bại: Không tìm thấy customer ID: " + customerId);
            }
            return existing;

        } catch (NotFoundException | ValidationException | DuplicateRecordException e) {
            throw e;
        } catch (SQLException e) {
            throw mapCustomerSaveException(e);
        }
    }

    /** Overload giữ tương thích với controller/code cũ chưa truyền CCCD. */
    public Customer updateCustomer(String customerId, String fullName, String phone,
                                   String email, String address, String note) {
        return updateCustomer(customerId, fullName, phone, null, email, address, note);
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────────

    /**
     * Xóa khách hàng.
     *
     * Quyền: BRANCH_MANAGER (4) trở lên.
     *
     * Ràng buộc:
     *   - Không xóa nếu còn booking đang hoạt động (PENDING/CONFIRMED/CHECKED_IN).
     *   - Không xóa nếu còn thú cưng được đăng ký.
     *
     * @param customerId mã khách hàng
     * @throws NotFoundException      nếu không tìm thấy
     * @throws BusinessException      nếu vi phạm ràng buộc nghiệp vụ
     * @throws AuthorizationException nếu không đủ quyền
     */
    public void deleteCustomer(String customerId) {
        // 1. Quyền
        authBUS.requireRole(Role.BRANCH_MANAGER);

        try {
            // 2. Kiểm tra tồn tại
            Customer existing = customerDAO.findById(customerId);
            if (existing == null) {
                throw new NotFoundException("Không tìm thấy khách hàng với ID: " + customerId);
            }

            // 3. Ràng buộc: còn booking đang hoạt động
            int activeBookings = customerDAO.countActiveBookings(customerId);
            if (activeBookings > 0) {
                throw new BusinessException(
                    "Không thể xóa khách hàng '" + existing.getFullName() +
                    "': còn " + activeBookings + " booking đang hoạt động.");
            }

            // 4. Ràng buộc: còn thú cưng
            int petCount = customerDAO.countPets(customerId);
            if (petCount > 0) {
                throw new BusinessException(
                    "Không thể xóa khách hàng '" + existing.getFullName() +
                    "': còn " + petCount + " thú cưng đang đăng ký. " +
                    "Vui lòng xóa hoặc chuyển thú cưng trước.");
            }

            // 5. Xóa
            customerDAO.delete(customerId, null);

        } catch (NotFoundException | BusinessException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi xóa khách hàng.", e);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // QUERY
    // ─────────────────────────────────────────────────────────────

    /**
     * Lấy chi tiết khách hàng theo ID.
     *
     * Quyền: RECEPTIONIST trở lên.
     *
     * @param customerId mã khách hàng
     * @return Customer
     * @throws NotFoundException nếu không tồn tại
     */
    public Customer getCustomerDetail(String customerId) {
        authBUS.requireRole(Role.RECEPTIONIST);

        try {
            Customer c = customerDAO.findById(customerId);
            if (c == null) {
                throw new NotFoundException("Không tìm thấy khách hàng với ID: " + customerId);
            }
            return c;
        } catch (NotFoundException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi lấy thông tin khách hàng.", e);
        }
    }

    /**
     * Tìm kiếm khách hàng theo từ khóa (tên, SĐT, email).
     *
     * Quyền: RECEPTIONIST trở lên.
     * TODO: Thêm pagination (page, pageSize).
     *
     * @param keyword từ khóa (không được null hoặc rỗng)
     * @return danh sách Customer khớp
     * @throws ValidationException nếu keyword rỗng
     */
    public List<Customer> searchCustomer(String keyword) {
        authBUS.requireRole(Role.RECEPTIONIST);

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ValidationException("Từ khóa tìm kiếm không được để trống.");
        }
        if (keyword.trim().length() < 2) {
            throw new ValidationException("Từ khóa tìm kiếm phải có ít nhất 2 ký tự.");
        }

        try {
            return customerDAO.search(keyword.trim());
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi tìm kiếm khách hàng.", e);
        }
    }

    /**
     * Lấy toàn bộ danh sách khách hàng.
     *
     * Quyền: RECEPTIONIST trở lên.
     * TODO: Thêm pagination.
     *
     * @return List<Customer>
     */
    public List<Customer> getAllCustomers() {
        authBUS.requireRole(Role.RECEPTIONIST);
        try {
            return customerDAO.findAll();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi lấy danh sách khách hàng.", e);
        }
    }

    /**
     * Lấy lịch sử dịch vụ của khách hàng.
     *
     * Quyền: RECEPTIONIST trở lên.
     * TODO: Trả về List<ServiceHistoryDTO> thay vì List<Object[]>.
     * TODO: Thêm filter theo ngày, loại dịch vụ, trạng thái.
     *
     * @param customerId mã khách hàng
     * @return danh sách lịch sử dạng Object[]
     * @throws NotFoundException nếu customer không tồn tại
     */
    public List<Object[]> getCustomerServiceHistory(String customerId) {
        authBUS.requireRole(Role.RECEPTIONIST);

        try {
            // Kiểm tra customer tồn tại trước
            if (customerDAO.findById(customerId) == null) {
                throw new NotFoundException("Không tìm thấy khách hàng với ID: " + customerId);
            }
            return customerDAO.getServiceHistory(customerId);
        } catch (NotFoundException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi lấy lịch sử dịch vụ.", e);
        }
    }

    /**
     * Lấy danh sách thú cưng của khách hàng.
     *
     * Quyền: RECEPTIONIST trở lên.
     *
     * @param customerId mã khách hàng
     * @return List<Pet> thuộc về khách hàng này
     * @throws NotFoundException nếu customer không tồn tại
     */
    public List<Pet> getPetsOfCustomer(String customerId) {
        authBUS.requireRole(Role.RECEPTIONIST);

        try {
            if (customerDAO.findById(customerId) == null) {
                throw new NotFoundException("Không tìm thấy khách hàng với ID: " + customerId);
            }
            return petDAO.findByCustomerId(customerId);
        } catch (NotFoundException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi lấy danh sách thú cưng.", e);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // VALIDATION HELPERS
    // ─────────────────────────────────────────────────────────────

    /**
     * Validate họ tên khách hàng.
     * Quy tắc: không rỗng, không vượt 120 ký tự, không chứa ký tự đặc biệt nguy hiểm.
     * TODO: Thêm kiểm tra ký tự không hợp lệ theo ngôn ngữ.
     */
    private void validateFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Họ tên khách hàng không được để trống.");
        }
        if (fullName.trim().length() > 120) {
            throw new ValidationException("Họ tên không được vượt quá 120 ký tự.");
        }
    }

    /**
     * Validate số điện thoại.
     *
     * @param phone     SĐT cần kiểm tra
     * @param excludeId customerId bỏ qua khi kiểm tra unique (null = kiểm tra tuyệt đối)
     * @throws ValidationException nếu không hợp lệ
     */
    private void validatePhone(String phone, String excludeId) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new ValidationException("Số điện thoại không được để trống.");
        }
        String cleaned = phone.trim().replaceAll("[\\s\\-]", "");
        if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new ValidationException(
                "Số điện thoại '" + phone + "' không đúng định dạng. " +
                "Chỉ chứa chữ số, dấu +, dấu - và khoảng trắng (7–19 ký tự).");
        }
        // TODO: validate thêm prefix quốc gia nếu cần
    }

    /**
     * Validate email.
     *
     * @param email     email cần kiểm tra
     * @param excludeId customerId bỏ qua khi kiểm tra unique
     * @throws ValidationException nếu không hợp lệ
     */
    private void validateEmail(String email, String excludeId) {
        if (email == null || email.trim().isEmpty()) return; // email optional
        if (email.trim().length() > 254) {
            throw new ValidationException("Email không được vượt quá 254 ký tự.");
        }
        if (email.trim().toLowerCase().endsWith("@gmail.co")) {
            throw new ValidationException("Email gmail.co có thể thiếu .com. Vui lòng kiểm tra lại.");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException(
                "Email '" + email + "' không đúng định dạng.");
        }
    }

    private void validateCccd(String cccd, String excludeId) {
        if (cccd == null || cccd.isEmpty()) {
            throw new ValidationException("CCCD không được để trống.");
        }
        if (!CCCD_PATTERN.matcher(cccd).matches()) {
            throw new ValidationException("CCCD phải gồm đúng 12 chữ số.");
        }
    }

    private String normalizeOptional(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private RuntimeException mapCustomerSaveException(SQLException e) {
        String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        if (message.contains("phone") || message.contains("uq_customer_phone")) {
            return new DuplicateRecordException("Số điện thoại đã tồn tại trong hệ thống.", e);
        }
        if (message.contains("email") || message.contains("uq_customer_email")) {
            return new DuplicateRecordException("Email đã tồn tại trong hệ thống.", e);
        }
        if (message.contains("cccd") || message.contains("uq_customer_cccd")) {
            return new DuplicateRecordException("CCCD đã tồn tại trong hệ thống.", e);
        }
        return new RuntimeException("Lỗi database khi lưu khách hàng. Vui lòng kiểm tra lại.", e);
    }
}
