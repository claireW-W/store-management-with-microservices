package com.store.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailResponse {
    
    private boolean success;
    private String message;
    private String emailId;
    private LocalDateTime sentTime;
    private String recipientEmail;
    private String subject;
}
