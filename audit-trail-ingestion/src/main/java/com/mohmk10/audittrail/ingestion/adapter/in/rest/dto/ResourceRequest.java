package com.mohmk10.audittrail.ingestion.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record ResourceRequest(
        @NotBlank(message = "Resource ID is required") String id,
        @NotBlank(message = "Resource type is required") String type,
        String name,
        Map<String, Object> before,
        Map<String, Object> after
) {
}
