package com.store.store.repository;

import com.store.store.model.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Order Status History Repository
 */
@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    
    List<OrderStatusHistory> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}

