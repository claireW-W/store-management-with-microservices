package com.store.delivery.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LostProbabilityConfig {
    
    @NotNull(message = "Probability is required")
    @DecimalMin(value = "0.0", message = "Probability must be between 0 and 1")
    @DecimalMax(value = "1.0", message = "Probability must be between 0 and 1")
    private Double probability;
}
