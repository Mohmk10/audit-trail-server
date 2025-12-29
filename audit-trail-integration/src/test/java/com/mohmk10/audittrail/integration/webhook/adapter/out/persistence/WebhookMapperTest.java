package com.mohmk10.audittrail.integration.webhook.adapter.out.persistence;

import com.mohmk10.audittrail.integration.webhook.domain.Webhook;
import com.mohmk10.audittrail.integration.webhook.domain.WebhookStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WebhookMapper Tests")
class WebhookMapperTest {

    private WebhookMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new WebhookMapper();
    }

    @Nested
    @DisplayName("toEntity() Tests")
    class ToEntityTests {

        @Test
        @DisplayName("Should map all fields from domain to entity")
        void shouldMapAllFieldsFromDomainToEntity() {
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();
            Set<String> events = Set.of("event.stored", "alert.created");
            Map<String, String> headers = Map.of("X-Custom", "value");

            Webhook domain = Webhook.builder()
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

            WebhookEntity entity = mapper.toEntity(domain);

            assertThat(entity.getId()).isEqualTo(id);
            assertThat(entity.getTenantId()).isEqualTo("tenant-1");
            assertThat(entity.getName()).isEqualTo("Test Webhook");
            assertThat(entity.getUrl()).isEqualTo("https://example.com/webhook");
            assertThat(entity.getSecret()).isEqualTo("whsec_test");
            assertThat(entity.getEvents()).containsExactlyInAnyOrderElementsOf(events);
            assertThat(entity.getStatus()).isEqualTo(WebhookStatus.ACTIVE);
            assertThat(entity.getHeaders()).containsAllEntriesOf(headers);
            assertThat(entity.getMaxRetries()).isEqualTo(5);
            assertThat(entity.getCreatedAt()).isEqualTo(now);
            assertThat(entity.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("Should handle null events")
        void shouldHandleNullEvents() {
            Webhook domain = Webhook.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-1")
                .name("Test")
                .url("https://example.com")
                .secret("secret")
                .events(null)
                .status(WebhookStatus.ACTIVE)
                .headers(Map.of())
                .maxRetries(5)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

            WebhookEntity entity = mapper.toEntity(domain);

            assertThat(entity.getEvents()).isEmpty();
        }

        @Test
        @DisplayName("Should handle null headers")
        void shouldHandleNullHeaders() {
            Webhook domain = Webhook.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-1")
                .name("Test")
                .url("https://example.com")
                .secret("secret")
                .events(Set.of())
                .status(WebhookStatus.ACTIVE)
                .headers(null)
                .maxRetries(5)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

            WebhookEntity entity = mapper.toEntity(domain);

            assertThat(entity.getHeaders()).isEmpty();
        }
    }

    @Nested
    @DisplayName("toDomain() Tests")
    class ToDomainTests {

        @Test
        @DisplayName("Should map all fields from entity to domain")
        void shouldMapAllFieldsFromEntityToDomain() {
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();
            Set<String> events = new HashSet<>(Set.of("event.stored", "alert.created"));
            Map<String, String> headers = new HashMap<>(Map.of("X-Custom", "value"));

            WebhookEntity entity = new WebhookEntity();
            entity.setId(id);
            entity.setTenantId("tenant-1");
            entity.setName("Test Webhook");
            entity.setUrl("https://example.com/webhook");
            entity.setSecret("whsec_test");
            entity.setEvents(events);
            entity.setStatus(WebhookStatus.ACTIVE);
            entity.setHeaders(headers);
            entity.setMaxRetries(5);
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);

            Webhook domain = mapper.toDomain(entity);

            assertThat(domain.id()).isEqualTo(id);
            assertThat(domain.tenantId()).isEqualTo("tenant-1");
            assertThat(domain.name()).isEqualTo("Test Webhook");
            assertThat(domain.url()).isEqualTo("https://example.com/webhook");
            assertThat(domain.secret()).isEqualTo("whsec_test");
            assertThat(domain.events()).containsExactlyInAnyOrderElementsOf(events);
            assertThat(domain.status()).isEqualTo(WebhookStatus.ACTIVE);
            assertThat(domain.headers()).containsAllEntriesOf(headers);
            assertThat(domain.maxRetries()).isEqualTo(5);
            assertThat(domain.createdAt()).isEqualTo(now);
            assertThat(domain.updatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("Should handle null events in entity")
        void shouldHandleNullEventsInEntity() {
            WebhookEntity entity = new WebhookEntity();
            entity.setId(UUID.randomUUID());
            entity.setTenantId("tenant-1");
            entity.setName("Test");
            entity.setUrl("https://example.com");
            entity.setSecret("secret");
            entity.setEvents(null);
            entity.setStatus(WebhookStatus.ACTIVE);
            entity.setHeaders(new HashMap<>());
            entity.setMaxRetries(5);
            entity.setCreatedAt(Instant.now());
            entity.setUpdatedAt(Instant.now());

            Webhook domain = mapper.toDomain(entity);

            assertThat(domain.events()).isEmpty();
        }

        @Test
        @DisplayName("Should handle null headers in entity")
        void shouldHandleNullHeadersInEntity() {
            WebhookEntity entity = new WebhookEntity();
            entity.setId(UUID.randomUUID());
            entity.setTenantId("tenant-1");
            entity.setName("Test");
            entity.setUrl("https://example.com");
            entity.setSecret("secret");
            entity.setEvents(new HashSet<>());
            entity.setStatus(WebhookStatus.ACTIVE);
            entity.setHeaders(null);
            entity.setMaxRetries(5);
            entity.setCreatedAt(Instant.now());
            entity.setUpdatedAt(Instant.now());

            Webhook domain = mapper.toDomain(entity);

            assertThat(domain.headers()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Round-trip Tests")
    class RoundTripTests {

        @Test
        @DisplayName("Should preserve data in round-trip")
        void shouldPreserveDataInRoundTrip() {
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();
            Set<String> events = Set.of("event.stored", "alert.created");
            Map<String, String> headers = Map.of("X-Custom", "value");

            Webhook original = Webhook.builder()
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

            WebhookEntity entity = mapper.toEntity(original);
            Webhook result = mapper.toDomain(entity);

            assertThat(result.id()).isEqualTo(original.id());
            assertThat(result.tenantId()).isEqualTo(original.tenantId());
            assertThat(result.name()).isEqualTo(original.name());
            assertThat(result.url()).isEqualTo(original.url());
            assertThat(result.secret()).isEqualTo(original.secret());
            assertThat(result.events()).containsExactlyInAnyOrderElementsOf(original.events());
            assertThat(result.status()).isEqualTo(original.status());
            assertThat(result.headers()).isEqualTo(original.headers());
            assertThat(result.maxRetries()).isEqualTo(original.maxRetries());
        }
    }
}
