package com.mohmk10.audittrail.core.fixtures;

import com.mohmk10.audittrail.core.domain.*;

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

    public static Actor createMinimalActor() {
        return new Actor(
                "user-minimal",
                Actor.ActorType.USER,
                "Minimal User",
                null,
                null,
                null
        );
    }

    public static Actor createSystemActor() {
        return new Actor(
                "system-001",
                Actor.ActorType.SYSTEM,
                "Background Job",
                null,
                null,
                Map.of()
        );
    }

    public static Actor createServiceActor() {
        return new Actor(
                "service-api",
                Actor.ActorType.SERVICE,
                "API Gateway",
                "10.0.0.1",
                "ServiceClient/1.0",
                Map.of("version", "1.0.0")
        );
    }

    public static Action createTestAction() {
        return new Action(Action.ActionType.CREATE, "Created document", "DOCUMENT");
    }

    public static Action createMinimalAction() {
        return new Action(Action.ActionType.READ, null, null);
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

    public static Resource createResourceWithBothStates() {
        return new Resource(
                "user-789",
                Resource.ResourceType.USER,
                "User Profile",
                Map.of("email", "old@example.com", "name", "Old Name"),
                Map.of("email", "new@example.com", "name", "New Name")
        );
    }

    public static Resource createMinimalResource() {
        return new Resource(
                "res-minimal",
                Resource.ResourceType.FILE,
                "Minimal Resource",
                null,
                null
        );
    }

    public static EventMetadata createTestMetadata() {
        return new EventMetadata(
                "web-app",
                "tenant-001",
                "corr-123",
                "session-abc",
                Map.of("env", "test"),
                Map.of("custom", "value")
        );
    }

    public static EventMetadata createMinimalMetadata() {
        return new EventMetadata(
                "minimal-source",
                "tenant-minimal",
                null,
                null,
                null,
                null
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

    public static Event createTestEventWithHash(String previousHash, String hash) {
        return new Event(
                UUID.randomUUID(),
                Instant.now(),
                createTestActor(),
                createTestAction(),
                createTestResource(),
                createTestMetadata(),
                previousHash,
                hash,
                "test-signature"
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

    public static Event createMinimalEvent() {
        return new Event(
                UUID.randomUUID(),
                Instant.now(),
                createMinimalActor(),
                createMinimalAction(),
                createMinimalResource(),
                createMinimalMetadata(),
                null,
                null,
                null
        );
    }

    public static EventChain createTestEventChain() {
        return new EventChain(
                "previous-hash-abc",
                "current-hash-xyz",
                "signature-123",
                Instant.now()
        );
    }

    public static EventChain createGenesisEventChain() {
        return new EventChain(
                null,
                "genesis-hash",
                "genesis-signature",
                Instant.now()
        );
    }
}
