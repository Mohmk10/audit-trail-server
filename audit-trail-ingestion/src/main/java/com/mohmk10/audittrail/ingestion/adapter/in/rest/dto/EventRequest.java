package com.mohmk10.audittrail.ingestion.adapter.in.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record EventRequest(
        @NotNull(message = "Actor is required") @Valid ActorRequest actor,
        @NotNull(message = "Action is required") @Valid ActionRequest action,
        @NotNull(message = "Resource is required") @Valid ResourceRequest resource,
        @Valid EventMetadataRequest metadata
) {
}
