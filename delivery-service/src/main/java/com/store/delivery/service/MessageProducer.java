package com.store.delivery.service;

import com.store.delivery.dto.DeliveryStatusUpdate;
import com.store.delivery.dto.DeliveryNotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageProducer {
    
    private final RabbitTemplate rabbitTemplate;
    
    @Value("${rabbitmq.exchanges.delivery:delivery.exchange}")
    private String deliveryExchange;
    
    public void sendStatusUpdate(DeliveryStatusUpdate update) {
        try {
            String routingKey = mapStatusToRoutingKey(update.getStatus());
            log.info("Sending delivery status update via exchange={}, routingKey={}: {}",
                    deliveryExchange, routingKey, update);
            rabbitTemplate.convertAndSend(deliveryExchange, routingKey, buildNotificationPayload(update));
            log.info("Successfully sent status update for delivery: {}", update.getDeliveryId());
        } catch (Exception e) {
            log.warn("Failed to send status update for delivery: {} - RabbitMQ may not be available: {}", 
                    update.getDeliveryId(), e.getMessage());
            // Don't throw exception - just log warning
        }
    }

    public void sendCreatedEvent(DeliveryStatusUpdate update) {
        try {
            String routingKey = "delivery.status.pending_pickup";
            log.info("Sending delivery created event via exchange={}, routingKey={}: {}",
                    deliveryExchange, routingKey, update);
            rabbitTemplate.convertAndSend(deliveryExchange, routingKey, buildNotificationPayload(update));
        } catch (Exception e) {
            log.warn("Failed to send created event for delivery {}: {}", update.getDeliveryId(), e.getMessage());
        }
    }

    private String mapStatusToRoutingKey(String status) {
        if (status == null) return "delivery.status.unknown";
        switch (status) {
            case "PENDING_PICKUP":
            case "CREATED":
                return "delivery.status.pending_pickup";
            case "PICKED_UP":
                return "delivery.status.picked_up";
            case "IN_TRANSIT":
                return "delivery.status.in_transit";
            case "DELIVERED":
                return "delivery.status.delivered";
            case "FAILED":
                return "delivery.status.failed";
            case "LOST":
                return "delivery.status.lost";
            default:
                return "delivery.status." + status.toLowerCase();
        }
    }

    private DeliveryNotificationMessage buildNotificationPayload(DeliveryStatusUpdate update) {
        return DeliveryNotificationMessage.builder()
                .deliveryId(update.getDeliveryId())
                .orderId(update.getOrderId())
                .customerId(null)
                .status(update.getStatus())
                .message(update.getNotes())
                .trackingNumber(null)
                .carrier(null)
                .estimatedDelivery(null)
                .actualDelivery(null)
                .timestamp(update.getTimestamp())
                .build();
    }
}
