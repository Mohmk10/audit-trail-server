package com.mohmk10.audittrail.reporting.template;

import com.mohmk10.audittrail.core.domain.Action;
import com.mohmk10.audittrail.core.dto.SearchCriteria;
import com.mohmk10.audittrail.reporting.domain.ReportType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SecurityReportTemplate implements ReportTemplate {

    @Override
    public ReportType getType() {
        return ReportType.SECURITY;
    }

    @Override
    public String getName() {
        return "Security Report";
    }

    @Override
    public String getDescription() {
        return "Security-focused report showing LOGIN, LOGOUT, FAILED_LOGIN, and PERMISSION_CHANGE events with IP addresses and detected anomalies";
    }

    @Override
    public List<String> getRequiredFields() {
        return List.of("tenantId", "dateRange");
    }

    @Override
    public List<String> getOptionalFields() {
        return List.of("actorId", "actorIp");
    }

    @Override
    public SearchCriteria getDefaultCriteria(String tenantId) {
        return SearchCriteria.builder()
                .tenantId(tenantId)
                .actionTypes(List.of(
                        Action.ActionType.LOGIN,
                        Action.ActionType.LOGOUT,
                        Action.ActionType.REJECT,
                        Action.ActionType.APPROVE
                ))
                .page(0)
                .size(10000)
                .sortBy("timestamp")
                .sortDirection(SearchCriteria.SortDirection.DESC)
                .build();
    }
}
