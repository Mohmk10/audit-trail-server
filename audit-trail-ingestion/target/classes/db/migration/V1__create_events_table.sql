CREATE TABLE IF NOT EXISTS events (
    id UUID PRIMARY KEY,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,

    actor_id VARCHAR(255) NOT NULL,
    actor_type VARCHAR(50) NOT NULL,
    actor_name VARCHAR(255) NOT NULL,
    actor_ip VARCHAR(45),
    actor_user_agent VARCHAR(500),
    actor_attributes JSONB,

    action_type VARCHAR(50) NOT NULL,
    action_description TEXT,
    action_category VARCHAR(100),

    resource_id VARCHAR(255) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_name VARCHAR(255) NOT NULL,
    resource_before JSONB,
    resource_after JSONB,

    metadata_source VARCHAR(255),
    tenant_id VARCHAR(100) NOT NULL,
    correlation_id VARCHAR(255),
    session_id VARCHAR(255),
    tags JSONB,
    extra JSONB,

    previous_hash VARCHAR(64),
    hash VARCHAR(64) NOT NULL,
    signature TEXT,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_hash_format CHECK (hash ~ '^[a-f0-9]{64}$'),
    CONSTRAINT chk_previous_hash_format CHECK (previous_hash IS NULL OR previous_hash = 'GENESIS' OR previous_hash ~ '^[a-f0-9]{64}$')
);

CREATE INDEX idx_events_timestamp ON events (timestamp DESC);
CREATE INDEX idx_events_actor_id ON events (actor_id);
CREATE INDEX idx_events_resource_id ON events (resource_id);
CREATE INDEX idx_events_tenant_id ON events (tenant_id);
CREATE INDEX idx_events_action_type ON events (action_type);
CREATE INDEX idx_events_correlation_id ON events (correlation_id) WHERE correlation_id IS NOT NULL;
CREATE INDEX idx_events_created_at ON events (created_at DESC);
CREATE INDEX idx_events_tenant_created ON events (tenant_id, created_at DESC);
CREATE INDEX idx_events_hash ON events (hash);
