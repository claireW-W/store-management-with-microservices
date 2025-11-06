package com.store.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LostPackageResponse {
    
    private String deliveryId;
    private String orderId;
    private String customerId;
    private String trackingNumber;
    private String reason;
    private LocalDateTime failedAt;
    private boolean success;
    private String message;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LostPackageListResponse {
        private List<LostPackageResponse> lostPackages;
        private int totalCount;
    }
}
