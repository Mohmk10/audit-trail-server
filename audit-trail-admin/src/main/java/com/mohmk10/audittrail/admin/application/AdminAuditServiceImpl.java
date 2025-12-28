package com.mohmk10.audittrail.admin.application;

import com.mohmk10.audittrail.admin.adapter.out.persistence.entity.AdminAuditLogEntity;
import com.mohmk10.audittrail.admin.adapter.out.persistence.mapper.AdminAuditLogMapper;
import com.mohmk10.audittrail.admin.adapter.out.persistence.repository.AdminAuditLogRepository;
import com.mohmk10.audittrail.admin.domain.AdminAction;
import com.mohmk10.audittrail.admin.domain.AdminAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminAuditServiceImpl implements AdminAuditService {

    private final AdminAuditLogRepository repository;
    private final AdminAuditLogMapper mapper;

    public AdminAuditServiceImpl(AdminAuditLogRepository repository, AdminAuditLogMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public AdminAuditLog log(AdminAuditLog auditLog) {
        AdminAuditLogEntity entity = mapper.toEntity(auditLog);
        AdminAuditLogEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public AdminAuditLog log(String tenantId, UUID actorId, String actorEmail, AdminAction action,
                             String resourceType, String resourceId, Map<String, Object> previousState,
                             Map<String, Object> newState, String details) {
        AdminAuditLog auditLog = AdminAuditLog.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .actorId(actorId)
                .actorEmail(actorEmail)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .previousState(previousState)
                .newState(newState)
                .details(details)
                .timestamp(Instant.now())
                .build();

        return log(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminAuditLog> findByTenantId(String tenantId, Pageable pageable) {
        return repository.findByTenantIdOrderByTimestampDesc(tenantId, pageable)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminAuditLog> findByFilters(String tenantId, UUID actorId, AdminAction action,
                                              String resourceType, Instant from, Instant to, Pageable pageable) {
        return repository.findByFilters(tenantId, actorId, action, resourceType, from, to, pageable)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminAuditLog> findByResource(String resourceType, String resourceId) {
        return repository.findByResourceTypeAndResourceIdOrderByTimestampDesc(resourceType, resourceId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByTenantIdToday(String tenantId) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Instant startOfDay = today.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endOfDay = today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return repository.countByTenantIdAndTimestampBetween(tenantId, startOfDay, endOfDay);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<AdminAction, Long> getActionStatsSince(String tenantId, Instant since) {
        List<Object[]> results = repository.countByActionSince(tenantId, since);
        Map<AdminAction, Long> stats = new HashMap<>();
        for (Object[] row : results) {
            AdminAction action = (AdminAction) row[0];
            Long count = (Long) row[1];
            stats.put(action, count);
        }
        return stats;
    }
}
