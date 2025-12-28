package com.mohmk10.audittrail.admin.adapter.in.rest.dto;

import com.mohmk10.audittrail.admin.domain.ApiKeyScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record CreateApiKeyRequest(
        @NotBlank(message = "Tenant ID is required") String tenantId,
        UUID sourceId,
        @NotBlank(message = "Name is required") String name,
        @NotEmpty(message = "At least one scope is required") Set<ApiKeyScope> scopes,
        Instant expiresAt
) {
}
