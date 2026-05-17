package PetHotel.bus;

import PetHotel.dao.CustomerDAO;
import PetHotel.dao.PetDAO;
import PetHotel.dao.PetHealthRecordDAO;
import PetHotel.exception.*;
import PetHotel.model.Pet;
import PetHotel.model.PetHealthRecord;
import PetHotel.util.IDGenerator;
import PetHotel.util.Role;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * PetBUS — Nghiệp vụ Quản Lý Thú Cưng.
 *
 * ═══════════════════════════════════════════════════════════════════
 * PHÂN QUYỀN:
 *   createPet           → RECEPTIONIST (1) trở lên
 *   updatePet           → RECEPTIONIST (1) trở lên
 *   deletePet           → BRANCH_MANAGER (4) trở lên
 *   getPetDetail        → RECEPTIONIST (1) trở lên
 *   searchPet           → RECEPTIONIST (1) trở lên
 *   getPetServiceHistory→ RECEPTIONIST (1) trở lên
 *   addHealthRecord     → PET_CARE_STAFF (2) trở lên
 *   getHealthRecords    → RECEPTIONIST (1) trở lên
 * ═══════════════════════════════════════════════════════════════════
 *
 * Ràng buộc nghiệp vụ khi xóa:
 *   - Không xóa nếu còn pet_health_record liên kết.
 *   - Không xóa nếu còn booking_room_pet liên kết (đã hoặc đang ở khách sạn).
 *
 * TODO: Thêm audit log khi tạo/sửa/xóa/ghi nhận sức khoẻ.
 * TODO: Thêm cache getHealthRecords.
 * TODO: Thêm pagination cho searchPet.
 */
public class PetBUS {

    /** Các loài hợp lệ được hệ thống hỗ trợ */
    private static final Set<String> VALID_SPECIES =
        new HashSet<>(Arrays.asList("DOG", "CAT", "RABBIT", "BIRD", "HAMSTER", "OTHER"));

    /** Các giá trị giới tính hợp lệ theo schema: CHECK (sex IN ('Male','Female')) */
    private static final Set<String> VALID_SEX =
        new HashSet<>(Arrays.asList("Male", "Female"));

    private final PetDAO             petDAO;
    private final CustomerDAO        customerDAO;
    private final PetHealthRecordDAO healthRecordDAO;
    private final AuthBUS            authBUS;

    public PetBUS(AuthBUS authBUS) {
        this.petDAO          = new PetDAO();
        this.customerDAO     = new CustomerDAO();
        this.healthRecordDAO = new PetHealthRecordDAO();
        this.authBUS         = authBUS;
    }

    /** Constructor cho dependency injection / unit test */
    public PetBUS(PetDAO petDAO, CustomerDAO customerDAO,
                  PetHealthRecordDAO healthRecordDAO, AuthBUS authBUS) {
        this.petDAO          = petDAO;
        this.customerDAO     = customerDAO;
        this.healthRecordDAO = healthRecordDAO;
        this.authBUS         = authBUS;
    }

    // ─────────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────────

