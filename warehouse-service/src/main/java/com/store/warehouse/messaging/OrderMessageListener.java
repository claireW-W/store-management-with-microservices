package com.store.warehouse.messaging;

import com.store.warehouse.config.RabbitMQConfig;
import com.store.warehouse.dto.OrderEventMessage;
import com.store.warehouse.dto.ReserveInventoryRequest;
import com.store.warehouse.dto.ReserveInventoryResponse;
import com.store.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Order Message Listener for Warehouse Service
 * Listens to order events from Store Backend
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderMessageListener {
    
    private final WarehouseService warehouseService;
    private final MessagePublisher messagePublisher;
    
    /**
     * Handle order events
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_EVENTS_QUEUE)
    @Transactional
    public void handleOrderEvent(OrderEventMessage message) {
        log.info("Received order event: orderNumber={}, eventType={}", 
                message.getOrderNumber(), message.getEventType());
        
        try {
            switch (message.getEventType()) {
                case "CREATED":
                    handleOrderCreated(message);
                    break;
                case "PAID":
                    handleOrderPaid(message);
                    break;
                case "CANCELLED":
                    handleOrderCancelled(message);
                    break;
                default:
                    log.warn("Unknown order event type: {}", message.getEventType());
            }
        } catch (Exception e) {
            log.error("Failed to process order event for order {}: {}", 
                    message.getOrderNumber(), e.getMessage(), e);
        }
    }
    
    /**
     * Handle order.created event - reserve inventory
     */
    private void handleOrderCreated(OrderEventMessage message) {
        log.info("Handling order created: {}", message.getOrderNumber());
        
        try {
            // Build reserve inventory request
            List<ReserveInventoryRequest.ReservationItem> items = message.getItems().stream()
                    .map(item -> new ReserveInventoryRequest.ReservationItem(
                            item.getProductId(), 
                            item.getQuantity()))
                    .collect(Collectors.toList());
            
            ReserveInventoryRequest request = new ReserveInventoryRequest();
            request.setOrderId(message.getOrderNumber());
            request.setItems(items);
            
            // Reserve inventory
            ReserveInventoryResponse response = warehouseService.reserveInventory(request);
            
            log.info("Inventory reserved successfully for order: {}", message.getOrderNumber());
            
            // Publish stock.reserved event
            messagePublisher.publishStockReservedEvent(message.getOrderNumber(), response);
            
        } catch (IllegalStateException e) {
            log.error("Insufficient inventory for order {}: {}", 
                    message.getOrderNumber(), e.getMessage());
            
            // Publish stock.insufficient event
            messagePublisher.publishStockInsufficientEvent(message.getOrderNumber(), e.getMessage());
        }
    }
    
    /**
     * Handle order.paid event - deduct inventory and notify delivery
     */
    private void handleOrderPaid(OrderEventMessage message) {
        log.info("Handling order paid: {}", message.getOrderNumber());
        
        try {
            // Deduct inventory (confirm reservation)
            warehouseService.confirmReservation(message.getOrderNumber());
            
            log.info("Inventory deducted successfully for order: {}", message.getOrderNumber());
            
            // Publish stock.deducted event
            messagePublisher.publishStockDeductedEvent(message);
            
        } catch (Exception e) {
            log.error("Failed to deduct inventory for order {}: {}", 
                    message.getOrderNumber(), e.getMessage());
        }
    }
    
    /**
     * Handle order.cancelled event - release reserved inventory
     */
    private void handleOrderCancelled(OrderEventMessage message) {
        log.warn("Handling order cancelled: {}", message.getOrderNumber());
        
        try {
            // Release inventory (cancel reservation)
            warehouseService.releaseReservation(message.getOrderNumber());
            
            log.info("Inventory released for cancelled order: {}", message.getOrderNumber());
            
        } catch (Exception e) {
            log.error("Failed to release inventory for order {}: {}", 
                    message.getOrderNumber(), e.getMessage());
        }
    }
}

