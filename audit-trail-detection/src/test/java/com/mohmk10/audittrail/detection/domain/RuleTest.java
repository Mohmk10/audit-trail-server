package com.mohmk10.audittrail.detection.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RuleTest {

    @Test
    void shouldCreateRuleWithAllFields() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        RuleCondition condition = createSimpleCondition();
        RuleAction action = new RuleAction(AlertType.NOTIFICATION, List.of("LOG"));

        Rule rule = Rule.builder()
                .id(id)
                .name("Test Rule")
                .description("Rule description")
                .tenantId("tenant-001")
                .enabled(true)
                .type(RuleType.PATTERN)
                .condition(condition)
                .action(action)
                .severity(Severity.HIGH)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(rule.getId()).isEqualTo(id);
        assertThat(rule.getName()).isEqualTo("Test Rule");
        assertThat(rule.getDescription()).isEqualTo("Rule description");
        assertThat(rule.getTenantId()).isEqualTo("tenant-001");
        assertThat(rule.isEnabled()).isTrue();
        assertThat(rule.getType()).isEqualTo(RuleType.PATTERN);
        assertThat(rule.getCondition()).isEqualTo(condition);
        assertThat(rule.getAction()).isEqualTo(action);
        assertThat(rule.getSeverity()).isEqualTo(Severity.HIGH);
        assertThat(rule.getCreatedAt()).isEqualTo(now);
        assertThat(rule.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldHaveDefaultEnabledTrue() {
        Rule rule = Rule.builder()
                .id(UUID.randomUUID())
                .name("Test Rule")
                .tenantId("tenant-001")
                .type(RuleType.PATTERN)
                .build();

        assertThat(rule.isEnabled()).isTrue();
    }

    @Test
    void shouldSupportAllRuleTypes() {
        for (RuleType type : RuleType.values()) {
            Rule rule = Rule.builder()
                    .id(UUID.randomUUID())
                    .name("Test Rule")
                    .tenantId("tenant-001")
                    .type(type)
                    .build();

            assertThat(rule.getType()).isEqualTo(type);
        }
    }

    @Test
    void shouldHaveCondition() {
        RuleCondition condition = createSimpleCondition();

        Rule rule = Rule.builder()
                .id(UUID.randomUUID())
                .name("Test Rule")
                .tenantId("tenant-001")
                .type(RuleType.PATTERN)
                .condition(condition)
                .build();

        assertThat(rule.getCondition()).isNotNull();
        assertThat(rule.getCondition().getField()).isEqualTo("actionType");
    }

    @Test
    void shouldHaveAction() {
        RuleAction action = new RuleAction(AlertType.NOTIFICATION, List.of("LOG", "EMAIL"));

        Rule rule = Rule.builder()
                .id(UUID.randomUUID())
                .name("Test Rule")
                .tenantId("tenant-001")
                .type(RuleType.PATTERN)
                .action(action)
                .build();

        assertThat(rule.getAction()).isNotNull();
        assertThat(rule.getAction().getAlertType()).isEqualTo(AlertType.NOTIFICATION);
        assertThat(rule.getAction().getNotificationChannels()).contains("LOG", "EMAIL");
    }

    @Test
    void shouldHaveSeverity() {
        Rule rule = Rule.builder()
                .id(UUID.randomUUID())
                .name("Test Rule")
                .tenantId("tenant-001")
                .type(RuleType.PATTERN)
                .severity(Severity.CRITICAL)
                .build();

        assertThat(rule.getSeverity()).isEqualTo(Severity.CRITICAL);
    }

    @Test
    void shouldUseToBuilder() {
        Rule original = Rule.builder()
                .id(UUID.randomUUID())
                .name("Original Rule")
                .tenantId("tenant-001")
                .type(RuleType.PATTERN)
                .enabled(true)
                .severity(Severity.LOW)
                .build();

        Rule modified = original.toBuilder()
                .name("Modified Rule")
                .severity(Severity.HIGH)
                .enabled(false)
                .build();

        assertThat(modified.getId()).isEqualTo(original.getId());
        assertThat(modified.getName()).isEqualTo("Modified Rule");
        assertThat(modified.getSeverity()).isEqualTo(Severity.HIGH);
        assertThat(modified.isEnabled()).isFalse();
    }

    @Test
    void shouldUseSetters() {
        Rule rule = new Rule();
        UUID id = UUID.randomUUID();

        rule.setId(id);
        rule.setName("Test");
        rule.setDescription("Desc");
        rule.setTenantId("tenant");
        rule.setEnabled(true);
        rule.setType(RuleType.THRESHOLD);
        rule.setSeverity(Severity.MEDIUM);

        assertThat(rule.getId()).isEqualTo(id);
        assertThat(rule.getName()).isEqualTo("Test");
        assertThat(rule.getDescription()).isEqualTo("Desc");
        assertThat(rule.getTenantId()).isEqualTo("tenant");
        assertThat(rule.isEnabled()).isTrue();
        assertThat(rule.getType()).isEqualTo(RuleType.THRESHOLD);
        assertThat(rule.getSeverity()).isEqualTo(Severity.MEDIUM);
    }

    private RuleCondition createSimpleCondition() {
        RuleCondition condition = new RuleCondition();
        condition.setField("actionType");
        condition.setOperator("EQUALS");
        condition.setValue("DELETE");
        return condition;
    }
}
