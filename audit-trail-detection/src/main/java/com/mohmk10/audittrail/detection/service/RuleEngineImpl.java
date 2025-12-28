package com.mohmk10.audittrail.detection.service;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.detection.adapter.out.persistence.mapper.RuleMapper;
import com.mohmk10.audittrail.detection.adapter.out.persistence.repository.JpaRuleRepository;
import com.mohmk10.audittrail.detection.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RuleEngineImpl implements RuleEngine {

    private static final Logger log = LoggerFactory.getLogger(RuleEngineImpl.class);

    private final JpaRuleRepository ruleRepository;
    private final RuleMapper ruleMapper;
    private final ThresholdEvaluator thresholdEvaluator;
    private final PatternEvaluator patternEvaluator;

    public RuleEngineImpl(
            JpaRuleRepository ruleRepository,
            RuleMapper ruleMapper,
            ThresholdEvaluator thresholdEvaluator,
            PatternEvaluator patternEvaluator) {
        this.ruleRepository = ruleRepository;
        this.ruleMapper = ruleMapper;
        this.thresholdEvaluator = thresholdEvaluator;
        this.patternEvaluator = patternEvaluator;
    }

    @Override
    public List<Alert> evaluate(Event event) {
        String tenantId = event.metadata().tenantId();
        List<Rule> enabledRules = ruleRepository.findByTenantIdAndEnabled(tenantId, true)
                .stream()
                .map(ruleMapper::toDomain)
                .toList();

        List<Alert> alerts = new ArrayList<>();

        for (Rule rule : enabledRules) {
            try {
                if (shouldTriggerAlert(event, rule)) {
                    Alert alert = createAlert(event, rule);
                    alerts.add(alert);
                    log.info("Rule {} triggered for event {} in tenant {}",
                            rule.getName(), event.id(), tenantId);
                }
            } catch (Exception e) {
                log.error("Error evaluating rule {} for event {}: {}",
                        rule.getId(), event.id(), e.getMessage());
            }
        }

        return alerts;
    }

    @Override
    public boolean matches(Event event, RuleCondition condition) {
        if (condition == null) {
            return false;
        }

        if (condition.getAnd() != null && !condition.getAnd().isEmpty()) {
            return condition.getAnd().stream().allMatch(c -> matches(event, c));
        }

        if (condition.getOr() != null && !condition.getOr().isEmpty()) {
            return condition.getOr().stream().anyMatch(c -> matches(event, c));
        }

        return patternEvaluator.evaluate(event, null, condition);
    }

    private boolean shouldTriggerAlert(Event event, Rule rule) {
        RuleCondition condition = rule.getCondition();
        if (condition == null) {
            return false;
        }

        return switch (rule.getType()) {
            case THRESHOLD -> evaluateThreshold(event, rule, condition);
            case PATTERN -> patternEvaluator.evaluate(event, rule, condition);
            case BLACKLIST -> evaluateBlacklist(event, condition);
            case TIME_BASED -> evaluateTimeBased(event, condition);
            case ANOMALY -> evaluateAnomaly(event, rule, condition);
        };
    }

    private boolean evaluateThreshold(Event event, Rule rule, RuleCondition condition) {
        if (!matches(event, condition)) {
            return false;
        }
        return thresholdEvaluator.evaluate(event, rule, condition);
    }

    private boolean evaluateBlacklist(Event event, RuleCondition condition) {
        return matches(event, condition);
    }

    private boolean evaluateTimeBased(Event event, RuleCondition condition) {
        if (!matches(event, condition)) {
            return false;
        }

        if (condition.getValue() instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> timeConfig = (java.util.Map<String, Object>) condition.getValue();
            return isOutsideAllowedHours(event.timestamp(), timeConfig);
        }

        return false;
    }

    private boolean isOutsideAllowedHours(Instant timestamp, java.util.Map<String, Object> timeConfig) {
        LocalTime eventTime = LocalTime.ofInstant(timestamp, ZoneId.systemDefault());

        Object startObj = timeConfig.get("allowedStartHour");
        Object endObj = timeConfig.get("allowedEndHour");

        if (startObj == null || endObj == null) {
            return false;
        }

        int startHour = Integer.parseInt(startObj.toString());
        int endHour = Integer.parseInt(endObj.toString());

        LocalTime startTime = LocalTime.of(startHour, 0);
        LocalTime endTime = LocalTime.of(endHour, 0);

        if (startHour < endHour) {
            return eventTime.isBefore(startTime) || eventTime.isAfter(endTime);
        } else {
            return eventTime.isBefore(startTime) && eventTime.isAfter(endTime);
        }
    }

    private boolean evaluateAnomaly(Event event, Rule rule, RuleCondition condition) {
        return matches(event, condition);
    }

    private Alert createAlert(Event event, Rule rule) {
        List<UUID> eventIds = List.of(event.id());

        if (rule.getType() == RuleType.THRESHOLD) {
            eventIds = thresholdEvaluator.getMatchingEventIds(event, rule.getCondition());
        }

        String message = String.format("Rule '%s' triggered: %s",
                rule.getName(),
                rule.getDescription() != null ? rule.getDescription() : "No description");

        return Alert.builder()
                .id(UUID.randomUUID())
                .tenantId(event.metadata().tenantId())
                .rule(rule)
                .ruleId(rule.getId())
                .severity(rule.getSeverity())
                .status(AlertStatus.OPEN)
                .message(message)
                .triggeringEventIds(eventIds)
                .triggeredAt(Instant.now())
                .createdAt(Instant.now())
                .build();
    }
}
