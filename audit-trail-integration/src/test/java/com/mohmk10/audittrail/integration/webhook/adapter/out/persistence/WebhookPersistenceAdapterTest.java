package com.mohmk10.audittrail.integration.webhook.adapter.out.persistence;

import com.mohmk10.audittrail.integration.webhook.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookPersistenceAdapter Tests")
class WebhookPersistenceAdapterTest {

    @Mock
    private JpaWebhookRepository webhookRepository;

    @Mock
    private JpaWebhookDeliveryRepository deliveryRepository;

    @Mock
    private WebhookMapper webhookMapper;

    @Mock
    private WebhookDeliveryMapper deliveryMapper;

    private WebhookPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new WebhookPersistenceAdapter(
            webhookRepository, deliveryRepository, webhookMapper, deliveryMapper
        );
    }

    @Nested
    @DisplayName("Webhook Operations")
    class WebhookOperationsTests {

        @Test
        @DisplayName("Should save webhook")
        void shouldSaveWebhook() {
            Webhook webhook = createWebhook();
            WebhookEntity entity = createWebhookEntity();

            when(webhookMapper.toEntity(webhook)).thenReturn(entity);
            when(webhookRepository.save(entity)).thenReturn(entity);
            when(webhookMapper.toDomain(entity)).thenReturn(webhook);

            Webhook result = adapter.saveWebhook(webhook);

            assertThat(result).isEqualTo(webhook);
            verify(webhookRepository).save(entity);
        }

        @Test
        @DisplayName("Should find webhook by id")
        void shouldFindWebhookById() {
            UUID id = UUID.randomUUID();
            WebhookEntity entity = createWebhookEntity();
            Webhook webhook = createWebhook();

            when(webhookRepository.findById(id)).thenReturn(Optional.of(entity));
            when(webhookMapper.toDomain(entity)).thenReturn(webhook);

            Optional<Webhook> result = adapter.findWebhookById(id);

            assertThat(result).isPresent().contains(webhook);
        }

        @Test
        @DisplayName("Should return empty when webhook not found")
        void shouldReturnEmptyWhenWebhookNotFound() {
            UUID id = UUID.randomUUID();
            when(webhookRepository.findById(id)).thenReturn(Optional.empty());

            Optional<Webhook> result = adapter.findWebhookById(id);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find webhooks by tenant id")
        void shouldFindWebhooksByTenantId() {
            WebhookEntity entity = createWebhookEntity();
            Webhook webhook = createWebhook();

            when(webhookRepository.findByTenantId("tenant-1"))
                .thenReturn(List.of(entity));
            when(webhookMapper.toDomain(entity)).thenReturn(webhook);

            List<Webhook> result = adapter.findWebhooksByTenantId("tenant-1");

            assertThat(result).hasSize(1).contains(webhook);
        }

        @Test
        @DisplayName("Should find webhooks by tenant id and status")
        void shouldFindWebhooksByTenantIdAndStatus() {
            WebhookEntity entity = createWebhookEntity();
            Webhook webhook = createWebhook();

            when(webhookRepository.findByTenantIdAndStatus("tenant-1", WebhookStatus.ACTIVE))
                .thenReturn(List.of(entity));
            when(webhookMapper.toDomain(entity)).thenReturn(webhook);

            List<Webhook> result = adapter.findWebhooksByTenantIdAndStatus(
                "tenant-1", WebhookStatus.ACTIVE
            );

            assertThat(result).hasSize(1).contains(webhook);
        }

        @Test
        @DisplayName("Should delete webhook")
        void shouldDeleteWebhook() {
            UUID id = UUID.randomUUID();

            adapter.deleteWebhook(id);

            verify(webhookRepository).deleteById(id);
        }

        @Test
        @DisplayName("Should check if webhook exists")
        void shouldCheckIfWebhookExists() {
            UUID id = UUID.randomUUID();
            when(webhookRepository.existsById(id)).thenReturn(true);

            boolean result = adapter.webhookExists(id);

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("Delivery Operations")
    class DeliveryOperationsTests {

        @Test
        @DisplayName("Should save delivery")
        void shouldSaveDelivery() {
            WebhookDelivery delivery = createDelivery();
            WebhookDeliveryEntity entity = createDeliveryEntity();

            when(deliveryMapper.toEntity(delivery)).thenReturn(entity);
            when(deliveryRepository.save(entity)).thenReturn(entity);
            when(deliveryMapper.toDomain(entity)).thenReturn(delivery);

            WebhookDelivery result = adapter.saveDelivery(delivery);

            assertThat(result).isEqualTo(delivery);
            verify(deliveryRepository).save(entity);
        }

        @Test
        @DisplayName("Should find delivery by id")
        void shouldFindDeliveryById() {
            UUID id = UUID.randomUUID();
            WebhookDeliveryEntity entity = createDeliveryEntity();
            WebhookDelivery delivery = createDelivery();

            when(deliveryRepository.findById(id)).thenReturn(Optional.of(entity));
            when(deliveryMapper.toDomain(entity)).thenReturn(delivery);

            Optional<WebhookDelivery> result = adapter.findDeliveryById(id);

            assertThat(result).isPresent().contains(delivery);
        }

        @Test
        @DisplayName("Should find deliveries by webhook id")
        void shouldFindDeliveriesByWebhookId() {
            UUID webhookId = UUID.randomUUID();
            WebhookDeliveryEntity entity = createDeliveryEntity();
            WebhookDelivery delivery = createDelivery();

            when(deliveryRepository.findByWebhookIdOrderByCreatedAtDesc(webhookId))
                .thenReturn(List.of(entity));
            when(deliveryMapper.toDomain(entity)).thenReturn(delivery);

            List<WebhookDelivery> result = adapter.findDeliveriesByWebhookId(webhookId);

            assertThat(result).hasSize(1).contains(delivery);
        }

        @Test
        @DisplayName("Should find deliveries ready for retry")
        void shouldFindDeliveriesReadyForRetry() {
            Instant before = Instant.now();
            WebhookDeliveryEntity entity = createDeliveryEntity();
            WebhookDelivery delivery = createDelivery();

            when(deliveryRepository.findByStatusAndNextRetryAtBefore(
                DeliveryStatus.RETRYING, before
            )).thenReturn(List.of(entity));
            when(deliveryMapper.toDomain(entity)).thenReturn(delivery);

            List<WebhookDelivery> result = adapter.findDeliveriesReadyForRetry(before);

            assertThat(result).hasSize(1).contains(delivery);
        }

        @Test
        @DisplayName("Should count deliveries by webhook id and status")
        void shouldCountDeliveriesByWebhookIdAndStatus() {
            UUID webhookId = UUID.randomUUID();
            when(deliveryRepository.countByWebhookIdAndStatus(webhookId, DeliveryStatus.DELIVERED))
                .thenReturn(5L);

            long result = adapter.countDeliveriesByWebhookIdAndStatus(
                webhookId, DeliveryStatus.DELIVERED
            );

            assertThat(result).isEqualTo(5L);
        }
    }

    private Webhook createWebhook() {
        Instant now = Instant.now();
        return Webhook.builder()
            .id(UUID.randomUUID())
            .tenantId("tenant-1")
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

    private WebhookEntity createWebhookEntity() {
        WebhookEntity entity = new WebhookEntity();
        entity.setId(UUID.randomUUID());
        entity.setTenantId("tenant-1");
        entity.setName("Test Webhook");
        entity.setUrl("https://example.com/webhook");
        entity.setSecret("whsec_test");
        entity.setEvents(new HashSet<>(Set.of("event.stored")));
        entity.setStatus(WebhookStatus.ACTIVE);
        entity.setHeaders(new HashMap<>());
        entity.setMaxRetries(5);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }

    private WebhookDelivery createDelivery() {
        return WebhookDelivery.builder()
            .id(UUID.randomUUID())
            .webhookId(UUID.randomUUID())
            .eventType("event.stored")
            .eventPayload("{}")
            .status(DeliveryStatus.PENDING)
            .attemptCount(0)
            .createdAt(Instant.now())
            .build();
    }

    private WebhookDeliveryEntity createDeliveryEntity() {
        WebhookDeliveryEntity entity = new WebhookDeliveryEntity();
        entity.setId(UUID.randomUUID());
        entity.setWebhookId(UUID.randomUUID());
        entity.setEventType("event.stored");
        entity.setEventPayload("{}");
        entity.setStatus(DeliveryStatus.PENDING);
        entity.setAttemptCount(0);
        entity.setCreatedAt(Instant.now());
        return entity;
    }
}
