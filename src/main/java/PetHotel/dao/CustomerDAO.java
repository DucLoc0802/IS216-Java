package PetHotel.dao;

import PetHotel.model.Customer;
import PetHotel.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CustomerDAO — Thao tác DB cho bảng CUSTOMER.
 *
 * Quy tắc:
 *   - Chỉ làm việc với DB, không chứa nghiệp vụ.
 *   - Dùng PreparedStatement, không dùng Statement thường.
 *   - Tất cả method ném SQLException để BUS xử lý.
 *
 * TODO: Thêm pagination (OFFSET/FETCH NEXT) cho findAll và search.
 * TODO: Externalize SQL ra constants file.
 * TODO: Thêm logging.
 * TODO: Optimize JOIN query trong getCustomerServiceHistory.
 */
public class CustomerDAO {

    // ── SQL ──────────────────────────────────────────────────────

    /** Lấy customer theo ID */
    private static final String SQL_FIND_BY_ID =
        "SELECT customer_id, full_name, email, phone, address, note, created_at, updated_at " +
        "FROM customer WHERE customer_id = ?";

    /** Lấy tất cả customer, mới nhất trước */
    private static final String SQL_FIND_ALL =
        "SELECT customer_id, full_name, email, phone, address, note, created_at, updated_at " +
        "FROM customer ORDER BY created_at DESC";

    /**
     * Tìm kiếm customer theo keyword (tên, SĐT, email).
     * Dùng LIKE với LOWER() để case-insensitive.
     * TODO: Thêm OFFSET/FETCH NEXT khi có pagination.
     */
    private static final String SQL_SEARCH =
        "SELECT customer_id, full_name, email, phone, address, note, created_at, updated_at " +
        "FROM customer " +
        "WHERE LOWER(full_name) LIKE LOWER(?) " +
        "   OR phone LIKE ? " +
        "   OR LOWER(email) LIKE LOWER(?) " +
        "ORDER BY full_name";

    /** Thêm customer mới */
    private static final String SQL_INSERT =
        "INSERT INTO customer (customer_id, full_name, email, phone, address, note, created_at, updated_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, SYSTIMESTAMP, SYSTIMESTAMP)";

    /** Cập nhật thông tin customer */
    private static final String SQL_UPDATE =
        "UPDATE customer " +
        "SET full_name = ?, email = ?, phone = ?, address = ?, note = ?, updated_at = SYSTIMESTAMP " +
        "WHERE customer_id = ?";

    /** Xóa customer (chỉ dùng sau khi BUS đã kiểm tra ràng buộc) */
    private static final String SQL_DELETE =
        "DELETE FROM customer WHERE customer_id = ?";

    /** Kiểm tra số điện thoại đã tồn tại chưa */
    private static final String SQL_EXISTS_PHONE =
        "SELECT COUNT(*) FROM customer WHERE phone = ? AND customer_id != ?";

    /** Kiểm tra email đã tồn tại chưa */
    private static final String SQL_EXISTS_EMAIL =
        "SELECT COUNT(*) FROM customer WHERE LOWER(email) = LOWER(?) AND customer_id != ?";

    /** Đếm booking đang hoạt động của customer */
    private static final String SQL_COUNT_ACTIVE_BOOKINGS =
        "SELECT COUNT(*) FROM booking " +
        "WHERE customer_id = ? AND status NOT IN ('CHECKED_OUT','CANCELLED')";

    /** Đếm số thú cưng của customer */
    private static final String SQL_COUNT_PETS =
        "SELECT COUNT(*) FROM pet WHERE customer_id = ?";

