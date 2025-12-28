package com.mohmk10.audittrail.admin.domain;

import java.time.Instant;
import java.util.Map;

public class ComponentHealth {
    private String name;
    private HealthStatus status;
    private String message;
    private long responseTimeMs;
    private Map<String, Object> details;
    private Instant lastChecked;

    public ComponentHealth() {
    }

    private ComponentHealth(Builder builder) {
        this.name = builder.name;
        this.status = builder.status;
        this.message = builder.message;
        this.responseTimeMs = builder.responseTimeMs;
        this.details = builder.details;
        this.lastChecked = builder.lastChecked;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ComponentHealth healthy(String name) {
        return builder()
                .name(name)
                .status(HealthStatus.HEALTHY)
                .lastChecked(Instant.now())
                .build();
    }

    public static ComponentHealth unhealthy(String name, String message) {
        return builder()
                .name(name)
                .status(HealthStatus.UNHEALTHY)
                .message(message)
                .lastChecked(Instant.now())
                .build();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HealthStatus getStatus() {
        return status;
    }

    public void setStatus(HealthStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public Instant getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(Instant lastChecked) {
        this.lastChecked = lastChecked;
    }

    public static class Builder {
        private String name;
        private HealthStatus status;
        private String message;
        private long responseTimeMs;
        private Map<String, Object> details;
        private Instant lastChecked;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder status(HealthStatus status) {
            this.status = status;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder responseTimeMs(long responseTimeMs) {
            this.responseTimeMs = responseTimeMs;
            return this;
        }

        public Builder details(Map<String, Object> details) {
            this.details = details;
            return this;
        }

        public Builder lastChecked(Instant lastChecked) {
            this.lastChecked = lastChecked;
            return this;
        }

        public ComponentHealth build() {
            return new ComponentHealth(this);
        }
    }
}
