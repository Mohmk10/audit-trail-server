package com.mohmk10.audittrail.reporting.adapter.out.persistence;

import com.mohmk10.audittrail.reporting.domain.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface JpaReportRepository extends JpaRepository<ReportEntity, UUID> {

    List<ReportEntity> findByTenantIdOrderByCreatedAtDesc(String tenantId);

    List<ReportEntity> findByStatus(ReportStatus status);

    List<ReportEntity> findByExpiresAtBeforeAndStatusNot(Instant expiresAt, ReportStatus status);

    void deleteByTenantId(String tenantId);
}
