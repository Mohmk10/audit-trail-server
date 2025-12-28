package com.mohmk10.audittrail.detection.adapter.in.rest.dto;

import com.mohmk10.audittrail.detection.domain.Severity;

public record UpdateRuleRequest(
        String name,
        String description,
        Boolean enabled,
        RuleConditionDto condition,
        RuleActionDto action,
        Severity severity
) {
}
