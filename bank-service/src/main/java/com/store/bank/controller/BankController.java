package com.store.bank.controller;

import com.store.bank.dto.*;
import com.store.bank.service.BankService;
import com.store.bank.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bank")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class BankController {
    
    private final BankService bankService;
    private final EmailService emailService;
    
    /**
     * Process payment request
     * POST /api/bank/payment
     */
    @PostMapping("/payment")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("Received payment request for order: {}", request.getOrderId());
        
        try {
            PaymentResponse response = bankService.processPayment(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid payment request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Payment processing error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Process refund request
     * POST /api/bank/refund
     */
    @PostMapping("/refund")
    public ResponseEntity<RefundResponse> processRefund(@Valid @RequestBody RefundRequest request) {
        log.info("Received refund request for order: {}", request.getOrderId());
        
        try {
            RefundResponse response = bankService.processRefund(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid refund request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Refund processing error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Process lost package refund
     * POST /api/bank/lost-package-refund
     */
    @PostMapping("/lost-package-refund")
    public ResponseEntity<RefundResponse> processLostPackageRefund(@Valid @RequestBody LostPackageRefundRequest request) {
        log.info("Received lost package refund request for order: {}", request.getOrderId());
        
        try {
            RefundResponse response = bankService.processLostPackageRefund(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid lost package refund request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Lost package refund processing error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Send order confirmation email
     * POST /api/email/order-confirmation
     */
    @PostMapping("/email/order-confirmation")
    public ResponseEntity<EmailResponse> sendOrderConfirmation(@Valid @RequestBody EmailRequest request) {
        log.info("Received order confirmation email request for: {}", request.getRecipientEmail());
        
        try {
            EmailResponse response = emailService.sendOrderConfirmation(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Order confirmation email failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Send delivery status update email
     * POST /api/email/delivery-update
     */
    @PostMapping("/email/delivery-update")
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
     * Send package lost notification email
     * POST /api/email/package-lost
     */
    @PostMapping("/email/package-lost")
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
    @PostMapping("/email/refund-notification")
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
    
    /**
     * Get customer account balance
     * GET /api/bank/balance/{customerId}
     */
    @GetMapping("/balance/{customerId}")
    public ResponseEntity<?> getBalance(@PathVariable String customerId) {
        log.info("Received balance request for customer: {}", customerId);
        
        try {
            BalanceResponse response = bankService.getBalance(customerId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Balance request failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Balance request error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Health check
     * GET /api/bank/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Bank Service is running");
    }
}
