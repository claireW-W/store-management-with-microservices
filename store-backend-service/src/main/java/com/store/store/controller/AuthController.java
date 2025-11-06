package com.store.store.controller;

import com.store.store.dto.LoginRequest;
import com.store.store.dto.LoginResponse;
import com.store.store.dto.SignupRequest;
import com.store.store.dto.SignupResponse;
import com.store.store.model.User;
import com.store.store.repository.UserRepository;
import com.store.store.service.AuthService;
import com.store.store.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {
    
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${services.bank.url:http://bank-service:8082/api}")
    private String bankServiceUrl;
    
    /**
     * User login authentication
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        String requestId = java.util.UUID.randomUUID().toString();
        log.info("[{}] [AUTH_REQUEST] Received login request for username: {} at {}", 
                requestId, request.getUsername(), java.time.Instant.now());
        
        try {
            LoginResponse response = authService.login(request);
            log.info("[{}] [AUTH_SUCCESS] Login successful for username: {} at {}", 
                    requestId, request.getUsername(), java.time.Instant.now());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("[{}] [AUTH_FAILED] Login failed for username: {} - Reason: {} at {}", 
                    requestId, request.getUsername(), e.getMessage(), java.time.Instant.now());
            
            // Return detailed error information
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Authentication Failed");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", java.time.Instant.now());
            errorResponse.put("requestId", requestId);
            errorResponse.put("username", request.getUsername());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (Exception e) {
            log.error("[{}] [AUTH_ERROR] Login error for username: {} - Error: {} at {}", 
                    requestId, request.getUsername(), e.getMessage(), java.time.Instant.now(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * User signup (registration) - Admin operation
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        String requestId = java.util.UUID.randomUUID().toString();
        log.info("[{}] [SIGNUP_REQUEST] Received signup request for username: {} at {}", 
                requestId, request.getUsername(), java.time.Instant.now());
        
        try {
            SignupResponse response = authService.signup(request);
            log.info("[{}] [SIGNUP_SUCCESS] Signup successful for username: {} at {}", 
                    requestId, request.getUsername(), java.time.Instant.now());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("[{}] [SIGNUP_FAILED] Signup failed for username: {} - Reason: {} at {}", 
                    requestId, request.getUsername(), e.getMessage(), java.time.Instant.now());
            
            // Return detailed error information
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Signup Failed");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", java.time.Instant.now());
            errorResponse.put("requestId", requestId);
            errorResponse.put("username", request.getUsername());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            log.error("[{}] [SIGNUP_ERROR] Signup error for username: {} - Error: {} at {}", 
                    requestId, request.getUsername(), e.getMessage(), java.time.Instant.now(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", "Signup service temporarily unavailable, please try again later");
            errorResponse.put("timestamp", java.time.Instant.now());
            errorResponse.put("requestId", requestId);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get user balance
     * GET /api/auth/balance
     */
    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(@RequestHeader("Authorization") String authHeader) {
        String requestId = java.util.UUID.randomUUID().toString();
        
        try {
            // Extract user ID from JWT token
            Long userId = extractUserIdFromToken(authHeader);
            String customerId = "CUST-" + userId;
            
            log.info("[{}] [GET_BALANCE] Getting balance for user: {} (customerId: {})", 
                    requestId, userId, customerId);
            
            // Call bank service to get balance
            String url = bankServiceUrl + "/bank/balance/" + customerId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("[{}] [GET_BALANCE_SUCCESS] Balance retrieved for user: {}", requestId, userId);
                return ResponseEntity.ok(response.getBody());
            } else {
                log.warn("[{}] [GET_BALANCE_ERROR] Failed to get balance for user: {}", requestId, userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Balance not found", "Account not found", requestId));
            }
        } catch (IllegalArgumentException e) {
            log.error("[{}] [GET_BALANCE_ERROR] Invalid token: {}", requestId, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Unauthorized", "Invalid or expired token", requestId));
        } catch (Exception e) {
            log.error("[{}] [GET_BALANCE_ERROR] Unexpected error: {}", requestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal server error", 
                            "An unexpected error occurred", requestId));
        }
    }
    
    /**
     * Extract user ID from JWT token
     */
    private Long extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header");
        }
        
        String token = authHeader.substring(7);
        
        // Extract username from token first
        String username = jwtUtil.extractUsername(token);
        
        // Validate token
        if (!jwtUtil.validateToken(token, username)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        
        // Look up user in database to get actual user ID
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        return user.getId();
    }
    
    private Map<String, Object> createErrorResponse(String error, String message, String requestId) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", java.time.Instant.now());
        errorResponse.put("requestId", requestId);
        return errorResponse;
    }
}
