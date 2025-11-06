-- Bank Database Schema
-- PostgreSQL Version - Bank Service

-- ============================================
-- Bank Database
-- ============================================

-- Customer Accounts Table
CREATE TABLE customer_accounts (
    id BIGSERIAL PRIMARY KEY,
    customer_id VARCHAR(50) UNIQUE NOT NULL,
    account_number VARCHAR(50) UNIQUE NOT NULL,
    account_holder_name VARCHAR(100) NOT NULL,
    balance DECIMAL(15,2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'AUD',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_customer_accounts_customer_id ON customer_accounts(customer_id);
CREATE INDEX idx_customer_accounts_account_number ON customer_accounts(account_number);

-- Store Accounts Table
CREATE TABLE store_accounts (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(50) UNIQUE NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    balance DECIMAL(15,2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'AUD',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_store_accounts_account_number ON store_accounts(account_number);

-- Transactions Table
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(50) UNIQUE NOT NULL,
    from_account VARCHAR(50),
    to_account VARCHAR(50) NOT NULL,
    from_account_type VARCHAR(20) CHECK (from_account_type IN ('CUSTOMER', 'STORE')),
    to_account_type VARCHAR(20) NOT NULL CHECK (to_account_type IN ('CUSTOMER', 'STORE')),
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'AUD',
    transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('PAYMENT', 'REFUND', 'TRANSFER')),
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED')),
    reference_id VARCHAR(100),
    reference_type VARCHAR(20) NOT NULL CHECK (reference_type IN ('ORDER', 'REFUND')),
    description TEXT,
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transactions_transaction_id ON transactions(transaction_id);
CREATE INDEX idx_transactions_from_account ON transactions(from_account);
CREATE INDEX idx_transactions_to_account ON transactions(to_account);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_reference_id ON transactions(reference_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);

-- Note: We use account_number (VARCHAR) instead of foreign keys because:
-- 1. from_account can reference either customer_accounts OR store_accounts
-- 2. PostgreSQL doesn't support "either/or" foreign keys
-- 3. Application-level validation ensures account existence via account_type field
-- 4. This design supports flexible account types in the future

-- Transaction Logs Table
CREATE TABLE transaction_logs (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    log_level VARCHAR(10) NOT NULL CHECK (log_level IN ('INFO', 'WARN', 'ERROR')),
    message TEXT NOT NULL,
    details JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE
);

CREATE INDEX idx_transaction_logs_transaction_id ON transaction_logs(transaction_id);
CREATE INDEX idx_transaction_logs_log_level ON transaction_logs(log_level);
CREATE INDEX idx_transaction_logs_created_at ON transaction_logs(created_at);

-- ============================================
-- Initial Data Setup
-- ============================================

-- Insert default store account
INSERT INTO store_accounts (account_number, account_name, balance, currency) 
VALUES ('STORE-001', 'Store Online Shop Account', 0.00, 'AUD');

