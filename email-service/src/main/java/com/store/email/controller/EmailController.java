package com.store.email.controller;

import com.store.email.EmailRequest;
import com.store.email.EmailResponse;
import com.store.email.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class EmailController {
    
    private final EmailService emailService;
    
    /**
     * Send order confirmation email
     * POST /api/email/order-confirmation
     */
    @PostMapping("/order-confirmation")
    public ResponseEntity<EmailResponse> sendOrderConfirmation(@Valid @RequestBody EmailRequest request) {
        String requestId = java.util.UUID.randomUUID().toString();
        log.info("[{}] [EMAIL_REQUEST] Received order confirmation email request for: {} at {}", 
                requestId, request.getRecipientEmail(), java.time.Instant.now());
        
        try {
            EmailResponse response = emailService.sendOrderConfirmation(request);
            log.info("[{}] [EMAIL_SUCCESS] Order confirmation email sent successfully to: {} at {}", 
                    requestId, request.getRecipientEmail(), java.time.Instant.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[{}] [EMAIL_ERROR] Order confirmation email failed for: {} - Error: {} at {}", 
                    requestId, request.getRecipientEmail(), e.getMessage(), java.time.Instant.now(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Send delivery status update email
     * POST /api/email/delivery-update
     */
    @PostMapping("/delivery-update")
    public ResponseEntity<EmailResponse> sendDeliveryUpdate(@Valid @RequestBody EmailRequest request) {
        log.info("Received delivery update email request for: {}", request.getRecipientEmail());
        
        try {
            EmailResponse response = emailService.sendDeliveryUpdate(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Delivery update email failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Send shipping status update email (alias for delivery-update)
     * POST /api/email/shipping-update
     * This endpoint is provided for compatibility with store-backend service
     * Handles requests that may not include recipientName or content fields
     */
    @PostMapping("/shipping-update")
    public ResponseEntity<EmailResponse> sendShippingUpdate(@RequestBody Map<String, Object> requestMap) {
        log.info("Received shipping update email request");
        
        try {
            // Build EmailRequest with defaults for missing fields
            EmailRequest request = new EmailRequest();
            request.setRecipientEmail((String) requestMap.getOrDefault("recipientEmail", "customer@example.com"));
            request.setRecipientName((String) requestMap.getOrDefault("recipientName", "Customer"));
            request.setSubject((String) requestMap.getOrDefault("subject", "Shipping Update"));
            
            // Generate content if not provided
            String content = (String) requestMap.get("content");
            if (content == null || content.trim().isEmpty()) {
                String orderNumber = (String) requestMap.getOrDefault("orderNumber", "");
                String status = (String) requestMap.getOrDefault("status", "");
                String message = (String) requestMap.getOrDefault("message", "");
                content = String.format("Your order %s status update: %s. %s", 
                    orderNumber, status, message).trim();
            }
            request.setContent(content);
            
            // Set optional fields
            request.setOrderNumber((String) requestMap.get("orderNumber"));
            request.setStatus((String) requestMap.get("status"));
            
            log.info("Sending shipping update email to: {}", request.getRecipientEmail());
            
            // Use the same service method as delivery-update
            EmailResponse response = emailService.sendDeliveryUpdate(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Shipping update email failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Send package lost notification email
     * POST /api/email/package-lost
     */
    @PostMapping("/package-lost")
    public ResponseEntity<EmailResponse> sendPackageLostNotification(@Valid @RequestBody EmailRequest request) {
        log.info("Received package lost notification email request for: {}", request.getRecipientEmail());
        
        try {
            EmailResponse response = emailService.sendPackageLostNotification(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Package lost notification email failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Send refund notification email
     * POST /api/email/refund-notification
     */
    @PostMapping("/refund-notification")
    public ResponseEntity<EmailResponse> sendRefundNotification(@Valid @RequestBody EmailRequest request) {
        log.info("Received refund notification email request for: {}", request.getRecipientEmail());
        
        try {
            EmailResponse response = emailService.sendRefundNotification(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Refund notification email failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
