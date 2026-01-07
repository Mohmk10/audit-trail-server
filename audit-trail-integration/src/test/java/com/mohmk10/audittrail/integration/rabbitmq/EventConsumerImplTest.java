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
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventConsumerImplTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private EventConsumerImpl eventConsumer;

    @BeforeEach
    void setUp() {
        eventConsumer = new EventConsumerImpl(applicationEventPublisher);
    }

    @Test
    void consumeEvent_shouldPublishApplicationEvent() {
        Event event = createTestEvent();

        eventConsumer.consumeEvent(event);

        ArgumentCaptor<RabbitMQEventReceivedEvent> captor =
            ArgumentCaptor.forClass(RabbitMQEventReceivedEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        RabbitMQEventReceivedEvent publishedEvent = captor.getValue();
        assertThat(publishedEvent.event().id()).isEqualTo(event.id());
    }

    @Test
    void consumeEvent_shouldRethrowException() {
        Event event = createTestEvent();
        doThrow(new RuntimeException("Processing failed"))
            .when(applicationEventPublisher).publishEvent(any(RabbitMQEventReceivedEvent.class));

        assertThatThrownBy(() -> eventConsumer.consumeEvent(event))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Processing failed");
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
