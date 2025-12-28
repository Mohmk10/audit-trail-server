package com.mohmk10.audittrail.core.dto;

import com.mohmk10.audittrail.core.domain.Event;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record Alert(
        @NotNull UUID id,
        @NotBlank String type,
        @NotNull Severity severity,
        @NotBlank String message,
        Event event,
        @NotNull Instant timestamp
) {
    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
