package com.deepdirect.deepwebide_be.global.exception;

import com.deepdirect.deepwebide_be.global.dto.ApiResponseDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<?>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponseDto.error(400, ex.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponseDto<?>> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDto.error(404, ex.getMessage()));
    }

//    @ExceptionHandler(AccessDeniedException.class)
//    public ResponseEntity<ApiResponseDto<?>> handleAccessDenied(AccessDeniedException ex) {
//        return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                .body(ApiResponseDto.error(403, "접근이 거부되었습니다."));
//    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<?>> handleOtherExceptions(Exception ex) {
        System.out.println("500" + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error(500, "서버 내부 오류가 발생했습니다."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("입력값이 유효하지 않습니다.");

        return ResponseEntity.badRequest()
                .body(ApiResponseDto.error(400, errorMessage));
    }


    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ApiResponseDto<?>> handleGlobalException(GlobalException ex) {
        return ResponseEntity
                .status(ex.getErrorCode().getStatus())
                .body(ApiResponseDto.error(
                        ex.getErrorCode().getStatus().value(),
                        ex.getErrorCode().getMessage()
                ));
    }
}
