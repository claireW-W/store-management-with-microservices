package com.store.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * WebSocket notification for real-time balance updates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceUpdateNotification {
    
    private String customerId;
    private String accountNumber;
    private BigDecimal oldBalance;
    private BigDecimal newBalance;
    private BigDecimal changeAmount;
    private String changeType; // DEBIT, CREDIT
    private String transactionId;
    private String reason;
    private String currency;
    private LocalDateTime timestamp;
}

