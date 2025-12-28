package com.mohmk10.audittrail.ingestion.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record EventMetadataRequest(
        @NotBlank(message = "Source is required") String source,
        @NotBlank(message = "Tenant ID is required") String tenantId,
        String correlationId,
        String sessionId,
        Map<String, String> tags,
        Map<String, Object> extra
) {
}
