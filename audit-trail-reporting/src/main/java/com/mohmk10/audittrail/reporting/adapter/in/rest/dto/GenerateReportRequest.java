package com.mohmk10.audittrail.reporting.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

public record GenerateReportRequest(
        String name,
        @NotNull String type,
        @NotNull String format,
        @NotBlank String tenantId,
        String actorId,
        String actionType,
        String resourceType,
        Instant fromDate,
        Instant toDate,
        Map<String, Object> parameters
) {
}
