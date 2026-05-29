package PetHotel.bus;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import PetHotel.dao.ServiceProductStandardDAO;
import PetHotel.exception.ValidationException;
import PetHotel.model.AppUser;
import PetHotel.model.ServiceProductStandard;
import PetHotel.util.Role;

public class ServiceProductStandardBUS {

    private final ServiceProductStandardDAO dao = new ServiceProductStandardDAO();

    public List<ServiceProductStandard> getByService(String serviceId, AppUser currentUser)
            throws SQLException, ValidationException {

        checkPermission(currentUser);

        if (serviceId == null || serviceId.trim().isEmpty()) {
            throw new ValidationException("Mã dịch vụ không hợp lệ.");
        }

        return dao.findByServiceId(serviceId);
    }

    public void create(ServiceProductStandard sps, AppUser currentUser)
            throws SQLException, ValidationException {

        checkPermission(currentUser);
        validate(sps);
        dao.insert(sps);
    }

    public void update(ServiceProductStandard sps, AppUser currentUser)
            throws SQLException, ValidationException {

        checkPermission(currentUser);

        if (sps.getServiceProductStandardId() == null || sps.getServiceProductStandardId().trim().isEmpty()) {
            throw new ValidationException("Mã định mức không hợp lệ.");
        }

        validate(sps);
        dao.update(sps);
    }

    public void delete(String id, AppUser currentUser)
            throws SQLException, ValidationException {

        checkPermission(currentUser);

        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("Mã định mức không hợp lệ.");
        }

        dao.delete(id);
    }

    private void validate(ServiceProductStandard sps) throws ValidationException {
        if (sps.getServiceId() == null || sps.getServiceId().trim().isEmpty()) {
            throw new ValidationException("Mã dịch vụ không hợp lệ.");
        }

        if (sps.getProductId() == null || sps.getProductId().trim().isEmpty()) {
            throw new ValidationException("Vui lòng chọn sản phẩm.");
        }

        if (!"DOG".equals(sps.getSpecies()) && !"CAT".equals(sps.getSpecies())) {
            throw new ValidationException("Loài chỉ được là DOG hoặc CAT.");
        }

        if (sps.getMinWeightKg() == null || sps.getMinWeightKg().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Cân nặng tối thiểu phải >= 0.");
        }

        if (sps.getMaxWeightKg() == null || sps.getMaxWeightKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Cân nặng tối đa phải > 0.");
        }

        if (sps.getMaxWeightKg().compareTo(sps.getMinWeightKg()) <= 0) {
            throw new ValidationException("Cân nặng tối đa phải lớn hơn cân nặng tối thiểu.");
        }

        if (sps.getUsageAmount() == null || sps.getUsageAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Số lượng sử dụng phải > 0.");
        }

        String unit = sps.getUsageUnit();
        if (!"ML".equals(unit) && !"L".equals(unit) && !"G".equals(unit) && !"KG".equals(unit)) {
            throw new ValidationException("Đơn vị chỉ được là G, KG, L hoặc ML.");
        }
    }

    private void checkPermission(AppUser currentUser) throws ValidationException {
        if (currentUser == null) {
            throw new ValidationException("Chưa đăng nhập.");
        }

        if (!currentUser.hasRole(Role.BRANCH_MANAGER) && !currentUser.hasRole(Role.ADMIN)) {
            throw new ValidationException("Bạn không có quyền cấu hình vật tư tiêu hao.");
        }
    }
}
