package com.mohmk10.audittrail.admin.domain;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class Tenant {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private TenantStatus status;
    private TenantPlan plan;
    private TenantQuota quota;
    private Map<String, String> settings;
    private Instant createdAt;
    private Instant updatedAt;

    public Tenant() {
    }

    private Tenant(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.slug = builder.slug;
        this.description = builder.description;
        this.status = builder.status;
        this.plan = builder.plan;
        this.quota = builder.quota;
        this.settings = builder.settings;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TenantStatus getStatus() {
        return status;
    }

    public void setStatus(TenantStatus status) {
        this.status = status;
    }

    public TenantPlan getPlan() {
        return plan;
    }

    public void setPlan(TenantPlan plan) {
        this.plan = plan;
    }

    public TenantQuota getQuota() {
        return quota;
    }

    public void setQuota(TenantQuota quota) {
        this.quota = quota;
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, String> settings) {
        this.settings = settings;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class Builder {
        private UUID id;
        private String name;
        private String slug;
        private String description;
        private TenantStatus status;
        private TenantPlan plan;
        private TenantQuota quota;
        private Map<String, String> settings;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder slug(String slug) {
            this.slug = slug;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder status(TenantStatus status) {
            this.status = status;
            return this;
        }

        public Builder plan(TenantPlan plan) {
            this.plan = plan;
            return this;
        }

        public Builder quota(TenantQuota quota) {
            this.quota = quota;
            return this;
        }

        public Builder settings(Map<String, String> settings) {
            this.settings = settings;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Tenant build() {
            return new Tenant(this);
        }
    }
}
