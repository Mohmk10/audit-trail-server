package com.mohmk10.audittrail.integration.webhook.adapter.in.rest;

import com.mohmk10.audittrail.integration.webhook.domain.Webhook;
import com.mohmk10.audittrail.integration.webhook.domain.WebhookStatus;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record WebhookResponse(
    UUID id,
    String tenantId,
    String name,
    String url,
    Set<String> events,
    WebhookStatus status,
    Map<String, String> headers,
    int maxRetries,
    Instant createdAt,
    Instant updatedAt
) {
    public static WebhookResponse from(Webhook webhook) {
        return new WebhookResponse(
            webhook.id(),
            webhook.tenantId(),
            webhook.name(),
            webhook.url(),
            webhook.events(),
            webhook.status(),
            webhook.headers(),
            webhook.maxRetries(),
            webhook.createdAt(),
            webhook.updatedAt()
        );
    }
}
