-- V6: Create webhook tables for integration module

-- Webhooks table
CREATE TABLE IF NOT EXISTS webhooks (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    url VARCHAR(2048) NOT NULL,
    secret VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    max_retries INT NOT NULL DEFAULT 5,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_webhook_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
);

-- Webhook events (many-to-many relationship)
CREATE TABLE IF NOT EXISTS webhook_events (
    webhook_id UUID NOT NULL REFERENCES webhooks(id) ON DELETE CASCADE,
    event_type VARCHAR(100) NOT NULL,
    PRIMARY KEY (webhook_id, event_type)
);

-- Webhook headers (key-value pairs)
CREATE TABLE IF NOT EXISTS webhook_headers (
    webhook_id UUID NOT NULL REFERENCES webhooks(id) ON DELETE CASCADE,
    header_name VARCHAR(255) NOT NULL,
    header_value VARCHAR(2048) NOT NULL,
    PRIMARY KEY (webhook_id, header_name)
);

-- Webhook deliveries table
CREATE TABLE IF NOT EXISTS webhook_deliveries (
    id UUID PRIMARY KEY,
    webhook_id UUID NOT NULL REFERENCES webhooks(id) ON DELETE CASCADE,
    event_type VARCHAR(100) NOT NULL,
    event_payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempt_count INT NOT NULL DEFAULT 0,
    http_status INT,
    response_body TEXT,
    error_message VARCHAR(500),
    next_retry_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivered_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_delivery_status CHECK (status IN ('PENDING', 'DELIVERED', 'FAILED', 'RETRYING'))
);

-- Indexes for webhooks
CREATE INDEX IF NOT EXISTS idx_webhooks_tenant_id ON webhooks(tenant_id);
CREATE INDEX IF NOT EXISTS idx_webhooks_tenant_status ON webhooks(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_webhooks_status ON webhooks(status);

-- Indexes for webhook events
CREATE INDEX IF NOT EXISTS idx_webhook_events_event_type ON webhook_events(event_type);

-- Indexes for webhook deliveries
CREATE INDEX IF NOT EXISTS idx_deliveries_webhook_id ON webhook_deliveries(webhook_id);
CREATE INDEX IF NOT EXISTS idx_deliveries_status ON webhook_deliveries(status);
CREATE INDEX IF NOT EXISTS idx_deliveries_status_retry ON webhook_deliveries(status, next_retry_at);
CREATE INDEX IF NOT EXISTS idx_deliveries_created_at ON webhook_deliveries(created_at);

-- Comments
COMMENT ON TABLE webhooks IS 'Webhook configurations for event delivery';
COMMENT ON TABLE webhook_events IS 'Event types that each webhook subscribes to';
COMMENT ON TABLE webhook_headers IS 'Custom headers to include in webhook requests';
COMMENT ON TABLE webhook_deliveries IS 'Webhook delivery attempts and their status';
