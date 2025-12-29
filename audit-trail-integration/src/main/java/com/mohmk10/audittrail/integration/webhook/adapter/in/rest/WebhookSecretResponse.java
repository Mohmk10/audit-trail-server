package com.mohmk10.audittrail.integration.webhook.adapter.in.rest;

import java.util.UUID;

public record WebhookSecretResponse(
    UUID webhookId,
    String secret
) {}
