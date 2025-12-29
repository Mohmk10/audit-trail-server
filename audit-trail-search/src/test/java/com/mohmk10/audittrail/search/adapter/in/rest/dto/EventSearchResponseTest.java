package com.mohmk10.audittrail.search.adapter.in.rest.dto;

import com.mohmk10.audittrail.core.domain.Action;
import com.mohmk10.audittrail.core.domain.Actor;
import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.domain.EventMetadata;
import com.mohmk10.audittrail.core.domain.Resource;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EventSearchResponseTest {

    private Event createTestEvent() {
        return new Event(
                UUID.randomUUID(),
                Instant.now(),
                new Actor("actor-123", Actor.ActorType.USER, "John Doe", "192.168.1.1", "Mozilla/5.0", null),
                new Action(Action.ActionType.CREATE, "Created document", "DOCS"),
                new Resource("res-123", Resource.ResourceType.DOCUMENT, "Annual Report", null, null),
                new EventMetadata("web-app", "tenant-001", "corr-123", "session-abc", Map.of("env", "prod"), null),
                null,
                "hash-123",
                null
        );
    }

    @Test
    void shouldMapFromEvent() {
        Event event = createTestEvent();

        EventSearchResponse response = EventSearchResponse.from(event);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(event.id());
        assertThat(response.timestamp()).isEqualTo(event.timestamp());
        assertThat(response.hash()).isEqualTo("hash-123");
    }

    @Test
    void shouldMapActorDetails() {
        Event event = createTestEvent();

        EventSearchResponse response = EventSearchResponse.from(event);

        assertThat(response.actor()).isNotNull();
        assertThat(response.actor().id()).isEqualTo("actor-123");
        assertThat(response.actor().type()).isEqualTo("USER");
        assertThat(response.actor().name()).isEqualTo("John Doe");
        assertThat(response.actor().ip()).isEqualTo("192.168.1.1");
    }

    @Test
    void shouldMapActionDetails() {
        Event event = createTestEvent();

        EventSearchResponse response = EventSearchResponse.from(event);

        assertThat(response.action()).isNotNull();
        assertThat(response.action().type()).isEqualTo("CREATE");
        assertThat(response.action().description()).isEqualTo("Created document");
        assertThat(response.action().category()).isEqualTo("DOCS");
    }

    @Test
    void shouldMapResourceDetails() {
        Event event = createTestEvent();

        EventSearchResponse response = EventSearchResponse.from(event);

        assertThat(response.resource()).isNotNull();
        assertThat(response.resource().id()).isEqualTo("res-123");
        assertThat(response.resource().type()).isEqualTo("DOCUMENT");
        assertThat(response.resource().name()).isEqualTo("Annual Report");
    }

    @Test
    void shouldMapMetadataDetails() {
        Event event = createTestEvent();

        EventSearchResponse response = EventSearchResponse.from(event);

        assertThat(response.metadata()).isNotNull();
        assertThat(response.metadata().source()).isEqualTo("web-app");
        assertThat(response.metadata().tenantId()).isEqualTo("tenant-001");
        assertThat(response.metadata().correlationId()).isEqualTo("corr-123");
        assertThat(response.metadata().sessionId()).isEqualTo("session-abc");
        assertThat(response.metadata().tags()).containsEntry("env", "prod");
    }

    @Test
    void shouldHandleNullActor() {
        Event event = new Event(
                UUID.randomUUID(),
                Instant.now(),
                null,
                new Action(Action.ActionType.CREATE, null, null),
                new Resource("res-123", Resource.ResourceType.DOCUMENT, "Report", null, null),
                new EventMetadata("web-app", "tenant-001", null, null, null, null),
                null, null, null
        );

        EventSearchResponse response = EventSearchResponse.from(event);

        assertThat(response.actor()).isNull();
    }

    @Test
    void shouldHandleNullAction() {
        Event event = new Event(
                UUID.randomUUID(),
                Instant.now(),
                new Actor("actor-123", Actor.ActorType.USER, "John", null, null, null),
                null,
                new Resource("res-123", Resource.ResourceType.DOCUMENT, "Report", null, null),
                new EventMetadata("web-app", "tenant-001", null, null, null, null),
                null, null, null
        );

        EventSearchResponse response = EventSearchResponse.from(event);

        assertThat(response.action()).isNull();
    }

    @Test
    void shouldHandleNullResource() {
        Event event = new Event(
                UUID.randomUUID(),
                Instant.now(),
                new Actor("actor-123", Actor.ActorType.USER, "John", null, null, null),
                new Action(Action.ActionType.CREATE, null, null),
                null,
                new EventMetadata("web-app", "tenant-001", null, null, null, null),
                null, null, null
        );

        EventSearchResponse response = EventSearchResponse.from(event);

        assertThat(response.resource()).isNull();
    }

    @Test
    void shouldHandleNullMetadata() {
        Event event = new Event(
                UUID.randomUUID(),
                Instant.now(),
                new Actor("actor-123", Actor.ActorType.USER, "John", null, null, null),
                new Action(Action.ActionType.CREATE, null, null),
                new Resource("res-123", Resource.ResourceType.DOCUMENT, "Report", null, null),
                null,
                null, null, null
        );

        EventSearchResponse response = EventSearchResponse.from(event);

        assertThat(response.metadata()).isNull();
    }

    @Test
    void shouldHandleNullTypes() {
        Event event = new Event(
                UUID.randomUUID(),
                Instant.now(),
                new Actor("actor-123", null, "John", null, null, null),
                new Action(null, null, null),
                new Resource("res-123", null, "Report", null, null),
                new EventMetadata("web-app", "tenant-001", null, null, null, null),
                null, null, null
        );

        EventSearchResponse response = EventSearchResponse.from(event);

        assertThat(response.actor().type()).isNull();
        assertThat(response.action().type()).isNull();
        assertThat(response.resource().type()).isNull();
    }

    @Test
    void shouldHandleNullHash() {
        Event event = new Event(
                UUID.randomUUID(),
                Instant.now(),
                new Actor("actor-123", Actor.ActorType.USER, "John", null, null, null),
                new Action(Action.ActionType.CREATE, null, null),
                new Resource("res-123", Resource.ResourceType.DOCUMENT, "Report", null, null),
                new EventMetadata("web-app", "tenant-001", null, null, null, null),
                null, null, null
        );

        EventSearchResponse response = EventSearchResponse.from(event);

        assertThat(response.hash()).isNull();
    }

    @Test
    void shouldPreserveTimestampPrecision() {
        Instant preciseTimestamp = Instant.parse("2024-06-15T10:30:45.123456789Z");
        Event event = new Event(
                UUID.randomUUID(),
                preciseTimestamp,
                new Actor("actor-123", Actor.ActorType.USER, "John", null, null, null),
                new Action(Action.ActionType.CREATE, null, null),
                new Resource("res-123", Resource.ResourceType.DOCUMENT, "Report", null, null),
                new EventMetadata("web-app", "tenant-001", null, null, null, null),
                null, "hash", null
        );

        EventSearchResponse response = EventSearchResponse.from(event);

        assertThat(response.timestamp()).isEqualTo(preciseTimestamp);
    }

    @Test
    void shouldPreserveUUID() {
        UUID eventId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        Event event = new Event(
                eventId,
                Instant.now(),
                new Actor("actor-123", Actor.ActorType.USER, "John", null, null, null),
                new Action(Action.ActionType.CREATE, null, null),
                new Resource("res-123", Resource.ResourceType.DOCUMENT, "Report", null, null),
                new EventMetadata("web-app", "tenant-001", null, null, null, null),
                null, "hash", null
        );

        EventSearchResponse response = EventSearchResponse.from(event);

        assertThat(response.id()).isEqualTo(eventId);
    }
}
