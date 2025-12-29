package com.mohmk10.audittrail.integration.webhook.adapter.in.rest;

import com.mohmk10.audittrail.integration.webhook.domain.DeliveryStatus;
import com.mohmk10.audittrail.integration.webhook.domain.WebhookDelivery;

import java.time.Instant;
import java.util.UUID;

public record WebhookDeliveryResponse(
    UUID id,
    UUID webhookId,
    String eventType,
    DeliveryStatus status,
    int attemptCount,
    Integer httpStatus,
    String errorMessage,
    Instant nextRetryAt,
    Instant createdAt,
    Instant deliveredAt
) {
    public static WebhookDeliveryResponse from(WebhookDelivery delivery) {
        return new WebhookDeliveryResponse(
            delivery.id(),
            delivery.webhookId(),
            delivery.eventType(),
            delivery.status(),
            delivery.attemptCount(),
            delivery.httpStatus(),
            delivery.errorMessage(),
            delivery.nextRetryAt(),
            delivery.createdAt(),
            delivery.deliveredAt()
        );
    }
}
