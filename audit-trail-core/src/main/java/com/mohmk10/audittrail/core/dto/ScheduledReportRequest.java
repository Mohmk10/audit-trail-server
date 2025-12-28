package com.mohmk10.audittrail.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ScheduledReportRequest(
        @NotBlank String name,
        @NotNull ReportRequest reportRequest,
        @NotBlank String cronExpression,
        List<String> recipients,
        boolean enabled
) {
}
