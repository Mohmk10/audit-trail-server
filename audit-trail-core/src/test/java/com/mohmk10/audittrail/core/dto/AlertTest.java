package com.mohmk10.audittrail.core.dto;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.fixtures.TestFixtures;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AlertTest {

    @Test
    void shouldCreateAlertWithSeverity() {
        UUID id = UUID.randomUUID();
        Instant timestamp = Instant.now();

        Alert alert = new Alert(
                id,
                "SUSPICIOUS_ACTIVITY",
                Alert.Severity.HIGH,
                "Suspicious login attempt detected",
                null,
                timestamp
        );

        assertThat(alert.severity()).isEqualTo(Alert.Severity.HIGH);
    }

    @Test
    void shouldCreateAlertWithMessage() {
        UUID id = UUID.randomUUID();
        String message = "Multiple failed login attempts from IP 192.168.1.100";

        Alert alert = new Alert(
                id,
                "LOGIN_FAILURE",
                Alert.Severity.MEDIUM,
                message,
                null,
                Instant.now()
        );

        assertThat(alert.message()).isEqualTo(message);
    }

    @Test
    void shouldLinkToEvent() {
        UUID alertId = UUID.randomUUID();
        Event event = TestFixtures.createTestEvent();

        Alert alert = new Alert(
                alertId,
                "DATA_ACCESS",
                Alert.Severity.LOW,
                "Unusual data access pattern",
                event,
                Instant.now()
        );

        assertThat(alert.event()).isNotNull();
        assertThat(alert.event()).isEqualTo(event);
    }

    @Test
    void shouldCreateAlertWithAllFields() {
        UUID id = UUID.randomUUID();
        Instant timestamp = Instant.now();
        Event event = TestFixtures.createTestEvent();

        Alert alert = new Alert(
                id,
                "SECURITY_BREACH",
                Alert.Severity.CRITICAL,
                "Potential security breach detected",
                event,
                timestamp
        );

        assertThat(alert.id()).isEqualTo(id);
        assertThat(alert.type()).isEqualTo("SECURITY_BREACH");
        assertThat(alert.severity()).isEqualTo(Alert.Severity.CRITICAL);
        assertThat(alert.message()).isEqualTo("Potential security breach detected");
        assertThat(alert.event()).isEqualTo(event);
        assertThat(alert.timestamp()).isEqualTo(timestamp);
    }

    @Test
    void shouldSupportAllSeverityLevels() {
        assertThat(Alert.Severity.LOW).isNotNull();
        assertThat(Alert.Severity.MEDIUM).isNotNull();
        assertThat(Alert.Severity.HIGH).isNotNull();
        assertThat(Alert.Severity.CRITICAL).isNotNull();
    }

    @Test
    void shouldHaveCorrectSeverityValues() {
        assertThat(Alert.Severity.values()).hasSize(4);
        assertThat(Alert.Severity.valueOf("LOW")).isEqualTo(Alert.Severity.LOW);
        assertThat(Alert.Severity.valueOf("MEDIUM")).isEqualTo(Alert.Severity.MEDIUM);
        assertThat(Alert.Severity.valueOf("HIGH")).isEqualTo(Alert.Severity.HIGH);
        assertThat(Alert.Severity.valueOf("CRITICAL")).isEqualTo(Alert.Severity.CRITICAL);
    }

    @Test
    void shouldHandleNullEvent() {
        Alert alert = new Alert(
                UUID.randomUUID(),
                "SYSTEM_ALERT",
                Alert.Severity.LOW,
                "System notification",
                null,
                Instant.now()
        );

        assertThat(alert.event()).isNull();
    }

    @Test
    void shouldSupportRecordEquality() {
        UUID id = UUID.randomUUID();
        Instant timestamp = Instant.now();

        Alert alert1 = new Alert(id, "TYPE", Alert.Severity.LOW, "msg", null, timestamp);
        Alert alert2 = new Alert(id, "TYPE", Alert.Severity.LOW, "msg", null, timestamp);

        assertThat(alert1).isEqualTo(alert2);
        assertThat(alert1.hashCode()).isEqualTo(alert2.hashCode());
    }

    @Test
    void shouldDifferentiateNonEqualAlerts() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Instant timestamp = Instant.now();

        Alert alert1 = new Alert(id1, "TYPE", Alert.Severity.LOW, "msg", null, timestamp);
        Alert alert2 = new Alert(id2, "TYPE", Alert.Severity.LOW, "msg", null, timestamp);

        assertThat(alert1).isNotEqualTo(alert2);
    }

    @Test
    void shouldPreserveTimestamp() {
        Instant expectedTimestamp = Instant.parse("2024-06-15T14:30:00Z");

        Alert alert = new Alert(
                UUID.randomUUID(),
                "AUDIT",
                Alert.Severity.MEDIUM,
                "Audit alert",
                null,
                expectedTimestamp
        );

        assertThat(alert.timestamp()).isEqualTo(expectedTimestamp);
    }
}
