package com.mohmk10.audittrail.storage.service;

import com.mohmk10.audittrail.core.domain.*;
import com.mohmk10.audittrail.storage.TestFixtures;
import com.mohmk10.audittrail.storage.adapter.out.persistence.entity.EventEntity;
import com.mohmk10.audittrail.storage.adapter.out.persistence.repository.JpaEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HashChainServiceTest {

    @Mock
    private JpaEventRepository jpaEventRepository;

    private HashChainServiceImpl hashChainService;

    @BeforeEach
    void setUp() {
        hashChainService = new HashChainServiceImpl(jpaEventRepository);
    }

    @Test
    void shouldCalculateHashForFirstEvent() {
        Event event = TestFixtures.createTestEvent();

        String hash = hashChainService.calculateHash(event, HashChainServiceImpl.GENESIS_HASH);

        assertThat(hash).isNotNull();
        assertThat(hash).isNotEmpty();
        assertThat(hash).hasSize(64); // SHA-256 produces 64 hex characters
    }

    @Test
    void shouldCalculateHashWithPreviousHash() {
        Event event = TestFixtures.createTestEvent();
        String previousHash = "abc123def456789";

        String hash = hashChainService.calculateHash(event, previousHash);

        assertThat(hash).isNotNull();
        assertThat(hash).isNotEmpty();
    }

    @Test
    void shouldProduceDifferentHashesForDifferentEvents() {
        Event event1 = TestFixtures.createTestEvent();
        Event event2 = TestFixtures.createTestEvent();

        String hash1 = hashChainService.calculateHash(event1, HashChainServiceImpl.GENESIS_HASH);
        String hash2 = hashChainService.calculateHash(event2, HashChainServiceImpl.GENESIS_HASH);

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void shouldProduceSameHashForSameInput() {
        UUID id = UUID.randomUUID();
        Instant timestamp = Instant.parse("2024-01-15T10:30:00Z");
        Actor actor = new Actor("actor-1", Actor.ActorType.USER, "User", null, null, null);
        Action action = new Action(Action.ActionType.CREATE, "desc", null);
        Resource resource = new Resource("res-1", Resource.ResourceType.DOCUMENT, "Doc", null, null);
        EventMetadata metadata = new EventMetadata("src", "tenant", null, null, null, null);

        Event event1 = new Event(id, timestamp, actor, action, resource, metadata, null, null, null);
        Event event2 = new Event(id, timestamp, actor, action, resource, metadata, null, null, null);

        String hash1 = hashChainService.calculateHash(event1, HashChainServiceImpl.GENESIS_HASH);
        String hash2 = hashChainService.calculateHash(event2, HashChainServiceImpl.GENESIS_HASH);

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void shouldIncludeTimestampInHash() {
        Instant timestamp1 = Instant.parse("2024-01-15T10:00:00Z");
        Instant timestamp2 = Instant.parse("2024-01-15T11:00:00Z");

        Event event1 = TestFixtures.createTestEventWithTimestamp(timestamp1);
        Event event2 = TestFixtures.createTestEventWithTimestamp(timestamp2);

        String hash1 = hashChainService.calculateHash(event1, HashChainServiceImpl.GENESIS_HASH);
        String hash2 = hashChainService.calculateHash(event2, HashChainServiceImpl.GENESIS_HASH);

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void shouldIncludeActorIdInHash() {
        Actor actor1 = new Actor("actor-1", Actor.ActorType.USER, "User", null, null, null);
        Actor actor2 = new Actor("actor-2", Actor.ActorType.USER, "User", null, null, null);

        Instant timestamp = Instant.now();
        Action action = new Action(Action.ActionType.CREATE, null, null);
        Resource resource = new Resource("res", Resource.ResourceType.DOCUMENT, "Doc", null, null);
        EventMetadata metadata = new EventMetadata("src", "tenant", null, null, null, null);

        Event event1 = new Event(UUID.randomUUID(), timestamp, actor1, action, resource, metadata, null, null, null);
        Event event2 = new Event(UUID.randomUUID(), timestamp, actor2, action, resource, metadata, null, null, null);

        String hash1 = hashChainService.calculateHash(event1, HashChainServiceImpl.GENESIS_HASH);
        String hash2 = hashChainService.calculateHash(event2, HashChainServiceImpl.GENESIS_HASH);

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void shouldIncludeActionTypeInHash() {
        Instant timestamp = Instant.now();
        Actor actor = new Actor("actor", Actor.ActorType.USER, "User", null, null, null);
        Resource resource = new Resource("res", Resource.ResourceType.DOCUMENT, "Doc", null, null);
        EventMetadata metadata = new EventMetadata("src", "tenant", null, null, null, null);

        Action action1 = new Action(Action.ActionType.CREATE, null, null);
        Action action2 = new Action(Action.ActionType.UPDATE, null, null);

        Event event1 = new Event(UUID.randomUUID(), timestamp, actor, action1, resource, metadata, null, null, null);
        Event event2 = new Event(UUID.randomUUID(), timestamp, actor, action2, resource, metadata, null, null, null);

        String hash1 = hashChainService.calculateHash(event1, HashChainServiceImpl.GENESIS_HASH);
        String hash2 = hashChainService.calculateHash(event2, HashChainServiceImpl.GENESIS_HASH);

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void shouldIncludeResourceIdInHash() {
        Instant timestamp = Instant.now();
        Actor actor = new Actor("actor", Actor.ActorType.USER, "User", null, null, null);
        Action action = new Action(Action.ActionType.CREATE, null, null);
        EventMetadata metadata = new EventMetadata("src", "tenant", null, null, null, null);

        Resource resource1 = new Resource("res-1", Resource.ResourceType.DOCUMENT, "Doc", null, null);
        Resource resource2 = new Resource("res-2", Resource.ResourceType.DOCUMENT, "Doc", null, null);

        Event event1 = new Event(UUID.randomUUID(), timestamp, actor, action, resource1, metadata, null, null, null);
        Event event2 = new Event(UUID.randomUUID(), timestamp, actor, action, resource2, metadata, null, null, null);

        String hash1 = hashChainService.calculateHash(event1, HashChainServiceImpl.GENESIS_HASH);
        String hash2 = hashChainService.calculateHash(event2, HashChainServiceImpl.GENESIS_HASH);

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void shouldVerifyValidChain() {
        Instant timestamp1 = Instant.parse("2024-01-15T10:00:00Z");
        Instant timestamp2 = Instant.parse("2024-01-15T10:01:00Z");
        Instant timestamp3 = Instant.parse("2024-01-15T10:02:00Z");

        Actor actor = new Actor("actor", Actor.ActorType.USER, "User", null, null, null);
        Action action = new Action(Action.ActionType.CREATE, null, null);
        Resource resource = new Resource("res", Resource.ResourceType.DOCUMENT, "Doc", null, null);
        EventMetadata metadata = new EventMetadata("src", "tenant", null, null, null, null);

        Event event1 = new Event(UUID.randomUUID(), timestamp1, actor, action, resource, metadata,
                HashChainServiceImpl.GENESIS_HASH, null, null);
        String hash1 = hashChainService.calculateHash(event1, HashChainServiceImpl.GENESIS_HASH);
        Event event1WithHash = new Event(event1.id(), event1.timestamp(), actor, action, resource, metadata,
                HashChainServiceImpl.GENESIS_HASH, hash1, null);

        Event event2 = new Event(UUID.randomUUID(), timestamp2, actor, action, resource, metadata, hash1, null, null);
        String hash2 = hashChainService.calculateHash(event2, hash1);
        Event event2WithHash = new Event(event2.id(), event2.timestamp(), actor, action, resource, metadata,
                hash1, hash2, null);

        Event event3 = new Event(UUID.randomUUID(), timestamp3, actor, action, resource, metadata, hash2, null, null);
        String hash3 = hashChainService.calculateHash(event3, hash2);
        Event event3WithHash = new Event(event3.id(), event3.timestamp(), actor, action, resource, metadata,
                hash2, hash3, null);

        List<Event> chain = List.of(event1WithHash, event2WithHash, event3WithHash);

        boolean isValid = hashChainService.verifyChain(chain);

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldDetectTamperedEvent() {
        Actor actor = new Actor("actor", Actor.ActorType.USER, "User", null, null, null);
        Action action = new Action(Action.ActionType.CREATE, null, null);
        Resource resource = new Resource("res", Resource.ResourceType.DOCUMENT, "Doc", null, null);
        EventMetadata metadata = new EventMetadata("src", "tenant", null, null, null, null);

        Event event = new Event(UUID.randomUUID(), Instant.now(), actor, action, resource, metadata,
                HashChainServiceImpl.GENESIS_HASH, "tampered-hash-value", null);

        List<Event> chain = List.of(event);

        boolean isValid = hashChainService.verifyChain(chain);

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldDetectMissingEvent() {
        Instant timestamp1 = Instant.parse("2024-01-15T10:00:00Z");
        Instant timestamp2 = Instant.parse("2024-01-15T10:02:00Z");

        Actor actor = new Actor("actor", Actor.ActorType.USER, "User", null, null, null);
        Action action = new Action(Action.ActionType.CREATE, null, null);
        Resource resource = new Resource("res", Resource.ResourceType.DOCUMENT, "Doc", null, null);
        EventMetadata metadata = new EventMetadata("src", "tenant", null, null, null, null);

        Event event1 = new Event(UUID.randomUUID(), timestamp1, actor, action, resource, metadata,
                HashChainServiceImpl.GENESIS_HASH, null, null);
        String hash1 = hashChainService.calculateHash(event1, HashChainServiceImpl.GENESIS_HASH);
        Event event1WithHash = new Event(event1.id(), event1.timestamp(), actor, action, resource, metadata,
                HashChainServiceImpl.GENESIS_HASH, hash1, null);

        // Simulating a missing event by providing wrong previousHash
        String wrongPreviousHash = "wrong-hash-simulating-missing-event";
        Event event2 = new Event(UUID.randomUUID(), timestamp2, actor, action, resource, metadata,
                wrongPreviousHash, null, null);
        String hash2 = hashChainService.calculateHash(event2, wrongPreviousHash);
        Event event2WithHash = new Event(event2.id(), event2.timestamp(), actor, action, resource, metadata,
                wrongPreviousHash, hash2, null);

        List<Event> chain = List.of(event1WithHash, event2WithHash);

        boolean isValid = hashChainService.verifyChain(chain);

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnTrueForEmptyChain() {
        boolean isValid = hashChainService.verifyChain(List.of());

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldReturnTrueForNullChain() {
        boolean isValid = hashChainService.verifyChain(null);

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldGetLastHashFromRepository() {
        EventEntity entity = TestFixtures.createTestEventEntity();
        entity.setHash("last-hash-in-chain");
        when(jpaEventRepository.findTopByTenantIdOrderByCreatedAtDesc("tenant-001"))
                .thenReturn(Optional.of(entity));

        String lastHash = hashChainService.getLastHash("tenant-001");

        assertThat(lastHash).isEqualTo("last-hash-in-chain");
    }

    @Test
    void shouldReturnGenesisHashWhenNoEventsExist() {
        when(jpaEventRepository.findTopByTenantIdOrderByCreatedAtDesc(anyString()))
                .thenReturn(Optional.empty());

        String lastHash = hashChainService.getLastHash("new-tenant");

        assertThat(lastHash).isEqualTo(HashChainServiceImpl.GENESIS_HASH);
    }

    @Test
    void shouldHandleNullPreviousHash() {
        Event event = TestFixtures.createTestEvent();

        String hash = hashChainService.calculateHash(event, null);

        assertThat(hash).isNotNull();
        assertThat(hash).isNotEmpty();
    }
}
