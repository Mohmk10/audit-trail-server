package com.mohmk10.audittrail.admin.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class SystemHealth {
    private HealthStatus overallStatus;
    private List<ComponentHealth> components;
    private String version;
    private Instant startTime;
    private long uptimeSeconds;
    private Map<String, Object> metrics;
    private Instant timestamp;

    public SystemHealth() {
    }

    private SystemHealth(Builder builder) {
        this.overallStatus = builder.overallStatus;
        this.components = builder.components;
        this.version = builder.version;
        this.startTime = builder.startTime;
        this.uptimeSeconds = builder.uptimeSeconds;
        this.metrics = builder.metrics;
        this.timestamp = builder.timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public HealthStatus getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(HealthStatus overallStatus) {
        this.overallStatus = overallStatus;
    }

    public List<ComponentHealth> getComponents() {
        return components;
    }

    public void setComponents(List<ComponentHealth> components) {
        this.components = components;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public long getUptimeSeconds() {
        return uptimeSeconds;
    }

    public void setUptimeSeconds(long uptimeSeconds) {
        this.uptimeSeconds = uptimeSeconds;
    }

    public Map<String, Object> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, Object> metrics) {
        this.metrics = metrics;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public static class Builder {
        private HealthStatus overallStatus;
        private List<ComponentHealth> components;
        private String version;
        private Instant startTime;
        private long uptimeSeconds;
        private Map<String, Object> metrics;
        private Instant timestamp;

        public Builder overallStatus(HealthStatus overallStatus) {
            this.overallStatus = overallStatus;
            return this;
        }

        public Builder components(List<ComponentHealth> components) {
            this.components = components;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder startTime(Instant startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder uptimeSeconds(long uptimeSeconds) {
            this.uptimeSeconds = uptimeSeconds;
            return this;
        }

        public Builder metrics(Map<String, Object> metrics) {
            this.metrics = metrics;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public SystemHealth build() {
            return new SystemHealth(this);
        }
    }
}
