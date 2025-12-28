package com.mohmk10.audittrail.detection.adapter.in.rest.dto;

import com.mohmk10.audittrail.detection.domain.Severity;

import java.util.Map;

public record AlertStatsResponse(
        long totalOpen,
        long totalAcknowledged,
        long totalResolved,
        long totalDismissed,
        Map<Severity, Long> bySeverity
) {
}
