package com.store.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailResponse {
    
    private String emailId;
    private String recipientEmail;
    private String subject;
    private String status;
    private LocalDateTime sentAt;
    private String message;
}
