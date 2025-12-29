package com.mohmk10.audittrail.detection.notification;

import com.mohmk10.audittrail.detection.domain.Alert;
import com.mohmk10.audittrail.detection.domain.AlertStatus;
import com.mohmk10.audittrail.detection.domain.Severity;
import com.mohmk10.audittrail.detection.fixtures.DetectionTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class LogNotificationChannelTest {

    private LogNotificationChannel logNotificationChannel;

    @BeforeEach
    void setUp() {
        logNotificationChannel = new LogNotificationChannel();
    }

    @Test
    void shouldReturnCorrectChannelType() {
        assertThat(logNotificationChannel.getChannelType()).isEqualTo("LOG");
    }

    @Test
    void shouldSendAlertSuccessfully() {
        Alert alert = DetectionTestFixtures.createTestAlert();

        assertThatCode(() -> logNotificationChannel.send(alert, Map.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldSendAlertWithNullParameters() {
        Alert alert = DetectionTestFixtures.createTestAlert();

        assertThatCode(() -> logNotificationChannel.send(alert, null))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldSendCriticalAlert() {
        Alert alert = Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .severity(Severity.CRITICAL)
                .status(AlertStatus.OPEN)
                .message("Critical security breach detected")
                .build();

        assertThatCode(() -> logNotificationChannel.send(alert, Map.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldSendAlertWithRule() {
        Alert alert = DetectionTestFixtures.createTestAlert();

        assertThatCode(() -> logNotificationChannel.send(alert, Map.of()))
                .doesNotThrowAnyException();

        assertThat(alert.getRule()).isNotNull();
    }

    @Test
    void shouldSendAlertWithTriggeringEvents() {
        Alert alert = Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .severity(Severity.HIGH)
                .status(AlertStatus.OPEN)
                .message("Multiple events detected")
                .triggeringEventIds(java.util.List.of(UUID.randomUUID(), UUID.randomUUID()))
                .build();

        assertThatCode(() -> logNotificationChannel.send(alert, Map.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldHandleAllSeverityLevels() {
        for (Severity severity : Severity.values()) {
            Alert alert = Alert.builder()
                    .id(UUID.randomUUID())
                    .tenantId("tenant-001")
                    .severity(severity)
                    .status(AlertStatus.OPEN)
                    .message("Test alert for " + severity)
                    .build();

            assertThatCode(() -> logNotificationChannel.send(alert, Map.of()))
                    .doesNotThrowAnyException();
        }
    }
}
