package com.mohmk10.audittrail.admin.adapter.in.rest.dto;

import com.mohmk10.audittrail.admin.domain.ApiKeyScope;
import com.mohmk10.audittrail.admin.domain.ApiKeyStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record RotateApiKeyResponse(
        UUID oldKeyId,
        UUID newKeyId,
        String tenantId,
        UUID sourceId,
        String name,
        String newKey,
        String newKeyPrefix,
        Set<ApiKeyScope> scopes,
        ApiKeyStatus status,
        Instant createdAt,
        Instant expiresAt,
        String warning
) {
    public static RotateApiKeyResponse from(
            UUID oldKeyId,
            UUID newKeyId,
            String tenantId,
            UUID sourceId,
            String name,
            String newKey,
            String newKeyPrefix,
            Set<ApiKeyScope> scopes,
            ApiKeyStatus status,
            Instant createdAt,
            Instant expiresAt
    ) {
        return new RotateApiKeyResponse(
                oldKeyId, newKeyId, tenantId, sourceId, name, newKey, newKeyPrefix,
                scopes, status, createdAt, expiresAt,
                "Old key has been revoked. This is the only time you will see the new key."
        );
    }
}
