package com.mohmk10.audittrail.ingestion.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record ActorRequest(
        @NotBlank(message = "Actor ID is required") String id,
        @NotBlank(message = "Actor type is required") String type,
        String name,
        String ip,
        String userAgent,
        Map<String, String> attributes
) {
}
