package com.mohmk10.audittrail.admin.domain;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class AdminAuditLog {
    private UUID id;
    private String tenantId;
    private UUID actorId;
    private String actorEmail;
    private AdminAction action;
    private String resourceType;
    private String resourceId;
    private Map<String, Object> previousState;
    private Map<String, Object> newState;
    private String ipAddress;
    private String userAgent;
    private Instant timestamp;
    private String details;

    public AdminAuditLog() {
    }

    private AdminAuditLog(Builder builder) {
        this.id = builder.id;
        this.tenantId = builder.tenantId;
        this.actorId = builder.actorId;
        this.actorEmail = builder.actorEmail;
        this.action = builder.action;
        this.resourceType = builder.resourceType;
        this.resourceId = builder.resourceId;
        this.previousState = builder.previousState;
        this.newState = builder.newState;
        this.ipAddress = builder.ipAddress;
        this.userAgent = builder.userAgent;
        this.timestamp = builder.timestamp;
        this.details = builder.details;
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

    public UUID getActorId() {
        return actorId;
    }

    public void setActorId(UUID actorId) {
        this.actorId = actorId;
    }

    public String getActorEmail() {
        return actorEmail;
    }

    public void setActorEmail(String actorEmail) {
        this.actorEmail = actorEmail;
    }

    public AdminAction getAction() {
        return action;
    }

    public void setAction(AdminAction action) {
        this.action = action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public Map<String, Object> getPreviousState() {
        return previousState;
    }

    public void setPreviousState(Map<String, Object> previousState) {
        this.previousState = previousState;
    }

    public Map<String, Object> getNewState() {
        return newState;
    }

    public void setNewState(Map<String, Object> newState) {
        this.newState = newState;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public static class Builder {
        private UUID id;
        private String tenantId;
        private UUID actorId;
        private String actorEmail;
        private AdminAction action;
        private String resourceType;
        private String resourceId;
        private Map<String, Object> previousState;
        private Map<String, Object> newState;
        private String ipAddress;
        private String userAgent;
        private Instant timestamp;
        private String details;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder actorId(UUID actorId) {
            this.actorId = actorId;
            return this;
        }

        public Builder actorEmail(String actorEmail) {
            this.actorEmail = actorEmail;
            return this;
        }

        public Builder action(AdminAction action) {
            this.action = action;
            return this;
        }

        public Builder resourceType(String resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public Builder resourceId(String resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public Builder previousState(Map<String, Object> previousState) {
            this.previousState = previousState;
            return this;
        }

        public Builder newState(Map<String, Object> newState) {
            this.newState = newState;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder details(String details) {
            this.details = details;
            return this;
        }

        public AdminAuditLog build() {
            return new AdminAuditLog(this);
        }
    }
}
