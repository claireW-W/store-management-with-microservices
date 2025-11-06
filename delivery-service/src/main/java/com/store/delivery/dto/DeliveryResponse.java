package com.store.delivery.dto;

import com.store.delivery.entity.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResponse {
    
    private String deliveryId;
    private String orderId;
    private String customerId;
    private DeliveryStatus status;
    private String trackingNumber;
    private LocalDateTime estimatedPickup;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime actualPickup;
    private LocalDateTime actualDelivery;
    private String carrier;
    private String notes;
    private String shippingAddress;
    private LocalDateTime createdAt;
}
