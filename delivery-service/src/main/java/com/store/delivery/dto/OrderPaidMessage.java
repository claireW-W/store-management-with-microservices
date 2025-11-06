package com.store.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPaidMessage {
    private String orderId;
    private String orderNumber;
    private String customerId;
    private String customerEmail;
    private String customerName;
    private String shippingAddress;
    private String warehouseId;
    private String carrier;
    private LocalDateTime paidAt;
}

