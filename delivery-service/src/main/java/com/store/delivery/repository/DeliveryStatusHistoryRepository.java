package com.store.delivery.repository;

import com.store.delivery.entity.DeliveryStatusHistory;
import com.store.delivery.entity.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryStatusHistoryRepository extends JpaRepository<DeliveryStatusHistory, Long> {
    
    List<DeliveryStatusHistory> findByDeliveryId(Long deliveryId);
    
    List<DeliveryStatusHistory> findByStatus(DeliveryStatus status);
    
    @Query("SELECT h FROM DeliveryStatusHistory h WHERE h.delivery.id = :deliveryId ORDER BY h.createdAt DESC")
    List<DeliveryStatusHistory> findByDeliveryIdOrderByCreatedAtDesc(@Param("deliveryId") Long deliveryId);
}
