package com.mohmk10.audittrail.admin.adapter.in.rest.dto;

import com.mohmk10.audittrail.admin.domain.AdminAction;
import com.mohmk10.audittrail.admin.domain.AdminAuditLog;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AdminAuditLogResponse(
        UUID id,
        String tenantId,
        UUID actorId,
        String actorEmail,
        AdminAction action,
        String resourceType,
        String resourceId,
        Map<String, Object> previousState,
        Map<String, Object> newState,
        String ipAddress,
        String userAgent,
        Instant timestamp,
        String details
) {
    public static AdminAuditLogResponse from(AdminAuditLog log) {
        return new AdminAuditLogResponse(
                log.getId(),
                log.getTenantId(),
                log.getActorId(),
                log.getActorEmail(),
                log.getAction(),
                log.getResourceType(),
                log.getResourceId(),
                log.getPreviousState(),
                log.getNewState(),
                log.getIpAddress(),
                log.getUserAgent(),
                log.getTimestamp(),
                log.getDetails()
        );
    }
}
