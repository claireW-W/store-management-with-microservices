package com.store.warehouse.repository;

import com.store.warehouse.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Warehouse Repository
 */
@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    
    Optional<Warehouse> findByWarehouseCode(String warehouseCode);
    
    List<Warehouse> findByIsActiveTrue();
}

