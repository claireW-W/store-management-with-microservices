package com.store.delivery.messaging;

import com.store.delivery.entity.Delivery;
import com.store.delivery.entity.DeliveryStatus;
import com.store.delivery.entity.DeliveryStatusHistory;
import com.store.delivery.repository.DeliveryRepository;
import com.store.delivery.repository.DeliveryStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCancellationListener {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryStatusHistoryRepository historyRepository;

    @RabbitListener(queues = "delivery.order.cancel.queue")
    @Transactional
    public void handleOrderCancelled(Map<String, Object> payload) {
        String orderId = (String) payload.get("orderId");
        String deliveryId = (String) payload.get("deliveryId");
        log.info("Received order.cancelled for orderId={}, deliveryId={}", orderId, deliveryId);

        Optional<Delivery> deliveryOpt = Optional.empty();
        if (deliveryId != null) {
            deliveryOpt = deliveryRepository.findByDeliveryId(deliveryId);
        }
        if (deliveryOpt.isEmpty() && orderId != null) {
            // Some repos return a list for orderId; pick the most recent if present
            try {
                List<Delivery> deliveries = deliveryRepository.findByOrderId(orderId);
                if (deliveries != null && !deliveries.isEmpty()) {
                    deliveryOpt = Optional.of(deliveries.get(0));
                }
            } catch (Exception ignore) {
                // If repository signature differs, just leave as empty
            }
        }

        if (deliveryOpt.isEmpty()) {
            log.warn("No delivery found to cancel for orderId={}, deliveryId={}", orderId, deliveryId);
            return;
        }

        Delivery delivery = deliveryOpt.get();
        // Mark as FAILED to take it out from active set
        delivery.setStatus(DeliveryStatus.FAILED);
        delivery.setNotes("Stopped due to order cancellation");
        deliveryRepository.save(delivery);

        DeliveryStatusHistory history = new DeliveryStatusHistory();
        history.setDelivery(delivery);
        history.setStatus(DeliveryStatus.FAILED);
        history.setNotes("Order cancelled by store");
        history.setCreatedAt(LocalDateTime.now());
        historyRepository.save(history);
    }
}


