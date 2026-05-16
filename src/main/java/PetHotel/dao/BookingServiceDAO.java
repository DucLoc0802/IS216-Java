package PetHotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import PetHotel.model.BookingService;
import PetHotel.model.Customer;
import PetHotel.model.Employee;
import PetHotel.model.Pet;
import PetHotel.model.PetService;
import PetHotel.util.DBConnection;

/**
 * BookingServiceDAO — Quản lý dữ liệu lịch dịch vụ/grooming từ bảng BOOKING_SERVICES.
 * 
 * Hỗ trợ:
 *  - Lấy lịch grooming theo ngày, nhân viên, thú cưng, trạng thái
 *  - Cập nhật trạng thái, tạo mới, xóa
 */
public class BookingServiceDAO {

    // SQL Queries
    private static final String SQL_FIND_BY_DATE_AND_FILTER =
        "SELECT bs.booking_service_id, bs.booking_id, bs.service_id, bs.pet_id, bs.employee_id, " +
        "       bs.scheduled_at, bs.status, bs.note, bs.created_at, bs.updated_at, " +
        "       sv.service_name, e.full_name, p.pet_name, p.species, c.customer_id, c.full_name as customer_name " +
        "FROM booking_services bs " +
        "LEFT JOIN services sv ON bs.service_id = sv.service_id " +
        "LEFT JOIN employee e ON bs.employee_id = e.employee_id " +
        "LEFT JOIN booking b ON bs.booking_id = b.booking_id " +
        "LEFT JOIN pet p ON bs.pet_id = p.pet_id " +
        "LEFT JOIN customer c ON b.customer_id = c.customer_id " +
        "WHERE TRUNC(bs.scheduled_at) = TRUNC(TO_DATE(?, 'YYYY-MM-DD')) " +
        "  AND (? IS NULL OR bs.employee_id = ?) " +
        "  AND (? IS NULL OR bs.status = ?) " +
        "ORDER BY bs.scheduled_at ASC";

    private static final String SQL_FIND_ALL_BY_DATE_RANGE =
        "SELECT bs.booking_service_id, bs.booking_id, bs.service_id, bs.pet_id, bs.employee_id, " +
        "       bs.scheduled_at, bs.status, bs.note, bs.created_at, bs.updated_at, " +
        "       sv.service_name, e.full_name, p.pet_name, c.full_name as customer_name " +
        "FROM booking_services bs " +
        "LEFT JOIN services sv ON bs.service_id = sv.service_id " +
        "LEFT JOIN employee e ON bs.employee_id = e.employee_id " +
        "LEFT JOIN booking b ON bs.booking_id = b.booking_id " +
        "LEFT JOIN pet p ON bs.pet_id = p.pet_id " +
        "LEFT JOIN customer c ON b.customer_id = c.customer_id " +
        "WHERE TRUNC(bs.scheduled_at) >= TRUNC(TO_DATE(?, 'YYYY-MM-DD')) " +
        "  AND TRUNC(bs.scheduled_at) <= TRUNC(TO_DATE(?, 'YYYY-MM-DD')) " +
        "ORDER BY bs.scheduled_at ASC";

    private static final String SQL_UPDATE_STATUS =
        "UPDATE booking_services SET status = ?, updated_at = SYSTIMESTAMP WHERE booking_service_id = ?";

    private static final String SQL_COUNT_PENDING_TODAY =
        "SELECT COUNT(*) FROM booking_services " +
        "WHERE TRUNC(scheduled_at) = TRUNC(SYSDATE) AND status IN ('PENDING', 'SCHEDULED')";

    private static final String SQL_FIND_BY_EMPLOYEE_TODAY =
    "SELECT bs.booking_service_id, bs.booking_id, bs.service_id, bs.pet_id, bs.employee_id, " +
    "       bs.scheduled_at, bs.status, bs.note, bs.created_at, bs.updated_at, " +
    "       sv.service_name, e.full_name AS employee_name, p.pet_name, " +
    "       p.species, c.customer_id, c.full_name AS customer_name " +
    "FROM booking_services bs " +
    "LEFT JOIN services sv ON bs.service_id = sv.service_id " +
    "LEFT JOIN employee e ON bs.employee_id = e.employee_id " +
    "LEFT JOIN booking b ON bs.booking_id = b.booking_id " +
    "LEFT JOIN pet p ON bs.pet_id = p.pet_id " +
    "LEFT JOIN customer c ON b.customer_id = c.customer_id " +
    "WHERE bs.employee_id = ? " +
    "  AND TRUNC(bs.scheduled_at) = TRUNC(SYSDATE) " +
    "ORDER BY bs.scheduled_at ASC";

