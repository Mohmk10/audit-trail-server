package com.mohmk10.audittrail.integration.webhook.adapter.out.persistence;

import com.mohmk10.audittrail.integration.webhook.domain.WebhookStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "webhooks")
public class WebhookEntity {
    
    @Id
    private UUID id;
    
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, length = 2048)
    private String url;
    
    @Column(nullable = false)
    private String secret;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "webhook_events", joinColumns = @JoinColumn(name = "webhook_id"))
    @Column(name = "event")
    private Set<String> events;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WebhookStatus status;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "webhook_headers", joinColumns = @JoinColumn(name = "webhook_id"))
    @MapKeyColumn(name = "header_name")
    @Column(name = "header_value")
    private Map<String, String> headers;
    
    @Column(name = "max_retries", nullable = false)
    private int maxRetries;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    
    public Set<String> getEvents() { return events; }
    public void setEvents(Set<String> events) { this.events = events; }
    
    public WebhookStatus getStatus() { return status; }
    public void setStatus(WebhookStatus status) { this.status = status; }
    
    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }
    
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
