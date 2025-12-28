package com.mohmk10.audittrail.detection.notification;

import com.mohmk10.audittrail.detection.domain.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EmailNotificationChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationChannel.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailNotificationChannel(
            JavaMailSender mailSender,
            @Value("${detection.notification.email.from:alerts@audittrail.local}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public String getChannelType() {
        return "EMAIL";
    }

    @Override
    public void send(Alert alert, Map<String, String> parameters) {
        if (parameters == null || !parameters.containsKey("to")) {
            log.warn("No email recipient configured for alert {}", alert.getId());
            return;
        }

        String to = parameters.get("to");
        String cc = parameters.get("cc");

        String subject = String.format("[%s] Alert: %s",
                alert.getSeverity(),
                alert.getRule() != null ? alert.getRule().getName() : "Unknown Rule");

        String body = buildEmailBody(alert);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            if (cc != null && !cc.isBlank()) {
                message.setCc(cc.split(","));
            }
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email notification sent for alert {} to {}", alert.getId(), to);
        } catch (Exception e) {
            log.error("Failed to send email notification for alert {}: {}", alert.getId(), e.getMessage());
        }
    }

    private String buildEmailBody(Alert alert) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== AUDIT TRAIL ALERT ===\n\n");

        sb.append("Alert ID: ").append(alert.getId()).append("\n");
        sb.append("Severity: ").append(alert.getSeverity()).append("\n");
        sb.append("Status: ").append(alert.getStatus()).append("\n");
        sb.append("Triggered At: ").append(alert.getTriggeredAt()).append("\n");
        sb.append("Tenant: ").append(alert.getTenantId()).append("\n\n");

        if (alert.getRule() != null) {
            sb.append("Rule: ").append(alert.getRule().getName()).append("\n");
            if (alert.getRule().getDescription() != null) {
                sb.append("Description: ").append(alert.getRule().getDescription()).append("\n");
            }
        }

        sb.append("\nMessage: ").append(alert.getMessage()).append("\n");

        if (alert.getTriggeringEventIds() != null && !alert.getTriggeringEventIds().isEmpty()) {
            sb.append("\nTriggering Events: ").append(alert.getTriggeringEventIds().size()).append("\n");
            for (var eventId : alert.getTriggeringEventIds()) {
                sb.append("  - ").append(eventId).append("\n");
            }
        }

        sb.append("\n---\n");
        sb.append("Please investigate and take appropriate action.\n");
        sb.append("Audit Trail System\n");

        return sb.toString();
    }
}
