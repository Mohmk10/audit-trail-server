package com.mohmk10.audittrail.detection.service;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.detection.domain.Rule;
import com.mohmk10.audittrail.detection.domain.RuleCondition;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PatternEvaluator {

    public boolean evaluate(Event event, Rule rule, RuleCondition condition) {
        if (condition.getAnd() != null && !condition.getAnd().isEmpty()) {
            return evaluateAndConditions(event, condition.getAnd());
        }

        if (condition.getOr() != null && !condition.getOr().isEmpty()) {
            return evaluateOrConditions(event, condition.getOr());
        }

        return matchesSingleCondition(event, condition);
    }

    private boolean evaluateAndConditions(Event event, List<RuleCondition> conditions) {
        for (RuleCondition condition : conditions) {
            if (!matchesSingleCondition(event, condition)) {
                return false;
            }
        }
        return true;
    }

    private boolean evaluateOrConditions(Event event, List<RuleCondition> conditions) {
        for (RuleCondition condition : conditions) {
            if (matchesSingleCondition(event, condition)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesSingleCondition(Event event, RuleCondition condition) {
        String field = condition.getField();
        String operator = condition.getOperator();
        Object value = condition.getValue();

        if (field == null || operator == null) {
            return false;
        }

        Object fieldValue = getFieldValue(event, field);
        if (fieldValue == null && !"NOT_EXISTS".equals(operator)) {
            return false;
        }

        return evaluateOperator(fieldValue, operator, value);
    }

    private Object getFieldValue(Event event, String field) {
        return switch (field) {
            case "actionType" -> event.action().type().name();
            case "actorId" -> event.actor().id();
            case "actorType" -> event.actor().type().name();
            case "actorName" -> event.actor().name();
            case "actorIp" -> event.actor().ip();
            case "resourceId" -> event.resource().id();
            case "resourceType" -> event.resource().type().name();
            case "resourceName" -> event.resource().name();
            case "tenantId" -> event.metadata().tenantId();
            case "source" -> event.metadata().source();
            case "correlationId" -> event.metadata().correlationId();
            default -> null;
        };
    }

    private boolean evaluateOperator(Object fieldValue, String operator, Object value) {
        if (fieldValue == null) {
            return "NOT_EXISTS".equals(operator);
        }

        String fieldStr = fieldValue.toString();
        String valueStr = value != null ? value.toString() : "";

        return switch (operator) {
            case "EQUALS" -> fieldStr.equals(valueStr);
            case "NOT_EQUALS" -> !fieldStr.equals(valueStr);
            case "CONTAINS" -> fieldStr.contains(valueStr);
            case "STARTS_WITH" -> fieldStr.startsWith(valueStr);
            case "ENDS_WITH" -> fieldStr.endsWith(valueStr);
            case "MATCHES" -> fieldStr.matches(valueStr);
            case "IN" -> evaluateInOperator(fieldStr, value);
            case "NOT_IN" -> !evaluateInOperator(fieldStr, value);
            case "EXISTS" -> true;
            case "NOT_EXISTS" -> false;
            default -> false;
        };
    }

    @SuppressWarnings("unchecked")
    private boolean evaluateInOperator(String fieldValue, Object value) {
        if (value instanceof List) {
            return ((List<Object>) value).stream()
                    .map(Object::toString)
                    .anyMatch(v -> v.equals(fieldValue));
        }
        if (value instanceof String valueStr) {
            // Handle comma-separated values
            String[] values = valueStr.split(",");
            for (String v : values) {
                if (v.trim().equals(fieldValue)) {
                    return true;
                }
            }
        }
        return false;
    }
}