    /**
     * Lấy lịch grooming theo ngày, nhân viên, trạng thái
     * @param dateStr Định dạng YYYY-MM-DD
     * @param employeeId null = tất cả
     * @param status null = tất cả
     */
    public List<BookingService> findByDateAndFilter(String dateStr, String employeeId, String status) throws SQLException {
        List<BookingService> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_DATE_AND_FILTER)) {
            ps.setString(1, dateStr);
            ps.setString(2, employeeId);
            ps.setString(3, employeeId);
            ps.setString(4, status);
            ps.setString(5, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private String generateShortId(String prefix) {
        int randomNumber = 100000 + new Random().nextInt(900000);
        return prefix + randomNumber;
    }

    /**
     * Lấy lịch grooming trong khoảng ngày
     */
    public List<BookingService> findByDateRange(String fromDateStr, String toDateStr) throws SQLException {
        List<BookingService> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL_BY_DATE_RANGE)) {
            ps.setString(1, fromDateStr);
            ps.setString(2, toDateStr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Đếm lịch grooming chờ xử lý hôm nay
     */
    public int countPendingToday() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_COUNT_PENDING_TODAY)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    /**
     * Lấy lịch grooming của nhân viên trong ngày hôm nay
     */
    public List<BookingService> findByEmployeeToday(String employeeId) throws SQLException {
        List<BookingService> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_EMPLOYEE_TODAY)) {
            ps.setString(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Cập nhật trạng thái lịch grooming
     */
    public void updateStatus(String bookingServiceId, String newStatus) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_STATUS)) {
            ps.setString(1, newStatus);
            ps.setString(2, bookingServiceId);
            ps.executeUpdate();
        }
    }

    private String getStringSafe(ResultSet rs, String columnName) {
        try {
            return rs.getString(columnName);
        } catch (Exception e) {
            return null;
        }
    }

    private BookingService mapRow(ResultSet rs) throws SQLException {
        BookingService bs = new BookingService();
        bs.setBookingServiceId(rs.getString("booking_service_id"));
        bs.setBookingId(rs.getString("booking_id"));
        bs.setServiceId(rs.getString("service_id"));
        bs.setPetId(rs.getString("pet_id"));
        bs.setEmployeeId(rs.getString("employee_id"));
        
        Timestamp scheduledTs = rs.getTimestamp("scheduled_at");
        if (scheduledTs != null) {
            bs.setScheduledAt(scheduledTs.toInstant().atOffset(java.time.ZoneOffset.UTC));
        }
        
        bs.setStatus(rs.getString("status"));
        bs.setNote(rs.getString("note"));
        
        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
            bs.setCreatedAt(createdTs.toInstant().atOffset(java.time.ZoneOffset.UTC));
        }
        
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if (updatedTs != null) {
            bs.setUpdatedAt(updatedTs.toInstant().atOffset(java.time.ZoneOffset.UTC));
        }
        
        bs.setServiceName(getStringSafe(rs, "service_name"));
        bs.setPetName(getStringSafe(rs, "pet_name"));
        bs.setPetSpecies(getStringSafe(rs, "species"));
        bs.setCustomerName(getStringSafe(rs, "customer_name"));
        bs.setCustomerPhone(getStringSafe(rs, "phone"));
        bs.setCustomerAddress(getStringSafe(rs, "address"));
        return bs;
    }
    public List<Customer> getAllCustomers() throws SQLException {
    List<Customer> list = new ArrayList<>();

    String sql =
        "SELECT customer_id, full_name, email, phone, address, note " +
        "FROM customer " +
        "ORDER BY full_name";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            Customer c = new Customer();
            c.setCustomerId(rs.getString("customer_id"));
            c.setFullName(rs.getString("full_name"));
            c.setEmail(rs.getString("email"));
            c.setPhone(rs.getString("phone"));
            c.setAddress(rs.getString("address"));
            c.setNote(rs.getString("note"));
            list.add(c);
        }
    }

    return list;
}

