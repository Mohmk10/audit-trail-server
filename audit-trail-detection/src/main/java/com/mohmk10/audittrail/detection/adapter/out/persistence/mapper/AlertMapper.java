package com.mohmk10.audittrail.detection.adapter.out.persistence.mapper;

import com.mohmk10.audittrail.detection.adapter.out.persistence.entity.AlertEntity;
import com.mohmk10.audittrail.detection.domain.Alert;
import org.springframework.stereotype.Component;

@Component
public class AlertMapper {

    private final RuleMapper ruleMapper;

    public AlertMapper(RuleMapper ruleMapper) {
        this.ruleMapper = ruleMapper;
    }

    public Alert toDomain(AlertEntity entity) {
        if (entity == null) {
            return null;
        }

        return Alert.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .ruleId(entity.getRuleId())
                .rule(entity.getRule() != null ? ruleMapper.toDomain(entity.getRule()) : null)
                .severity(entity.getSeverity())
                .status(entity.getStatus())
                .message(entity.getMessage())
                .triggeringEventIds(entity.getTriggeringEventIds())
                .triggeredAt(entity.getTriggeredAt())
                .acknowledgedAt(entity.getAcknowledgedAt())
                .acknowledgedBy(entity.getAcknowledgedBy())
                .resolution(entity.getResolution())
                .resolvedAt(entity.getResolvedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public AlertEntity toEntity(Alert alert) {
        if (alert == null) {
            return null;
        }

        AlertEntity entity = new AlertEntity();
        entity.setId(alert.getId());
        entity.setTenantId(alert.getTenantId());
        entity.setRuleId(alert.getRuleId() != null ? alert.getRuleId() :
                (alert.getRule() != null ? alert.getRule().getId() : null));
        entity.setSeverity(alert.getSeverity());
        entity.setStatus(alert.getStatus());
        entity.setMessage(alert.getMessage());
        entity.setTriggeringEventIds(alert.getTriggeringEventIds());
        entity.setTriggeredAt(alert.getTriggeredAt());
        entity.setAcknowledgedAt(alert.getAcknowledgedAt());
        entity.setAcknowledgedBy(alert.getAcknowledgedBy());
        entity.setResolution(alert.getResolution());
        entity.setResolvedAt(alert.getResolvedAt());
        entity.setCreatedAt(alert.getCreatedAt());
        return entity;
    }
}
