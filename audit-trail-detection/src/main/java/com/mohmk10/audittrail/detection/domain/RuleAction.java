package com.mohmk10.audittrail.detection.domain;

import java.util.List;
import java.util.Map;

public class RuleAction {

    private AlertType alertType;
    private List<String> notificationChannels;
    private Map<String, String> parameters;

    public RuleAction() {
    }

    public RuleAction(AlertType alertType, List<String> notificationChannels) {
        this.alertType = alertType;
        this.notificationChannels = notificationChannels;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public List<String> getNotificationChannels() {
        return notificationChannels;
    }

    public void setNotificationChannels(List<String> notificationChannels) {
        this.notificationChannels = notificationChannels;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
