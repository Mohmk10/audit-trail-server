package com.mohmk10.audittrail.detection.listener;

import com.mohmk10.audittrail.core.event.EventStoredEvent;
import com.mohmk10.audittrail.detection.domain.Alert;
import com.mohmk10.audittrail.detection.service.AlertService;
import com.mohmk10.audittrail.detection.service.NotificationService;
import com.mohmk10.audittrail.detection.service.RuleEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DetectionEventListener {

    private static final Logger log = LoggerFactory.getLogger(DetectionEventListener.class);

    private final RuleEngine ruleEngine;
    private final AlertService alertService;
    private final NotificationService notificationService;

    public DetectionEventListener(
            RuleEngine ruleEngine,
            AlertService alertService,
            NotificationService notificationService) {
        this.ruleEngine = ruleEngine;
        this.alertService = alertService;
        this.notificationService = notificationService;
    }

    @EventListener
    @Async
    public void onEventStored(EventStoredEvent event) {
        try {
            List<Alert> alerts = ruleEngine.evaluate(event.getEvent());

            for (Alert alert : alerts) {
                Alert saved = alertService.create(alert);
                notificationService.notify(saved);
                log.debug("Created and notified alert {} for event {}",
                        saved.getId(), event.getEvent().id());
            }

            if (!alerts.isEmpty()) {
                log.info("Generated {} alert(s) for event {} in tenant {}",
                        alerts.size(),
                        event.getEvent().id(),
                        event.getEvent().metadata().tenantId());
            }
        } catch (Exception e) {
            log.error("Error processing event {} for detection: {}",
                    event.getEvent().id(), e.getMessage());
        }
    }
}
