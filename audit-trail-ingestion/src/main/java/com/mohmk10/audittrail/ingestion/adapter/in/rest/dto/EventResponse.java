package com.mohmk10.audittrail.ingestion.adapter.in.rest.dto;

import java.time.Instant;
import java.util.UUID;

public record EventResponse(
        UUID id,
        Instant timestamp,
        String hash,
        String status
) {
}
