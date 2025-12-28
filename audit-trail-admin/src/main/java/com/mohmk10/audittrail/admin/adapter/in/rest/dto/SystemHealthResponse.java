package com.mohmk10.audittrail.admin.adapter.in.rest.dto;

import com.mohmk10.audittrail.admin.domain.HealthStatus;
import com.mohmk10.audittrail.admin.domain.SystemHealth;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record SystemHealthResponse(
        HealthStatus status,
        List<ComponentHealthResponse> components,
        String version,
        Instant startTime,
        long uptimeSeconds,
        Map<String, Object> metrics,
        Instant timestamp
) {
    public static SystemHealthResponse from(SystemHealth health) {
        List<ComponentHealthResponse> componentResponses = health.getComponents() != null
                ? health.getComponents().stream()
                    .map(ComponentHealthResponse::from)
                    .toList()
                : List.of();

        return new SystemHealthResponse(
                health.getOverallStatus(),
                componentResponses,
                health.getVersion(),
                health.getStartTime(),
                health.getUptimeSeconds(),
                health.getMetrics(),
                health.getTimestamp()
        );
    }
}
