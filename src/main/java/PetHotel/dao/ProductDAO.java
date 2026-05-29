package PetHotel.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import PetHotel.model.Product;
import PetHotel.util.DBConnection;
import PetHotel.util.IDGenerator;

public class ProductDAO {
    private static final Set<String> INACTIVE_PRODUCT_IDS = ConcurrentHashMap.newKeySet();
    private static final Map<String, BigDecimal> MIN_QUANTITY_BY_ID = new ConcurrentHashMap<>();
    private static final Map<String, String> NOTE_BY_ID = new ConcurrentHashMap<>();

    private static final String PRODUCT_SELECT =
        "SELECT p.product_id, p.product_category_id, cp.category_name, p.product_name, " +
        "       p.unit, p.cost_price, p.created_at, p.updated_at " +
        "FROM product p " +
        "LEFT JOIN category_product cp ON cp.product_category_id = p.product_category_id ";

    public List<Product> findAll() throws SQLException {
        return search(null, null, null);
    }

    public List<Product> search(String keyword, String category, Boolean active) throws SQLException {
        String sql = PRODUCT_SELECT +
            "WHERE (? IS NULL OR LOWER(p.product_id) LIKE LOWER(?) OR LOWER(p.product_name) LIKE LOWER(?)) " +
            "  AND (? IS NULL OR cp.category_name = ?) " +
            "ORDER BY p.product_id";

        String normalizedKeyword = normalize(keyword);
        String pattern = normalizedKeyword == null ? null : "%" + normalizedKeyword + "%";
        String normalizedCategory = normalize(category);

        List<Product> products = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizedKeyword);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, normalizedCategory);
            ps.setString(5, normalizedCategory);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product product = mapProduct(rs);
                    if (active == null || product.isActive() == active) {
                        products.add(product);
                    }
                }
            }
        }
        return products;
    }

    public Product findById(String productId) throws SQLException {
        String sql = PRODUCT_SELECT + "WHERE p.product_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapProduct(rs) : null;
            }
        }
    }

    public void insert(Product product) throws SQLException {
        String sql =
            "INSERT INTO product (product_id, product_category_id, product_name, unit, cost_price, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, SYSTIMESTAMP, SYSTIMESTAMP)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String categoryId = findOrCreateCategoryId(conn, product.getProductCategory());

            ps.setString(1, product.getProductId());
            ps.setString(2, categoryId);
            ps.setString(3, product.getProductName());
            ps.setString(4, product.getUnit());
            ps.setBigDecimal(5, valueOrZero(product.getImportPrice()));
            ps.executeUpdate();
        }

        rememberManagementState(product);
    }

    public void update(Product product) throws SQLException {
        String sql =
            "UPDATE product " +
            "SET product_category_id = ?, product_name = ?, unit = ?, cost_price = ?, updated_at = SYSTIMESTAMP " +
            "WHERE product_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String categoryId = findOrCreateCategoryId(conn, product.getProductCategory());

            ps.setString(1, categoryId);
            ps.setString(2, product.getProductName());
            ps.setString(3, product.getUnit());
            ps.setBigDecimal(4, valueOrZero(product.getImportPrice()));
            ps.setString(5, product.getProductId());
            ps.executeUpdate();
        }

        rememberManagementState(product);
    }

    public void softDelete(String productId) {
        String normalizedId = normalize(productId);
        if (normalizedId != null) {
            INACTIVE_PRODUCT_IDS.add(normalizedId);
        }
    }

    public boolean existsByName(String productName) throws SQLException {
        return existsByNameExceptId(productName, null);
    }

    public boolean existsByNameExceptId(String productName, String productId) throws SQLException {
        String sql =
            "SELECT COUNT(*) " +
            "FROM product " +
            "WHERE LOWER(product_name) = LOWER(?) " +
            "  AND (? IS NULL OR product_id <> ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productName);
            ps.setString(2, productId);
            ps.setString(3, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public String generateNextProductId() throws SQLException {
        return IDGenerator.nextProductId();
    }

    public List<String> findCategoryNames() throws SQLException {
        String sql = "SELECT category_name FROM category_product ORDER BY category_name";

        List<String> categories = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categories.add(rs.getString("category_name"));
            }
        }
        return categories;
    }


    public List<Product> findActiveProducts() throws SQLException {
        List<Product> list = new ArrayList<>();

        String sql =
            "SELECT p.product_id, p.product_name, p.product_category_id, " +
            "       cp.category_name AS product_category, " +
            "       p.unit, p.import_price, p.min_quantity, p.is_active, " +
            "       p.note, p.created_at, p.updated_at " +
            "FROM product p " +
            "LEFT JOIN category_product cp " +
            "  ON cp.product_category_id = p.product_category_id " +
            "WHERE p.is_active = 1 " +
            "ORDER BY p.product_name";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Product p = new Product();
                p.setProductId(rs.getString("product_id"));
                p.setProductName(rs.getString("product_name"));
                p.setProductCategory(rs.getString("product_category"));
                p.setUnit(rs.getString("unit"));
                p.setImportPrice(rs.getBigDecimal("import_price"));
                p.setMinQuantity(rs.getBigDecimal("min_quantity"));
                p.setActive(rs.getInt("is_active") == 1);
                p.setNote(rs.getString("note"));

                list.add(p);
            }
        }

        return list;
    }
    
    public boolean existsCategoryByName(String categoryName) throws SQLException {
        String normalizedCategory = normalize(categoryName);
        if (normalizedCategory == null) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM category_product WHERE LOWER(category_name) = LOWER(?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizedCategory);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public String createCategory(String categoryName) throws SQLException {
        try (Connection conn = getConnection()) {
            return findOrCreateCategoryId(conn, categoryName);
        }
    }

    private String findOrCreateCategoryId(Connection conn, String categoryName) throws SQLException {
        String normalizedCategory = normalize(categoryName);
        if (normalizedCategory == null) {
            normalizedCategory = "Khác";
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT product_category_id FROM category_product WHERE LOWER(category_name) = LOWER(?)")) {
            ps.setString(1, normalizedCategory);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("product_category_id");
                }
            }
        }

        String categoryId = generateNextCategoryId(conn);
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO category_product (product_category_id, category_name, created_at, updated_at) " +
                "VALUES (?, ?, SYSTIMESTAMP, SYSTIMESTAMP)")) {
            ps.setString(1, categoryId);
            ps.setString(2, normalizedCategory);
            ps.executeUpdate();
        }
        return categoryId;
    }

    private String generateNextCategoryId(Connection conn) throws SQLException {
        String sql =
            "SELECT NVL(MAX(TO_NUMBER(SUBSTR(product_category_id, 3))), 0) + 1 AS next_id " +
            "FROM category_product " +
            "WHERE REGEXP_LIKE(product_category_id, '^PC[0-9]{3}$')";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return String.format("PC%03d", rs.getInt("next_id"));
            }
        }
        return "PC001";
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getString("product_id"));
        product.setProductCategoryId(rs.getString("product_category_id"));
        product.setProductCategory(rs.getString("category_name"));
        product.setProductName(rs.getString("product_name"));
        product.setUnit(rs.getString("unit"));
        product.setImportPrice(rs.getBigDecimal("cost_price"));
        product.setMinQuantity(MIN_QUANTITY_BY_ID.getOrDefault(product.getProductId(), BigDecimal.ZERO));
        product.setActive(!INACTIVE_PRODUCT_IDS.contains(product.getProductId()));
        product.setNote(NOTE_BY_ID.get(product.getProductId()));
        product.setCreatedAt(readOffsetDateTime(rs, "created_at"));
        product.setUpdatedAt(readOffsetDateTime(rs, "updated_at"));
        return product;
    }

    private void rememberManagementState(Product product) {
        String productId = normalize(product.getProductId());
        if (productId == null) {
            return;
        }

        MIN_QUANTITY_BY_ID.put(productId, valueOrZero(product.getMinQuantity()));
        String note = normalize(product.getNote());
        if (note == null) {
            NOTE_BY_ID.remove(productId);
        } else {
            NOTE_BY_ID.put(productId, note);
        }

        if (product.isActive()) {
            INACTIVE_PRODUCT_IDS.remove(productId);
        } else {
            INACTIVE_PRODUCT_IDS.add(productId);
        }
    }

    private OffsetDateTime readOffsetDateTime(ResultSet rs, String columnName) throws SQLException {
        Object value = rs.getObject(columnName);
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime;
        }
        Timestamp timestamp = rs.getTimestamp(columnName);
        return timestamp == null ? null : timestamp.toInstant().atOffset(ZoneOffset.UTC);
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Connection getConnection() throws SQLException {
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu.");
        }
        return conn;
    }
}
