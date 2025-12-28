package com.mohmk10.audittrail.detection.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mohmk10.audittrail.detection.domain.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class WebhookNotificationChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(WebhookNotificationChannel.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WebhookNotificationChannel(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public String getChannelType() {
        return "WEBHOOK";
    }

    @Override
    public void send(Alert alert, Map<String, String> parameters) {
        if (parameters == null || !parameters.containsKey("url")) {
            log.warn("No webhook URL configured for alert {}", alert.getId());
            return;
        }

        String url = parameters.get("url");
        String customHeaders = parameters.get("headers");

        try {
            Map<String, Object> payload = buildPayload(alert);
            String jsonPayload = objectMapper.writeValueAsString(payload);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            if (customHeaders != null && !customHeaders.isBlank()) {
                parseCustomHeaders(customHeaders, headers);
            }

            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);
            restTemplate.postForObject(url, request, String.class);

            log.info("Webhook notification sent for alert {} to {}", alert.getId(), url);
        } catch (Exception e) {
            log.error("Failed to send webhook notification for alert {}: {}", alert.getId(), e.getMessage());
        }
    }

    private Map<String, Object> buildPayload(Alert alert) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", alert.getId());
        payload.put("tenantId", alert.getTenantId());
        payload.put("severity", alert.getSeverity());
        payload.put("status", alert.getStatus());
        payload.put("message", alert.getMessage());
        payload.put("triggeredAt", alert.getTriggeredAt());
        payload.put("triggeringEventIds", alert.getTriggeringEventIds());

        if (alert.getRule() != null) {
            Map<String, Object> ruleInfo = new HashMap<>();
            ruleInfo.put("id", alert.getRule().getId());
            ruleInfo.put("name", alert.getRule().getName());
            ruleInfo.put("type", alert.getRule().getType());
            payload.put("rule", ruleInfo);
        }

        return payload;
    }

    private void parseCustomHeaders(String headersString, HttpHeaders headers) {
        String[] pairs = headersString.split(";");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                headers.add(keyValue[0].trim(), keyValue[1].trim());
            }
        }
    }
}
