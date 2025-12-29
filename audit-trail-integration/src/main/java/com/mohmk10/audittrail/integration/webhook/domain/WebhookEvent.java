package com.mohmk10.audittrail.integration.webhook.domain;

import java.time.Instant;
import java.util.Map;

public record WebhookEvent(
    String type,
    String tenantId,
    Instant timestamp,
    Map<String, Object> payload
) {
    public static final String EVENT_STORED = "event.stored";
    public static final String ALERT_CREATED = "alert.created";
    public static final String REPORT_GENERATED = "report.generated";
    public static final String TEST = "test";

    public static WebhookEvent of(String type, String tenantId, Map<String, Object> payload) {
        return new WebhookEvent(type, tenantId, Instant.now(), payload);
    }

    public static WebhookEvent test(String tenantId) {
        return new WebhookEvent(TEST, tenantId, Instant.now(), Map.of("message", "This is a test webhook event"));
    }
}
