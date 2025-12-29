package com.mohmk10.audittrail.detection.service;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.detection.domain.RuleCondition;
import com.mohmk10.audittrail.detection.fixtures.DetectionTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PatternEvaluatorTest {

    private PatternEvaluator patternEvaluator;
    private Event matchingEvent;
    private Event nonMatchingEvent;

    @BeforeEach
    void setUp() {
        patternEvaluator = new PatternEvaluator();
        matchingEvent = DetectionTestFixtures.createMatchingEvent();
        nonMatchingEvent = DetectionTestFixtures.createNonMatchingEvent();
    }

    @Test
    void shouldMatchEqualsOperator() {
        RuleCondition condition = DetectionTestFixtures.createSimpleCondition(
                "actionType", "EQUALS", "DELETE");

        boolean result = patternEvaluator.evaluate(matchingEvent, null, condition);

        assertThat(result).isTrue();
    }

    @Test
    void shouldNotMatchEqualsOperatorWhenDifferent() {
        RuleCondition condition = DetectionTestFixtures.createSimpleCondition(
                "actionType", "EQUALS", "DELETE");

        boolean result = patternEvaluator.evaluate(nonMatchingEvent, null, condition);

        assertThat(result).isFalse();
    }

    @Test
    void shouldMatchNotEqualsOperator() {
        RuleCondition condition = DetectionTestFixtures.createSimpleCondition(
                "actionType", "NOT_EQUALS", "DELETE");

        boolean result = patternEvaluator.evaluate(nonMatchingEvent, null, condition);

        assertThat(result).isTrue();
    }

    @Test
    void shouldMatchContainsOperator() {
        RuleCondition condition = DetectionTestFixtures.createSimpleCondition(
                "actorName", "CONTAINS", "John");

        boolean result = patternEvaluator.evaluate(matchingEvent, null, condition);

        assertThat(result).isTrue();
    }

    @Test
    void shouldMatchStartsWithOperator() {
        RuleCondition condition = DetectionTestFixtures.createSimpleCondition(
                "actorName", "STARTS_WITH", "John");

        boolean result = patternEvaluator.evaluate(matchingEvent, null, condition);

        assertThat(result).isTrue();
    }

    @Test
    void shouldMatchEndsWithOperator() {
        RuleCondition condition = DetectionTestFixtures.createSimpleCondition(
                "actorName", "ENDS_WITH", "Doe");

        boolean result = patternEvaluator.evaluate(matchingEvent, null, condition);

        assertThat(result).isTrue();
    }

    @Test
    void shouldMatchMatchesOperator() {
        RuleCondition condition = DetectionTestFixtures.createSimpleCondition(
                "actorName", "MATCHES", "John.*");

        boolean result = patternEvaluator.evaluate(matchingEvent, null, condition);

        assertThat(result).isTrue();
    }

    @Test
    void shouldMatchInOperatorWithList() {
        RuleCondition condition = DetectionTestFixtures.createSimpleCondition(
                "actorIp", "IN", List.of("192.168.1.100", "10.0.0.1"));

        boolean result = patternEvaluator.evaluate(matchingEvent, null, condition);

        assertThat(result).isTrue();
    }

    @Test
    void shouldMatchInOperatorWithCommaSeparatedString() {
        RuleCondition condition = DetectionTestFixtures.createSimpleCondition(
                "actorIp", "IN", "192.168.1.100,10.0.0.1");

        boolean result = patternEvaluator.evaluate(matchingEvent, null, condition);

        assertThat(result).isTrue();
    }

    @Test
    void shouldNotMatchInOperatorWhenNotInList() {
        RuleCondition condition = DetectionTestFixtures.createSimpleCondition(
                "actorIp", "IN", List.of("10.0.0.1", "10.0.0.2"));

        boolean result = patternEvaluator.evaluate(matchingEvent, null, condition);

        assertThat(result).isFalse();
    }

    @Test
    void shouldMatchNotInOperator() {
        RuleCondition condition = DetectionTestFixtures.createSimpleCondition(
                "actorIp", "NOT_IN", List.of("10.0.0.1", "10.0.0.2"));

        boolean result = patternEvaluator.evaluate(matchingEvent, null, condition);

        assertThat(result).isTrue();
    }

    @Test
    void shouldMatchExistsOperator() {
        RuleCondition condition = DetectionTestFixtures.createSimpleCondition(
                "actorId", "EXISTS", null);

        boolean result = patternEvaluator.evaluate(matchingEvent, null, condition);

        assertThat(result).isTrue();
    }

    @Test
    void shouldEvaluateAndConditionsAllTrue() {
        RuleCondition condition1 = DetectionTestFixtures.createSimpleCondition(
                "actionType", "EQUALS", "DELETE");
        RuleCondition condition2 = DetectionTestFixtures.createSimpleCondition(
                "tenantId", "EQUALS", "tenant-001");

        RuleCondition andCondition = DetectionTestFixtures.createAndCondition(
                List.of(condition1, condition2));

        boolean result = patternEvaluator.evaluate(matchingEvent, null, andCondition);

        assertThat(result).isTrue();
    }

    @Test
    void shouldEvaluateAndConditionsOneFalse() {
        RuleCondition condition1 = DetectionTestFixtures.createSimpleCondition(
                "actionType", "EQUALS", "DELETE");
        RuleCondition condition2 = DetectionTestFixtures.createSimpleCondition(
                "tenantId", "EQUALS", "wrong-tenant");

        RuleCondition andCondition = DetectionTestFixtures.createAndCondition(
                List.of(condition1, condition2));

        boolean result = patternEvaluator.evaluate(matchingEvent, null, andCondition);

        assertThat(result).isFalse();
    }

    @Test
    void shouldEvaluateOrConditionsOneTrue() {
        RuleCondition condition1 = DetectionTestFixtures.createSimpleCondition(
                "actionType", "EQUALS", "DELETE");
        RuleCondition condition2 = DetectionTestFixtures.createSimpleCondition(
                "actionType", "EQUALS", "UPDATE");

        RuleCondition orCondition = DetectionTestFixtures.createOrCondition(
                List.of(condition1, condition2));

        boolean result = patternEvaluator.evaluate(matchingEvent, null, orCondition);

        assertThat(result).isTrue();
    }

    @Test
    void shouldEvaluateOrConditionsAllFalse() {
        RuleCondition condition1 = DetectionTestFixtures.createSimpleCondition(
                "actionType", "EQUALS", "CREATE");
        RuleCondition condition2 = DetectionTestFixtures.createSimpleCondition(
                "actionType", "EQUALS", "UPDATE");

        RuleCondition orCondition = DetectionTestFixtures.createOrCondition(
                List.of(condition1, condition2));

        boolean result = patternEvaluator.evaluate(matchingEvent, null, orCondition);

        assertThat(result).isFalse();
    }

    @Test
    void shouldHandleNullField() {
        RuleCondition condition = new RuleCondition();
        condition.setOperator("EQUALS");
        condition.setValue("test");

        boolean result = patternEvaluator.evaluate(matchingEvent, null, condition);

        assertThat(result).isFalse();
    }

    @Test
    void shouldHandleNullOperator() {
        RuleCondition condition = new RuleCondition();
        condition.setField("actionType");
        condition.setValue("DELETE");

        boolean result = patternEvaluator.evaluate(matchingEvent, null, condition);

        assertThat(result).isFalse();
    }

    @Test
    void shouldHandleUnknownField() {
        RuleCondition condition = DetectionTestFixtures.createSimpleCondition(
                "unknownField", "EQUALS", "test");

        boolean result = patternEvaluator.evaluate(matchingEvent, null, condition);

        assertThat(result).isFalse();
    }

    @Test
    void shouldMatchActorId() {
        RuleCondition condition = DetectionTestFixtures.createSimpleCondition(
                "actorId", "EQUALS", "actor-123");

        boolean result = patternEvaluator.evaluate(matchingEvent, null, condition);

        assertThat(result).isTrue();
    }

    @Test
    void shouldMatchActorType() {
        RuleCondition condition = DetectionTestFixtures.createSimpleCondition(
                "actorType", "EQUALS", "USER");

        boolean result = patternEvaluator.evaluate(matchingEvent, null, condition);

        assertThat(result).isTrue();
    }

    @Test
    void shouldMatchResourceId() {
        RuleCondition condition = DetectionTestFixtures.createSimpleCondition(
                "resourceId", "EQUALS", "doc-456");

        boolean result = patternEvaluator.evaluate(matchingEvent, null, condition);

        assertThat(result).isTrue();
    }

    @Test
    void shouldMatchResourceType() {
        RuleCondition condition = DetectionTestFixtures.createSimpleCondition(
                "resourceType", "EQUALS", "DOCUMENT");

        boolean result = patternEvaluator.evaluate(matchingEvent, null, condition);

        assertThat(result).isTrue();
    }

    @Test
    void shouldMatchResourceName() {
        RuleCondition condition = DetectionTestFixtures.createSimpleCondition(
                "resourceName", "CONTAINS", "Sensitive");

        boolean result = patternEvaluator.evaluate(matchingEvent, null, condition);

        assertThat(result).isTrue();
    }

    @Test
    void shouldMatchSource() {
        RuleCondition condition = DetectionTestFixtures.createSimpleCondition(
                "source", "EQUALS", "web-app");

        boolean result = patternEvaluator.evaluate(matchingEvent, null, condition);

        assertThat(result).isTrue();
    }

    @Test
    void shouldMatchCorrelationId() {
        RuleCondition condition = DetectionTestFixtures.createSimpleCondition(
                "correlationId", "EQUALS", "corr-789");

        boolean result = patternEvaluator.evaluate(matchingEvent, null, condition);

        assertThat(result).isTrue();
    }

    @Test
    void shouldHandleUnknownOperator() {
        RuleCondition condition = DetectionTestFixtures.createSimpleCondition(
                "actionType", "UNKNOWN_OP", "DELETE");

        boolean result = patternEvaluator.evaluate(matchingEvent, null, condition);

        assertThat(result).isFalse();
    }
}
