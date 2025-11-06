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
public class PaymentResponse {
    
    private String transactionId;
    private String orderId;
    private String customerId;
    private BigDecimal amount;
    private String currency;
    private Transaction.TransactionStatus status;
    private String reference;
    private LocalDateTime processedAt;
    private String message;
}
