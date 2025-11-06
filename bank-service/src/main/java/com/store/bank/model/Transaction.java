package com.store.bank.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;
    
    @Column(name = "from_account")
    private String fromAccount;
    
    @Column(name = "to_account", nullable = false)
    private String toAccount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "from_account_type")
    private AccountType fromAccountType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "to_account_type", nullable = false)
    private AccountType toAccountType;
    
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "currency", nullable = false)
    @Builder.Default
    private String currency = "AUD";
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;
    
    @Column(name = "reference_id")
    private String referenceId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false)
    private ReferenceType referenceType;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TransactionLog> logs = new ArrayList<>();
    
    public enum TransactionType {
        PAYMENT, REFUND, TRANSFER
    }
    
    public enum TransactionStatus {
        PENDING, SUCCESS, FAILED, CANCELLED
    }
    
    public enum ReferenceType {
        ORDER, REFUND
    }
    
    public enum AccountType {
        CUSTOMER, STORE
    }
    
    public void addLog(TransactionLog log) {
        logs.add(log);
        log.setTransaction(this);
    }
    
    public void markAsSuccess() {
        this.status = TransactionStatus.SUCCESS;
        this.processedAt = LocalDateTime.now();
    }
    
    public void markAsFailed() {
        this.status = TransactionStatus.FAILED;
        this.processedAt = LocalDateTime.now();
    }
    
    public void markAsCancelled() {
        this.status = TransactionStatus.CANCELLED;
        this.processedAt = LocalDateTime.now();
    }
    
    public boolean isSuccess() {
        return TransactionStatus.SUCCESS.equals(this.status);
    }
    
    public boolean isFailed() {
        return TransactionStatus.FAILED.equals(this.status);
    }
}
