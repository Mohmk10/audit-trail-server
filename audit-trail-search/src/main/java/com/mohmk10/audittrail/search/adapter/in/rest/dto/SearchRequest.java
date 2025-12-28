package com.mohmk10.audittrail.search.adapter.in.rest.dto;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SearchRequest(
        @NotBlank String tenantId,
        String actorId,
        String actorType,
        String actionType,
        String actionCategory,
        String resourceId,
        String resourceType,
        String query,
        Instant fromDate,
        Instant toDate,
        List<String> tags,
        @Min(0) int page,
        @Min(1) @Max(100) int size,
        String sortBy,
        String sortOrder
) {
    public SearchRequest {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (size > 100) size = 100;
    }
}
