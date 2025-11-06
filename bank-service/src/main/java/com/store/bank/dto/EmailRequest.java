package com.store.bank.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    
    @NotBlank(message = "Recipient email cannot be empty")
    @Email(message = "Invalid email format")
    private String recipientEmail;
    
    private String recipientName;
    private String orderNumber;
    private String deliveryId;
    private String transactionId;
    private String subject;
    private String content;
    private String templateType;
}
