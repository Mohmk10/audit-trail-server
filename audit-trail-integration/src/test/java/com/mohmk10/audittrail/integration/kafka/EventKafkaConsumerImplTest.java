package com.mohmk10.audittrail.integration.kafka;

import com.mohmk10.audittrail.core.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventKafkaConsumerImpl Tests")
class EventKafkaConsumerImplTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private EventKafkaConsumerImpl consumer;

    @BeforeEach
    void setUp() {
        consumer = new EventKafkaConsumerImpl(eventPublisher);
    }

    @Nested
    @DisplayName("consume(event) Tests")
    class ConsumeEventTests {

        @Test
        @DisplayName("Should publish KafkaEventReceivedEvent")
        void shouldPublishKafkaEventReceivedEvent() {
            Event event = createEvent();

            consumer.consume(event);

            ArgumentCaptor<KafkaEventReceivedEvent> captor =
                ArgumentCaptor.forClass(KafkaEventReceivedEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            assertThat(captor.getValue().event()).isEqualTo(event);
        }

        @Test
        @DisplayName("Should propagate exception from event publisher")
        void shouldPropagateExceptionFromEventPublisher() {
            Event event = createEvent();
            doThrow(new RuntimeException("Event processing failed"))
                .when(eventPublisher).publishEvent(any(KafkaEventReceivedEvent.class));

            assertThatThrownBy(() -> consumer.consume(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Event processing failed");
        }
    }

    @Nested
    @DisplayName("consume(key, event) Tests")
    class ConsumeKeyEventTests {

        @Test
        @DisplayName("Should publish event with key context")
        void shouldPublishEventWithKeyContext() {
            Event event = createEvent();

            consumer.consume("tenant-1:auth-service", event);

            verify(eventPublisher).publishEvent(any(KafkaEventReceivedEvent.class));
        }

        @Test
        @DisplayName("Should handle null key gracefully")
        void shouldHandleNullKeyGracefully() {
            Event event = createEvent();

            assertThatNoException().isThrownBy(() ->
                consumer.consume(null, event)
            );
            verify(eventPublisher).publishEvent(any(KafkaEventReceivedEvent.class));
        }
    }

    private Event createEvent() {
        return new Event(
            UUID.randomUUID(),
            Instant.now(),
            new Actor("test-user", Actor.ActorType.USER, "Test User", "127.0.0.1", null, Map.of()),
            new Action(Action.ActionType.READ, "test-action", "security"),
            new Resource("resource-1", Resource.ResourceType.DOCUMENT, "Test Resource", null, null),
            null,
            null,
            null,
            null
        );
    }
}
