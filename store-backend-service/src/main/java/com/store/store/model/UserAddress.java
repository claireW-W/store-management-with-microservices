package com.store.store.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User Address Entity
 */
@Entity
@Table(name = "user_addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "address_type", length = 20)
    private String addressType = "SHIPPING"; // BILLING or SHIPPING
    
    @Column(nullable = false)
    private String street;
    
    @Column(nullable = false, length = 100)
    private String city;
    
    @Column(nullable = false, length = 50)
    private String state;
    
    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;
    
    @Column(nullable = false, length = 50)
    private String country;
    
    @Column(name = "is_default")
    private Boolean isDefault = false;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

