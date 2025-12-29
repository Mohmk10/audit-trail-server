package com.mohmk10.audittrail.integration.webhook.adapter.in.event;

import com.mohmk10.audittrail.integration.webhook.domain.Webhook;
import com.mohmk10.audittrail.integration.webhook.domain.WebhookEvent;
import com.mohmk10.audittrail.integration.webhook.domain.WebhookStatus;
import com.mohmk10.audittrail.integration.webhook.service.WebhookDeliveryService;
import com.mohmk10.audittrail.integration.webhook.service.WebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookEventListener Tests")
class WebhookEventListenerTest {

    @Mock
    private WebhookService webhookService;

    @Mock
    private WebhookDeliveryService deliveryService;

    private WebhookEventListener eventListener;

    @BeforeEach
    void setUp() {
        eventListener = new WebhookEventListener(webhookService, deliveryService);
    }

    @Nested
    @DisplayName("onWebhookEvent() Tests")
    class OnWebhookEventTests {

        @Test
        @DisplayName("Should deliver event to subscribed webhooks")
        void shouldDeliverEventToSubscribedWebhooks() {
            WebhookEvent event = WebhookEvent.of(
                "event.stored",
                "tenant-1",
                Map.of("key", "value")
            );

            Webhook webhook = createWebhookSubscribedTo(Set.of("event.stored"));
            when(webhookService.findActiveByTenantId("tenant-1"))
                .thenReturn(List.of(webhook));

            eventListener.onWebhookEvent(event);

            verify(deliveryService).deliverAsync(webhook, event);
        }

        @Test
        @DisplayName("Should not deliver to unsubscribed webhooks")
        void shouldNotDeliverToUnsubscribedWebhooks() {
            WebhookEvent event = WebhookEvent.of(
                "event.stored",
                "tenant-1",
                Map.of()
            );

            Webhook webhook = createWebhookSubscribedTo(Set.of("alert.created"));
            when(webhookService.findActiveByTenantId("tenant-1"))
                .thenReturn(List.of(webhook));

            eventListener.onWebhookEvent(event);

            verify(deliveryService, never()).deliverAsync(any(), any());
        }

        @Test
        @DisplayName("Should deliver to webhooks with wildcard subscription")
        void shouldDeliverToWebhooksWithWildcard() {
            WebhookEvent event = WebhookEvent.of(
                "any.event.type",
                "tenant-1",
                Map.of()
            );

            Webhook webhook = createWebhookSubscribedTo(Set.of("*"));
            when(webhookService.findActiveByTenantId("tenant-1"))
                .thenReturn(List.of(webhook));

            eventListener.onWebhookEvent(event);

            verify(deliveryService).deliverAsync(webhook, event);
        }

        @Test
        @DisplayName("Should deliver to multiple webhooks")
        void shouldDeliverToMultipleWebhooks() {
            WebhookEvent event = WebhookEvent.of(
                "event.stored",
                "tenant-1",
                Map.of()
            );

            Webhook webhook1 = createWebhookSubscribedTo(Set.of("event.stored"));
            Webhook webhook2 = createWebhookSubscribedTo(Set.of("*"));
            when(webhookService.findActiveByTenantId("tenant-1"))
                .thenReturn(List.of(webhook1, webhook2));

            eventListener.onWebhookEvent(event);

            verify(deliveryService).deliverAsync(webhook1, event);
            verify(deliveryService).deliverAsync(webhook2, event);
        }

        @Test
        @DisplayName("Should handle no active webhooks")
        void shouldHandleNoActiveWebhooks() {
            WebhookEvent event = WebhookEvent.of(
                "event.stored",
                "tenant-1",
                Map.of()
            );

            when(webhookService.findActiveByTenantId("tenant-1"))
                .thenReturn(List.of());

            eventListener.onWebhookEvent(event);

            verify(deliveryService, never()).deliverAsync(any(), any());
        }

        @Test
        @DisplayName("Should continue on delivery failure")
        void shouldContinueOnDeliveryFailure() {
            WebhookEvent event = WebhookEvent.of(
                "event.stored",
                "tenant-1",
                Map.of()
            );

            Webhook webhook1 = createWebhookSubscribedTo(Set.of("event.stored"));
            Webhook webhook2 = createWebhookSubscribedTo(Set.of("event.stored"));
            when(webhookService.findActiveByTenantId("tenant-1"))
                .thenReturn(List.of(webhook1, webhook2));

            doThrow(new RuntimeException("Delivery failed"))
                .when(deliveryService).deliverAsync(eq(webhook1), any());

            eventListener.onWebhookEvent(event);

            verify(deliveryService).deliverAsync(webhook1, event);
            verify(deliveryService).deliverAsync(webhook2, event);
        }
    }

    private Webhook createWebhookSubscribedTo(Set<String> events) {
        Instant now = Instant.now();
        return Webhook.builder()
            .id(UUID.randomUUID())
            .tenantId("tenant-1")
            .name("Test Webhook")
            .url("https://example.com/webhook")
            .secret("whsec_test")
            .events(events)
            .status(WebhookStatus.ACTIVE)
            .headers(Map.of())
            .maxRetries(5)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }
}
