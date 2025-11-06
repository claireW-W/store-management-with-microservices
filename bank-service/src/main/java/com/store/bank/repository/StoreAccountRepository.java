package com.store.bank.repository;

import com.store.bank.model.StoreAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreAccountRepository extends JpaRepository<StoreAccount, Long> {
    
    Optional<StoreAccount> findByAccountNumber(String accountNumber);
    
    @Query("SELECT sa FROM StoreAccount sa WHERE sa.accountNumber = :accountNumber AND sa.isActive = true")
    Optional<StoreAccount> findActiveByAccountNumber(@Param("accountNumber") String accountNumber);
    
    boolean existsByAccountNumber(String accountNumber);
}
