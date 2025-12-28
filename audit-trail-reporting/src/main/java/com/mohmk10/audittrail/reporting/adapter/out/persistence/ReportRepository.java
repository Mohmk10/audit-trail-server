package com.mohmk10.audittrail.reporting.adapter.out.persistence;

import com.mohmk10.audittrail.reporting.domain.Report;
import com.mohmk10.audittrail.reporting.domain.ReportStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ReportRepository {

    private final JpaReportRepository jpaRepository;
    private final ReportMapper mapper;

    public ReportRepository(JpaReportRepository jpaRepository, ReportMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Transactional
    public Report save(Report report) {
        ReportEntity entity = mapper.toEntity(report);
        ReportEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    public Optional<Report> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    public List<Report> findByTenantId(String tenantId) {
        return jpaRepository.findByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    public List<Report> findExpired() {
        return jpaRepository.findByExpiresAtBeforeAndStatusNot(Instant.now(), ReportStatus.EXPIRED)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Transactional
    public void delete(Report report) {
        jpaRepository.deleteById(report.id());
    }

    @Transactional
    public void deleteByTenantId(String tenantId) {
        jpaRepository.deleteByTenantId(tenantId);
    }
}
