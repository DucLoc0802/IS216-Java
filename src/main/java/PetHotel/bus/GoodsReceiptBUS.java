package PetHotel.bus;

import PetHotel.dao.GoodsReceiptDAO;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.model.GoodsReceipt;
import PetHotel.model.GoodsReceiptDetail;
import PetHotel.util.Role;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GoodsReceiptBUS {
    private static final Set<String> ALLOWED_UNITS = Set.of("G", "KG", "L", "ML");

    private final GoodsReceiptDAO goodsReceiptDAO = new GoodsReceiptDAO();

    public List<GoodsReceipt> search(String keyword, String status, LocalDate fromDate, LocalDate toDate, AppUser user)
            throws SQLException {
        requireManager(user, "Bạn không có quyền xem phiếu nhập kho.");
        validateDateRange(fromDate, toDate);

        List<GoodsReceipt> receipts = goodsReceiptDAO.search(keyword, status, fromDate, toDate);
        if (user.getRole() == Role.ADMIN) {
            return receipts;
        }

        String branchId = resolveUserBranch(user);
        List<GoodsReceipt> filtered = new ArrayList<>();
        for (GoodsReceipt receipt : receipts) {
            if (branchId.equals(receipt.getBranchId())) {
                filtered.add(receipt);
            }
        }
        return filtered;
    }

    public GoodsReceipt findById(String goodsReceiptId, AppUser user) throws SQLException {
        requireManager(user, "Bạn không có quyền xem phiếu nhập kho.");
        requireText(goodsReceiptId, "Mã phiếu nhập không hợp lệ.");

        GoodsReceipt receipt = goodsReceiptDAO.findById(goodsReceiptId.trim());
        if (receipt != null && user.getRole() != Role.ADMIN && !resolveUserBranch(user).equals(receipt.getBranchId())) {
            throw new ValidationException("Bạn không có quyền xem phiếu nhập của chi nhánh khác.");
        }
        return receipt;
    }

    public void validateReceipt(GoodsReceipt receipt, List<GoodsReceiptDetail> details) {
        if (receipt == null) {
            throw new ValidationException("Dữ liệu phiếu nhập không hợp lệ.");
        }
        requireText(receipt.getBranchId(), "Vui lòng chọn chi nhánh.");
        if (receipt.getSupplierName() != null && receipt.getSupplierName().trim().length() > 120) {
            throw new ValidationException("Nhà cung cấp không được vượt quá 120 ký tự.");
        }
        if (receipt.getReceiptDate() == null) {
            throw new ValidationException("Vui lòng chọn ngày nhập.");
        }
        if (receipt.getReceiptDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Ngày nhập không được lớn hơn ngày hiện tại.");
        }
        if (receipt.getNote() != null && receipt.getNote().length() > 4000) {
            throw new ValidationException("Ghi chú không được vượt quá 4000 ký tự.");
        }
        if (details == null || details.isEmpty()) {
            throw new ValidationException("Phiếu nhập phải có ít nhất một sản phẩm.");
        }

        Set<String> productIds = new HashSet<>();
        for (GoodsReceiptDetail detail : details) {
            requireText(detail.getProductId(), "Vui lòng chọn sản phẩm.");
            if (!productIds.add(detail.getProductId().trim())) {
                throw new ValidationException("Một sản phẩm không được xuất hiện hai lần trong cùng phiếu nhập.");
            }
            if (detail.getQuantity() == null || detail.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Số lượng nhập phải lớn hơn 0.");
            }
            requireText(detail.getUnit(), "Đơn vị tính không được rỗng.");
            String unit = detail.getUnit().trim().toUpperCase();
            if (!ALLOWED_UNITS.contains(unit)) {
                throw new ValidationException("Đơn vị tính chỉ được là G, KG, L hoặc ML.");
            }
            if (detail.getLineTotal() != null && detail.getLineTotal().compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("Thành tiền không được nhỏ hơn 0.");
            }
            if (detail.getNote() != null && detail.getNote().length() > 4000) {
                throw new ValidationException("Ghi chú dòng sản phẩm không được vượt quá 4000 ký tự.");
            }
        }
    }

    public GoodsReceipt createDraftReceipt(GoodsReceipt receipt, List<GoodsReceiptDetail> details, AppUser user)
            throws SQLException {
        requireManager(user, "Bạn không có quyền lập phiếu nhập kho.");
        normalizeReceipt(receipt, details, user);
        validateReceipt(receipt, details);
        goodsReceiptDAO.insertReceipt(receipt, normalizeDetails(details));
        return goodsReceiptDAO.findById(receipt.getGoodsReceiptId());
    }

    public void updateDraftReceipt(GoodsReceipt receipt, List<GoodsReceiptDetail> details, AppUser user)
            throws SQLException {
        requireManager(user, "Bạn không có quyền sửa phiếu nhập kho.");
        requireText(receipt == null ? null : receipt.getGoodsReceiptId(), "Vui lòng chọn phiếu nhập cần sửa.");
        normalizeReceipt(receipt, details, user);
        validateReceipt(receipt, details);
        goodsReceiptDAO.updateReceipt(receipt, normalizeDetails(details));
    }

    public void approveReceipt(String goodsReceiptId, AppUser user) throws SQLException {
        requireManager(user, "Bạn không có quyền duyệt phiếu nhập kho.");
        requireText(goodsReceiptId, "Mã phiếu nhập không hợp lệ.");
        GoodsReceipt receipt = findById(goodsReceiptId, user);
        if (receipt == null) {
            throw new ValidationException("Không tìm thấy phiếu nhập.");
        }
        if (!"DRAFT".equals(receipt.getStatus())) {
            throw new ValidationException("Chỉ được duyệt phiếu nhập DRAFT.");
        }
        goodsReceiptDAO.approveReceipt(goodsReceiptId.trim(), user.getEmployeeId());
    }

    public void cancelReceipt(String goodsReceiptId, AppUser user) throws SQLException {
        requireManager(user, "Bạn không có quyền hủy phiếu nhập kho.");
        requireText(goodsReceiptId, "Mã phiếu nhập không hợp lệ.");
        GoodsReceipt receipt = findById(goodsReceiptId, user);
        if (receipt == null) {
            throw new ValidationException("Không tìm thấy phiếu nhập.");
        }
        if (!"DRAFT".equals(receipt.getStatus())) {
            throw new ValidationException("Chỉ được hủy phiếu nhập DRAFT.");
        }
        goodsReceiptDAO.cancelReceipt(goodsReceiptId.trim());
    }

    private void normalizeReceipt(GoodsReceipt receipt, List<GoodsReceiptDetail> details, AppUser user)
            throws SQLException {
        String branchId = normalize(receipt.getBranchId());
        if (branchId == null) {
            branchId = resolveUserBranch(user);
        }
        if (user.getRole() != Role.ADMIN && !resolveUserBranch(user).equals(branchId)) {
            throw new ValidationException("Bạn không có quyền thao tác phiếu nhập của chi nhánh khác.");
        }
        receipt.setBranchId(branchId);
        receipt.setEmployeeId(user.getEmployeeId());
        receipt.setSupplierName(normalize(receipt.getSupplierName()));
        receipt.setNote(normalize(receipt.getNote()));
        if (receipt.getReceiptDate() == null) {
            receipt.setReceiptDate(LocalDate.now());
        }
        if (normalize(receipt.getGoodsReceiptId()) == null) {
            receipt.setGoodsReceiptId(goodsReceiptDAO.generateNextGoodsReceiptId());
        }
        for (GoodsReceiptDetail detail : details) {
            detail.setProductId(normalize(detail.getProductId()));
            String unit = normalize(detail.getUnit());
            detail.setUnit(unit == null ? null : unit.toUpperCase());
            detail.setNote(normalize(detail.getNote()));
        }
    }

    private List<GoodsReceiptDetail> normalizeDetails(List<GoodsReceiptDetail> details) {
        List<GoodsReceiptDetail> normalized = new ArrayList<>();
        for (GoodsReceiptDetail detail : details) {
            GoodsReceiptDetail copy = new GoodsReceiptDetail();
            copy.setProductId(detail.getProductId().trim());
            copy.setQuantity(detail.getQuantity());
            copy.setUnit(detail.getUnit().trim().toUpperCase());
            copy.setLineTotal(detail.getLineTotal());
            copy.setNote(normalize(detail.getNote()));
            normalized.add(copy);
        }
        return normalized;
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new ValidationException("Ngày bắt đầu không được sau ngày kết thúc.");
        }
    }

    private void requireManager(AppUser user, String message) {
        if (user == null) {
            throw new ValidationException("Chưa đăng nhập.");
        }
        Role role = user.getRole();
        if (role != Role.ADMIN && role != Role.BRANCH_MANAGER) {
            throw new ValidationException(message);
        }
    }

    private String resolveUserBranch(AppUser user) {
        if (user != null && user.getEmployee() != null && normalize(user.getEmployee().getBranchId()) != null) {
            return user.getEmployee().getBranchId().trim();
        }
        throw new ValidationException("Không xác định được chi nhánh hiện tại.");
    }

    private void requireText(String value, String message) {
        if (normalize(value) == null) {
            throw new ValidationException(message);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
