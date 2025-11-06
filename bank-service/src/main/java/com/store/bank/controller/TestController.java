package com.store.bank.controller;

import com.store.bank.config.RabbitMQConfig;
import com.store.bank.dto.RefundMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Test Controller for testing RabbitMQ and WebSocket functionality
 * This controller is for development and testing purposes only
 * Remove or disable in production
 */
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TestController {
    
    private final RabbitTemplate rabbitTemplate;
    
    /**
     * Test RabbitMQ message publishing
     * POST /api/test/send-refund-message
     */
    @PostMapping("/send-refund-message")
    public ResponseEntity<String> sendTestRefundMessage(
            @RequestParam(defaultValue = "ORD-12345") String orderId,
            @RequestParam(defaultValue = "CUST-001") String customerId,
            @RequestParam(defaultValue = "100.00") BigDecimal amount) {
        
        log.info("Sending test refund message to queue");
        
        try {
            RefundMessage message = RefundMessage.builder()
                    .orderId(orderId)
                    .customerId(customerId)
                    .amount(amount)
                    .currency("AUD")
                    .deliveryId("DEL-12345")
                    .reason("Package lost during delivery")
                    .messageType("LOST_PACKAGE")
                    .build();
            
            // Send to refund queue
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.REFUND_QUEUE,
                    message
            );
            
            log.info("Test refund message sent successfully");
            
            return ResponseEntity.ok("Test refund message sent to queue. Check logs for processing.");
            
        } catch (Exception e) {
            log.error("Failed to send test message", e);
            return ResponseEntity.internalServerError()
                    .body("Failed to send test message: " + e.getMessage());
        }
    }
    
    /**
     * Test RabbitMQ connection
     * GET /api/test/rabbitmq-connection
     */
    @GetMapping("/rabbitmq-connection")
    public ResponseEntity<String> testRabbitMQConnection() {
        log.info("Testing RabbitMQ connection");
        
        try {
            // Try to send a simple test message
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.BANK_EXCHANGE,
                    "test.connection",
                    "Connection test message"
            );
            
            log.info("RabbitMQ connection is working");
            return ResponseEntity.ok("RabbitMQ connection is working");
            
        } catch (Exception e) {
            log.error("RabbitMQ connection failed", e);
            return ResponseEntity.internalServerError()
                    .body("RabbitMQ connection failed: " + e.getMessage());
        }
    }
    
    /**
     * Health check
     * GET /api/test/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Test Controller is running");
    }
}

