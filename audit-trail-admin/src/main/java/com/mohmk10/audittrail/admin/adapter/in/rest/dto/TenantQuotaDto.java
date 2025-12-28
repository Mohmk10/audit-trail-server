package com.mohmk10.audittrail.admin.adapter.in.rest.dto;

public record TenantQuotaDto(
        Long maxEventsPerDay,
        Long maxEventsPerMonth,
        Integer maxSources,
        Integer maxApiKeys,
        Integer maxUsers,
        Integer retentionDays
) {
}
