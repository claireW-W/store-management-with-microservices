package com.store.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for canceling an order
 * POST /api/orders/{orderId}/cancel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelOrderRequest {
    
    private String reason;
}

