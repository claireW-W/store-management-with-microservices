package com.store.bank.repository;

import com.store.bank.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    Optional<Transaction> findByTransactionId(String transactionId);
    
    @Query("SELECT t FROM Transaction t WHERE t.referenceId = :referenceId AND t.referenceType = :referenceType")
    List<Transaction> findByReferenceIdAndReferenceType(@Param("referenceId") String referenceId, 
                                                       @Param("referenceType") Transaction.ReferenceType referenceType);
    
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount = :accountNumber OR t.toAccount = :accountNumber")
    List<Transaction> findByAccountNumber(@Param("accountNumber") String accountNumber);
    
    @Query("SELECT t FROM Transaction t WHERE t.status = :status AND t.createdAt >= :fromDate")
    List<Transaction> findByStatusAndCreatedAtAfter(@Param("status") Transaction.TransactionStatus status,
                                                   @Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount = :accountNumber AND t.status = :status")
    List<Transaction> findByFromAccountAndStatus(@Param("accountNumber") String accountNumber,
                                                @Param("status") Transaction.TransactionStatus status);
    
    @Query("SELECT t FROM Transaction t WHERE t.toAccount = :accountNumber AND t.status = :status")
    List<Transaction> findByToAccountAndStatus(@Param("accountNumber") String accountNumber,
                                              @Param("status") Transaction.TransactionStatus status);
    
    boolean existsByTransactionId(String transactionId);
}
