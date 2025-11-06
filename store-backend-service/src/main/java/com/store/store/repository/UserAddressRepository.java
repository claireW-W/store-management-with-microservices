package com.store.store.repository;

import com.store.store.model.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User Address Repository
 */
@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    
    List<UserAddress> findByUserId(Long userId);
    
    Optional<UserAddress> findByUserIdAndIsDefaultTrue(Long userId);
    
    List<UserAddress> findByUserIdAndAddressType(Long userId, String addressType);
}

