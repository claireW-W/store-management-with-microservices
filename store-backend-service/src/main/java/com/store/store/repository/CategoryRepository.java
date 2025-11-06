package com.store.store.repository;

import com.store.store.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Category Repository
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByIsActiveTrue();
    
    List<Category> findByParentIdAndIsActiveTrue(Long parentId);
}

