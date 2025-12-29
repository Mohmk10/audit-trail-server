package com.mohmk10.audittrail.detection.service;

import com.mohmk10.audittrail.detection.domain.*;
import com.mohmk10.audittrail.detection.fixtures.DetectionTestFixtures;
import com.mohmk10.audittrail.detection.notification.NotificationChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationChannel logChannel;

    @Mock
    private NotificationChannel emailChannel;

    @Mock
    private NotificationChannel slackChannel;

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        lenient().when(logChannel.getChannelType()).thenReturn("LOG");
        lenient().when(emailChannel.getChannelType()).thenReturn("EMAIL");
        lenient().when(slackChannel.getChannelType()).thenReturn("SLACK");

        notificationService = new NotificationServiceImpl(
                List.of(logChannel, emailChannel, slackChannel));

        // Reset interaction counts after constructor setup
        clearInvocations(logChannel, emailChannel, slackChannel);
    }

    @Test
    void shouldSendNotificationToConfiguredChannels() {
        RuleAction action = new RuleAction(AlertType.NOTIFICATION, List.of("LOG", "EMAIL"));
        Rule rule = Rule.builder()
                .id(UUID.randomUUID())
                .name("Test Rule")
                .action(action)
                .build();

        Alert alert = Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .rule(rule)
                .severity(Severity.HIGH)
                .status(AlertStatus.OPEN)
                .message("Test alert")
                .build();

        notificationService.notify(alert);

        verify(logChannel).send(eq(alert), any());
        verify(emailChannel).send(eq(alert), any());
        verifyNoInteractions(slackChannel);
    }

    @Test
    void shouldDoNothingWhenNoRuleConfigured() {
        Alert alert = Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .severity(Severity.HIGH)
                .status(AlertStatus.OPEN)
                .message("Test alert")
                .build();

        notificationService.notify(alert);

        verifyNoInteractions(logChannel);
        verifyNoInteractions(emailChannel);
        verifyNoInteractions(slackChannel);
    }

    @Test
    void shouldDoNothingWhenNoActionConfigured() {
        Rule rule = Rule.builder()
                .id(UUID.randomUUID())
                .name("Test Rule")
                .build();

        Alert alert = Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .rule(rule)
                .severity(Severity.HIGH)
                .status(AlertStatus.OPEN)
                .message("Test alert")
                .build();

        notificationService.notify(alert);

        verifyNoInteractions(logChannel);
        verifyNoInteractions(emailChannel);
        verifyNoInteractions(slackChannel);
    }

    @Test
    void shouldDoNothingWhenNoChannelsConfigured() {
        RuleAction action = new RuleAction(AlertType.NOTIFICATION, List.of());
        Rule rule = Rule.builder()
                .id(UUID.randomUUID())
                .name("Test Rule")
                .action(action)
                .build();

        Alert alert = Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .rule(rule)
                .severity(Severity.HIGH)
                .status(AlertStatus.OPEN)
                .message("Test alert")
                .build();

        notificationService.notify(alert);

        verifyNoInteractions(logChannel);
        verifyNoInteractions(emailChannel);
        verifyNoInteractions(slackChannel);
    }

    @Test
    void shouldIgnoreUnknownChannel() {
        RuleAction action = new RuleAction(AlertType.NOTIFICATION, List.of("LOG", "UNKNOWN"));
        Rule rule = Rule.builder()
                .id(UUID.randomUUID())
                .name("Test Rule")
                .action(action)
                .build();

        Alert alert = Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .rule(rule)
                .severity(Severity.HIGH)
                .status(AlertStatus.OPEN)
                .message("Test alert")
                .build();

        notificationService.notify(alert);

        verify(logChannel).send(eq(alert), any());
        verifyNoInteractions(emailChannel);
        verifyNoInteractions(slackChannel);
    }

    @Test
    void shouldPassParametersToChannels() {
        Map<String, String> params = Map.of("to", "admin@example.com");
        RuleAction action = new RuleAction(AlertType.NOTIFICATION, List.of("EMAIL"));
        action.setParameters(params);
        Rule rule = Rule.builder()
                .id(UUID.randomUUID())
                .name("Test Rule")
                .action(action)
                .build();

        Alert alert = Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .rule(rule)
                .severity(Severity.HIGH)
                .status(AlertStatus.OPEN)
                .message("Test alert")
                .build();

        notificationService.notify(alert);

        verify(emailChannel).send(alert, params);
    }

    @Test
    void shouldContinueOnChannelError() {
        RuleAction action = new RuleAction(AlertType.NOTIFICATION, List.of("LOG", "EMAIL", "SLACK"));
        Rule rule = Rule.builder()
                .id(UUID.randomUUID())
                .name("Test Rule")
                .action(action)
                .build();

        Alert alert = Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .rule(rule)
                .severity(Severity.HIGH)
                .status(AlertStatus.OPEN)
                .message("Test alert")
                .build();

        doThrow(new RuntimeException("Email error")).when(emailChannel).send(any(), any());

        notificationService.notify(alert);

        verify(logChannel).send(eq(alert), any());
        verify(emailChannel).send(eq(alert), any());
        verify(slackChannel).send(eq(alert), any());
    }

    @Test
    void shouldHandleCaseInsensitiveChannelType() {
        RuleAction action = new RuleAction(AlertType.NOTIFICATION, List.of("log", "email"));
        Rule rule = Rule.builder()
                .id(UUID.randomUUID())
                .name("Test Rule")
                .action(action)
                .build();

        Alert alert = Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .rule(rule)
                .severity(Severity.HIGH)
                .status(AlertStatus.OPEN)
                .message("Test alert")
                .build();

        notificationService.notify(alert);

        verify(logChannel).send(eq(alert), any());
        verify(emailChannel).send(eq(alert), any());
    }

    @Test
    void shouldNotifyWithTestFixtureAlert() {
        Alert alert = DetectionTestFixtures.createTestAlert();

        notificationService.notify(alert);

        verify(logChannel).send(eq(alert), any());
    }

    @Test
    void shouldHandleNullNotificationChannels() {
        RuleAction action = new RuleAction(AlertType.NOTIFICATION, null);
        Rule rule = Rule.builder()
                .id(UUID.randomUUID())
                .name("Test Rule")
                .action(action)
                .build();

        Alert alert = Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .rule(rule)
                .severity(Severity.HIGH)
                .status(AlertStatus.OPEN)
                .message("Test alert")
                .build();

        notificationService.notify(alert);

        verifyNoInteractions(logChannel);
        verifyNoInteractions(emailChannel);
        verifyNoInteractions(slackChannel);
    }
}
