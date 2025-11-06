package com.store.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    
    @NotBlank(message = "Recipient email cannot be empty")
    @Email(message = "Invalid email format")
    private String recipientEmail;
    
    @NotBlank(message = "Recipient name cannot be empty")
    private String recipientName;
    
    @NotBlank(message = "Email subject cannot be empty")
    private String subject;
    
    @NotBlank(message = "Email content cannot be empty")
    private String content;
    
    private String orderNumber;
    private String status;
    private String amount;
    private String templateType;
}
