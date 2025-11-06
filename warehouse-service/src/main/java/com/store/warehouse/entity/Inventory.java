package com.store.warehouse.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Inventory Entity
 */
@Entity
@Table(name = "inventory", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"warehouse_id", "product_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity = 0;
    
    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0;
    
    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity = 0;
    
    @Column(name = "reorder_level")
    private Integer reorderLevel = 10;
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}

