package com.mohmk10.audittrail.integration.rabbitmq;

import com.mohmk10.audittrail.core.domain.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "audit-trail.rabbitmq", name = "enabled", havingValue = "true")
public class EventConsumerImpl implements EventConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventConsumerImpl.class);

    private final ApplicationEventPublisher eventPublisher;

    public EventConsumerImpl(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    @RabbitListener(queues = "${audit-trail.rabbitmq.queue.events:audit-events-queue}")
    public void consumeEvent(Event event) {
        log.debug("Received event from RabbitMQ: eventId={}, action={}",
            event.id(), event.action().type());

        try {
            eventPublisher.publishEvent(new RabbitMQEventReceivedEvent(event));
            log.debug("Event published to application context: eventId={}", event.id());
        } catch (Exception e) {
            log.error("Failed to process RabbitMQ event: eventId={}, error={}",
                event.id(), e.getMessage(), e);
            throw e;
        }
    }
}
