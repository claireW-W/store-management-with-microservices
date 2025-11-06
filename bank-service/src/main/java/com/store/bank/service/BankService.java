package com.store.bank.service;

import com.store.bank.dto.*;
import com.store.bank.model.*;
import com.store.bank.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BankService {
    
    private final CustomerAccountRepository customerAccountRepository;
    private final StoreAccountRepository storeAccountRepository;
    private final TransactionRepository transactionRepository;
    private final com.store.bank.messaging.MessagePublisher messagePublisher;
    private final com.store.bank.websocket.WebSocketNotificationService webSocketNotificationService;
    
    @Value("${bank.store-account.number}")
    private String storeAccountNumber;
    
    @Value("${bank.default-currency}")
    private String defaultCurrency;
    
    /**
     * Process payment request
     */
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for order: {}", request.getOrderId());
        
        try {
            // Find customer account
            CustomerAccount customerAccount = customerAccountRepository
                    .findActiveByCustomerId(request.getCustomerId())
                    .orElseThrow(() -> new IllegalArgumentException("Customer account not found: " + request.getCustomerId()));
            
            // Find store account
            StoreAccount storeAccount = storeAccountRepository
                    .findActiveByAccountNumber(storeAccountNumber)
                    .orElseThrow(() -> new IllegalStateException("Store account not found: " + storeAccountNumber));
            
            // Check balance
            if (!customerAccount.hasSufficientBalance(request.getAmount())) {
                throw new IllegalArgumentException("Insufficient balance");
            }
            
            // Store old balance for notification
            BigDecimal oldBalance = customerAccount.getBalance();
            
            // Create transaction record
            String transactionId = generateTransactionId();
            Transaction transaction = Transaction.builder()
                    .transactionId(transactionId)
                    .fromAccount(customerAccount.getAccountNumber())
                    .toAccount(storeAccount.getAccountNumber())
                    .fromAccountType(Transaction.AccountType.CUSTOMER)
                    .toAccountType(Transaction.AccountType.STORE)
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .transactionType(Transaction.TransactionType.PAYMENT)
                    .status(Transaction.TransactionStatus.PENDING)
                    .referenceId(request.getOrderId())
                    .referenceType(Transaction.ReferenceType.ORDER)
                    .description(request.getDescription())
                    .build();
            
            transaction = transactionRepository.save(transaction);
            
            // Execute transfer
            customerAccount.debit(request.getAmount());
            storeAccount.credit(request.getAmount());
            
            // Update accounts
            customerAccountRepository.save(customerAccount);
            storeAccountRepository.save(storeAccount);
            
            // Mark transaction as successful
            transaction.markAsSuccess();
            transaction = transactionRepository.save(transaction);
            
            log.info("Payment processed successfully for order: {}, transaction: {}", 
                    request.getOrderId(), transactionId);
            
            PaymentResponse response = PaymentResponse.builder()
                    .transactionId(transactionId)
                    .orderId(request.getOrderId())
                    .customerId(request.getCustomerId())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .status(Transaction.TransactionStatus.SUCCESS)
                    .reference(transactionId)
                    .processedAt(transaction.getProcessedAt())
                    .message("Payment processed successfully")
                    .build();
            
            // Publish payment success notification via RabbitMQ
            messagePublisher.publishPaymentSuccess(response);
            
            // Send real-time WebSocket notification
            webSocketNotificationService.sendBalanceUpdate(
                    request.getCustomerId(),
                    customerAccount,
                    oldBalance,
                    "DEBIT",
                    transactionId,
                    "Payment for order: " + request.getOrderId()
            );
            
            webSocketNotificationService.sendPaymentSuccess(
                    request.getCustomerId(),
                    transactionId,
                    request.getAmount(),
                    request.getOrderId()
            );
            
            return response;
                    
        } catch (Exception e) {
            log.error("Payment processing failed for order: {}", request.getOrderId(), e);
            
            // Publish payment failure notification
            messagePublisher.publishPaymentFailure(
                    request.getOrderId(), 
                    request.getCustomerId(), 
                    e.getMessage()
            );
            
            // Send WebSocket failure notification
            webSocketNotificationService.sendPaymentFailure(
                    request.getCustomerId(),
                    request.getOrderId(),
                    e.getMessage()
            );
            
            throw new RuntimeException("Payment processing failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Process refund request
     */
    public RefundResponse processRefund(RefundRequest request) {
        log.info("Processing refund for order: {}", request.getOrderId());
        
        try {
            // Find original transaction
            Transaction originalTransaction = transactionRepository
                    .findByTransactionId(request.getTransactionId())
                    .orElseThrow(() -> new IllegalArgumentException("Original transaction not found: " + request.getTransactionId()));
            
            // Verify order ID match
            if (!request.getOrderId().equals(originalTransaction.getReferenceId())) {
                throw new IllegalArgumentException("Order ID mismatch");
            }
            
            // Find related accounts
            StoreAccount storeAccount = storeAccountRepository
                    .findActiveByAccountNumber(originalTransaction.getToAccount())
                    .orElseThrow(() -> new IllegalStateException("Store account not found"));
            
            CustomerAccount customerAccount = customerAccountRepository
                    .findActiveByAccountNumber(originalTransaction.getFromAccount())
                    .orElseThrow(() -> new IllegalStateException("Customer account not found"));
            
            // Check refund amount
            if (request.getRefundAmount().compareTo(originalTransaction.getAmount()) > 0) {
                throw new IllegalArgumentException("Refund amount cannot exceed original payment amount");
            }
            
            // Create refund transaction record
            String refundId = generateTransactionId();
            Transaction refundTransaction = Transaction.builder()
                    .transactionId(refundId)
                    .fromAccount(storeAccount.getAccountNumber())
                    .toAccount(customerAccount.getAccountNumber())
                    .fromAccountType(Transaction.AccountType.STORE)
                    .toAccountType(Transaction.AccountType.CUSTOMER)
                    .amount(request.getRefundAmount())
                    .currency(originalTransaction.getCurrency())
                    .transactionType(Transaction.TransactionType.REFUND)
                    .status(Transaction.TransactionStatus.PENDING)
                    .referenceId(request.getOrderId())
                    .referenceType(Transaction.ReferenceType.REFUND)
                    .description("Refund for order: " + request.getOrderId() + ", Reason: " + request.getReason())
                    .build();
            
            refundTransaction = transactionRepository.save(refundTransaction);
            
            // Execute refund transfer
            storeAccount.debit(request.getRefundAmount());
            customerAccount.credit(request.getRefundAmount());
            
            // Update accounts
            storeAccountRepository.save(storeAccount);
            customerAccountRepository.save(customerAccount);
            
            // Mark refund transaction as successful
            refundTransaction.markAsSuccess();
            refundTransaction = transactionRepository.save(refundTransaction);
            
            log.info("Refund processed successfully for order: {}, refund: {}", 
                    request.getOrderId(), refundId);
            
            return RefundResponse.builder()
                    .refundId(refundId)
                    .originalTransactionId(request.getTransactionId())
                    .orderId(request.getOrderId())
                    .refundAmount(request.getRefundAmount())
                    .currency(originalTransaction.getCurrency())
                    .status(Transaction.TransactionStatus.SUCCESS)
                    .reason(request.getReason())
                    .processedAt(refundTransaction.getProcessedAt())
                    .message("Refund processed successfully")
                    .build();
                    
        } catch (Exception e) {
            log.error("Refund processing failed for order: {}", request.getOrderId(), e);
            throw new RuntimeException("Refund processing failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Process lost package refund
     */
    public RefundResponse processLostPackageRefund(LostPackageRefundRequest request) {
        log.info("Processing lost package refund for order: {}", request.getOrderId());
        
        try {
            // Find customer account
            CustomerAccount customerAccount = customerAccountRepository
                    .findActiveByCustomerId(request.getCustomerId())
                    .orElseThrow(() -> new IllegalArgumentException("Customer account not found: " + request.getCustomerId()));
            
            // Find store account
            StoreAccount storeAccount = storeAccountRepository
                    .findActiveByAccountNumber(storeAccountNumber)
                    .orElseThrow(() -> new IllegalStateException("Store account not found: " + storeAccountNumber));
            
            // Create lost package refund transaction record
            String refundId = generateTransactionId();
            Transaction refundTransaction = Transaction.builder()
                    .transactionId(refundId)
                    .fromAccount(storeAccount.getAccountNumber())
                    .toAccount(customerAccount.getAccountNumber())
                    .fromAccountType(Transaction.AccountType.STORE)
                    .toAccountType(Transaction.AccountType.CUSTOMER)
                    .amount(request.getRefundAmount())
                    .currency(request.getCurrency())
                    .transactionType(Transaction.TransactionType.REFUND)
                    .status(Transaction.TransactionStatus.PENDING)
                    .referenceId(request.getOrderId())
                    .referenceType(Transaction.ReferenceType.REFUND)
                    .description("Lost package refund for order: " + request.getOrderId() + 
                               ", delivery: " + request.getDeliveryId() + 
                               ", reason: " + request.getLostReason())
                    .build();
            
            refundTransaction = transactionRepository.save(refundTransaction);
            
            // Store old balance for notification
            BigDecimal oldBalance = customerAccount.getBalance();
            
            // Execute refund transfer
            storeAccount.debit(request.getRefundAmount());
            customerAccount.credit(request.getRefundAmount());
            
            // Update accounts
            storeAccountRepository.save(storeAccount);
            customerAccountRepository.save(customerAccount);
            
            // Mark refund transaction as successful
            refundTransaction.markAsSuccess();
            refundTransaction = transactionRepository.save(refundTransaction);
            
            log.info("Lost package refund processed successfully for order: {}, refund: {}", 
                    request.getOrderId(), refundId);
            
            RefundResponse response = RefundResponse.builder()
                    .refundId(refundId)
                    .originalTransactionId(null) // Lost package has no original transaction ID
                    .orderId(request.getOrderId())
                    .refundAmount(request.getRefundAmount())
                    .currency(request.getCurrency())
                    .status(Transaction.TransactionStatus.SUCCESS)
                    .reason(request.getLostReason())
                    .processedAt(refundTransaction.getProcessedAt())
                    .message("Lost package refund processed successfully")
                    .build();
            
            // Send real-time WebSocket notification
            webSocketNotificationService.sendBalanceUpdate(
                    request.getCustomerId(),
                    customerAccount,
                    oldBalance,
                    "CREDIT",
                    refundId,
                    "Refund for lost package: " + request.getOrderId()
            );
            
            webSocketNotificationService.sendRefundSuccess(
                    request.getCustomerId(),
                    refundId,
                    request.getRefundAmount(),
                    request.getOrderId()
            );
            
            return response;
                    
        } catch (Exception e) {
            log.error("Lost package refund processing failed for order: {}", request.getOrderId(), e);
            throw new RuntimeException("Lost package refund processing failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get customer account balance
     */
    public BalanceResponse getBalance(String customerId) {
        log.info("Getting balance for customer: {}", customerId);
        
        CustomerAccount customerAccount = customerAccountRepository
                .findActiveByCustomerId(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer account not found: " + customerId));
        
        return BalanceResponse.builder()
                .customerId(customerAccount.getCustomerId())
                .accountNumber(customerAccount.getAccountNumber())
                .accountHolderName(customerAccount.getAccountHolderName())
                .balance(customerAccount.getBalance())
                .currency(customerAccount.getCurrency())
                .isActive(customerAccount.getIsActive())
                .build();
    }
    
    /**
     * Generate transaction ID
     */
    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
