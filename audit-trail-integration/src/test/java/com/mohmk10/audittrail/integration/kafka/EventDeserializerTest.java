package com.mohmk10.audittrail.integration.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mohmk10.audittrail.core.domain.*;
import org.apache.kafka.common.errors.SerializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("EventDeserializer Tests")
class EventDeserializerTest {

    private EventDeserializer deserializer;
    private EventSerializer serializer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        deserializer = new EventDeserializer(objectMapper);
        serializer = new EventSerializer(objectMapper);
    }

    @Nested
    @DisplayName("deserialize() Tests")
    class DeserializeTests {

        @Test
        @DisplayName("Should deserialize bytes to Event")
        void shouldDeserializeBytesToEvent() {
            Event original = createEvent();
            byte[] data = serializer.serialize("audit-events", original);

            Event result = deserializer.deserialize("audit-events", data);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(original.id());
            assertThat(result.actor().id()).isEqualTo(original.actor().id());
        }

        @Test
        @DisplayName("Should return null for null data")
        void shouldReturnNullForNullData() {
            Event result = deserializer.deserialize("audit-events", null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null for empty data")
        void shouldReturnNullForEmptyData() {
            Event result = deserializer.deserialize("audit-events", new byte[0]);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should throw SerializationException for invalid JSON")
        void shouldThrowForInvalidJson() {
            byte[] invalidData = "not-valid-json".getBytes();

            assertThatThrownBy(() -> deserializer.deserialize("audit-events", invalidData))
                .isInstanceOf(SerializationException.class);
        }

        @Test
        @DisplayName("Should preserve all fields after round-trip")
        void shouldPreserveAllFieldsAfterRoundTrip() {
            UUID id = UUID.randomUUID();
            Instant timestamp = Instant.parse("2024-01-15T12:30:00Z");

            Event original = new Event(
                id,
                timestamp,
                new Actor("user@example.com", Actor.ActorType.USER, "User", null, null, null),
                new Action(Action.ActionType.LOGIN, "login", "security"),
                new Resource("session", Resource.ResourceType.SYSTEM, "Session", null, null),
                null,
                null,
                null,
                null
            );

            byte[] data = serializer.serialize("audit-events", original);
            Event result = deserializer.deserialize("audit-events", data);

            assertThat(result.id()).isEqualTo(id);
            assertThat(result.actor().id()).isEqualTo("user@example.com");
            assertThat(result.actor().type()).isEqualTo(Actor.ActorType.USER);
            assertThat(result.action().type()).isEqualTo(Action.ActionType.LOGIN);
            assertThat(result.resource().id()).isEqualTo("session");
            assertThat(result.timestamp()).isEqualTo(timestamp);
        }
    }

    @Nested
    @DisplayName("configure() Tests")
    class ConfigureTests {

        @Test
        @DisplayName("Should not throw on configure")
        void shouldNotThrowOnConfigure() {
            assertThatNoException().isThrownBy(() ->
                deserializer.configure(Map.of(), false)
            );
        }
    }

    @Nested
    @DisplayName("close() Tests")
    class CloseTests {

        @Test
        @DisplayName("Should not throw on close")
        void shouldNotThrowOnClose() {
            assertThatNoException().isThrownBy(() -> deserializer.close());
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
