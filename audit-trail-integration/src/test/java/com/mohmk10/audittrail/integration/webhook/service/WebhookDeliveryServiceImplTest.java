package com.mohmk10.audittrail.integration.webhook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mohmk10.audittrail.integration.webhook.adapter.out.persistence.JpaWebhookDeliveryRepository;
import com.mohmk10.audittrail.integration.webhook.adapter.out.persistence.JpaWebhookRepository;
import com.mohmk10.audittrail.integration.webhook.adapter.out.persistence.WebhookDeliveryEntity;
import com.mohmk10.audittrail.integration.webhook.adapter.out.persistence.WebhookDeliveryMapper;
import com.mohmk10.audittrail.integration.webhook.domain.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookDeliveryServiceImpl Tests")
class WebhookDeliveryServiceImplTest {

    private MockWebServer mockWebServer;

    @Mock
    private JpaWebhookDeliveryRepository deliveryRepository;

    @Mock
    private JpaWebhookRepository webhookRepository;

    @Mock
    private WebhookDeliveryMapper deliveryMapper;

    @Mock
    private WebhookRetryService retryService;

    private WebhookDeliveryServiceImpl deliveryService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        WebClient.Builder webClientBuilder = WebClient.builder();

        deliveryService = new WebhookDeliveryServiceImpl(
            webClientBuilder,
            deliveryRepository,
            webhookRepository,
            deliveryMapper,
            objectMapper,
            retryService
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Nested
    @DisplayName("deliver() Tests")
    class DeliverTests {

        @Test
        @DisplayName("Should deliver event successfully")
        void shouldDeliverEventSuccessfully() throws Exception {
            mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("OK"));

            Webhook webhook = createWebhook(mockWebServer.url("/webhook").toString());
            WebhookEvent event = WebhookEvent.test("tenant-1");

            WebhookDeliveryEntity savedEntity = createDeliveryEntity(DeliveryStatus.DELIVERED, 1);
            WebhookDelivery savedDelivery = createDelivery(DeliveryStatus.DELIVERED, 1);

            when(deliveryRepository.save(any())).thenReturn(savedEntity);
            when(deliveryMapper.toDomain(any(WebhookDeliveryEntity.class))).thenReturn(savedDelivery);

            WebhookDelivery result = deliveryService.deliver(webhook, event);

            assertThat(result).isNotNull();
            verify(deliveryRepository, atLeastOnce()).save(any());

            RecordedRequest request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
            assertThat(request).isNotNull();
            assertThat(request.getHeader("Content-Type")).contains("application/json");
        }

        @Test
        @DisplayName("Should include correct headers in request")
        void shouldIncludeCorrectHeaders() throws Exception {
            mockWebServer.enqueue(new MockResponse().setResponseCode(200));

            Map<String, String> customHeaders = Map.of("X-Custom-Header", "custom-value");
            Webhook webhook = createWebhookWithHeaders(
                mockWebServer.url("/webhook").toString(),
                customHeaders
            );
            WebhookEvent event = WebhookEvent.test("tenant-1");

            WebhookDeliveryEntity savedEntity = createDeliveryEntity(DeliveryStatus.DELIVERED, 1);
            WebhookDelivery savedDelivery = createDelivery(DeliveryStatus.DELIVERED, 1);

            when(deliveryRepository.save(any())).thenReturn(savedEntity);
            when(deliveryMapper.toDomain(any(WebhookDeliveryEntity.class))).thenReturn(savedDelivery);

            deliveryService.deliver(webhook, event);

            RecordedRequest request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
            assertThat(request.getHeader("X-Custom-Header")).isEqualTo("custom-value");
        }
    }

    @Nested
    @DisplayName("findByWebhookId() Tests")
    class FindByWebhookIdTests {

        @Test
        @DisplayName("Should return deliveries for webhook")
        void shouldReturnDeliveriesForWebhook() {
            UUID webhookId = UUID.randomUUID();
            WebhookDeliveryEntity entity = createDeliveryEntity(DeliveryStatus.DELIVERED, 1);
            WebhookDelivery delivery = createDelivery(DeliveryStatus.DELIVERED, 1);

            when(deliveryRepository.findByWebhookIdOrderByCreatedAtDesc(webhookId))
                .thenReturn(List.of(entity));
            when(deliveryMapper.toDomain(entity)).thenReturn(delivery);

            List<WebhookDelivery> result = deliveryService.findByWebhookId(webhookId);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("calculateSignature() Tests")
    class CalculateSignatureTests {

        @Test
        @DisplayName("Should calculate HMAC-SHA256 signature")
        void shouldCalculateHmacSha256Signature() {
            String payload = "{\"test\": \"data\"}";
            String secret = "whsec_test_secret";

            String signature = deliveryService.calculateSignature(payload, secret);

            assertThat(signature).startsWith("sha256=");
            assertThat(signature.length()).isGreaterThan(64);
        }

        @Test
        @DisplayName("Should produce consistent signatures")
        void shouldProduceConsistentSignatures() {
            String payload = "{\"test\": \"data\"}";
            String secret = "whsec_test_secret";

            String signature1 = deliveryService.calculateSignature(payload, secret);
            String signature2 = deliveryService.calculateSignature(payload, secret);

            assertThat(signature1).isEqualTo(signature2);
        }
    }

    private Webhook createWebhook(String url) {
        return createWebhookWithHeaders(url, Map.of());
    }

    private Webhook createWebhookWithHeaders(String url, Map<String, String> headers) {
        Instant now = Instant.now();
        return Webhook.builder()
            .id(UUID.randomUUID())
            .tenantId("tenant-1")
            .name("Test Webhook")
            .url(url)
            .secret("whsec_test")
            .events(Set.of("*"))
            .status(WebhookStatus.ACTIVE)
            .headers(headers)
            .maxRetries(5)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    private WebhookDelivery createDelivery(DeliveryStatus status, int attemptCount) {
        return WebhookDelivery.builder()
            .id(UUID.randomUUID())
            .webhookId(UUID.randomUUID())
            .eventType("test")
            .eventPayload("{}")
            .status(status)
            .attemptCount(attemptCount)
            .httpStatus(200)
            .createdAt(Instant.now())
            .deliveredAt(Instant.now())
            .build();
    }

    private WebhookDeliveryEntity createDeliveryEntity(DeliveryStatus status, int attemptCount) {
        WebhookDeliveryEntity entity = new WebhookDeliveryEntity();
        entity.setId(UUID.randomUUID());
        entity.setWebhookId(UUID.randomUUID());
        entity.setEventType("test");
        entity.setEventPayload("{}");
        entity.setStatus(status);
        entity.setAttemptCount(attemptCount);
        entity.setHttpStatus(200);
        entity.setCreatedAt(Instant.now());
        entity.setDeliveredAt(Instant.now());
        return entity;
    }
}
