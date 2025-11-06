package com.store.store.messaging;

import com.store.store.config.RabbitMQConfig;
import com.store.store.dto.DeliveryNotificationMessage;
import com.store.store.model.Order;
import com.store.store.model.OrderStatusHistory;
import com.store.store.model.User;
import com.store.store.repository.OrderRepository;
import com.store.store.repository.OrderStatusHistoryRepository;
import com.store.store.repository.UserRepository;
import com.store.store.websocket.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;

/**
 * Delivery Message Listener
 * Listens to delivery status updates from Delivery Service
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryMessageListener {
    
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final DeliveryStatusUpdater deliveryStatusUpdater;
    private final MessagePublisher messagePublisher;
    private final WebSocketNotificationService webSocketNotificationService;
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    @Value("${services.email.url:http://localhost:8085/api}")
    private String emailServiceUrl;
    
    /**
     * Handle delivery status notifications
     */
    @RabbitListener(queues = RabbitMQConfig.DELIVERY_STATUS_QUEUE)
    @Transactional
    public void handleDeliveryNotification(DeliveryNotificationMessage message) {
        log.info("Received delivery notification: orderId={}, status={}", 
                message.getOrderId(), message.getStatus());
        
        try {
            switch (message.getStatus()) {
                case "CREATED":
                    handleDeliveryCreated(message);
                    break;
                case "SHIPPED":
                    handleDeliveryShipped(message);
                    break;
                case "IN_TRANSIT":
                    handleDeliveryInTransit(message);
                    break;
                case "DELIVERED":
                    handleDeliveryDelivered(message);
                    break;
                case "FAILED":
                    // Treat FAILED same as LOST for assignment semantics
                    handleDeliveryLost(message);
                    break;
                case "LOST":
                    handleDeliveryLost(message);
                    break;
                case "DELAYED":
                    handleDeliveryDelayed(message);
                    break;
                case "PICKED_UP":
                    handleDeliveryPickedUp(message);
                    break;
                case "PENDING_PICKUP":
                    handleDeliveryPendingPickup(message);
                    break;
                default:
                    log.warn("Unknown delivery status: {}", message.getStatus());
            }
        } catch (Exception e) {
            log.error("Failed to process delivery notification for order {}: {}", 
                    message.getOrderId(), e.getMessage(), e);
        }
    }
    
    private void handleDeliveryCreated(DeliveryNotificationMessage message) {
        log.info("Delivery created for order: {}", message.getOrderId());
        deliveryStatusUpdater.updateOrderStatus(message, "PROCESSING", "Delivery created - " + message.getMessage());
    }
    
    private void handleDeliveryShipped(DeliveryNotificationMessage message) {
        log.info("Order shipped: {}", message.getOrderId());
        deliveryStatusUpdater.updateOrderStatus(message, "SHIPPED", "Order shipped - Tracking: " + message.getTrackingNumber());
        sendShippingUpdateEmail(message, "SHIPPED", "Your order has been shipped. Tracking: " + message.getTrackingNumber());
    }
    
    private void handleDeliveryInTransit(DeliveryNotificationMessage message) {
        log.info("Order in transit: {}", message.getOrderId());
        deliveryStatusUpdater.updateOrderStatus(message, "IN_TRANSIT", "Order in transit - " + message.getMessage());
        sendShippingUpdateEmail(message, "IN_TRANSIT", "Your package is in transit. " + (message.getMessage() != null ? message.getMessage() : ""));
    }
    
    private void handleDeliveryDelivered(DeliveryNotificationMessage message) {
        log.info("Order delivered: {}", message.getOrderId());
        deliveryStatusUpdater.updateOrderStatus(message, "DELIVERED", "Order delivered successfully");
        sendShippingUpdateEmail(message, "DELIVERED", "Your order has been delivered successfully!");
        
        // Find and complete the order
        Optional<Order> orderOpt = orderRepository.findByOrderNumber(message.getOrderId());
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            // Publish order.completed event
            messagePublisher.publishOrderCompletedEvent(order);
        }
    }
    
    private void handleDeliveryLost(DeliveryNotificationMessage message) {
        log.error("Package lost for order: {}", message.getOrderId());
        deliveryStatusUpdater.updateOrderStatus(message, "LOST", "Package lost - " + message.getMessage());
        
        // Send package lost notification email
        sendPackageLostEmail(message);
        
        // Note: Bank Service will handle the refund automatically via its own listener
    }
    
    private void handleDeliveryDelayed(DeliveryNotificationMessage message) {
        log.warn("Delivery delayed for order: {}", message.getOrderId());
        // Don't change order status, just log it
        Optional<Order> orderOpt = orderRepository.findByOrderNumber(message.getOrderId());
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            OrderStatusHistory history = OrderStatusHistory.builder()
                    .orderId(order.getId())
                    .status(order.getStatus())
                    .notes("Delivery delayed - " + message.getMessage())
                    .build();
            orderStatusHistoryRepository.save(history);
        }
        // Send delay notification email
        sendShippingUpdateEmail(message, "DELAYED", "Your delivery has been delayed. " + (message.getMessage() != null ? message.getMessage() : ""));
    }

    private void handleDeliveryPickedUp(DeliveryNotificationMessage message) {
        log.info("Order picked up: {}", message.getOrderId());
        deliveryStatusUpdater.updateOrderStatus(message, "PICKED_UP", "Package picked up - " + message.getMessage());
        sendShippingUpdateEmail(message, "PICKED_UP", "Your package has been picked up by the carrier.");
    }

    private void handleDeliveryPendingPickup(DeliveryNotificationMessage message) {
        log.info("Order pending pickup: {}", message.getOrderId());
        deliveryStatusUpdater.updateOrderStatus(message, "PENDING_PICKUP", "Pending pickup - " + message.getMessage());
    }

    private void sendShippingUpdateEmail(DeliveryNotificationMessage message, String stage, String defaultMsg) {
        try {
            Optional<Order> orderOpt = orderRepository.findByOrderNumber(message.getOrderId());
            if (orderOpt.isEmpty()) {
                log.warn("Order not found for email: {}", message.getOrderId());
                return;
            }
            Order order = orderOpt.get();
            
            // Get user email from database
            String userEmail = "customer@example.com"; // default fallback
            String userName = "Customer"; // default fallback
            if (message.getCustomerEmail() != null && !message.getCustomerEmail().isEmpty()) {
                userEmail = message.getCustomerEmail();
                userName = message.getCustomerName() != null ? message.getCustomerName() : "Customer";
                log.info("Using customer email from message: {} ({})", userName, userEmail);
            } else {
                Optional<User> userOpt = userRepository.findById(order.getUserId());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    userEmail = user.getEmail();
                    userName = user.getFirstName() + " " + user.getLastName();
                    log.info("Sending shipping update email to user: {} ({})", userName, userEmail);
                }
            }
            
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("recipientEmail", userEmail);
            body.put("recipientName", userName);
            body.put("subject", "Shipping Update - " + stage + " - " + order.getOrderNumber());
            body.put("orderId", order.getOrderNumber());
            body.put("orderNumber", order.getOrderNumber());
            body.put("status", stage);
            body.put("message", defaultMsg);
            body.put("content", defaultMsg);
            
            String url = emailServiceUrl + "/email/shipping-update";
            restTemplate.postForEntity(url, body, java.util.Map.class);
            log.info("Shipping update email sent successfully for order {} to {}", order.getOrderNumber(), userEmail);
        } catch (Exception e) {
            log.warn("Failed to send shipping update email for order {}: {}", message.getOrderId(), e.getMessage());
        }
    }
    
    private void sendPackageLostEmail(DeliveryNotificationMessage message) {
        try {
            Optional<Order> orderOpt = orderRepository.findByOrderNumber(message.getOrderId());
            if (orderOpt.isEmpty()) {
                log.warn("Order not found for lost package email: {}", message.getOrderId());
                return;
            }
            Order order = orderOpt.get();
            
            // Get user email from database
            String userEmail = "customer@example.com"; // default fallback
            String userName = "Customer"; // default fallback
            if (message.getCustomerEmail() != null && !message.getCustomerEmail().isEmpty()) {
                userEmail = message.getCustomerEmail();
                userName = message.getCustomerName() != null ? message.getCustomerName() : "Customer";
                log.info("Using customer email from message for lost package: {} ({})", userName, userEmail);
            } else {
                Optional<User> userOpt = userRepository.findById(order.getUserId());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    userEmail = user.getEmail();
                    userName = user.getFirstName() + " " + user.getLastName();
                    log.info("Sending package lost email to user: {} ({})", userName, userEmail);
                }
            }
            
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("recipientEmail", userEmail);
            body.put("recipientName", userName);
            body.put("subject", "Package Lost - " + order.getOrderNumber());
            body.put("orderId", order.getOrderNumber());
            body.put("orderNumber", order.getOrderNumber());
            body.put("status", "LOST");
            String lostMessage = "We're sorry to inform you that your package has been lost in transit. " +
                    "We are processing a refund for your order. " +
                    (message.getMessage() != null ? message.getMessage() : "");
            body.put("message", lostMessage);
            body.put("content", lostMessage);
            
            String url = emailServiceUrl + "/email/package-lost";
            restTemplate.postForEntity(url, body, java.util.Map.class);
            log.info("Package lost notification email sent successfully for order {} to {}", order.getOrderNumber(), userEmail);
        } catch (Exception e) {
            log.error("Failed to send package lost email for order {}: {}", message.getOrderId(), e.getMessage(), e);
        }
    }
    
    /**
     * Helper method to update order status
     */
    // Status updates are delegated to DeliveryStatusUpdater with retry
}

