package com.store.bank.dto;

import com.store.bank.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {
    
    private String refundId;
    private String originalTransactionId;
    private String orderId;
    private BigDecimal refundAmount;
    private String currency;
    private Transaction.TransactionStatus status;
    private String reason;
    private LocalDateTime processedAt;
    private String message;
}
