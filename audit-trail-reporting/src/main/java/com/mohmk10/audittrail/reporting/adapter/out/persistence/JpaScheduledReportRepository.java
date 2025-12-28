package com.mohmk10.audittrail.reporting.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaScheduledReportRepository extends JpaRepository<ScheduledReportEntity, UUID> {

    List<ScheduledReportEntity> findByTenantIdOrderByCreatedAtDesc(String tenantId);

    List<ScheduledReportEntity> findByEnabled(boolean enabled);

    void deleteByTenantId(String tenantId);
}
