package com.mohmk10.audittrail.core.dto;

import com.mohmk10.audittrail.core.domain.Event;
import java.util.List;

public record AnomalyResult(
        boolean anomalyDetected,
        double confidenceScore,
        String anomalyType,
        String description,
        Event event,
        List<String> matchedRules
) {
}
