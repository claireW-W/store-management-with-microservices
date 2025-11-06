package com.store.store.controller;

import com.store.store.model.Product;
import com.store.store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ProductController {
    
    private final ProductRepository productRepository;
    
    /**
     * Get all active products
     * GET /api/products
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        try {
            List<Product> products = productRepository.findByIsActiveTrue();
            log.info("Retrieved {} active products", products.size());
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error retrieving products: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get product by ID
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        try {
            Optional<Product> product = productRepository.findById(id);
            if (product.isPresent() && product.get().getIsActive()) {
                log.info("Retrieved product: {}", product.get().getName());
                return ResponseEntity.ok(product.get());
            } else {
                log.warn("Product not found or inactive: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error retrieving product {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get product by SKU
     * GET /api/products/sku/{sku}
     */
    @GetMapping("/sku/{sku}")
    public ResponseEntity<Product> getProductBySku(@PathVariable String sku) {
        try {
            Optional<Product> product = productRepository.findBySku(sku);
            if (product.isPresent() && product.get().getIsActive()) {
                log.info("Retrieved product by SKU: {}", sku);
                return ResponseEntity.ok(product.get());
            } else {
                log.warn("Product not found or inactive for SKU: {}", sku);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error retrieving product by SKU {}: {}", sku, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get products by category
     * GET /api/products/category/{categoryId}
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable Long categoryId) {
        try {
            List<Product> products = productRepository.findByCategoryIdAndIsActiveTrue(categoryId);
            log.info("Retrieved {} products for category: {}", products.size(), categoryId);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error retrieving products for category {}: {}", categoryId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
