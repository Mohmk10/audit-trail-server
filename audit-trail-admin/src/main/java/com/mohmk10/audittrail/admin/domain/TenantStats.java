package com.mohmk10.audittrail.admin.domain;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class TenantStats {
    private UUID tenantId;
    private String tenantName;
    private long totalEvents;
    private long eventsToday;
    private long eventsThisMonth;
    private long storageUsedBytes;
    private int activeSources;
    private int activeUsers;
    private int activeApiKeys;
    private int alertsTriggeredToday;
    private Map<String, Long> eventsByType;
    private Map<String, Long> eventsBySource;
    private double averageEventsPerDay;
    private Instant lastEventAt;
    private Instant calculatedAt;

    public TenantStats() {
    }

    private TenantStats(Builder builder) {
        this.tenantId = builder.tenantId;
        this.tenantName = builder.tenantName;
        this.totalEvents = builder.totalEvents;
        this.eventsToday = builder.eventsToday;
        this.eventsThisMonth = builder.eventsThisMonth;
        this.storageUsedBytes = builder.storageUsedBytes;
        this.activeSources = builder.activeSources;
        this.activeUsers = builder.activeUsers;
        this.activeApiKeys = builder.activeApiKeys;
        this.alertsTriggeredToday = builder.alertsTriggeredToday;
        this.eventsByType = builder.eventsByType;
        this.eventsBySource = builder.eventsBySource;
        this.averageEventsPerDay = builder.averageEventsPerDay;
        this.lastEventAt = builder.lastEventAt;
        this.calculatedAt = builder.calculatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public long getTotalEvents() {
        return totalEvents;
    }

    public void setTotalEvents(long totalEvents) {
        this.totalEvents = totalEvents;
    }

    public long getEventsToday() {
        return eventsToday;
    }

    public void setEventsToday(long eventsToday) {
        this.eventsToday = eventsToday;
    }

    public long getEventsThisMonth() {
        return eventsThisMonth;
    }

    public void setEventsThisMonth(long eventsThisMonth) {
        this.eventsThisMonth = eventsThisMonth;
    }

    public long getStorageUsedBytes() {
        return storageUsedBytes;
    }

    public void setStorageUsedBytes(long storageUsedBytes) {
        this.storageUsedBytes = storageUsedBytes;
    }

    public int getActiveSources() {
        return activeSources;
    }

    public void setActiveSources(int activeSources) {
        this.activeSources = activeSources;
    }

    public int getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(int activeUsers) {
        this.activeUsers = activeUsers;
    }

    public int getActiveApiKeys() {
        return activeApiKeys;
    }

    public void setActiveApiKeys(int activeApiKeys) {
        this.activeApiKeys = activeApiKeys;
    }

    public int getAlertsTriggeredToday() {
        return alertsTriggeredToday;
    }

    public void setAlertsTriggeredToday(int alertsTriggeredToday) {
        this.alertsTriggeredToday = alertsTriggeredToday;
    }

    public Map<String, Long> getEventsByType() {
        return eventsByType;
    }

    public void setEventsByType(Map<String, Long> eventsByType) {
        this.eventsByType = eventsByType;
    }

    public Map<String, Long> getEventsBySource() {
        return eventsBySource;
    }

    public void setEventsBySource(Map<String, Long> eventsBySource) {
        this.eventsBySource = eventsBySource;
    }

    public double getAverageEventsPerDay() {
        return averageEventsPerDay;
    }

    public void setAverageEventsPerDay(double averageEventsPerDay) {
        this.averageEventsPerDay = averageEventsPerDay;
    }

    public Instant getLastEventAt() {
        return lastEventAt;
    }

    public void setLastEventAt(Instant lastEventAt) {
        this.lastEventAt = lastEventAt;
    }

    public Instant getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(Instant calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public static class Builder {
        private UUID tenantId;
        private String tenantName;
        private long totalEvents;
        private long eventsToday;
        private long eventsThisMonth;
        private long storageUsedBytes;
        private int activeSources;
        private int activeUsers;
        private int activeApiKeys;
        private int alertsTriggeredToday;
        private Map<String, Long> eventsByType;
        private Map<String, Long> eventsBySource;
        private double averageEventsPerDay;
        private Instant lastEventAt;
        private Instant calculatedAt;

        public Builder tenantId(UUID tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder tenantName(String tenantName) {
            this.tenantName = tenantName;
            return this;
        }

        public Builder totalEvents(long totalEvents) {
            this.totalEvents = totalEvents;
            return this;
        }

        public Builder eventsToday(long eventsToday) {
            this.eventsToday = eventsToday;
            return this;
        }

        public Builder eventsThisMonth(long eventsThisMonth) {
            this.eventsThisMonth = eventsThisMonth;
            return this;
        }

        public Builder storageUsedBytes(long storageUsedBytes) {
            this.storageUsedBytes = storageUsedBytes;
            return this;
        }

        public Builder activeSources(int activeSources) {
            this.activeSources = activeSources;
            return this;
        }

        public Builder activeUsers(int activeUsers) {
            this.activeUsers = activeUsers;
            return this;
        }

        public Builder activeApiKeys(int activeApiKeys) {
            this.activeApiKeys = activeApiKeys;
            return this;
        }

        public Builder alertsTriggeredToday(int alertsTriggeredToday) {
            this.alertsTriggeredToday = alertsTriggeredToday;
            return this;
        }

        public Builder eventsByType(Map<String, Long> eventsByType) {
            this.eventsByType = eventsByType;
            return this;
        }

        public Builder eventsBySource(Map<String, Long> eventsBySource) {
            this.eventsBySource = eventsBySource;
            return this;
        }

        public Builder averageEventsPerDay(double averageEventsPerDay) {
            this.averageEventsPerDay = averageEventsPerDay;
            return this;
        }

        public Builder lastEventAt(Instant lastEventAt) {
            this.lastEventAt = lastEventAt;
            return this;
        }

        public Builder calculatedAt(Instant calculatedAt) {
            this.calculatedAt = calculatedAt;
            return this;
        }

        public TenantStats build() {
            return new TenantStats(this);
        }
    }
}
