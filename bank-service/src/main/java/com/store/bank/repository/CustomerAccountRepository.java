package com.store.bank.repository;

import com.store.bank.model.CustomerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerAccountRepository extends JpaRepository<CustomerAccount, Long> {
    
    Optional<CustomerAccount> findByCustomerId(String customerId);
    
    Optional<CustomerAccount> findByAccountNumber(String accountNumber);
    
    @Query("SELECT ca FROM CustomerAccount ca WHERE ca.customerId = :customerId AND ca.isActive = true")
    Optional<CustomerAccount> findActiveByCustomerId(@Param("customerId") String customerId);
    
    @Query("SELECT ca FROM CustomerAccount ca WHERE ca.accountNumber = :accountNumber AND ca.isActive = true")
    Optional<CustomerAccount> findActiveByAccountNumber(@Param("accountNumber") String accountNumber);
    
    boolean existsByCustomerId(String customerId);
    
    boolean existsByAccountNumber(String accountNumber);
}
