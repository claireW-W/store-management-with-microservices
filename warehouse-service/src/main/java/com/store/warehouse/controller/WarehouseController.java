package com.store.warehouse.controller;

import com.store.warehouse.dto.InventoryResponse;
import com.store.warehouse.dto.ReserveInventoryRequest;
import com.store.warehouse.dto.ReserveInventoryResponse;
import com.store.warehouse.service.WarehouseService;
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
 * Warehouse Controller - handles warehouse and inventory API endpoints
 */
@RestController
@RequestMapping("/warehouse")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class WarehouseController {
    
    private final WarehouseService warehouseService;
    
    /**
     * Query product inventory across all warehouses
     * GET /api/warehouse/inventory/{productId}
     */
    @GetMapping("/inventory/{productId}")
    public ResponseEntity<?> getInventoryByProduct(@PathVariable Long productId) {
        
        String requestId = java.util.UUID.randomUUID().toString();
        
        try {
            log.info("[{}] [GET_INVENTORY] Querying inventory for product: {}", 
                    requestId, productId);
            
            List<InventoryResponse> inventories = warehouseService.getInventoryByProduct(productId);
            
            if (inventories.isEmpty()) {
                log.warn("[{}] [GET_INVENTORY] No inventory found for product: {}", 
                        requestId, productId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Not Found", 
                                "No inventory found for product: " + productId, 
                                requestId));
            }
            
            log.info("[{}] [GET_INVENTORY_SUCCESS] Found inventory in {} warehouses", 
                    requestId, inventories.size());
            return ResponseEntity.ok(inventories);
            
        } catch (Exception e) {
            log.error("[{}] [GET_INVENTORY_ERROR] Unexpected error: {}", 
                    requestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal Server Error", 
                            "An unexpected error occurred", 
                            requestId));
        }
    }
    
    /**
     * Reserve inventory for an order
     * POST /api/warehouse/reserve
     */
    @PostMapping("/reserve")
    public ResponseEntity<?> reserveInventory(
            @Valid @RequestBody ReserveInventoryRequest request) {
        
        String requestId = java.util.UUID.randomUUID().toString();
        
        try {
            log.info("[{}] [RESERVE_INVENTORY] Reserving inventory for order: {}", 
                    requestId, request.getOrderId());
            
            ReserveInventoryResponse response = warehouseService.reserveInventory(request);
            
            log.info("[{}] [RESERVE_INVENTORY_SUCCESS] Reserved inventory for order: {} ({} items)", 
                    requestId, request.getOrderId(), response.getReservations().size());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            log.error("[{}] [RESERVE_INVENTORY_ERROR] Invalid request: {}", 
                    requestId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Bad Request", e.getMessage(), requestId));
                    
        } catch (IllegalStateException e) {
            log.error("[{}] [RESERVE_INVENTORY_ERROR] Reservation failed: {}", 
                    requestId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Reservation Failed", e.getMessage(), requestId));
                    
        } catch (Exception e) {
            log.error("[{}] [RESERVE_INVENTORY_ERROR] Unexpected error: {}", 
                    requestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal Server Error", 
                            "An unexpected error occurred", 
                            requestId));
        }
    }
    
    /**
     * Health check endpoint
     * GET /api/warehouse/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "warehouse-service");
        response.put("timestamp", java.time.Instant.now());
        return ResponseEntity.ok(response);
    }
    
    // ========== Private Helper Methods ==========
    
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

