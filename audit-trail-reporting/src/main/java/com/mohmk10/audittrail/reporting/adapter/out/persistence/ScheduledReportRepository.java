package com.mohmk10.audittrail.reporting.adapter.out.persistence;

import com.mohmk10.audittrail.reporting.domain.ScheduledReport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ScheduledReportRepository {

    private final JpaScheduledReportRepository jpaRepository;
    private final ReportMapper mapper;

    public ScheduledReportRepository(JpaScheduledReportRepository jpaRepository, ReportMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Transactional
    public ScheduledReport save(ScheduledReport report) {
        ScheduledReportEntity entity = mapper.toEntity(report);
        ScheduledReportEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    public Optional<ScheduledReport> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    public List<ScheduledReport> findByTenantId(String tenantId) {
        return jpaRepository.findByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    public List<ScheduledReport> findEnabled() {
        return jpaRepository.findByEnabled(true)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Transactional
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Transactional
    public void deleteByTenantId(String tenantId) {
        jpaRepository.deleteByTenantId(tenantId);
    }
}
