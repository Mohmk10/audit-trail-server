package com.mohmk10.audittrail.admin.application;

import com.mohmk10.audittrail.admin.domain.AdminAction;
import com.mohmk10.audittrail.admin.domain.AdminAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AdminAuditService {

    AdminAuditLog log(AdminAuditLog auditLog);

    AdminAuditLog log(String tenantId, UUID actorId, String actorEmail, AdminAction action,
                      String resourceType, String resourceId, Map<String, Object> previousState,
                      Map<String, Object> newState, String details);

    Page<AdminAuditLog> findByTenantId(String tenantId, Pageable pageable);

    Page<AdminAuditLog> findByFilters(String tenantId, UUID actorId, AdminAction action,
                                       String resourceType, Instant from, Instant to, Pageable pageable);

    List<AdminAuditLog> findByResource(String resourceType, String resourceId);

    long countByTenantIdToday(String tenantId);

    Map<AdminAction, Long> getActionStatsSince(String tenantId, Instant since);
}
