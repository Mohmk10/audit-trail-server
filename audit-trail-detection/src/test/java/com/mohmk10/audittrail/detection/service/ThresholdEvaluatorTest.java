package com.mohmk10.audittrail.detection.service;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.dto.SearchCriteria;
import com.mohmk10.audittrail.core.dto.SearchResult;
import com.mohmk10.audittrail.detection.domain.Rule;
import com.mohmk10.audittrail.detection.domain.RuleCondition;
import com.mohmk10.audittrail.detection.fixtures.DetectionTestFixtures;
import com.mohmk10.audittrail.search.service.EventSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThresholdEvaluatorTest {

    @Mock
    private EventSearchService eventSearchService;

    private ThresholdEvaluator thresholdEvaluator;
    private Event matchingEvent;

    @BeforeEach
    void setUp() {
        thresholdEvaluator = new ThresholdEvaluator(eventSearchService);
        matchingEvent = DetectionTestFixtures.createMatchingEvent();
    }

    @Test
    void shouldReturnTrueWhenThresholdExceeded() {
        Rule rule = DetectionTestFixtures.createThresholdRule();
        RuleCondition condition = rule.getCondition();

        List<Event> events = List.of(
                DetectionTestFixtures.createMatchingEvent(),
                DetectionTestFixtures.createMatchingEvent(),
                DetectionTestFixtures.createMatchingEvent(),
                DetectionTestFixtures.createMatchingEvent(),
                DetectionTestFixtures.createMatchingEvent()
        );
        SearchResult<Event> searchResult = SearchResult.of(events, 5, 0, 10);
        when(eventSearchService.search(any(SearchCriteria.class))).thenReturn(searchResult);

        boolean result = thresholdEvaluator.evaluate(matchingEvent, rule, condition);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenThresholdNotReached() {
        Rule rule = DetectionTestFixtures.createThresholdRule();
        RuleCondition condition = rule.getCondition();

        List<Event> events = List.of(
                DetectionTestFixtures.createMatchingEvent(),
                DetectionTestFixtures.createMatchingEvent()
        );
        SearchResult<Event> searchResult = SearchResult.of(events, 2, 0, 10);
        when(eventSearchService.search(any(SearchCriteria.class))).thenReturn(searchResult);

        boolean result = thresholdEvaluator.evaluate(matchingEvent, rule, condition);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenThresholdIsNull() {
        RuleCondition condition = new RuleCondition();
        condition.setField("actionType");
        condition.setOperator("EQUALS");
        condition.setValue("DELETE");
        condition.setWindowMinutes(60);

        boolean result = thresholdEvaluator.evaluate(matchingEvent, null, condition);

        assertThat(result).isFalse();
        verifyNoInteractions(eventSearchService);
    }

    @Test
    void shouldReturnFalseWhenWindowMinutesIsNull() {
        RuleCondition condition = new RuleCondition();
        condition.setField("actionType");
        condition.setOperator("EQUALS");
        condition.setValue("DELETE");
        condition.setThreshold(5);

        boolean result = thresholdEvaluator.evaluate(matchingEvent, null, condition);

        assertThat(result).isFalse();
        verifyNoInteractions(eventSearchService);
    }

    @Test
    void shouldUseCorrectSearchCriteria() {
        Rule rule = DetectionTestFixtures.createThresholdRule();
        RuleCondition condition = rule.getCondition();

        List<Event> events = List.of(DetectionTestFixtures.createMatchingEvent());
        SearchResult<Event> searchResult = SearchResult.of(events, 1, 0, 10);
        when(eventSearchService.search(any(SearchCriteria.class))).thenReturn(searchResult);

        thresholdEvaluator.evaluate(matchingEvent, rule, condition);

        ArgumentCaptor<SearchCriteria> captor = ArgumentCaptor.forClass(SearchCriteria.class);
        verify(eventSearchService).search(captor.capture());

        SearchCriteria criteria = captor.getValue();
        assertThat(criteria.tenantId()).isEqualTo("tenant-001");
        assertThat(criteria.dateRange()).isNotNull();
    }

    @Test
    void shouldGetMatchingEventIds() {
        RuleCondition condition = new RuleCondition();
        condition.setField("actionType");
        condition.setOperator("EQUALS");
        condition.setValue("DELETE");
        condition.setWindowMinutes(60);

        Event event1 = DetectionTestFixtures.createMatchingEvent();
        Event event2 = DetectionTestFixtures.createMatchingEvent();
        List<Event> events = List.of(event1, event2);
        SearchResult<Event> searchResult = SearchResult.of(events, 2, 0, 100);
        when(eventSearchService.search(any(SearchCriteria.class))).thenReturn(searchResult);

        List<UUID> eventIds = thresholdEvaluator.getMatchingEventIds(matchingEvent, condition);

        assertThat(eventIds).hasSize(2);
        assertThat(eventIds).contains(event1.id(), event2.id());
    }

    @Test
    void shouldReturnSingleEventIdWhenNoWindow() {
        RuleCondition condition = new RuleCondition();
        condition.setField("actionType");
        condition.setOperator("EQUALS");
        condition.setValue("DELETE");

        List<UUID> eventIds = thresholdEvaluator.getMatchingEventIds(matchingEvent, condition);

        assertThat(eventIds).hasSize(1);
        assertThat(eventIds).contains(matchingEvent.id());
        verifyNoInteractions(eventSearchService);
    }

    @Test
    void shouldApplyActionTypeToSearchCriteria() {
        RuleCondition condition = new RuleCondition();
        condition.setField("actionType");
        condition.setOperator("EQUALS");
        condition.setValue("DELETE");
        condition.setThreshold(5);
        condition.setWindowMinutes(60);

        SearchResult<Event> searchResult = SearchResult.of(List.of(), 0, 0, 10);
        when(eventSearchService.search(any(SearchCriteria.class))).thenReturn(searchResult);

        thresholdEvaluator.evaluate(matchingEvent, null, condition);

        ArgumentCaptor<SearchCriteria> captor = ArgumentCaptor.forClass(SearchCriteria.class);
        verify(eventSearchService).search(captor.capture());

        SearchCriteria criteria = captor.getValue();
        assertThat(criteria.actionTypes()).isNotNull();
    }

    @Test
    void shouldApplyActorIdToSearchCriteria() {
        RuleCondition condition = new RuleCondition();
        condition.setField("actorId");
        condition.setOperator("EQUALS");
        condition.setValue("actor-123");
        condition.setThreshold(5);
        condition.setWindowMinutes(60);

        SearchResult<Event> searchResult = SearchResult.of(List.of(), 0, 0, 10);
        when(eventSearchService.search(any(SearchCriteria.class))).thenReturn(searchResult);

        thresholdEvaluator.evaluate(matchingEvent, null, condition);

        ArgumentCaptor<SearchCriteria> captor = ArgumentCaptor.forClass(SearchCriteria.class);
        verify(eventSearchService).search(captor.capture());

        SearchCriteria criteria = captor.getValue();
        assertThat(criteria.actorId()).isEqualTo("actor-123");
    }

    @Test
    void shouldHandleInvalidActionType() {
        RuleCondition condition = new RuleCondition();
        condition.setField("actionType");
        condition.setOperator("EQUALS");
        condition.setValue("INVALID_ACTION");
        condition.setThreshold(5);
        condition.setWindowMinutes(60);

        SearchResult<Event> searchResult = SearchResult.of(List.of(), 0, 0, 10);
        when(eventSearchService.search(any(SearchCriteria.class))).thenReturn(searchResult);

        boolean result = thresholdEvaluator.evaluate(matchingEvent, null, condition);

        assertThat(result).isFalse();
    }
}
