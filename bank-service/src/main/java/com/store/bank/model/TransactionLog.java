package com.store.bank.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "log_level", nullable = false)
    private LogLevel logLevel;
    
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "details", columnDefinition = "jsonb")
    private String details;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public enum LogLevel {
        INFO, WARN, ERROR
    }
    
    public static TransactionLog info(String message) {
        return TransactionLog.builder()
                .logLevel(LogLevel.INFO)
                .message(message)
                .build();
    }
    
    public static TransactionLog warn(String message) {
        return TransactionLog.builder()
                .logLevel(LogLevel.WARN)
                .message(message)
                .build();
    }
    
    public static TransactionLog error(String message) {
        return TransactionLog.builder()
                .logLevel(LogLevel.ERROR)
                .message(message)
                .build();
    }
}
