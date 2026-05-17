package PetHotel.util;

/**
 * PasswordUtil — Tạm thời xử lý mật khẩu dạng plaintext để khớp với dữ liệu
 * hiện đang lưu trực tiếp trong database.
 *
 * LƯU Ý:
 * - `hashPassword(...)` hiện không băm nữa, chỉ trả về chính mật khẩu gốc.
 * - `checkPassword(...)` so sánh trực tiếp với giá trị trong DB.
 * - Đây là cách phù hợp cho dữ liệu demo hiện tại, không an toàn cho production.
 */
public class PasswordUtil {

    private PasswordUtil() {}

    /**
     * Tạm thời không hash, trả về nguyên giá trị để lưu xuống DB.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống.");
        }
        return plainPassword;
    }

    /**
     * So sánh trực tiếp mật khẩu người dùng nhập với giá trị trong DB.
     */
    public static boolean checkPassword(String plainPassword, String storedPassword) {
        if (plainPassword == null || storedPassword == null) {
            return false;
        }
        return plainPassword.equals(storedPassword);
    }
}
