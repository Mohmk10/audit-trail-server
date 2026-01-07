package com.mohmk10.audittrail.storage.adapter.out.persistence.repository;

import com.mohmk10.audittrail.storage.adapter.out.persistence.entity.EventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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

    // Search methods for PostgreSQL fallback
    Page<EventEntity> findByTenantIdAndActorIdOrderByTimestampDesc(
            String tenantId, String actorId, Pageable pageable);

    Page<EventEntity> findByTenantIdAndActionTypeOrderByTimestampDesc(
            String tenantId, String actionType, Pageable pageable);

    Page<EventEntity> findByTenantIdAndResourceTypeOrderByTimestampDesc(
            String tenantId, String resourceType, Pageable pageable);

    Page<EventEntity> findByTenantIdAndActorIdAndActionTypeOrderByTimestampDesc(
            String tenantId, String actorId, String actionType, Pageable pageable);

    @Query("SELECT e FROM EventEntity e WHERE e.tenantId = :tenantId " +
           "AND e.timestamp >= :from AND e.timestamp <= :to " +
           "ORDER BY e.timestamp DESC")
    Page<EventEntity> findByTenantIdAndTimestampBetween(
            @Param("tenantId") String tenantId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable);

    @Query("SELECT e FROM EventEntity e WHERE e.tenantId = :tenantId " +
           "AND (LOWER(e.actorName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(e.actionDescription) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(e.resourceName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY e.timestamp DESC")
    Page<EventEntity> quickSearch(
            @Param("tenantId") String tenantId,
            @Param("query") String query,
            Pageable pageable);

    @Query("SELECT e.actionType, COUNT(e) FROM EventEntity e " +
           "WHERE e.tenantId = :tenantId " +
           "AND (:from IS NULL OR e.timestamp >= :from) " +
           "AND (:to IS NULL OR e.timestamp <= :to) " +
           "GROUP BY e.actionType")
    List<Object[]> countByActionType(
            @Param("tenantId") String tenantId,
            @Param("from") Instant from,
            @Param("to") Instant to);

    @Query("SELECT e.resourceType, COUNT(e) FROM EventEntity e " +
           "WHERE e.tenantId = :tenantId " +
           "AND (:from IS NULL OR e.timestamp >= :from) " +
           "AND (:to IS NULL OR e.timestamp <= :to) " +
           "GROUP BY e.resourceType")
    List<Object[]> countByResourceType(
            @Param("tenantId") String tenantId,
            @Param("from") Instant from,
            @Param("to") Instant to);

    @Query("SELECT e.actorId, COUNT(e) FROM EventEntity e " +
           "WHERE e.tenantId = :tenantId " +
           "AND (:from IS NULL OR e.timestamp >= :from) " +
           "AND (:to IS NULL OR e.timestamp <= :to) " +
           "GROUP BY e.actorId")
    List<Object[]> countByActorId(
            @Param("tenantId") String tenantId,
            @Param("from") Instant from,
            @Param("to") Instant to);
}
