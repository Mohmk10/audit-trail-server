package com.mohmk10.audittrail.admin.adapter.in.rest.dto;

import com.mohmk10.audittrail.admin.domain.ComponentHealth;
import com.mohmk10.audittrail.admin.domain.HealthStatus;

import java.time.Instant;
import java.util.Map;

public record ComponentHealthResponse(
        String name,
        HealthStatus status,
        String message,
        long responseTimeMs,
        Map<String, Object> details,
        Instant lastChecked
) {
    public static ComponentHealthResponse from(ComponentHealth component) {
        return new ComponentHealthResponse(
                component.getName(),
                component.getStatus(),
                component.getMessage(),
                component.getResponseTimeMs(),
                component.getDetails(),
                component.getLastChecked()
        );
    }
}
