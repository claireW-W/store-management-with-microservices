package com.store.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryStatusMessage {
    private String deliveryId;
    private String orderId;
    private String orderNumber;
    private String customerId;
    private String customerEmail;
    private String status; // PREPARING, SHIPPED, IN_TRANSIT, DELIVERED, LOST
    private String trackingNumber;
    private LocalDateTime statusChangeTime;
    private String message;
}

