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
public class OrderCreatedMessage {
    private String orderId;
    private String orderNumber;
    private String customerId;
    private String customerEmail;
    private String customerName;
    private BigDecimal totalAmount;
    private String currency;
    private LocalDateTime orderDate;
}

