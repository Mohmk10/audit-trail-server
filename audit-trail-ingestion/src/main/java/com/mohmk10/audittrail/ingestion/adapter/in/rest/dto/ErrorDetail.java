package com.mohmk10.audittrail.ingestion.adapter.in.rest.dto;

import java.util.List;

public record ErrorDetail(
        int index,
        String message,
        List<String> violations
) {
}
