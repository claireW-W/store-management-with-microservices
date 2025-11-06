package com.store.store.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating a new order
 * POST /api/orders
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
    
    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<OrderItemDTO> items;
    
    @NotNull(message = "Shipping address is required")
    @Valid
    private AddressDTO shippingAddress;
    
    @Valid
    private AddressDTO billingAddress;
    
    private String paymentMethod;
    
    private String notes;
}

