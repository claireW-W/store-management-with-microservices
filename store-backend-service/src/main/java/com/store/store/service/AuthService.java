package com.store.store.service;

import com.store.store.dto.LoginRequest;
import com.store.store.dto.LoginResponse;
import com.store.store.dto.SignupRequest;
import com.store.store.dto.SignupResponse;
import com.store.store.model.User;
import com.store.store.repository.UserRepository;
import com.store.store.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * User login authentication
     */
    public LoginResponse login(LoginRequest request) {
        String requestId = java.util.UUID.randomUUID().toString();
        log.info("[{}] [AUTH_SERVICE] Attempting login for username: {} at {}", 
                requestId, request.getUsername(), java.time.Instant.now());
        
        try {
            // Bootstrap: if no users exist, create a default demo user
            if (userRepository.count() == 0) {
                User bootstrap = User.builder()
                        .username("customer")
                        .passwordHash(passwordEncoder.encode("COMP5348"))
                        .email("customer@example.com")
                        .firstName("Demo")
                        .lastName("User")
                        .isActive(true)
                        .build();
                userRepository.save(bootstrap);
                log.info("[{}] [AUTH_SERVICE] Created default demo user 'customer'", requestId);
            }

            // Find user
            User user = userRepository.findActiveByUsername(request.getUsername())
                        .orElseThrow(() -> {
                            log.warn("[{}] [AUTH_SERVICE] User not found: {} at {}", 
                                    requestId, request.getUsername(), java.time.Instant.now());
                            return new IllegalArgumentException("Invalid username or password");
                        });
            
            log.info("[{}] [AUTH_SERVICE] User found: {} (ID: {}) at {}", 
                    requestId, user.getUsername(), user.getId(), java.time.Instant.now());
            
            // Password verification
            boolean passwordMatch = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
            log.info("[{}] [AUTH_SERVICE] Password verification result: {} at {}", 
                    requestId, passwordMatch, java.time.Instant.now());
            
                if (!passwordMatch) {
                    log.warn("[{}] [AUTH_SERVICE] Password mismatch for user: {} at {}", 
                            requestId, request.getUsername(), java.time.Instant.now());
                    throw new IllegalArgumentException("Invalid username or password");
                }
            
            // Generate JWT Token
            String token = jwtUtil.generateToken(
                    user.getUsername(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName()
            );
            
            log.info("[{}] [AUTH_SERVICE] JWT token generated for user: {} at {}", 
                    requestId, user.getUsername(), java.time.Instant.now());
            
            LoginResponse response = LoginResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getExpirationTime())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .loginTime(LocalDateTime.now())
                        .message("Login successful")
                    .build();
            
            log.info("[{}] [AUTH_SERVICE] Login successful for username: {} at {}", 
                    requestId, request.getUsername(), java.time.Instant.now());
            
            return response;
        } catch (IllegalArgumentException e) {
            log.warn("[{}] [AUTH_SERVICE] Authentication failed for username: {} - {}", 
                    requestId, request.getUsername(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[{}] [AUTH_SERVICE] Unexpected error during login for username: {} - {}", 
                    requestId, request.getUsername(), e.getMessage(), e);
                throw new RuntimeException("Login service temporarily unavailable, please try again later", e);
        }
    }
    
    /**
     * Validate token
     */
    public boolean validateToken(String token, String username) {
        try {
            return jwtUtil.validateToken(token, username);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Extract username from token
     */
    public String extractUsernameFromToken(String token) {
        try {
            return jwtUtil.extractUsername(token);
        } catch (Exception e) {
            log.warn("Failed to extract username from token: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * User signup (registration) - Admin operation
     * Creates a new user account in the system
     */
    public SignupResponse signup(SignupRequest request) {
        String requestId = java.util.UUID.randomUUID().toString();
        log.info("[{}] [SIGNUP_REQUEST] Received signup request for username: {} at {}", 
                requestId, request.getUsername(), java.time.Instant.now());
        
        try {
            // Check if username already exists
            if (userRepository.existsByUsername(request.getUsername())) {
                log.warn("[{}] [SIGNUP_FAILED] Username already exists: {} at {}", 
                        requestId, request.getUsername(), java.time.Instant.now());
                throw new IllegalArgumentException("Username already exists");
            }
            
            // Check if email already exists
            if (userRepository.existsByEmail(request.getEmail())) {
                log.warn("[{}] [SIGNUP_FAILED] Email already exists: {} at {}", 
                        requestId, request.getEmail(), java.time.Instant.now());
                throw new IllegalArgumentException("Email already exists");
            }
            
            // Hash password
            String hashedPassword = passwordEncoder.encode(request.getPassword());
            log.info("[{}] [SIGNUP_SERVICE] Password hashed successfully at {}", 
                    requestId, java.time.Instant.now());
            
            // Create new user
            User newUser = User.builder()
                    .username(request.getUsername())
                    .passwordHash(hashedPassword)
                    .email(request.getEmail())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phone(request.getPhone())
                    .isActive(true)
                    .build();
            
            // Save user
            User savedUser = userRepository.save(newUser);
            log.info("[{}] [SIGNUP_SUCCESS] User created successfully: {} (ID: {}) at {}", 
                    requestId, savedUser.getUsername(), savedUser.getId(), java.time.Instant.now());
            
            // Build response
            SignupResponse response = SignupResponse.builder()
                    .success(true)
                    .message("User created successfully")
                    .username(savedUser.getUsername())
                    .email(savedUser.getEmail())
                    .firstName(savedUser.getFirstName())
                    .lastName(savedUser.getLastName())
                    .userId(savedUser.getId())
                    .createdAt(savedUser.getCreatedAt())
                    .build();
            
            log.info("[{}] [SIGNUP_SUCCESS] Signup completed for username: {} at {}", 
                    requestId, request.getUsername(), java.time.Instant.now());
            
            return response;
        } catch (IllegalArgumentException e) {
            log.warn("[{}] [SIGNUP_FAILED] Signup failed for username: {} - {} at {}", 
                    requestId, request.getUsername(), e.getMessage(), java.time.Instant.now());
            throw e;
        } catch (Exception e) {
            log.error("[{}] [SIGNUP_ERROR] Unexpected error during signup for username: {} - {} at {}", 
                    requestId, request.getUsername(), e.getMessage(), java.time.Instant.now(), e);
            throw new RuntimeException("Signup service temporarily unavailable, please try again later", e);
        }
    }
}
