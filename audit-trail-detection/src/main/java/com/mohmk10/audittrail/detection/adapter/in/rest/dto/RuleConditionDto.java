package com.mohmk10.audittrail.detection.adapter.in.rest.dto;

import java.util.List;

public record RuleConditionDto(
        String field,
        String operator,
        Object value,
        Integer threshold,
        Integer windowMinutes,
        List<RuleConditionDto> and,
        List<RuleConditionDto> or
) {
}
