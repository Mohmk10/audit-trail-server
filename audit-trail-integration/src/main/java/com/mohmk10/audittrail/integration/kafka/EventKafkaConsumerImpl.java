package com.mohmk10.audittrail.integration.kafka;

import com.mohmk10.audittrail.core.domain.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class EventKafkaConsumerImpl implements EventKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventKafkaConsumerImpl.class);

    private final ApplicationEventPublisher eventPublisher;

    public EventKafkaConsumerImpl(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    @KafkaListener(
        topics = "${audit-trail.kafka.topic:audit-events}",
        groupId = "${audit-trail.kafka.consumer.group-id:audit-trail-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(@Payload Event event) {
        log.debug("Received event from Kafka: eventId={}, action={}",
            event.id(), event.action().type());
        processEvent(event);
    }

    @Override
    public void consume(
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Payload Event event) {
        log.debug("Received event from Kafka: key={}, eventId={}, action={}",
            key, event.id(), event.action().type());
        processEvent(event);
    }

    private void processEvent(Event event) {
        try {
            eventPublisher.publishEvent(new KafkaEventReceivedEvent(event));
            log.debug("Event published to application context: eventId={}", event.id());
        } catch (Exception e) {
            log.error("Failed to process Kafka event: eventId={}, error={}",
                event.id(), e.getMessage(), e);
            throw e;
        }
    }
}
