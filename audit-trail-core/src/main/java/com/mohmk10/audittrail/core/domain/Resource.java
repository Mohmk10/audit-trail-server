package com.mohmk10.audittrail.core.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record Resource(
        @NotBlank String id,
        @NotNull ResourceType type,
        @NotBlank String name,
        Map<String, Object> before,
        Map<String, Object> after
) {
    public enum ResourceType {
        DOCUMENT, USER, TRANSACTION, CONFIG, FILE, API, DATABASE, SYSTEM
    }
}
