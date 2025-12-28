package com.mohmk10.audittrail.reporting.adapter.in.rest.dto;

import com.mohmk10.audittrail.reporting.domain.Report;

import java.time.Instant;
import java.util.UUID;

public record ReportResponse(
        UUID id,
        String name,
        String type,
        String format,
        String status,
        String tenantId,
        Instant generatedAt,
        Instant expiresAt,
        long fileSize,
        String checksum,
        Instant createdAt,
        String errorMessage
) {
    public static ReportResponse from(Report report) {
        return new ReportResponse(
                report.id(),
                report.name(),
                report.type().name(),
                report.format().name(),
                report.status().name(),
                report.tenantId(),
                report.generatedAt(),
                report.expiresAt(),
                report.fileSize(),
                report.checksum(),
                report.createdAt(),
                report.errorMessage()
        );
    }
}
