package com.mohmk10.audittrail.integration.webhook.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Webhook Domain Tests")
class WebhookTest {

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create webhook with all fields")
        void shouldCreateWebhookWithAllFields() {
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();
            Set<String> events = Set.of("event.stored", "alert.created");
            Map<String, String> headers = Map.of("X-Custom", "value");

            Webhook webhook = Webhook.builder()
                .id(id)
                .tenantId("tenant-1")
                .name("Test Webhook")
                .url("https://example.com/webhook")
                .secret("whsec_test")
                .events(events)
                .status(WebhookStatus.ACTIVE)
                .headers(headers)
                .maxRetries(5)
                .createdAt(now)
                .updatedAt(now)
                .build();

            assertThat(webhook.id()).isEqualTo(id);
            assertThat(webhook.tenantId()).isEqualTo("tenant-1");
            assertThat(webhook.name()).isEqualTo("Test Webhook");
            assertThat(webhook.url()).isEqualTo("https://example.com/webhook");
            assertThat(webhook.secret()).isEqualTo("whsec_test");
            assertThat(webhook.events()).containsExactlyInAnyOrderElementsOf(events);
            assertThat(webhook.status()).isEqualTo(WebhookStatus.ACTIVE);
            assertThat(webhook.headers()).containsAllEntriesOf(headers);
            assertThat(webhook.maxRetries()).isEqualTo(5);
            assertThat(webhook.createdAt()).isEqualTo(now);
            assertThat(webhook.updatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("Should use default max retries when not specified")
        void shouldUseDefaultMaxRetries() {
            Webhook webhook = Webhook.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-1")
                .name("Test")
                .url("https://example.com")
                .secret("secret")
                .events(Set.of())
                .status(WebhookStatus.ACTIVE)
                .headers(Map.of())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

            assertThat(webhook.maxRetries()).isEqualTo(Webhook.DEFAULT_MAX_RETRIES);
        }
    }

    @Nested
    @DisplayName("isActive Tests")
    class IsActiveTests {

        @Test
        @DisplayName("Should return true when status is ACTIVE")
        void shouldReturnTrueWhenActive() {
            Webhook webhook = createWebhook(WebhookStatus.ACTIVE);
            assertThat(webhook.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should return false when status is INACTIVE")
        void shouldReturnFalseWhenInactive() {
            Webhook webhook = createWebhook(WebhookStatus.INACTIVE);
            assertThat(webhook.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should return false when status is SUSPENDED")
        void shouldReturnFalseWhenSuspended() {
            Webhook webhook = createWebhook(WebhookStatus.SUSPENDED);
            assertThat(webhook.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("subscribesTo Tests")
    class SubscribesToTests {

        @Test
        @DisplayName("Should return true when subscribed to exact event type")
        void shouldReturnTrueForExactMatch() {
            Webhook webhook = createWebhookWithEvents(Set.of("event.stored", "alert.created"));
            assertThat(webhook.subscribesTo("event.stored")).isTrue();
        }

        @Test
        @DisplayName("Should return false when not subscribed to event type")
        void shouldReturnFalseWhenNotSubscribed() {
            Webhook webhook = createWebhookWithEvents(Set.of("event.stored"));
            assertThat(webhook.subscribesTo("alert.created")).isFalse();
        }

        @Test
        @DisplayName("Should return true when subscribed to wildcard *")
        void shouldReturnTrueForWildcard() {
            Webhook webhook = createWebhookWithEvents(Set.of("*"));
            assertThat(webhook.subscribesTo("any.event")).isTrue();
            assertThat(webhook.subscribesTo("another.event")).isTrue();
        }

        @Test
        @DisplayName("Should return false for empty events set")
        void shouldReturnFalseForEmptyEvents() {
            Webhook webhook = createWebhookWithEvents(Set.of());
            assertThat(webhook.subscribesTo("event.stored")).isFalse();
        }
    }

    private Webhook createWebhook(WebhookStatus status) {
        return Webhook.builder()
            .id(UUID.randomUUID())
            .tenantId("tenant-1")
            .name("Test")
            .url("https://example.com")
            .secret("secret")
            .events(Set.of())
            .status(status)
            .headers(Map.of())
            .maxRetries(5)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    }

    private Webhook createWebhookWithEvents(Set<String> events) {
        return Webhook.builder()
            .id(UUID.randomUUID())
            .tenantId("tenant-1")
            .name("Test")
            .url("https://example.com")
            .secret("secret")
            .events(events)
            .status(WebhookStatus.ACTIVE)
            .headers(Map.of())
            .maxRetries(5)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    }
}
