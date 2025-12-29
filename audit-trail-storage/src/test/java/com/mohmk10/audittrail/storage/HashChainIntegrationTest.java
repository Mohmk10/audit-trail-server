package com.mohmk10.audittrail.storage;

import com.mohmk10.audittrail.core.domain.*;
import com.mohmk10.audittrail.storage.adapter.out.persistence.entity.EventEntity;
import com.mohmk10.audittrail.storage.adapter.out.persistence.mapper.EventMapper;
import com.mohmk10.audittrail.storage.adapter.out.persistence.repository.JpaEventRepository;
import com.mohmk10.audittrail.storage.service.HashChainService;
import com.mohmk10.audittrail.storage.service.HashChainServiceImpl;
import com.mohmk10.audittrail.storage.service.ImmutableStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class HashChainIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ImmutableStorageService storageService;

    @Autowired
    private HashChainService hashChainService;

    @Autowired
    private JpaEventRepository jpaEventRepository;

    @BeforeEach
    void setUp() {
        jpaEventRepository.deleteAll();
    }

    @Test
    void shouldCreateValidChainOfEvents() {
        String tenantId = "chain-test-tenant";
        List<Event> storedEvents = new ArrayList<>();

        // Create 10 events
        for (int i = 0; i < 10; i++) {
            Event event = createTestEventWithTenant(tenantId);
            Event stored = storageService.store(event);
            storedEvents.add(stored);
        }

        // Verify all events were stored
        assertThat(storedEvents).hasSize(10);

        // Verify chain integrity
        List<EventEntity> entities = jpaEventRepository.findAllByTenantIdForChainVerification(tenantId);
        List<Event> chain = entities.stream()
                .map(EventMapper::toDomain)
                .toList();

        boolean isValid = hashChainService.verifyChain(chain);
        assertThat(isValid).isTrue();

        // Verify chain links
        String expectedPreviousHash = HashChainServiceImpl.GENESIS_HASH;
        for (Event event : chain) {
            assertThat(event.previousHash()).isEqualTo(expectedPreviousHash);
            assertThat(event.hash()).isNotNull();
            assertThat(event.hash()).isNotEmpty();
            expectedPreviousHash = event.hash();
        }
    }

    @Test
    void shouldDetectTamperedEventInChain() {
        String tenantId = "tamper-test-tenant";

        // Create a chain of 5 events
        for (int i = 0; i < 5; i++) {
            Event event = createTestEventWithTenant(tenantId);
            storageService.store(event);
        }

        // Get all events
        List<EventEntity> entities = jpaEventRepository.findAllByTenantIdForChainVerification(tenantId);
        assertThat(entities).hasSize(5);

        // Tamper with the middle event's hash
        EventEntity tamperedEntity = entities.get(2);
        tamperedEntity.setHash("tampered-hash-value");
        jpaEventRepository.save(tamperedEntity);

        // Verify chain - should fail
        List<Event> chain = jpaEventRepository.findAllByTenantIdForChainVerification(tenantId)
                .stream()
                .map(EventMapper::toDomain)
                .toList();

        boolean isValid = hashChainService.verifyChain(chain);
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldVerifyIndividualEventIntegrity() {
        String tenantId = "integrity-test-tenant";

        Event event = createTestEventWithTenant(tenantId);
        Event stored = storageService.store(event);

        boolean isValid = storageService.verifyIntegrity(stored.id());
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldDetectIntegrityViolation() {
        String tenantId = "violation-test-tenant";

        Event event = createTestEventWithTenant(tenantId);
        Event stored = storageService.store(event);

        // Tamper with the stored event
        EventEntity entity = jpaEventRepository.findById(stored.id()).orElseThrow();
        entity.setActorId("tampered-actor-id");
        jpaEventRepository.save(entity);

        // The integrity check should now fail
        boolean isValid = storageService.verifyIntegrity(stored.id());
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldHandleMultipleTenantsConcurrently() throws InterruptedException {
        int numTenants = 3;
        int eventsPerTenant = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numTenants);
        CountDownLatch latch = new CountDownLatch(numTenants);

        for (int t = 0; t < numTenants; t++) {
            final String tenantId = "concurrent-tenant-" + t;
            executor.submit(() -> {
                try {
                    for (int e = 0; e < eventsPerTenant; e++) {
                        Event event = createTestEventWithTenant(tenantId);
                        storageService.store(event);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Verify each tenant's chain independently
        for (int t = 0; t < numTenants; t++) {
            String tenantId = "concurrent-tenant-" + t;
            List<EventEntity> entities = jpaEventRepository.findAllByTenantIdForChainVerification(tenantId);
            List<Event> chain = entities.stream()
                    .map(EventMapper::toDomain)
                    .toList();

            assertThat(chain).hasSize(eventsPerTenant);
            boolean isValid = hashChainService.verifyChain(chain);
            assertThat(isValid).isTrue();
        }
    }

    @Test
    void shouldStoreBatchAndMaintainChain() {
        String tenantId = "batch-chain-tenant";

        List<Event> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            events.add(createTestEventWithTenant(tenantId));
        }

        List<Event> stored = storageService.storeBatch(events);

        assertThat(stored).hasSize(5);

        // Verify chain
        List<EventEntity> entities = jpaEventRepository.findAllByTenantIdForChainVerification(tenantId);
        List<Event> chain = entities.stream()
                .map(EventMapper::toDomain)
                .toList();

        boolean isValid = hashChainService.verifyChain(chain);
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldRetrieveLastHashForTenant() {
        String tenantId = "last-hash-tenant";

        // Initially should return GENESIS
        String initialHash = hashChainService.getLastHash(tenantId);
        assertThat(initialHash).isEqualTo(HashChainServiceImpl.GENESIS_HASH);

        // After storing an event
        Event event = createTestEventWithTenant(tenantId);
        Event stored = storageService.store(event);

        String lastHash = hashChainService.getLastHash(tenantId);
        assertThat(lastHash).isEqualTo(stored.hash());
    }

    private Event createTestEventWithTenant(String tenantId) {
        return new Event(
                UUID.randomUUID(),
                Instant.now(),
                new Actor("user-" + UUID.randomUUID().toString().substring(0, 8),
                        Actor.ActorType.USER, "Test User", null, null, null),
                new Action(Action.ActionType.CREATE, "Test action", null),
                new Resource("res-" + UUID.randomUUID().toString().substring(0, 8),
                        Resource.ResourceType.DOCUMENT, "Test Document", null, null),
                new EventMetadata("test-app", tenantId, null, null, null, null),
                null,
                null,
                null
        );
    }
}
