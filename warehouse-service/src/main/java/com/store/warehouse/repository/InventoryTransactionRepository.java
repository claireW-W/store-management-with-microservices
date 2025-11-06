package com.store.warehouse.repository;

import com.store.warehouse.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Inventory Transaction Repository
 */
@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    
    List<InventoryTransaction> findByWarehouseId(Long warehouseId);
    
    List<InventoryTransaction> findByProductId(Long productId);
    
    List<InventoryTransaction> findByWarehouseIdAndProductIdOrderByCreatedAtDesc(
            Long warehouseId, Long productId);
}

