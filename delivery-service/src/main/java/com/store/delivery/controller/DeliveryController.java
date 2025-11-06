package com.store.delivery.controller;

import com.store.delivery.dto.*;
import com.store.delivery.entity.Delivery;
import com.store.delivery.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/delivery")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DeliveryController {
    
    private final DeliveryService deliveryService;
    
    /**
     * API 1: Create delivery request
     * POST /api/delivery/request
     */
    @PostMapping("/request")
    public ResponseEntity<DeliveryResponse> createDelivery(@Valid @RequestBody DeliveryRequest request) {
        log.info("Received delivery request for order: {}", request.getOrderId());
        
        try {
            DeliveryResponse response = deliveryService.createDelivery(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid delivery request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating delivery: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * API 2: Update delivery status (manual)
     * PUT /api/delivery/{deliveryId}/status
     */
    @PutMapping("/{deliveryId}/status")
    public ResponseEntity<StatusUpdateResponse> updateStatus(
            @PathVariable String deliveryId,
            @Valid @RequestBody UpdateStatusRequest request) {
        log.info("Received status update request for delivery: {} to {}", deliveryId, request.getStatus());
        
        try {
            StatusUpdateResponse response = deliveryService.updateStatus(deliveryId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("Delivery not found: {}", deliveryId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating delivery status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * API 3: Handle lost package
     * POST /api/delivery/{deliveryId}/lost
     */
    @PostMapping("/{deliveryId}/lost")
    public ResponseEntity<LostPackageResponse> handleLostPackage(
            @PathVariable String deliveryId,
            @Valid @RequestBody LostPackageRequest request) {
        log.info("Received lost package request for delivery: {}", deliveryId);
        
        try {
            LostPackageResponse response = deliveryService.handleLostPackage(deliveryId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("Delivery not found: {}", deliveryId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error handling lost package: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * API 4: Query lost packages
     * GET /api/delivery/lost-packages
     */
    @GetMapping("/lost-packages")
    public ResponseEntity<List<DeliveryResponse>> getLostPackages() {
        log.info("Retrieving all lost packages");
        
        try {
            List<DeliveryResponse> lostPackages = deliveryService.getLostPackages();
            return ResponseEntity.ok(lostPackages);
        } catch (Exception e) {
            log.error("Error retrieving lost packages: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * API 5: Set lost probability
     * PUT /api/delivery/config/lost-probability
     */
    @PutMapping("/config/lost-probability")
    public ResponseEntity<ConfigResponse> setLostProbability(
            @Valid @RequestBody LostProbabilityConfig config) {
        log.info("Setting lost probability to: {}", config.getProbability());
        
        try {
            ConfigResponse response = deliveryService.setLostProbability(config.getProbability());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid probability value: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error setting lost probability: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * API 6: Get lost probability
     * GET /api/delivery/config/lost-probability
     */
    @GetMapping("/config/lost-probability")
    public ResponseEntity<ConfigResponse> getLostProbability() {
        log.info("Retrieving current lost probability");
        
        try {
            ConfigResponse response = deliveryService.getLostProbability();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving lost probability: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
