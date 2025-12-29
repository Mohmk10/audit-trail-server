package com.mohmk10.audittrail.integration.webhook.service;

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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookRetryServiceImpl Tests")
class WebhookRetryServiceImplTest {

    @Mock
    private WebhookDeliveryService deliveryService;

    private WebhookRetryServiceImpl retryService;

    @BeforeEach
    void setUp() {
        retryService = new WebhookRetryServiceImpl(deliveryService);
    }

    @Nested
    @DisplayName("processRetries() Tests")
    class ProcessRetriesTests {

        @Test
        @DisplayName("Should process deliveries ready for retry")
        void shouldProcessDeliveriesReadyForRetry() {
            UUID deliveryId = UUID.randomUUID();
            UUID webhookId = UUID.randomUUID();
            WebhookDelivery delivery = createDelivery(deliveryId, webhookId, DeliveryStatus.RETRYING);
            WebhookDelivery retriedDelivery = createDelivery(deliveryId, webhookId, DeliveryStatus.DELIVERED);

            when(deliveryService.findPendingRetries()).thenReturn(List.of(delivery));
            when(deliveryService.retry(deliveryId)).thenReturn(retriedDelivery);

            retryService.processRetries();

            verify(deliveryService).retry(deliveryId);
        }

        @Test
        @DisplayName("Should handle empty pending list")
        void shouldHandleEmptyPendingList() {
            when(deliveryService.findPendingRetries()).thenReturn(List.of());

            retryService.processRetries();

            verify(deliveryService, never()).retry(any());
        }

        @Test
        @DisplayName("Should continue processing on individual retry failure")
        void shouldContinueOnIndividualFailure() {
            UUID deliveryId1 = UUID.randomUUID();
            UUID deliveryId2 = UUID.randomUUID();
            UUID webhookId = UUID.randomUUID();
            WebhookDelivery delivery1 = createDelivery(deliveryId1, webhookId, DeliveryStatus.RETRYING);
            WebhookDelivery delivery2 = createDelivery(deliveryId2, webhookId, DeliveryStatus.RETRYING);

            when(deliveryService.findPendingRetries()).thenReturn(List.of(delivery1, delivery2));
            when(deliveryService.retry(deliveryId1)).thenThrow(new RuntimeException("Connection failed"));
            when(deliveryService.retry(deliveryId2)).thenReturn(delivery2);

            retryService.processRetries();

            verify(deliveryService).retry(deliveryId1);
            verify(deliveryService).retry(deliveryId2);
        }
    }

    @Nested
    @DisplayName("calculateNextRetry() Tests")
    class CalculateNextRetryTests {

        @Test
        @DisplayName("Should return instant ~60 seconds in future for first retry")
        void shouldReturn60SecondsForFirstRetry() {
            Instant before = Instant.now();
            Instant result = retryService.calculateNextRetry(0);
            Instant after = Instant.now();

            // Should be approximately 60 seconds in the future
            assertThat(result).isAfter(before.plusSeconds(59));
            assertThat(result).isBefore(after.plusSeconds(61));
        }

        @Test
        @DisplayName("Should return instant ~300 seconds in future for second retry")
        void shouldReturn300SecondsForSecondRetry() {
            Instant before = Instant.now();
            Instant result = retryService.calculateNextRetry(1);

            assertThat(result).isAfter(before.plusSeconds(299));
            assertThat(result).isBefore(before.plusSeconds(302));
        }

        @Test
        @DisplayName("Should return instant ~900 seconds in future for third retry")
        void shouldReturn900SecondsForThirdRetry() {
            Instant before = Instant.now();
            Instant result = retryService.calculateNextRetry(2);

            assertThat(result).isAfter(before.plusSeconds(899));
            assertThat(result).isBefore(before.plusSeconds(902));
        }

        @Test
        @DisplayName("Should return instant ~3600 seconds in future for fourth retry")
        void shouldReturn3600SecondsForFourthRetry() {
            Instant before = Instant.now();
            Instant result = retryService.calculateNextRetry(3);

            assertThat(result).isAfter(before.plusSeconds(3599));
            assertThat(result).isBefore(before.plusSeconds(3602));
        }

        @Test
        @DisplayName("Should return instant ~14400 seconds in future for fifth retry and beyond")
        void shouldReturn14400SecondsForFifthRetryAndBeyond() {
            Instant before = Instant.now();

            Instant result4 = retryService.calculateNextRetry(4);
            Instant result5 = retryService.calculateNextRetry(5);
            Instant result10 = retryService.calculateNextRetry(10);

            assertThat(result4).isAfter(before.plusSeconds(14399));
            assertThat(result5).isAfter(before.plusSeconds(14399));
            assertThat(result10).isAfter(before.plusSeconds(14399));
        }
    }

    private WebhookDelivery createDelivery(UUID id, UUID webhookId, DeliveryStatus status) {
        return WebhookDelivery.builder()
            .id(id)
            .webhookId(webhookId)
            .eventType("test")
            .eventPayload("{}")
            .status(status)
            .attemptCount(1)
            .nextRetryAt(Instant.now().minusSeconds(60))
            .createdAt(Instant.now())
            .build();
    }
}
