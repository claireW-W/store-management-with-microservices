package com.store.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for order cancellation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelOrderResponse {
    
    private String message;
    private String orderNumber;
    private String status;
    private String refundTransactionId;
    private boolean refundProcessed;
}

