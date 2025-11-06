package com.store.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigResponse {
    
    private Double probability;
    private LocalDateTime updatedAt;
    private boolean success;
    private String message;
}
