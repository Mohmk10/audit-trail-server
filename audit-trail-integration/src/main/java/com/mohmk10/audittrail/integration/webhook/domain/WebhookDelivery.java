package com.mohmk10.audittrail.integration.webhook.domain;

import java.time.Instant;
import java.util.UUID;

public record WebhookDelivery(
    UUID id,
    UUID webhookId,
    String eventType,
    String eventPayload,
    DeliveryStatus status,
    int attemptCount,
    Integer httpStatus,
    String responseBody,
    String errorMessage,
    Instant nextRetryAt,
    Instant createdAt,
    Instant deliveredAt
) {
    public boolean canRetry(int maxRetries) {
        return attemptCount < maxRetries && status != DeliveryStatus.DELIVERED && status != DeliveryStatus.FAILED;
    }
    
    public boolean isReadyForRetry() {
        return status == DeliveryStatus.RETRYING 
            && nextRetryAt != null 
            && nextRetryAt.isBefore(Instant.now());
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private UUID id;
        private UUID webhookId;
        private String eventType;
        private String eventPayload;
        private DeliveryStatus status = DeliveryStatus.PENDING;
        private int attemptCount = 0;
        private Integer httpStatus;
        private String responseBody;
        private String errorMessage;
        private Instant nextRetryAt;
        private Instant createdAt;
        private Instant deliveredAt;
        
        public Builder id(UUID id) { this.id = id; return this; }
        public Builder webhookId(UUID webhookId) { this.webhookId = webhookId; return this; }
        public Builder eventType(String eventType) { this.eventType = eventType; return this; }
        public Builder eventPayload(String eventPayload) { this.eventPayload = eventPayload; return this; }
        public Builder status(DeliveryStatus status) { this.status = status; return this; }
        public Builder attemptCount(int attemptCount) { this.attemptCount = attemptCount; return this; }
        public Builder httpStatus(Integer httpStatus) { this.httpStatus = httpStatus; return this; }
        public Builder responseBody(String responseBody) { this.responseBody = responseBody; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public Builder nextRetryAt(Instant nextRetryAt) { this.nextRetryAt = nextRetryAt; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder deliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; return this; }
        
        public WebhookDelivery build() {
            return new WebhookDelivery(id, webhookId, eventType, eventPayload, status, attemptCount, 
                httpStatus, responseBody, errorMessage, nextRetryAt, createdAt, deliveredAt);
        }
    }
}
