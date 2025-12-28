package com.mohmk10.audittrail.detection.domain;

import java.time.Instant;
import java.util.UUID;

public class Rule {

    private UUID id;
    private String name;
    private String description;
    private String tenantId;
    private boolean enabled;
    private RuleType type;
    private RuleCondition condition;
    private RuleAction action;
    private Severity severity;
    private Instant createdAt;
    private Instant updatedAt;

    public Rule() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .id(id)
                .name(name)
                .description(description)
                .tenantId(tenantId)
                .enabled(enabled)
                .type(type)
                .condition(condition)
                .action(action)
                .severity(severity)
                .createdAt(createdAt)
                .updatedAt(updatedAt);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public RuleType getType() {
        return type;
    }

    public void setType(RuleType type) {
        this.type = type;
    }

    public RuleCondition getCondition() {
        return condition;
    }

    public void setCondition(RuleCondition condition) {
        this.condition = condition;
    }

    public RuleAction getAction() {
        return action;
    }

    public void setAction(RuleAction action) {
        this.action = action;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class Builder {
        private UUID id;
        private String name;
        private String description;
        private String tenantId;
        private boolean enabled = true;
        private RuleType type;
        private RuleCondition condition;
        private RuleAction action;
        private Severity severity;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder type(RuleType type) {
            this.type = type;
            return this;
        }

        public Builder condition(RuleCondition condition) {
            this.condition = condition;
            return this;
        }

        public Builder action(RuleAction action) {
            this.action = action;
            return this;
        }

        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Rule build() {
            Rule rule = new Rule();
            rule.id = this.id;
            rule.name = this.name;
            rule.description = this.description;
            rule.tenantId = this.tenantId;
            rule.enabled = this.enabled;
            rule.type = this.type;
            rule.condition = this.condition;
            rule.action = this.action;
            rule.severity = this.severity;
            rule.createdAt = this.createdAt;
            rule.updatedAt = this.updatedAt;
            return rule;
        }
    }
}
