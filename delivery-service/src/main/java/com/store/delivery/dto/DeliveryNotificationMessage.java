package com.store.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Unified message DTO published by Delivery Service and consumed by Store Backend.
 * It mirrors com.store.store.dto.DeliveryNotificationMessage to ensure JSON structure matches.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryNotificationMessage {
    private String deliveryId;
    private String orderId;
    private String customerId;
    private String customerEmail;      // User email
    private String customerName;       // User name
    private String status; // CREATED, SHIPPED, IN_TRANSIT, DELIVERED, LOST, DELAYED
    private String message;
    private String trackingNumber;
    private String carrier;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime actualDelivery;
    private LocalDateTime timestamp;
}


