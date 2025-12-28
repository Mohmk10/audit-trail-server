package com.mohmk10.audittrail.detection.adapter.in.rest.dto;

import com.mohmk10.audittrail.detection.domain.AlertStatus;
import com.mohmk10.audittrail.detection.domain.Severity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AlertResponse(
        UUID id,
        String tenantId,
        UUID ruleId,
        String ruleName,
        Severity severity,
        AlertStatus status,
        String message,
        List<UUID> triggeringEventIds,
        Instant triggeredAt,
        Instant acknowledgedAt,
        String acknowledgedBy,
        String resolution,
        Instant resolvedAt
) {
}
