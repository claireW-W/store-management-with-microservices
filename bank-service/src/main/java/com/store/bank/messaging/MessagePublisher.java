package com.store.bank.messaging;

import com.store.bank.config.RabbitMQConfig;
import com.store.bank.dto.PaymentNotification;
import com.store.bank.dto.PaymentResponse;
import com.store.bank.dto.RefundResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Message Publisher for sending messages to RabbitMQ
 * Used to publish payment and refund notifications to other services
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessagePublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    /**
     * Publish payment success notification
     * Other services (Email, Store Backend) can listen to this
     */
    public void publishPaymentSuccess(PaymentResponse response) {
        log.info("Publishing payment success notification: transactionId={}", response.getTransactionId());
        
        try {
            PaymentNotification notification = PaymentNotification.builder()
                    .transactionId(response.getTransactionId())
                    .orderId(response.getOrderId())
                    .customerId(response.getCustomerId())
                    .amount(response.getAmount())
                    .currency(response.getCurrency())
                    .status(response.getStatus().name())
                    .processedAt(response.getProcessedAt())
                    .message(response.getMessage())
                    .build();
            
            // Publish to bank exchange with routing key
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.BANK_EXCHANGE,
                    "bank.payment.success",
                    notification
            );
            
            log.info("Payment notification published successfully");
            
        } catch (Exception e) {
            log.error("Failed to publish payment notification", e);
            // Don't throw exception - notification failure shouldn't affect payment
        }
    }
    
    /**
     * Publish refund success notification
     * Email service can listen to this to send refund confirmation emails
     */
    public void publishRefundSuccess(RefundResponse response) {
        log.info("Publishing refund success notification: refundId={}", response.getRefundId());
        
        try {
            PaymentNotification notification = PaymentNotification.builder()
                    .transactionId(response.getRefundId())
                    .orderId(response.getOrderId())
                    .customerId(null) // Can be added if needed
                    .amount(response.getRefundAmount())
                    .currency(response.getCurrency())
                    .status(response.getStatus().name())
                    .processedAt(response.getProcessedAt())
                    .message(response.getMessage())
                    .build();
            
            // Publish to bank exchange with routing key
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.BANK_EXCHANGE,
                    "bank.refund.success",
                    notification
            );
            
            log.info("Refund notification published successfully");
            
        } catch (Exception e) {
            log.error("Failed to publish refund notification", e);
        }
    }
    
    /**
     * Publish payment failure notification
     */
    public void publishPaymentFailure(String orderId, String customerId, String reason) {
        log.info("Publishing payment failure notification: orderId={}", orderId);
        
        try {
            PaymentNotification notification = PaymentNotification.builder()
                    .orderId(orderId)
                    .customerId(customerId)
                    .status("FAILED")
                    .message(reason)
                    .build();
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.BANK_EXCHANGE,
                    "bank.payment.failed",
                    notification
            );
            
            log.info("Payment failure notification published");
            
        } catch (Exception e) {
            log.error("Failed to publish payment failure notification", e);
        }
    }
}

