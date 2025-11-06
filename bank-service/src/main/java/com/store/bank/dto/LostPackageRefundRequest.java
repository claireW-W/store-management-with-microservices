package com.store.bank.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LostPackageRefundRequest {
    
    @NotBlank(message = "Order ID is required")
    @Size(max = 50, message = "Order ID must not exceed 50 characters")
    private String orderId;
    
    @NotBlank(message = "Customer ID is required")
    @Size(max = 50, message = "Customer ID must not exceed 50 characters")
    private String customerId;
    
    @NotBlank(message = "Delivery ID is required")
    @Size(max = 50, message = "Delivery ID must not exceed 50 characters")
    private String deliveryId;
    
    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
    @Digits(integer = 13, fraction = 2, message = "Refund amount must have at most 13 integer digits and 2 decimal places")
    private BigDecimal refundAmount;
    
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter code")
    private String currency;
    
    @NotBlank(message = "Lost reason is required")
    @Size(max = 255, message = "Lost reason must not exceed 255 characters")
    private String lostReason;
    
    private String description;
}
