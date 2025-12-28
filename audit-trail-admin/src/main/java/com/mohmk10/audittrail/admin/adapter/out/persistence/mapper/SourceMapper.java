package com.mohmk10.audittrail.admin.adapter.out.persistence.mapper;

import com.mohmk10.audittrail.admin.adapter.out.persistence.entity.SourceEntity;
import com.mohmk10.audittrail.admin.domain.Source;
import org.springframework.stereotype.Component;

@Component
public class SourceMapper {

    public Source toDomain(SourceEntity entity) {
        if (entity == null) {
            return null;
        }

        return Source.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .description(entity.getDescription())
                .type(entity.getType())
                .status(entity.getStatus())
                .config(entity.getConfig())
                .createdAt(entity.getCreatedAt())
                .lastEventAt(entity.getLastEventAt())
                .eventCount(entity.getEventCount())
                .build();
    }

    public SourceEntity toEntity(Source domain) {
        if (domain == null) {
            return null;
        }

        SourceEntity entity = new SourceEntity();
        entity.setId(domain.getId());
        entity.setTenantId(domain.getTenantId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setType(domain.getType());
        entity.setStatus(domain.getStatus());
        entity.setConfig(domain.getConfig());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setLastEventAt(domain.getLastEventAt());
        entity.setEventCount(domain.getEventCount());
        return entity;
    }
}
