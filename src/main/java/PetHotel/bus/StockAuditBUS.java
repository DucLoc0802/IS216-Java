package PetHotel.bus;

import PetHotel.dao.StockAuditDAO;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.model.StockAudit;
import PetHotel.model.StockAuditDetail;
import PetHotel.util.Role;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class StockAuditBUS {
    private final StockAuditDAO stockAuditDAO = new StockAuditDAO();

    public List<StockAudit> search(String branchId, String status, LocalDate fromDate, LocalDate toDate, AppUser user)
            throws SQLException {
        requireManager(user, "Bạn không có quyền xem kiểm kê kho.");
        validateDateRange(fromDate, toDate);

        String effectiveBranchId = normalize(branchId);
        if (user.getRole() == Role.BRANCH_MANAGER) {
            effectiveBranchId = resolveUserBranch(user);
        }
        return stockAuditDAO.search(effectiveBranchId, status, fromDate, toDate);
    }

    public StockAudit findById(String stockAuditId, AppUser user) throws SQLException {
        requireManager(user, "Bạn không có quyền xem kiểm kê kho.");
        requireText(stockAuditId, "Mã kiểm kê không hợp lệ.");

        StockAudit audit = stockAuditDAO.findById(stockAuditId.trim());
        if (audit != null && user.getRole() != Role.ADMIN && !resolveUserBranch(user).equals(audit.getBranchId())) {
            throw new ValidationException("Bạn không có quyền xem phiếu kiểm kê của chi nhánh khác.");
        }
        return audit;
    }

    public void createAudit(String branchId, String note, AppUser user) throws SQLException {
        requireManager(user, "Bạn không có quyền tạo phiếu kiểm kê.");
        String effectiveBranchId = normalize(branchId);
        if (effectiveBranchId == null) {
            effectiveBranchId = resolveUserBranch(user);
        }
        if (user.getRole() != Role.ADMIN && !resolveUserBranch(user).equals(effectiveBranchId)) {
            throw new ValidationException("Bạn không có quyền kiểm kê chi nhánh khác.");
        }
        if (note != null && note.length() > 4000) {
            throw new ValidationException("Ghi chú không được vượt quá 4000 ký tự.");
        }
        stockAuditDAO.createAuditFromInventory(effectiveBranchId, user.getEmployeeId(), normalize(note));
    }

    public void updateDraftAudit(StockAudit audit, List<StockAuditDetail> details, AppUser user)
            throws SQLException {
        requireManager(user, "Bạn không có quyền sửa phiếu kiểm kê.");
        validateAudit(audit, details);

        StockAudit current = findById(audit.getStockAuditId(), user);
        if (current == null) {
            throw new ValidationException("Không tìm thấy phiếu kiểm kê.");
        }
        if (!"DRAFT".equals(current.getStatus())) {
            throw new ValidationException("Chỉ được sửa phiếu kiểm kê DRAFT.");
        }

        audit.setNote(normalize(audit.getNote()));
        stockAuditDAO.updateDraftAudit(audit, details);
    }

    public void completeAudit(String stockAuditId, AppUser user) throws SQLException {
        requireManager(user, "Bạn không có quyền hoàn tất kiểm kê.");
        StockAudit audit = findById(stockAuditId, user);
        if (audit == null) {
            throw new ValidationException("Không tìm thấy phiếu kiểm kê.");
        }
        if (!"DRAFT".equals(audit.getStatus())) {
            throw new ValidationException("Chỉ được hoàn tất phiếu kiểm kê DRAFT.");
        }
        stockAuditDAO.completeAudit(stockAuditId.trim());
    }

    public void cancelAudit(String stockAuditId, AppUser user) throws SQLException {
        requireManager(user, "Bạn không có quyền hủy phiếu kiểm kê.");
        StockAudit audit = findById(stockAuditId, user);
        if (audit == null) {
            throw new ValidationException("Không tìm thấy phiếu kiểm kê.");
        }
        if (!"DRAFT".equals(audit.getStatus())) {
            throw new ValidationException("Chỉ được hủy phiếu kiểm kê DRAFT.");
        }
        stockAuditDAO.cancelAudit(stockAuditId.trim());
    }

    public void validateAudit(StockAudit audit, List<StockAuditDetail> details) {
        if (audit == null) {
            throw new ValidationException("Dữ liệu phiếu kiểm kê không hợp lệ.");
        }
        requireText(audit.getStockAuditId(), "Mã kiểm kê không hợp lệ.");
        if (audit.getNote() != null && audit.getNote().length() > 4000) {
            throw new ValidationException("Ghi chú không được vượt quá 4000 ký tự.");
        }
        if (details == null || details.isEmpty()) {
            throw new ValidationException("Phiếu kiểm kê phải có ít nhất một sản phẩm.");
        }
        for (StockAuditDetail detail : details) {
            requireText(detail.getProductId(), "Dòng kiểm kê thiếu mã sản phẩm.");
            if (detail.getActualQuantity() == null || detail.getActualQuantity().compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("Số lượng thực tế phải lớn hơn hoặc bằng 0.");
            }
            if (detail.getNote() != null && detail.getNote().length() > 4000) {
                throw new ValidationException("Ghi chú dòng kiểm kê không được vượt quá 4000 ký tự.");
            }
            detail.recalculateDifference();
        }
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
