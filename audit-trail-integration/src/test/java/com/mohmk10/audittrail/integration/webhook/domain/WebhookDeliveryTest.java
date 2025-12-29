package com.mohmk10.audittrail.integration.webhook.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WebhookDelivery Domain Tests")
class WebhookDeliveryTest {

    private static final int MAX_RETRIES = 5;

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create delivery with all fields")
        void shouldCreateDeliveryWithAllFields() {
            UUID id = UUID.randomUUID();
            UUID webhookId = UUID.randomUUID();
            Instant now = Instant.now();

            WebhookDelivery delivery = WebhookDelivery.builder()
                .id(id)
                .webhookId(webhookId)
                .eventType("event.stored")
                .eventPayload("{\"key\": \"value\"}")
                .status(DeliveryStatus.PENDING)
                .attemptCount(0)
                .httpStatus(null)
                .responseBody(null)
                .errorMessage(null)
                .nextRetryAt(null)
                .createdAt(now)
                .deliveredAt(null)
                .build();

            assertThat(delivery.id()).isEqualTo(id);
            assertThat(delivery.webhookId()).isEqualTo(webhookId);
            assertThat(delivery.eventType()).isEqualTo("event.stored");
            assertThat(delivery.eventPayload()).isEqualTo("{\"key\": \"value\"}");
            assertThat(delivery.status()).isEqualTo(DeliveryStatus.PENDING);
            assertThat(delivery.attemptCount()).isZero();
            assertThat(delivery.createdAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("Should create delivered delivery")
        void shouldCreateDeliveredDelivery() {
            Instant now = Instant.now();

            WebhookDelivery delivery = WebhookDelivery.builder()
                .id(UUID.randomUUID())
                .webhookId(UUID.randomUUID())
                .eventType("event.stored")
                .eventPayload("{}")
                .status(DeliveryStatus.DELIVERED)
                .attemptCount(1)
                .httpStatus(200)
                .responseBody("OK")
                .createdAt(now)
                .deliveredAt(now)
                .build();

            assertThat(delivery.status()).isEqualTo(DeliveryStatus.DELIVERED);
            assertThat(delivery.httpStatus()).isEqualTo(200);
            assertThat(delivery.deliveredAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("Should create failed delivery with error")
        void shouldCreateFailedDeliveryWithError() {
            WebhookDelivery delivery = WebhookDelivery.builder()
                .id(UUID.randomUUID())
                .webhookId(UUID.randomUUID())
                .eventType("event.stored")
                .eventPayload("{}")
                .status(DeliveryStatus.FAILED)
                .attemptCount(5)
                .httpStatus(500)
                .errorMessage("Internal Server Error")
                .createdAt(Instant.now())
                .build();

            assertThat(delivery.status()).isEqualTo(DeliveryStatus.FAILED);
            assertThat(delivery.errorMessage()).isEqualTo("Internal Server Error");
        }
    }

    @Nested
    @DisplayName("canRetry Tests")
    class CanRetryTests {

        @Test
        @DisplayName("Should return true when attempts less than max")
        void shouldReturnTrueWhenBelowMaxRetries() {
            WebhookDelivery delivery = createDelivery(DeliveryStatus.RETRYING, 2);
            assertThat(delivery.canRetry(MAX_RETRIES)).isTrue();
        }

        @Test
        @DisplayName("Should return false when attempts equal to max")
        void shouldReturnFalseWhenAtMaxRetries() {
            WebhookDelivery delivery = createDelivery(DeliveryStatus.RETRYING, MAX_RETRIES);
            assertThat(delivery.canRetry(MAX_RETRIES)).isFalse();
        }

        @Test
        @DisplayName("Should return false when attempts exceed max")
        void shouldReturnFalseWhenExceedsMaxRetries() {
            WebhookDelivery delivery = createDelivery(DeliveryStatus.RETRYING, MAX_RETRIES + 1);
            assertThat(delivery.canRetry(MAX_RETRIES)).isFalse();
        }

        @Test
        @DisplayName("Should return false when status is DELIVERED")
        void shouldReturnFalseWhenDelivered() {
            WebhookDelivery delivery = createDelivery(DeliveryStatus.DELIVERED, 1);
            assertThat(delivery.canRetry(MAX_RETRIES)).isFalse();
        }

        @Test
        @DisplayName("Should return false when status is FAILED")
        void shouldReturnFalseWhenFailed() {
            WebhookDelivery delivery = createDelivery(DeliveryStatus.FAILED, 1);
            assertThat(delivery.canRetry(MAX_RETRIES)).isFalse();
        }

        @ParameterizedTest
        @EnumSource(value = DeliveryStatus.class, names = {"PENDING", "RETRYING"})
        @DisplayName("Should allow retry for pending/retrying status")
        void shouldAllowRetryForRetryableStatus(DeliveryStatus status) {
            WebhookDelivery delivery = createDelivery(status, 0);
            assertThat(delivery.canRetry(MAX_RETRIES)).isTrue();
        }
    }

    @Nested
    @DisplayName("isReadyForRetry Tests")
    class IsReadyForRetryTests {

        @Test
        @DisplayName("Should return true when RETRYING and nextRetryAt is past")
        void shouldReturnTrueWhenRetryTimeHasPassed() {
            WebhookDelivery delivery = createDeliveryWithRetryTime(
                DeliveryStatus.RETRYING,
                Instant.now().minusSeconds(60)
            );
            assertThat(delivery.isReadyForRetry()).isTrue();
        }

        @Test
        @DisplayName("Should return false when RETRYING but nextRetryAt is future")
        void shouldReturnFalseWhenRetryTimeInFuture() {
            WebhookDelivery delivery = createDeliveryWithRetryTime(
                DeliveryStatus.RETRYING,
                Instant.now().plusSeconds(60)
            );
            assertThat(delivery.isReadyForRetry()).isFalse();
        }

        @Test
        @DisplayName("Should return false when nextRetryAt is null")
        void shouldReturnFalseWhenNextRetryAtIsNull() {
            WebhookDelivery delivery = createDeliveryWithRetryTime(
                DeliveryStatus.RETRYING,
                null
            );
            assertThat(delivery.isReadyForRetry()).isFalse();
        }

        @Test
        @DisplayName("Should return false when status is not RETRYING")
        void shouldReturnFalseWhenNotRetrying() {
            WebhookDelivery delivery = createDeliveryWithRetryTime(
                DeliveryStatus.PENDING,
                Instant.now().minusSeconds(60)
            );
            assertThat(delivery.isReadyForRetry()).isFalse();
        }

        @Test
        @DisplayName("Should return false when DELIVERED")
        void shouldReturnFalseWhenDelivered() {
            WebhookDelivery delivery = createDeliveryWithRetryTime(
                DeliveryStatus.DELIVERED,
                Instant.now().minusSeconds(60)
            );
            assertThat(delivery.isReadyForRetry()).isFalse();
        }
    }

    private WebhookDelivery createDelivery(DeliveryStatus status, int attemptCount) {
        return WebhookDelivery.builder()
            .id(UUID.randomUUID())
            .webhookId(UUID.randomUUID())
            .eventType("event.stored")
            .eventPayload("{}")
            .status(status)
            .attemptCount(attemptCount)
            .createdAt(Instant.now())
            .build();
    }

    private WebhookDelivery createDeliveryWithRetryTime(DeliveryStatus status, Instant nextRetryAt) {
        return WebhookDelivery.builder()
            .id(UUID.randomUUID())
            .webhookId(UUID.randomUUID())
            .eventType("event.stored")
            .eventPayload("{}")
            .status(status)
            .attemptCount(1)
            .nextRetryAt(nextRetryAt)
            .createdAt(Instant.now())
            .build();
    }
}