public List<Pet> getPetsByCustomer(String customerId) throws SQLException {
    List<Pet> list = new ArrayList<>();

    String sql =
        "SELECT pet_id, customer_id, pet_name, species, breed, sex, weight_kg, special_note " +
        "FROM pet " +
        "WHERE customer_id = ? " +
        "ORDER BY pet_name";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, customerId);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Pet p = new Pet();
                p.setPetId(rs.getString("pet_id"));
                p.setCustomerId(rs.getString("customer_id"));
                p.setPetName(rs.getString("pet_name"));
                p.setSpecies(rs.getString("species"));
                p.setBreed(rs.getString("breed"));
                p.setSex(rs.getString("sex"));
                p.setWeightKg(rs.getDouble("weight_kg"));
                p.setSpecialNote(rs.getString("special_note"));
                list.add(p);
            }
        }
    }

    return list;
}

public List<PetService> getGroomingServices() throws SQLException {
    List<PetService> list = new ArrayList<>();

    String sql =
        "SELECT s.service_id, s.service_category_id, s.service_name, " +
        "       s.species, s.base_price, s.duration_minutes " +
        "FROM services s " +
        "JOIN category_services cs ON s.service_category_id = cs.service_category_id " +
        "WHERE s.is_active = 1 " +
        "  AND (UPPER(cs.category_name) LIKE '%GROOMING%' " +
        "       OR UPPER(s.service_name) LIKE '%GROOMING%' " +
        "       OR UPPER(s.service_name) LIKE '%TẮM%' " +
        "       OR UPPER(s.service_name) LIKE '%CẮT%') " +
        "ORDER BY s.service_name";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            PetService s = new PetService();
            s.setServiceId(rs.getString("service_id"));
            s.setServiceCategoryId(rs.getString("service_category_id"));
            s.setServiceName(rs.getString("service_name"));
            s.setSpecies(rs.getString("species"));
            s.setBasePrice(rs.getDouble("base_price"));
            s.setDurationMinutes(rs.getInt("duration_minutes"));
            list.add(s);
        }
    }

    return list;
}

public List<Employee> getWorkingEmployeesByBranch(String branchId) throws SQLException {
    List<Employee> list = new ArrayList<>();

    String sql =
        "SELECT e.employee_id, e.branch_id, e.full_name, e.email, e.phone, e.status_code " +
        "FROM employee e " +
        "JOIN app_user au ON au.employee_id = e.employee_id " +
        "WHERE e.branch_id = ? " +
        "  AND e.status_code = 'WORKING' " +
        "  AND au.is_active = 1 " +
        "  AND au.role_emp = 2 " +
        "ORDER BY e.full_name";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, branchId);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Employee e = new Employee();
                e.setEmployeeId(rs.getString("employee_id"));
                e.setBranchId(rs.getString("branch_id"));
                e.setFullName(rs.getString("full_name"));
                e.setEmail(rs.getString("email"));
                e.setPhone(rs.getString("phone"));
                e.setStatusCode(rs.getString("status_code"));
                list.add(e);
            }
        }
    }

    return list;
}

