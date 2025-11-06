package com.store.bank.repository;

import com.store.bank.model.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {
    
    List<TransactionLog> findByTransactionId(Long transactionId);
    
    List<TransactionLog> findByTransactionIdOrderByCreatedAtAsc(Long transactionId);
}
