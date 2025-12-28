package com.mohmk10.audittrail.detection.adapter.in.rest.dto;

import com.mohmk10.audittrail.detection.domain.RuleType;
import com.mohmk10.audittrail.detection.domain.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRuleRequest(
        @NotBlank String name,
        String description,
        @NotBlank String tenantId,
        @NotNull RuleType type,
        @NotNull RuleConditionDto condition,
        @NotNull RuleActionDto action,
        @NotNull Severity severity
) {
}