public void createGroomingSchedule(
        String customerId,
        String petId,
        String serviceId,
        String employeeId,
        String branchId,
        LocalDate scheduleDate,
        LocalTime scheduleTime,
        String note
) throws SQLException {

    String bookingId = generateShortId("BK");
    String bookingServiceId = generateShortId("BS");

    LocalDateTime scheduledDateTime = LocalDateTime.of(scheduleDate, scheduleTime);
    Timestamp scheduledTimestamp = Timestamp.valueOf(scheduledDateTime);

    String insertBookingSql =
        "INSERT INTO booking ( " +
        "    booking_id, customer_id, branch_id, " +
        "    checkin_expected_at, checkout_expected_at, " +
        "    status, deposit_amount, special_note, created_at, updated_at " +
        ") VALUES ( " +
        "    ?, ?, ?, ?, ?, 'CONFIRMED', 0, ?, SYSTIMESTAMP, SYSTIMESTAMP " +
        ")";

    String insertBookingServiceSql =
        "INSERT INTO booking_services ( " +
        "    booking_service_id, booking_id, service_id, pet_id, employee_id, " +
        "    scheduled_at, status, note, created_at, updated_at " +
        ") VALUES ( " +
        "    ?, ?, ?, ?, ?, ?, 'SCHEDULED', ?, SYSTIMESTAMP, SYSTIMESTAMP " +
        ")";

    Connection conn = null;

    try {
        conn = DBConnection.getConnection();
        conn.setAutoCommit(false);

        try (PreparedStatement ps = conn.prepareStatement(insertBookingSql)) {
            ps.setString(1, bookingId);
            ps.setString(2, customerId);
            ps.setString(3, branchId);
            ps.setTimestamp(4, scheduledTimestamp);

            // Grooming tạm tính kết thúc sau 2 giờ.
            ps.setTimestamp(5, Timestamp.valueOf(scheduledDateTime.plusHours(2)));

            if (note == null || note.trim().isEmpty()) {
                ps.setNull(6, Types.CLOB);
            } else {
                ps.setString(6, note);
            }

            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement(insertBookingServiceSql)) {
            ps.setString(1, bookingServiceId);
            ps.setString(2, bookingId);
            ps.setString(3, serviceId);
            ps.setString(4, petId);
            ps.setString(5, employeeId);
            ps.setTimestamp(6, scheduledTimestamp);

            if (note == null || note.trim().isEmpty()) {
                ps.setNull(7, Types.CLOB);
            } else {
                ps.setString(7, note);
            }

            ps.executeUpdate();
        }

        conn.commit();

    } catch (SQLException e) {
        if (conn != null) {
            conn.rollback();
        }
        throw e;

    } finally {
        if (conn != null) {
            conn.setAutoCommit(true);
            conn.close();
        }
    }
}

/**
 * Lấy danh sách dịch vụ grooming theo loại dịch vụ
 */
public List<PetService> getGroomingServicesByCategory(String serviceCategoryId) throws SQLException {
    List<PetService> list = new ArrayList<>();

    String sql =
        "SELECT s.service_id, s.service_category_id, s.service_name, " +
        "       s.species, s.base_price, s.duration_minutes " +
        "FROM services s " +
        "WHERE s.service_category_id = ? " +
        "  AND s.is_active = 1 " +
        "ORDER BY s.service_name";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, serviceCategoryId);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PetService s = new PetService();
                s.setServiceId(rs.getString("service_id"));
                s.setServiceCategoryId(rs.getString("service_category_id"));
                s.setServiceName(rs.getString("service_name"));
                s.setSpecies(rs.getString("species"));
                s.setBasePrice(rs.getDouble("base_price"));
                s.setDurationMinutes(rs.getInt("duration_minutes"));
                list.add(s);
            }
        }
    }

    return list;
}

/**
 * Lấy danh sách công việc grooming chưa phân công
 */
public List<BookingService> findUnassignedGroomingTasks(String dateStr, String status, String keyword) throws SQLException {
    List<BookingService> list = new ArrayList<>();

    StringBuilder sql = new StringBuilder(
        "SELECT bs.booking_service_id, bs.booking_id, bs.service_id, bs.pet_id, bs.employee_id, " +
        "       bs.scheduled_at, bs.status, bs.note, bs.created_at, bs.updated_at, " +
        "       sv.service_name, p.pet_name, c.full_name as customer_name " +
        "FROM booking_services bs " +
        "LEFT JOIN services sv ON bs.service_id = sv.service_id " +
        "LEFT JOIN booking b ON bs.booking_id = b.booking_id " +
        "LEFT JOIN pet p ON bs.pet_id = p.pet_id " +
        "LEFT JOIN customer c ON b.customer_id = c.customer_id " +
        "WHERE TRUNC(bs.scheduled_at) = TRUNC(TO_DATE(?, 'YYYY-MM-DD')) " +
        "  AND bs.employee_id IS NULL " // Only unassigned tasks
    );

    if (status != null && !status.isEmpty()) {
        sql.append(" AND bs.status = ? ");
    }

    if (keyword != null && !keyword.isEmpty()) {
        sql.append(" AND (LOWER(p.pet_name) LIKE LOWER(?) OR LOWER(c.full_name) LIKE LOWER(?)) ");
    }

    sql.append(" ORDER BY bs.scheduled_at ASC");

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql.toString())) {

        int paramIndex = 1;
        ps.setString(paramIndex++, dateStr);

        if (status != null && !status.isEmpty()) {
            ps.setString(paramIndex++, status);
        }

        if (keyword != null && !keyword.isEmpty()) {
            String pattern = "%" + keyword + "%";
            ps.setString(paramIndex++, pattern);
            ps.setString(paramIndex++, pattern);
        }

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
    }

    return list;
}

