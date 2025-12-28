package com.mohmk10.audittrail.admin.adapter.out.persistence.mapper;

import com.mohmk10.audittrail.admin.adapter.out.persistence.entity.AdminAuditLogEntity;
import com.mohmk10.audittrail.admin.domain.AdminAuditLog;
import org.springframework.stereotype.Component;

@Component
public class AdminAuditLogMapper {

    public AdminAuditLog toDomain(AdminAuditLogEntity entity) {
        if (entity == null) {
            return null;
        }

        return AdminAuditLog.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .actorId(entity.getActorId())
                .actorEmail(entity.getActorEmail())
                .action(entity.getAction())
                .resourceType(entity.getResourceType())
                .resourceId(entity.getResourceId())
                .previousState(entity.getPreviousState())
                .newState(entity.getNewState())
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .timestamp(entity.getTimestamp())
                .details(entity.getDetails())
                .build();
    }

    public AdminAuditLogEntity toEntity(AdminAuditLog domain) {
        if (domain == null) {
            return null;
        }

        AdminAuditLogEntity entity = new AdminAuditLogEntity();
        entity.setId(domain.getId());
        entity.setTenantId(domain.getTenantId());
        entity.setActorId(domain.getActorId());
        entity.setActorEmail(domain.getActorEmail());
        entity.setAction(domain.getAction());
        entity.setResourceType(domain.getResourceType());
        entity.setResourceId(domain.getResourceId());
        entity.setPreviousState(domain.getPreviousState());
        entity.setNewState(domain.getNewState());
        entity.setIpAddress(domain.getIpAddress());
        entity.setUserAgent(domain.getUserAgent());
        entity.setTimestamp(domain.getTimestamp());
        entity.setDetails(domain.getDetails());
        return entity;
    }
}
