package PetHotel.bus;

import PetHotel.dao.MaterialWasteDAO;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.model.MaterialWaste;
import PetHotel.util.IDGenerator;
import PetHotel.util.Role;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class MaterialWasteBUS {
    private final MaterialWasteDAO materialWasteDAO = new MaterialWasteDAO();

    public List<MaterialWaste> getWastes(String keyword, String status, AppUser currentUser) throws SQLException {
        requireWasteAccess(currentUser);
        String normalizedStatus = normalizeStatus(status);
        String branchId = null;
        String employeeId = null;

        if (currentUser.getRole() == Role.PET_CARE_STAFF) {
            branchId = resolveUserBranch(currentUser);
            employeeId = currentUser.getEmployeeId();
        } else if (currentUser.getRole() == Role.BRANCH_MANAGER) {
            branchId = resolveUserBranch(currentUser);
        }

        return materialWasteDAO.search(keyword, normalizedStatus, branchId, employeeId);
    }

    public void createWasteRequest(String branchId, String productId, BigDecimal quantity,
                                   String reason, String note, AppUser currentUser)
            throws SQLException {
        requireCreator(currentUser);
        String resolvedBranchId = normalize(branchId);
        if (resolvedBranchId == null) {
            resolvedBranchId = resolveUserBranch(currentUser);
        }
        if (currentUser.getRole() == Role.PET_CARE_STAFF
                && !resolvedBranchId.equals(resolveUserBranch(currentUser))) {
            throw new ValidationException("Bạn không có quyền lập phiếu hao hụt cho chi nhánh khác.");
        }

        validateWaste(productId, quantity, reason, note);

        MaterialWaste waste = new MaterialWaste();
        waste.setMaterialWasteId(IDGenerator.nextMaterialWasteId());
        waste.setProductId(productId.trim());
        waste.setEmployeeId(currentUser.getEmployeeId());
        waste.setBranchId(resolvedBranchId);
        waste.setWasteQuantity(quantity);
        waste.setReason(reason.trim());
        waste.setNote(normalize(note));

        materialWasteDAO.insertPending(waste);
    }

    public void approveWaste(String materialWasteId, String managerNote, AppUser currentUser)
            throws SQLException {
        requireManager(currentUser);
        MaterialWaste waste = getActionableWaste(materialWasteId, currentUser);
        if (!MaterialWaste.STATUS_PENDING.equals(waste.getStatus())) {
            throw new ValidationException("Chỉ duyệt được phiếu đang chờ duyệt.");
        }
        materialWasteDAO.approve(waste.getMaterialWasteId(), managerNote);
    }

    public void rejectWaste(String materialWasteId, String managerNote, AppUser currentUser)
            throws SQLException {
        requireManager(currentUser);
        MaterialWaste waste = getActionableWaste(materialWasteId, currentUser);
        if (!MaterialWaste.STATUS_PENDING.equals(waste.getStatus())) {
            throw new ValidationException("Chỉ hủy được phiếu đang chờ duyệt.");
        }
        materialWasteDAO.reject(waste.getMaterialWasteId(), managerNote);
    }

    private MaterialWaste getActionableWaste(String materialWasteId, AppUser currentUser) throws SQLException {
        String id = normalize(materialWasteId);
        if (id == null) {
            throw new ValidationException("Vui lòng chọn phiếu hao hụt.");
        }
        MaterialWaste waste = materialWasteDAO.findById(id);
        if (waste == null) {
            throw new ValidationException("Không tìm thấy phiếu hao hụt.");
        }
        if (currentUser.getRole() == Role.BRANCH_MANAGER
                && !resolveUserBranch(currentUser).equals(waste.getBranchId())) {
            throw new ValidationException("Bạn không có quyền xử lý phiếu hao hụt của chi nhánh khác.");
        }
        return waste;
    }

    private void validateWaste(String productId, BigDecimal quantity, String reason, String note) {
        if (normalize(productId) == null) {
            throw new ValidationException("Vui lòng chọn sản phẩm/vật tư hao hụt.");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Số lượng hao hụt phải lớn hơn 0.");
        }
        if (normalize(reason) == null) {
            throw new ValidationException("Lý do hao hụt là bắt buộc.");
        }
        if (reason.trim().length() > 4000 || (note != null && note.length() > 4000)) {
            throw new ValidationException("Nội dung ghi chú không được vượt quá 4000 ký tự.");
        }
    }

    private String normalizeStatus(String status) {
        String normalized = normalize(status);
        if (normalized == null || MaterialWaste.STATUS_ALL.equals(normalized)) {
            return null;
        }
        if ("Chờ duyệt".equals(normalized)) {
            return MaterialWaste.STATUS_PENDING;
        }
        if ("Đã duyệt".equals(normalized)) {
            return MaterialWaste.STATUS_APPROVED;
        }
        if ("Đã hủy".equals(normalized)) {
            return MaterialWaste.STATUS_REJECTED;
        }
        if (MaterialWaste.STATUS_PENDING.equals(normalized)
                || MaterialWaste.STATUS_APPROVED.equals(normalized)
                || MaterialWaste.STATUS_REJECTED.equals(normalized)) {
            return normalized;
        }
        return null;
    }

    private void requireWasteAccess(AppUser currentUser) {
        if (currentUser == null) {
            throw new ValidationException("Chưa đăng nhập.");
        }
        Role role = currentUser.getRole();
        if (role != Role.PET_CARE_STAFF && role != Role.BRANCH_MANAGER && role != Role.ADMIN) {
            throw new ValidationException("Bạn không có quyền truy cập ghi nhận hao hụt vật liệu.");
        }
    }

    private void requireCreator(AppUser currentUser) {
        requireWasteAccess(currentUser);
        Role role = currentUser.getRole();
        if (role != Role.PET_CARE_STAFF && role != Role.BRANCH_MANAGER) {
            throw new ValidationException("Bạn không có quyền lập phiếu hao hụt vật liệu.");
        }
    }

    private void requireManager(AppUser currentUser) {
        if (currentUser == null) {
            throw new ValidationException("Chưa đăng nhập.");
        }
        Role role = currentUser.getRole();
        if (role != Role.BRANCH_MANAGER && role != Role.ADMIN) {
            throw new ValidationException("Chỉ quản lý chi nhánh được duyệt hoặc hủy phiếu hao hụt.");
        }
    }

    private String resolveUserBranch(AppUser currentUser) {
        if (currentUser != null && currentUser.getEmployee() != null) {
            String branchId = normalize(currentUser.getEmployee().getBranchId());
            if (branchId != null) {
                return branchId;
            }
        }
        throw new ValidationException("Không xác định được chi nhánh hiện tại.");
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