    /**
     * Thêm mới thú cưng cho một khách hàng.
     *
     * Quyền: RECEPTIONIST trở lên.
     *
     * Nghiệp vụ:
     *   1. Kiểm tra quyền.
     *   2. Validate dữ liệu.
     *   3. Kiểm tra khách hàng tồn tại.
     *   4. Sinh ID mới.
     *   5. Insert vào DB.
     *
     * @param customerId  mã khách hàng sở hữu (bắt buộc)
     * @param petName     tên thú cưng (bắt buộc, <= 20 ký tự)
     * @param species     loài (bắt buộc)
     * @param breed       giống (có thể null)
     * @param sex         giới tính: "Male" hoặc "Female" (có thể null)
     * @param weightKg    cân nặng kg (có thể null, phải > 0 nếu có)
     * @param specialNote ghi chú đặc biệt (có thể null)
     * @return Pet vừa tạo (có petId)
     * @throws ValidationException    nếu dữ liệu không hợp lệ
     * @throws NotFoundException      nếu khách hàng không tồn tại
     * @throws AuthorizationException nếu không đủ quyền
     */
    public Pet createPet(String customerId, String petName, String species,
                         String breed, String sex, Double weightKg, String specialNote) {
        // 1. Quyền
        authBUS.requireRole(Role.RECEPTIONIST);

        // 2. Validate
        validatePetName(petName);
        validateSpecies(species);
        validateSex(sex);
        validateWeightKg(weightKg);

        try {
            // 3. Kiểm tra khách hàng tồn tại
            if (customerDAO.findById(customerId) == null) {
                throw new NotFoundException(
                    "Không tìm thấy khách hàng với ID: " + customerId +
                    ". Vui lòng tạo khách hàng trước khi thêm thú cưng.");
            }

            // 4. Sinh ID
            String newId = IDGenerator.nextPetId();

            // 5. Insert
            Pet pet = new Pet(
                newId,
                customerId,
                petName.trim(),
                species.trim().toUpperCase(),
                breed  != null ? breed.trim()  : null,
                sex    != null ? normalizeSex(sex) : null,
                weightKg,
                specialNote
            );
            petDAO.insert(pet, null);
            return pet;

        } catch (NotFoundException | ValidationException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi thêm thú cưng.", e);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────

    /**
     * Cập nhật thông tin thú cưng.
     *
     * Quyền: RECEPTIONIST trở lên.
     *
     * LƯU Ý: Không cho phép đổi customer_id qua method này.
     *         Nếu cần chuyển thú cưng sang chủ khác → cần method riêng với quyền cao hơn.
     *
     * @param petId       mã thú cưng cần cập nhật
     * @param petName     tên mới
     * @param species     loài mới
     * @param breed       giống mới (có thể null)
     * @param sex         giới tính mới (có thể null)
     * @param weightKg    cân nặng mới (có thể null)
     * @param specialNote ghi chú mới (có thể null)
     * @return Pet sau khi cập nhật
     * @throws NotFoundException   nếu không tìm thấy
     * @throws ValidationException nếu dữ liệu không hợp lệ
     */
    public Pet updatePet(String petId, String petName, String species,
                         String breed, String sex, Double weightKg, String specialNote) {
        // 1. Quyền
        authBUS.requireRole(Role.RECEPTIONIST);

        // 2. Validate
        validatePetName(petName);
        validateSpecies(species);
        validateSex(sex);
        validateWeightKg(weightKg);

        try {
            // 3. Kiểm tra tồn tại
            Pet existing = petDAO.findById(petId);
            if (existing == null) {
                throw new NotFoundException("Không tìm thấy thú cưng với ID: " + petId);
            }

            // 4. Cập nhật các trường
            existing.setPetName(petName.trim());
            existing.setSpecies(species.trim().toUpperCase());
            existing.setBreed(breed != null ? breed.trim() : null);
            existing.setSex(sex != null ? normalizeSex(sex) : null);
            existing.setWeightKg(weightKg);
            existing.setSpecialNote(specialNote);

            int rows = petDAO.update(existing, null);
            if (rows == 0) {
                throw new NotFoundException("Cập nhật thất bại: Không tìm thấy pet ID: " + petId);
            }
            return existing;

        } catch (NotFoundException | ValidationException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi cập nhật thú cưng.", e);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────────

    /**
     * Xóa thú cưng.
     *
     * Quyền: BRANCH_MANAGER (4) trở lên.
     *
     * Ràng buộc:
     *   - Không xóa nếu còn health_record.
     *   - Không xóa nếu đã từng được gán vào phòng (booking_room_pet).
     *
     * @param petId mã thú cưng
     * @throws NotFoundException  nếu không tìm thấy
     * @throws BusinessException  nếu vi phạm ràng buộc
     */
    public void deletePet(String petId) {
        // 1. Quyền
        authBUS.requireRole(Role.BRANCH_MANAGER);

        try {
            // 2. Kiểm tra tồn tại
            Pet existing = petDAO.findById(petId);
            if (existing == null) {
                throw new NotFoundException("Không tìm thấy thú cưng với ID: " + petId);
            }

            // 3. Ràng buộc: còn health record
            int healthCount = petDAO.countHealthRecords(petId);
            if (healthCount > 0) {
                throw new BusinessException(
                    "Không thể xóa thú cưng '" + existing.getPetName() +
                    "': còn " + healthCount + " hồ sơ sức khoẻ liên quan.");
            }

            // 4. Ràng buộc: còn booking_room_pet
            int bookingCount = petDAO.countBookingRoomPets(petId);
            if (bookingCount > 0) {
                throw new BusinessException(
                    "Không thể xóa thú cưng '" + existing.getPetName() +
                    "': đã từng lưu trú " + bookingCount + " lần trong hệ thống. " +
                    "Không thể xóa dữ liệu lịch sử.");
            }

            // 5. Xóa
            petDAO.delete(petId, null);

        } catch (NotFoundException | BusinessException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi xóa thú cưng.", e);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // QUERY
    // ─────────────────────────────────────────────────────────────

    /**
     * Lấy chi tiết thú cưng theo ID.
     *
     * Quyền: RECEPTIONIST trở lên.
     *
     * @param petId mã thú cưng
     * @return Pet
     * @throws NotFoundException nếu không tồn tại
     */
    public Pet getPetDetail(String petId) {
        authBUS.requireRole(Role.RECEPTIONIST);

        try {
            Pet p = petDAO.findById(petId);
            if (p == null) {
                throw new NotFoundException("Không tìm thấy thú cưng với ID: " + petId);
            }
            return p;
        } catch (NotFoundException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi lấy thông tin thú cưng.", e);
        }
    }

    /**
     * Tìm kiếm thú cưng theo từ khóa (tên, loài, giống, tên chủ).
     *
     * Quyền: RECEPTIONIST trở lên.
     * TODO: Thêm filter loài, trạng thái sức khoẻ.
     * TODO: Thêm pagination.
     *
     * @param keyword từ khóa (>= 2 ký tự)
     * @return List<Pet>
     */
    public List<Pet> searchPet(String keyword) {
        authBUS.requireRole(Role.RECEPTIONIST);

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ValidationException("Từ khóa tìm kiếm không được để trống.");
        }
        if (keyword.trim().length() < 2) {
            throw new ValidationException("Từ khóa phải có ít nhất 2 ký tự.");
        }

        try {
            return petDAO.search(keyword.trim());
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi tìm kiếm thú cưng.", e);
        }
    }

    /**
     * Lấy tất cả thú cưng của một khách hàng.
     *
     * Quyền: RECEPTIONIST trở lên.
     *
     * @param customerId mã khách hàng
     * @return List<Pet>
     */
    public List<Pet> getPetsByCustomer(String customerId) {
        authBUS.requireRole(Role.RECEPTIONIST);

        try {
            if (customerDAO.findById(customerId) == null) {
                throw new NotFoundException("Không tìm thấy khách hàng ID: " + customerId);
            }
            return petDAO.findByCustomerId(customerId);
        } catch (NotFoundException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi lấy danh sách thú cưng.", e);
        }
    }

    /**
     * Lấy lịch sử dịch vụ của thú cưng.
     *
     * Quyền: RECEPTIONIST trở lên.
     * TODO: Trả về List<PetServiceHistoryDTO> thay vì List<Object[]>.
     *
     * @param petId mã thú cưng
     * @return danh sách lịch sử
     */
    public List<Object[]> getPetServiceHistory(String petId) {
        authBUS.requireRole(Role.RECEPTIONIST);

        try {
            if (petDAO.findById(petId) == null) {
                throw new NotFoundException("Không tìm thấy thú cưng ID: " + petId);
            }
            return petDAO.getPetServiceHistory(petId);
        } catch (NotFoundException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi lấy lịch sử dịch vụ.", e);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // HEALTH RECORD
    // ─────────────────────────────────────────────────────────────

    /**
     * Ghi nhận tình trạng sức khoẻ của thú cưng trong một booking.
     *
     * Quyền: PET_CARE_STAFF (2) trở lên.
     *
     * Nghiệp vụ:
     *   1. Kiểm tra quyền.
     *   2. Validate: pet tồn tại, note không rỗng, status hợp lệ.
     *   3. Kiểm tra bookingId hợp lệ (pet phải thuộc booking đó).
     *      TODO: Thêm kiểm tra pet có trong booking_room_pet của bookingId đó.
     *   4. Insert health record.
     *
     * @param petId     mã thú cưng
     * @param bookingId mã booking liên quan (bắt buộc theo schema)
     * @param note      nội dung ghi nhận sức khoẻ (bắt buộc)
     * @param status    0 = có vấn đề, 1 = bình thường
     * @return PetHealthRecord vừa tạo
     * @throws NotFoundException   nếu pet không tồn tại
     * @throws ValidationException nếu dữ liệu không hợp lệ
     */
    public PetHealthRecord addHealthRecord(String petId, String bookingId,
                                           String note, int status) {
        // 1. Quyền: PET_CARE_STAFF trở lên
        authBUS.requireRole(Role.PET_CARE_STAFF);

        // 2. Validate
        if (note == null || note.trim().isEmpty()) {
            throw new ValidationException("Nội dung ghi nhận sức khoẻ không được để trống.");
        }
        if (status != 0 && status != 1) {
            throw new ValidationException("Trạng thái sức khoẻ phải là 0 (có vấn đề) hoặc 1 (bình thường).");
        }
        if (bookingId == null || bookingId.trim().isEmpty()) {
            throw new ValidationException("Booking ID không được để trống.");
        }

        try {
            // Kiểm tra pet tồn tại
            if (petDAO.findById(petId) == null) {
                throw new NotFoundException("Không tìm thấy thú cưng ID: " + petId);
            }

            // TODO: Kiểm tra bookingId tồn tại và pet thuộc booking đó
            //       (cần BookingDAO.findById + BookingRoomPetDAO.existsByBookingAndPet)

            // 4. Tạo và insert
            String newId = IDGenerator.nextHealthRecordId();
            PetHealthRecord record = new PetHealthRecord(
                newId, petId, bookingId, note.trim(), status
            );
            healthRecordDAO.insert(record, null);
            return record;

        } catch (NotFoundException | ValidationException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi ghi nhận sức khoẻ.", e);
        }
    }

    /**
     * Lấy danh sách hồ sơ sức khoẻ của thú cưng, mới nhất trước.
     *
     * Quyền: RECEPTIONIST trở lên.
     *
     * @param petId mã thú cưng
     * @return List<PetHealthRecord>
     */
    public List<PetHealthRecord> getHealthRecords(String petId) {
        authBUS.requireRole(Role.RECEPTIONIST);

        try {
            if (petDAO.findById(petId) == null) {
                throw new NotFoundException("Không tìm thấy thú cưng ID: " + petId);
            }
            return healthRecordDAO.findByPetId(petId);
        } catch (NotFoundException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi lấy hồ sơ sức khoẻ.", e);
        }
    }

    /**
     * Lấy hồ sơ sức khoẻ mới nhất của thú cưng.
     *
     * Quyền: RECEPTIONIST trở lên.
     *
     * @param petId mã thú cưng
     * @return PetHealthRecord mới nhất, hoặc null nếu chưa có hồ sơ
     */
    public PetHealthRecord getLatestHealthRecord(String petId) {
        authBUS.requireRole(Role.RECEPTIONIST);

        try {
            if (petDAO.findById(petId) == null) {
                throw new NotFoundException("Không tìm thấy thú cưng ID: " + petId);
            }
            return healthRecordDAO.findLatestByPetId(petId);
        } catch (NotFoundException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi lấy hồ sơ sức khoẻ mới nhất.", e);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // VALIDATION HELPERS
    // ─────────────────────────────────────────────────────────────

    /**
     * Validate tên thú cưng.
     * Schema: pet_name NVARCHAR2(20) NOT NULL.
     */
    private void validatePetName(String petName) {
        if (petName == null || petName.trim().isEmpty()) {
            throw new ValidationException("Tên thú cưng không được để trống.");
        }
        if (petName.trim().length() > 20) {
            throw new ValidationException("Tên thú cưng không được vượt quá 20 ký tự.");
        }
    }

    /**
     * Validate loài.
     * Schema: species NVARCHAR2(30) NOT NULL.
     */
    private void validateSpecies(String species) {
        if (species == null || species.trim().isEmpty()) {
            throw new ValidationException("Loài thú cưng không được để trống.");
        }
        // TODO: Mở rộng VALID_SPECIES nếu hệ thống hỗ trợ thêm loài
        String upper = species.trim().toUpperCase();
        if (!VALID_SPECIES.contains(upper)) {
            throw new ValidationException(
                "Loài '" + species + "' không hợp lệ. Các loài được hỗ trợ: " + VALID_SPECIES);
        }
    }

    /**
     * Validate giới tính.
     * Schema: sex IN ('Male', 'Female') — nullable.
     */
    private void validateSex(String sex) {
        if (sex == null || sex.trim().isEmpty()) return; // nullable
        if (!VALID_SEX.contains(normalizeSex(sex))) {
            throw new ValidationException(
                "Giới tính '" + sex + "' không hợp lệ. Chỉ chấp nhận 'Male' hoặc 'Female'.");
        }
    }

    /**
     * Validate cân nặng.
     * Schema: weight_kg CHECK > 0 (nullable).
     */
    private void validateWeightKg(Double weightKg) {
        if (weightKg == null) return; // nullable
        if (weightKg <= 0) {
            throw new ValidationException("Cân nặng phải lớn hơn 0 kg.");
        }
        if (weightKg > 200) {
            throw new ValidationException("Cân nặng không thể vượt quá 200 kg.");
        }
    }

    /**
     * Chuẩn hoá giới tính về đúng format schema: "Male" / "Female".
     */
    private String normalizeSex(String sex) {
        if (sex == null) return null;
        String s = sex.trim().toLowerCase();
        if ("male".equals(s) || "đực".equals(s) || "m".equals(s)) return "Male";
        if ("female".equals(s) || "cái".equals(s) || "f".equals(s)) return "Female";
        return sex.trim(); // giữ nguyên để validator bắt lỗi
    }
}
