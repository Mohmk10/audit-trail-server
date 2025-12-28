package com.mohmk10.audittrail.reporting.service;

import com.mohmk10.audittrail.core.dto.DateRange;
import com.mohmk10.audittrail.reporting.domain.ReportFormat;
import com.mohmk10.audittrail.reporting.domain.ReportType;

import java.util.Map;

public record ReportRequest(
        String name,
        ReportType type,
        ReportFormat format,
        String tenantId,
        String actorId,
        String actionType,
        String resourceType,
        DateRange dateRange,
        Map<String, Object> parameters
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private ReportType type;
        private ReportFormat format;
        private String tenantId;
        private String actorId;
        private String actionType;
        private String resourceType;
        private DateRange dateRange;
        private Map<String, Object> parameters;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(ReportType type) {
            this.type = type;
            return this;
        }

        public Builder format(ReportFormat format) {
            this.format = format;
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder actorId(String actorId) {
            this.actorId = actorId;
            return this;
        }

        public Builder actionType(String actionType) {
            this.actionType = actionType;
            return this;
        }

        public Builder resourceType(String resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public Builder dateRange(DateRange dateRange) {
            this.dateRange = dateRange;
            return this;
        }

        public Builder parameters(Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }

        public ReportRequest build() {
            return new ReportRequest(name, type, format, tenantId, actorId,
                    actionType, resourceType, dateRange, parameters);
        }
    }
}
