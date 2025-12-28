package com.mohmk10.audittrail.admin.adapter.in.rest.dto;

import com.mohmk10.audittrail.admin.domain.SourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record CreateSourceRequest(
        @NotBlank(message = "Tenant ID is required") String tenantId,
        @NotBlank(message = "Name is required") String name,
        String description,
        @NotNull(message = "Type is required") SourceType type,
        Map<String, String> config
) {
}
