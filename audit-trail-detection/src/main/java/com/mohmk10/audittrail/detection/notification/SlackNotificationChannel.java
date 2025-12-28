package com.mohmk10.audittrail.detection.notification;

import com.mohmk10.audittrail.detection.domain.Alert;
import com.mohmk10.audittrail.detection.domain.Severity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class SlackNotificationChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(SlackNotificationChannel.class);

    private final RestTemplate restTemplate;

    public SlackNotificationChannel(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getChannelType() {
        return "SLACK";
    }

    @Override
    public void send(Alert alert, Map<String, String> parameters) {
        if (parameters == null || !parameters.containsKey("webhookUrl")) {
            log.warn("No Slack webhook URL configured for alert {}", alert.getId());
            return;
        }

        String webhookUrl = parameters.get("webhookUrl");
        String payload = buildSlackPayload(alert);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(payload, headers);
            restTemplate.postForObject(webhookUrl, request, String.class);

            log.info("Slack notification sent for alert {}", alert.getId());
        } catch (Exception e) {
            log.error("Failed to send Slack notification for alert {}: {}", alert.getId(), e.getMessage());
        }
    }

    private String buildSlackPayload(Alert alert) {
        String emoji = getEmojiForSeverity(alert.getSeverity());
        String color = getColorForSeverity(alert.getSeverity());

        String ruleName = alert.getRule() != null ? alert.getRule().getName() : "Unknown Rule";

        return String.format("""
            {
                "attachments": [{
                    "color": "%s",
                    "blocks": [
                        {
                            "type": "header",
                            "text": {
                                "type": "plain_text",
                                "text": "%s Alert: %s"
                            }
                        },
                        {
                            "type": "section",
                            "fields": [
                                {"type": "mrkdwn", "text": "*Severity:*\\n%s"},
                                {"type": "mrkdwn", "text": "*Status:*\\n%s"},
                                {"type": "mrkdwn", "text": "*Tenant:*\\n%s"},
                                {"type": "mrkdwn", "text": "*Triggered:*\\n%s"}
                            ]
                        },
                        {
                            "type": "section",
                            "text": {
                                "type": "mrkdwn",
                                "text": "*Message:*\\n%s"
                            }
                        }
                    ]
                }]
            }
            """,
                color,
                emoji, ruleName,
                alert.getSeverity(),
                alert.getStatus(),
                alert.getTenantId(),
                alert.getTriggeredAt(),
                escapeJson(alert.getMessage())
        );
    }

    private String getEmojiForSeverity(Severity severity) {
        return switch (severity) {
            case CRITICAL -> "\uD83D\uDED1";
            case HIGH -> "\uD83D\uDD34";
            case MEDIUM -> "\uD83D\uDFE0";
            case LOW -> "\uD83D\uDFE1";
        };
    }

    private String getColorForSeverity(Severity severity) {
        return switch (severity) {
            case CRITICAL -> "#8B0000";
            case HIGH -> "#FF0000";
            case MEDIUM -> "#FFA500";
            case LOW -> "#FFFF00";
        };
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
