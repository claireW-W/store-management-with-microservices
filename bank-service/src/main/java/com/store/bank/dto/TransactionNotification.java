package com.store.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * WebSocket notification for real-time transaction updates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionNotification {
    
    private String transactionId;
    private String customerId;
    private String transactionType; // PAYMENT, REFUND, TRANSFER
    private BigDecimal amount;
    private String currency;
    private String status; // SUCCESS, FAILED, PENDING
    private String orderId;
    private String message;
    private LocalDateTime timestamp;
}

