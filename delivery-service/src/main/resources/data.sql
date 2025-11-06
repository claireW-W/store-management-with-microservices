-- Ensure status constraints include LOST (for auto loss transitions)
ALTER TABLE IF EXISTS deliveries
  DROP CONSTRAINT IF EXISTS deliveries_status_check;
ALTER TABLE IF EXISTS deliveries
  ADD CONSTRAINT deliveries_status_check
  CHECK (status IN ('PENDING_PICKUP','PICKED_UP','IN_TRANSIT','DELIVERED','FAILED','CANCELLED','LOST'));

ALTER TABLE IF EXISTS delivery_status_history
  DROP CONSTRAINT IF EXISTS delivery_status_history_status_check;
ALTER TABLE IF EXISTS delivery_status_history
  ADD CONSTRAINT delivery_status_history_status_check
  CHECK (status IN ('PENDING_PICKUP','PICKED_UP','IN_TRANSIT','DELIVERED','FAILED','CANCELLED','LOST'));

-- Insert test delivery data
INSERT INTO deliveries (delivery_id, order_id, customer_id, status, pickup_warehouse_id, shipping_address, tracking_number, carrier, notes, created_at, updated_at) VALUES
('DEL-12345678', 'ORDER-001', 'CUSTOMER-001', 'PENDING_PICKUP', 1, '{"street": "123 Main St", "city": "Sydney", "state": "NSW", "postalCode": "2000", "country": "Australia"}', 'DEL-1703123456789', 'Australia Post', 'Test delivery', NOW(), NOW()),
('DEL-87654321', 'ORDER-002', 'CUSTOMER-002', 'IN_TRANSIT', 2, '{"street": "456 Queen St", "city": "Melbourne", "state": "VIC", "postalCode": "3000", "country": "Australia"}', 'DEL-1703123456790', 'DHL', 'Express delivery', NOW(), NOW());

-- Insert test delivery items
INSERT INTO delivery_items (delivery_id, product_id, quantity, warehouse_id) VALUES
(1, 101, 2, 1),
(1, 102, 1, 1),
(2, 103, 3, 2);

-- Insert test status history
INSERT INTO delivery_status_history (delivery_id, status, location, notes, created_at) VALUES
(1, 'PENDING_PICKUP', 'Warehouse 1', 'Delivery created', NOW()),
(2, 'PENDING_PICKUP', 'Warehouse 2', 'Delivery created', NOW()),
(2, 'PICKED_UP', 'Warehouse 2', 'Package picked up', NOW()),
(2, 'IN_TRANSIT', 'In transit', 'Package in transit', NOW());