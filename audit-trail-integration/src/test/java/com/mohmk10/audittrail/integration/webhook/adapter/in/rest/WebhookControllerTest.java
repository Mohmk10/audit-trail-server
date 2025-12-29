package com.mohmk10.audittrail.integration.webhook.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohmk10.audittrail.integration.webhook.domain.*;
import com.mohmk10.audittrail.integration.webhook.service.WebhookDeliveryService;
import com.mohmk10.audittrail.integration.webhook.service.WebhookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WebhookController.class)
@DisplayName("WebhookController Tests")
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebhookService webhookService;

    @MockBean
    private WebhookDeliveryService deliveryService;

    private static final String TENANT_ID = "tenant-1";

    @Nested
    @DisplayName("POST /api/v1/webhooks")
    class CreateWebhookTests {

        @Test
        @DisplayName("Should create webhook successfully")
        void shouldCreateWebhookSuccessfully() throws Exception {
            WebhookRequest request = new WebhookRequest(
                "Test Webhook",
                "https://example.com/webhook",
                Set.of("event.stored"),
                Map.of(),
                5
            );

            Webhook webhook = createWebhook();
            when(webhookService.create(any(Webhook.class))).thenReturn(webhook);

            mockMvc.perform(post("/api/v1/webhooks")
                    .header("X-Tenant-ID", TENANT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(webhook.id().toString()))
                .andExpect(jsonPath("$.name").value("Test Webhook"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("Should return 400 for missing name")
        void shouldReturn400ForMissingName() throws Exception {
            WebhookRequest request = new WebhookRequest(
                null,
                "https://example.com/webhook",
                Set.of("event.stored"),
                Map.of(),
                5
            );

            mockMvc.perform(post("/api/v1/webhooks")
                    .header("X-Tenant-ID", TENANT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for invalid URL")
        void shouldReturn400ForInvalidUrl() throws Exception {
            WebhookRequest request = new WebhookRequest(
                "Test Webhook",
                "not-a-url",
                Set.of("event.stored"),
                Map.of(),
                5
            );

            mockMvc.perform(post("/api/v1/webhooks")
                    .header("X-Tenant-ID", TENANT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for empty events")
        void shouldReturn400ForEmptyEvents() throws Exception {
            WebhookRequest request = new WebhookRequest(
                "Test Webhook",
                "https://example.com/webhook",
                Set.of(),
                Map.of(),
                5
            );

            mockMvc.perform(post("/api/v1/webhooks")
                    .header("X-Tenant-ID", TENANT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/webhooks")
    class ListWebhooksTests {

        @Test
        @DisplayName("Should list webhooks for tenant")
        void shouldListWebhooksForTenant() throws Exception {
            Webhook webhook1 = createWebhook();
            Webhook webhook2 = createWebhook();
            when(webhookService.findByTenantId(TENANT_ID))
                .thenReturn(List.of(webhook1, webhook2));

            mockMvc.perform(get("/api/v1/webhooks")
                    .header("X-Tenant-ID", TENANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("Should return empty list when no webhooks")
        void shouldReturnEmptyListWhenNoWebhooks() throws Exception {
            when(webhookService.findByTenantId(TENANT_ID)).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/webhooks")
                    .header("X-Tenant-ID", TENANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/webhooks/{id}")
    class GetWebhookTests {

        @Test
        @DisplayName("Should return webhook when found")
        void shouldReturnWebhookWhenFound() throws Exception {
            Webhook webhook = createWebhook();
            when(webhookService.findById(webhook.id())).thenReturn(Optional.of(webhook));

            mockMvc.perform(get("/api/v1/webhooks/{id}", webhook.id())
                    .header("X-Tenant-ID", TENANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(webhook.id().toString()));
        }

        @Test
        @DisplayName("Should return 404 when not found")
        void shouldReturn404WhenNotFound() throws Exception {
            UUID id = UUID.randomUUID();
            when(webhookService.findById(id)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/webhooks/{id}", id)
                    .header("X-Tenant-ID", TENANT_ID))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 for different tenant")
        void shouldReturn404ForDifferentTenant() throws Exception {
            Webhook webhook = createWebhookForTenant("other-tenant");
            when(webhookService.findById(webhook.id())).thenReturn(Optional.of(webhook));

            mockMvc.perform(get("/api/v1/webhooks/{id}", webhook.id())
                    .header("X-Tenant-ID", TENANT_ID))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/webhooks/{id}")
    class UpdateWebhookTests {

        @Test
        @DisplayName("Should update webhook successfully")
        void shouldUpdateWebhookSuccessfully() throws Exception {
            Webhook existing = createWebhook();
            Webhook updated = createWebhookWithName("Updated Name");
            WebhookRequest request = new WebhookRequest(
                "Updated Name",
                "https://new.url",
                Set.of("event.stored"),
                Map.of(),
                5
            );

            when(webhookService.findById(existing.id())).thenReturn(Optional.of(existing));
            when(webhookService.update(eq(existing.id()), any(Webhook.class)))
                .thenReturn(updated);

            mockMvc.perform(put("/api/v1/webhooks/{id}", existing.id())
                    .header("X-Tenant-ID", TENANT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/webhooks/{id}")
    class DeleteWebhookTests {

        @Test
        @DisplayName("Should delete webhook successfully")
        void shouldDeleteWebhookSuccessfully() throws Exception {
            Webhook webhook = createWebhook();
            when(webhookService.findById(webhook.id())).thenReturn(Optional.of(webhook));

            mockMvc.perform(delete("/api/v1/webhooks/{id}", webhook.id())
                    .header("X-Tenant-ID", TENANT_ID))
                .andExpect(status().isNoContent());

            verify(webhookService).delete(webhook.id());
        }

        @Test
        @DisplayName("Should return 404 when not found")
        void shouldReturn404WhenNotFound() throws Exception {
            UUID id = UUID.randomUUID();
            when(webhookService.findById(id)).thenReturn(Optional.empty());

            mockMvc.perform(delete("/api/v1/webhooks/{id}", id)
                    .header("X-Tenant-ID", TENANT_ID))
                .andExpect(status().isNotFound());

            verify(webhookService, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/webhooks/{id}/activate")
    class ActivateWebhookTests {

        @Test
        @DisplayName("Should activate webhook")
        void shouldActivateWebhook() throws Exception {
            Webhook webhook = createWebhookWithStatus(WebhookStatus.INACTIVE);
            Webhook activated = createWebhookWithStatus(WebhookStatus.ACTIVE);

            when(webhookService.findById(webhook.id())).thenReturn(Optional.of(webhook));
            when(webhookService.activate(webhook.id())).thenReturn(activated);

            mockMvc.perform(post("/api/v1/webhooks/{id}/activate", webhook.id())
                    .header("X-Tenant-ID", TENANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/webhooks/{id}/deactivate")
    class DeactivateWebhookTests {

        @Test
        @DisplayName("Should deactivate webhook")
        void shouldDeactivateWebhook() throws Exception {
            Webhook webhook = createWebhookWithStatus(WebhookStatus.ACTIVE);
            Webhook deactivated = createWebhookWithStatus(WebhookStatus.INACTIVE);

            when(webhookService.findById(webhook.id())).thenReturn(Optional.of(webhook));
            when(webhookService.deactivate(webhook.id())).thenReturn(deactivated);

            mockMvc.perform(post("/api/v1/webhooks/{id}/deactivate", webhook.id())
                    .header("X-Tenant-ID", TENANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/webhooks/{id}/rotate-secret")
    class RotateSecretTests {

        @Test
        @DisplayName("Should rotate secret")
        void shouldRotateSecret() throws Exception {
            Webhook webhook = createWebhook();
            Webhook rotated = createWebhookWithSecret("whsec_new");

            when(webhookService.findById(webhook.id())).thenReturn(Optional.of(webhook));
            when(webhookService.generateSecret()).thenReturn("whsec_new");
            when(webhookService.update(eq(webhook.id()), any(Webhook.class))).thenReturn(rotated);

            mockMvc.perform(post("/api/v1/webhooks/{id}/rotate-secret", webhook.id())
                    .header("X-Tenant-ID", TENANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.webhookId").value(webhook.id().toString()))
                .andExpect(jsonPath("$.secret").value("whsec_new"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/webhooks/{id}/test")
    class TestWebhookTests {

        @Test
        @DisplayName("Should send test event")
        void shouldSendTestEvent() throws Exception {
            Webhook webhook = createWebhook();
            WebhookDelivery delivery = createDelivery(DeliveryStatus.DELIVERED);

            when(webhookService.findById(webhook.id())).thenReturn(Optional.of(webhook));
            when(deliveryService.deliver(eq(webhook), any(WebhookEvent.class)))
                .thenReturn(delivery);

            mockMvc.perform(post("/api/v1/webhooks/{id}/test", webhook.id())
                    .header("X-Tenant-ID", TENANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/webhooks/{id}/deliveries")
    class GetDeliveriesTests {

        @Test
        @DisplayName("Should list deliveries for webhook")
        void shouldListDeliveriesForWebhook() throws Exception {
            Webhook webhook = createWebhook();
            WebhookDelivery delivery = createDelivery(DeliveryStatus.DELIVERED);

            when(webhookService.findById(webhook.id())).thenReturn(Optional.of(webhook));
            when(deliveryService.findByWebhookId(webhook.id()))
                .thenReturn(List.of(delivery));

            mockMvc.perform(get("/api/v1/webhooks/{id}/deliveries", webhook.id())
                    .header("X-Tenant-ID", TENANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        }
    }

    private Webhook createWebhook() {
        return createWebhookForTenant(TENANT_ID);
    }

    private Webhook createWebhookForTenant(String tenantId) {
        Instant now = Instant.now();
        return Webhook.builder()
            .id(UUID.randomUUID())
            .tenantId(tenantId)
            .name("Test Webhook")
            .url("https://example.com/webhook")
            .secret("whsec_test")
            .events(Set.of("event.stored"))
            .status(WebhookStatus.ACTIVE)
            .headers(Map.of())
            .maxRetries(5)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    private Webhook createWebhookWithName(String name) {
        Instant now = Instant.now();
        return Webhook.builder()
            .id(UUID.randomUUID())
            .tenantId(TENANT_ID)
            .name(name)
            .url("https://example.com/webhook")
            .secret("whsec_test")
            .events(Set.of("event.stored"))
            .status(WebhookStatus.ACTIVE)
            .headers(Map.of())
            .maxRetries(5)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    private Webhook createWebhookWithStatus(WebhookStatus status) {
        Instant now = Instant.now();
        return Webhook.builder()
            .id(UUID.randomUUID())
            .tenantId(TENANT_ID)
            .name("Test Webhook")
            .url("https://example.com/webhook")
            .secret("whsec_test")
            .events(Set.of("event.stored"))
            .status(status)
            .headers(Map.of())
            .maxRetries(5)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    private Webhook createWebhookWithSecret(String secret) {
        Instant now = Instant.now();
        return Webhook.builder()
            .id(UUID.randomUUID())
            .tenantId(TENANT_ID)
            .name("Test Webhook")
            .url("https://example.com/webhook")
            .secret(secret)
            .events(Set.of("event.stored"))
            .status(WebhookStatus.ACTIVE)
            .headers(Map.of())
            .maxRetries(5)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    private WebhookDelivery createDelivery(DeliveryStatus status) {
        return WebhookDelivery.builder()
            .id(UUID.randomUUID())
            .webhookId(UUID.randomUUID())
            .eventType("test")
            .eventPayload("{}")
            .status(status)
            .attemptCount(1)
            .httpStatus(200)
            .createdAt(Instant.now())
            .deliveredAt(Instant.now())
            .build();
    }
}
