package com.store.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Delivery Notification Message from Delivery Service
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

