package com.store.warehouse.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Inventory Transaction Entity - Audit log for inventory changes
 */
@Entity
@Table(name = "inventory_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType; // IN, OUT, ADJUSTMENT, RESERVE, RELEASE
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "reference_id", length = 100)
    private String referenceId;
    
    @Column(name = "reference_type", nullable = false, length = 20)
    private String referenceType; // ORDER, RESERVATION, ADJUSTMENT
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

