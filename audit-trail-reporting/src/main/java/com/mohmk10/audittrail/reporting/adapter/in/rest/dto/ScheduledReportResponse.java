package com.mohmk10.audittrail.reporting.adapter.in.rest.dto;

import com.mohmk10.audittrail.reporting.domain.ScheduledReport;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ScheduledReportResponse(
        UUID id,
        String name,
        String type,
        String format,
        String tenantId,
        String cronExpression,
        boolean enabled,
        Instant lastRunAt,
        Instant nextRunAt,
        List<String> recipients,
        Instant createdAt
) {
    public static ScheduledReportResponse from(ScheduledReport report) {
        return new ScheduledReportResponse(
                report.id(),
                report.name(),
                report.type().name(),
                report.format().name(),
                report.tenantId(),
                report.cronExpression(),
                report.enabled(),
                report.lastRunAt(),
                report.nextRunAt(),
                report.recipients(),
                report.createdAt()
        );
    }
}
