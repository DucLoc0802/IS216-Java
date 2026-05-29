package PetHotel.bus;

import PetHotel.dao.ProductDAO;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.model.Product;
import PetHotel.util.Role;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ProductBUS {
    public static final String STATUS_ALL = "Tất cả";
    public static final String STATUS_ACTIVE = "Đang sử dụng";
    public static final String STATUS_INACTIVE = "Ngừng sử dụng";

    private static final List<String> DEFAULT_CATEGORIES = List.of(
        "Vệ sinh",
        "Cắt tỉa",
        "Nhuộm",
        "Khác"
    );

    private static final Set<String> ALLOWED_UNITS = Set.of("G", "KG", "L", "ML");

    private final ProductDAO productDAO = new ProductDAO();

    public List<Product> getProducts(String keyword, String category, String status) throws SQLException {
        return productDAO.search(
            normalize(keyword),
            normalizeCategory(category),
            parseStatus(status)
        );
    }

    public Product getProductById(String productId) throws SQLException {
        String normalizedId = normalize(productId);
        if (normalizedId == null) {
            throw new ValidationException("Mã sản phẩm không hợp lệ.");
        }
        return productDAO.findById(normalizedId);
    }

    public void createProduct(Product product) throws SQLException {
        validateProduct(product);
        normalizeProduct(product);

        if (productDAO.existsByName(product.getProductName())) {
            throw new ValidationException("Tên sản phẩm đã tồn tại.");
        }

        if (normalize(product.getProductId()) == null) {
            product.setProductId(productDAO.generateNextProductId());
        }
        product.setActive(true);
        productDAO.insert(product);
    }

    public void updateProduct(Product product) throws SQLException {
        String productId = normalize(product == null ? null : product.getProductId());
        if (productId == null) {
            throw new ValidationException("Vui lòng chọn sản phẩm cần sửa.");
        }

        validateProduct(product);
        normalizeProduct(product);
        product.setProductId(productId);

        if (productDAO.existsByNameExceptId(product.getProductName(), productId)) {
            throw new ValidationException("Tên sản phẩm đã tồn tại.");
        }

        productDAO.update(product);
    }

    public void deleteProduct(String productId) throws SQLException {
        String normalizedId = normalize(productId);
        if (normalizedId == null) {
            throw new ValidationException("Vui lòng chọn sản phẩm cần xóa.");
        }
        productDAO.softDelete(normalizedId);
    }

    public void validateProduct(Product product) {
        if (product == null) {
            throw new ValidationException("Dữ liệu sản phẩm không hợp lệ.");
        }

        if (normalize(product.getProductName()) == null) {
            throw new ValidationException("Tên sản phẩm không được rỗng.");
        }
        if (normalize(product.getUnit()) == null) {
            throw new ValidationException("Đơn vị tính không được rỗng.");
        }
        String normalizedUnit = product.getUnit().trim().toUpperCase();
        if (!ALLOWED_UNITS.contains(normalizedUnit)) {
            throw new ValidationException("Đơn vị tính chỉ được là G, KG, L hoặc ML.");
        }
        if (product.getImportPrice() == null || product.getImportPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Giá nhập phải lớn hơn hoặc bằng 0.");
        }
        if (product.getMinQuantity() == null || product.getMinQuantity().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Tồn tối thiểu phải lớn hơn hoặc bằng 0.");
        }
        if (product.getProductName().trim().length() > 160) {
            throw new ValidationException("Tên sản phẩm không được vượt quá 160 ký tự.");
        }
        if (product.getUnit().trim().length() > 30) {
            throw new ValidationException("Đơn vị tính không được vượt quá 30 ký tự.");
        }
        String category = normalize(product.getProductCategory());
        if (category != null && category.length() > 100) {
            throw new ValidationException("Loại sản phẩm không được vượt quá 100 ký tự.");
        }
        String note = normalize(product.getNote());
        if (note != null && note.length() > 4000) {
            throw new ValidationException("Ghi chú không được vượt quá 4000 ký tự.");
        }
    }

    public List<String> getCategories() throws SQLException {
        Set<String> categories = new LinkedHashSet<>();
        categories.addAll(DEFAULT_CATEGORIES);
        categories.addAll(productDAO.findCategoryNames());
        return new ArrayList<>(categories);
    }

    public String createCategory(String categoryName, AppUser currentUser) throws SQLException {
        requireManager(currentUser);

        String normalizedName = normalize(categoryName);
        if (normalizedName == null) {
            throw new ValidationException("Tên loại sản phẩm không được rỗng.");
        }
        if (normalizedName.length() > 100) {
            throw new ValidationException("Tên loại sản phẩm không được vượt quá 100 ký tự.");
        }
        if (productDAO.existsCategoryByName(normalizedName)) {
            throw new ValidationException("Loại sản phẩm đã tồn tại.");
        }

        return productDAO.createCategory(normalizedName);
    }

    private void normalizeProduct(Product product) {
        product.setProductName(product.getProductName().trim());
        product.setUnit(product.getUnit().trim().toUpperCase());

        String category = normalize(product.getProductCategory());
        product.setProductCategory(category == null ? "Khác" : category);

        String note = normalize(product.getNote());
        product.setNote(note);
    }

    private void requireManager(AppUser currentUser) {
        if (currentUser == null) {
            throw new ValidationException("Chưa đăng nhập.");
        }
        Role role = currentUser.getRole();
        if (role != Role.ADMIN && role != Role.BRANCH_MANAGER) {
            throw new ValidationException("Bạn không có quyền quản lý loại sản phẩm.");
        }
    }

    private Boolean parseStatus(String status) {
        String normalizedStatus = normalize(status);
        if (normalizedStatus == null || STATUS_ALL.equals(normalizedStatus)) {
            return null;
        }
        if (STATUS_ACTIVE.equals(normalizedStatus)) {
            return Boolean.TRUE;
        }
        if (STATUS_INACTIVE.equals(normalizedStatus)) {
            return Boolean.FALSE;
        }
        return null;
    }

    private String normalizeCategory(String category) {
        String normalizedCategory = normalize(category);
        if (normalizedCategory == null || STATUS_ALL.equals(normalizedCategory)) {
            return null;
        }
        return normalizedCategory;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
