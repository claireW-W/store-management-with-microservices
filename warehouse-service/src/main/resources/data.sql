-- Warehouse Service Data Initialization
-- This file is automatically executed when the application starts

-- Insert test warehouses
INSERT INTO warehouses (warehouse_code, name, address, contact_phone, contact_email, is_active) VALUES
('SYD001', 'Sydney Warehouse', '{"street": "123 Industrial St", "suburb": "Sydney", "state": "NSW", "postcode": "2000", "country": "Australia"}', '02-1234-5678', 'sydney@warehouse.com', true),
('MEL001', 'Melbourne Warehouse', '{"street": "456 Factory Ave", "suburb": "Melbourne", "state": "VIC", "postcode": "3000", "country": "Australia"}', '03-9876-5432', 'melbourne@warehouse.com', true),
('BRI001', 'Brisbane Warehouse', '{"street": "789 Logistics Blvd", "suburb": "Brisbane", "state": "QLD", "postcode": "4000", "country": "Australia"}', '07-5555-1234', 'brisbane@warehouse.com', true)
ON CONFLICT (warehouse_code) DO UPDATE SET
    name = EXCLUDED.name,
    address = EXCLUDED.address,
    contact_phone = EXCLUDED.contact_phone,
    contact_email = EXCLUDED.contact_email,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;

-- Insert test inventory
INSERT INTO inventory (warehouse_id, product_id, available_quantity, reserved_quantity, total_quantity, reorder_level) VALUES
(1, 1, 50, 0, 50, 10),  -- Laptop Computer
(1, 2, 100, 0, 100, 20), -- Wireless Mouse
(1, 3, 200, 0, 200, 30), -- T-Shirt
(1, 4, 75, 0, 75, 15),   -- Programming Book
(1, 5, 25, 0, 25, 5),    -- Garden Tools Set
(2, 1, 30, 0, 30, 10),   -- Laptop Computer (Melbourne)
(2, 2, 80, 0, 80, 20),   -- Wireless Mouse (Melbourne)
(2, 3, 150, 0, 150, 30), -- T-Shirt (Melbourne)
(3, 1, 20, 0, 20, 10),   -- Laptop Computer (Brisbane)
(3, 2, 60, 0, 60, 20)    -- Wireless Mouse (Brisbane)
ON CONFLICT (warehouse_id, product_id) DO UPDATE SET
    available_quantity = EXCLUDED.available_quantity,
    reserved_quantity = EXCLUDED.reserved_quantity,
    total_quantity = EXCLUDED.total_quantity,
    reorder_level = EXCLUDED.reorder_level,
    last_updated = CURRENT_TIMESTAMP;
