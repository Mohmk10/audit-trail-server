package com.mohmk10.audittrail.admin.adapter.in.rest.dto;

import com.mohmk10.audittrail.admin.domain.TenantPlan;
import com.mohmk10.audittrail.admin.domain.TenantStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record TenantResponse(
        UUID id,
        String name,
        String slug,
        String description,
        TenantStatus status,
        TenantPlan plan,
        TenantQuotaDto quota,
        Map<String, String> settings,
        Instant createdAt,
        Instant updatedAt
) {
}
