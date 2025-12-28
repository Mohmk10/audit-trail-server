package com.mohmk10.audittrail.reporting.template;

import com.mohmk10.audittrail.core.domain.Action;
import com.mohmk10.audittrail.core.dto.SearchCriteria;
import com.mohmk10.audittrail.reporting.domain.ReportType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ComplianceReportTemplate implements ReportTemplate {

    @Override
    public ReportType getType() {
        return ReportType.COMPLIANCE;
    }

    @Override
    public String getName() {
        return "Compliance Report";
    }

    @Override
    public String getDescription() {
        return "Compliance-focused report showing CREATE, UPDATE, DELETE actions on sensitive resources with hash chain verification status";
    }

    @Override
    public List<String> getRequiredFields() {
        return List.of("tenantId", "dateRange");
    }

    @Override
    public List<String> getOptionalFields() {
        return List.of("resourceType");
    }

    @Override
    public SearchCriteria getDefaultCriteria(String tenantId) {
        return SearchCriteria.builder()
                .tenantId(tenantId)
                .actionTypes(List.of(
                        Action.ActionType.CREATE,
                        Action.ActionType.UPDATE,
                        Action.ActionType.DELETE
                ))
                .page(0)
                .size(10000)
                .sortBy("timestamp")
                .sortDirection(SearchCriteria.SortDirection.DESC)
                .build();
    }
}
