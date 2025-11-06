package com.store.warehouse.messaging;

import com.store.warehouse.config.RabbitMQConfig;
import com.store.warehouse.dto.OrderEventMessage;
import com.store.warehouse.dto.ReserveInventoryResponse;
import com.store.warehouse.dto.WarehouseNotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Message Publisher for Warehouse Service
 * Publishes warehouse events to other services via RabbitMQ
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessagePublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    /**
     * Publish warehouse.stock.reserved event to Store Backend
     */
    public void publishStockReservedEvent(String orderNumber, ReserveInventoryResponse response) {
        log.info("Publishing stock.reserved event: {}", orderNumber);
        
        try {
            WarehouseNotificationMessage message = WarehouseNotificationMessage.builder()
                    .orderNumber(orderNumber)
                    .status("RESERVED")
                    .message("Inventory reserved successfully")
                    .timestamp(LocalDateTime.now())
                    .items(response.getReservations().stream()
                            .map(res -> WarehouseNotificationMessage.InventoryItem.builder()
                                    .productId(res.getProductId())
                                    .warehouseId(res.getWarehouseId())
                                    .quantity(res.getQuantity())
                                    .build())
                            .collect(Collectors.toList()))
                    .build();
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.WAREHOUSE_EXCHANGE,
                    RabbitMQConfig.WAREHOUSE_STOCK_RESERVED_KEY,
                    message
            );
            
            log.info("Stock reserved event published successfully");
            
        } catch (Exception e) {
            log.error("Failed to publish stock reserved event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Publish warehouse.stock.insufficient event to Store Backend
     */
    public void publishStockInsufficientEvent(String orderNumber, String reason) {
        log.info("Publishing stock.insufficient event: {}", orderNumber);
        
        try {
            WarehouseNotificationMessage message = WarehouseNotificationMessage.builder()
                    .orderNumber(orderNumber)
                    .status("INSUFFICIENT")
                    .message("Insufficient inventory: " + reason)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.WAREHOUSE_EXCHANGE,
                    RabbitMQConfig.WAREHOUSE_STOCK_INSUFFICIENT_KEY,
                    message
            );
            
            log.info("Stock insufficient event published successfully");
            
        } catch (Exception e) {
            log.error("Failed to publish stock insufficient event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Publish warehouse.stock.deducted event to Delivery Service
     */
    public void publishStockDeductedEvent(OrderEventMessage orderEvent) {
        log.info("Publishing stock.deducted event: {}", orderEvent.getOrderNumber());
        
        try {
            // Build delivery request message
            java.util.Map<String, Object> deliveryMessage = new java.util.HashMap<>();
            deliveryMessage.put("orderNumber", orderEvent.getOrderNumber());
            deliveryMessage.put("customerId", orderEvent.getCustomerId());
            deliveryMessage.put("shippingAddress", orderEvent.getShippingAddress());
            deliveryMessage.put("totalAmount", orderEvent.getTotalAmount());
            deliveryMessage.put("items", orderEvent.getItems());
            deliveryMessage.put("timestamp", LocalDateTime.now());
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.DELIVERY_EXCHANGE,
                    "delivery.create",
                    deliveryMessage
            );
            
            log.info("Stock deducted event (delivery create) published successfully");
            
        } catch (Exception e) {
            log.error("Failed to publish stock deducted event: {}", e.getMessage(), e);
        }
    }
}

