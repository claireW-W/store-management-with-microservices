package com.store.delivery.dto;

import com.store.delivery.entity.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateResponse {
    
    private String deliveryId;
    private String orderId;
    private DeliveryStatus status;
    private String location;
    private String notes;
    private LocalDateTime updatedAt;
    private boolean success;
    private String message;
}
