package com.mohmk10.audittrail.integration.webhook.adapter.in.event;

import com.mohmk10.audittrail.integration.webhook.domain.Webhook;
import com.mohmk10.audittrail.integration.webhook.domain.WebhookEvent;
import com.mohmk10.audittrail.integration.webhook.service.WebhookDeliveryService;
import com.mohmk10.audittrail.integration.webhook.service.WebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebhookEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebhookEventListener.class);

    private final WebhookService webhookService;
    private final WebhookDeliveryService deliveryService;

    public WebhookEventListener(WebhookService webhookService, WebhookDeliveryService deliveryService) {
        this.webhookService = webhookService;
        this.deliveryService = deliveryService;
    }

    @Async
    @EventListener
    public void onWebhookEvent(WebhookEvent event) {
        log.debug("Received webhook event: type={}, tenantId={}", event.type(), event.tenantId());

        List<Webhook> activeWebhooks = webhookService.findActiveByTenantId(event.tenantId());

        for (Webhook webhook : activeWebhooks) {
            if (webhook.subscribesTo(event.type())) {
                try {
                    log.debug("Delivering event {} to webhook {}", event.type(), webhook.id());
                    deliveryService.deliverAsync(webhook, event);
                } catch (Exception e) {
                    log.error("Failed to queue delivery for webhook {}: {}",
                        webhook.id(), e.getMessage(), e);
                }
            }
        }
    }
}
