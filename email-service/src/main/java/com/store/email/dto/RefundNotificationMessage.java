package com.store.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundNotificationMessage {
    private String transactionId;
    private String orderId;
    private String customerId;
    private String customerEmail;
    private BigDecimal amount;
    private String currency;
    private String message;
    private LocalDateTime timestamp;
}

