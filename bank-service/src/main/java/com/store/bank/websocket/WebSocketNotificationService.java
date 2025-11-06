package com.store.bank.websocket;

import com.store.bank.dto.BalanceUpdateNotification;
import com.store.bank.dto.TransactionNotification;
import com.store.bank.model.CustomerAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * WebSocket Notification Service
 * Sends real-time notifications to connected clients via WebSocket
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * Send balance update notification to specific user
     */
    public void sendBalanceUpdate(String customerId, 
                                  CustomerAccount account,
                                  BigDecimal oldBalance,
                                  String changeType,
                                  String transactionId,
                                  String reason) {
        
        log.info("Sending balance update via WebSocket to customer: {}", customerId);
        
        try {
            BigDecimal changeAmount = account.getBalance().subtract(oldBalance);
            
            BalanceUpdateNotification notification = BalanceUpdateNotification.builder()
                    .customerId(customerId)
                    .accountNumber(account.getAccountNumber())
                    .oldBalance(oldBalance)
                    .newBalance(account.getBalance())
                    .changeAmount(changeAmount.abs())
                    .changeType(changeType)
                    .transactionId(transactionId)
                    .reason(reason)
                    .currency(account.getCurrency())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            // Send to specific user's queue
            messagingTemplate.convertAndSendToUser(
                    customerId,
                    "/queue/balance",
                    notification
            );
            
            log.info("Balance update notification sent successfully");
            
        } catch (Exception e) {
            log.error("Failed to send balance update notification", e);
            // Don't throw - notification failure shouldn't affect transaction
        }
    }
    
    /**
     * Send transaction notification to specific user
     */
    public void sendTransactionNotification(String customerId,
                                           String transactionId,
                                           String transactionType,
                                           BigDecimal amount,
                                           String currency,
                                           String status,
                                           String orderId,
                                           String message) {
        
        log.info("Sending transaction notification via WebSocket to customer: {}", customerId);
        
        try {
            TransactionNotification notification = TransactionNotification.builder()
                    .transactionId(transactionId)
                    .customerId(customerId)
                    .transactionType(transactionType)
                    .amount(amount)
                    .currency(currency)
                    .status(status)
                    .orderId(orderId)
                    .message(message)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            // Send to specific user's queue
            messagingTemplate.convertAndSendToUser(
                    customerId,
                    "/queue/transactions",
                    notification
            );
            
            // Also broadcast to topic for general monitoring
            messagingTemplate.convertAndSend(
                    "/topic/transactions",
                    notification
            );
            
            log.info("Transaction notification sent successfully");
            
        } catch (Exception e) {
            log.error("Failed to send transaction notification", e);
        }
    }
    
    /**
     * Send payment success notification
     */
    public void sendPaymentSuccess(String customerId,
                                   String transactionId,
                                   BigDecimal amount,
                                   String orderId) {
        
        sendTransactionNotification(
                customerId,
                transactionId,
                "PAYMENT",
                amount,
                "AUD",
                "SUCCESS",
                orderId,
                "Payment processed successfully"
        );
    }
    
    /**
     * Send refund success notification
     */
    public void sendRefundSuccess(String customerId,
                                  String refundId,
                                  BigDecimal amount,
                                  String orderId) {
        
        sendTransactionNotification(
                customerId,
                refundId,
                "REFUND",
                amount,
                "AUD",
                "SUCCESS",
                orderId,
                "Refund processed successfully"
        );
    }
    
    /**
     * Send payment failure notification
     */
    public void sendPaymentFailure(String customerId,
                                   String orderId,
                                   String reason) {
        
        sendTransactionNotification(
                customerId,
                null,
                "PAYMENT",
                null,
                "AUD",
                "FAILED",
                orderId,
                "Payment failed: " + reason
        );
    }
}

