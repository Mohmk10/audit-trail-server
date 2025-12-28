package com.mohmk10.audittrail.reporting.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ScheduleReportRequest(
        @NotBlank String name,
        @NotNull String type,
        @NotNull String format,
        @NotBlank String tenantId,
        String actorId,
        String actionType,
        String resourceType,
        @NotBlank String cronExpression,
        List<String> recipients
) {
}
