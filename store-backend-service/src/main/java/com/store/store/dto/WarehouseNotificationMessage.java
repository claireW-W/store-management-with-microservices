package com.store.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Warehouse Notification Message
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseNotificationMessage {
    private String orderId;
    private String status; // RESERVED, INSUFFICIENT, DEDUCTED
    private String message;
    private LocalDateTime timestamp;
    private List<InventoryItem> items;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InventoryItem {
        private Long productId;
        private Integer quantity;
        private Integer availableStock;
    }
}

