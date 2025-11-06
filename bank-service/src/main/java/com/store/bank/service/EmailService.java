package com.store.bank.service;

import com.store.bank.dto.EmailRequest;
import com.store.bank.dto.EmailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.from:noreply@store.com}")
    private String fromEmail;
    
    /**
     * Send order confirmation email
     */
    public EmailResponse sendOrderConfirmation(EmailRequest request) {
        log.info("Sending order confirmation email to: {}", request.getRecipientEmail());
        
        String subject = "Order Confirmation - " + request.getOrderNumber();
        String content = buildOrderConfirmationContent(request);
        
        return sendEmail(request.getRecipientEmail(), request.getRecipientName(), subject, content);
    }
    
    /**
     * Send delivery status update email
     */
    public EmailResponse sendDeliveryUpdate(EmailRequest request) {
        log.info("Sending delivery update email to: {}", request.getRecipientEmail());
        
        String subject = "Delivery Status Update - " + request.getOrderNumber();
        String content = buildDeliveryUpdateContent(request);
        
        return sendEmail(request.getRecipientEmail(), request.getRecipientName(), subject, content);
    }
    
    /**
     * Send package lost notification email
     */
    public EmailResponse sendPackageLostNotification(EmailRequest request) {
        log.info("Sending package lost notification email to: {}", request.getRecipientEmail());
        
        String subject = "Package Lost Notification - " + request.getOrderNumber();
        String content = buildPackageLostContent(request);
        
        return sendEmail(request.getRecipientEmail(), request.getRecipientName(), subject, content);
    }
    
    /**
     * Send refund notification email
     */
    public EmailResponse sendRefundNotification(EmailRequest request) {
        log.info("Sending refund notification email to: {}", request.getRecipientEmail());
        
        String subject = "Refund Processing Notification - " + request.getOrderNumber();
        String content = buildRefundNotificationContent(request);
        
        return sendEmail(request.getRecipientEmail(), request.getRecipientName(), subject, content);
    }
    
    /**
     * Generic email sending method
     */
    private EmailResponse sendEmail(String recipientEmail, String recipientName, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(recipientEmail);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            
            log.info("Email sent successfully to: {}", recipientEmail);
            
            return EmailResponse.builder()
                    .emailId(UUID.randomUUID().toString())
                    .recipientEmail(recipientEmail)
                    .subject(subject)
                    .status("SENT")
                    .sentAt(LocalDateTime.now())
                    .message("Email sent successfully")
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to send email to: {}", recipientEmail, e);
            
            return EmailResponse.builder()
                    .emailId(UUID.randomUUID().toString())
                    .recipientEmail(recipientEmail)
                    .subject(subject)
                    .status("FAILED")
                    .sentAt(LocalDateTime.now())
                    .message("Email sending failed: " + e.getMessage())
                    .build();
        }
    }
    
    private String buildOrderConfirmationContent(EmailRequest request) {
        return String.format("""
                Dear %s,
                
                Your order %s has been confirmed.
                
                Thank you for your purchase!
                
                Best regards,
                Store Online Shop Team
                """, 
                request.getRecipientName() != null ? request.getRecipientName() : "Customer",
                request.getOrderNumber());
    }
    
    private String buildDeliveryUpdateContent(EmailRequest request) {
        return String.format("""
                Dear %s,
                
                The delivery status of your order %s has been updated.
                
                Please check the latest delivery information.
                
                Best regards,
                Store Online Shop Team
                """, 
                request.getRecipientName() != null ? request.getRecipientName() : "Customer",
                request.getOrderNumber());
    }
    
    private String buildPackageLostContent(EmailRequest request) {
        return String.format("""
                Dear %s,
                
                We regret to inform you that the package for your order %s was lost during delivery.
                
                We will immediately process a full refund for you.
                
                We sincerely apologize for the inconvenience caused.
                
                Best regards,
                Store Online Shop Team
                """, 
                request.getRecipientName() != null ? request.getRecipientName() : "Customer",
                request.getOrderNumber());
    }
    
    private String buildRefundNotificationContent(EmailRequest request) {
        return String.format("""
                Dear %s,
                
                The refund for your order %s has been processed.
                
                The refund amount will be returned to your payment account.
                
                Thank you for your understanding.
                
                Best regards,
                Store Online Shop Team
                """, 
                request.getRecipientName() != null ? request.getRecipientName() : "Customer",
                request.getOrderNumber());
    }
}
