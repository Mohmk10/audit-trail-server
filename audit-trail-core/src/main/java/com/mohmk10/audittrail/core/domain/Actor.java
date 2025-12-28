package com.mohmk10.audittrail.core.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record Actor(
        @NotBlank String id,
        @NotNull ActorType type,
        @NotBlank String name,
        String ip,
        String userAgent,
        Map<String, String> attributes
) {
    public enum ActorType {
        USER, SYSTEM, SERVICE
    }
}
