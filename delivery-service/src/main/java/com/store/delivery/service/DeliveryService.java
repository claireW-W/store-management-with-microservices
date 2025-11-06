package com.store.delivery.service;

import com.store.delivery.dto.*;
import com.store.delivery.entity.Delivery;
import com.store.delivery.entity.DeliveryStatus;
import com.store.delivery.entity.DeliveryStatusHistory;
import com.store.delivery.entity.ShippingAddress;
import com.store.delivery.repository.DeliveryRepository;
import com.store.delivery.repository.DeliveryStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {
    
    private final DeliveryRepository deliveryRepository;
    private final DeliveryStatusHistoryRepository statusHistoryRepository;
    private final MessageProducer messageProducer;
    
    @Value("${delivery.failure-rate:1}")
    private double failureRate;
    
    @Value("${delivery.tracking-number-prefix:DEL}")
    private String trackingNumberPrefix;
    
    private final Random random = new Random();
    
    @Transactional
    public DeliveryResponse createDelivery(DeliveryRequest request) {
        log.info("Creating delivery for order: {}", request.getOrderId());
        
        Delivery delivery = new Delivery();
        delivery.setDeliveryId(generateDeliveryId());
        delivery.setOrderId(request.getOrderId());
        delivery.setCustomerId(request.getCustomerId());
        // Convert String address to ShippingAddress object
        ShippingAddress shippingAddress = new ShippingAddress(request.getShippingAddress());
        delivery.setShippingAddress(shippingAddress);
        delivery.setPickupWarehouseId(request.getWarehouseId());
        delivery.setCarrier(request.getCarrier());
        delivery.setNotes(request.getNotes());
        delivery.setStatus(DeliveryStatus.PENDING_PICKUP);
        delivery.setTrackingNumber(generateTrackingNumber());
        delivery.setEstimatedPickup(LocalDateTime.now().plusHours(1));
        delivery.setEstimatedDelivery(LocalDateTime.now().plusDays(2));
        
        Delivery savedDelivery = deliveryRepository.save(delivery);
        
        // Record status history
        recordStatusHistory(savedDelivery, DeliveryStatus.PENDING_PICKUP, "Delivery created");
        
        log.info("Successfully created delivery: {}", savedDelivery.getDeliveryId());
        
        // Publish created event to Store via RabbitMQ
        try {
            DeliveryStatusUpdate created = new DeliveryStatusUpdate(
                    savedDelivery.getDeliveryId(),
                    savedDelivery.getOrderId(),
                    "CREATED",
                    null,
                    "Delivery created",
                    LocalDateTime.now()
            );
            messageProducer.sendCreatedEvent(created);
        } catch (Exception ignored) {}

        // Do not immediately set or emit PICKED_UP; scheduler will advance after 10s dwell-time

        return convertToResponse(savedDelivery);
    }
    
    @Transactional
    public StatusUpdateResponse updateStatus(String deliveryId, UpdateStatusRequest request) {
        log.info("Updating status for delivery: {} to {}", deliveryId, request.getStatus());
        
        Delivery delivery = deliveryRepository.findByDeliveryId(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));
        
        DeliveryStatus oldStatus = delivery.getStatus();
        delivery.setStatus(request.getStatus());
        
        // Update timestamps
        LocalDateTime now = LocalDateTime.now();
        switch (request.getStatus()) {
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
        
        Delivery savedDelivery = deliveryRepository.save(delivery);
        
        // Record status history
        recordStatusHistory(savedDelivery, request.getStatus(), request.getNotes());
        
        // Send RabbitMQ message
        DeliveryStatusUpdate statusUpdate = new DeliveryStatusUpdate(
                savedDelivery.getDeliveryId(),
                savedDelivery.getOrderId(),
                request.getStatus().name(),
                request.getLocation(),
                request.getNotes(),
                now
        );
        messageProducer.sendStatusUpdate(statusUpdate);
        
        log.info("Successfully updated delivery status: {} -> {}", oldStatus, request.getStatus());
        
        return new StatusUpdateResponse(
                savedDelivery.getDeliveryId(),
                savedDelivery.getOrderId(),
                request.getStatus(),
                request.getLocation(),
                request.getNotes(),
                now,
                true,
                "Status updated successfully"
        );
    }
    
    @Transactional
    public LostPackageResponse handleLostPackage(String deliveryId, LostPackageRequest request) {
        log.info("Handling lost package for delivery: {}", deliveryId);
        
        Delivery delivery = deliveryRepository.findByDeliveryId(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));
        
        delivery.setStatus(DeliveryStatus.FAILED);
        delivery.setNotes(request.getNotes());
        Delivery savedDelivery = deliveryRepository.save(delivery);
        
        // Record status history
        recordStatusHistory(savedDelivery, DeliveryStatus.FAILED, 
                "Package lost: " + request.getReason());
        
        // Send RabbitMQ message
        DeliveryStatusUpdate statusUpdate = new DeliveryStatusUpdate(
                savedDelivery.getDeliveryId(),
                savedDelivery.getOrderId(),
                DeliveryStatus.FAILED.name(),
                request.getLocation(),
                "Package lost: " + request.getReason(),
                LocalDateTime.now()
        );
        messageProducer.sendStatusUpdate(statusUpdate);
        
        log.info("Successfully marked delivery as lost: {}", deliveryId);
        
        return new LostPackageResponse(
                savedDelivery.getDeliveryId(),
                savedDelivery.getOrderId(),
                savedDelivery.getCustomerId(),
                savedDelivery.getTrackingNumber(),
                request.getReason(),
                LocalDateTime.now(),
                true,
                "Package marked as lost"
        );
    }
    
    public List<DeliveryResponse> getLostPackages() {
        log.info("Retrieving all lost packages");
        List<Delivery> lostDeliveries = deliveryRepository.findLostPackages();
        return lostDeliveries.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public ConfigResponse setLostProbability(double probability) {
        log.info("Setting lost probability to: {}", probability);
        
        if (probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException("Probability must be between 0 and 1");
        }
        
        this.failureRate = probability;
        
        log.info("Successfully updated lost probability to: {}", probability);
        
        return new ConfigResponse(
                probability,
                LocalDateTime.now(),
                true,
                "Lost probability updated successfully"
        );
    }
    
    public ConfigResponse getLostProbability() {
        log.info("Retrieving current lost probability: {}", failureRate);
        
        return new ConfigResponse(
                failureRate,
                LocalDateTime.now(),
                true,
                "Current lost probability retrieved"
        );
    }
    
    public boolean shouldFail() {
        return random.nextDouble() < failureRate;
    }
    
    private String generateDeliveryId() {
        return "DEL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private String generateTrackingNumber() {
        return trackingNumberPrefix + "-" + System.currentTimeMillis();
    }
    
    private void recordStatusHistory(Delivery delivery, DeliveryStatus status, String notes) {
        DeliveryStatusHistory history = new DeliveryStatusHistory();
        history.setDelivery(delivery);
        history.setStatus(status);
        history.setNotes(notes);
        history.setCreatedAt(LocalDateTime.now());
        statusHistoryRepository.save(history);
    }
    
    private DeliveryResponse convertToResponse(Delivery delivery) {
        return new DeliveryResponse(
                delivery.getDeliveryId(),
                delivery.getOrderId(),
                delivery.getCustomerId(),
                delivery.getStatus(),
                delivery.getTrackingNumber(),
                delivery.getEstimatedPickup(),
                delivery.getEstimatedDelivery(),
                delivery.getActualPickup(),
                delivery.getActualDelivery(),
                delivery.getCarrier(),
                delivery.getNotes(),
                delivery.getShippingAddress() != null ? delivery.getShippingAddress().getFullAddress() : null,
                delivery.getCreatedAt()
        );
    }
}
