package com.mohmk10.audittrail.integration.rabbitmq;

import com.mohmk10.audittrail.core.domain.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnProperty(prefix = "audit-trail.rabbitmq", name = "enabled", havingValue = "true")
public class EventPublisherImpl implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisherImpl.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${audit-trail.rabbitmq.exchange.events:audit-events-exchange}")
    private String eventsExchange;

    @Value("${audit-trail.rabbitmq.routing-key.events:audit.events}")
    private String eventsRoutingKey;

    public EventPublisherImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishEvent(Event event) {
        log.debug("Publishing event to RabbitMQ: exchange={}, routingKey={}, eventId={}",
            eventsExchange, eventsRoutingKey, event.id());

        try {
            rabbitTemplate.convertAndSend(eventsExchange, eventsRoutingKey, event);
            log.debug("Event published successfully: eventId={}", event.id());
        } catch (Exception e) {
            log.error("Failed to publish event to RabbitMQ: eventId={}, error={}",
                event.id(), e.getMessage(), e);
            throw new RuntimeException("Failed to publish event to RabbitMQ", e);
        }
    }

    @Override
    public void publishEventBatch(List<Event> events) {
        log.debug("Publishing batch of {} events to RabbitMQ", events.size());
        events.forEach(this::publishEvent);
        log.debug("Batch of {} events published successfully", events.size());
    }
}
