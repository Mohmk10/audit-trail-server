package com.mohmk10.audittrail.core.domain;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record EventMetadata(
        @NotBlank String source,
        @NotBlank String tenantId,
        String correlationId,
        String sessionId,
        Map<String, String> tags,
        Map<String, Object> extra
) {
}
