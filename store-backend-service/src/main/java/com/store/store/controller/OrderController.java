package com.store.store.controller;

import com.store.store.dto.CancelOrderRequest;
import com.store.store.dto.CancelOrderResponse;
import com.store.store.dto.CreateOrderRequest;
import com.store.store.dto.OrderResponse;
import com.store.store.model.User;
import com.store.store.repository.UserRepository;
import com.store.store.service.OrderService;
import com.store.store.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Order Controller - handles order-related API endpoints
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class OrderController {
    
    private final OrderService orderService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    
    /**
     * Create a new order
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CreateOrderRequest request) {
        
        String requestId = java.util.UUID.randomUUID().toString();
        
        try {
            // Extract user ID from JWT token
            Long userId = extractUserIdFromToken(authHeader);
            log.info("[{}] [CREATE_ORDER] Creating order for user: {}", requestId, userId);
            
            OrderResponse response = orderService.createOrder(userId, request);
            
            log.info("[{}] [CREATE_ORDER_SUCCESS] Order created: {}", 
                    requestId, response.getOrderNumber());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            log.error("[{}] [CREATE_ORDER_ERROR] Invalid request: {}", requestId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Invalid request", e.getMessage(), requestId));
                    
        } catch (IllegalStateException e) {
            log.error("[{}] [CREATE_ORDER_ERROR] Processing error: {}", requestId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Order processing failed", e.getMessage(), requestId));
                    
        } catch (Exception e) {
            log.error("[{}] [CREATE_ORDER_ERROR] Unexpected error: {}", 
                    requestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal server error", 
                            "An unexpected error occurred", requestId));
        }
    }
    
    /**
     * Get all orders for the authenticated user
     * GET /api/orders
     */
    @GetMapping
    public ResponseEntity<?> getOrders(@RequestHeader("Authorization") String authHeader) {
        
        String requestId = java.util.UUID.randomUUID().toString();
        
        try {
            // Extract user ID from JWT token
            Long userId = extractUserIdFromToken(authHeader);
            log.info("[{}] [GET_ORDERS] Getting orders for user: {}", requestId, userId);
            
            List<OrderResponse> orders = orderService.getOrders(userId);
            
            log.info("[{}] [GET_ORDERS_SUCCESS] Found {} orders", requestId, orders.size());
            return ResponseEntity.ok(orders);
            
        } catch (IllegalArgumentException e) {
            log.error("[{}] [GET_ORDERS_ERROR] Invalid token: {}", requestId, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Unauthorized", "Invalid or expired token", requestId));
                    
        } catch (Exception e) {
            log.error("[{}] [GET_ORDERS_ERROR] Unexpected error: {}", 
                    requestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal server error", 
                            "An unexpected error occurred", requestId));
        }
    }
    
    /**
     * Get a specific order by ID for the authenticated user
     * GET /api/orders/{id}
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long orderId) {
        
        String requestId = java.util.UUID.randomUUID().toString();
        
        try {
            // Extract user ID from JWT token
            Long userId = extractUserIdFromToken(authHeader);
            log.info("[{}] [GET_ORDER] Getting order {} for user: {}", requestId, orderId, userId);
            
            OrderResponse order = orderService.getOrderById(userId, orderId);
            
            log.info("[{}] [GET_ORDER_SUCCESS] Found order: {}", requestId, order.getOrderNumber());
            return ResponseEntity.ok(order);
            
        } catch (IllegalArgumentException e) {
            log.error("[{}] [GET_ORDER_ERROR] Order not found: {}", requestId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Order not found", e.getMessage(), requestId));
                    
        } catch (Exception e) {
            log.error("[{}] [GET_ORDER_ERROR] Unexpected error: {}", 
                    requestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal server error", 
                            "An unexpected error occurred", requestId));
        }
    }
    
    /**
     * Cancel an order
     * POST /api/orders/{orderId}/cancel
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long orderId,
            @RequestBody(required = false) CancelOrderRequest request) {
        
        String requestId = java.util.UUID.randomUUID().toString();
        
        try {
            // Extract user ID from JWT token
            Long userId = extractUserIdFromToken(authHeader);
            log.info("[{}] [CANCEL_ORDER] Canceling order {} for user: {}", 
                    requestId, orderId, userId);
            
            // Use empty request if not provided
            if (request == null) {
                request = new CancelOrderRequest();
            }
            
            CancelOrderResponse response = orderService.cancelOrder(userId, orderId, request);
            
            log.info("[{}] [CANCEL_ORDER_SUCCESS] Order cancelled: {}", 
                    requestId, response.getOrderNumber());
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("[{}] [CANCEL_ORDER_ERROR] Invalid request: {}", requestId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Invalid request", e.getMessage(), requestId));
                    
        } catch (IllegalStateException e) {
            log.error("[{}] [CANCEL_ORDER_ERROR] Cannot cancel: {}", requestId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Cannot cancel order", e.getMessage(), requestId));
                    
        } catch (Exception e) {
            log.error("[{}] [CANCEL_ORDER_ERROR] Unexpected error: {}", 
                    requestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal server error", 
                            "An unexpected error occurred", requestId));
        }
    }
    
    // ========== Private Helper Methods ==========
    
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
    
    /**
     * Create error response map
     */
    private Map<String, Object> createErrorResponse(String error, String message, String requestId) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", java.time.Instant.now());
        errorResponse.put("requestId", requestId);
        return errorResponse;
    }
}

