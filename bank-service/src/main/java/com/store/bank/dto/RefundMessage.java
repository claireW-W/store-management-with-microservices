package com.store.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * RabbitMQ message for refund requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String orderId;
    private String customerId;
    private BigDecimal amount;
    private String currency;
    private String deliveryId;
    private String reason;
    private String messageType; // LOST_PACKAGE, ORDER_CANCELLED, etc.
}

