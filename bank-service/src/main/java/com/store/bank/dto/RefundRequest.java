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
public class RefundRequest {
    
    @NotBlank(message = "Transaction ID is required")
    @Size(max = 50, message = "Transaction ID must not exceed 50 characters")
    private String transactionId;
    
    @NotBlank(message = "Order ID is required")
    @Size(max = 50, message = "Order ID must not exceed 50 characters")
    private String orderId;
    
    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
    @Digits(integer = 13, fraction = 2, message = "Refund amount must have at most 13 integer digits and 2 decimal places")
    private BigDecimal refundAmount;
    
    @NotBlank(message = "Reason is required")
    @Size(max = 255, message = "Reason must not exceed 255 characters")
    private String reason;
    
    private String description;
}
