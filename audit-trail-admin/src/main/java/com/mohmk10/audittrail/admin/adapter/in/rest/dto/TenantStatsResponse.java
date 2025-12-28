package com.mohmk10.audittrail.admin.adapter.in.rest.dto;

import com.mohmk10.audittrail.admin.domain.TenantStats;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record TenantStatsResponse(
        UUID tenantId,
        String tenantName,
        long totalEvents,
        long eventsToday,
        long eventsThisMonth,
        long storageUsedBytes,
        int activeSources,
        int activeUsers,
        int activeApiKeys,
        int alertsTriggeredToday,
        Map<String, Long> eventsByType,
        Map<String, Long> eventsBySource,
        double averageEventsPerDay,
        Instant lastEventAt,
        Instant calculatedAt
) {
    public static TenantStatsResponse from(TenantStats stats) {
        return new TenantStatsResponse(
                stats.getTenantId(),
                stats.getTenantName(),
                stats.getTotalEvents(),
                stats.getEventsToday(),
                stats.getEventsThisMonth(),
                stats.getStorageUsedBytes(),
                stats.getActiveSources(),
                stats.getActiveUsers(),
                stats.getActiveApiKeys(),
                stats.getAlertsTriggeredToday(),
                stats.getEventsByType(),
                stats.getEventsBySource(),
                stats.getAverageEventsPerDay(),
                stats.getLastEventAt(),
                stats.getCalculatedAt()
        );
    }
}
