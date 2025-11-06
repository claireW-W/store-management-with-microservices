-- Bank Service Data Initialization
-- This file is automatically executed when the application starts

-- Insert test customer accounts
-- Only insert if account doesn't exist, preserve existing balance
INSERT INTO customer_accounts (customer_id, account_number, account_holder_name, balance, currency, is_active) VALUES
('CUST-1', 'ACC-001', 'Test Customer', 10000.00, 'AUD', true),
('CUST-2', 'ACC-002', 'John Doe', 1000.00, 'AUD', true),
('CUST-3', 'ACC-003', 'Jane Smith', 500.00, 'AUD', true)
ON CONFLICT (customer_id) DO UPDATE SET
    account_number = EXCLUDED.account_number,
    account_holder_name = EXCLUDED.account_holder_name,
    -- balance = EXCLUDED.balance,  -- Preserve existing balance, do not reset
    currency = EXCLUDED.currency,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;

-- Insert store account
-- Only insert if account doesn't exist, preserve existing balance
INSERT INTO store_accounts (account_number, account_name, balance, currency, is_active) VALUES
('STORE-001', 'Store Account', 0.00, 'AUD', true)
ON CONFLICT (account_number) DO UPDATE SET
    account_name = EXCLUDED.account_name,
    -- balance = EXCLUDED.balance,  -- Preserve existing balance, do not reset
    currency = EXCLUDED.currency,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;
