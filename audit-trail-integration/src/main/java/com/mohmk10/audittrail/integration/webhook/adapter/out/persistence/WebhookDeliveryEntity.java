package com.mohmk10.audittrail.integration.webhook.adapter.out.persistence;

import com.mohmk10.audittrail.integration.webhook.domain.DeliveryStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "webhook_deliveries")
public class WebhookDeliveryEntity {
    
    @Id
    private UUID id;
    
    @Column(name = "webhook_id", nullable = false)
    private UUID webhookId;
    
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;
    
    @Column(name = "event_payload", nullable = false, columnDefinition = "TEXT")
    private String eventPayload;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;
    
    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;
    
    @Column(name = "http_status")
    private Integer httpStatus;
    
    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;
    
    @Column(name = "error_message", length = 500)
    private String errorMessage;
    
    @Column(name = "next_retry_at")
    private Instant nextRetryAt;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "delivered_at")
    private Instant deliveredAt;
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getWebhookId() { return webhookId; }
    public void setWebhookId(UUID webhookId) { this.webhookId = webhookId; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public String getEventPayload() { return eventPayload; }
    public void setEventPayload(String eventPayload) { this.eventPayload = eventPayload; }
    
    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }
    
    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
    
    public Integer getHttpStatus() { return httpStatus; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }
    
    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public Instant getNextRetryAt() { return nextRetryAt; }
    public void setNextRetryAt(Instant nextRetryAt) { this.nextRetryAt = nextRetryAt; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }
}
