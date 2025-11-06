package com.store.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponse {
    
    private boolean success;
    
    private String message;
    
    private String username;
    
    private String email;
    
    private String firstName;
    
    private String lastName;
    
    private Long userId;
    
    private LocalDateTime createdAt;
}


