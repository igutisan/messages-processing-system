package com.prueba.tecnica.infrastructure.rest.advice;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.prueba.tecnica.domain.exception.DomainException;
import com.prueba.tecnica.domain.exception.OriginNotFoundException;
import com.prueba.tecnica.infrastructure.rest.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OriginNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleOriginNotFoundException(OriginNotFoundException ex,
            HttpServletRequest request) {
        log.warn("Origin not found: {} for URI {}", ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException ex, HttpServletRequest request) {
        log.warn("Domain exception: {} for URI {}", ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadableException(HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        log.warn("Unreadable message for URI {}: {}", request.getRequestURI(), ex.getMessage());

        String field = "request";
        if (ex.getCause() instanceof InvalidFormatException cause && !cause.getPath().isEmpty()) {
            field = cause.getPath().get(cause.getPath().size() - 1).getFieldName();
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Invalid request data",
                        Map.of(field, "Invalid value")));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        log.warn("Validation error for URI {}: {}", request.getRequestURI(), ex.getMessage());

        Map<String, String> validationErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            validationErrors.put(error.getField(), error.getDefaultMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Invalid request data", validationErrors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error for URI {}", request.getRequestURI(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error"));
    }
}
