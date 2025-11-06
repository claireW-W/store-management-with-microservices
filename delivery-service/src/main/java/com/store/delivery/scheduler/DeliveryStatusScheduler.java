package com.store.delivery.scheduler;

import com.store.delivery.dto.DeliveryStatusUpdate;
import com.store.delivery.entity.Delivery;
import com.store.delivery.entity.DeliveryStatus;
import com.store.delivery.repository.DeliveryRepository;
import com.store.delivery.repository.DeliveryStatusHistoryRepository;
import com.store.delivery.entity.DeliveryStatusHistory;
import com.store.delivery.service.DeliveryService;
import com.store.delivery.service.MessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryStatusScheduler {
    
    private final DeliveryRepository deliveryRepository;
    private final DeliveryService deliveryService;
    private final DeliveryStatusHistoryRepository deliveryStatusHistoryRepository;
    private final MessageProducer messageProducer;
    
    @Scheduled(fixedDelay = 5000) // Execute every 5 seconds
    @Transactional
    public void autoUpdateDeliveryStatus() {
        log.debug("Starting automatic delivery status update");
        
        try {
            // Query all active deliveries
            List<DeliveryStatus> activeStatuses = List.of(
                    DeliveryStatus.PENDING_PICKUP,
                    DeliveryStatus.PICKED_UP,
                    DeliveryStatus.IN_TRANSIT
            );
            
            List<Delivery> activeDeliveries = deliveryRepository.findByStatusIn(activeStatuses);
            
            log.debug("Found {} active deliveries to process", activeDeliveries.size());
            
            for (Delivery delivery : activeDeliveries) {
                processDeliveryStatusUpdate(delivery);
            }
            
        } catch (Exception e) {
            log.error("Error in automatic delivery status update", e);
        }
    }
    
    private void processDeliveryStatusUpdate(Delivery delivery) {
        try {
            // Enforce 5s dwell-time since last update before advancing
            LocalDateTime lastUpdated = delivery.getUpdatedAt() != null ? delivery.getUpdatedAt() : delivery.getCreatedAt();
            if (lastUpdated != null) {
                long secondsSinceLastUpdate = Duration.between(lastUpdated, LocalDateTime.now()).getSeconds();
                if (secondsSinceLastUpdate < 5) {
                    return; // Not yet eligible to advance
                }
            }
            DeliveryStatus currentStatus = delivery.getStatus();
            DeliveryStatus newStatus = determineNextStatus(currentStatus, delivery);
            
            if (newStatus != currentStatus) {
                log.info("Auto-updating delivery {} from {} to {}", 
                        delivery.getDeliveryId(), currentStatus, newStatus);
                
                // Update status
                delivery.setStatus(newStatus);
                updateTimestamps(delivery, newStatus);
                deliveryRepository.save(delivery);
                
                // Record status history
                recordStatusHistory(delivery, newStatus, "Automatic status update");
                
                // Send RabbitMQ message
                DeliveryStatusUpdate statusUpdate = new DeliveryStatusUpdate(
                        delivery.getDeliveryId(),
                        delivery.getOrderId(),
                        newStatus.name(),
                        null,
                        "Automatic status update",
                        LocalDateTime.now()
                );
                messageProducer.sendStatusUpdate(statusUpdate);
                
                log.info("Successfully auto-updated delivery {} to {}", 
                        delivery.getDeliveryId(), newStatus);
            }
            
        } catch (Exception e) {
            log.error("Error processing delivery {}: {}", delivery.getDeliveryId(), e.getMessage(), e);
        }
    }
    
    private DeliveryStatus determineNextStatus(DeliveryStatus currentStatus, Delivery delivery) {
        switch (currentStatus) {
            case PENDING_PICKUP:
                return DeliveryStatus.PICKED_UP;
                
            case PICKED_UP:
                return DeliveryStatus.IN_TRANSIT;
                
            case IN_TRANSIT:
                // Determine success or loss based on configured failure rate
                if (deliveryService.shouldFail()) {
                    log.info("Delivery {} marked as LOST due to configured loss probability",
                            delivery.getDeliveryId());
                    return DeliveryStatus.LOST;
                } else {
                    return DeliveryStatus.DELIVERED;
                }
                
            default:
                return currentStatus; // No update needed
        }
    }
    
    private void updateTimestamps(Delivery delivery, DeliveryStatus newStatus) {
        LocalDateTime now = LocalDateTime.now();
        
        switch (newStatus) {
            case PICKED_UP:
                delivery.setActualPickup(now);
                break;
            case DELIVERED:
                delivery.setActualDelivery(now);
                break;
            case FAILED:
                // Failure time recorded in status history
                break;
        }
    }
    
    private void recordStatusHistory(Delivery delivery, DeliveryStatus status, String notes) {
        log.debug("Recording status history for delivery {}: {}", delivery.getDeliveryId(), status);
        DeliveryStatusHistory history = new DeliveryStatusHistory();
        history.setDelivery(delivery);
        history.setStatus(status);
        history.setNotes(notes);
        history.setCreatedAt(LocalDateTime.now());
        deliveryStatusHistoryRepository.save(history);
    }
}
