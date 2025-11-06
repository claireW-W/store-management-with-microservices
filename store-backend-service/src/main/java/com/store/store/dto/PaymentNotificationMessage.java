package com.store.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Notification Message from Bank Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentNotificationMessage {
    private String transactionId;
    private String orderId;
    private String customerId;
    private BigDecimal amount;
    private String currency;
    private String status; // SUCCESS, FAILED
    private String message;
    private LocalDateTime processedAt;
}

