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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookNotificationChannelTest {

    @Mock
    private RestTemplate restTemplate;

    private WebhookNotificationChannel webhookChannel;

    @BeforeEach
    void setUp() {
        webhookChannel = new WebhookNotificationChannel(restTemplate);
    }

    @Test
    void shouldReturnCorrectChannelType() {
        assertThat(webhookChannel.getChannelType()).isEqualTo("WEBHOOK");
    }

    @Test
    void shouldSendWebhookNotification() {
        Alert alert = createAlertWithRule();
        Map<String, String> params = Map.of("url", "https://api.example.com/webhook");

        webhookChannel.send(alert, params);

        verify(restTemplate).postForObject(
                eq("https://api.example.com/webhook"),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    void shouldNotSendWhenNoUrl() {
        Alert alert = createAlertWithRule();
        Map<String, String> params = Map.of();

        webhookChannel.send(alert, params);

        verifyNoInteractions(restTemplate);
    }

    @Test
    void shouldNotSendWhenParametersAreNull() {
        Alert alert = createAlertWithRule();

        webhookChannel.send(alert, null);

        verifyNoInteractions(restTemplate);
    }

    @Test
    void shouldBuildPayloadWithAlertDetails() {
        UUID alertId = UUID.randomUUID();
        UUID eventId1 = UUID.randomUUID();
        UUID eventId2 = UUID.randomUUID();

        Alert alert = Alert.builder()
                .id(alertId)
                .tenantId("tenant-001")
                .rule(DetectionTestFixtures.createPatternRule())
                .severity(Severity.HIGH)
                .status(AlertStatus.OPEN)
                .message("Test webhook alert")
                .triggeringEventIds(List.of(eventId1, eventId2))
                .triggeredAt(Instant.now())
                .build();

        Map<String, String> params = Map.of("url", "https://api.example.com/webhook");

        webhookChannel.send(alert, params);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<String>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(anyString(), captor.capture(), eq(String.class));

        String payload = captor.getValue().getBody();
        assertThat(payload).contains(alertId.toString());
        assertThat(payload).contains("tenant-001");
        assertThat(payload).contains("HIGH");
        assertThat(payload).contains("OPEN");
        assertThat(payload).contains("Test webhook alert");
    }

    @Test
    void shouldIncludeRuleInfoInPayload() {
        Rule rule = DetectionTestFixtures.createPatternRule();
        Alert alert = Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .rule(rule)
                .severity(Severity.MEDIUM)
                .status(AlertStatus.OPEN)
                .message("Alert with rule")
                .triggeredAt(Instant.now())
                .build();

        Map<String, String> params = Map.of("url", "https://api.example.com/webhook");

        webhookChannel.send(alert, params);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<String>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(anyString(), captor.capture(), eq(String.class));

        String payload = captor.getValue().getBody();
        assertThat(payload).contains(rule.getId().toString());
        assertThat(payload).contains(rule.getName());
        assertThat(payload).contains("PATTERN");
    }

    @Test
    void shouldHandleAlertWithoutRule() {
        Alert alert = Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .severity(Severity.LOW)
                .status(AlertStatus.OPEN)
                .message("Alert without rule")
                .triggeredAt(Instant.now())
                .build();

        Map<String, String> params = Map.of("url", "https://api.example.com/webhook");

        webhookChannel.send(alert, params);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<String>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(anyString(), captor.capture(), eq(String.class));

        String payload = captor.getValue().getBody();
        // When there's no rule, the payload should not contain a "rule" object with name/type
        assertThat(payload).doesNotContain("\"name\":");
    }

    @Test
    void shouldAddCustomHeaders() {
        Alert alert = createAlertWithRule();
        Map<String, String> params = Map.of(
                "url", "https://api.example.com/webhook",
                "headers", "Authorization:Bearer token123;X-Custom:value"
        );

        webhookChannel.send(alert, params);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<String>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(anyString(), captor.capture(), eq(String.class));

        assertThat(captor.getValue().getHeaders().get("Authorization"))
                .contains("Bearer token123");
        assertThat(captor.getValue().getHeaders().get("X-Custom"))
                .contains("value");
    }

    @Test
    void shouldSetContentTypeToJson() {
        Alert alert = createAlertWithRule();
        Map<String, String> params = Map.of("url", "https://api.example.com/webhook");

        webhookChannel.send(alert, params);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<String>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(anyString(), captor.capture(), eq(String.class));

        assertThat(captor.getValue().getHeaders().getContentType().toString())
                .contains("application/json");
    }

    @Test
    void shouldHandleRestTemplateException() {
        Alert alert = createAlertWithRule();
        Map<String, String> params = Map.of("url", "https://api.example.com/webhook");

        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Connection failed"));

        assertThatCode(() -> webhookChannel.send(alert, params))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldHandleInvalidCustomHeaderFormat() {
        Alert alert = createAlertWithRule();
        Map<String, String> params = Map.of(
                "url", "https://api.example.com/webhook",
                "headers", "InvalidHeader"
        );

        webhookChannel.send(alert, params);

        verify(restTemplate).postForObject(anyString(), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void shouldHandleBlankCustomHeaders() {
        Alert alert = createAlertWithRule();
        Map<String, String> params = Map.of(
                "url", "https://api.example.com/webhook",
                "headers", "   "
        );

        webhookChannel.send(alert, params);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<String>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(anyString(), captor.capture(), eq(String.class));

        // Only Content-Type should be set
        assertThat(captor.getValue().getHeaders().size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void shouldIncludeTriggeringEventIds() {
        UUID eventId = UUID.randomUUID();
        Alert alert = Alert.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .severity(Severity.HIGH)
                .status(AlertStatus.OPEN)
                .message("Alert with events")
                .triggeringEventIds(List.of(eventId))
                .triggeredAt(Instant.now())
                .build();

        Map<String, String> params = Map.of("url", "https://api.example.com/webhook");

        webhookChannel.send(alert, params);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<String>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(anyString(), captor.capture(), eq(String.class));

        String payload = captor.getValue().getBody();
        assertThat(payload).contains(eventId.toString());
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
