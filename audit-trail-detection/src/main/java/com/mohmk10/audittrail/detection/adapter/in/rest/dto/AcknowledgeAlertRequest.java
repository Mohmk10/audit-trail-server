package com.mohmk10.audittrail.detection.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record AcknowledgeAlertRequest(
        @NotBlank String acknowledgedBy
) {
}
