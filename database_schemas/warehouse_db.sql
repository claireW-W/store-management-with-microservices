-- Warehouse Database Schema
-- PostgreSQL Version - Warehouse Service

-- ============================================
-- Warehouse Database
-- ============================================

-- Warehouses Table
CREATE TABLE warehouses (
    id BIGSERIAL PRIMARY KEY,
    warehouse_code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    address JSONB NOT NULL,
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_warehouses_warehouse_code ON warehouses(warehouse_code);

-- Inventory Table
CREATE TABLE inventory (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    available_quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    total_quantity INT NOT NULL DEFAULT 0,
    reorder_level INT DEFAULT 10,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id) ON DELETE CASCADE,
    UNIQUE (warehouse_id, product_id)
);

CREATE INDEX idx_inventory_warehouse_id ON inventory(warehouse_id);
CREATE INDEX idx_inventory_product_id ON inventory(product_id);
CREATE INDEX idx_inventory_available_quantity ON inventory(available_quantity);

-- Inventory Reservations Table
CREATE TABLE inventory_reservations (
    id BIGSERIAL PRIMARY KEY,
    reservation_id VARCHAR(50) UNIQUE NOT NULL,
    order_id VARCHAR(50) NOT NULL,
    warehouse_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'RELEASED', 'EXPIRED')),
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id)
);

CREATE INDEX idx_inventory_reservations_reservation_id ON inventory_reservations(reservation_id);
CREATE INDEX idx_inventory_reservations_order_id ON inventory_reservations(order_id);
CREATE INDEX idx_inventory_reservations_status ON inventory_reservations(status);
CREATE INDEX idx_inventory_reservations_expires_at ON inventory_reservations(expires_at);

-- Inventory Transactions Table
CREATE TABLE inventory_transactions (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('IN', 'OUT', 'ADJUSTMENT', 'RESERVE', 'RELEASE')),
    quantity INT NOT NULL,
    reference_id VARCHAR(100),
    reference_type VARCHAR(20) NOT NULL CHECK (reference_type IN ('ORDER', 'RESERVATION', 'ADJUSTMENT')),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id)
);

CREATE INDEX idx_inventory_transactions_warehouse_id ON inventory_transactions(warehouse_id);
CREATE INDEX idx_inventory_transactions_product_id ON inventory_transactions(product_id);
CREATE INDEX idx_inventory_transactions_transaction_type ON inventory_transactions(transaction_type);
CREATE INDEX idx_inventory_transactions_created_at ON inventory_transactions(created_at);

