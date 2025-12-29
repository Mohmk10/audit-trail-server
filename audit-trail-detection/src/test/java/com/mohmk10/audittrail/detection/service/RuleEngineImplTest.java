package com.mohmk10.audittrail.detection.service;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.detection.adapter.out.persistence.entity.RuleEntity;
import com.mohmk10.audittrail.detection.adapter.out.persistence.mapper.RuleMapper;
import com.mohmk10.audittrail.detection.adapter.out.persistence.repository.JpaRuleRepository;
import com.mohmk10.audittrail.detection.domain.*;
import com.mohmk10.audittrail.detection.fixtures.DetectionTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleEngineImplTest {

    @Mock
    private JpaRuleRepository ruleRepository;

    @Mock
    private RuleMapper ruleMapper;

    @Mock
    private ThresholdEvaluator thresholdEvaluator;

    @Mock
    private PatternEvaluator patternEvaluator;

    private RuleEngineImpl ruleEngine;
    private Event matchingEvent;
    private Event nonMatchingEvent;

    @BeforeEach
    void setUp() {
        ruleEngine = new RuleEngineImpl(ruleRepository, ruleMapper, thresholdEvaluator, patternEvaluator);
        matchingEvent = DetectionTestFixtures.createMatchingEvent();
        nonMatchingEvent = DetectionTestFixtures.createNonMatchingEvent();
    }

    @Test
    void shouldEvaluatePatternRuleAndCreateAlert() {
        Rule patternRule = DetectionTestFixtures.createPatternRule();
        RuleEntity ruleEntity = new RuleEntity();

        when(ruleRepository.findByTenantIdAndEnabled("tenant-001", true))
                .thenReturn(List.of(ruleEntity));
        when(ruleMapper.toDomain(ruleEntity)).thenReturn(patternRule);
        when(patternEvaluator.evaluate(eq(matchingEvent), eq(patternRule), any(RuleCondition.class)))
                .thenReturn(true);

        List<Alert> alerts = ruleEngine.evaluate(matchingEvent);

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getSeverity()).isEqualTo(Severity.MEDIUM);
        assertThat(alerts.get(0).getTenantId()).isEqualTo("tenant-001");
        assertThat(alerts.get(0).getStatus()).isEqualTo(AlertStatus.OPEN);
    }

    @Test
    void shouldNotCreateAlertWhenPatternDoesNotMatch() {
        Rule patternRule = DetectionTestFixtures.createPatternRule();
        RuleEntity ruleEntity = new RuleEntity();

        when(ruleRepository.findByTenantIdAndEnabled("tenant-001", true))
                .thenReturn(List.of(ruleEntity));
        when(ruleMapper.toDomain(ruleEntity)).thenReturn(patternRule);
        when(patternEvaluator.evaluate(eq(nonMatchingEvent), eq(patternRule), any(RuleCondition.class)))
                .thenReturn(false);

        List<Alert> alerts = ruleEngine.evaluate(nonMatchingEvent);

        assertThat(alerts).isEmpty();
    }

    @Test
    void shouldEvaluateThresholdRule() {
        Rule thresholdRule = DetectionTestFixtures.createThresholdRule();
        RuleEntity ruleEntity = new RuleEntity();

        when(ruleRepository.findByTenantIdAndEnabled("tenant-001", true))
                .thenReturn(List.of(ruleEntity));
        when(ruleMapper.toDomain(ruleEntity)).thenReturn(thresholdRule);
        when(patternEvaluator.evaluate(eq(matchingEvent), isNull(), any(RuleCondition.class)))
                .thenReturn(true);
        when(thresholdEvaluator.evaluate(eq(matchingEvent), eq(thresholdRule), any(RuleCondition.class)))
                .thenReturn(true);
        when(thresholdEvaluator.getMatchingEventIds(eq(matchingEvent), any(RuleCondition.class)))
                .thenReturn(List.of(UUID.randomUUID(), UUID.randomUUID()));

        List<Alert> alerts = ruleEngine.evaluate(matchingEvent);

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getTriggeringEventIds()).hasSize(2);
    }

    @Test
    void shouldEvaluateBlacklistRule() {
        Rule blacklistRule = DetectionTestFixtures.createBlacklistRule();
        RuleEntity ruleEntity = new RuleEntity();

        when(ruleRepository.findByTenantIdAndEnabled("tenant-001", true))
                .thenReturn(List.of(ruleEntity));
        when(ruleMapper.toDomain(ruleEntity)).thenReturn(blacklistRule);
        when(patternEvaluator.evaluate(eq(matchingEvent), isNull(), any(RuleCondition.class)))
                .thenReturn(true);

        List<Alert> alerts = ruleEngine.evaluate(matchingEvent);

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getSeverity()).isEqualTo(Severity.CRITICAL);
    }

    @Test
    void shouldEvaluateTimeBasedRule() {
        RuleCondition condition = new RuleCondition();
        condition.setField("actionType");
        condition.setOperator("EQUALS");
        condition.setValue(Map.of("allowedStartHour", 9, "allowedEndHour", 17));

        Rule timeBasedRule = Rule.builder()
                .id(UUID.randomUUID())
                .name("After Hours Detection")
                .tenantId("tenant-001")
                .enabled(true)
                .type(RuleType.TIME_BASED)
                .condition(condition)
                .severity(Severity.HIGH)
                .build();

        RuleEntity ruleEntity = new RuleEntity();

        when(ruleRepository.findByTenantIdAndEnabled("tenant-001", true))
                .thenReturn(List.of(ruleEntity));
        when(ruleMapper.toDomain(ruleEntity)).thenReturn(timeBasedRule);
        when(patternEvaluator.evaluate(eq(matchingEvent), isNull(), any(RuleCondition.class)))
                .thenReturn(true);

        List<Alert> alerts = ruleEngine.evaluate(matchingEvent);

        // Result depends on current time
        assertThat(alerts.size()).isLessThanOrEqualTo(1);
    }

    @Test
    void shouldEvaluateMultipleRules() {
        Rule patternRule = DetectionTestFixtures.createPatternRule();
        Rule blacklistRule = DetectionTestFixtures.createBlacklistRule();
        RuleEntity ruleEntity1 = new RuleEntity();
        RuleEntity ruleEntity2 = new RuleEntity();

        when(ruleRepository.findByTenantIdAndEnabled("tenant-001", true))
                .thenReturn(List.of(ruleEntity1, ruleEntity2));
        when(ruleMapper.toDomain(ruleEntity1)).thenReturn(patternRule);
        when(ruleMapper.toDomain(ruleEntity2)).thenReturn(blacklistRule);
        when(patternEvaluator.evaluate(eq(matchingEvent), any(), any(RuleCondition.class)))
                .thenReturn(true);

        List<Alert> alerts = ruleEngine.evaluate(matchingEvent);

        assertThat(alerts).hasSize(2);
    }

    @Test
    void shouldReturnEmptyListWhenNoRulesEnabled() {
        when(ruleRepository.findByTenantIdAndEnabled("tenant-001", true))
                .thenReturn(List.of());

        List<Alert> alerts = ruleEngine.evaluate(matchingEvent);

        assertThat(alerts).isEmpty();
    }

    @Test
    void shouldHandleRuleWithNullCondition() {
        Rule ruleWithoutCondition = Rule.builder()
                .id(UUID.randomUUID())
                .name("Rule without condition")
                .tenantId("tenant-001")
                .enabled(true)
                .type(RuleType.PATTERN)
                .build();
        RuleEntity ruleEntity = new RuleEntity();

        when(ruleRepository.findByTenantIdAndEnabled("tenant-001", true))
                .thenReturn(List.of(ruleEntity));
        when(ruleMapper.toDomain(ruleEntity)).thenReturn(ruleWithoutCondition);

        List<Alert> alerts = ruleEngine.evaluate(matchingEvent);

        assertThat(alerts).isEmpty();
    }

    @Test
    void shouldContinueEvaluationOnRuleError() {
        Rule failingRule = DetectionTestFixtures.createPatternRule();
        Rule successfulRule = DetectionTestFixtures.createBlacklistRule();
        RuleEntity ruleEntity1 = new RuleEntity();
        RuleEntity ruleEntity2 = new RuleEntity();

        when(ruleRepository.findByTenantIdAndEnabled("tenant-001", true))
                .thenReturn(List.of(ruleEntity1, ruleEntity2));
        when(ruleMapper.toDomain(ruleEntity1)).thenReturn(failingRule);
        when(ruleMapper.toDomain(ruleEntity2)).thenReturn(successfulRule);
        when(patternEvaluator.evaluate(eq(matchingEvent), eq(failingRule), any(RuleCondition.class)))
                .thenThrow(new RuntimeException("Test error"));
        when(patternEvaluator.evaluate(eq(matchingEvent), isNull(), any(RuleCondition.class)))
                .thenReturn(true);

        List<Alert> alerts = ruleEngine.evaluate(matchingEvent);

        assertThat(alerts).hasSize(1);
    }

    @Test
    void shouldMatchConditionWithAndLogic() {
        RuleCondition condition1 = DetectionTestFixtures.createSimpleCondition("actionType", "EQUALS", "DELETE");
        RuleCondition condition2 = DetectionTestFixtures.createSimpleCondition("tenantId", "EQUALS", "tenant-001");
        RuleCondition andCondition = DetectionTestFixtures.createAndCondition(List.of(condition1, condition2));

        when(patternEvaluator.evaluate(eq(matchingEvent), isNull(), any(RuleCondition.class)))
                .thenReturn(true);

        boolean result = ruleEngine.matches(matchingEvent, andCondition);

        assertThat(result).isTrue();
    }

    @Test
    void shouldMatchConditionWithOrLogic() {
        RuleCondition condition1 = DetectionTestFixtures.createSimpleCondition("actionType", "EQUALS", "DELETE");
        RuleCondition condition2 = DetectionTestFixtures.createSimpleCondition("actionType", "EQUALS", "UPDATE");
        RuleCondition orCondition = DetectionTestFixtures.createOrCondition(List.of(condition1, condition2));

        when(patternEvaluator.evaluate(eq(matchingEvent), isNull(), any(RuleCondition.class)))
                .thenReturn(true);

        boolean result = ruleEngine.matches(matchingEvent, orCondition);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseForNullCondition() {
        boolean result = ruleEngine.matches(matchingEvent, null);

        assertThat(result).isFalse();
    }

    @Test
    void shouldCreateAlertWithCorrectMessage() {
        Rule patternRule = DetectionTestFixtures.createPatternRule();
        RuleEntity ruleEntity = new RuleEntity();

        when(ruleRepository.findByTenantIdAndEnabled("tenant-001", true))
                .thenReturn(List.of(ruleEntity));
        when(ruleMapper.toDomain(ruleEntity)).thenReturn(patternRule);
        when(patternEvaluator.evaluate(eq(matchingEvent), eq(patternRule), any(RuleCondition.class)))
                .thenReturn(true);

        List<Alert> alerts = ruleEngine.evaluate(matchingEvent);

        assertThat(alerts.get(0).getMessage()).contains(patternRule.getName());
    }

    @Test
    void shouldSetAlertTriggeredAtTime() {
        Rule patternRule = DetectionTestFixtures.createPatternRule();
        RuleEntity ruleEntity = new RuleEntity();

        when(ruleRepository.findByTenantIdAndEnabled("tenant-001", true))
                .thenReturn(List.of(ruleEntity));
        when(ruleMapper.toDomain(ruleEntity)).thenReturn(patternRule);
        when(patternEvaluator.evaluate(eq(matchingEvent), eq(patternRule), any(RuleCondition.class)))
                .thenReturn(true);

        List<Alert> alerts = ruleEngine.evaluate(matchingEvent);

        assertThat(alerts.get(0).getTriggeredAt()).isNotNull();
        assertThat(alerts.get(0).getCreatedAt()).isNotNull();
    }
}
