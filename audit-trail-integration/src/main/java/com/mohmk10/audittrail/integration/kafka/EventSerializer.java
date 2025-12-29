package com.mohmk10.audittrail.integration.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mohmk10.audittrail.core.domain.Event;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class EventSerializer implements Serializer<Event> {

    private static final Logger log = LoggerFactory.getLogger(EventSerializer.class);

    private final ObjectMapper objectMapper;

    public EventSerializer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public EventSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // No additional configuration needed
    }

    @Override
    public byte[] serialize(String topic, Event event) {
        if (event == null) {
            log.debug("Null event received for serialization");
            return null;
        }

        try {
            byte[] data = objectMapper.writeValueAsBytes(event);
            log.trace("Serialized event: topic={}, eventId={}, size={}",
                topic, event.id(), data.length);
            return data;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize Event: eventId={}, error={}",
                event.id(), e.getMessage());
            throw new SerializationException("Failed to serialize Event", e);
        }
    }

    @Override
    public void close() {
        // No resources to close
    }
}
