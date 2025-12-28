package com.mohmk10.audittrail.admin.adapter.out.persistence.repository;

import com.mohmk10.audittrail.admin.adapter.out.persistence.entity.AdminAuditLogEntity;
import com.mohmk10.audittrail.admin.domain.AdminAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLogEntity, UUID> {

    Page<AdminAuditLogEntity> findByTenantIdOrderByTimestampDesc(String tenantId, Pageable pageable);

    Page<AdminAuditLogEntity> findByActorIdOrderByTimestampDesc(UUID actorId, Pageable pageable);

    List<AdminAuditLogEntity> findByResourceTypeAndResourceIdOrderByTimestampDesc(
            String resourceType, String resourceId);

    @Query("SELECT a FROM AdminAuditLogEntity a WHERE a.tenantId = :tenantId " +
            "AND (:actorId IS NULL OR a.actorId = :actorId) " +
            "AND (:action IS NULL OR a.action = :action) " +
            "AND (:resourceType IS NULL OR a.resourceType = :resourceType) " +
            "AND (:from IS NULL OR a.timestamp >= :from) " +
            "AND (:to IS NULL OR a.timestamp <= :to) " +
            "ORDER BY a.timestamp DESC")
    Page<AdminAuditLogEntity> findByFilters(
            @Param("tenantId") String tenantId,
            @Param("actorId") UUID actorId,
            @Param("action") AdminAction action,
            @Param("resourceType") String resourceType,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );

    @Query("SELECT COUNT(a) FROM AdminAuditLogEntity a WHERE a.tenantId = :tenantId " +
            "AND a.timestamp >= :from AND a.timestamp <= :to")
    long countByTenantIdAndTimestampBetween(
            @Param("tenantId") String tenantId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query("SELECT a.action, COUNT(a) FROM AdminAuditLogEntity a " +
            "WHERE a.tenantId = :tenantId AND a.timestamp >= :from " +
            "GROUP BY a.action ORDER BY COUNT(a) DESC")
    List<Object[]> countByActionSince(@Param("tenantId") String tenantId, @Param("from") Instant from);
}
