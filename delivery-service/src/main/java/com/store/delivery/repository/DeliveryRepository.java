package com.store.delivery.repository;

import com.store.delivery.entity.Delivery;
import com.store.delivery.entity.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    
    Optional<Delivery> findByDeliveryId(String deliveryId);
    
    List<Delivery> findByOrderId(String orderId);
    
    List<Delivery> findByCustomerId(String customerId);
    
    List<Delivery> findByStatus(DeliveryStatus status);
    
    @Query("SELECT d FROM Delivery d WHERE d.status IN :statuses")
    List<Delivery> findByStatusIn(@Param("statuses") List<DeliveryStatus> statuses);
    
    @Query("SELECT d FROM Delivery d WHERE d.status IN ('FAILED','LOST')")
    List<Delivery> findLostPackages();
    
    Optional<Delivery> findByTrackingNumber(String trackingNumber);
}
