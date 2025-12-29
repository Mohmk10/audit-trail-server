package com.mohmk10.audittrail.storage.adapter.out.persistence;

import com.mohmk10.audittrail.core.domain.*;
import com.mohmk10.audittrail.storage.TestFixtures;
import com.mohmk10.audittrail.storage.adapter.out.persistence.entity.EventEntity;
import com.mohmk10.audittrail.storage.adapter.out.persistence.mapper.EventMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EventMapperTest {

    @Test
    void shouldMapDomainToEntity() {
        Event event = TestFixtures.createTestEvent();

        EventEntity entity = EventMapper.toEntity(event);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(event.id());
        assertThat(entity.getTimestamp()).isEqualTo(event.timestamp());
    }

    @Test
    void shouldMapEntityToDomain() {
        EventEntity entity = TestFixtures.createTestEventEntity();

        Event event = EventMapper.toDomain(entity);

        assertThat(event).isNotNull();
        assertThat(event.id()).isEqualTo(entity.getId());
        assertThat(event.timestamp()).isEqualTo(entity.getTimestamp());
    }

    @Test
    void shouldHandleNullOptionalFields() {
        Event event = new Event(
                UUID.randomUUID(),
                Instant.now(),
                new Actor("actor", Actor.ActorType.USER, "Name", null, null, null),
                new Action(Action.ActionType.CREATE, null, null),
                new Resource("res", Resource.ResourceType.DOCUMENT, "Name", null, null),
                new EventMetadata("source", "tenant", null, null, null, null),
                null,
                null,
                null
        );

        EventEntity entity = EventMapper.toEntity(event);

        assertThat(entity.getActorIp()).isNull();
        assertThat(entity.getActorUserAgent()).isNull();
        assertThat(entity.getActorAttributes()).isNull();
        assertThat(entity.getActionDescription()).isNull();
        assertThat(entity.getActionCategory()).isNull();
        assertThat(entity.getResourceBefore()).isNull();
        assertThat(entity.getResourceAfter()).isNull();
        assertThat(entity.getCorrelationId()).isNull();
        assertThat(entity.getSessionId()).isNull();
        assertThat(entity.getTags()).isNull();
        assertThat(entity.getExtra()).isNull();
        assertThat(entity.getPreviousHash()).isNull();
        assertThat(entity.getHash()).isNull();
        assertThat(entity.getSignature()).isNull();
    }

    @Test
    void shouldPreserveAllActorFields() {
        Actor actor = new Actor(
                "user-123",
                Actor.ActorType.SERVICE,
                "API Service",
                "10.0.0.1",
                "CustomAgent/1.0",
                Map.of("version", "1.0", "env", "prod")
        );
        Event event = new Event(
                UUID.randomUUID(),
                Instant.now(),
                actor,
                TestFixtures.createTestAction(),
                TestFixtures.createTestResource(),
                TestFixtures.createTestMetadata(),
                null, null, null
        );

        EventEntity entity = EventMapper.toEntity(event);

        assertThat(entity.getActorId()).isEqualTo("user-123");
        assertThat(entity.getActorType()).isEqualTo("SERVICE");
        assertThat(entity.getActorName()).isEqualTo("API Service");
        assertThat(entity.getActorIp()).isEqualTo("10.0.0.1");
        assertThat(entity.getActorUserAgent()).isEqualTo("CustomAgent/1.0");
        assertThat(entity.getActorAttributes()).containsEntry("version", "1.0");
        assertThat(entity.getActorAttributes()).containsEntry("env", "prod");
    }

    @Test
    void shouldPreserveAllActionFields() {
        Action action = new Action(
                Action.ActionType.DELETE,
                "Deleted important document",
                "DOCUMENT_MANAGEMENT"
        );
        Event event = new Event(
                UUID.randomUUID(),
                Instant.now(),
                TestFixtures.createTestActor(),
                action,
                TestFixtures.createTestResource(),
                TestFixtures.createTestMetadata(),
                null, null, null
        );

        EventEntity entity = EventMapper.toEntity(event);

        assertThat(entity.getActionType()).isEqualTo("DELETE");
        assertThat(entity.getActionDescription()).isEqualTo("Deleted important document");
        assertThat(entity.getActionCategory()).isEqualTo("DOCUMENT_MANAGEMENT");
    }

    @Test
    void shouldPreserveAllResourceFields() {
        Resource resource = new Resource(
                "doc-789",
                Resource.ResourceType.TRANSACTION,
                "Payment Transaction",
                Map.of("amount", 1000, "status", "pending"),
                Map.of("amount", 1000, "status", "completed")
        );
        Event event = new Event(
                UUID.randomUUID(),
                Instant.now(),
                TestFixtures.createTestActor(),
                TestFixtures.createTestAction(),
                resource,
                TestFixtures.createTestMetadata(),
                null, null, null
        );

        EventEntity entity = EventMapper.toEntity(event);

        assertThat(entity.getResourceId()).isEqualTo("doc-789");
        assertThat(entity.getResourceType()).isEqualTo("TRANSACTION");
        assertThat(entity.getResourceName()).isEqualTo("Payment Transaction");
        assertThat(entity.getResourceBefore()).containsEntry("status", "pending");
        assertThat(entity.getResourceAfter()).containsEntry("status", "completed");
    }

    @Test
    void shouldPreserveAllMetadataFields() {
        EventMetadata metadata = new EventMetadata(
                "mobile-app",
                "tenant-xyz",
                "corr-abc-123",
                "session-def-456",
                Map.of("region", "us-west", "priority", "high"),
                Map.of("deviceId", "device-001", "appVersion", "2.0")
        );
        Event event = new Event(
                UUID.randomUUID(),
                Instant.now(),
                TestFixtures.createTestActor(),
                TestFixtures.createTestAction(),
                TestFixtures.createTestResource(),
                metadata,
                null, null, null
        );

        EventEntity entity = EventMapper.toEntity(event);

        assertThat(entity.getMetadataSource()).isEqualTo("mobile-app");
        assertThat(entity.getTenantId()).isEqualTo("tenant-xyz");
        assertThat(entity.getCorrelationId()).isEqualTo("corr-abc-123");
        assertThat(entity.getSessionId()).isEqualTo("session-def-456");
        assertThat(entity.getTags()).containsEntry("region", "us-west");
        assertThat(entity.getTags()).containsEntry("priority", "high");
        assertThat(entity.getExtra()).containsEntry("deviceId", "device-001");
    }

    @Test
    void shouldPreserveHashChainFields() {
        Event event = TestFixtures.createTestEventWithHash("prev-hash", "curr-hash", "signature");

        EventEntity entity = EventMapper.toEntity(event);

        assertThat(entity.getPreviousHash()).isEqualTo("prev-hash");
        assertThat(entity.getHash()).isEqualTo("curr-hash");
        assertThat(entity.getSignature()).isEqualTo("signature");
    }

    @Test
    void shouldHandleNullDomainEvent() {
        EventEntity entity = EventMapper.toEntity(null);

        assertThat(entity).isNull();
    }

    @Test
    void shouldHandleNullEntity() {
        Event event = EventMapper.toDomain(null);

        assertThat(event).isNull();
    }

    @Test
    void shouldRoundTripConversion() {
        Event originalEvent = TestFixtures.createTestEvent();

        EventEntity entity = EventMapper.toEntity(originalEvent);
        Event convertedEvent = EventMapper.toDomain(entity);

        assertThat(convertedEvent.id()).isEqualTo(originalEvent.id());
        assertThat(convertedEvent.timestamp()).isEqualTo(originalEvent.timestamp());
        assertThat(convertedEvent.actor().id()).isEqualTo(originalEvent.actor().id());
        assertThat(convertedEvent.actor().type()).isEqualTo(originalEvent.actor().type());
        assertThat(convertedEvent.actor().name()).isEqualTo(originalEvent.actor().name());
        assertThat(convertedEvent.action().type()).isEqualTo(originalEvent.action().type());
        assertThat(convertedEvent.resource().id()).isEqualTo(originalEvent.resource().id());
        assertThat(convertedEvent.metadata().tenantId()).isEqualTo(originalEvent.metadata().tenantId());
    }

    @Test
    void shouldConvertActorTypeFromStringToEnum() {
        EventEntity entity = TestFixtures.createTestEventEntity();
        entity.setActorType("SYSTEM");

        Event event = EventMapper.toDomain(entity);

        assertThat(event.actor().type()).isEqualTo(Actor.ActorType.SYSTEM);
    }

    @Test
    void shouldConvertActionTypeFromStringToEnum() {
        EventEntity entity = TestFixtures.createTestEventEntity();
        entity.setActionType("UPDATE");

        Event event = EventMapper.toDomain(entity);

        assertThat(event.action().type()).isEqualTo(Action.ActionType.UPDATE);
    }

    @Test
    void shouldConvertResourceTypeFromStringToEnum() {
        EventEntity entity = TestFixtures.createTestEventEntity();
        entity.setResourceType("FILE");

        Event event = EventMapper.toDomain(entity);

        assertThat(event.resource().type()).isEqualTo(Resource.ResourceType.FILE);
    }
}
