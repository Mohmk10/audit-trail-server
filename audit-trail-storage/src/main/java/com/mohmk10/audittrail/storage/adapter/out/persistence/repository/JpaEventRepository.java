package com.mohmk10.audittrail.storage.adapter.out.persistence.repository;

import com.mohmk10.audittrail.storage.adapter.out.persistence.entity.EventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaEventRepository extends JpaRepository<EventEntity, UUID> {

    Optional<EventEntity> findTopByTenantIdOrderByCreatedAtDesc(String tenantId);

    Page<EventEntity> findByTenantIdOrderByTimestampDesc(String tenantId, Pageable pageable);

    List<EventEntity> findByTenantIdOrderByCreatedAtAsc(String tenantId);

    @Query("SELECT e FROM EventEntity e WHERE e.tenantId = :tenantId ORDER BY e.createdAt ASC")
    List<EventEntity> findAllByTenantIdForChainVerification(@Param("tenantId") String tenantId);

    @Query("SELECT e FROM EventEntity e WHERE e.correlationId = :correlationId ORDER BY e.timestamp DESC")
    List<EventEntity> findByCorrelationId(@Param("correlationId") String correlationId);

    Optional<EventEntity> findByHash(String hash);
}
