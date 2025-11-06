-- Email Database Schema
-- PostgreSQL Version - Email Service

-- ============================================
-- Email Database
-- ============================================

-- Email Templates Table
CREATE TABLE email_templates (
    id BIGSERIAL PRIMARY KEY,
    template_name VARCHAR(100) UNIQUE NOT NULL,
    subject VARCHAR(255) NOT NULL,
    body_template TEXT NOT NULL,
    template_type VARCHAR(30) NOT NULL CHECK (template_type IN ('ORDER_CONFIRMATION', 'DELIVERY_UPDATE', 'ORDER_CANCELLATION', 'PAYMENT_NOTIFICATION')),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email_templates_template_name ON email_templates(template_name);
CREATE INDEX idx_email_templates_template_type ON email_templates(template_type);

-- Email Queue Table
CREATE TABLE email_queue (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL,
    user_id BIGINT,
    order_id BIGINT,
    delivery_id BIGINT,
    transaction_id BIGINT,
    recipient_email VARCHAR(255) NOT NULL,
    recipient_name VARCHAR(100),
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'RETRY')),
    priority INT DEFAULT 5,
    retry_count INT DEFAULT 0,
    max_retries INT DEFAULT 3,
    scheduled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,
    error_message TEXT,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (template_id) REFERENCES email_templates(id)
);

CREATE INDEX idx_email_queue_status ON email_queue(status);
CREATE INDEX idx_email_queue_priority ON email_queue(priority);
CREATE INDEX idx_email_queue_scheduled_at ON email_queue(scheduled_at);
CREATE INDEX idx_email_queue_recipient_email ON email_queue(recipient_email);
CREATE INDEX idx_email_queue_user_id ON email_queue(user_id);
CREATE INDEX idx_email_queue_order_id ON email_queue(order_id);
CREATE INDEX idx_email_queue_delivery_id ON email_queue(delivery_id);
CREATE INDEX idx_email_queue_transaction_id ON email_queue(transaction_id);

-- Email Logs Table
CREATE TABLE email_logs (
    id BIGSERIAL PRIMARY KEY,
    email_queue_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL CHECK (action IN ('QUEUED', 'SENT', 'FAILED', 'RETRY')),
    message TEXT,
    details JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (email_queue_id) REFERENCES email_queue(id) ON DELETE CASCADE
);

CREATE INDEX idx_email_logs_email_queue_id ON email_logs(email_queue_id);
CREATE INDEX idx_email_logs_action ON email_logs(action);
CREATE INDEX idx_email_logs_created_at ON email_logs(created_at);

-- ============================================
-- Initial Data Setup
-- ============================================

-- Insert default email templates
INSERT INTO email_templates (template_name, subject, body_template, template_type) VALUES
('order_confirmation', 'Order Confirmation - {{orderNumber}}', 'Dear {{customerName}},\n\nYour order {{orderNumber}} has been confirmed.\n\nTotal Amount: ${{totalAmount}}\n\nThank you for your purchase!', 'ORDER_CONFIRMATION'),
('delivery_update', 'Delivery Update - {{orderNumber}}', 'Dear {{customerName}},\n\nYour order {{orderNumber}} status has been updated to {{deliveryStatus}}.\n\nEstimated delivery: {{estimatedDelivery}}\n\nThank you!', 'DELIVERY_UPDATE'),
('package_lost', 'Package Lost - {{orderNumber}}', 'Dear {{customerName}},\n\nWe regret to inform you that your package for order {{orderNumber}} has been lost during delivery.\n\nA full refund will be processed shortly.\n\nWe apologize for the inconvenience.', 'ORDER_CANCELLATION'),
('refund_notification', 'Refund Processed - {{orderNumber}}', 'Dear {{customerName}},\n\nYour refund for order {{orderNumber}} has been processed.\n\nRefund Amount: ${{refundAmount}}\n\nThank you for your understanding.', 'PAYMENT_NOTIFICATION');

