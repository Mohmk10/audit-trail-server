package com.mohmk10.audittrail.reporting.adapter.in.rest.dto;

import com.mohmk10.audittrail.reporting.template.ReportTemplate;

import java.util.List;

public record ReportTemplateResponse(
        String type,
        String name,
        String description,
        List<String> requiredFields,
        List<String> optionalFields
) {
    public static ReportTemplateResponse from(ReportTemplate template) {
        return new ReportTemplateResponse(
                template.getType().name(),
                template.getName(),
                template.getDescription(),
                template.getRequiredFields(),
                template.getOptionalFields()
        );
    }
}
