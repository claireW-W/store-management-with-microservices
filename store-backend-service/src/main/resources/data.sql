-- Ensure order and history status constraints include full delivery states
ALTER TABLE IF EXISTS orders DROP CONSTRAINT IF EXISTS orders_status_check;
ALTER TABLE IF EXISTS orders
  ADD CONSTRAINT orders_status_check
  CHECK (status IN ('PENDING','PROCESSING','PAID','SHIPPED','PENDING_PICKUP','PICKED_UP','IN_TRANSIT','DELIVERED','CANCELLED','REFUNDED','FAILED','LOST'));

ALTER TABLE IF EXISTS order_status_history DROP CONSTRAINT IF EXISTS order_status_history_status_check;
ALTER TABLE IF EXISTS order_status_history
  ADD CONSTRAINT order_status_history_status_check
  CHECK (status IN ('PENDING','PROCESSING','PAID','SHIPPED','PENDING_PICKUP','PICKED_UP','IN_TRANSIT','DELIVERED','CANCELLED','REFUNDED','FAILED','LOST'));

-- Store Backend Service Data Initialization
-- This file is automatically executed when the application starts

-- Insert test user data
INSERT INTO users (username, password_hash, email, first_name, last_name, phone, is_active) VALUES
('customer', '$2b$12$J4BUC5k26R0vzO1GMl8.EusAMc4Fnbj5vG0RI1lKM6wGsRPT2lYiu', 'customer@store.com', 'Test', 'Customer', '1234567890', true)
ON CONFLICT (username) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    email = EXCLUDED.email,
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    phone = EXCLUDED.phone,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;

-- Insert test categories
INSERT INTO categories (name, description, is_active) VALUES
('Electronics', 'Electronic devices and accessories', true),
('Clothing', 'Fashion and apparel', true),
('Books', 'Books and educational materials', true),
('Home & Garden', 'Home improvement and gardening', true)
ON CONFLICT DO NOTHING;

-- Insert test products
INSERT INTO products (name, description, price, category_id, sku, weight, dimensions, is_active) VALUES
('Laptop Computer', 'High-performance laptop for work and gaming', 1299.99, 1, 'LAPTOP-001', 2.5, '35x25x2 cm', true),
('Wireless Mouse', 'Ergonomic wireless mouse with USB receiver', 29.99, 1, 'MOUSE-001', 0.1, '12x6x4 cm', true),
('T-Shirt', 'Comfortable cotton t-shirt in various sizes', 19.99, 2, 'TSHIRT-001', 0.2, 'One Size', true),
('Programming Book', 'Learn Java programming from scratch', 49.99, 3, 'BOOK-001', 0.8, '23x18x3 cm', true),
('Garden Tools Set', 'Complete set of gardening tools', 79.99, 4, 'GARDEN-001', 3.0, 'Various sizes', true)
ON CONFLICT (sku) DO NOTHING;
