package com.mohmk10.audittrail.search.adapter.out.elasticsearch.mapper;

import com.mohmk10.audittrail.core.domain.Action;
import com.mohmk10.audittrail.core.domain.Actor;
import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.domain.EventMetadata;
import com.mohmk10.audittrail.core.domain.Resource;
import com.mohmk10.audittrail.search.adapter.out.elasticsearch.document.EventDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EventDocumentMapperTest {

    private EventDocumentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new EventDocumentMapper();
    }

    private Event createTestEvent() {
        return new Event(
                UUID.randomUUID(),
                Instant.now(),
                new Actor("actor-123", Actor.ActorType.USER, "John Doe", "192.168.1.1", "Mozilla/5.0", null),
                new Action(Action.ActionType.CREATE, "Created document", "DOCS"),
                new Resource("res-123", Resource.ResourceType.DOCUMENT, "Annual Report", null, null),
                new EventMetadata("web-app", "tenant-001", "corr-123", "session-abc",
                        Map.of("env", "prod", "region", "us-east-1"), null),
                null,
                "hash-123",
                null
        );
    }

    @Test
    void shouldMapEventToDocument() {
        Event event = createTestEvent();

        EventDocument document = mapper.toDocument(event);

        assertThat(document).isNotNull();
        assertThat(document.getId()).isEqualTo(event.id().toString());
        assertThat(document.getTimestamp()).isEqualTo(event.timestamp());
        assertThat(document.getHash()).isEqualTo("hash-123");
    }

    @Test
    void shouldMapActorFields() {
        Event event = createTestEvent();

        EventDocument document = mapper.toDocument(event);

        assertThat(document.getActorId()).isEqualTo("actor-123");
        assertThat(document.getActorType()).isEqualTo("USER");
        assertThat(document.getActorName()).isEqualTo("John Doe");
        assertThat(document.getActorIp()).isEqualTo("192.168.1.1");
    }

    @Test
    void shouldMapActionFields() {
        Event event = createTestEvent();

        EventDocument document = mapper.toDocument(event);

        assertThat(document.getActionType()).isEqualTo("CREATE");
        assertThat(document.getActionDescription()).isEqualTo("Created document");
        assertThat(document.getActionCategory()).isEqualTo("DOCS");
    }

    @Test
    void shouldMapResourceFields() {
        Event event = createTestEvent();

        EventDocument document = mapper.toDocument(event);

        assertThat(document.getResourceId()).isEqualTo("res-123");
        assertThat(document.getResourceType()).isEqualTo("DOCUMENT");
        assertThat(document.getResourceName()).isEqualTo("Annual Report");
    }

    @Test
    void shouldMapMetadataFields() {
        Event event = createTestEvent();

        EventDocument document = mapper.toDocument(event);

        assertThat(document.getSource()).isEqualTo("web-app");
        assertThat(document.getTenantId()).isEqualTo("tenant-001");
        assertThat(document.getCorrelationId()).isEqualTo("corr-123");
        assertThat(document.getSessionId()).isEqualTo("session-abc");
    }

    @Test
    void shouldConvertTagsMapToList() {
        Event event = createTestEvent();

        EventDocument document = mapper.toDocument(event);

        assertThat(document.getTags()).isNotNull();
        assertThat(document.getTags()).contains("env:prod");
        assertThat(document.getTags()).contains("region:us-east-1");
    }

    @Test
    void shouldReturnNullForNullEvent() {
        EventDocument document = mapper.toDocument(null);

        assertThat(document).isNull();
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

        EventDocument document = mapper.toDocument(event);

        assertThat(document).isNotNull();
        assertThat(document.getActorId()).isNull();
        assertThat(document.getActorType()).isNull();
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

        EventDocument document = mapper.toDocument(event);

        assertThat(document).isNotNull();
        assertThat(document.getActionType()).isNull();
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

        EventDocument document = mapper.toDocument(event);

        assertThat(document).isNotNull();
        assertThat(document.getResourceId()).isNull();
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

        EventDocument document = mapper.toDocument(event);

        assertThat(document).isNotNull();
        assertThat(document.getTenantId()).isNull();
        assertThat(document.getSource()).isNull();
    }

    @Test
    void shouldMapDocumentToDomain() {
        EventDocument document = new EventDocument();
        String id = UUID.randomUUID().toString();
        Instant timestamp = Instant.now();

        document.setId(id);
        document.setTimestamp(timestamp);
        document.setActorId("actor-123");
        document.setActorType("USER");
        document.setActorName("John Doe");
        document.setActorIp("192.168.1.1");
        document.setActionType("CREATE");
        document.setActionDescription("Created document");
        document.setActionCategory("DOCS");
        document.setResourceId("res-123");
        document.setResourceType("DOCUMENT");
        document.setResourceName("Annual Report");
        document.setSource("web-app");
        document.setTenantId("tenant-001");
        document.setCorrelationId("corr-123");
        document.setSessionId("session-abc");
        document.setTags(List.of("env:prod", "region:us-east-1"));
        document.setHash("hash-123");

        Event event = mapper.toDomain(document);

        assertThat(event).isNotNull();
        assertThat(event.id().toString()).isEqualTo(id);
        assertThat(event.timestamp()).isEqualTo(timestamp);
        assertThat(event.actor().id()).isEqualTo("actor-123");
        assertThat(event.actor().type()).isEqualTo(Actor.ActorType.USER);
        assertThat(event.action().type()).isEqualTo(Action.ActionType.CREATE);
        assertThat(event.resource().id()).isEqualTo("res-123");
        assertThat(event.metadata().tenantId()).isEqualTo("tenant-001");
        assertThat(event.hash()).isEqualTo("hash-123");
    }

    @Test
    void shouldReturnNullForNullDocument() {
        Event event = mapper.toDomain(null);

        assertThat(event).isNull();
    }

    @Test
    void shouldConvertTagsListToMap() {
        EventDocument document = new EventDocument();
        document.setId(UUID.randomUUID().toString());
        document.setTimestamp(Instant.now());
        document.setTags(List.of("env:prod", "region:us-east-1"));

        Event event = mapper.toDomain(document);

        assertThat(event.metadata().tags()).isNotNull();
        assertThat(event.metadata().tags()).containsEntry("env", "prod");
        assertThat(event.metadata().tags()).containsEntry("region", "us-east-1");
    }

    @Test
    void shouldHandleEmptyTagsList() {
        EventDocument document = new EventDocument();
        document.setId(UUID.randomUUID().toString());
        document.setTimestamp(Instant.now());
        document.setTags(List.of());

        Event event = mapper.toDomain(document);

        assertThat(event.metadata().tags()).isNull();
    }

    @Test
    void shouldMapEventListToDocumentList() {
        List<Event> events = List.of(createTestEvent(), createTestEvent());

        List<EventDocument> documents = mapper.toDocumentList(events);

        assertThat(documents).hasSize(2);
    }

    @Test
    void shouldReturnEmptyListForNullEventList() {
        List<EventDocument> documents = mapper.toDocumentList(null);

        assertThat(documents).isEmpty();
    }

    @Test
    void shouldMapDocumentListToEventList() {
        EventDocument doc1 = new EventDocument();
        doc1.setId(UUID.randomUUID().toString());
        doc1.setTimestamp(Instant.now());
        EventDocument doc2 = new EventDocument();
        doc2.setId(UUID.randomUUID().toString());
        doc2.setTimestamp(Instant.now());

        List<Event> events = mapper.toDomainList(List.of(doc1, doc2));

        assertThat(events).hasSize(2);
    }

    @Test
    void shouldReturnEmptyListForNullDocumentList() {
        List<Event> events = mapper.toDomainList(null);

        assertThat(events).isEmpty();
    }

    @Test
    void shouldHandleNullTypesInDocument() {
        EventDocument document = new EventDocument();
        document.setId(UUID.randomUUID().toString());
        document.setTimestamp(Instant.now());
        document.setActorType(null);
        document.setActionType(null);
        document.setResourceType(null);

        Event event = mapper.toDomain(document);

        assertThat(event.actor().type()).isNull();
        assertThat(event.action().type()).isNull();
        assertThat(event.resource().type()).isNull();
    }

    @Test
    void shouldHandleNullTagsMap() {
        Event event = new Event(
                UUID.randomUUID(),
                Instant.now(),
                new Actor("actor-123", Actor.ActorType.USER, "John", null, null, null),
                new Action(Action.ActionType.CREATE, null, null),
                new Resource("res-123", Resource.ResourceType.DOCUMENT, "Report", null, null),
                new EventMetadata("web-app", "tenant-001", null, null, null, null),
                null, null, null
        );

        EventDocument document = mapper.toDocument(event);

        assertThat(document.getTags()).isNull();
    }

    @Test
    void shouldHandleEmptyTagsMap() {
        Event event = new Event(
                UUID.randomUUID(),
                Instant.now(),
                new Actor("actor-123", Actor.ActorType.USER, "John", null, null, null),
                new Action(Action.ActionType.CREATE, null, null),
                new Resource("res-123", Resource.ResourceType.DOCUMENT, "Report", null, null),
                new EventMetadata("web-app", "tenant-001", null, null, Map.of(), null),
                null, null, null
        );

        EventDocument document = mapper.toDocument(event);

        assertThat(document.getTags()).isNull();
    }
}
