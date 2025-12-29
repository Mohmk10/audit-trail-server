package com.mohmk10.audittrail.integration.webhook.domain;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record Webhook(
    UUID id,
    String tenantId,
    String name,
    String url,
    String secret,
    Set<String> events,
    WebhookStatus status,
    Map<String, String> headers,
    int maxRetries,
    Instant createdAt,
    Instant updatedAt
) {
    public static final int DEFAULT_MAX_RETRIES = 5;
    
    public boolean isActive() {
        return status == WebhookStatus.ACTIVE;
    }
    
    public boolean subscribesTo(String eventType) {
        return events.contains("*") || events.contains(eventType);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private UUID id;
        private String tenantId;
        private String name;
        private String url;
        private String secret;
        private Set<String> events = Set.of("*");
        private WebhookStatus status = WebhookStatus.ACTIVE;
        private Map<String, String> headers = Map.of();
        private int maxRetries = DEFAULT_MAX_RETRIES;
        private Instant createdAt;
        private Instant updatedAt;
        
        public Builder id(UUID id) { this.id = id; return this; }
        public Builder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder url(String url) { this.url = url; return this; }
        public Builder secret(String secret) { this.secret = secret; return this; }
        public Builder events(Set<String> events) { this.events = events; return this; }
        public Builder status(WebhookStatus status) { this.status = status; return this; }
        public Builder headers(Map<String, String> headers) { this.headers = headers; return this; }
        public Builder maxRetries(int maxRetries) { this.maxRetries = maxRetries; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }
        
        public Webhook build() {
            return new Webhook(id, tenantId, name, url, secret, events, status, headers, maxRetries, createdAt, updatedAt);
        }
    }
}
