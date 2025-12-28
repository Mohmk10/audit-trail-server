package com.mohmk10.audittrail.storage.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "events")
public class EventEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "actor_id", nullable = false)
    private String actorId;

    @Column(name = "actor_type", nullable = false)
    private String actorType;

    @Column(name = "actor_name", nullable = false)
    private String actorName;

    @Column(name = "actor_ip")
    private String actorIp;

    @Column(name = "actor_user_agent")
    private String actorUserAgent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "actor_attributes", columnDefinition = "jsonb")
    private Map<String, String> actorAttributes;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "action_description")
    private String actionDescription;

    @Column(name = "action_category")
    private String actionCategory;

    @Column(name = "resource_id", nullable = false)
    private String resourceId;

    @Column(name = "resource_type", nullable = false)
    private String resourceType;

    @Column(name = "resource_name", nullable = false)
    private String resourceName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resource_before", columnDefinition = "jsonb")
    private Map<String, Object> resourceBefore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resource_after", columnDefinition = "jsonb")
    private Map<String, Object> resourceAfter;

    @Column(name = "metadata_source")
    private String metadataSource;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "session_id")
    private String sessionId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "jsonb")
    private Map<String, String> tags;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extra", columnDefinition = "jsonb")
    private Map<String, Object> extra;

    @Column(name = "previous_hash")
    private String previousHash;

    @Column(name = "hash", nullable = false)
    private String hash;

    @Column(name = "signature")
    private String signature;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }

    public String getActorType() {
        return actorType;
    }

    public void setActorType(String actorType) {
        this.actorType = actorType;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getActorIp() {
        return actorIp;
    }

    public void setActorIp(String actorIp) {
        this.actorIp = actorIp;
    }

    public String getActorUserAgent() {
        return actorUserAgent;
    }

    public void setActorUserAgent(String actorUserAgent) {
        this.actorUserAgent = actorUserAgent;
    }

    public Map<String, String> getActorAttributes() {
        return actorAttributes;
    }

    public void setActorAttributes(Map<String, String> actorAttributes) {
        this.actorAttributes = actorAttributes;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionDescription() {
        return actionDescription;
    }

    public void setActionDescription(String actionDescription) {
        this.actionDescription = actionDescription;
    }

    public String getActionCategory() {
        return actionCategory;
    }

    public void setActionCategory(String actionCategory) {
        this.actionCategory = actionCategory;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public Map<String, Object> getResourceBefore() {
        return resourceBefore;
    }

    public void setResourceBefore(Map<String, Object> resourceBefore) {
        this.resourceBefore = resourceBefore;
    }

    public Map<String, Object> getResourceAfter() {
        return resourceAfter;
    }

    public void setResourceAfter(Map<String, Object> resourceAfter) {
        this.resourceAfter = resourceAfter;
    }

    public String getMetadataSource() {
        return metadataSource;
    }

    public void setMetadataSource(String metadataSource) {
        this.metadataSource = metadataSource;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
