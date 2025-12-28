package com.mohmk10.audittrail.admin.adapter.in.rest.dto;

import com.mohmk10.audittrail.admin.domain.ApiKeyScope;
import com.mohmk10.audittrail.admin.domain.ApiKeyStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ApiKeyCreatedResponse(
        UUID id,
        String tenantId,
        UUID sourceId,
        String name,
        String key,
        String keyPrefix,
        Set<ApiKeyScope> scopes,
        ApiKeyStatus status,
        Instant createdAt,
        Instant expiresAt,
        String warning
) {
    public static ApiKeyCreatedResponse from(
            UUID id,
            String tenantId,
            UUID sourceId,
            String name,
            String key,
            String keyPrefix,
            Set<ApiKeyScope> scopes,
            ApiKeyStatus status,
            Instant createdAt,
            Instant expiresAt
    ) {
        return new ApiKeyCreatedResponse(
                id, tenantId, sourceId, name, key, keyPrefix, scopes, status, createdAt, expiresAt,
                "This is the only time you will see this key. Please store it securely."
        );
    }
}
