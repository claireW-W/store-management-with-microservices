package com.store.store.messaging;

import com.store.store.config.RabbitMQConfig;
import com.store.store.dto.OrderEventMessage;
import com.store.store.model.Order;
import com.store.store.model.OrderItem;
import com.store.store.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Message Publisher for Store Backend Service
 * Publishes order events to other services via RabbitMQ
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessagePublisher {
    
    private final RabbitTemplate rabbitTemplate;
    private final OrderItemRepository orderItemRepository;
    
    /**
     * Publish order.created event to Warehouse Service
     * Warehouse will reserve inventory
     */
    public void publishOrderCreatedEvent(Order order) {
        log.info("Publishing order.created event: {}", order.getOrderNumber());
        
        try {
            OrderEventMessage message = buildOrderEventMessage(order, "CREATED", 
                    "Order created, please reserve inventory");
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_CREATED_KEY,
                    message
            );
            
            log.info("Order created event published successfully for: {}", order.getOrderNumber());
            
        } catch (Exception e) {
            log.error("Failed to publish order created event for {}: {}", 
                    order.getOrderNumber(), e.getMessage(), e);
        }
    }
    
    /**
     * Publish order.paid event to Warehouse Service
     * Warehouse will deduct inventory and notify Delivery
     */
    public void publishOrderPaidEvent(Order order) {
        log.info("Publishing order.paid event: {}", order.getOrderNumber());
        
        try {
            OrderEventMessage message = buildOrderEventMessage(order, "PAID", 
                    "Payment successful, please process shipment");
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_PAID_KEY,
                    message
            );
            
            log.info("Order paid event published successfully for: {}", order.getOrderNumber());
            
        } catch (Exception e) {
            log.error("Failed to publish order paid event for {}: {}", 
                    order.getOrderNumber(), e.getMessage(), e);
        }
    }
    
    /**
     * Publish order.cancelled event to Warehouse Service
     * Warehouse will release reserved inventory
     */
    public void publishOrderCancelledEvent(Order order, String reason) {
        log.info("Publishing order.cancelled event: {}", order.getOrderNumber());
        
        try {
            OrderEventMessage message = buildOrderEventMessage(order, "CANCELLED", 
                    "Order cancelled: " + reason + ", please release inventory");
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_CANCELLED_KEY,
                    message
            );
            
            log.info("Order cancelled event published successfully for: {}", order.getOrderNumber());
            
        } catch (Exception e) {
            log.error("Failed to publish order cancelled event for {}: {}", 
                    order.getOrderNumber(), e.getMessage(), e);
        }
    }

    /**
     * Publish order.cancelled to Delivery so the scheduler stops advancing the delivery
     */
    public void publishOrderCancelledToDelivery(Order order) {
        try {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("orderId", order.getOrderNumber());
            payload.put("deliveryId", order.getDeliveryId());
            payload.put("timestamp", LocalDateTime.now());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.DELIVERY_EXCHANGE,
                    "order.cancelled",
                    payload
            );
            log.info("Published order.cancelled to Delivery for order {}", order.getOrderNumber());
        } catch (Exception e) {
            log.warn("Failed to publish order.cancelled to Delivery for {}: {}", order.getOrderNumber(), e.getMessage());
        }
    }
    
    /**
     * Publish order.completed event to Email Service
     * Email Service will send thank you email
     */
    public void publishOrderCompletedEvent(Order order) {
        log.info("Publishing order.completed event: {}", order.getOrderNumber());
        
        try {
            OrderEventMessage message = buildOrderEventMessage(order, "COMPLETED", 
                    "Order completed successfully");
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_COMPLETED_KEY,
                    message
            );
            
            log.info("Order completed event published successfully for: {}", order.getOrderNumber());
            
        } catch (Exception e) {
            log.error("Failed to publish order completed event for {}: {}", 
                    order.getOrderNumber(), e.getMessage(), e);
        }
    }
    
    /**
     * Helper method to build OrderEventMessage from Order entity
     */
    private OrderEventMessage buildOrderEventMessage(Order order, String eventType, String message) {
        // Get order items
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        
        List<OrderEventMessage.OrderItemInfo> items = orderItems.stream()
                .map(item -> OrderEventMessage.OrderItemInfo.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .build())
                .collect(Collectors.toList());
        
        // Extract shipping address
        String shippingAddress = order.getShippingAddress() != null ? 
                extractFullAddress(order.getShippingAddress()) : null;
        
        return OrderEventMessage.builder()
                .orderId(order.getId().toString())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .customerId("CUST-" + order.getUserId())
                .eventType(eventType)
                .totalAmount(order.getTotalAmount())
                .currency("AUD")
                .shippingAddress(shippingAddress)
                .message(message)
                .timestamp(LocalDateTime.now())
                .items(items)
                .build();
    }
    
    /**
     * Helper method to extract full address from address map
     */
    private String extractFullAddress(java.util.Map<String, Object> addressMap) {
        if (addressMap == null) {
            return null;
        }
        
        StringBuilder address = new StringBuilder();
        if (addressMap.containsKey("street")) {
            address.append(addressMap.get("street")).append(", ");
        }
        if (addressMap.containsKey("suburb")) {
            address.append(addressMap.get("suburb")).append(", ");
        }
        if (addressMap.containsKey("state")) {
            address.append(addressMap.get("state")).append(" ");
        }
        if (addressMap.containsKey("postcode")) {
            address.append(addressMap.get("postcode")).append(", ");
        }
        if (addressMap.containsKey("country")) {
            address.append(addressMap.get("country"));
        }
        
        return address.toString().trim().replaceAll(", $", "");
    }
}

