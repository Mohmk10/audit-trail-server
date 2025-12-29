package com.mohmk10.audittrail.core.domain;

import com.mohmk10.audittrail.core.fixtures.TestFixtures;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EventTest {

    @Test
    void shouldCreateEventWithAllComponents() {
        UUID id = UUID.randomUUID();
        Instant timestamp = Instant.now();
        Actor actor = TestFixtures.createTestActor();
        Action action = TestFixtures.createTestAction();
        Resource resource = TestFixtures.createTestResource();
        EventMetadata metadata = TestFixtures.createTestMetadata();

        Event event = new Event(
                id,
                timestamp,
                actor,
                action,
                resource,
                metadata,
                null,
                null,
                null
        );

        assertThat(event.id()).isEqualTo(id);
        assertThat(event.timestamp()).isEqualTo(timestamp);
        assertThat(event.actor()).isEqualTo(actor);
        assertThat(event.action()).isEqualTo(action);
        assertThat(event.resource()).isEqualTo(resource);
        assertThat(event.metadata()).isEqualTo(metadata);
    }

    @Test
    void shouldCreateEventWithProvidedId() {
        UUID expectedId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        Event event = TestFixtures.createTestEventWithId(expectedId);

        assertThat(event.id()).isEqualTo(expectedId);
    }

    @Test
    void shouldCreateEventWithProvidedTimestamp() {
        Instant expectedTimestamp = Instant.parse("2024-01-15T10:30:00Z");
        Event event = TestFixtures.createTestEventWithTimestamp(expectedTimestamp);

        assertThat(event.timestamp()).isEqualTo(expectedTimestamp);
    }

    @Test
    void shouldPreserveHashChainFields() {
        String previousHash = "abc123previoushash";
        String hash = "xyz789currenthash";
        String signature = "signature-data";

        Event event = new Event(
                UUID.randomUUID(),
                Instant.now(),
                TestFixtures.createTestActor(),
                TestFixtures.createTestAction(),
                TestFixtures.createTestResource(),
                TestFixtures.createTestMetadata(),
                previousHash,
                hash,
                signature
        );

        assertThat(event.previousHash()).isEqualTo(previousHash);
        assertThat(event.hash()).isEqualTo(hash);
        assertThat(event.signature()).isEqualTo(signature);
    }

    @Test
    void shouldHandleNullHashFields() {
        Event event = TestFixtures.createTestEvent();

        assertThat(event.previousHash()).isNull();
        assertThat(event.hash()).isNull();
        assertThat(event.signature()).isNull();
    }

    @Test
    void shouldHandleNullMetadata() {
        Event event = new Event(
                UUID.randomUUID(),
                Instant.now(),
                TestFixtures.createTestActor(),
                TestFixtures.createTestAction(),
                TestFixtures.createTestResource(),
                null,
                null,
                null,
                null
        );

        assertThat(event.metadata()).isNull();
    }

    @Test
    void shouldSupportRecordEquality() {
        UUID id = UUID.randomUUID();
        Instant timestamp = Instant.now();
        Actor actor = TestFixtures.createTestActor();
        Action action = TestFixtures.createTestAction();
        Resource resource = TestFixtures.createTestResource();
        EventMetadata metadata = TestFixtures.createTestMetadata();

        Event event1 = new Event(id, timestamp, actor, action, resource, metadata, "prev", "hash", "sig");
        Event event2 = new Event(id, timestamp, actor, action, resource, metadata, "prev", "hash", "sig");

        assertThat(event1).isEqualTo(event2);
        assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
    }

    @Test
    void shouldDifferentiateNonEqualEvents() {
        Event event1 = TestFixtures.createTestEvent();
        Event event2 = TestFixtures.createTestEvent();

        assertThat(event1).isNotEqualTo(event2);
    }

    @Test
    void shouldPreserveActorInformation() {
        Event event = TestFixtures.createTestEvent();

        assertThat(event.actor()).isNotNull();
        assertThat(event.actor().id()).isNotBlank();
        assertThat(event.actor().type()).isNotNull();
        assertThat(event.actor().name()).isNotBlank();
    }

    @Test
    void shouldPreserveActionInformation() {
        Event event = TestFixtures.createTestEvent();

        assertThat(event.action()).isNotNull();
        assertThat(event.action().type()).isNotNull();
    }

    @Test
    void shouldPreserveResourceInformation() {
        Event event = TestFixtures.createTestEvent();

        assertThat(event.resource()).isNotNull();
        assertThat(event.resource().id()).isNotBlank();
        assertThat(event.resource().type()).isNotNull();
        assertThat(event.resource().name()).isNotBlank();
    }

    @Test
    void shouldCreateMinimalEvent() {
        Event event = TestFixtures.createMinimalEvent();

        assertThat(event.id()).isNotNull();
        assertThat(event.timestamp()).isNotNull();
        assertThat(event.actor()).isNotNull();
        assertThat(event.action()).isNotNull();
        assertThat(event.resource()).isNotNull();
        assertThat(event.metadata()).isNotNull();
    }
}
