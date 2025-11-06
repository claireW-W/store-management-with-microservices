package com.store.bank.messaging;

import com.store.bank.config.RabbitMQConfig;
import com.store.bank.dto.LostPackageRefundRequest;
import com.store.bank.dto.RefundMessage;
import com.store.bank.dto.RefundResponse;
import com.store.bank.service.BankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ Message Listener for Refund Requests
 * Listens to the refund queue and processes refund messages asynchronously
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RefundMessageListener {
    
    private final BankService bankService;
    private final MessagePublisher messagePublisher;
    
    /**
     * Handle refund messages from the queue
     * This method is triggered automatically when a message arrives
     */
    @RabbitListener(queues = RabbitMQConfig.REFUND_QUEUE)
    public void handleRefundRequest(RefundMessage message) {
        log.info("Received refund message from queue: {}", message);
        
        try {
            // Process different types of refund messages
            RefundResponse response = processRefundMessage(message);
            
            log.info("Refund processed successfully: transactionId={}, orderId={}", 
                    response.getRefundId(), response.getOrderId());
            
            // Publish success notification
            messagePublisher.publishRefundSuccess(response);
            
        } catch (Exception e) {
            log.error("Failed to process refund message: {}", message, e);
            // In production, you might want to:
            // 1. Send to a dead letter queue
            // 2. Retry with exponential backoff
            // 3. Alert monitoring system
        }
    }
    
    /**
     * Process refund message based on message type
     */
    private RefundResponse processRefundMessage(RefundMessage message) {
        log.info("Processing refund type: {}", message.getMessageType());
        
        // Build refund request
        LostPackageRefundRequest request = LostPackageRefundRequest.builder()
                .orderId(message.getOrderId())
                .customerId(message.getCustomerId())
                .refundAmount(message.getAmount())
                .currency(message.getCurrency())
                .deliveryId(message.getDeliveryId())
                .lostReason(message.getReason())
                .build();
        
        // Process the refund using existing service
        return bankService.processLostPackageRefund(request);
    }
}

