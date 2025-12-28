package com.mohmk10.audittrail.admin.adapter.in.rest.dto;

import com.mohmk10.audittrail.admin.domain.TenantPlan;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record CreateTenantRequest(
        @NotBlank(message = "Name is required") String name,
        String slug,
        String description,
        TenantPlan plan,
        TenantQuotaDto quota,
        Map<String, String> settings
) {
}
