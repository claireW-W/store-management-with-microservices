package com.store.store.websocket;

import com.store.store.dto.OrderStatusUpdateNotification;
import com.store.store.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * WebSocket Notification Service for Store Backend
 * Sends real-time notifications to connected clients via WebSocket
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * Send order status update notification to specific user
     * The message is sent to /user/{userId}/queue/orders
     */
    public void sendOrderStatusUpdate(Long userId, Order order, String oldStatus, String message) {
        log.info("Sending order status update via WebSocket to user {}: order={}, status={}", 
                userId, order.getOrderNumber(), order.getStatus());
        
        try {
            OrderStatusUpdateNotification notification = OrderStatusUpdateNotification.builder()
                    .orderId(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .oldStatus(oldStatus)
                    .newStatus(order.getStatus())
                    .message(message)
                    .trackingNumber(order.getDeliveryId())
                    .timestamp(LocalDateTime.now())
                    .notificationType("ORDER_UPDATE")
                    .build();
            
            // Send to specific user's queue
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/orders",
                    notification
            );
            // Also broadcast to topics for clients subscribed without user-binding
            messagingTemplate.convertAndSend("/topic/orders", notification);
            messagingTemplate.convertAndSend("/topic/orders/" + order.getOrderNumber(), notification);
            
            log.info("Order status update notification sent successfully");
            
        } catch (Exception e) {
            log.error("Failed to send order status update notification", e);
            // Don't throw - notification failure shouldn't affect order processing
        }
    }
    
    /**
     * Send payment success notification to user
     */
    public void sendPaymentSuccessNotification(Long userId, String orderNumber, String transactionId) {
        log.info("Sending payment success notification via WebSocket to user {}: order={}", 
                userId, orderNumber);
        
        try {
            OrderStatusUpdateNotification notification = OrderStatusUpdateNotification.builder()
                    .orderNumber(orderNumber)
                    .message("Payment successful! Transaction ID: " + transactionId)
                    .timestamp(LocalDateTime.now())
                    .notificationType("PAYMENT_SUCCESS")
                    .build();
            
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/orders",
                    notification
            );
            messagingTemplate.convertAndSend("/topic/orders", notification);
            messagingTemplate.convertAndSend("/topic/orders/" + orderNumber, notification);
            
            log.info("Payment success notification sent successfully");
            
        } catch (Exception e) {
            log.error("Failed to send payment success notification", e);
        }
    }
    
    /**
     * Send delivery update notification to user
     */
    public void sendDeliveryUpdateNotification(Long userId, String orderNumber, String status, 
                                               String trackingNumber, String message) {
        log.info("Sending delivery update notification via WebSocket to user {}: order={}, status={}", 
                userId, orderNumber, status);
        
        try {
            OrderStatusUpdateNotification notification = OrderStatusUpdateNotification.builder()
                    .orderNumber(orderNumber)
                    .newStatus(status)
                    .message(message)
                    .trackingNumber(trackingNumber)
                    .timestamp(LocalDateTime.now())
                    .notificationType("DELIVERY_UPDATE")
                    .build();
            
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/orders",
                    notification
            );
            messagingTemplate.convertAndSend("/topic/orders", notification);
            messagingTemplate.convertAndSend("/topic/orders/" + orderNumber, notification);
            
            log.info("Delivery update notification sent successfully");
            
        } catch (Exception e) {
            log.error("Failed to send delivery update notification", e);
        }
    }
    
    /**
     * Broadcast stock update to all connected users
     * The message is sent to /topic/products/stock
     */
    public void broadcastStockUpdate(Long productId, Integer newStock) {
        log.info("Broadcasting stock update for product {}: newStock={}", productId, newStock);
        
        try {
            java.util.Map<String, Object> stockUpdate = new java.util.HashMap<>();
            stockUpdate.put("productId", productId);
            stockUpdate.put("newStock", newStock);
            stockUpdate.put("timestamp", LocalDateTime.now());
            
            // Broadcast to all users subscribed to /topic/products/stock
            messagingTemplate.convertAndSend("/topic/products/stock", stockUpdate);
            
            log.info("Stock update broadcasted successfully");
            
        } catch (Exception e) {
            log.error("Failed to broadcast stock update", e);
        }
    }
    
    /**
     * Broadcast promotion/announcement to all users
     * The message is sent to /topic/promotions
     */
    public void broadcastPromotion(String title, String message) {
        log.info("Broadcasting promotion: {}", title);
        
        try {
            java.util.Map<String, Object> promotion = new java.util.HashMap<>();
            promotion.put("title", title);
            promotion.put("message", message);
            promotion.put("timestamp", LocalDateTime.now());
            
            // Broadcast to all users subscribed to /topic/promotions
            messagingTemplate.convertAndSend("/topic/promotions", promotion);
            
            log.info("Promotion broadcasted successfully");
            
        } catch (Exception e) {
            log.error("Failed to broadcast promotion", e);
        }
    }
}

