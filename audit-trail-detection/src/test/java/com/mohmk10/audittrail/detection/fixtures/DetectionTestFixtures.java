package com.mohmk10.audittrail.detection.fixtures;

import com.mohmk10.audittrail.core.domain.*;
import com.mohmk10.audittrail.detection.domain.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class DetectionTestFixtures {

    private DetectionTestFixtures() {}

    public static Rule createThresholdRule() {
        RuleCondition condition = new RuleCondition();
        condition.setField("actionType");
        condition.setOperator("EQUALS");
        condition.setValue("DELETE");
        condition.setThreshold(5);
        condition.setWindowMinutes(60);

        RuleAction action = new RuleAction(AlertType.NOTIFICATION, List.of("LOG", "EMAIL"));

        return Rule.builder()
                .id(UUID.randomUUID())
                .name("Delete Threshold Alert")
                .description("Alert when more than 5 deletions occur within an hour")
                .tenantId("tenant-001")
                .enabled(true)
                .type(RuleType.THRESHOLD)
                .condition(condition)
                .action(action)
                .severity(Severity.HIGH)
                .createdAt(Instant.now())
                .build();
    }

    public static Rule createBlacklistRule() {
        RuleCondition condition = new RuleCondition();
        condition.setField("actorIp");
        condition.setOperator("IN");
        condition.setValue(List.of("192.168.1.100", "10.0.0.50"));

        RuleAction action = new RuleAction(AlertType.BLOCK, List.of("LOG", "SLACK"));

        return Rule.builder()
                .id(UUID.randomUUID())
                .name("Blacklisted IP Detection")
                .description("Block access from blacklisted IPs")
                .tenantId("tenant-001")
                .enabled(true)
                .type(RuleType.BLACKLIST)
                .condition(condition)
                .action(action)
                .severity(Severity.CRITICAL)
                .createdAt(Instant.now())
                .build();
    }

    public static Rule createPatternRule() {
        RuleCondition condition = new RuleCondition();
        condition.setField("actionType");
        condition.setOperator("EQUALS");
        condition.setValue("DELETE");

        RuleAction action = new RuleAction(AlertType.NOTIFICATION, List.of("LOG"));

        return Rule.builder()
                .id(UUID.randomUUID())
                .name("Delete Pattern")
                .description("Alert on any delete action")
                .tenantId("tenant-001")
                .enabled(true)
                .type(RuleType.PATTERN)
                .condition(condition)
                .action(action)
                .severity(Severity.MEDIUM)
                .createdAt(Instant.now())
                .build();
    }

    public static RuleCondition createSimpleCondition(String field, String operator, Object value) {
        RuleCondition condition = new RuleCondition();
        condition.setField(field);
        condition.setOperator(operator);
        condition.setValue(value);
        return condition;
    }

    public static RuleCondition createAndCondition(List<RuleCondition> conditions) {
        RuleCondition condition = new RuleCondition();
        condition.setAnd(conditions);
        return condition;
    }

    public static RuleCondition createOrCondition(List<RuleCondition> conditions) {
        RuleCondition condition = new RuleCondition();
        condition.setOr(conditions);
        return condition;
    }

    public static Alert createTestAlert() {
        Rule rule = createPatternRule();

        return Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .rule(rule)
                .ruleId(rule.getId())
                .severity(Severity.MEDIUM)
                .status(AlertStatus.OPEN)
                .message("Test alert triggered")
                .triggeringEventIds(List.of(UUID.randomUUID()))
                .triggeredAt(Instant.now())
                .createdAt(Instant.now())
                .build();
    }

    public static Alert createAcknowledgedAlert() {
        Alert alert = createTestAlert();
        return alert.toBuilder()
                .status(AlertStatus.ACKNOWLEDGED)
                .acknowledgedAt(Instant.now())
                .acknowledgedBy("admin@example.com")
                .build();
    }

    public static Alert createResolvedAlert() {
        Alert alert = createTestAlert();
        return alert.toBuilder()
                .status(AlertStatus.RESOLVED)
                .acknowledgedAt(Instant.now().minusSeconds(3600))
                .acknowledgedBy("admin@example.com")
                .resolvedAt(Instant.now())
                .resolution("Issue investigated and resolved")
                .build();
    }

    public static Event createMatchingEvent() {
        return new Event(
                UUID.randomUUID(),
                Instant.now(),
                new Actor(
                        "actor-123",
                        Actor.ActorType.USER,
                        "John Doe",
                        "192.168.1.100",
                        null,
                        null
                ),
                new Action(
                        Action.ActionType.DELETE,
                        "Deleted sensitive document",
                        "DOCUMENTS"
                ),
                new Resource(
                        "doc-456",
                        Resource.ResourceType.DOCUMENT,
                        "Sensitive Data.pdf",
                        null,
                        null
                ),
                new EventMetadata(
                        "web-app",
                        "tenant-001",
                        "corr-789",
                        "session-abc",
                        null,
                        null
                ),
                null,
                "hash123",
                null
        );
    }

    public static Event createNonMatchingEvent() {
        return new Event(
                UUID.randomUUID(),
                Instant.now(),
                new Actor(
                        "actor-456",
                        Actor.ActorType.USER,
                        "Jane Doe",
                        "192.168.1.50",
                        null,
                        null
                ),
                new Action(
                        Action.ActionType.READ,
                        "Read document",
                        "DOCUMENTS"
                ),
                new Resource(
                        "doc-789",
                        Resource.ResourceType.DOCUMENT,
                        "Public Info.pdf",
                        null,
                        null
                ),
                new EventMetadata(
                        "web-app",
                        "tenant-001",
                        "corr-123",
                        "session-xyz",
                        null,
                        null
                ),
                null,
                "hash456",
                null
        );
    }
}
