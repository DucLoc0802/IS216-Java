package PetHotel.exception;

/**
 * Ngoại lệ xảy ra khi xác thực danh tính thất bại (Login).
 */
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}