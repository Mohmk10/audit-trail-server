package com.mohmk10.audittrail.core.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record DateRange(
        @NotNull Instant from,
        @NotNull Instant to
) {
}
