package com.mohmk10.audittrail.detection.notification;

import com.mohmk10.audittrail.detection.domain.*;
import com.mohmk10.audittrail.detection.fixtures.DetectionTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationChannelTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailNotificationChannel emailChannel;

    @BeforeEach
    void setUp() {
        emailChannel = new EmailNotificationChannel(mailSender, "alerts@audittrail.local");
    }

    @Test
    void shouldReturnCorrectChannelType() {
        assertThat(emailChannel.getChannelType()).isEqualTo("EMAIL");
    }

    @Test
    void shouldSendEmailWithCorrectRecipient() {
        Alert alert = createAlertWithRule();
        Map<String, String> params = Map.of("to", "admin@example.com");

        emailChannel.send(alert, params);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertThat(message.getTo()).contains("admin@example.com");
        assertThat(message.getFrom()).isEqualTo("alerts@audittrail.local");
    }

    @Test
    void shouldSetCorrectSubjectWithSeverity() {
        Rule rule = DetectionTestFixtures.createPatternRule();
        Alert alert = Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .rule(rule)
                .severity(Severity.CRITICAL)
                .status(AlertStatus.OPEN)
                .message("Critical alert")
                .triggeredAt(Instant.now())
                .build();

        Map<String, String> params = Map.of("to", "admin@example.com");

        emailChannel.send(alert, params);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertThat(message.getSubject()).contains("CRITICAL");
        assertThat(message.getSubject()).contains(rule.getName());
    }

    @Test
    void shouldIncludeCcRecipientsWhenProvided() {
        Alert alert = createAlertWithRule();
        Map<String, String> params = Map.of(
                "to", "admin@example.com",
                "cc", "manager@example.com,security@example.com"
        );

        emailChannel.send(alert, params);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertThat(message.getCc()).contains("manager@example.com", "security@example.com");
    }

    @Test
    void shouldNotSendWhenNoRecipientProvided() {
        Alert alert = createAlertWithRule();
        Map<String, String> params = Map.of();

        emailChannel.send(alert, params);

        verifyNoInteractions(mailSender);
    }

    @Test
    void shouldNotSendWhenParametersAreNull() {
        Alert alert = createAlertWithRule();

        emailChannel.send(alert, null);

        verifyNoInteractions(mailSender);
    }

    @Test
    void shouldBuildEmailBodyWithAlertDetails() {
        Alert alert = Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .rule(DetectionTestFixtures.createPatternRule())
                .severity(Severity.HIGH)
                .status(AlertStatus.OPEN)
                .message("Test alert message")
                .triggeringEventIds(List.of(UUID.randomUUID(), UUID.randomUUID()))
                .triggeredAt(Instant.now())
                .build();

        Map<String, String> params = Map.of("to", "admin@example.com");

        emailChannel.send(alert, params);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        String body = captor.getValue().getText();
        assertThat(body).contains("AUDIT TRAIL ALERT");
        assertThat(body).contains(alert.getId().toString());
        assertThat(body).contains("HIGH");
        assertThat(body).contains("tenant-001");
        assertThat(body).contains("Test alert message");
        assertThat(body).contains("Triggering Events: 2");
    }

    @Test
    void shouldHandleAlertWithoutRule() {
        Alert alert = Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .severity(Severity.MEDIUM)
                .status(AlertStatus.OPEN)
                .message("Alert without rule")
                .triggeredAt(Instant.now())
                .build();

        Map<String, String> params = Map.of("to", "admin@example.com");

        emailChannel.send(alert, params);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        assertThat(captor.getValue().getSubject()).contains("Unknown Rule");
    }

    @Test
    void shouldHandleMailSenderException() {
        Alert alert = createAlertWithRule();
        Map<String, String> params = Map.of("to", "admin@example.com");

        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> emailChannel.send(alert, params))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldIgnoreBlankCcRecipients() {
        Alert alert = createAlertWithRule();
        Map<String, String> params = Map.of(
                "to", "admin@example.com",
                "cc", "   "
        );

        emailChannel.send(alert, params);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        assertThat(captor.getValue().getCc()).isNull();
    }

    private Alert createAlertWithRule() {
        return Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .rule(DetectionTestFixtures.createPatternRule())
                .severity(Severity.HIGH)
                .status(AlertStatus.OPEN)
                .message("Test alert")
                .triggeredAt(Instant.now())
                .build();
    }
}
