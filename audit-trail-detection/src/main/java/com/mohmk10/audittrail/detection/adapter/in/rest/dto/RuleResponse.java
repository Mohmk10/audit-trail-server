package com.mohmk10.audittrail.detection.adapter.in.rest.dto;

import com.mohmk10.audittrail.detection.domain.RuleType;
import com.mohmk10.audittrail.detection.domain.Severity;

import java.time.Instant;
import java.util.UUID;

public record RuleResponse(
        UUID id,
        String name,
        String description,
        String tenantId,
        boolean enabled,
        RuleType type,
        RuleConditionDto condition,
        RuleActionDto action,
        Severity severity,
        Instant createdAt,
        Instant updatedAt
) {
}
