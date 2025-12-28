package com.mohmk10.audittrail.admin.adapter.out.persistence.repository;

import com.mohmk10.audittrail.admin.adapter.out.persistence.entity.SourceEntity;
import com.mohmk10.audittrail.admin.domain.SourceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface JpaSourceRepository extends JpaRepository<SourceEntity, UUID> {
    List<SourceEntity> findByTenantId(String tenantId);
    List<SourceEntity> findByTenantIdAndStatus(String tenantId, SourceStatus status);

    int countByTenantIdAndStatus(String tenantId, SourceStatus status);

    @Modifying
    @Query("UPDATE SourceEntity s SET s.eventCount = s.eventCount + 1 WHERE s.id = :id")
    void incrementEventCount(UUID id);

    @Modifying
    @Query("UPDATE SourceEntity s SET s.lastEventAt = :timestamp WHERE s.id = :id")
    void updateLastEventAt(UUID id, Instant timestamp);
}
