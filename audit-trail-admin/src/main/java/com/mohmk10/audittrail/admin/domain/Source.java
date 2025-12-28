package com.mohmk10.audittrail.admin.domain;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class Source {
    private UUID id;
    private String tenantId;
    private String name;
    private String description;
    private SourceType type;
    private SourceStatus status;
    private Map<String, String> config;
    private Instant createdAt;
    private Instant lastEventAt;
    private long eventCount;

    public Source() {
    }

    private Source(Builder builder) {
        this.id = builder.id;
        this.tenantId = builder.tenantId;
        this.name = builder.name;
        this.description = builder.description;
        this.type = builder.type;
        this.status = builder.status;
        this.config = builder.config;
        this.createdAt = builder.createdAt;
        this.lastEventAt = builder.lastEventAt;
        this.eventCount = builder.eventCount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SourceType getType() {
        return type;
    }

    public void setType(SourceType type) {
        this.type = type;
    }

    public SourceStatus getStatus() {
        return status;
    }

    public void setStatus(SourceStatus status) {
        this.status = status;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastEventAt() {
        return lastEventAt;
    }

    public void setLastEventAt(Instant lastEventAt) {
        this.lastEventAt = lastEventAt;
    }

    public long getEventCount() {
        return eventCount;
    }

    public void setEventCount(long eventCount) {
        this.eventCount = eventCount;
    }

    public static class Builder {
        private UUID id;
        private String tenantId;
        private String name;
        private String description;
        private SourceType type;
        private SourceStatus status;
        private Map<String, String> config;
        private Instant createdAt;
        private Instant lastEventAt;
        private long eventCount;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder type(SourceType type) {
            this.type = type;
            return this;
        }

        public Builder status(SourceStatus status) {
            this.status = status;
            return this;
        }

        public Builder config(Map<String, String> config) {
            this.config = config;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder lastEventAt(Instant lastEventAt) {
            this.lastEventAt = lastEventAt;
            return this;
        }

        public Builder eventCount(long eventCount) {
            this.eventCount = eventCount;
            return this;
        }

        public Source build() {
            return new Source(this);
        }
    }
}
