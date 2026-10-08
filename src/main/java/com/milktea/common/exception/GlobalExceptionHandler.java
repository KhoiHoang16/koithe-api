package com.milktea.common.exception;

import com.milktea.common.response.ApiResponse;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiResponse<Void>> business(BusinessException ex) {
        return ResponseEntity.status(ex.getStatus()).body(new ApiResponse<>(ex.getStatus().value(), ex.getMessage(), null, Instant.now()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> validation(MethodArgumentNotValidException ex) {
        FieldError error = ex.getBindingResult().getFieldError();
        String message = error == null ? "Dữ liệu không hợp lệ" : error.getField() + ": " + error.getDefaultMessage();
        return ResponseEntity.badRequest().body(new ApiResponse<>(400, message, null, Instant.now()));
    }
    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, jakarta.persistence.OptimisticLockException.class})
    ResponseEntity<ApiResponse<Void>> optimisticLock(ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse<>(409, "Dữ liệu tồn kho vừa được cập nhật; vui lòng thử lại", null, Instant.now()));
    }
    @ExceptionHandler({org.springframework.security.access.AccessDeniedException.class, org.springframework.security.authorization.AuthorizationDeniedException.class})
    ResponseEntity<ApiResponse<Void>> accessDenied(Exception ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(403, "Bạn không có quyền truy cập tính năng này", null, Instant.now()));
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> unexpected(Exception ex) {
        return ResponseEntity.internalServerError().body(new ApiResponse<>(500, "Lỗi hệ thống", null, Instant.now()));
    }
}
