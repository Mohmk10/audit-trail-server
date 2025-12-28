package com.mohmk10.audittrail.search.adapter.in.rest.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;

public record AggregationRequestDto(
        @NotBlank String tenantId,
        @NotBlank String groupByField,
        String aggregationType,
        Instant fromDate,
        Instant toDate
) {
    public AggregationRequestDto {
        if (aggregationType == null || aggregationType.isBlank()) {
            aggregationType = "COUNT";
        }
    }
}
