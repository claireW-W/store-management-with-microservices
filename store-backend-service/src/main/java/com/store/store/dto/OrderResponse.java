package com.store.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Order Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    
    private Long id;
    private String orderNumber;
    private Long userId;
    private String status;
    private BigDecimal totalAmount;
    private Map<String, Object> shippingAddress;
    private Map<String, Object> billingAddress;
    private String paymentMethod;
    private String paymentStatus;
    private String paymentTransactionId;
    private String deliveryId;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Order items
    private List<OrderItemDTO> items;
}

