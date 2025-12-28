package com.mohmk10.audittrail.ingestion.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record ActionRequest(
        @NotBlank(message = "Action type is required") String type,
        String description,
        String category
) {
}
