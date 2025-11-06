package com.store.delivery.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddress {
    
    @JsonProperty("street")
    private String street;
    
    @JsonProperty("city")
    private String city;
    
    @JsonProperty("state")
    private String state;
    
    @JsonProperty("postalCode")
    private String postalCode;
    
    @JsonProperty("country")
    private String country;
    
    @JsonProperty("fullAddress")
    private String fullAddress;
    
    // Constructor: create from full address string
    public ShippingAddress(String fullAddress) {
        this.fullAddress = fullAddress;
        // Can parse address string here to extract individual parts
        // For simplicity, we only set fullAddress
    }
    
    // Get full address string
    public String getFullAddress() {
        if (fullAddress != null) {
            return fullAddress;
        }
        // If all parts have values, combine into full address
        if (street != null && city != null) {
            StringBuilder sb = new StringBuilder();
            if (street != null) sb.append(street);
            if (city != null) sb.append(", ").append(city);
            if (state != null) sb.append(", ").append(state);
            if (postalCode != null) sb.append(" ").append(postalCode);
            if (country != null) sb.append(", ").append(country);
            return sb.toString();
        }
        return null;
    }
}
