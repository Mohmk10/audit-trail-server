package com.mohmk10.audittrail.ingestion.adapter.in.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record BatchEventRequest(
        @NotEmpty(message = "Events list cannot be empty")
        @Size(max = 1000, message = "Maximum 1000 events per batch")
        List<@Valid EventRequest> events
) {
}
