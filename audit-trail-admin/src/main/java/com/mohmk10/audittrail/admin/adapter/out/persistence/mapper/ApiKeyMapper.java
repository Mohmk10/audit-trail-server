package com.mohmk10.audittrail.admin.adapter.out.persistence.mapper;

import com.mohmk10.audittrail.admin.adapter.out.persistence.entity.ApiKeyEntity;
import com.mohmk10.audittrail.admin.domain.ApiKey;
import com.mohmk10.audittrail.admin.domain.ApiKeyScope;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ApiKeyMapper {

    public ApiKey toDomain(ApiKeyEntity entity) {
        if (entity == null) {
            return null;
        }

        Set<ApiKeyScope> scopes = entity.getScopes() != null
                ? entity.getScopes().stream()
                        .map(ApiKeyScope::valueOf)
                        .collect(Collectors.toSet())
                : Set.of();

        return ApiKey.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .sourceId(entity.getSourceId())
                .name(entity.getName())
                .keyHash(entity.getKeyHash())
                .keyPrefix(entity.getKeyPrefix())
                .scopes(scopes)
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .expiresAt(entity.getExpiresAt())
                .lastUsedAt(entity.getLastUsedAt())
                .lastUsedIp(entity.getLastUsedIp())
                .build();
    }

    public ApiKeyEntity toEntity(ApiKey domain) {
        if (domain == null) {
            return null;
        }

        Set<String> scopes = domain.getScopes() != null
                ? domain.getScopes().stream()
                        .map(Enum::name)
                        .collect(Collectors.toSet())
                : Set.of();

        ApiKeyEntity entity = new ApiKeyEntity();
        entity.setId(domain.getId());
        entity.setTenantId(domain.getTenantId());
        entity.setSourceId(domain.getSourceId());
        entity.setName(domain.getName());
        entity.setKeyHash(domain.getKeyHash());
        entity.setKeyPrefix(domain.getKeyPrefix());
        entity.setScopes(scopes);
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setLastUsedAt(domain.getLastUsedAt());
        entity.setLastUsedIp(domain.getLastUsedIp());
        return entity;
    }
}
