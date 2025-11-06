package com.store.store.errors;

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
        log.warn("[{}] [VALIDATION_ERROR] Request validation failed: {} at {}", 
                requestId, ex.getMessage(), Instant.now());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("Validation Failed")
                .message("Request parameter validation failed")
                .details(errors)
                .timestamp(Instant.now())
                .requestId(requestId)
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle authentication failure exception
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(IllegalArgumentException ex) {
        String requestId = java.util.UUID.randomUUID().toString();
        log.warn("[{}] [AUTH_ERROR] Authentication failed: {} at {}", 
                requestId, ex.getMessage(), Instant.now());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("Authentication Failed")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .requestId(requestId)
                .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    /**
     * Handle runtime exception
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        String requestId = java.util.UUID.randomUUID().toString();
        log.error("[{}] [RUNTIME_ERROR] Runtime error occurred: {} at {}", 
                requestId, ex.getMessage(), Instant.now(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("Internal Server Error")
                .message("Service temporarily unavailable, please try again later")
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
        log.error("[{}] [GENERIC_ERROR] Unexpected error occurred: {} at {}", 
                requestId, ex.getMessage(), Instant.now(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("Internal Server Error")
                .message("Internal system error")
                .timestamp(Instant.now())
                .requestId(requestId)
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
