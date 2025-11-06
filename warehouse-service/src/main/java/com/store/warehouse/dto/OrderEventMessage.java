package com.store.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Event Message received from Store Backend
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEventMessage {
    private String orderId;
    private String orderNumber;
    private Long userId;
    private String customerId;
    private String eventType; // CREATED, PAID, CANCELLED, COMPLETED
    private BigDecimal totalAmount;
    private String currency;
    private String shippingAddress;
    private String message;
    private LocalDateTime timestamp;
    private List<OrderItemInfo> items;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemInfo {
        private Long productId;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
}

