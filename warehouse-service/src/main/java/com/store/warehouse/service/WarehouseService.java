package com.store.warehouse.service;

import com.store.warehouse.dto.InventoryResponse;
import com.store.warehouse.dto.ReserveInventoryRequest;
import com.store.warehouse.dto.ReserveInventoryResponse;
import com.store.warehouse.entity.*;
import com.store.warehouse.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Warehouse Service - handles warehouse and inventory operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseService {
    
    private final WarehouseRepository warehouseRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;
    private final InventoryTransactionRepository transactionRepository;
    
    private static final int RESERVATION_EXPIRY_MINUTES = 30;
    
    /**
     * Query product inventory across all warehouses
     * GET /api/warehouse/inventory/{productId}
     */
    public List<InventoryResponse> getInventoryByProduct(Long productId) {
        String requestId = UUID.randomUUID().toString();
        log.info("[{}] Querying inventory for product: {}", requestId, productId);
        
        List<Inventory> inventories = inventoryRepository.findByProductId(productId);
        
        if (inventories.isEmpty()) {
            log.warn("[{}] No inventory found for product: {}", requestId, productId);
            return new ArrayList<>();
        }
        
        // Get warehouse details
        List<Long> warehouseIds = inventories.stream()
                .map(Inventory::getWarehouseId)
                .collect(Collectors.toList());
        
        List<Warehouse> warehouses = warehouseRepository.findAllById(warehouseIds);
        
        // Map warehouse info
        var warehouseMap = warehouses.stream()
                .collect(Collectors.toMap(Warehouse::getId, w -> w));
        
        List<InventoryResponse> responses = inventories.stream()
                .map(inv -> {
                    Warehouse warehouse = warehouseMap.get(inv.getWarehouseId());
                    return InventoryResponse.builder()
                            .warehouseId(inv.getWarehouseId())
                            .warehouseCode(warehouse != null ? warehouse.getWarehouseCode() : null)
                            .warehouseName(warehouse != null ? warehouse.getName() : null)
                            .productId(inv.getProductId())
                            .availableQuantity(inv.getAvailableQuantity())
                            .reservedQuantity(inv.getReservedQuantity())
                            .totalQuantity(inv.getTotalQuantity())
                            .build();
                })
                .collect(Collectors.toList());
        
        log.info("[{}] Found inventory in {} warehouses for product: {}", 
                requestId, responses.size(), productId);
        
        return responses;
    }
    
    /**
     * Reserve inventory for an order
     * POST /api/warehouse/reserve
     */
    @Transactional
    public ReserveInventoryResponse reserveInventory(ReserveInventoryRequest request) {
        String requestId = UUID.randomUUID().toString();
        log.info("[{}] Reserving inventory for order: {}", requestId, request.getOrderId());
        
        List<ReserveInventoryResponse.ReservationDetail> reservationDetails = new ArrayList<>();
        
        for (ReserveInventoryRequest.ReservationItem item : request.getItems()) {
            // Find available inventory
            List<Inventory> inventories = inventoryRepository.findByProductId(item.getProductId());
            
            if (inventories.isEmpty()) {
                throw new IllegalStateException(
                        "No inventory found for product: " + item.getProductId());
            }
            
            // Find first warehouse with sufficient stock
            Inventory selectedInventory = null;
            for (Inventory inv : inventories) {
                if (inv.getAvailableQuantity() >= item.getQuantity()) {
                    selectedInventory = inv;
                    break;
                }
            }
            
            if (selectedInventory == null) {
                throw new IllegalStateException(
                        "Insufficient inventory for product: " + item.getProductId() + 
                        ". Required: " + item.getQuantity());
            }
            
            // Create reservation
            String reservationId = generateReservationId();
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(RESERVATION_EXPIRY_MINUTES);
            
            InventoryReservation reservation = InventoryReservation.builder()
                    .reservationId(reservationId)
                    .orderId(request.getOrderId())
                    .warehouseId(selectedInventory.getWarehouseId())
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .status("PENDING")
                    .expiresAt(expiresAt)
                    .build();
            
            reservationRepository.save(reservation);
            
            // Update inventory - reduce available, increase reserved
            selectedInventory.setAvailableQuantity(
                    selectedInventory.getAvailableQuantity() - item.getQuantity());
            selectedInventory.setReservedQuantity(
                    selectedInventory.getReservedQuantity() + item.getQuantity());
            inventoryRepository.save(selectedInventory);
            
            // Create transaction log
            createInventoryTransaction(
                    selectedInventory.getWarehouseId(),
                    item.getProductId(),
                    "RESERVE",
                    item.getQuantity(),
                    reservationId,
                    "RESERVATION",
                    "Reserved for order: " + request.getOrderId()
            );
            
            // Add to response
            reservationDetails.add(
                    ReserveInventoryResponse.ReservationDetail.builder()
                            .reservationId(reservationId)
                            .productId(item.getProductId())
                            .warehouseId(selectedInventory.getWarehouseId())
                            .quantity(item.getQuantity())
                            .status("PENDING")
                            .expiresAt(expiresAt)
                            .build()
            );
            
            log.info("[{}] Reserved {} units of product {} from warehouse {}", 
                    requestId, item.getQuantity(), item.getProductId(), 
                    selectedInventory.getWarehouseId());
        }
        
        log.info("[{}] Successfully reserved inventory for order: {}", 
                requestId, request.getOrderId());
        
        return ReserveInventoryResponse.builder()
                .message("Inventory reserved successfully")
                .orderId(request.getOrderId())
                .reservations(reservationDetails)
                .build();
    }
    
    /**
     * Confirm reservation and deduct inventory (called when payment is successful)
     */
    @Transactional
    public void confirmReservation(String orderId) {
        String requestId = UUID.randomUUID().toString();
        log.info("[{}] Confirming reservation for order: {}", requestId, orderId);
        
        List<InventoryReservation> reservations = reservationRepository.findByOrderId(orderId);
        
        if (reservations.isEmpty()) {
            throw new IllegalStateException("No reservations found for order: " + orderId);
        }
        
        for (InventoryReservation reservation : reservations) {
            if (!"PENDING".equals(reservation.getStatus())) {
                log.warn("[{}] Reservation {} is not PENDING: {}", 
                        requestId, reservation.getReservationId(), reservation.getStatus());
                continue;
            }
            
            // Update reservation status
            reservation.setStatus("CONFIRMED");
            reservationRepository.save(reservation);
            
            // Get inventory and deduct reserved quantity
            Inventory inventory = inventoryRepository.findByWarehouseIdAndProductId(
                    reservation.getWarehouseId(), 
                    reservation.getProductId()
            ).orElseThrow(() -> new IllegalStateException(
                    "Inventory not found for warehouse " + reservation.getWarehouseId() + 
                    " and product " + reservation.getProductId()));
            
            // Deduct from reserved and total
            inventory.setReservedQuantity(inventory.getReservedQuantity() - reservation.getQuantity());
            inventory.setTotalQuantity(inventory.getTotalQuantity() - reservation.getQuantity());
            inventoryRepository.save(inventory);
            
            // Create transaction log
            createInventoryTransaction(
                    reservation.getWarehouseId(),
                    reservation.getProductId(),
                    "DEDUCT",
                    reservation.getQuantity(),
                    orderId,
                    "ORDER",
                    "Inventory deducted for order: " + orderId
            );
            
            log.info("[{}] Confirmed reservation {} and deducted {} units", 
                    requestId, reservation.getReservationId(), reservation.getQuantity());
        }
        
        log.info("[{}] Successfully confirmed all reservations for order: {}", requestId, orderId);
    }
    
    /**
     * Release reservation (called when order is cancelled)
     */
    @Transactional
    public void releaseReservation(String orderId) {
        String requestId = UUID.randomUUID().toString();
        log.info("[{}] Releasing reservation for order: {}", requestId, orderId);
        
        List<InventoryReservation> reservations = reservationRepository.findByOrderId(orderId);
        
        if (reservations.isEmpty()) {
            log.warn("[{}] No reservations found for order: {}", requestId, orderId);
            return;
        }
        
        for (InventoryReservation reservation : reservations) {
            if ("CANCELLED".equals(reservation.getStatus())) {
                log.warn("[{}] Reservation {} is already cancelled", 
                        requestId, reservation.getReservationId());
                continue;
            }
            
            // Update reservation status
            reservation.setStatus("CANCELLED");
            reservationRepository.save(reservation);
            
            // Get inventory and release reserved quantity
            Inventory inventory = inventoryRepository.findByWarehouseIdAndProductId(
                    reservation.getWarehouseId(), 
                    reservation.getProductId()
            ).orElse(null);
            
            if (inventory != null) {
                // Add back to available, reduce from reserved
                inventory.setAvailableQuantity(inventory.getAvailableQuantity() + reservation.getQuantity());
                inventory.setReservedQuantity(inventory.getReservedQuantity() - reservation.getQuantity());
                inventoryRepository.save(inventory);
                
                // Create transaction log
                createInventoryTransaction(
                        reservation.getWarehouseId(),
                        reservation.getProductId(),
                        "RELEASE",
                        reservation.getQuantity(),
                        orderId,
                        "ORDER_CANCELLATION",
                        "Reservation released for cancelled order: " + orderId
                );
                
                log.info("[{}] Released reservation {} and restored {} units", 
                        requestId, reservation.getReservationId(), reservation.getQuantity());
            }
        }
        
        log.info("[{}] Successfully released all reservations for order: {}", requestId, orderId);
    }
    
    // ========== Private Helper Methods ==========
    
    private String generateReservationId() {
        return "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private void createInventoryTransaction(Long warehouseId, Long productId, 
                                           String transactionType, Integer quantity,
                                           String referenceId, String referenceType, 
                                           String notes) {
        InventoryTransaction transaction = InventoryTransaction.builder()
                .warehouseId(warehouseId)
                .productId(productId)
                .transactionType(transactionType)
                .quantity(quantity)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .notes(notes)
                .build();
        
        transactionRepository.save(transaction);
    }
}

