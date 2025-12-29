package com.mohmk10.audittrail.integration.webhook.domain;

import java.util.UUID;

public class WebhookNotFoundException extends RuntimeException {
    private final UUID webhookId;
    
    public WebhookNotFoundException(UUID webhookId) {
        super("Webhook not found: " + webhookId);
        this.webhookId = webhookId;
    }
    
    public UUID getWebhookId() {
        return webhookId;
    }
}
