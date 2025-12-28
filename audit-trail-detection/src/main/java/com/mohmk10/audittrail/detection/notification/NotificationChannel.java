package com.mohmk10.audittrail.detection.notification;

import com.mohmk10.audittrail.detection.domain.Alert;

import java.util.Map;

public interface NotificationChannel {

    String getChannelType();

    void send(Alert alert, Map<String, String> parameters);
}
