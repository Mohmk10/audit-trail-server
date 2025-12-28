package com.mohmk10.audittrail.admin.adapter.out.persistence.repository;

import com.mohmk10.audittrail.admin.adapter.out.persistence.entity.ApiKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaApiKeyRepository extends JpaRepository<ApiKeyEntity, UUID> {
    List<ApiKeyEntity> findByTenantId(String tenantId);
    Optional<ApiKeyEntity> findByKeyPrefix(String keyPrefix);
    List<ApiKeyEntity> findBySourceId(UUID sourceId);
    Optional<ApiKeyEntity> findByKeyHash(String keyHash);

    int countByTenantIdAndStatus(String tenantId, com.mohmk10.audittrail.admin.domain.ApiKeyStatus status);

    @Modifying
    @Query("UPDATE ApiKeyEntity a SET a.lastUsedAt = :timestamp, a.lastUsedIp = :ip WHERE a.id = :id")
    void updateLastUsed(UUID id, Instant timestamp, String ip);
}
