package com.store.warehouse.repository;

import com.store.warehouse.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Inventory Repository
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    
    List<Inventory> findByProductId(Long productId);
    
    List<Inventory> findByWarehouseId(Long warehouseId);
    
    Optional<Inventory> findByWarehouseIdAndProductId(Long warehouseId, Long productId);
}

