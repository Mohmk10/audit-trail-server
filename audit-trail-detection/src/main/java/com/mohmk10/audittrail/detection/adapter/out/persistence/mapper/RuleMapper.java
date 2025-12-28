package com.mohmk10.audittrail.detection.adapter.out.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mohmk10.audittrail.detection.adapter.out.persistence.entity.RuleEntity;
import com.mohmk10.audittrail.detection.domain.Rule;
import com.mohmk10.audittrail.detection.domain.RuleAction;
import com.mohmk10.audittrail.detection.domain.RuleCondition;
import org.springframework.stereotype.Component;

@Component
public class RuleMapper {

    private final ObjectMapper objectMapper;

    public RuleMapper() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public Rule toDomain(RuleEntity entity) {
        if (entity == null) {
            return null;
        }

        return Rule.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .tenantId(entity.getTenantId())
                .enabled(entity.isEnabled())
                .type(entity.getType())
                .condition(deserializeCondition(entity.getConditionJson()))
                .action(deserializeAction(entity.getActionJson()))
                .severity(entity.getSeverity())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public RuleEntity toEntity(Rule rule) {
        if (rule == null) {
            return null;
        }

        RuleEntity entity = new RuleEntity();
        entity.setId(rule.getId());
        entity.setName(rule.getName());
        entity.setDescription(rule.getDescription());
        entity.setTenantId(rule.getTenantId());
        entity.setEnabled(rule.isEnabled());
        entity.setType(rule.getType());
        entity.setConditionJson(serializeCondition(rule.getCondition()));
        entity.setActionJson(serializeAction(rule.getAction()));
        entity.setSeverity(rule.getSeverity());
        entity.setCreatedAt(rule.getCreatedAt());
        entity.setUpdatedAt(rule.getUpdatedAt());
        return entity;
    }

    private String serializeCondition(RuleCondition condition) {
        if (condition == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(condition);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private RuleCondition deserializeCondition(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, RuleCondition.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String serializeAction(RuleAction action) {
        if (action == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(action);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private RuleAction deserializeAction(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, RuleAction.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
