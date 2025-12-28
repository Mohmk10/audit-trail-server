package com.mohmk10.audittrail.detection.notification;

import com.mohmk10.audittrail.detection.domain.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LogNotificationChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(LogNotificationChannel.class);

    @Override
    public String getChannelType() {
        return "LOG";
    }

    @Override
    public void send(Alert alert, Map<String, String> parameters) {
        String ruleName = alert.getRule() != null ? alert.getRule().getName() : "Unknown Rule";

        switch (alert.getSeverity()) {
            case CRITICAL, HIGH -> log.error(
                    "ALERT [{}] Rule: {} - Tenant: {} - Message: {} - Events: {}",
                    alert.getSeverity(),
                    ruleName,
                    alert.getTenantId(),
                    alert.getMessage(),
                    alert.getTriggeringEventIds() != null ? alert.getTriggeringEventIds().size() : 0
            );
            case MEDIUM -> log.warn(
                    "ALERT [{}] Rule: {} - Tenant: {} - Message: {}",
                    alert.getSeverity(),
                    ruleName,
                    alert.getTenantId(),
                    alert.getMessage()
            );
            case LOW -> log.info(
                    "ALERT [{}] Rule: {} - Tenant: {} - Message: {}",
                    alert.getSeverity(),
                    ruleName,
                    alert.getTenantId(),
                    alert.getMessage()
            );
        }
    }
}
