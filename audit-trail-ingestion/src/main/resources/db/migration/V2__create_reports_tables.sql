CREATE TABLE reports (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    format VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,
    tenant_id VARCHAR(255) NOT NULL,
    criteria JSONB,
    parameters JSONB,
    generated_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ,
    file_path VARCHAR(500),
    file_size BIGINT,
    checksum VARCHAR(64),
    signature TEXT,
    error_message TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE scheduled_reports (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    format VARCHAR(10) NOT NULL,
    tenant_id VARCHAR(255) NOT NULL,
    criteria JSONB,
    cron_expression VARCHAR(100) NOT NULL,
    enabled BOOLEAN DEFAULT true,
    last_run_at TIMESTAMPTZ,
    next_run_at TIMESTAMPTZ,
    recipients JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_reports_tenant_id ON reports(tenant_id);
CREATE INDEX idx_reports_status ON reports(status);
CREATE INDEX idx_reports_expires_at ON reports(expires_at);
CREATE INDEX idx_scheduled_reports_tenant_id ON scheduled_reports(tenant_id);
CREATE INDEX idx_scheduled_reports_enabled ON scheduled_reports(enabled);
