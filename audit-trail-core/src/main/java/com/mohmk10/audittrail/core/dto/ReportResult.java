package com.mohmk10.audittrail.core.dto;

import java.time.Instant;

public record ReportResult(
        String reportId,
        String fileName,
        byte[] content,
        ReportRequest.ReportFormat format,
        Instant generatedAt,
        long recordCount
) {
}
