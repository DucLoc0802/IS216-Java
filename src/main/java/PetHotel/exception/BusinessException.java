package PetHotel.exception;

// ═══════════════════════════════════════════════════════════════
// FILE: exception/BusinessException.java
// ═══════════════════════════════════════════════════════════════

/**
 * BusinessException — Lỗi nghiệp vụ chung.
 * Dùng khi vi phạm quy tắc kinh doanh mà không thuộc loại lỗi chuyên biệt hơn.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
