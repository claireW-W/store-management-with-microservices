package com.store.store.repository;

import com.store.store.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Product Repository
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findBySku(String sku);
    
    List<Product> findByIsActiveTrue();
    
    List<Product> findByCategoryIdAndIsActiveTrue(Long categoryId);
}

