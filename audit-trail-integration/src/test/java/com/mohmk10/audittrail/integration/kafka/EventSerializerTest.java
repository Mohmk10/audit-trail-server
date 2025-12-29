package com.mohmk10.audittrail.integration.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mohmk10.audittrail.core.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("EventSerializer Tests")
class EventSerializerTest {

    private EventSerializer serializer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        serializer = new EventSerializer(objectMapper);
    }

    @Nested
    @DisplayName("serialize() Tests")
    class SerializeTests {

        @Test
        @DisplayName("Should serialize Event to bytes")
        void shouldSerializeEventToBytes() {
            Event event = createEvent();

            byte[] result = serializer.serialize("audit-events", event);

            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should serialize to valid JSON")
        void shouldSerializeToValidJson() throws Exception {
            Event event = createEvent();

            byte[] result = serializer.serialize("audit-events", event);

            // Should be able to parse as JSON
            objectMapper.readTree(result);
        }

        @Test
        @DisplayName("Should return null for null event")
        void shouldReturnNullForNullEvent() {
            byte[] result = serializer.serialize("audit-events", null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should preserve event fields in serialized data")
        void shouldPreserveEventFields() throws Exception {
            UUID id = UUID.randomUUID();
            Event event = new Event(
                id,
                Instant.parse("2024-01-01T12:00:00Z"),
                new Actor("user@example.com", Actor.ActorType.USER, "User", "192.168.1.1", null, null),
                new Action(Action.ActionType.LOGIN, "login", "security"),
                new Resource("session", Resource.ResourceType.SYSTEM, "Session", null, null),
                null,
                null,
                null,
                null
            );

            byte[] result = serializer.serialize("audit-events", event);
            String json = new String(result);

            assertThat(json).contains(id.toString());
            assertThat(json).contains("user@example.com");
            assertThat(json).contains("LOGIN");
        }

        @Test
        @DisplayName("Should serialize timestamp in ISO format")
        void shouldSerializeTimestampInIsoFormat() throws Exception {
            Event event = new Event(
                UUID.randomUUID(),
                Instant.parse("2024-06-15T10:30:00Z"),
                new Actor("user", Actor.ActorType.USER, "User", null, null, null),
                new Action(Action.ActionType.READ, "action", "security"),
                new Resource("resource", Resource.ResourceType.DOCUMENT, "Resource", null, null),
                null,
                null,
                null,
                null
            );

            byte[] result = serializer.serialize("audit-events", event);
            String json = new String(result);

            assertThat(json).contains("2024-06-15T10:30:00");
        }
    }

    @Nested
    @DisplayName("configure() Tests")
    class ConfigureTests {

        @Test
        @DisplayName("Should not throw on configure")
        void shouldNotThrowOnConfigure() {
            assertThatNoException().isThrownBy(() ->
                serializer.configure(Map.of(), false)
            );
        }
    }

    @Nested
    @DisplayName("close() Tests")
    class CloseTests {

        @Test
        @DisplayName("Should not throw on close")
        void shouldNotThrowOnClose() {
            assertThatNoException().isThrownBy(() -> serializer.close());
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
