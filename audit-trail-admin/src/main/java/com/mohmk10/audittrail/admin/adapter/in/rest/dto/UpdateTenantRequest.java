package com.mohmk10.audittrail.admin.adapter.in.rest.dto;

import com.mohmk10.audittrail.admin.domain.TenantPlan;

import java.util.Map;

public record UpdateTenantRequest(
        String name,
        String description,
        TenantPlan plan,
        TenantQuotaDto quota,
        Map<String, String> settings
) {
}
