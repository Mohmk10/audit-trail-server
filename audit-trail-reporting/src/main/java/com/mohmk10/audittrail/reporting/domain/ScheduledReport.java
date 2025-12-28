package com.mohmk10.audittrail.reporting.domain;

import com.mohmk10.audittrail.core.dto.SearchCriteria;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ScheduledReport(
        UUID id,
        String name,
        ReportType type,
        ReportFormat format,
        String tenantId,
        SearchCriteria criteria,
        String cronExpression,
        boolean enabled,
        Instant lastRunAt,
        Instant nextRunAt,
        List<String> recipients,
        Instant createdAt
) {
    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .id(id)
                .name(name)
                .type(type)
                .format(format)
                .tenantId(tenantId)
                .criteria(criteria)
                .cronExpression(cronExpression)
                .enabled(enabled)
                .lastRunAt(lastRunAt)
                .nextRunAt(nextRunAt)
                .recipients(recipients)
                .createdAt(createdAt);
    }

    public static class Builder {
        private UUID id;
        private String name;
        private ReportType type;
        private ReportFormat format;
        private String tenantId;
        private SearchCriteria criteria;
        private String cronExpression;
        private boolean enabled = true;
        private Instant lastRunAt;
        private Instant nextRunAt;
        private List<String> recipients;
        private Instant createdAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

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

        public Builder criteria(SearchCriteria criteria) {
            this.criteria = criteria;
            return this;
        }

        public Builder cronExpression(String cronExpression) {
            this.cronExpression = cronExpression;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder lastRunAt(Instant lastRunAt) {
            this.lastRunAt = lastRunAt;
            return this;
        }

        public Builder nextRunAt(Instant nextRunAt) {
            this.nextRunAt = nextRunAt;
            return this;
        }

        public Builder recipients(List<String> recipients) {
            this.recipients = recipients;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ScheduledReport build() {
            return new ScheduledReport(id, name, type, format, tenantId, criteria,
                    cronExpression, enabled, lastRunAt, nextRunAt, recipients, createdAt);
        }
    }
}
