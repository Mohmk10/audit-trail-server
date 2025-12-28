package com.mohmk10.audittrail.core.dto;

import java.util.List;
import java.util.Map;

public record AggregationResult(
        List<Bucket> buckets,
        long totalDocuments
) {
    public record Bucket(
            String key,
            long count,
            Map<String, Object> additionalMetrics
    ) {
    }
}
