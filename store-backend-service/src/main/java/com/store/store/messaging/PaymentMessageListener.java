package com.store.store.messaging;

import com.store.store.config.RabbitMQConfig;
import com.store.store.dto.PaymentNotificationMessage;
import com.store.store.model.Order;
import com.store.store.repository.OrderRepository;
import com.store.store.repository.OrderStatusHistoryRepository;
import com.store.store.model.OrderStatusHistory;
import com.store.store.websocket.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Payment Message Listener
 * Listens to payment notifications from Bank Service
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentMessageListener {
    
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final MessagePublisher messagePublisher;
    private final WebSocketNotificationService webSocketNotificationService;
    
    /**
     * Handle payment success notification from Bank Service
     */
    @RabbitListener(queues = RabbitMQConfig.PAYMENT_RESULT_QUEUE)
    @Transactional
    public void handlePaymentNotification(PaymentNotificationMessage message) {
        log.info("Received payment notification: orderId={}, status={}", 
                message.getOrderId(), message.getStatus());
        
        try {
            if ("SUCCESS".equals(message.getStatus())) {
                handlePaymentSuccess(message);
            } else if ("FAILED".equals(message.getStatus())) {
                handlePaymentFailure(message);
            } else {
                log.warn("Unknown payment status: {}", message.getStatus());
            }
        } catch (Exception e) {
            log.error("Failed to process payment notification for order {}: {}", 
                    message.getOrderId(), e.getMessage(), e);
        }
    }
    
    /**
     * Handle successful payment
     */
    private void handlePaymentSuccess(PaymentNotificationMessage message) {
        log.info("Processing payment success for order: {}", message.getOrderId());
        
        // Find order by order number (message.getOrderId() is actually the order number)
        Optional<Order> orderOpt = orderRepository.findByOrderNumber(message.getOrderId());
        
        if (orderOpt.isEmpty()) {
            log.error("Order not found: {}", message.getOrderId());
            return;
        }
        
        Order order = orderOpt.get();
        
        // Update order payment status
        order.setPaymentTransactionId(message.getTransactionId());
        order.setPaymentStatus("PAID");
        order.setStatus("PAID");
        orderRepository.save(order);
        
        // Create status history
        OrderStatusHistory history = OrderStatusHistory.builder()
                .orderId(order.getId())
                .status("PAID")
                .notes("Payment successful via message queue - Transaction ID: " + message.getTransactionId())
                .build();
        orderStatusHistoryRepository.save(history);
        
        log.info("Order {} payment status updated to PAID", message.getOrderId());
        
        // Send WebSocket notification to user
        webSocketNotificationService.sendPaymentSuccessNotification(
                order.getUserId(), 
                order.getOrderNumber(), 
                message.getTransactionId()
        );
        
        // Publish order.paid event to Warehouse Service
        messagePublisher.publishOrderPaidEvent(order);
    }
    
    /**
     * Handle failed payment
     */
    private void handlePaymentFailure(PaymentNotificationMessage message) {
        log.warn("Processing payment failure for order: {}", message.getOrderId());
        
        Optional<Order> orderOpt = orderRepository.findByOrderNumber(message.getOrderId());
        
        if (orderOpt.isEmpty()) {
            log.error("Order not found: {}", message.getOrderId());
            return;
        }
        
        Order order = orderOpt.get();
        
        // Update order payment status
        order.setPaymentStatus("FAILED");
        order.setStatus("CANCELLED");
        orderRepository.save(order);
        
        // Create status history
        OrderStatusHistory history = OrderStatusHistory.builder()
                .orderId(order.getId())
                .status("CANCELLED")
                .notes("Payment failed via message queue: " + message.getMessage())
                .build();
        orderStatusHistoryRepository.save(history);
        
        log.warn("Order {} cancelled due to payment failure", message.getOrderId());
        
        // Publish order.cancelled event to release inventory
        messagePublisher.publishOrderCancelledEvent(order, "Payment failed");
    }
}

