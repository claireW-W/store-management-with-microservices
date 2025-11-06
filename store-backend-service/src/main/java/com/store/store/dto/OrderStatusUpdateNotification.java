package com.store.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Order Status Update Notification for WebSocket
 * Sent to users in real-time when order status changes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusUpdateNotification {
    private Long orderId;
    private String orderNumber;
    private String oldStatus;
    private String newStatus;
    private String message;
    private String trackingNumber;
    private LocalDateTime timestamp;
    private String notificationType; // ORDER_UPDATE, PAYMENT_SUCCESS, DELIVERY_UPDATE, etc.
}

