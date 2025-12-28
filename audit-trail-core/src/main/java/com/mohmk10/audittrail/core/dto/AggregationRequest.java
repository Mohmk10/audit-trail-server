package com.mohmk10.audittrail.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AggregationRequest(
        @NotNull SearchCriteria baseCriteria,
        @NotBlank String groupByField,
        @NotNull AggregationType aggregationType,
        List<String> additionalGroupByFields
) {
    public enum AggregationType {
        COUNT, SUM, AVG, MIN, MAX, CARDINALITY
    }
}
