package com.store.warehouse.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Reserve Inventory Request DTO
 * Request for POST /api/warehouse/reserve
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReserveInventoryRequest {
    
    @NotBlank(message = "Order ID is required")
    private String orderId;
    
    @NotEmpty(message = "Items cannot be empty")
    @Valid
    private List<ReservationItem> items;
    
    /**
     * Reservation Item
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReservationItem {
        
        @NotNull(message = "Product ID is required")
        private Long productId;
        
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private Integer quantity;
    }
}

