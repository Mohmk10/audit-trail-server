package com.mohmk10.audittrail.integration.kafka;

import com.mohmk10.audittrail.core.domain.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class EventKafkaProducerImpl implements EventKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(EventKafkaProducerImpl.class);

    private final KafkaTemplate<String, Event> kafkaTemplate;
    private final String defaultTopic;

    public EventKafkaProducerImpl(
            KafkaTemplate<String, Event> kafkaTemplate,
            @Value("${audit-trail.kafka.topic:audit-events}") String defaultTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.defaultTopic = defaultTopic;
    }

    @Override
    public CompletableFuture<Void> send(Event event) {
        return send(defaultTopic, event);
    }

    @Override
    public CompletableFuture<Void> send(String topic, Event event) {
        String key = buildKey(event);
        return send(topic, key, event);
    }

    @Override
    public CompletableFuture<Void> send(String topic, String key, Event event) {
        log.debug("Sending event to Kafka: topic={}, key={}, eventId={}",
            topic, key, event.id());

        return kafkaTemplate.send(topic, key, event)
            .thenAccept(result -> {
                log.debug("Event sent successfully: topic={}, partition={}, offset={}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            })
            .exceptionally(ex -> {
                log.error("Failed to send event to Kafka: topic={}, key={}, error={}",
                    topic, key, ex.getMessage(), ex);
                throw new RuntimeException("Failed to send event to Kafka", ex);
            });
    }

    private String buildKey(Event event) {
        return event.actor().id() + ":" + event.resource().type();
    }
}
