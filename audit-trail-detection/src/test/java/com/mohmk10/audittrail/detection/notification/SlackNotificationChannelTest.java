package com.mohmk10.audittrail.detection.notification;

import com.mohmk10.audittrail.detection.domain.*;
import com.mohmk10.audittrail.detection.fixtures.DetectionTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlackNotificationChannelTest {

    @Mock
    private RestTemplate restTemplate;

    private SlackNotificationChannel slackChannel;

    @BeforeEach
    void setUp() {
        slackChannel = new SlackNotificationChannel(restTemplate);
    }

    @Test
    void shouldReturnCorrectChannelType() {
        assertThat(slackChannel.getChannelType()).isEqualTo("SLACK");
    }

    @Test
    void shouldSendSlackNotification() {
        Alert alert = createAlertWithRule(Severity.HIGH);
        Map<String, String> params = Map.of("webhookUrl", "https://hooks.slack.com/services/test");

        slackChannel.send(alert, params);

        verify(restTemplate).postForObject(
                eq("https://hooks.slack.com/services/test"),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    void shouldNotSendWhenNoWebhookUrl() {
        Alert alert = createAlertWithRule(Severity.HIGH);
        Map<String, String> params = Map.of();

        slackChannel.send(alert, params);

        verifyNoInteractions(restTemplate);
    }

    @Test
    void shouldNotSendWhenParametersAreNull() {
        Alert alert = createAlertWithRule(Severity.HIGH);

        slackChannel.send(alert, null);

        verifyNoInteractions(restTemplate);
    }

    @Test
    void shouldBuildSlackPayloadWithAlertDetails() {
        Alert alert = createAlertWithRule(Severity.CRITICAL);
        Map<String, String> params = Map.of("webhookUrl", "https://hooks.slack.com/services/test");

        slackChannel.send(alert, params);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<String>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(anyString(), captor.capture(), eq(String.class));

        String payload = captor.getValue().getBody();
        assertThat(payload).contains("CRITICAL");
        assertThat(payload).contains("tenant-001");
    }

    @Test
    void shouldUseCorrectColorForCriticalSeverity() {
        Alert alert = createAlertWithRule(Severity.CRITICAL);
        Map<String, String> params = Map.of("webhookUrl", "https://hooks.slack.com/services/test");

        slackChannel.send(alert, params);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<String>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(anyString(), captor.capture(), eq(String.class));

        String payload = captor.getValue().getBody();
        assertThat(payload).contains("#8B0000");
    }

    @Test
    void shouldUseCorrectColorForHighSeverity() {
        Alert alert = createAlertWithRule(Severity.HIGH);
        Map<String, String> params = Map.of("webhookUrl", "https://hooks.slack.com/services/test");

        slackChannel.send(alert, params);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<String>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(anyString(), captor.capture(), eq(String.class));

        String payload = captor.getValue().getBody();
        assertThat(payload).contains("#FF0000");
    }

    @Test
    void shouldUseCorrectColorForMediumSeverity() {
        Alert alert = createAlertWithRule(Severity.MEDIUM);
        Map<String, String> params = Map.of("webhookUrl", "https://hooks.slack.com/services/test");

        slackChannel.send(alert, params);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<String>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(anyString(), captor.capture(), eq(String.class));

        String payload = captor.getValue().getBody();
        assertThat(payload).contains("#FFA500");
    }

    @Test
    void shouldUseCorrectColorForLowSeverity() {
        Alert alert = createAlertWithRule(Severity.LOW);
        Map<String, String> params = Map.of("webhookUrl", "https://hooks.slack.com/services/test");

        slackChannel.send(alert, params);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<String>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(anyString(), captor.capture(), eq(String.class));

        String payload = captor.getValue().getBody();
        assertThat(payload).contains("#FFFF00");
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

        Map<String, String> params = Map.of("webhookUrl", "https://hooks.slack.com/services/test");

        slackChannel.send(alert, params);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<String>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(anyString(), captor.capture(), eq(String.class));

        String payload = captor.getValue().getBody();
        assertThat(payload).contains("Unknown Rule");
    }

    @Test
    void shouldEscapeJsonInMessage() {
        Alert alert = Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .rule(DetectionTestFixtures.createPatternRule())
                .severity(Severity.HIGH)
                .status(AlertStatus.OPEN)
                .message("Message with \"quotes\" and\nnewlines")
                .triggeredAt(Instant.now())
                .build();

        Map<String, String> params = Map.of("webhookUrl", "https://hooks.slack.com/services/test");

        slackChannel.send(alert, params);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<String>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(anyString(), captor.capture(), eq(String.class));

        String payload = captor.getValue().getBody();
        assertThat(payload).contains("\\\"quotes\\\"");
        assertThat(payload).contains("\\n");
    }

    @Test
    void shouldHandleRestTemplateException() {
        Alert alert = createAlertWithRule(Severity.HIGH);
        Map<String, String> params = Map.of("webhookUrl", "https://hooks.slack.com/services/test");

        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Connection failed"));

        assertThatCode(() -> slackChannel.send(alert, params))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldSetContentTypeToJson() {
        Alert alert = createAlertWithRule(Severity.HIGH);
        Map<String, String> params = Map.of("webhookUrl", "https://hooks.slack.com/services/test");

        slackChannel.send(alert, params);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<String>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(anyString(), captor.capture(), eq(String.class));

        assertThat(captor.getValue().getHeaders().getContentType().toString())
                .contains("application/json");
    }

    private Alert createAlertWithRule(Severity severity) {
        return Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .rule(DetectionTestFixtures.createPatternRule())
                .severity(severity)
                .status(AlertStatus.OPEN)
                .message("Test alert")
                .triggeredAt(Instant.now())
                .build();
    }
}
