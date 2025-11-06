package com.store.delivery.messaging;

import com.store.delivery.config.RabbitMQConfig;
import com.store.delivery.dto.DeliveryRequest;
import com.store.delivery.dto.DeliveryResponse;
import com.store.delivery.dto.OrderPaidMessage;
import com.store.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderMessageListener {

    private final DeliveryService deliveryService;

    /**
     * Listen to order paid events and automatically create delivery
     */
    @RabbitListener(queues = RabbitMQConfig.DELIVERY_ORDER_PAID_QUEUE)
    public void handleOrderPaid(OrderPaidMessage message) {
        log.info("Received order paid message: orderId={}, orderNumber={}", 
                message.getOrderId(), message.getOrderNumber());

        try {
            // Automatically create delivery for the paid order
            DeliveryRequest deliveryRequest = new DeliveryRequest();
            deliveryRequest.setOrderId(message.getOrderId());
            deliveryRequest.setCustomerId(message.getCustomerId());
            deliveryRequest.setShippingAddress(message.getShippingAddress());
            deliveryRequest.setWarehouseId(message.getWarehouseId() != null ? Long.parseLong(message.getWarehouseId()) : 1L);
            deliveryRequest.setCarrier(message.getCarrier() != null ? message.getCarrier() : "Standard Express");
            deliveryRequest.setNotes("Auto-created from paid order: " + message.getOrderNumber());

            DeliveryResponse deliveryResponse = deliveryService.createDelivery(deliveryRequest);
            
            log.info("Successfully created delivery {} for paid order {}", 
                    deliveryResponse.getDeliveryId(), message.getOrderId());
        } catch (Exception e) {
            log.error("Failed to create delivery for paid order {}: {}", 
                    message.getOrderId(), e.getMessage(), e);
            // In production, you might want to send to a Dead Letter Queue here
        }
    }
}

