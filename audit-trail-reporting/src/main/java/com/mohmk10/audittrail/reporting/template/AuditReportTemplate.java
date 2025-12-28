package com.mohmk10.audittrail.reporting.template;

import com.mohmk10.audittrail.core.dto.SearchCriteria;
import com.mohmk10.audittrail.reporting.domain.ReportType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuditReportTemplate implements ReportTemplate {

    @Override
    public ReportType getType() {
        return ReportType.AUDIT;
    }

    @Override
    public String getName() {
        return "Audit Report";
    }

    @Override
    public String getDescription() {
        return "Complete audit trail report with all events in the specified period, grouped by date and actor";
    }

    @Override
    public List<String> getRequiredFields() {
        return List.of("tenantId", "dateRange");
    }

    @Override
    public List<String> getOptionalFields() {
        return List.of("actorId", "resourceType", "actionType");
    }

    @Override
    public SearchCriteria getDefaultCriteria(String tenantId) {
        return SearchCriteria.builder()
                .tenantId(tenantId)
                .page(0)
                .size(10000)
                .sortBy("timestamp")
                .sortDirection(SearchCriteria.SortDirection.DESC)
                .build();
    }
}
