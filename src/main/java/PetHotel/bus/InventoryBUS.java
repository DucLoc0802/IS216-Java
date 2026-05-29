package PetHotel.bus;

import PetHotel.dao.InventoryDAO;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.model.BranchInventory;
import PetHotel.model.CategoryProduct;
import PetHotel.model.GoodsReceipt;
import PetHotel.model.GoodsReceiptDetail;
import PetHotel.model.InventoryItem;
import PetHotel.model.InventoryStats;
import PetHotel.model.Product;
import PetHotel.util.IDGenerator;
import PetHotel.util.Role;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InventoryBUS {
    private final InventoryDAO inventoryDAO = new InventoryDAO();

    public List<CategoryProduct> getCategories(AppUser currentUser) throws SQLException {
        requireInventoryView(currentUser);
        return inventoryDAO.findCategories();
    }

    public List<String> getBranchIds(AppUser currentUser) throws SQLException {
        requireManager(currentUser, "Bạn không có quyền xem kho.");
        return inventoryDAO.findBranchIds();
    }

    public List<Product> getProducts(AppUser currentUser) throws SQLException {
        requireInventoryView(currentUser);
        return inventoryDAO.findProducts();
    }

    public Product getProductById(String productId, AppUser currentUser) throws SQLException {
        requireInventoryView(currentUser);
        requireText(productId, "Mã vật tư không hợp lệ.");
        return inventoryDAO.findProductById(productId.trim());
    }

    public List<InventoryItem> searchInventory(
            String branchId,
            String keyword,
            String categoryId,
            String status,
            AppUser currentUser
    ) throws SQLException {
        requireInventoryView(currentUser);
        requireBranch(branchId);
        return inventoryDAO.findInventory(branchId, keyword, categoryId, status);
    }

    public List<BranchInventory> searchInventory(
            String branchId,
            String keyword,
            String stockStatus,
            AppUser currentUser
    ) throws SQLException {
        requireManager(currentUser, "Bạn không có quyền xem tồn kho.");
        if (currentUser.getRole() == Role.BRANCH_MANAGER) {
            requireBranch(branchId);
        }
        return inventoryDAO.searchInventory(branchId, keyword, stockStatus);
    }

    public BranchInventory findByBranchAndProduct(String branchId, String productId, AppUser currentUser)
            throws SQLException {
        requireManager(currentUser, "Bạn không có quyền xem tồn kho.");
        requireBranch(branchId);
        requireText(productId, "Mã sản phẩm không hợp lệ.");
        return inventoryDAO.findByBranchAndProduct(branchId.trim(), productId.trim());
    }

    public void upsertInventory(String branchId, String productId, BigDecimal quantityDelta, AppUser currentUser)
            throws SQLException {
        requireManager(currentUser, "Bạn không có quyền cập nhật tồn kho.");
        requireBranch(branchId);
        requireText(productId, "Mã sản phẩm không hợp lệ.");
        if (quantityDelta == null) {
            throw new ValidationException("Số lượng thay đổi không hợp lệ.");
        }
        inventoryDAO.upsertInventory(branchId.trim(), productId.trim(), quantityDelta);
    }

    public void updateInventoryQuantity(String branchId, String productId, BigDecimal actualQuantity, AppUser currentUser)
            throws SQLException {
        requireManager(currentUser, "Bạn không có quyền cập nhật tồn kho.");
        requireBranch(branchId);
        requireText(productId, "Mã sản phẩm không hợp lệ.");
        if (actualQuantity == null || actualQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Số lượng tồn phải lớn hơn hoặc bằng 0.");
        }
        inventoryDAO.updateInventoryQuantity(branchId.trim(), productId.trim(), actualQuantity);
    }

    public void updateReorderPoint(String branchId, String productId, BigDecimal reorderPoint, AppUser currentUser)
            throws SQLException {
        requireManager(currentUser, "Bạn không có quyền cập nhật điểm đặt hàng.");
        requireBranch(branchId);
        requireText(productId, "Mã sản phẩm không hợp lệ.");
        if (reorderPoint != null && reorderPoint.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Điểm đặt hàng lại phải lớn hơn hoặc bằng 0.");
        }
        inventoryDAO.updateReorderPoint(branchId.trim(), productId.trim(), reorderPoint);
    }

    public InventoryItem getInventoryItem(String branchId, String productId, AppUser currentUser)
            throws SQLException {
        requireInventoryView(currentUser);
        requireBranch(branchId);
        requireText(productId, "Mã vật tư không hợp lệ.");
        return inventoryDAO.findInventoryItem(branchId, productId.trim());
    }

    public InventoryStats getStats(String branchId, AppUser currentUser) throws SQLException {
        requireInventoryView(currentUser);
        requireBranch(branchId);

        List<InventoryItem> items = inventoryDAO.findInventory(branchId, null, null, null);
        InventoryStats stats = new InventoryStats();
        stats.setTotalSku(items.size());
        for (InventoryItem item : items) {
            switch (item.getStatus()) {
                case InventoryItem.STATUS_OK -> stats.setOkStock(stats.getOkStock() + 1);
                case InventoryItem.STATUS_LOW -> stats.setLowStock(stats.getLowStock() + 1);
                case InventoryItem.STATUS_CRITICAL, InventoryItem.STATUS_OUT ->
                    stats.setCriticalStock(stats.getCriticalStock() + 1);
                default -> { }
            }
        }
        stats.setMonthImport(inventoryDAO.countApprovedReceiptsInMonth(branchId));
        return stats;
    }

    public List<InventoryItem> getLowStockItems(String branchId, AppUser currentUser) throws SQLException {
        requireManager(currentUser, "Bạn không có quyền xem cảnh báo tồn kho thấp.");
        requireBranch(branchId);
        return inventoryDAO.findLowStockItems(branchId);
    }

    public List<GoodsReceipt> searchReceipts(
            String branchId,
            String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            AppUser currentUser
    ) throws SQLException {
        requireManager(currentUser, "Bạn không có quyền xem lịch sử nhập hàng.");
        requireBranch(branchId);
        validateDateRange(fromDate, toDate);
        return inventoryDAO.findReceipts(branchId, keyword, fromDate, toDate);
    }

    public GoodsReceipt getReceiptById(String receiptId, AppUser currentUser) throws SQLException {
        requireManager(currentUser, "Bạn không có quyền xem phiếu nhập hàng.");
        requireText(receiptId, "Mã phiếu nhập không hợp lệ.");
        return inventoryDAO.findReceiptById(receiptId.trim());
    }

    public GoodsReceipt createReceipt(
            String branchId,
            String supplierName,
            LocalDate receiptDate,
            List<GoodsReceiptDetail> details,
            String note,
            AppUser currentUser
    ) throws SQLException {
        requireManager(currentUser, "Bạn không có quyền nhập hàng.");
        requireBranch(branchId);
        validateReceipt(supplierName, receiptDate, details, note);
        return inventoryDAO.createApprovedReceipt(
            branchId,
            currentUser.getEmployeeId(),
            normalizeSupplierName(supplierName),
            receiptDate,
            normalizeDetails(details),
            normalizeNote(note)
        );
    }

    public void updateReceipt(
            String receiptId,
            String branchId,
            String supplierName,
            LocalDate receiptDate,
            List<GoodsReceiptDetail> details,
            String note,
            AppUser currentUser
    ) throws SQLException {
        requireManager(currentUser, "Bạn không có quyền sửa phiếu nhập hàng.");
        requireText(receiptId, "Mã phiếu nhập không hợp lệ.");
        requireBranch(branchId);
        validateReceipt(supplierName, receiptDate, details, note);
        inventoryDAO.updateApprovedReceipt(
            receiptId.trim(),
            branchId,
            currentUser.getEmployeeId(),
            normalizeSupplierName(supplierName),
            receiptDate,
            normalizeDetails(details),
            normalizeNote(note)
        );
    }

    public void cancelReceipt(String receiptId, String branchId, AppUser currentUser) throws SQLException {
        requireManager(currentUser, "Bạn không có quyền hủy phiếu nhập hàng.");
        requireText(receiptId, "Mã phiếu nhập không hợp lệ.");
        requireBranch(branchId);
        inventoryDAO.cancelReceipt(receiptId.trim(), branchId);
    }

    public void adjustStock(
            String branchId,
            String productId,
            BigDecimal actualQuantity,
            BigDecimal reorderPoint,
            String note,
            AppUser currentUser
    ) throws SQLException {
        requireManager(currentUser, "Bạn không có quyền điều chỉnh tồn kho.");
        requireBranch(branchId);
        requireText(productId, "Vui lòng chọn vật tư cần điều chỉnh.");
        if (actualQuantity == null || actualQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Số lượng thực tế phải lớn hơn hoặc bằng 0.");
        }
        if (reorderPoint != null && reorderPoint.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Ngưỡng tồn tối thiểu phải lớn hơn hoặc bằng 0.");
        }
        if (note != null && note.length() > 4000) {
            throw new ValidationException("Ghi chú không được vượt quá 4000 ký tự.");
        }

        inventoryDAO.adjustStock(
            branchId,
            currentUser.getEmployeeId(),
            productId.trim(),
            actualQuantity,
            reorderPoint,
            normalizeNote(note)
        );
    }

    public void recordMaterialWaste(
            String branchId,
            String productId,
            BigDecimal quantity,
            String reason,
            String note,
            AppUser currentUser
    ) throws SQLException {
        requireWasteRole(currentUser);
        requireBranch(branchId);
        requireText(productId, "Vui lòng chọn vật tư tiêu hao.");
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Số lượng tiêu hao phải lớn hơn 0.");
        }
        requireText(reason, "Vui lòng nhập lý do tiêu hao.");
        if (reason.length() > 4000 || (note != null && note.length() > 4000)) {
            throw new ValidationException("Nội dung ghi chú không được vượt quá 4000 ký tự.");
        }

        inventoryDAO.recordMaterialWaste(
            IDGenerator.nextMaterialWasteId(),
            productId.trim(),
            currentUser.getEmployeeId(),
            branchId,
            quantity,
            reason.trim(),
            normalizeNote(note)
        );
    }

    private void validateReceipt(
            String supplierName,
            LocalDate receiptDate,
            List<GoodsReceiptDetail> details,
            String note
    ) {
        if (supplierName != null && !supplierName.trim().isEmpty() && supplierName.trim().length() > 120) {
            throw new ValidationException("Nguồn nhập không được vượt quá 120 ký tự.");
        }
        if (receiptDate == null) {
            throw new ValidationException("Vui lòng chọn ngày nhập hàng.");
        }
        if (receiptDate.isAfter(LocalDate.now())) {
            throw new ValidationException("Ngày nhập hàng không được lớn hơn ngày hiện tại.");
        }
        if (details == null || details.isEmpty()) {
            throw new ValidationException("Phiếu nhập phải có ít nhất một vật tư.");
        }
        Set<String> productIds = new HashSet<>();
        for (GoodsReceiptDetail detail : details) {
            requireText(detail.getProductId(), "Vui lòng chọn vật tư.");
            if (!productIds.add(detail.getProductId().trim())) {
                throw new ValidationException("Một vật tư không được xuất hiện hai lần trong cùng phiếu nhập.");
            }
            if (detail.getQuantity() == null || detail.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Số lượng nhập phải lớn hơn 0.");
            }
            requireText(detail.getUnit(), "Vui lòng chọn đơn vị tính.");
            String unit = detail.getUnit().trim().toUpperCase();
            if (!Set.of("G", "KG", "ML", "L").contains(unit)) {
                throw new ValidationException("Đơn vị tính chỉ được là G, KG, L hoặc ML.");
            }
        }
        if (note != null && note.length() > 4000) {
            throw new ValidationException("Ghi chú không được vượt quá 4000 ký tự.");
        }
    }

    private List<GoodsReceiptDetail> normalizeDetails(List<GoodsReceiptDetail> details) {
        List<GoodsReceiptDetail> normalized = new ArrayList<>();
        for (GoodsReceiptDetail detail : details) {
            GoodsReceiptDetail copy = new GoodsReceiptDetail();
            copy.setProductId(detail.getProductId().trim());
            copy.setQuantity(detail.getQuantity());
            copy.setUnit(detail.getUnit().trim().toUpperCase());
            copy.setNote(normalizeNote(detail.getNote()));
            normalized.add(copy);
        }
        return normalized;
    }

    private void requireInventoryView(AppUser currentUser) {
        if (currentUser == null) {
            throw new ValidationException("Chưa đăng nhập.");
        }
        Role role = currentUser.getRole();
        if (role != Role.BRANCH_MANAGER
                && role != Role.PET_CARE_STAFF
                && role != Role.ADMIN
                && role != Role.CEO) {
            throw new ValidationException("Bạn không có quyền truy cập kho vật tư.");
        }
    }

    private void requireManager(AppUser currentUser, String message) {
        if (currentUser == null) {
            throw new ValidationException("Chưa đăng nhập.");
        }
        Role role = currentUser.getRole();
        if (role != Role.BRANCH_MANAGER && role != Role.ADMIN) {
            throw new ValidationException(message);
        }
    }

    private void requireWasteRole(AppUser currentUser) {
        if (currentUser == null) {
            throw new ValidationException("Chưa đăng nhập.");
        }
        Role role = currentUser.getRole();
        if (role != Role.PET_CARE_STAFF && role != Role.BRANCH_MANAGER && role != Role.ADMIN) {
            throw new ValidationException("Bạn không có quyền ghi nhận tiêu hao vật tư.");
        }
    }

    private void requireBranch(String branchId) {
        requireText(branchId, "Không xác định được chi nhánh hiện tại.");
    }

    private void requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(message);
        }
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new ValidationException("Ngày bắt đầu không được sau ngày kết thúc.");
        }
    }

    private String normalizeNote(String note) {
        return note == null || note.trim().isEmpty() ? null : note.trim();
    }

    private String normalizeSupplierName(String supplierName) {
        return supplierName == null || supplierName.trim().isEmpty() ? null : supplierName.trim();
    }
}
