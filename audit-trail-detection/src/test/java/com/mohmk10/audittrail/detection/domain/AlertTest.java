package com.mohmk10.audittrail.detection.domain;

import com.mohmk10.audittrail.detection.fixtures.DetectionTestFixtures;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AlertTest {

    @Test
    void shouldCreateAlertWithAllFields() {
        UUID id = UUID.randomUUID();
        UUID ruleId = UUID.randomUUID();
        Rule rule = DetectionTestFixtures.createPatternRule();
        Instant now = Instant.now();
        List<UUID> eventIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        Alert alert = Alert.builder()
                .id(id)
                .tenantId("tenant-001")
                .rule(rule)
                .ruleId(ruleId)
                .severity(Severity.HIGH)
                .status(AlertStatus.OPEN)
                .message("Test alert")
                .triggeringEventIds(eventIds)
                .triggeredAt(now)
                .createdAt(now)
                .build();

        assertThat(alert.getId()).isEqualTo(id);
        assertThat(alert.getTenantId()).isEqualTo("tenant-001");
        assertThat(alert.getRule()).isEqualTo(rule);
        assertThat(alert.getRuleId()).isEqualTo(ruleId);
        assertThat(alert.getSeverity()).isEqualTo(Severity.HIGH);
        assertThat(alert.getStatus()).isEqualTo(AlertStatus.OPEN);
        assertThat(alert.getMessage()).isEqualTo("Test alert");
        assertThat(alert.getTriggeringEventIds()).hasSize(2);
        assertThat(alert.getTriggeredAt()).isEqualTo(now);
        assertThat(alert.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void shouldHaveDefaultStatusOpen() {
        Alert alert = Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .severity(Severity.LOW)
                .message("Test")
                .build();

        assertThat(alert.getStatus()).isEqualTo(AlertStatus.OPEN);
    }

    @Test
    void shouldLinkToRule() {
        Rule rule = DetectionTestFixtures.createThresholdRule();

        Alert alert = Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .rule(rule)
                .ruleId(rule.getId())
                .severity(Severity.HIGH)
                .message("Rule triggered")
                .build();

        assertThat(alert.getRule()).isNotNull();
        assertThat(alert.getRuleId()).isEqualTo(rule.getId());
        assertThat(alert.getRule().getName()).isEqualTo("Delete Threshold Alert");
    }

    @Test
    void shouldStoreTriggeringEventIds() {
        List<UUID> eventIds = List.of(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        Alert alert = Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .severity(Severity.MEDIUM)
                .message("Multiple events triggered")
                .triggeringEventIds(eventIds)
                .build();

        assertThat(alert.getTriggeringEventIds()).hasSize(3);
        assertThat(alert.getTriggeringEventIds()).containsAll(eventIds);
    }

    @Test
    void shouldTrackAcknowledgement() {
        Instant acknowledgedAt = Instant.now();

        Alert alert = Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .severity(Severity.HIGH)
                .status(AlertStatus.ACKNOWLEDGED)
                .message("Alert acknowledged")
                .acknowledgedAt(acknowledgedAt)
                .acknowledgedBy("admin@example.com")
                .build();

        assertThat(alert.getStatus()).isEqualTo(AlertStatus.ACKNOWLEDGED);
        assertThat(alert.getAcknowledgedAt()).isEqualTo(acknowledgedAt);
        assertThat(alert.getAcknowledgedBy()).isEqualTo("admin@example.com");
    }

    @Test
    void shouldTrackResolution() {
        Instant resolvedAt = Instant.now();

        Alert alert = Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .severity(Severity.CRITICAL)
                .status(AlertStatus.RESOLVED)
                .message("Critical issue")
                .resolution("Root cause identified and fixed")
                .resolvedAt(resolvedAt)
                .build();

        assertThat(alert.getStatus()).isEqualTo(AlertStatus.RESOLVED);
        assertThat(alert.getResolution()).isEqualTo("Root cause identified and fixed");
        assertThat(alert.getResolvedAt()).isEqualTo(resolvedAt);
    }

    @Test
    void shouldUseToBuilder() {
        Alert original = Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .severity(Severity.LOW)
                .status(AlertStatus.OPEN)
                .message("Original message")
                .triggeredAt(Instant.now())
                .build();

        Alert modified = original.toBuilder()
                .status(AlertStatus.ACKNOWLEDGED)
                .acknowledgedAt(Instant.now())
                .acknowledgedBy("user@example.com")
                .build();

        assertThat(modified.getId()).isEqualTo(original.getId());
        assertThat(modified.getStatus()).isEqualTo(AlertStatus.ACKNOWLEDGED);
        assertThat(modified.getAcknowledgedBy()).isEqualTo("user@example.com");
    }

    @Test
    void shouldUseSetters() {
        Alert alert = new Alert();
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        alert.setId(id);
        alert.setTenantId("tenant-001");
        alert.setSeverity(Severity.HIGH);
        alert.setStatus(AlertStatus.OPEN);
        alert.setMessage("Test message");
        alert.setTriggeredAt(now);
        alert.setCreatedAt(now);

        assertThat(alert.getId()).isEqualTo(id);
        assertThat(alert.getTenantId()).isEqualTo("tenant-001");
        assertThat(alert.getSeverity()).isEqualTo(Severity.HIGH);
        assertThat(alert.getStatus()).isEqualTo(AlertStatus.OPEN);
        assertThat(alert.getMessage()).isEqualTo("Test message");
    }

    @Test
    void shouldSupportDismissedStatus() {
        Alert alert = Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .severity(Severity.LOW)
                .status(AlertStatus.DISMISSED)
                .message("False positive")
                .build();

        assertThat(alert.getStatus()).isEqualTo(AlertStatus.DISMISSED);
    }
}
