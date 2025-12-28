package com.mohmk10.audittrail.detection.adapter.in.rest.dto;

import com.mohmk10.audittrail.detection.domain.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RuleDtoMapper {

    public Rule toDomain(CreateRuleRequest request) {
        return Rule.builder()
                .name(request.name())
                .description(request.description())
                .tenantId(request.tenantId())
                .type(request.type())
                .condition(toCondition(request.condition()))
                .action(toAction(request.action()))
                .severity(request.severity())
                .enabled(true)
                .build();
    }

    public Rule applyUpdate(Rule existing, UpdateRuleRequest request) {
        Rule.Builder builder = existing.toBuilder();

        if (request.name() != null) {
            builder.name(request.name());
        }
        if (request.description() != null) {
            builder.description(request.description());
        }
        if (request.enabled() != null) {
            builder.enabled(request.enabled());
        }
        if (request.condition() != null) {
            builder.condition(toCondition(request.condition()));
        }
        if (request.action() != null) {
            builder.action(toAction(request.action()));
        }
        if (request.severity() != null) {
            builder.severity(request.severity());
        }

        return builder.build();
    }

    public RuleResponse toResponse(Rule rule) {
        return new RuleResponse(
                rule.getId(),
                rule.getName(),
                rule.getDescription(),
                rule.getTenantId(),
                rule.isEnabled(),
                rule.getType(),
                toConditionDto(rule.getCondition()),
                toActionDto(rule.getAction()),
                rule.getSeverity(),
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }

    public AlertResponse toAlertResponse(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getTenantId(),
                alert.getRuleId(),
                alert.getRule() != null ? alert.getRule().getName() : null,
                alert.getSeverity(),
                alert.getStatus(),
                alert.getMessage(),
                alert.getTriggeringEventIds(),
                alert.getTriggeredAt(),
                alert.getAcknowledgedAt(),
                alert.getAcknowledgedBy(),
                alert.getResolution(),
                alert.getResolvedAt()
        );
    }

    private RuleCondition toCondition(RuleConditionDto dto) {
        if (dto == null) {
            return null;
        }

        RuleCondition condition = new RuleCondition();
        condition.setField(dto.field());
        condition.setOperator(dto.operator());
        condition.setValue(dto.value());
        condition.setThreshold(dto.threshold());
        condition.setWindowMinutes(dto.windowMinutes());

        if (dto.and() != null) {
            condition.setAnd(dto.and().stream().map(this::toCondition).toList());
        }
        if (dto.or() != null) {
            condition.setOr(dto.or().stream().map(this::toCondition).toList());
        }

        return condition;
    }

    private RuleConditionDto toConditionDto(RuleCondition condition) {
        if (condition == null) {
            return null;
        }

        List<RuleConditionDto> andDtos = condition.getAnd() != null
                ? condition.getAnd().stream().map(this::toConditionDto).toList()
                : null;
        List<RuleConditionDto> orDtos = condition.getOr() != null
                ? condition.getOr().stream().map(this::toConditionDto).toList()
                : null;

        return new RuleConditionDto(
                condition.getField(),
                condition.getOperator(),
                condition.getValue(),
                condition.getThreshold(),
                condition.getWindowMinutes(),
                andDtos,
                orDtos
        );
    }

    private RuleAction toAction(RuleActionDto dto) {
        if (dto == null) {
            return null;
        }

        RuleAction action = new RuleAction();
        action.setAlertType(dto.alertType());
        action.setNotificationChannels(dto.notificationChannels());
        action.setParameters(dto.parameters());
        return action;
    }

    private RuleActionDto toActionDto(RuleAction action) {
        if (action == null) {
            return null;
        }

        return new RuleActionDto(
                action.getAlertType(),
                action.getNotificationChannels(),
                action.getParameters()
        );
    }
}
