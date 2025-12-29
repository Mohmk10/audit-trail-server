package com.mohmk10.audittrail.integration.webhook.adapter.out.persistence;

import com.mohmk10.audittrail.integration.webhook.domain.WebhookDelivery;
import org.springframework.stereotype.Component;

@Component
public class WebhookDeliveryMapper {
    
    public WebhookDeliveryEntity toEntity(WebhookDelivery domain) {
        WebhookDeliveryEntity entity = new WebhookDeliveryEntity();
        entity.setId(domain.id());
        entity.setWebhookId(domain.webhookId());
        entity.setEventType(domain.eventType());
        entity.setEventPayload(domain.eventPayload());
        entity.setStatus(domain.status());
        entity.setAttemptCount(domain.attemptCount());
        entity.setHttpStatus(domain.httpStatus());
        entity.setResponseBody(domain.responseBody());
        entity.setErrorMessage(domain.errorMessage());
        entity.setNextRetryAt(domain.nextRetryAt());
        entity.setCreatedAt(domain.createdAt());
        entity.setDeliveredAt(domain.deliveredAt());
        return entity;
    }
    
    public WebhookDelivery toDomain(WebhookDeliveryEntity entity) {
        return new WebhookDelivery(
            entity.getId(),
            entity.getWebhookId(),
            entity.getEventType(),
            entity.getEventPayload(),
            entity.getStatus(),
            entity.getAttemptCount(),
            entity.getHttpStatus(),
            entity.getResponseBody(),
            entity.getErrorMessage(),
            entity.getNextRetryAt(),
            entity.getCreatedAt(),
            entity.getDeliveredAt()
        );
    }
}
