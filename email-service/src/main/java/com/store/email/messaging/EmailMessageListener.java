package com.store.email.messaging;

import com.store.email.config.RabbitMQConfig;
import com.store.email.dto.*;
import com.store.email.EmailRequest;
import com.store.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailMessageListener {

    private final EmailService emailService;

    /**
     * Listen to order creation events and send confirmation emails
     */
    @RabbitListener(queues = RabbitMQConfig.EMAIL_ORDER_CONFIRMATION_QUEUE)
    public void handleOrderCreated(OrderCreatedMessage message) {
        log.info("Received order created message: orderId={}, customer={}", 
                message.getOrderId(), message.getCustomerEmail());

        try {
            EmailRequest emailRequest = new EmailRequest();
            emailRequest.setRecipientEmail(message.getCustomerEmail());
            emailRequest.setRecipientName(message.getCustomerName());
            emailRequest.setSubject("Order Confirmation - " + message.getOrderNumber());
            emailRequest.setOrderNumber(message.getOrderNumber());
            emailRequest.setAmount(message.getTotalAmount() != null ? message.getTotalAmount().toString() : "0.00");
            
            emailService.sendOrderConfirmation(emailRequest);
            log.info("Order confirmation email sent successfully for order: {}", message.getOrderId());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email for order {}: {}", 
                    message.getOrderId(), e.getMessage(), e);
        }
    }

    /**
     * Listen to payment notifications and send payment status emails
     */
    @RabbitListener(queues = RabbitMQConfig.EMAIL_PAYMENT_NOTIFICATION_QUEUE)
    public void handlePaymentNotification(PaymentNotificationMessage message) {
        log.info("Received payment notification: orderId={}, status={}", 
                message.getOrderId(), message.getStatus());

        try {
            EmailRequest emailRequest = new EmailRequest();
            emailRequest.setRecipientEmail(message.getCustomerEmail());
            emailRequest.setOrderNumber(message.getOrderId());
            emailRequest.setAmount(message.getAmount() != null ? message.getAmount().toString() : "0.00");
            
            if ("SUCCESS".equals(message.getStatus())) {
                emailRequest.setSubject("Payment Successful - Order " + message.getOrderId());
                emailService.sendOrderConfirmation(emailRequest);
                log.info("Payment success email sent for order: {}", message.getOrderId());
            } else {
                emailRequest.setSubject("Payment Failed - Order " + message.getOrderId());
                emailService.sendOrderConfirmation(emailRequest);
                log.info("Payment failure email sent for order: {}", message.getOrderId());
            }
        } catch (Exception e) {
            log.error("Failed to send payment notification email for order {}: {}", 
                    message.getOrderId(), e.getMessage(), e);
        }
    }

    /**
     * Listen to delivery status updates and send notification emails
     */
    @RabbitListener(queues = RabbitMQConfig.EMAIL_DELIVERY_UPDATE_QUEUE)
    public void handleDeliveryStatusUpdate(DeliveryStatusMessage message) {
        log.info("Received delivery status update: orderId={}, status={}", 
                message.getOrderId(), message.getStatus());

        try {
            EmailRequest emailRequest = new EmailRequest();
            emailRequest.setRecipientEmail(message.getCustomerEmail());
            emailRequest.setOrderNumber(message.getOrderNumber());
            emailRequest.setStatus(message.getStatus());
            emailRequest.setSubject("Delivery Update - " + message.getStatus());
            
            if ("LOST".equals(message.getStatus())) {
                emailService.sendPackageLostNotification(emailRequest);
                log.info("Package lost notification email sent for order: {}", message.getOrderId());
            } else {
                emailService.sendDeliveryUpdate(emailRequest);
                log.info("Delivery status update email sent for order: {}", message.getOrderId());
            }
        } catch (Exception e) {
            log.error("Failed to send delivery status email for order {}: {}", 
                    message.getOrderId(), e.getMessage(), e);
        }
    }

    /**
     * Listen to refund notifications and send refund confirmation emails
     */
    @RabbitListener(queues = RabbitMQConfig.EMAIL_REFUND_NOTIFICATION_QUEUE)
    public void handleRefundNotification(RefundNotificationMessage message) {
        log.info("Received refund notification: orderId={}, amount={}", 
                message.getOrderId(), message.getAmount());

        try {
            EmailRequest emailRequest = new EmailRequest();
            emailRequest.setRecipientEmail(message.getCustomerEmail());
            emailRequest.setOrderNumber(message.getOrderId());
            emailRequest.setAmount(message.getAmount() != null ? message.getAmount().toString() : "0.00");
            emailRequest.setSubject("Refund Processed - Order " + message.getOrderId());
            
            emailService.sendRefundNotification(emailRequest);
            log.info("Refund notification email sent successfully for order: {}", message.getOrderId());
        } catch (Exception e) {
            log.error("Failed to send refund notification email for order {}: {}", 
                    message.getOrderId(), e.getMessage(), e);
        }
    }
}

