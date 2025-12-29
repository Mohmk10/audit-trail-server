package com.mohmk10.audittrail.ingestion.adapter.in.rest.mapper;

import com.mohmk10.audittrail.core.domain.Action;
import com.mohmk10.audittrail.core.domain.Actor;
import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.domain.EventMetadata;
import com.mohmk10.audittrail.core.domain.Resource;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.ActionRequest;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.ActorRequest;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.EventMetadataRequest;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.EventRequest;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.EventResponse;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.ResourceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EventRequestMapperTest {

    private EventRequestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new EventRequestMapper();
    }

    @Test
    void shouldMapEventRequestToEvent() {
        ActorRequest actor = new ActorRequest("actor-123", "USER", "John Doe", "192.168.1.1", "Mozilla/5.0",
                Map.of("role", "admin"));
        ActionRequest action = new ActionRequest("CREATE", "Created document", "DOCUMENT");
        ResourceRequest resource = new ResourceRequest("res-123", "DOCUMENT", "Annual Report",
                Map.of("status", "draft"), Map.of("status", "published"));
        EventMetadataRequest metadata = new EventMetadataRequest("web-app", "tenant-001", "corr-123", "session-abc",
                Map.of("env", "prod"), Map.of("custom", "value"));

        EventRequest request = new EventRequest(actor, action, resource, metadata);

        Event event = mapper.toEvent(request);

        assertThat(event).isNotNull();
        assertThat(event.id()).isNotNull();
        assertThat(event.timestamp()).isNotNull();
        assertThat(event.actor().id()).isEqualTo("actor-123");
        assertThat(event.actor().type()).isEqualTo(Actor.ActorType.USER);
        assertThat(event.actor().name()).isEqualTo("John Doe");
        assertThat(event.action().type()).isEqualTo(Action.ActionType.CREATE);
        assertThat(event.action().description()).isEqualTo("Created document");
        assertThat(event.resource().id()).isEqualTo("res-123");
        assertThat(event.resource().type()).isEqualTo(Resource.ResourceType.DOCUMENT);
        assertThat(event.metadata().source()).isEqualTo("web-app");
        assertThat(event.metadata().tenantId()).isEqualTo("tenant-001");
    }

    @Test
    void shouldGenerateUUIDAndTimestamp() {
        ActorRequest actor = new ActorRequest("actor-123", "USER", null, null, null, null);
        ActionRequest action = new ActionRequest("READ", null, null);
        ResourceRequest resource = new ResourceRequest("res-123", "FILE", null, null, null);
        EventRequest request = new EventRequest(actor, action, resource, null);

        Event event = mapper.toEvent(request);

        assertThat(event.id()).isNotNull();
        assertThat(event.timestamp()).isNotNull();
        assertThat(event.timestamp()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void shouldMapActorTypeCorrectly() {
        for (String type : new String[]{"USER", "SYSTEM", "SERVICE"}) {
            ActorRequest actor = new ActorRequest("actor-123", type, null, null, null, null);
            EventRequest request = new EventRequest(actor,
                    new ActionRequest("READ", null, null),
                    new ResourceRequest("res-123", "DOCUMENT", null, null, null), null);

            Event event = mapper.toEvent(request);

            assertThat(event.actor().type()).isEqualTo(Actor.ActorType.valueOf(type));
        }
    }

    @Test
    void shouldDefaultToUserForInvalidActorType() {
        ActorRequest actor = new ActorRequest("actor-123", "INVALID_TYPE", null, null, null, null);
        EventRequest request = new EventRequest(actor,
                new ActionRequest("READ", null, null),
                new ResourceRequest("res-123", "DOCUMENT", null, null, null), null);

        Event event = mapper.toEvent(request);

        assertThat(event.actor().type()).isEqualTo(Actor.ActorType.USER);
    }

    @Test
    void shouldMapActionTypeCorrectly() {
        for (String type : new String[]{"CREATE", "READ", "UPDATE", "DELETE", "LOGIN", "LOGOUT"}) {
            ActionRequest action = new ActionRequest(type, null, null);
            EventRequest request = new EventRequest(
                    new ActorRequest("actor-123", "USER", null, null, null, null),
                    action,
                    new ResourceRequest("res-123", "DOCUMENT", null, null, null), null);

            Event event = mapper.toEvent(request);

            assertThat(event.action().type()).isEqualTo(Action.ActionType.valueOf(type));
        }
    }

    @Test
    void shouldDefaultToReadForInvalidActionType() {
        ActionRequest action = new ActionRequest("INVALID_ACTION", null, null);
        EventRequest request = new EventRequest(
                new ActorRequest("actor-123", "USER", null, null, null, null),
                action,
                new ResourceRequest("res-123", "DOCUMENT", null, null, null), null);

        Event event = mapper.toEvent(request);

        assertThat(event.action().type()).isEqualTo(Action.ActionType.READ);
    }

    @Test
    void shouldMapResourceTypeCorrectly() {
        for (String type : new String[]{"DOCUMENT", "USER", "TRANSACTION", "CONFIG", "FILE", "API"}) {
            ResourceRequest resource = new ResourceRequest("res-123", type, null, null, null);
            EventRequest request = new EventRequest(
                    new ActorRequest("actor-123", "USER", null, null, null, null),
                    new ActionRequest("READ", null, null),
                    resource, null);

            Event event = mapper.toEvent(request);

            assertThat(event.resource().type()).isEqualTo(Resource.ResourceType.valueOf(type));
        }
    }

    @Test
    void shouldDefaultToDocumentForInvalidResourceType() {
        ResourceRequest resource = new ResourceRequest("res-123", "INVALID_RESOURCE", null, null, null);
        EventRequest request = new EventRequest(
                new ActorRequest("actor-123", "USER", null, null, null, null),
                new ActionRequest("READ", null, null),
                resource, null);

        Event event = mapper.toEvent(request);

        assertThat(event.resource().type()).isEqualTo(Resource.ResourceType.DOCUMENT);
    }

    @Test
    void shouldUseActorIdAsNameWhenNameIsNull() {
        ActorRequest actor = new ActorRequest("actor-123", "USER", null, null, null, null);
        EventRequest request = new EventRequest(actor,
                new ActionRequest("READ", null, null),
                new ResourceRequest("res-123", "DOCUMENT", null, null, null), null);

        Event event = mapper.toEvent(request);

        assertThat(event.actor().name()).isEqualTo("actor-123");
    }

    @Test
    void shouldUseResourceIdAsNameWhenNameIsNull() {
        ResourceRequest resource = new ResourceRequest("res-123", "DOCUMENT", null, null, null);
        EventRequest request = new EventRequest(
                new ActorRequest("actor-123", "USER", null, null, null, null),
                new ActionRequest("READ", null, null),
                resource, null);

        Event event = mapper.toEvent(request);

        assertThat(event.resource().name()).isEqualTo("res-123");
    }

    @Test
    void shouldUseDefaultMetadataWhenNull() {
        EventRequest request = new EventRequest(
                new ActorRequest("actor-123", "USER", null, null, null, null),
                new ActionRequest("READ", null, null),
                new ResourceRequest("res-123", "DOCUMENT", null, null, null),
                null);

        Event event = mapper.toEvent(request);

        assertThat(event.metadata()).isNotNull();
        assertThat(event.metadata().source()).isEqualTo("unknown");
        assertThat(event.metadata().tenantId()).isEqualTo("default");
    }

    @Test
    void shouldMapEventToResponse() {
        UUID id = UUID.randomUUID();
        Instant timestamp = Instant.now();
        Event event = new Event(
                id,
                timestamp,
                new Actor("actor-123", Actor.ActorType.USER, "John", null, null, null),
                new Action(Action.ActionType.CREATE, "Created", null),
                new Resource("res-123", Resource.ResourceType.DOCUMENT, "Report", null, null),
                new EventMetadata("web-app", "tenant-001", null, null, null, null),
                "prevHash",
                "currentHash",
                "signature"
        );

        EventResponse response = mapper.toResponse(event);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.timestamp()).isEqualTo(timestamp);
        assertThat(response.hash()).isEqualTo("currentHash");
        assertThat(response.status()).isEqualTo("STORED");
    }

    @Test
    void shouldHandleNullHashInResponse() {
        UUID id = UUID.randomUUID();
        Event event = new Event(
                id,
                Instant.now(),
                new Actor("actor-123", Actor.ActorType.USER, "John", null, null, null),
                new Action(Action.ActionType.READ, null, null),
                new Resource("res-123", Resource.ResourceType.DOCUMENT, "Report", null, null),
                new EventMetadata("web-app", "tenant-001", null, null, null, null),
                null,
                null,
                null
        );

        EventResponse response = mapper.toResponse(event);

        assertThat(response.hash()).isNull();
        assertThat(response.status()).isEqualTo("STORED");
    }

    @Test
    void shouldPreserveActorAttributes() {
        Map<String, String> attributes = Map.of("role", "admin", "department", "IT");
        ActorRequest actor = new ActorRequest("actor-123", "USER", "John", null, null, attributes);
        EventRequest request = new EventRequest(actor,
                new ActionRequest("READ", null, null),
                new ResourceRequest("res-123", "DOCUMENT", null, null, null), null);

        Event event = mapper.toEvent(request);

        assertThat(event.actor().attributes()).containsEntry("role", "admin");
        assertThat(event.actor().attributes()).containsEntry("department", "IT");
    }

    @Test
    void shouldPreserveResourceBeforeAndAfter() {
        Map<String, Object> before = Map.of("status", "draft", "version", 1);
        Map<String, Object> after = Map.of("status", "published", "version", 2);
        ResourceRequest resource = new ResourceRequest("res-123", "DOCUMENT", "Report", before, after);
        EventRequest request = new EventRequest(
                new ActorRequest("actor-123", "USER", null, null, null, null),
                new ActionRequest("UPDATE", null, null),
                resource, null);

        Event event = mapper.toEvent(request);

        assertThat(event.resource().before()).containsEntry("status", "draft");
        assertThat(event.resource().after()).containsEntry("status", "published");
    }

    @Test
    void shouldPreserveMetadataTags() {
        Map<String, String> tags = Map.of("env", "prod", "region", "us-east-1");
        EventMetadataRequest metadata = new EventMetadataRequest("web-app", "tenant-001", null, null, tags, null);
        EventRequest request = new EventRequest(
                new ActorRequest("actor-123", "USER", null, null, null, null),
                new ActionRequest("READ", null, null),
                new ResourceRequest("res-123", "DOCUMENT", null, null, null),
                metadata);

        Event event = mapper.toEvent(request);

        assertThat(event.metadata().tags()).containsEntry("env", "prod");
        assertThat(event.metadata().tags()).containsEntry("region", "us-east-1");
    }

    @Test
    void shouldHandleCaseInsensitiveTypes() {
        ActorRequest actor = new ActorRequest("actor-123", "user", null, null, null, null);
        ActionRequest action = new ActionRequest("create", null, null);
        ResourceRequest resource = new ResourceRequest("res-123", "document", null, null, null);
        EventRequest request = new EventRequest(actor, action, resource, null);

        Event event = mapper.toEvent(request);

        assertThat(event.actor().type()).isEqualTo(Actor.ActorType.USER);
        assertThat(event.action().type()).isEqualTo(Action.ActionType.CREATE);
        assertThat(event.resource().type()).isEqualTo(Resource.ResourceType.DOCUMENT);
    }
}
