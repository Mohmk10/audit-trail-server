package com.mohmk10.audittrail.integration.kafka;

import com.mohmk10.audittrail.core.domain.*;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventKafkaProducerImpl Tests")
class EventKafkaProducerImplTest {

    @Mock
    private KafkaTemplate<String, Event> kafkaTemplate;

    private EventKafkaProducerImpl producer;
    private static final String DEFAULT_TOPIC = "audit-events";

    @BeforeEach
    void setUp() {
        producer = new EventKafkaProducerImpl(kafkaTemplate, DEFAULT_TOPIC);
    }

    @Nested
    @DisplayName("send(event) Tests")
    class SendEventTests {

        @Test
        @DisplayName("Should send to default topic")
        void shouldSendToDefaultTopic() {
            Event event = createEvent();
            SendResult<String, Event> sendResult = createSendResult(DEFAULT_TOPIC);
            when(kafkaTemplate.send(eq(DEFAULT_TOPIC), any(), eq(event)))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

            CompletableFuture<Void> result = producer.send(event);

            assertThatNoException().isThrownBy(result::join);
            verify(kafkaTemplate).send(eq(DEFAULT_TOPIC), any(), eq(event));
        }

        @Test
        @DisplayName("Should use actor id as key")
        void shouldUseActorIdAsKey() {
            Event event = new Event(
                UUID.randomUUID(),
                Instant.now(),
                new Actor("user@example.com", Actor.ActorType.USER, "User", null, null, null),
                new Action(Action.ActionType.LOGIN, "login", "security"),
                new Resource("session", Resource.ResourceType.SYSTEM, "Session", null, null),
                null,
                null,
                null,
                null
            );

            SendResult<String, Event> sendResult = createSendResult(DEFAULT_TOPIC);
            when(kafkaTemplate.send(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

            producer.send(event);

            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate).send(any(), keyCaptor.capture(), any());
            assertThat(keyCaptor.getValue()).contains("user@example.com");
        }
    }

    @Nested
    @DisplayName("send(topic, event) Tests")
    class SendTopicEventTests {

        @Test
        @DisplayName("Should send to specified topic")
        void shouldSendToSpecifiedTopic() {
            String customTopic = "custom-events";
            Event event = createEvent();
            SendResult<String, Event> sendResult = createSendResult(customTopic);
            when(kafkaTemplate.send(eq(customTopic), any(), eq(event)))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

            CompletableFuture<Void> result = producer.send(customTopic, event);

            assertThatNoException().isThrownBy(result::join);
            verify(kafkaTemplate).send(eq(customTopic), any(), eq(event));
        }
    }

    @Nested
    @DisplayName("send(topic, key, event) Tests")
    class SendTopicKeyEventTests {

        @Test
        @DisplayName("Should send with specified key")
        void shouldSendWithSpecifiedKey() {
            String topic = "audit-events";
            String key = "custom-key";
            Event event = createEvent();
            SendResult<String, Event> sendResult = createSendResult(topic);
            when(kafkaTemplate.send(topic, key, event))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

            CompletableFuture<Void> result = producer.send(topic, key, event);

            assertThatNoException().isThrownBy(result::join);
            verify(kafkaTemplate).send(topic, key, event);
        }

        @Test
        @DisplayName("Should complete exceptionally on send failure")
        void shouldCompleteExceptionallyOnSendFailure() {
            Event event = createEvent();
            CompletableFuture<SendResult<String, Event>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new RuntimeException("Kafka unavailable"));

            when(kafkaTemplate.send(any(), any(), any())).thenReturn(failedFuture);

            CompletableFuture<Void> result = producer.send(DEFAULT_TOPIC, "key", event);

            assertThatThrownBy(result::join)
                .isInstanceOf(java.util.concurrent.CompletionException.class)
                .hasCauseInstanceOf(RuntimeException.class);
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

    private SendResult<String, Event> createSendResult(String topic) {
        RecordMetadata metadata = new RecordMetadata(
            new TopicPartition(topic, 0),
            0L,
            0,
            System.currentTimeMillis(),
            0,
            0
        );
        return new SendResult<>(null, metadata);
    }
}