    /**
     * Lấy lịch sử dịch vụ của khách hàng.
     * JOIN: customer → booking → order_details → orders → payments → services
     *
     * Kết quả trả về dạng ResultSet thô (caller map sang DTO).
     * TODO: Tạo ServiceHistoryDTO riêng thay vì dùng List<Object[]>.
     * TODO: Optimize query, thêm index trên booking.customer_id.
     */
    private static final String SQL_SERVICE_HISTORY =
        "SELECT b.booking_id, b.status AS booking_status, " +
        "       b.checkin_expected_at, b.checkout_expected_at, " +
        "       s.service_name, bs.status AS service_status, bs.scheduled_at, " +
        "       od.unit_price, od.quantity, od.line_total, " +
        "       o.order_id, o.status AS order_status, o.grand_total, " +
        "       p.payment_method, p.amount AS paid_amount, p.status AS payment_status, p.paid_at " +
        "FROM booking b " +
        "LEFT JOIN booking_services bs ON b.booking_id = bs.booking_id " +
        "LEFT JOIN services s          ON bs.service_id = s.service_id " +
        "LEFT JOIN order_details od    ON b.booking_id = od.booking_id " +
        "LEFT JOIN orders o            ON od.order_id = o.order_id " +
        "LEFT JOIN payments p          ON o.order_id = p.order_id " +
        "WHERE b.customer_id = ? " +
        "ORDER BY b.created_at DESC, bs.scheduled_at DESC";

    // ── Public Methods ───────────────────────────────────────────

