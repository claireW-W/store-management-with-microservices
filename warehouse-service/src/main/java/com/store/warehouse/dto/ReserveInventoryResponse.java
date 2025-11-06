package com.store.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Reserve Inventory Response DTO
 * Response for POST /api/warehouse/reserve
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReserveInventoryResponse {
    
    private String message;
    private String orderId;
    private List<ReservationDetail> reservations;
    
    /**
     * Reservation Detail
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReservationDetail {
        
        private String reservationId;
        private Long productId;
        private Long warehouseId;
        private Integer quantity;
        private String status;
        private LocalDateTime expiresAt;
    }
}

