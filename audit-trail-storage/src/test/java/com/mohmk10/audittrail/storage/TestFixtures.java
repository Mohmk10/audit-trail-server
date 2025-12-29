package com.mohmk10.audittrail.storage;

import com.mohmk10.audittrail.core.domain.*;
import com.mohmk10.audittrail.storage.adapter.out.persistence.entity.EventEntity;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class TestFixtures {

    private TestFixtures() {
    }

    public static Actor createTestActor() {
        return new Actor(
                "user-123",
                Actor.ActorType.USER,
                "John Doe",
                "192.168.1.1",
                "Mozilla/5.0",
                Map.of("role", "admin")
        );
    }

    public static Action createTestAction() {
        return new Action(Action.ActionType.CREATE, "Created document", "DOCUMENT");
    }

    public static Resource createTestResource() {
        return new Resource(
                "doc-456",
                Resource.ResourceType.DOCUMENT,
                "Annual Report",
                null,
                Map.of("status", "created")
        );
    }

    public static EventMetadata createTestMetadata() {
        return new EventMetadata(
                "web-app",
                "tenant-001",
                "corr-123",
                "session-abc",
                Map.of("env", "test"),
                Map.of()
        );
    }

    public static EventMetadata createTestMetadataWithTenant(String tenantId) {
        return new EventMetadata(
                "web-app",
                tenantId,
                "corr-123",
                "session-abc",
                Map.of("env", "test"),
                Map.of()
        );
    }

    public static Event createTestEvent() {
        return new Event(
                UUID.randomUUID(),
                Instant.now(),
                createTestActor(),
                createTestAction(),
                createTestResource(),
                createTestMetadata(),
                null,
                null,
                null
        );
    }

    public static Event createTestEventWithTenant(String tenantId) {
        return new Event(
                UUID.randomUUID(),
                Instant.now(),
                createTestActor(),
                createTestAction(),
                createTestResource(),
                createTestMetadataWithTenant(tenantId),
                null,
                null,
                null
        );
    }

    public static Event createTestEventWithId(UUID id) {
        return new Event(
                id,
                Instant.now(),
                createTestActor(),
                createTestAction(),
                createTestResource(),
                createTestMetadata(),
                null,
                null,
                null
        );
    }

    public static Event createTestEventWithHash(String previousHash, String hash, String signature) {
        return new Event(
                UUID.randomUUID(),
                Instant.now(),
                createTestActor(),
                createTestAction(),
                createTestResource(),
                createTestMetadata(),
                previousHash,
                hash,
                signature
        );
    }

    public static Event createTestEventWithTimestamp(Instant timestamp) {
        return new Event(
                UUID.randomUUID(),
                timestamp,
                createTestActor(),
                createTestAction(),
                createTestResource(),
                createTestMetadata(),
                null,
                null,
                null
        );
    }

    public static EventEntity createTestEventEntity() {
        EventEntity entity = new EventEntity();
        entity.setId(UUID.randomUUID());
        entity.setTimestamp(Instant.now());
        entity.setActorId("user-123");
        entity.setActorType("USER");
        entity.setActorName("John Doe");
        entity.setActorIp("192.168.1.1");
        entity.setActorUserAgent("Mozilla/5.0");
        entity.setActorAttributes(Map.of("role", "admin"));
        entity.setActionType("CREATE");
        entity.setActionDescription("Created document");
        entity.setActionCategory("DOCUMENT");
        entity.setResourceId("doc-456");
        entity.setResourceType("DOCUMENT");
        entity.setResourceName("Annual Report");
        entity.setResourceBefore(null);
        entity.setResourceAfter(Map.of("status", "created"));
        entity.setMetadataSource("web-app");
        entity.setTenantId("tenant-001");
        entity.setCorrelationId("corr-123");
        entity.setSessionId("session-abc");
        entity.setTags(Map.of("env", "test"));
        entity.setExtra(Map.of());
        entity.setPreviousHash("GENESIS");
        entity.setHash("testhash123");
        entity.setSignature("testsignature");
        entity.setCreatedAt(Instant.now());
        return entity;
    }
}
