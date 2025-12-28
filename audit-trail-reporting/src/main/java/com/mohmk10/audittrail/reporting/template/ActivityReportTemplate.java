package com.mohmk10.audittrail.reporting.template;

import com.mohmk10.audittrail.core.dto.SearchCriteria;
import com.mohmk10.audittrail.reporting.domain.ReportType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ActivityReportTemplate implements ReportTemplate {

    @Override
    public ReportType getType() {
        return ReportType.ACTIVITY;
    }

    @Override
    public String getName() {
        return "Activity Report";
    }

    @Override
    public String getDescription() {
        return "Detailed activity report for a specific actor showing complete timeline of their actions";
    }

    @Override
    public List<String> getRequiredFields() {
        return List.of("tenantId", "actorId", "dateRange");
    }

    @Override
    public List<String> getOptionalFields() {
        return List.of("resourceType", "actionType");
    }

    @Override
    public SearchCriteria getDefaultCriteria(String tenantId) {
        return SearchCriteria.builder()
                .tenantId(tenantId)
                .page(0)
                .size(10000)
                .sortBy("timestamp")
                .sortDirection(SearchCriteria.SortDirection.ASC)
                .build();
    }
}
