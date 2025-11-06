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
public class LoginResponse {
    
    private String token;
    private String tokenType;
    private Long expiresIn;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime loginTime;
    private String message;
}
