package com.mohmk10.audittrail.integration.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mohmk10.audittrail.core.domain.Event;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class EventDeserializer implements Deserializer<Event> {

    private static final Logger log = LoggerFactory.getLogger(EventDeserializer.class);

    private final ObjectMapper objectMapper;

    public EventDeserializer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public EventDeserializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // No additional configuration needed
    }

    @Override
    public Event deserialize(String topic, byte[] data) {
        if (data == null || data.length == 0) {
            log.debug("Null or empty data received for deserialization");
            return null;
        }

        try {
            Event event = objectMapper.readValue(data, Event.class);
            log.trace("Deserialized event: topic={}, eventId={}", topic, event.id());
            return event;
        } catch (IOException e) {
            log.error("Failed to deserialize Event: topic={}, error={}",
                topic, e.getMessage());
            throw new SerializationException("Failed to deserialize Event", e);
        }
    }

    @Override
    public void close() {
        // No resources to close
    }
}
