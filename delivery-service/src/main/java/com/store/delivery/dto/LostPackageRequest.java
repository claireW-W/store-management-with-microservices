package com.store.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LostPackageRequest {
    
    @NotBlank(message = "Reason is required")
    private String reason;
    
    private String location;
    private String notes;
}
