package com.store.warehouse.repository;

import com.store.warehouse.entity.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Inventory Reservation Repository
 */
@Repository
public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {
    
    Optional<InventoryReservation> findByReservationId(String reservationId);
    
    List<InventoryReservation> findByOrderId(String orderId);
    
    List<InventoryReservation> findByStatus(String status);
    
    List<InventoryReservation> findByExpiresAtBeforeAndStatus(LocalDateTime dateTime, String status);
}

