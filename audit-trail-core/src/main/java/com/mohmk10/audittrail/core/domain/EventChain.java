package com.mohmk10.audittrail.core.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record EventChain(
        String previousHash,
        @NotBlank String hash,
        String signature,
        @NotNull Instant chainedAt
) {
}
