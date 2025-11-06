package com.store.store.messaging;

import com.store.store.dto.DeliveryNotificationMessage;
import com.store.store.model.Order;
import com.store.store.model.OrderStatusHistory;
import com.store.store.repository.OrderRepository;
import com.store.store.repository.OrderStatusHistoryRepository;
import com.store.store.websocket.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryStatusUpdater {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final WebSocketNotificationService webSocketNotificationService;

    public static class OrderNotReadyException extends RuntimeException {
        public OrderNotReadyException(String orderNumber) {
            super("Order not ready for consumption: " + orderNumber);
        }
    }

    @Retryable(
            value = OrderNotReadyException.class,
            maxAttempts = 8,
            backoff = @Backoff(delay = 500, multiplier = 1.5)
    )
    @Transactional
    public void updateOrderStatus(DeliveryNotificationMessage message, String status, String notes) {
        Optional<Order> orderOpt = orderRepository.findByOrderNumber(message.getOrderId());
        if (orderOpt.isEmpty()) {
            log.warn("Order not found yet (will retry): {}", message.getOrderId());
            throw new OrderNotReadyException(message.getOrderId());
        }

        Order order = orderOpt.get();

        // Guard 1: Ignore any updates for terminal orders
        if ("CANCELLED".equals(order.getStatus()) || "REFUNDED".equals(order.getStatus()) || "LOST".equals(order.getStatus())) {
            log.info("Ignoring delivery update for terminal order {} (current status: {})", order.getOrderNumber(), order.getStatus());
            return;
        }

        // Guard 2: Prevent cross-order updates when deliveryId does not match
        if (message.getDeliveryId() != null && order.getDeliveryId() != null && !message.getDeliveryId().equals(order.getDeliveryId())) {
            log.warn("Ignoring delivery update due to deliveryId mismatch for order {}: msg={}, order={}",
                    order.getOrderNumber(), message.getDeliveryId(), order.getDeliveryId());
            return;
        }

        // Idempotency: if status is same, avoid duplicate writes
        if (status.equals(order.getStatus())) {
            log.info("Order {} already in status {}, idempotent skip", order.getOrderNumber(), status);
        } else {
            order.setStatus(status);
            if (message.getDeliveryId() != null) {
                order.setDeliveryId(message.getDeliveryId());
            }
            orderRepository.save(order);
        }

        OrderStatusHistory history = OrderStatusHistory.builder()
                .orderId(order.getId())
                .status(status)
                .notes(notes)
                .build();
        orderStatusHistoryRepository.save(history);

        webSocketNotificationService.sendDeliveryUpdateNotification(
                order.getUserId(),
                order.getOrderNumber(),
                status,
                message.getTrackingNumber(),
                message.getMessage()
        );

        log.info("Order {} status handled: {}", order.getOrderNumber(), status);
    }

    @Recover
    public void recover(OrderNotReadyException ex, DeliveryNotificationMessage message, String status, String notes) {
        log.error("Dropping delivery notification after retries. orderId={}, status={}, reason={}",
                message.getOrderId(), status, ex.getMessage());
    }
}