/**
 * Phân công nhân viên cho công việc grooming
 */
public void assignEmployeeToTask(String bookingServiceId, String employeeId, String note) throws SQLException {
    String sql = "UPDATE booking_services SET employee_id = ?, status = 'SCHEDULED', note = ?, updated_at = SYSTIMESTAMP " +
                 "WHERE booking_service_id = ?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, employeeId);
        if (note != null && !note.isEmpty()) {
            ps.setString(2, note);
        } else {
            ps.setNull(2, Types.CLOB);
        }
        ps.setString(3, bookingServiceId);

        ps.executeUpdate();
    }
}

/**
 * Đếm số công việc của nhân viên trong một ngày
 */
public int countEmployeeTasksByDate(String employeeId, String dateStr) throws SQLException {
    String sql = "SELECT COUNT(*) FROM booking_services " +
                 "WHERE employee_id = ? AND TRUNC(scheduled_at) = TRUNC(TO_DATE(?, 'YYYY-MM-DD'))";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, employeeId);
        ps.setString(2, dateStr);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
    }

    return 0;
}

/**
 * Lấy danh sách công việc được phân công cho nhân viên
 */
public List<BookingService> findEmployeeAssignedTasks(String employeeId, String dateStr, String status, String keyword) throws SQLException {
    List<BookingService> list = new ArrayList<>();

    StringBuilder sql = new StringBuilder(
        "SELECT bs.booking_service_id, bs.booking_id, bs.service_id, bs.pet_id, bs.employee_id, " +
        "       bs.scheduled_at, bs.status, bs.note, bs.created_at, bs.updated_at, " +
        "       sv.service_name, p.pet_name, p.species, " +
        "       c.full_name AS customer_name, c.phone, c.address " +
        "FROM booking_services bs " +
        "LEFT JOIN services sv ON bs.service_id = sv.service_id " +
        "LEFT JOIN booking b ON bs.booking_id = b.booking_id " +
        "LEFT JOIN pet p ON bs.pet_id = p.pet_id " +
        "LEFT JOIN customer c ON b.customer_id = c.customer_id " +
        "WHERE bs.employee_id = ? " +
        "  AND TRUNC(bs.scheduled_at) = TRUNC(TO_DATE(?, 'YYYY-MM-DD')) "
    );

    if (status != null && !status.isEmpty()) {
        sql.append(" AND bs.status = ? ");
    }

    if (keyword != null && !keyword.isEmpty()) {
        sql.append(" AND (LOWER(p.pet_name) LIKE LOWER(?) OR LOWER(c.full_name) LIKE LOWER(?)) ");
    }

    sql.append(" ORDER BY bs.scheduled_at ASC");

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql.toString())) {

        int paramIndex = 1;
        ps.setString(paramIndex++, employeeId);
        ps.setString(paramIndex++, dateStr);

        if (status != null && !status.isEmpty()) {
            ps.setString(paramIndex++, status);
        }

        if (keyword != null && !keyword.isEmpty()) {
            String pattern = "%" + keyword + "%";
            ps.setString(paramIndex++, pattern);
            ps.setString(paramIndex++, pattern);
        }

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
    }

    return list;
}

/**
 * Đếm số công việc của nhân viên theo trạng thái trong một ngày
 */
public int countEmployeeTasksByDateAndStatus(String employeeId, String dateStr, String status) throws SQLException {
    String sql = "SELECT COUNT(*) FROM booking_services " +
                 "WHERE employee_id = ? AND TRUNC(scheduled_at) = TRUNC(TO_DATE(?, 'YYYY-MM-DD')) " +
                 "  AND (? IS NULL OR status = ?)";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, employeeId);
        ps.setString(2, dateStr);
        if (status != null && !status.isEmpty()) {
            ps.setString(3, status);
            ps.setString(4, status);
        } else {
            ps.setNull(3, Types.VARCHAR);
            ps.setNull(4, Types.VARCHAR);
        }

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
    }

    return 0;
}
}
