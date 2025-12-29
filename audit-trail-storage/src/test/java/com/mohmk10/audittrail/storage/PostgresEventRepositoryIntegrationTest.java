package com.mohmk10.audittrail.storage;

import com.mohmk10.audittrail.core.domain.*;
import com.mohmk10.audittrail.storage.adapter.out.persistence.entity.EventEntity;
import com.mohmk10.audittrail.storage.adapter.out.persistence.repository.JpaEventRepository;
import com.mohmk10.audittrail.storage.service.HashChainServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresEventRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JpaEventRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldSaveEvent() {
        EventEntity entity = createTestEntity();

        EventEntity saved = repository.save(entity);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindById() {
        EventEntity entity = createTestEntity();
        EventEntity saved = repository.save(entity);

        Optional<EventEntity> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getActorId()).isEqualTo(entity.getActorId());
        assertThat(found.get().getActionType()).isEqualTo(entity.getActionType());
    }

    @Test
    void shouldFindByTenantId() {
        EventEntity entity1 = createTestEntityWithTenant("tenant-A");
        EventEntity entity2 = createTestEntityWithTenant("tenant-A");
        EventEntity entity3 = createTestEntityWithTenant("tenant-B");

        repository.save(entity1);
        repository.save(entity2);
        repository.save(entity3);

        Page<EventEntity> result = repository.findByTenantIdOrderByTimestampDesc("tenant-A", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(e -> e.getTenantId().equals("tenant-A"));
    }

    @Test
    void shouldMaintainHashChain() {
        EventEntity first = createTestEntityWithTenant("tenant-chain");
        first.setPreviousHash(HashChainServiceImpl.GENESIS_HASH);
        first.setHash("hash-1");
        repository.save(first);

        EventEntity second = createTestEntityWithTenant("tenant-chain");
        second.setPreviousHash("hash-1");
        second.setHash("hash-2");
        repository.save(second);

        Optional<EventEntity> latestOpt = repository.findTopByTenantIdOrderByCreatedAtDesc("tenant-chain");

        assertThat(latestOpt).isPresent();
        assertThat(latestOpt.get().getHash()).isEqualTo("hash-2");
        assertThat(latestOpt.get().getPreviousHash()).isEqualTo("hash-1");
    }

    @Test
    void shouldFindByCorrelationId() {
        EventEntity entity1 = createTestEntity();
        entity1.setCorrelationId("corr-123");
        EventEntity entity2 = createTestEntity();
        entity2.setCorrelationId("corr-123");
        EventEntity entity3 = createTestEntity();
        entity3.setCorrelationId("corr-456");

        repository.save(entity1);
        repository.save(entity2);
        repository.save(entity3);

        var result = repository.findByCorrelationId("corr-123");

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(e -> e.getCorrelationId().equals("corr-123"));
    }

    @Test
    void shouldFindByHash() {
        EventEntity entity = createTestEntity();
        entity.setHash("unique-hash-value");
        repository.save(entity);

        Optional<EventEntity> found = repository.findByHash("unique-hash-value");

        assertThat(found).isPresent();
        assertThat(found.get().getHash()).isEqualTo("unique-hash-value");
    }

    @Test
    void shouldReturnEmptyForNonExistentId() {
        Optional<EventEntity> result = repository.findById(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    void shouldPreserveJsonbFields() {
        EventEntity entity = createTestEntity();
        entity.setActorAttributes(Map.of("role", "admin", "department", "IT"));
        entity.setResourceBefore(Map.of("status", "draft", "version", 1));
        entity.setResourceAfter(Map.of("status", "published", "version", 2));
        entity.setTags(Map.of("env", "production"));
        entity.setExtra(Map.of("customField", "customValue"));

        EventEntity saved = repository.save(entity);
        Optional<EventEntity> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getActorAttributes()).containsEntry("role", "admin");
        assertThat(found.get().getResourceBefore()).containsEntry("status", "draft");
        assertThat(found.get().getResourceAfter()).containsEntry("status", "published");
        assertThat(found.get().getTags()).containsEntry("env", "production");
        assertThat(found.get().getExtra()).containsEntry("customField", "customValue");
    }

    @Test
    void shouldOrderByTimestampDesc() {
        Instant now = Instant.now();

        EventEntity older = createTestEntityWithTenant("tenant-order");
        older.setTimestamp(now.minusSeconds(3600));
        repository.save(older);

        EventEntity newer = createTestEntityWithTenant("tenant-order");
        newer.setTimestamp(now);
        repository.save(newer);

        Page<EventEntity> result = repository.findByTenantIdOrderByTimestampDesc("tenant-order", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getTimestamp()).isAfterOrEqualTo(result.getContent().get(1).getTimestamp());
    }

    private EventEntity createTestEntity() {
        return createTestEntityWithTenant("tenant-001");
    }

    private EventEntity createTestEntityWithTenant(String tenantId) {
        EventEntity entity = new EventEntity();
        entity.setId(UUID.randomUUID());
        entity.setTimestamp(Instant.now());
        entity.setActorId("user-" + UUID.randomUUID().toString().substring(0, 8));
        entity.setActorType("USER");
        entity.setActorName("Test User");
        entity.setActionType("CREATE");
        entity.setActionDescription("Test action");
        entity.setResourceId("res-" + UUID.randomUUID().toString().substring(0, 8));
        entity.setResourceType("DOCUMENT");
        entity.setResourceName("Test Document");
        entity.setMetadataSource("test-app");
        entity.setTenantId(tenantId);
        entity.setPreviousHash("GENESIS");
        entity.setHash("hash-" + UUID.randomUUID().toString().substring(0, 8));
        entity.setSignature("signature");
        return entity;
    }
}
