package com.mohmk10.audittrail.ingestion.adapter.in.rest.dto;

import java.util.List;

public record BatchEventResponse(
        int total,
        int succeeded,
        int failed,
        List<EventResponse> events,
        List<ErrorDetail> errors
) {
}
