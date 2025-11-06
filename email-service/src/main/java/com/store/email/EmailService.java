package com.store.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    /**
     * Send order confirmation email
     */
    public EmailResponse sendOrderConfirmation(EmailRequest request) {
        log.info("Sending order confirmation email to: {}", request.getRecipientEmail());
        
        // Simulate email sending
        String emailId = UUID.randomUUID().toString();
        log.info("Email sent successfully - ID: {}, recipient: {}, subject: {}", 
                emailId, request.getRecipientEmail(), request.getSubject());
        
        return new EmailResponse(
            true,
            "Order confirmation email sent successfully",
            emailId,
            LocalDateTime.now(),
            request.getRecipientEmail(),
            request.getSubject()
        );
    }
    
    /**
     * Send delivery status update email
     */
    public EmailResponse sendDeliveryUpdate(EmailRequest request) {
        log.info("Sending delivery status update email to: {}", request.getRecipientEmail());
        
        String emailId = UUID.randomUUID().toString();
        log.info("Delivery status update email sent successfully - ID: {}, recipient: {}, status: {}", 
                emailId, request.getRecipientEmail(), request.getStatus());
        
        return new EmailResponse(
            true,
            "Delivery status update email sent successfully",
            emailId,
            LocalDateTime.now(),
            request.getRecipientEmail(),
            request.getSubject()
        );
    }
    
    /**
     * Send package lost notification email
     */
    public EmailResponse sendPackageLostNotification(EmailRequest request) {
        log.info("Sending package lost notification email to: {}", request.getRecipientEmail());
        
        String emailId = UUID.randomUUID().toString();
        log.info("Package lost notification email sent successfully - ID: {}, recipient: {}, order number: {}", 
                emailId, request.getRecipientEmail(), request.getOrderNumber());
        
        return new EmailResponse(
            true,
            "Package lost notification email sent successfully",
            emailId,
            LocalDateTime.now(),
            request.getRecipientEmail(),
            request.getSubject()
        );
    }
    
    /**
     * Send refund notification email
     */
    public EmailResponse sendRefundNotification(EmailRequest request) {
        log.info("Sending refund notification email to: {}", request.getRecipientEmail());
        
        String emailId = UUID.randomUUID().toString();
        log.info("Refund notification email sent successfully - ID: {}, recipient: {}, amount: {}", 
                emailId, request.getRecipientEmail(), request.getAmount());
        
        return new EmailResponse(
            true,
            "Refund notification email sent successfully",
            emailId,
            LocalDateTime.now(),
            request.getRecipientEmail(),
            request.getSubject()
        );
    }
}