    /**
     * Tìm customer theo PK.
     *
     * @param customerId mã khách hàng
     * @return Customer hoặc null nếu không tồn tại
     * @throws SQLException nếu lỗi DB
     */
    public Customer findById(String customerId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {

            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /**
     * Lấy danh sách toàn bộ customer.
     * TODO: Thêm tham số page và pageSize.
     *
     * @return List<Customer>, rỗng nếu không có
     * @throws SQLException nếu lỗi DB
     */
    public List<Customer> findAll() throws SQLException {
        List<Customer> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /**
     * Tìm kiếm customer theo tên, SĐT, hoặc email (đều dùng LIKE).
     *
     * @param keyword từ khóa tìm kiếm (không null, không rỗng)
     * @return List<Customer> phù hợp
     * @throws SQLException nếu lỗi DB
     */
    public List<Customer> search(String keyword) throws SQLException {
        String pattern = "%" + keyword.trim() + "%";
        List<Customer> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SEARCH)) {

            ps.setString(1, pattern); // full_name
            ps.setString(2, pattern); // phone (không cần LOWER vì phone chỉ chứa số)
            ps.setString(3, pattern); // email

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Thêm mới customer.
     * Caller phải truyền vào customerId đã được generate (sequence / UUID / ...).
     *
     * @param customer đối tượng Customer đã có customerId, fullName, phone
     * @param conn connection dùng chung nếu trong transaction (null = tự tạo)
     * @throws SQLException nếu lỗi DB (bao gồm vi phạm UNIQUE)
     */
    public void insert(Customer customer, Connection conn) throws SQLException {
        boolean own = (conn == null);
        Connection c = own ? DBConnection.getConnection() : conn;
        try {
            PreparedStatement ps = c.prepareStatement(SQL_INSERT);
            ps.setString(1, customer.getCustomerId());
            ps.setString(2, customer.getFullName());
            ps.setString(3, customer.getEmail());      // có thể null
            ps.setString(4, customer.getPhone());
            ps.setString(5, customer.getAddress());    // có thể null
            ps.setString(6, customer.getNote());       // có thể null (CLOB)
            ps.executeUpdate();
            ps.close();
        } finally {
            if (own) DBConnection.closeQuietly(c);
        }
    }

    /**
     * Cập nhật thông tin customer.
     *
     * @param customer customer với thông tin mới, phải có customerId
     * @param conn connection dùng chung (null = tự tạo)
     * @return số dòng bị ảnh hưởng (0 nếu không tìm thấy)
     * @throws SQLException nếu lỗi DB
     */
    public int update(Customer customer, Connection conn) throws SQLException {
        boolean own = (conn == null);
        Connection c = own ? DBConnection.getConnection() : conn;
        try {
            PreparedStatement ps = c.prepareStatement(SQL_UPDATE);
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhone());
            ps.setString(4, customer.getAddress());
            ps.setString(5, customer.getNote());
            ps.setString(6, customer.getCustomerId());
            int rows = ps.executeUpdate();
            ps.close();
            return rows;
        } finally {
            if (own) DBConnection.closeQuietly(c);
        }
    }

    /**
     * Xóa customer theo ID.
     * BUS phải kiểm tra ràng buộc trước khi gọi method này.
     *
     * @param customerId mã khách hàng
     * @param conn connection dùng chung (null = tự tạo)
     * @return số dòng bị ảnh hưởng
     * @throws SQLException nếu lỗi DB
     */
    public int delete(String customerId, Connection conn) throws SQLException {
        boolean own = (conn == null);
        Connection c = own ? DBConnection.getConnection() : conn;
        try {
            PreparedStatement ps = c.prepareStatement(SQL_DELETE);
            ps.setString(1, customerId);
            int rows = ps.executeUpdate();
            ps.close();
            return rows;
        } finally {
            if (own) DBConnection.closeQuietly(c);
        }
    }

    /**
     * Kiểm tra số điện thoại đã được dùng bởi customer khác chưa.
     * Dùng khi thêm mới (excludeId = "") hoặc cập nhật (excludeId = customerId hiện tại).
     *
     * @param phone     số điện thoại cần kiểm tra
     * @param excludeId customer_id sẽ được bỏ qua khi kiểm tra
     * @return true nếu phone đã tồn tại ở customer khác
     * @throws SQLException nếu lỗi DB
     */
    public boolean existsByPhone(String phone, String excludeId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_EXISTS_PHONE)) {

            ps.setString(1, phone);
            ps.setString(2, excludeId != null ? excludeId : "");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Kiểm tra email đã tồn tại ở customer khác chưa.
     *
     * @param email     email cần kiểm tra
     * @param excludeId customer_id bỏ qua
     * @return true nếu email đã tồn tại
     * @throws SQLException nếu lỗi DB
     */
    public boolean existsByEmail(String email, String excludeId) throws SQLException {
        if (email == null || email.isEmpty()) return false;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_EXISTS_EMAIL)) {

            ps.setString(1, email);
            ps.setString(2, excludeId != null ? excludeId : "");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Đếm số booking đang hoạt động của customer.
     * Dùng để kiểm tra trước khi xóa customer.
     *
     * @param customerId mã khách hàng
     * @return số lượng booking chưa kết thúc
     * @throws SQLException nếu lỗi DB
     */
    public int countActiveBookings(String customerId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT_ACTIVE_BOOKINGS)) {

            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Đếm số thú cưng của customer.
     * Dùng để kiểm tra trước khi xóa customer.
     *
     * @param customerId mã khách hàng
     * @return số lượng pet
     * @throws SQLException nếu lỗi DB
     */
    public int countPets(String customerId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT_PETS)) {

            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Lấy lịch sử dịch vụ của khách hàng.
     * Trả về ResultSet thô để caller (BUS/Controller) map thành DTO phù hợp.
     *
     * QUAN TRỌNG: Caller phải đóng cả Connection và ResultSet sau khi dùng xong.
     * Pattern: try-with-resources ở caller.
     *
     * TODO: Tạo ServiceHistoryDTO và map ở đây.
     * TODO: Thêm filter theo ngày, loại dịch vụ, trạng thái.
     * TODO: Thêm pagination.
     *
     * @param customerId mã khách hàng
     * @return List các mảng Object[] chứa dữ liệu lịch sử
     * @throws SQLException nếu lỗi DB
     */
    public List<Object[]> getServiceHistory(String customerId) throws SQLException {
        List<Object[]> result = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SERVICE_HISTORY)) {

            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                while (rs.next()) {
                    Object[] row = new Object[colCount];
                    for (int i = 1; i <= colCount; i++) {
                        row[i - 1] = rs.getObject(i);
                    }
                    result.add(row);
                }
            }
        }
        return result;
    }

    // ── Private Helpers ──────────────────────────────────────────

    /**
     * Map một hàng ResultSet sang Customer.
     * Gọi sau rs.next() == true.
     */
    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getString("customer_id"));
        c.setFullName(rs.getString("full_name"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address"));

        // CLOB → String
        Clob noteClob = rs.getClob("note");
        if (noteClob != null) {
            c.setNote(noteClob.getSubString(1, (int) noteClob.length()));
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null)
            c.setCreatedAt(createdAt.toInstant().atOffset(java.time.ZoneOffset.UTC));

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null)
            c.setUpdatedAt(updatedAt.toInstant().atOffset(java.time.ZoneOffset.UTC));

        return c;
    }
}
