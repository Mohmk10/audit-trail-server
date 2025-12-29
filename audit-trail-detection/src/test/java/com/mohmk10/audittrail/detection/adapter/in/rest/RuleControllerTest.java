package com.mohmk10.audittrail.detection.adapter.in.rest;

import com.mohmk10.audittrail.detection.adapter.in.rest.dto.*;
import com.mohmk10.audittrail.detection.domain.*;
import com.mohmk10.audittrail.detection.fixtures.DetectionTestFixtures;
import com.mohmk10.audittrail.detection.service.RuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleControllerTest {

    @Mock
    private RuleService ruleService;

    @Mock
    private RuleDtoMapper mapper;

    private RuleController controller;

    @BeforeEach
    void setUp() {
        controller = new RuleController(ruleService, mapper);
    }

    @Test
    void shouldCreateRule() {
        RuleConditionDto conditionDto = new RuleConditionDto(
                "actionType", "EQUALS", "DELETE", null, null, null, null);
        RuleActionDto actionDto = new RuleActionDto(
                AlertType.NOTIFICATION, List.of("LOG"), null);

        CreateRuleRequest request = new CreateRuleRequest(
                "Test Rule",
                "Description",
                "tenant-001",
                RuleType.PATTERN,
                conditionDto,
                actionDto,
                Severity.HIGH
        );

        Rule rule = DetectionTestFixtures.createPatternRule();
        RuleResponse response = createRuleResponse(rule);

        when(mapper.toDomain(request)).thenReturn(rule);
        when(ruleService.create(rule)).thenReturn(rule);
        when(mapper.toResponse(rule)).thenReturn(response);

        ResponseEntity<RuleResponse> result = controller.create(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().name()).isEqualTo(rule.getName());
    }

    @Test
    void shouldListRulesByTenantId() {
        Rule rule1 = DetectionTestFixtures.createPatternRule();
        Rule rule2 = DetectionTestFixtures.createThresholdRule();
        RuleResponse response1 = createRuleResponse(rule1);
        RuleResponse response2 = createRuleResponse(rule2);

        when(ruleService.findByTenantId("tenant-001")).thenReturn(List.of(rule1, rule2));
        when(mapper.toResponse(rule1)).thenReturn(response1);
        when(mapper.toResponse(rule2)).thenReturn(response2);

        ResponseEntity<List<RuleResponse>> result = controller.list("tenant-001");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).hasSize(2);
    }

    @Test
    void shouldReturnEmptyListWhenNoRules() {
        when(ruleService.findByTenantId("tenant-001")).thenReturn(List.of());

        ResponseEntity<List<RuleResponse>> result = controller.list("tenant-001");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEmpty();
    }

    @Test
    void shouldGetRuleById() {
        UUID ruleId = UUID.randomUUID();
        Rule rule = DetectionTestFixtures.createPatternRule();
        RuleResponse response = createRuleResponse(rule);

        when(ruleService.findById(ruleId)).thenReturn(Optional.of(rule));
        when(mapper.toResponse(rule)).thenReturn(response);

        ResponseEntity<RuleResponse> result = controller.getById(ruleId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
    }

    @Test
    void shouldReturnNotFoundWhenRuleDoesNotExist() {
        UUID ruleId = UUID.randomUUID();

        when(ruleService.findById(ruleId)).thenReturn(Optional.empty());

        ResponseEntity<RuleResponse> result = controller.getById(ruleId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldUpdateRule() {
        UUID ruleId = UUID.randomUUID();
        Rule existingRule = DetectionTestFixtures.createPatternRule();
        UpdateRuleRequest request = new UpdateRuleRequest(
                "Updated Rule Name",
                "Updated description",
                true,
                null,
                null,
                Severity.CRITICAL
        );
        Rule updatedRule = existingRule.toBuilder()
                .name("Updated Rule Name")
                .description("Updated description")
                .severity(Severity.CRITICAL)
                .build();
        RuleResponse response = createRuleResponse(updatedRule);

        when(ruleService.findById(ruleId)).thenReturn(Optional.of(existingRule));
        when(mapper.applyUpdate(existingRule, request)).thenReturn(updatedRule);
        when(ruleService.update(eq(ruleId), any(Rule.class))).thenReturn(updatedRule);
        when(mapper.toResponse(updatedRule)).thenReturn(response);

        ResponseEntity<RuleResponse> result = controller.update(ruleId, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().name()).isEqualTo("Updated Rule Name");
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentRule() {
        UUID ruleId = UUID.randomUUID();
        UpdateRuleRequest request = new UpdateRuleRequest(
                "Updated", null, null, null, null, null);

        when(ruleService.findById(ruleId)).thenReturn(Optional.empty());

        ResponseEntity<RuleResponse> result = controller.update(ruleId, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldDeleteRule() {
        UUID ruleId = UUID.randomUUID();

        ResponseEntity<Void> result = controller.delete(ruleId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(ruleService).delete(ruleId);
    }

    @Test
    void shouldEnableRule() {
        UUID ruleId = UUID.randomUUID();
        Rule rule = DetectionTestFixtures.createPatternRule();
        RuleResponse response = createRuleResponse(rule);

        when(ruleService.findById(ruleId)).thenReturn(Optional.of(rule));
        when(mapper.toResponse(rule)).thenReturn(response);

        ResponseEntity<RuleResponse> result = controller.enable(ruleId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(ruleService).enable(ruleId);
    }

    @Test
    void shouldReturnNotFoundWhenEnablingNonExistentRule() {
        UUID ruleId = UUID.randomUUID();

        when(ruleService.findById(ruleId)).thenReturn(Optional.empty());

        ResponseEntity<RuleResponse> result = controller.enable(ruleId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldDisableRule() {
        UUID ruleId = UUID.randomUUID();
        Rule rule = DetectionTestFixtures.createPatternRule().toBuilder().enabled(false).build();
        RuleResponse response = createRuleResponse(rule);

        when(ruleService.findById(ruleId)).thenReturn(Optional.of(rule));
        when(mapper.toResponse(rule)).thenReturn(response);

        ResponseEntity<RuleResponse> result = controller.disable(ruleId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(ruleService).disable(ruleId);
    }

    @Test
    void shouldReturnNotFoundWhenDisablingNonExistentRule() {
        UUID ruleId = UUID.randomUUID();

        when(ruleService.findById(ruleId)).thenReturn(Optional.empty());

        ResponseEntity<RuleResponse> result = controller.disable(ruleId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private RuleResponse createRuleResponse(Rule rule) {
        RuleConditionDto conditionDto = null;
        if (rule.getCondition() != null) {
            conditionDto = new RuleConditionDto(
                    rule.getCondition().getField(),
                    rule.getCondition().getOperator(),
                    rule.getCondition().getValue(),
                    rule.getCondition().getThreshold(),
                    rule.getCondition().getWindowMinutes(),
                    null, null
            );
        }

        RuleActionDto actionDto = null;
        if (rule.getAction() != null) {
            actionDto = new RuleActionDto(
                    rule.getAction().getAlertType(),
                    rule.getAction().getNotificationChannels(),
                    rule.getAction().getParameters()
            );
        }

        return new RuleResponse(
                rule.getId(),
                rule.getName(),
                rule.getDescription(),
                rule.getTenantId(),
                rule.isEnabled(),
                rule.getType(),
                conditionDto,
                actionDto,
                rule.getSeverity(),
                Instant.now(),
                Instant.now()
        );
    }
}
