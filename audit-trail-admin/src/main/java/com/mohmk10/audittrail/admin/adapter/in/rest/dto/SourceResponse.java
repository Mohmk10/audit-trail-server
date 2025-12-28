package com.mohmk10.audittrail.admin.adapter.in.rest.dto;

import com.mohmk10.audittrail.admin.domain.SourceStatus;
import com.mohmk10.audittrail.admin.domain.SourceType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record SourceResponse(
        UUID id,
        String tenantId,
        String name,
        String description,
        SourceType type,
        SourceStatus status,
        Map<String, String> config,
        Instant createdAt,
        Instant lastEventAt,
        long eventCount
) {
}
