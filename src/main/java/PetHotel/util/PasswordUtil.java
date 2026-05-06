package PetHotel.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * PasswordUtil — Công cụ mã hóa và kiểm tra mật khẩu.
 * 
 * Sử dụng thuật toán PBKDF2 (Password-Based Key Derivation Function 2)
 * tích hợp sẵn trong Java. An toàn cao, tự động sinh Salt ngẫu nhiên.
 */
public class PasswordUtil {

    // Số vòng lặp băm (Càng cao càng bảo mật nhưng tính toán chậm hơn. 65536 là chuẩn an toàn hiện tại)
    private static final int ITERATIONS = 65536; 
    
    // Độ dài khóa sinh ra (bit)
    private static final int KEY_LENGTH = 256;   
    
    // Thuật toán băm
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    /**
     * Mã hóa mật khẩu gốc thành chuỗi Hash.
     * (Sử dụng khi: Tạo tài khoản mới, hoặc Đổi mật khẩu)
     * 
     * @param plainPassword Mật khẩu người dùng nhập (VD: "123456")
     * @return Chuỗi đã mã hóa định dạng "salt:hash" để lưu xuống Database
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống.");
        }

        try {
            // 1. Tạo một Salt ngẫu nhiên (chuỗi gia vị) để trộn vào mật khẩu
            byte[] salt = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(salt);

            // 2. Băm mật khẩu cùng với Salt
            byte[] hash = pbkdf2(plainPassword.toCharArray(), salt);

            // 3. Ghép Salt và Hash lại thành 1 chuỗi bằng Base64 để dễ dàng lưu vào kiểu VARCHAR trong CSDL
            return Base64.getEncoder().encodeToString(salt) + ":" +
                   Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi hệ thống trong quá trình mã hóa mật khẩu", e);
        }
    }

    /**
     * Kiểm tra mật khẩu người dùng nhập vào có khớp với mã Hash trong Database không.
     * (Sử dụng khi: Đăng nhập)
     * 
     * @param plainPassword Mật khẩu người dùng vừa gõ vào ô đăng nhập
     * @param storedHash    Mã băm lấy từ Database lên (có chứa cả salt)
     * @return true nếu mật khẩu đúng, ngược lại là false
     */
    public static boolean checkPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null || !storedHash.contains(":")) {
            return false;
        }

        try {
            // 1. Tách chuỗi trong Database ra thành Salt và Hash
            String[] parts = storedHash.split(":");
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] hashFromDb = Base64.getDecoder().decode(parts[1]);

            // 2. Lấy mật khẩu người dùng vừa nhập, đem băm với chính cái Salt lấy từ DB
            byte[] testHash = pbkdf2(plainPassword.toCharArray(), salt);

            // 3. So sánh kết quả băm thử với mã băm trong DB
            return java.util.Arrays.equals(hashFromDb, testHash);

        } catch (Exception e) {
            System.err.println("Lỗi kiểm tra mật khẩu (có thể do chuỗi Hash sai định dạng): " + e.getMessage());
            return false;
        }
    }

    /**
     * Hàm tiện ích (Internal) thực thi thuật toán PBKDF2
     */
    private static byte[] pbkdf2(char[] password, byte[] salt) 
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }
}