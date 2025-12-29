package com.mohmk10.audittrail.integration.webhook.service;

import com.mohmk10.audittrail.integration.webhook.adapter.out.persistence.JpaWebhookRepository;
import com.mohmk10.audittrail.integration.webhook.adapter.out.persistence.WebhookEntity;
import com.mohmk10.audittrail.integration.webhook.adapter.out.persistence.WebhookMapper;
import com.mohmk10.audittrail.integration.webhook.domain.Webhook;
import com.mohmk10.audittrail.integration.webhook.domain.WebhookNotFoundException;
import com.mohmk10.audittrail.integration.webhook.domain.WebhookStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookServiceImpl Tests")
class WebhookServiceImplTest {

    @Mock
    private JpaWebhookRepository repository;

    @Mock
    private WebhookMapper mapper;

    private WebhookServiceImpl webhookService;

    @BeforeEach
    void setUp() {
        webhookService = new WebhookServiceImpl(repository, mapper);
    }

    @Nested
    @DisplayName("create() Tests")
    class CreateTests {

        @Test
        @DisplayName("Should create webhook with generated id and secret")
        void shouldCreateWebhookWithGeneratedIdAndSecret() {
            Webhook input = Webhook.builder()
                .tenantId("tenant-1")
                .name("Test Webhook")
                .url("https://example.com/webhook")
                .events(Set.of("event.stored"))
                .headers(Map.of("X-Custom", "value"))
                .maxRetries(5)
                .build();

            WebhookEntity savedEntity = createEntity(UUID.randomUUID());
            Webhook savedWebhook = createWebhook(savedEntity.getId());

            when(repository.save(any())).thenReturn(savedEntity);
            when(mapper.toEntity(any(Webhook.class))).thenReturn(savedEntity);
            when(mapper.toDomain(savedEntity)).thenReturn(savedWebhook);

            Webhook result = webhookService.create(input);

            assertThat(result).isNotNull();
            verify(repository).save(any());
        }
    }

    @Nested
    @DisplayName("findById() Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should return webhook when found")
        void shouldReturnWebhookWhenFound() {
            UUID id = UUID.randomUUID();
            WebhookEntity entity = createEntity(id);
            Webhook webhook = createWebhook(id);

            when(repository.findById(id)).thenReturn(Optional.of(entity));
            when(mapper.toDomain(entity)).thenReturn(webhook);

            Optional<Webhook> result = webhookService.findById(id);

            assertThat(result).isPresent().contains(webhook);
        }

