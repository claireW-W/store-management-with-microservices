package com.store.email.errors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * Handle parameter validation exception
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String requestId = java.util.UUID.randomUUID().toString();
        log.warn("[{}] [EMAIL_VALIDATION_ERROR] Email request validation failed: {} at {}", 
                requestId, ex.getMessage(), Instant.now());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("Email Validation Failed")
                .message("Email request parameter validation failed")
                .details(errors)
                .timestamp(Instant.now())
                .requestId(requestId)
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle email sending exception
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleEmailException(RuntimeException ex) {
        String requestId = java.util.UUID.randomUUID().toString();
        log.error("[{}] [EMAIL_ERROR] Email service error: {} at {}", 
                requestId, ex.getMessage(), Instant.now(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("Email Service Error")
                .message("Email service temporarily unavailable, please try again later")
                .timestamp(Instant.now())
                .requestId(requestId)
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        String requestId = java.util.UUID.randomUUID().toString();
        log.error("[{}] [EMAIL_GENERIC_ERROR] Unexpected email service error: {} at {}", 
                requestId, ex.getMessage(), Instant.now(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("Email Service Error")
                .message("Email service internal error")
                .timestamp(Instant.now())
                .requestId(requestId)
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
