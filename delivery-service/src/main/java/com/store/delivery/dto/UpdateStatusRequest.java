package com.store.delivery.dto;

import com.store.delivery.entity.DeliveryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequest {
    
    @NotNull(message = "Status is required")
    private DeliveryStatus status;
    
    private String location;
    private String notes;
}
