package com.mohmk10.audittrail.integration.webhook.service;

import com.mohmk10.audittrail.integration.webhook.domain.Webhook;
import com.mohmk10.audittrail.integration.webhook.domain.WebhookDelivery;
import com.mohmk10.audittrail.integration.webhook.domain.WebhookEvent;

import java.util.List;
import java.util.UUID;

public interface WebhookDeliveryService {
    WebhookDelivery deliver(Webhook webhook, WebhookEvent event);
    void deliverAsync(Webhook webhook, WebhookEvent event);
    List<WebhookDelivery> findByWebhookId(UUID webhookId);
    List<WebhookDelivery> findPendingRetries();
    WebhookDelivery retry(UUID deliveryId);
    String calculateSignature(String payload, String secret);
}
