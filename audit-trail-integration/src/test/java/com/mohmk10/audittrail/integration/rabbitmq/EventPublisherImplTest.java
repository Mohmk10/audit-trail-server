package com.mohmk10.audittrail.integration.rabbitmq;

import com.mohmk10.audittrail.core.domain.Action;
import com.mohmk10.audittrail.core.domain.Actor;
import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.domain.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventPublisherImplTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private EventPublisherImpl eventPublisher;

    private static final String EXCHANGE = "audit-events-exchange";
    private static final String ROUTING_KEY = "audit.events";

    @BeforeEach
    void setUp() {
        eventPublisher = new EventPublisherImpl(rabbitTemplate);
        ReflectionTestUtils.setField(eventPublisher, "eventsExchange", EXCHANGE);
        ReflectionTestUtils.setField(eventPublisher, "eventsRoutingKey", ROUTING_KEY);
    }

    @Test
    void publishEvent_shouldSendToRabbitMQ() {
        Event event = createTestEvent();

        eventPublisher.publishEvent(event);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(rabbitTemplate).convertAndSend(eq(EXCHANGE), eq(ROUTING_KEY), eventCaptor.capture());
        assertThat(eventCaptor.getValue().id()).isEqualTo(event.id());
    }

    @Test
    void publishEvent_shouldThrowExceptionOnError() {
        Event event = createTestEvent();
        doThrow(new RuntimeException("Connection failed"))
            .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Event.class));

        assertThatThrownBy(() -> eventPublisher.publishEvent(event))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to publish event to RabbitMQ");
    }

    @Test
    void publishEventBatch_shouldSendAllEvents() {
        List<Event> events = List.of(
            createTestEvent(),
            createTestEvent(),
            createTestEvent()
        );

        eventPublisher.publishEventBatch(events);

        verify(rabbitTemplate, times(3)).convertAndSend(eq(EXCHANGE), eq(ROUTING_KEY), any(Event.class));
    }

    private Event createTestEvent() {
        return new Event(
            UUID.randomUUID(),
            Instant.now(),
            new Actor("user-123", Actor.ActorType.USER, "Test User", "127.0.0.1", "TestAgent", Map.of()),
            new Action(Action.ActionType.LOGIN, "User login", "AUTH"),
            new Resource("acc-456", Resource.ResourceType.USER, "Test Account", null, null),
            null,
            null,
            null,
            null
        );
    }
}
