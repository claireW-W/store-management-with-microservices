package com.store.store.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Address DTO for shipping and billing addresses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {
    
    @NotBlank(message = "Street is required")
    private String street;
    
    @NotBlank(message = "Suburb is required")
    private String suburb;
    
    @NotBlank(message = "State is required")
    private String state;
    
    @NotBlank(message = "Postcode is required")
    private String postcode;
    
    @NotBlank(message = "Country is required")
    private String country;
}

