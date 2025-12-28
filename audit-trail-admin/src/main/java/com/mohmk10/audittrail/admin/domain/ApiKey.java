package com.mohmk10.audittrail.admin.domain;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class ApiKey {
    private UUID id;
    private String tenantId;
    private UUID sourceId;
    private String name;
    private String keyHash;
    private String keyPrefix;
    private Set<ApiKeyScope> scopes;
    private ApiKeyStatus status;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant lastUsedAt;
    private String lastUsedIp;

    public ApiKey() {
    }

    private ApiKey(Builder builder) {
        this.id = builder.id;
        this.tenantId = builder.tenantId;
        this.sourceId = builder.sourceId;
        this.name = builder.name;
        this.keyHash = builder.keyHash;
        this.keyPrefix = builder.keyPrefix;
        this.scopes = builder.scopes;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
        this.expiresAt = builder.expiresAt;
        this.lastUsedAt = builder.lastUsedAt;
        this.lastUsedIp = builder.lastUsedIp;
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

    public UUID getSourceId() {
        return sourceId;
    }

    public void setSourceId(UUID sourceId) {
        this.sourceId = sourceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKeyHash() {
        return keyHash;
    }

    public void setKeyHash(String keyHash) {
        this.keyHash = keyHash;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public Set<ApiKeyScope> getScopes() {
        return scopes;
    }

    public void setScopes(Set<ApiKeyScope> scopes) {
        this.scopes = scopes;
    }

    public ApiKeyStatus getStatus() {
        return status;
    }

    public void setStatus(ApiKeyStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(Instant lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public String getLastUsedIp() {
        return lastUsedIp;
    }

    public void setLastUsedIp(String lastUsedIp) {
        this.lastUsedIp = lastUsedIp;
    }

    public static class Builder {
        private UUID id;
        private String tenantId;
        private UUID sourceId;
        private String name;
        private String keyHash;
        private String keyPrefix;
        private Set<ApiKeyScope> scopes;
        private ApiKeyStatus status;
        private Instant createdAt;
        private Instant expiresAt;
        private Instant lastUsedAt;
        private String lastUsedIp;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder sourceId(UUID sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder keyHash(String keyHash) {
            this.keyHash = keyHash;
            return this;
        }

        public Builder keyPrefix(String keyPrefix) {
            this.keyPrefix = keyPrefix;
            return this;
        }

        public Builder scopes(Set<ApiKeyScope> scopes) {
            this.scopes = scopes;
            return this;
        }

        public Builder status(ApiKeyStatus status) {
            this.status = status;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder lastUsedAt(Instant lastUsedAt) {
            this.lastUsedAt = lastUsedAt;
            return this;
        }

        public Builder lastUsedIp(String lastUsedIp) {
            this.lastUsedIp = lastUsedIp;
            return this;
        }

        public ApiKey build() {
            return new ApiKey(this);
        }
    }
}
