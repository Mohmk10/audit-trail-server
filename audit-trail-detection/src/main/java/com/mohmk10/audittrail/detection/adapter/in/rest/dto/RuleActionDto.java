package com.mohmk10.audittrail.detection.adapter.in.rest.dto;

import com.mohmk10.audittrail.detection.domain.AlertType;

import java.util.List;
import java.util.Map;

public record RuleActionDto(
        AlertType alertType,
        List<String> notificationChannels,
        Map<String, String> parameters
) {
}
