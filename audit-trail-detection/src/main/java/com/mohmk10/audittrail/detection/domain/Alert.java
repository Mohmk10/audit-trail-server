package com.mohmk10.audittrail.detection.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Alert {

    private UUID id;
    private String tenantId;
    private Rule rule;
    private UUID ruleId;
    private Severity severity;
    private AlertStatus status;
    private String message;
    private List<UUID> triggeringEventIds;
    private Instant triggeredAt;
    private Instant acknowledgedAt;
    private String acknowledgedBy;
    private String resolution;
    private Instant resolvedAt;
    private Instant createdAt;

    public Alert() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .id(id)
                .tenantId(tenantId)
                .rule(rule)
                .ruleId(ruleId)
                .severity(severity)
                .status(status)
                .message(message)
                .triggeringEventIds(triggeringEventIds)
                .triggeredAt(triggeredAt)
                .acknowledgedAt(acknowledgedAt)
                .acknowledgedBy(acknowledgedBy)
                .resolution(resolution)
                .resolvedAt(resolvedAt)
                .createdAt(createdAt);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public UUID getRuleId() {
        return ruleId;
    }

    public void setRuleId(UUID ruleId) {
        this.ruleId = ruleId;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<UUID> getTriggeringEventIds() {
        return triggeringEventIds;
    }

    public void setTriggeringEventIds(List<UUID> triggeringEventIds) {
        this.triggeringEventIds = triggeringEventIds;
    }

    public Instant getTriggeredAt() {
        return triggeredAt;
    }

    public void setTriggeredAt(Instant triggeredAt) {
        this.triggeredAt = triggeredAt;
    }

    public Instant getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(Instant acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }

    public String getAcknowledgedBy() {
        return acknowledgedBy;
    }

    public void setAcknowledgedBy(String acknowledgedBy) {
        this.acknowledgedBy = acknowledgedBy;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public static class Builder {
        private UUID id;
        private String tenantId;
        private Rule rule;
        private UUID ruleId;
        private Severity severity;
        private AlertStatus status = AlertStatus.OPEN;
        private String message;
        private List<UUID> triggeringEventIds;
        private Instant triggeredAt;
        private Instant acknowledgedAt;
        private String acknowledgedBy;
        private String resolution;
        private Instant resolvedAt;
        private Instant createdAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder rule(Rule rule) {
            this.rule = rule;
            return this;
        }

        public Builder ruleId(UUID ruleId) {
            this.ruleId = ruleId;
            return this;
        }

        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public Builder status(AlertStatus status) {
            this.status = status;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder triggeringEventIds(List<UUID> triggeringEventIds) {
            this.triggeringEventIds = triggeringEventIds;
            return this;
        }

        public Builder triggeredAt(Instant triggeredAt) {
            this.triggeredAt = triggeredAt;
            return this;
        }

        public Builder acknowledgedAt(Instant acknowledgedAt) {
            this.acknowledgedAt = acknowledgedAt;
            return this;
        }

        public Builder acknowledgedBy(String acknowledgedBy) {
            this.acknowledgedBy = acknowledgedBy;
            return this;
        }

        public Builder resolution(String resolution) {
            this.resolution = resolution;
            return this;
        }

        public Builder resolvedAt(Instant resolvedAt) {
            this.resolvedAt = resolvedAt;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Alert build() {
            Alert alert = new Alert();
            alert.id = this.id;
            alert.tenantId = this.tenantId;
            alert.rule = this.rule;
            alert.ruleId = this.ruleId;
            alert.severity = this.severity;
            alert.status = this.status;
            alert.message = this.message;
            alert.triggeringEventIds = this.triggeringEventIds;
            alert.triggeredAt = this.triggeredAt;
            alert.acknowledgedAt = this.acknowledgedAt;
            alert.acknowledgedBy = this.acknowledgedBy;
            alert.resolution = this.resolution;
            alert.resolvedAt = this.resolvedAt;
            alert.createdAt = this.createdAt;
            return alert;
        }
    }
}
