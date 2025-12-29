package com.mohmk10.audittrail.integration.webhook.adapter.out.persistence;

import com.mohmk10.audittrail.integration.webhook.domain.DeliveryStatus;
import com.mohmk10.audittrail.integration.webhook.domain.WebhookDelivery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WebhookDeliveryMapper Tests")
class WebhookDeliveryMapperTest {

    private WebhookDeliveryMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new WebhookDeliveryMapper();
    }

    @Nested
    @DisplayName("toEntity() Tests")
    class ToEntityTests {

        @Test
        @DisplayName("Should map all fields from domain to entity")
        void shouldMapAllFieldsFromDomainToEntity() {
            UUID id = UUID.randomUUID();
            UUID webhookId = UUID.randomUUID();
            Instant now = Instant.now();
            Instant nextRetry = now.plusSeconds(300);

            WebhookDelivery domain = WebhookDelivery.builder()
                .id(id)
                .webhookId(webhookId)
                .eventType("event.stored")
                .eventPayload("{\"key\": \"value\"}")
                .status(DeliveryStatus.RETRYING)
                .attemptCount(2)
                .httpStatus(500)
                .responseBody("Error")
                .errorMessage("Internal Server Error")
                .nextRetryAt(nextRetry)
                .createdAt(now)
                .deliveredAt(null)
                .build();

            WebhookDeliveryEntity entity = mapper.toEntity(domain);

            assertThat(entity.getId()).isEqualTo(id);
            assertThat(entity.getWebhookId()).isEqualTo(webhookId);
            assertThat(entity.getEventType()).isEqualTo("event.stored");
            assertThat(entity.getEventPayload()).isEqualTo("{\"key\": \"value\"}");
            assertThat(entity.getStatus()).isEqualTo(DeliveryStatus.RETRYING);
            assertThat(entity.getAttemptCount()).isEqualTo(2);
            assertThat(entity.getHttpStatus()).isEqualTo(500);
            assertThat(entity.getResponseBody()).isEqualTo("Error");
            assertThat(entity.getErrorMessage()).isEqualTo("Internal Server Error");
            assertThat(entity.getNextRetryAt()).isEqualTo(nextRetry);
            assertThat(entity.getCreatedAt()).isEqualTo(now);
            assertThat(entity.getDeliveredAt()).isNull();
        }

        @Test
        @DisplayName("Should map delivered delivery")
        void shouldMapDeliveredDelivery() {
            Instant now = Instant.now();

            WebhookDelivery domain = WebhookDelivery.builder()
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

            WebhookDeliveryEntity entity = mapper.toEntity(domain);

            assertThat(entity.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
            assertThat(entity.getHttpStatus()).isEqualTo(200);
            assertThat(entity.getDeliveredAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("toDomain() Tests")
    class ToDomainTests {

        @Test
        @DisplayName("Should map all fields from entity to domain")
        void shouldMapAllFieldsFromEntityToDomain() {
            UUID id = UUID.randomUUID();
            UUID webhookId = UUID.randomUUID();
            Instant now = Instant.now();
            Instant nextRetry = now.plusSeconds(300);

            WebhookDeliveryEntity entity = new WebhookDeliveryEntity();
            entity.setId(id);
            entity.setWebhookId(webhookId);
            entity.setEventType("event.stored");
            entity.setEventPayload("{\"key\": \"value\"}");
            entity.setStatus(DeliveryStatus.RETRYING);
            entity.setAttemptCount(2);
            entity.setHttpStatus(500);
            entity.setResponseBody("Error");
            entity.setErrorMessage("Internal Server Error");
            entity.setNextRetryAt(nextRetry);
            entity.setCreatedAt(now);
            entity.setDeliveredAt(null);

            WebhookDelivery domain = mapper.toDomain(entity);

            assertThat(domain.id()).isEqualTo(id);
            assertThat(domain.webhookId()).isEqualTo(webhookId);
            assertThat(domain.eventType()).isEqualTo("event.stored");
            assertThat(domain.eventPayload()).isEqualTo("{\"key\": \"value\"}");
            assertThat(domain.status()).isEqualTo(DeliveryStatus.RETRYING);
            assertThat(domain.attemptCount()).isEqualTo(2);
            assertThat(domain.httpStatus()).isEqualTo(500);
            assertThat(domain.responseBody()).isEqualTo("Error");
            assertThat(domain.errorMessage()).isEqualTo("Internal Server Error");
            assertThat(domain.nextRetryAt()).isEqualTo(nextRetry);
            assertThat(domain.createdAt()).isEqualTo(now);
            assertThat(domain.deliveredAt()).isNull();
        }

        @Test
        @DisplayName("Should map pending delivery")
        void shouldMapPendingDelivery() {
            Instant now = Instant.now();

            WebhookDeliveryEntity entity = new WebhookDeliveryEntity();
            entity.setId(UUID.randomUUID());
            entity.setWebhookId(UUID.randomUUID());
            entity.setEventType("event.stored");
            entity.setEventPayload("{}");
            entity.setStatus(DeliveryStatus.PENDING);
            entity.setAttemptCount(0);
            entity.setCreatedAt(now);

            WebhookDelivery domain = mapper.toDomain(entity);

            assertThat(domain.status()).isEqualTo(DeliveryStatus.PENDING);
            assertThat(domain.attemptCount()).isZero();
            assertThat(domain.httpStatus()).isNull();
        }
    }

    @Nested
    @DisplayName("Round-trip Tests")
    class RoundTripTests {

        @Test
        @DisplayName("Should preserve data in round-trip for delivered")
        void shouldPreserveDataForDelivered() {
            UUID id = UUID.randomUUID();
            UUID webhookId = UUID.randomUUID();
            Instant now = Instant.now();

            WebhookDelivery original = WebhookDelivery.builder()
                .id(id)
                .webhookId(webhookId)
                .eventType("event.stored")
                .eventPayload("{\"test\": true}")
                .status(DeliveryStatus.DELIVERED)
                .attemptCount(1)
                .httpStatus(200)
                .responseBody("OK")
                .createdAt(now)
                .deliveredAt(now)
                .build();

            WebhookDeliveryEntity entity = mapper.toEntity(original);
            WebhookDelivery result = mapper.toDomain(entity);

            assertThat(result.id()).isEqualTo(original.id());
            assertThat(result.webhookId()).isEqualTo(original.webhookId());
            assertThat(result.eventType()).isEqualTo(original.eventType());
            assertThat(result.eventPayload()).isEqualTo(original.eventPayload());
            assertThat(result.status()).isEqualTo(original.status());
            assertThat(result.attemptCount()).isEqualTo(original.attemptCount());
            assertThat(result.httpStatus()).isEqualTo(original.httpStatus());
            assertThat(result.responseBody()).isEqualTo(original.responseBody());
        }

        @Test
        @DisplayName("Should preserve data in round-trip for failed")
        void shouldPreserveDataForFailed() {
            UUID id = UUID.randomUUID();
            UUID webhookId = UUID.randomUUID();
            Instant now = Instant.now();

            WebhookDelivery original = WebhookDelivery.builder()
                .id(id)
                .webhookId(webhookId)
                .eventType("event.stored")
                .eventPayload("{}")
                .status(DeliveryStatus.FAILED)
                .attemptCount(5)
                .httpStatus(500)
                .errorMessage("Connection refused")
                .createdAt(now)
                .build();

            WebhookDeliveryEntity entity = mapper.toEntity(original);
            WebhookDelivery result = mapper.toDomain(entity);

            assertThat(result.status()).isEqualTo(DeliveryStatus.FAILED);
            assertThat(result.attemptCount()).isEqualTo(5);
            assertThat(result.errorMessage()).isEqualTo("Connection refused");
        }
    }
}
