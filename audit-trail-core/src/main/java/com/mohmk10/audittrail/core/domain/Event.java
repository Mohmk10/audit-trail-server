package com.mohmk10.audittrail.core.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record Event(
        @NotNull UUID id,
        @NotNull Instant timestamp,
        @NotNull @Valid Actor actor,
        @NotNull @Valid Action action,
        @NotNull @Valid Resource resource,
        @Valid EventMetadata metadata,
        String previousHash,
        String hash,
        String signature
) {
}
