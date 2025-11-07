package com.noffice.exception;

import com.noffice.reponse.ResponseAPI;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.persistence.OptimisticLockException;

import java.util.Map;


@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý lỗi validation (@Valid).
     * Bắt lỗi khi dữ liệu gửi lên không hợp lệ theo các annotation đã định nghĩa trong DTO.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ResponseAPI> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("Dữ liệu không hợp lệ");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseAPI(null, errorMessage, 400));
    }

    /**
     * Xử lý các lỗi liên quan đến ràng buộc CSDL.
     * Ví dụ: giá trị quá dài, vi phạm khóa ngoại, vi phạm ràng buộc unique.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ResponseAPI> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String message = "Lỗi ràng buộc dữ liệu";
       if (ex.getMostSpecificCause().getMessage().contains("value too long for type character varying")) {
            message = "Dữ liệu nhập vào quá dài so với quy định.";
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseAPI(null, message, 400));
    }

    @ExceptionHandler(jakarta.persistence.OptimisticLockException.class)
    public ResponseEntity<Map<String, String>> handleConflict(OptimisticLockException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "conflict",
                        "message", "Dữ liệu đã bị thay đổi bởi người khác! Vui lòng tải lại."
                ));
    }

}