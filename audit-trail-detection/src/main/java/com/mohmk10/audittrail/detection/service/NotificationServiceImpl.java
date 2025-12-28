package com.mohmk10.audittrail.detection.service;

import com.mohmk10.audittrail.detection.domain.Alert;
import com.mohmk10.audittrail.detection.domain.RuleAction;
import com.mohmk10.audittrail.detection.notification.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final Map<String, NotificationChannel> channels;

    public NotificationServiceImpl(List<NotificationChannel> channelList) {
        this.channels = channelList.stream()
                .collect(Collectors.toMap(
                        NotificationChannel::getChannelType,
                        Function.identity()
                ));
        log.info("Registered {} notification channels: {}",
                channels.size(), channels.keySet());
    }

    @Override
    @Async("detectionTaskExecutor")
    public void notify(Alert alert) {
        if (alert.getRule() == null || alert.getRule().getAction() == null) {
            log.debug("No action configured for alert {}", alert.getId());
            return;
        }

        RuleAction action = alert.getRule().getAction();
        List<String> channelTypes = action.getNotificationChannels();

        if (channelTypes == null || channelTypes.isEmpty()) {
            log.debug("No notification channels configured for alert {}", alert.getId());
            return;
        }

        Map<String, String> parameters = action.getParameters() != null
                ? action.getParameters()
                : Map.of();

        for (String channelType : channelTypes) {
            String type = channelType.toUpperCase();
            NotificationChannel channel = channels.get(type);

            if (channel == null) {
                log.warn("Unknown notification channel: {}", channelType);
                continue;
            }

            try {
                channel.send(alert, parameters);
            } catch (Exception e) {
                log.error("Failed to send notification via {}: {}", channelType, e.getMessage());
            }
        }
    }
}
