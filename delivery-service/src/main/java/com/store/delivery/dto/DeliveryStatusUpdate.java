package com.store.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryStatusUpdate {
    
    private String deliveryId;
    private String orderId;
    private String status;
    private String location;
    private String notes;
    private LocalDateTime timestamp;
}