        @Test
        @DisplayName("Should return empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(repository.findById(id)).thenReturn(Optional.empty());

            Optional<Webhook> result = webhookService.findById(id);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByTenantId() Tests")
    class FindByTenantIdTests {

        @Test
        @DisplayName("Should return webhooks for tenant")
        void shouldReturnWebhooksForTenant() {
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            WebhookEntity entity1 = createEntity(id1);
            WebhookEntity entity2 = createEntity(id2);
            Webhook webhook1 = createWebhook(id1);
            Webhook webhook2 = createWebhook(id2);

            when(repository.findByTenantId("tenant-1"))
                .thenReturn(List.of(entity1, entity2));
            when(mapper.toDomain(entity1)).thenReturn(webhook1);
            when(mapper.toDomain(entity2)).thenReturn(webhook2);

            List<Webhook> result = webhookService.findByTenantId("tenant-1");

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list when no webhooks found")
        void shouldReturnEmptyListWhenNoWebhooksFound() {
            when(repository.findByTenantId("tenant-1")).thenReturn(List.of());

            List<Webhook> result = webhookService.findByTenantId("tenant-1");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findActiveByTenantId() Tests")
    class FindActiveByTenantIdTests {

        @Test
        @DisplayName("Should return active webhooks for tenant")
        void shouldReturnActiveWebhooksForTenant() {
            UUID id = UUID.randomUUID();
            WebhookEntity entity = createEntity(id);
            Webhook webhook = createWebhook(id);

            when(repository.findByTenantIdAndStatus("tenant-1", WebhookStatus.ACTIVE))
                .thenReturn(List.of(entity));
            when(mapper.toDomain(entity)).thenReturn(webhook);

            List<Webhook> result = webhookService.findActiveByTenantId("tenant-1");

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("update() Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update webhook when found")
        void shouldUpdateWebhookWhenFound() {
            UUID id = UUID.randomUUID();
            WebhookEntity existingEntity = createEntity(id);
            Webhook updateData = Webhook.builder()
                .name("Updated Name")
                .url("https://new.url")
                .events(Set.of("alert.created"))
                .headers(Map.of("New-Header", "value"))
                .maxRetries(10)
                .build();

            Webhook updatedWebhook = createWebhookWithName(id, "Updated Name");

            when(repository.findById(id)).thenReturn(Optional.of(existingEntity));
            when(repository.save(any())).thenReturn(existingEntity);
            when(mapper.toDomain(any(WebhookEntity.class))).thenReturn(updatedWebhook);

            Webhook result = webhookService.update(id, updateData);

            assertThat(result.name()).isEqualTo("Updated Name");
            verify(repository).save(any());
        }

        @Test
        @DisplayName("Should throw exception when webhook not found")
        void shouldThrowExceptionWhenNotFound() {
            UUID id = UUID.randomUUID();
            Webhook updateData = Webhook.builder().name("Name").build();

            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> webhookService.update(id, updateData))
                .isInstanceOf(WebhookNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete() Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete webhook when found")
        void shouldDeleteWebhookWhenFound() {
            UUID id = UUID.randomUUID();
            when(repository.existsById(id)).thenReturn(true);

            webhookService.delete(id);

            verify(repository).deleteById(id);
        }

        @Test
        @DisplayName("Should throw exception when webhook not found")
        void shouldThrowExceptionWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(repository.existsById(id)).thenReturn(false);

            assertThatThrownBy(() -> webhookService.delete(id))
                .isInstanceOf(WebhookNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Status Management Tests")
    class StatusManagementTests {

        @Test
        @DisplayName("Should activate webhook")
        void shouldActivateWebhook() {
            UUID id = UUID.randomUUID();
            WebhookEntity existingEntity = createEntityWithStatus(id, WebhookStatus.INACTIVE);
            Webhook activatedWebhook = createWebhookWithStatus(id, WebhookStatus.ACTIVE);

            when(repository.findById(id)).thenReturn(Optional.of(existingEntity));
            when(repository.save(any())).thenReturn(existingEntity);
            when(mapper.toDomain(any(WebhookEntity.class))).thenReturn(activatedWebhook);

            Webhook result = webhookService.activate(id);

            assertThat(result.status()).isEqualTo(WebhookStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should deactivate webhook")
        void shouldDeactivateWebhook() {
            UUID id = UUID.randomUUID();
            WebhookEntity existingEntity = createEntityWithStatus(id, WebhookStatus.ACTIVE);
            Webhook deactivatedWebhook = createWebhookWithStatus(id, WebhookStatus.INACTIVE);

            when(repository.findById(id)).thenReturn(Optional.of(existingEntity));
            when(repository.save(any())).thenReturn(existingEntity);
            when(mapper.toDomain(any(WebhookEntity.class))).thenReturn(deactivatedWebhook);

            Webhook result = webhookService.deactivate(id);

            assertThat(result.status()).isEqualTo(WebhookStatus.INACTIVE);
        }

        @Test
        @DisplayName("Should suspend webhook")
        void shouldSuspendWebhook() {
            UUID id = UUID.randomUUID();
            WebhookEntity existingEntity = createEntityWithStatus(id, WebhookStatus.ACTIVE);
            Webhook suspendedWebhook = createWebhookWithStatus(id, WebhookStatus.SUSPENDED);

            when(repository.findById(id)).thenReturn(Optional.of(existingEntity));
            when(repository.save(any())).thenReturn(existingEntity);
            when(mapper.toDomain(any(WebhookEntity.class))).thenReturn(suspendedWebhook);

            Webhook result = webhookService.suspend(id);

            assertThat(result.status()).isEqualTo(WebhookStatus.SUSPENDED);
        }
    }

    @Nested
    @DisplayName("generateSecret() Tests")
    class GenerateSecretTests {

        @Test
        @DisplayName("Should generate secret with correct format")
        void shouldGenerateSecretWithCorrectFormat() {
            String secret = webhookService.generateSecret();

            assertThat(secret).startsWith("whsec_");
            assertThat(secret.length()).isGreaterThan(20);
        }

        @Test
        @DisplayName("Should generate unique secrets")
        void shouldGenerateUniqueSecrets() {
            String secret1 = webhookService.generateSecret();
            String secret2 = webhookService.generateSecret();

            assertThat(secret1).isNotEqualTo(secret2);
        }
    }

    @Nested
    @DisplayName("verifySignature() Tests")
    class VerifySignatureTests {

        @Test
        @DisplayName("Should verify valid signature")
        void shouldVerifyValidSignature() {
            String payload = "{\"test\":\"data\"}";
            String secret = "whsec_test_secret";
            // Create a valid signature first
            String validSignature = createSignature(payload, secret);

            boolean result = webhookService.verifySignature(payload, validSignature, secret);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should reject invalid signature")
        void shouldRejectInvalidSignature() {
            boolean result = webhookService.verifySignature(
                "{\"test\":\"data\"}",
                "sha256=invalid",
                "secret"
            );

            assertThat(result).isFalse();
        }
    }

    private String createSignature(String payload, String secret) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(
                secret.getBytes(), "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(payload.getBytes());
            StringBuilder hexString = new StringBuilder("sha256=");
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Webhook createWebhook(UUID id) {
        return createWebhookWithStatus(id, WebhookStatus.ACTIVE);
    }

    private Webhook createWebhookWithName(UUID id, String name) {
        Instant now = Instant.now();
        return Webhook.builder()
            .id(id)
            .tenantId("tenant-1")
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

    private Webhook createWebhookWithStatus(UUID id, WebhookStatus status) {
        Instant now = Instant.now();
        return Webhook.builder()
            .id(id)
            .tenantId("tenant-1")
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

    private WebhookEntity createEntity(UUID id) {
        return createEntityWithStatus(id, WebhookStatus.ACTIVE);
    }

    private WebhookEntity createEntityWithStatus(UUID id, WebhookStatus status) {
        WebhookEntity entity = new WebhookEntity();
        entity.setId(id);
        entity.setTenantId("tenant-1");
        entity.setName("Test Webhook");
        entity.setUrl("https://example.com/webhook");
        entity.setSecret("whsec_test");
        entity.setEvents(Set.of("event.stored"));
        entity.setStatus(status);
        entity.setHeaders(Map.of());
        entity.setMaxRetries(5);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }
}
