package com.mohmk10.audittrail.detection.service;

import com.mohmk10.audittrail.core.domain.Action;
import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.dto.DateRange;
import com.mohmk10.audittrail.core.dto.SearchCriteria;
import com.mohmk10.audittrail.core.dto.SearchResult;
import com.mohmk10.audittrail.detection.domain.Rule;
import com.mohmk10.audittrail.detection.domain.RuleCondition;
import com.mohmk10.audittrail.search.service.EventSearchService;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Component
public class ThresholdEvaluator {

    private final EventSearchService eventSearchService;

    public ThresholdEvaluator(EventSearchService eventSearchService) {
        this.eventSearchService = eventSearchService;
    }

    public boolean evaluate(Event event, Rule rule, RuleCondition condition) {
        if (condition.getThreshold() == null || condition.getWindowMinutes() == null) {
            return false;
        }

        int windowMinutes = condition.getWindowMinutes();
        int threshold = condition.getThreshold();

        Instant now = Instant.now();
        Instant windowStart = now.minus(windowMinutes, ChronoUnit.MINUTES);

        SearchCriteria.Builder criteriaBuilder = SearchCriteria.builder()
                .tenantId(event.metadata().tenantId())
                .dateRange(new DateRange(windowStart, now))
                .page(0)
                .size(threshold + 1);

        applyConditionToSearchCriteria(criteriaBuilder, condition);

        SearchResult<Event> result = eventSearchService.search(criteriaBuilder.build());

        return result.totalCount() >= threshold;
    }

    public List<UUID> getMatchingEventIds(Event event, RuleCondition condition) {
        if (condition.getWindowMinutes() == null) {
            return List.of(event.id());
        }

        int windowMinutes = condition.getWindowMinutes();
        Instant now = Instant.now();
        Instant windowStart = now.minus(windowMinutes, ChronoUnit.MINUTES);

        SearchCriteria.Builder criteriaBuilder = SearchCriteria.builder()
                .tenantId(event.metadata().tenantId())
                .dateRange(new DateRange(windowStart, now))
                .page(0)
                .size(100);

        applyConditionToSearchCriteria(criteriaBuilder, condition);

        SearchResult<Event> result = eventSearchService.search(criteriaBuilder.build());

        return result.items().stream()
                .map(Event::id)
                .toList();
    }

    private void applyConditionToSearchCriteria(SearchCriteria.Builder criteriaBuilder, RuleCondition condition) {
        if (condition.getField() == null || condition.getValue() == null) {
            return;
        }

        String field = condition.getField();
        String value = condition.getValue().toString();

        switch (field) {
            case "actionType" -> {
                try {
                    Action.ActionType actionType = Action.ActionType.valueOf(value);
                    criteriaBuilder.actionTypes(List.of(actionType));
                } catch (IllegalArgumentException e) {
                    // Invalid action type, ignore
                }
            }
            case "actorId" -> criteriaBuilder.actorId(value);
            case "correlationId" -> criteriaBuilder.correlationId(value);
            case "sessionId" -> criteriaBuilder.sessionId(value);
            default -> {
                // For other fields, use query string
                criteriaBuilder.query(field + ":" + value);
            }
        }
    }
}
