package com.store.delivery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "deliveries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "delivery_id", unique = true, nullable = false)
    private String deliveryId;
    
    @Column(name = "order_id", nullable = false)
    private String orderId;
    
    @Column(name = "customer_id", nullable = false)
    private String customerId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DeliveryStatus status = DeliveryStatus.PENDING_PICKUP;
    
    @Column(name = "pickup_warehouse_id")
    private Long pickupWarehouseId;
    
    @Column(name = "shipping_address", columnDefinition = "jsonb")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private ShippingAddress shippingAddress;
    
    @Column(name = "estimated_pickup")
    private LocalDateTime estimatedPickup;
    
    @Column(name = "estimated_delivery")
    private LocalDateTime estimatedDelivery;
    
    @Column(name = "actual_pickup")
    private LocalDateTime actualPickup;
    
    @Column(name = "actual_delivery")
    private LocalDateTime actualDelivery;
    
    @Column(name = "tracking_number")
    private String trackingNumber;
    
    @Column(name = "carrier")
    private String carrier;
    
    @Column(name = "notes")
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DeliveryItem> deliveryItems;
    
    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DeliveryStatusHistory> statusHistory;
}
