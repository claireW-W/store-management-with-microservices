package com.store.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Inventory Response DTO
 * Response for GET /api/warehouse/inventory/{productId}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {
    
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private Long productId;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer totalQuantity;
}

