package com.mohmk10.audittrail.reporting.template;

import com.mohmk10.audittrail.core.dto.SearchCriteria;
import com.mohmk10.audittrail.reporting.domain.ReportType;

import java.util.List;

public interface ReportTemplate {
    ReportType getType();
    String getName();
    String getDescription();
    List<String> getRequiredFields();
    List<String> getOptionalFields();
    SearchCriteria getDefaultCriteria(String tenantId);
}
