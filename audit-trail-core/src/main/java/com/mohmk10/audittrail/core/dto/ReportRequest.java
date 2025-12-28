package com.mohmk10.audittrail.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReportRequest(
        @NotBlank String templateId,
        @NotNull SearchCriteria criteria,
        @NotNull ReportFormat format
) {
    public enum ReportFormat {
        PDF, CSV, EXCEL, JSON
    }
}
