package com.mohmk10.audittrail.admin.adapter.in.rest.dto;

import com.mohmk10.audittrail.admin.domain.ApiKeyScope;
import com.mohmk10.audittrail.admin.domain.ApiKeyStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ApiKeyResponse(
        UUID id,
        String tenantId,
        UUID sourceId,
        String name,
        String keyPrefix,
        Set<ApiKeyScope> scopes,
        ApiKeyStatus status,
        Instant createdAt,
        Instant expiresAt,
        Instant lastUsedAt,
        String lastUsedIp
) {
}
