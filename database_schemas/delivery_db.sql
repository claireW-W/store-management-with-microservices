-- Delivery Database Schema
-- PostgreSQL Version - DeliveryCo Service

-- ============================================
-- Delivery Database
-- ============================================

-- Deliveries Table
CREATE TABLE deliveries (
    id BIGSERIAL PRIMARY KEY,
    delivery_id VARCHAR(50) UNIQUE NOT NULL,
    order_id VARCHAR(50) NOT NULL,
    customer_id VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING_PICKUP' CHECK (status IN ('PENDING_PICKUP', 'PICKED_UP', 'IN_TRANSIT', 'DELIVERED', 'FAILED', 'CANCELLED')),
    pickup_warehouse_id BIGINT,
    shipping_address JSONB NOT NULL,
    estimated_pickup TIMESTAMP,
    estimated_delivery TIMESTAMP,
    actual_pickup TIMESTAMP,
    actual_delivery TIMESTAMP,
    tracking_number VARCHAR(100),
    carrier VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_deliveries_delivery_id ON deliveries(delivery_id);
CREATE INDEX idx_deliveries_order_id ON deliveries(order_id);
CREATE INDEX idx_deliveries_customer_id ON deliveries(customer_id);
CREATE INDEX idx_deliveries_status ON deliveries(status);
CREATE INDEX idx_deliveries_estimated_delivery ON deliveries(estimated_delivery);

-- Delivery Items Table
CREATE TABLE delivery_items (
    id BIGSERIAL PRIMARY KEY,
    delivery_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    FOREIGN KEY (delivery_id) REFERENCES deliveries(id) ON DELETE CASCADE
);

CREATE INDEX idx_delivery_items_delivery_id ON delivery_items(delivery_id);
CREATE INDEX idx_delivery_items_product_id ON delivery_items(product_id);
CREATE INDEX idx_delivery_items_warehouse_id ON delivery_items(warehouse_id);

-- Delivery Status History Table
CREATE TABLE delivery_status_history (
    id BIGSERIAL PRIMARY KEY,
    delivery_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING_PICKUP', 'PICKED_UP', 'IN_TRANSIT', 'DELIVERED', 'FAILED', 'CANCELLED')),
    location VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (delivery_id) REFERENCES deliveries(id) ON DELETE CASCADE
);

CREATE INDEX idx_delivery_status_history_delivery_id ON delivery_status_history(delivery_id);
CREATE INDEX idx_delivery_status_history_status ON delivery_status_history(status);
CREATE INDEX idx_delivery_status_history_created_at ON delivery_status_history(created_at);

